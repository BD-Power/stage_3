package org.example;

import es.ulpgc.searchcluster.DocumentEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/crawler")
public class CrawlController {

    private final CrawlerService crawlerService;
    private final DocumentProducer producer;

    public CrawlController(CrawlerService crawlerService, DocumentProducer producer) {
        this.crawlerService = crawlerService;
        this.producer = producer;
    }

    @PostMapping("/crawl")
    public ResponseEntity<String> crawl(@RequestParam("folder") String folder) {
        try {
            List<Path> files = crawlerService.listDocuments(folder);
            for (Path p : files) {
                // crea id simple y envía evento con path absoluto
                String id = "doc_" + System.currentTimeMillis() + "_" + p.getFileName();
                producer.sendDocumentIngested(id, p.toAbsolutePath().toString());
            }
            return ResponseEntity.ok("Enqueued " + files.size() + " files");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
