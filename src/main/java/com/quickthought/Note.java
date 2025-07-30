package com.quickthought;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Note {
    private UUID id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Note(UUID id, String title, String content, List<String> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    //Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void SetContent(String content) {
        this.content = content;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setUpdatedAt(LocalDateTime updateAt) {
        this.updatedAt = updatedAt;
    }
}
