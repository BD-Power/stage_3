package es.ulpgc.searchcluster;

import java.io.Serializable;

public class Document implements Serializable {

    private String id;
    private String content;

    public Document() {}

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
