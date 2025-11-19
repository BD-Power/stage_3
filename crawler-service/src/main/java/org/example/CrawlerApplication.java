package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

@SpringBootApplication
public class CrawlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
    @Bean
    public CommandLineRunner run(CrawlerService service, DocumentProducer producer) {
        return args -> {
            if (args.length == 0) return;
            String folder = args[0];
            for (Path p : service.listDocuments(folder)) {
                String id = "doc_" + System.currentTimeMillis() + "_" + p.getFileName();
                producer.sendDocumentIngested(id, p.toAbsolutePath().toString());
            }
        };
    }

}
