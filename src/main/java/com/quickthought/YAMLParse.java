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
        if (parts.length <3) throw new IllegalArgumentException("Invaild note Format");

        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap =yaml.load(new StringReader (parts[1]));

        UUID id =UUID.fromString ((String) yamlMap.get("id"));
        String title = (String) yamlMap.get("title");
        List<String> tags = (List<String>) yamlMap.get("tags");
        LocalDateTime createdAt = LocalDateTime.parse ((String) yamlMap.get("created_at"));
        LocalDateTime updatedAt = LocalDateTime.parse ((String) yamlMap.get("updated_at"));
        String content = parts[2].trim();

        return new Note(id, title, content, tags, createdAt, updatedAt);
    }
}
