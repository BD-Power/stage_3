package org.example;

import org.example.DocumentProducer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class CrawlerService {

    private final DocumentProducer producer;

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

                    // Crear ID único para el documento
                    String documentId = UUID.randomUUID().toString();
                    // Calcular hash SHA-256
                    String hash = computeHash(file);
                    // Leemos aquí el contenido
                    String content = Files.readString(file);
                    // Enviamos contenido
                    producer.sendDocumentReady(documentId, content, hash);

                    count++;
                }
            }
        }

        return count;
    }

    private String computeHash(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] content = Files.readAllBytes(file);
            byte[] hash = digest.digest(content);

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating hash: " + e.getMessage(), e);
        }
    }
}
