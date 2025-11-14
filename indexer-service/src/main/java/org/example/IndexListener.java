package org.example;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.multimap.MultiMap;
import es.ulpgc.searchcluster.DocumentEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class IndexListener {
    private final HazelcastInstance hz;

    public IndexListener(HazelcastInstance hz) { this.hz = hz; }

    @JmsListener(destination = "documents.ingested")
    public void onMessage(DocumentEvent ev) {
        try {
            String content = Files.readString(Path.of(ev.getPath()));
            indexDocument(ev.getDocId(), content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void indexDocument(String docId, String content) {
        MultiMap<String,String> inverted = hz.getMultiMap("inverted-index");
        String[] tokens = content.toLowerCase().split("\\W+");
        for (String t: tokens) {
            if (t == null || t.isBlank()) continue;
            inverted.put(t, docId);
        }
    }
}
