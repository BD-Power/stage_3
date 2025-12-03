package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class IndexListener {

    private final HazelcastInstance hz;
    private final ObjectMapper mapper = new ObjectMapper();
    private final IMap<String, Boolean> processedDocuments;

    public IndexListener(HazelcastInstance hz) {
        this.hz = hz;

        this.processedDocuments = hz.getMap("processedDocuments");
    }

    @JmsListener(destination = "document.queue")
    public void onMessage(String messageJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(messageJson, Map.class);

            String docId = (String) msg.get("documentId");
            String ruta = (String) msg.get("ruta");


            if (processedDocuments.putIfAbsent(docId, Boolean.TRUE) != null) {
                System.out.println("Duplicado detectado: El ID " + docId + " ya fue procesado. Ignorando.");
                return;
            }


            System.out.println("📩 Mensaje recibido en INDEXER");
            System.out.println(" - ID:   " + docId);
            System.out.println(" - Ruta: " + ruta);

            String content = Files.readString(Path.of(ruta));

            indexDocument(docId, content);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void indexDocument(String docId, String content) {
        MultiMap<String, String> inverted = hz.getMultiMap("inverted-index");

        String[] tokens = content.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            inverted.put(token, docId);
        }

        System.out.println("Documento indexado en MultiMap: " + docId);
    }
}