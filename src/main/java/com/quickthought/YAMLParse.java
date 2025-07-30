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

    public
}
