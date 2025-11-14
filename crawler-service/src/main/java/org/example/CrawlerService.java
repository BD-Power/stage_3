package org.example;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CrawlerService {

    public List<Path> listDocuments(String folder) throws Exception {
        return Files.list(Path.of(folder))
                .filter(Files::isRegularFile)
                .toList();
    }
}
