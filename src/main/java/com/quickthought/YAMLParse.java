package com.quickthought;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
public class YAMLParse {
    
    public String serialize (Note note) {
        Map<String, Object> yamlMap = new LinkedHashMap<>();
        yamlMap.put("id", note.getId().toString());
        yamlMap.put("title", note.getTitle());
        yamlMap.put("tags", note.getTags());
        yamlMap.put("created_at", note.getCreatedAt().toString());
        yamlMap.put("updated_at", note.getUpdatedAt().toString());


        Yaml yaml = new Yaml();
        StringWriter writer = new StringWriter();

        
        writer.write("---\n");
        yaml.dump(yamlMap, writer);
        writer.write("---\n");
        writer.write(note.getContent());

        return writer.toString();
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

        return new Note(java.util.UUID.fromString(id), title, content, tags, createdAt, updatedAt);
    }
}
