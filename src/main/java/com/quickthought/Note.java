package com.quickthought;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

public class Note {
    private UUID id;
    private String title;
    private String content;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    public Note(String title, String content, List<String> tags) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>(tags != null ? tags : new ArrayList<>());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    
    public Note(UUID id, String title, String content, List<String> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;  // Use the provided ID, don't generate new one
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>(tags != null ? tags : new ArrayList<>());
        this.createdAt = createdAt;  // Use provided timestamp
        this.updatedAt = updatedAt;  // Use provided timestamp
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
        return new ArrayList<>(tags);
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
        this.updatedAt = LocalDateTime.now();
    }

    public void setContent(String content) {  
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setTags(List<String> tags) {
        this.tags = new ArrayList<>(tags != null ? tags : new ArrayList<>());
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {  
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("Note{id=%s, title='%s', tags=%s}", 
            id.toString().substring(0, 8), title, tags);
    }
}
