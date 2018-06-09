package com.jianhua.naio.nio.demo.moniter;

/**
 * @author lijianhua
 */
public class FileEvent {

    private String filename;

    private String kind;

    public FileEvent(String filename, String kind) {
        this.filename = filename;
        this.kind = kind;
    }

    public String getFilename() {
        return filename;
    }

    public String getKind() {
        return kind;
    }
}