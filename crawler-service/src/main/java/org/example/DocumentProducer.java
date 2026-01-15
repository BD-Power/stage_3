    package org.example;

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

        public void sendDocumentReady(String documentId, String ruta, String hash) {
            try {
                Map<String, String> payload = new HashMap<>();
                payload.put("type", "document_ready");
                payload.put("documentId", documentId);
                payload.put("ruta", ruta);
                payload.put("hash", hash);

                String json = mapper.writeValueAsString(payload);

                jmsTemplate.convertAndSend("document.queue", json, message -> {
                    message.setStringProperty("messageType", "document_ready");
                    message.setJMSCorrelationID(documentId);
                    return message;
                });

                System.out.println("Enviado mensaje document_ready id=" + documentId);
            } catch (Exception e) {
                throw new RuntimeException("Error enviando mensaje JMS: " + e.getMessage(), e);
            }
        }
    }

