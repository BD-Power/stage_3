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

    public CrawlController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/crawl")
    public ResponseEntity<String> crawl(@RequestParam("folder") String folder) {
        try {
            // Esto lista documentos y ENVÍA los eventos a ActiveMQ desde el service
            List<Path> files = crawlerService.listDocuments(folder);

            return ResponseEntity.ok("Enqueued " + files.size() + " files");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

