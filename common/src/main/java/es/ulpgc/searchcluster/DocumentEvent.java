package es.ulpgc.searchcluster;

import java.io.Serializable;

public class DocumentEvent implements Serializable {

    private String docId;
    private String path;

    public DocumentEvent() {}

    public DocumentEvent(String docId, String path) {
        this.docId = docId;
        this.path = path;
    }

    public String getDocId() {
        return docId;
    }

    public String getPath() {
        return path;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "DocumentEvent{docId='" + docId + "', path='" + path + "'}";
    }
}
