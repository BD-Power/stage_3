package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crawler")
public class CrawlController {

    private final CrawlerService crawlerService;

    public CrawlController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/crawl")
    public ResponseEntity<String> crawl(
            @RequestParam(required = false) String folder,
            @RequestParam(required = false) String bookId) {

        try {
            if (bookId != null) {
                // Descargar desde Project Gutenberg
                for (String id : bookId.split(",")) {
                    crawlerService.downloadAndProcessBook(Integer.parseInt(id.trim()));
                }
                return ResponseEntity.ok("Books downloaded and replicated.");
            } else if (folder != null) {
                // Procesar carpeta local
                int count = crawlerService.processFolder(folder);
                return ResponseEntity.ok("Processed " + count + " files.");
            } else {
                return ResponseEntity.badRequest().body("Specify 'folder' or 'bookId'");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/replicate")
    public ResponseEntity<Void> replicate(@RequestBody ReplicationRequest req) {
        crawlerService.storeReplica(req.documentId(), req.content());
        return ResponseEntity.ok().build();
    }
}