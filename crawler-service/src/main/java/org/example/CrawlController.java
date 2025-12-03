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
    public ResponseEntity<String> crawl(@RequestParam("folder") String folder) {
        try {
            int count = crawlerService.processFolder(folder);
            return ResponseEntity.ok("Enqueued " + count + " files");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error: " + e.getMessage());
        }
    }
}
