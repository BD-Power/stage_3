package org.example;

import es.ulpgc.searchcluster.DocumentEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentProducer {
    private final JmsTemplate jms;
    private final String destination = "documents.ingested";

    public DocumentProducer(JmsTemplate jms) { this.jms = jms; }

    public void sendDocumentIngested(String docId, String path) {
        DocumentEvent ev = new DocumentEvent(docId, path);
        jms.convertAndSend(destination, ev);
    }
}
