package com.quickthought;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class YAMLParse {
    
    public String serialize (Note note) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("---\n");
        yaml.append("id: ").append(note.getId()).append("\n");
        yaml.append("title: \"").append(escapeYaml(note.getTitle())).append("\"\n");
        yaml.append("created: ").append(note.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        yaml.append("updated: ").append(note.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        yaml.append("tags: [").append(String.join(", ", note.getTags())).append("]\n");
        yaml.append("---\n");
        yaml.append(note.getContent());
        return yaml.toString();
    }

    public Note parse (String fileContent) {
        String[] parts = fileContent.split("---\\s*", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid note format");
        }

        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(new StringReader(parts[1]));

        // Handle different YAML formats
        String id;
        if (yamlMap.containsKey("id")) {
            id = (String) yamlMap.get("id");
        } else {
            // Generate new ID for imported notes without one
            id = java.util.UUID.randomUUID().toString();
        }

        // Get title - ADD THIS MISSING DECLARATION
        String title = (String) yamlMap.get("title");
        if (title == null) {
            throw new IllegalArgumentException("Title is required");
        }

        // Handle different tag formats
        List<String> tags = new ArrayList<>();
        Object tagsObj = yamlMap.get("tags");
        if (tagsObj instanceof List) {
            tags = (List<String>) tagsObj;
        } else if (tagsObj instanceof String) {
            // Handle comma-separated tags
            tags = Arrays.asList(((String) tagsObj).split(","));
        }

        // Handle different timestamp formats
        LocalDateTime createdAt;
        LocalDateTime updatedAt;

        try {
            Object createdObj = yamlMap.get("created_at");
            if (createdObj == null) {
                createdObj = yamlMap.get("created"); // Try alternative field name
            }

            if (createdObj != null) {
                createdAt = LocalDateTime.parse(createdObj.toString().replace("Z", ""));
            } else {
                createdAt = LocalDateTime.now();
            }

            Object updatedObj = yamlMap.get("updated_at");
            if (updatedObj == null) {
                updatedObj = yamlMap.get("modified"); // Try alternative field name
            }

            if (updatedObj != null) {
                updatedAt = LocalDateTime.parse(updatedObj.toString().replace("Z", ""));
            } else {
                updatedAt = LocalDateTime.now();
            }

        } catch (Exception e) {
            // Fallback to current time if parsing fails
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }

        String content = parts[2].trim();

        // In YAMLParse.parse() method, make sure you handle the ID correctly:
        String idString = (String) yamlMap.get("id");
        UUID noteId;

        try {
            if (idString != null) {
                noteId = UUID.fromString(idString);
            } else {
                noteId = UUID.randomUUID();
            }
        } catch (IllegalArgumentException e) {
            // If the stored ID isn't a valid UUID, generate a new one
            System.err.println("Warning: Invalid UUID in note, generating new ID");
            noteId = UUID.randomUUID();
        }

        // Create note with the parsed/generated ID
        return new Note(noteId, title, content, tags, createdAt, updatedAt);
    }
    
    public Note deserialize(String yamlContent) {
        return parse(yamlContent); // Use the same logic
    }
    
    private String extractValue(String yamlContent, String key) {
        String[] lines = yamlContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith(key + ":")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return "";
    }
    
    private String escapeYaml(String value) {
        // Basic YAML escaping
        return value.replace("\"", "\\\"");
    }
}
