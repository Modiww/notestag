package com.example.notestag;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
    private final String title;
    private final String description;
    // 0 - не начата, 1 - в процессе, 2 - готова
    public static final int STATUS_NOT_STARTED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_DONE = 2;

    private int status;
    private final List<String> tags;
    private final long createdAt;

    public Task(String title, String description, List<String> tags) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.status = STATUS_NOT_STARTED;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

