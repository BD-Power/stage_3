package org.example;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CrawlerService {

    private final DocumentProducer producer;

    public CrawlerService(DocumentProducer producer) {
        this.producer = producer;
    }
    public List<Path> listDocuments(String folder) throws Exception {
    // Ruta real dentro del contenedor: /data/<folder>
    Path folderPath = Path.of("/data", folder);

    List<Path> docs = Files.list(folderPath)
            .filter(Files::isRegularFile)
            .toList();

    // Emitir un mensaje por cada documento
    for (Path file : docs) {
        String id = file.getFileName().toString();
        String path = file.toAbsolutePath().toString();
        producer.sendDocumentIngested(id, path);
    }

    return docs;
}

    
}
