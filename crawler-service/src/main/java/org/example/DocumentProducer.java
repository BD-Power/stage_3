package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DocumentProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public DocumentProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendDocumentReady(String documentId, String location, String content) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("documentId", documentId);
            payload.put("location", location);
            payload.put("content", content);

            String json = mapper.writeValueAsString(payload);

            jmsTemplate.convertAndSend("document.queue", json, message -> {
                message.setJMSCorrelationID(documentId);
                return message;
            });

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando JSON", e);
        }
    }

}
