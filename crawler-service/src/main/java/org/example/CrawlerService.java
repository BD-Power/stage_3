package org.example;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

@Service
public class CrawlerService {

    private static final String DATALAKE_BASE = "/data";
    private static final int REPLICATION_FACTOR = 2;

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

                    int acks = replicateToPeers(documentId, content);
                    if (acks >= REPLICATION_FACTOR) {
                        producer.sendDocumentReady(documentId, crawlerId, content);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    // === NUEVO MÉTODO: descargar desde Project Gutenberg ===
    public void downloadAndProcessBook(int bookId) throws IOException {
        String url = String.format("https://www.gutenberg.org/cache/epub/%d/pg%d.txt", bookId, bookId);
        String content;
        try {
            content = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw new IOException("Failed to download book " + bookId + ": " + e.getMessage(), e);
        }

        if (content == null) {
            throw new IOException("Empty response from Gutenberg for book " + bookId);
        }

        String documentId = "pg" + bookId;
        storeLocal(documentId, content);

        int acks = replicateToPeers(documentId, content);
        if (acks >= REPLICATION_FACTOR) {
            producer.sendDocumentReady(documentId, crawlerId, content);
        }
    }

    // === LÓGICA DE REPLICACIÓN ===
    protected int replicateToPeers(String docId, String content) {
        int acks = 1;
        for (String peer : peers) {
            if (peer == null || peer.trim().isEmpty()) continue;
            try {
                restTemplate.postForEntity(
                        "http://" + peer.trim() + "/crawler/replicate",
                        new ReplicationRequest(docId, content),
                        Void.class
                );
                acks++;
            } catch (Exception e) {
                System.err.println("Replication failed to peer '" + peer + "': " + e.getMessage());
            }
        }
        return acks;
    }

    // === ALMACENAMIENTO LOCAL ===
    protected void storeLocal(String docId, String content) throws IOException {
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

    // === CÁLCULO DE HASH ===
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
            throw new RuntimeException("Hash computation failed", e);
        }
    }


    public static void main(String[] args) {
        try {
            System.out.println(" Iniciando prueba de descarga desde Project Gutenberg...");

            DocumentProducer mockProducer = new DocumentProducer(null) {
                @Override
                public void sendDocumentReady(String documentId, String location, String content) {
                    System.out.println(" Enviado a JMS (simulado): " + documentId);
                }
            };

            // Creamos una instancia con valores de prueba
            CrawlerService service = new CrawlerService(mockProducer) {
                // Sobrescribimos métodos para usar rutas locales
                @Override
                protected void storeLocal(String docId, String content) throws IOException {
                    Path dir = Paths.get("./datalake", "test-crawler");
                    Files.createDirectories(dir);
                    Files.writeString(dir.resolve(docId + ".txt"), content);
                }

                @Override
                protected int replicateToPeers(String docId, String content) {
                    System.out.println(" Replicación omitida en prueba local.");
                    return 1; // solo el nodo local
                }
            };

            // Descargamos Pride and Prejudice (ID 1342)
            service.downloadAndProcessBook(1342);

            // Verificamos
            Path file = Paths.get("./datalake/test-crawler/pg1342.txt");
            if (Files.exists(file)) {
                System.out.println(" Éxito. Libro guardado en: " + file.toAbsolutePath());
                String preview = Files.readString(file).substring(0, Math.min(300, (int) Files.size(file)));
                System.out.println(" Vista previa: " + preview.replace("\n", " ").trim() + "...");
            } else {
                System.err.println(" Error: archivo no encontrado.");
            }

        } catch (Exception e) {
            System.err.println(" Error:");
            e.printStackTrace();
        }
    }
}