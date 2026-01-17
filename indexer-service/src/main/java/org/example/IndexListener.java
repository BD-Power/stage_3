package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class IndexListener {

    private static final String DATALAKE_BASE = "/data";

    private final HazelcastInstance hz;
    private final ObjectMapper mapper = new ObjectMapper();
    private final IMap<String, Boolean> processedDocuments;

    public IndexListener(@Qualifier("indexerHazelcast") HazelcastInstance hz) {
        this.hz = hz;
        this.processedDocuments = hz.getMap("processedDocuments");
    }

    @JmsListener(
            destination = "document.queue",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void onMessage(Message message) {

        try {
            TextMessage textMessage = (TextMessage) message;
            String messageJson = textMessage.getText();

            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(messageJson, Map.class);

            String docId = (String) msg.get("documentId");
            String location = (String) msg.get("location");
            String content = (String) msg.get("content");

            
            if (processedDocuments.putIfAbsent(docId, Boolean.TRUE) != null) {
                message.acknowledge();
                return;
            }

            if (content == null || content.isBlank()) {
                content = readFromDatalake(docId, location);
            }

            indexDocument(docId, content);

            
            message.acknowledge();

            System.out.println("Documento indexado correctamente: " + docId);

        } catch (Exception e) {
            System.err.println("Error procesando mensaje JMS → reentrega");
            e.printStackTrace();
            
        }
    }

    private String readFromDatalake(String docId, String location) throws Exception {

        
        if (location != null) {
            Path primary = Paths.get(DATALAKE_BASE, location, docId + ".txt");
            if (Files.exists(primary)) {
                return Files.readString(primary);
            }
        }

        
        List<String> replicas = List.of("crawler1", "crawler2", "crawler3");
        for (String replica : replicas) {
            Path candidate = Paths.get(DATALAKE_BASE, replica, docId + ".txt");
            if (Files.exists(candidate)) {
                return Files.readString(candidate);
            }
        }

        throw new RuntimeException("Documento " + docId + " no encontrado en el datalake");
    }

    private void indexDocument(String docId, String content) {

        MultiMap<String, String> inverted = hz.getMultiMap("inverted-index");

        String[] tokens = content.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (!token.isBlank()) {
                inverted.put(token, docId);
            }
        }
    }
}
