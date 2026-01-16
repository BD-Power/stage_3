package org.example;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import org.springframework.web.client.RestTemplate;
import java.util.List;


@Service
public class CrawlerService {

    private static final String DATALAKE_BASE = "/datalake";
    private static final int REPLICATION_FACTOR = 1;

    private final DocumentProducer producer;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String crawlerId =
            System.getenv().getOrDefault("CRAWLER_ID", "crawler1");

    private final List<String> peers =
            List.of(System.getenv()
                    .getOrDefault("CRAWLER_PEERS", "")
                    .split(","));

    public CrawlerService(DocumentProducer producer) {
        this.producer = producer;
    }

    public int processFolder(String folder) throws IOException {
        Path dir = Paths.get(folder);

        if (!Files.exists(dir)) {
            throw new IOException("Folder does not exist: " + folder);
        }

        int count = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {

                    String content = Files.readString(file);
                    String documentId = computeHash(content);

                    storeLocal(documentId, content);

                    int acks = 1;
                    for (String peer : peers) {
                        if (peer.isBlank()) continue;
                        try {
                            restTemplate.postForEntity(
                                    "http://" + peer + "/crawler/replicate",
                                    new ReplicationRequest(documentId, content),
                                    Void.class
                            );
                            acks++;
                        } catch (Exception ignored) {
                            System.out.println("It could not be replicated in: " + peer);
                        }
                    }

                    if (acks >= REPLICATION_FACTOR) {
                        producer.sendDocumentReady(documentId, crawlerId, content);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void storeLocal(String docId, String content) throws IOException {
        Path dir = Paths.get(DATALAKE_BASE, crawlerId);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(docId + ".txt"), content);
    }

    public void storeReplica(String docId, String content) {
        try {
            storeLocal(docId, content);
        } catch (Exception e) {
            throw new RuntimeException("Error saving replica", e);
        }
    }

    private String computeHash(String content) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

}

