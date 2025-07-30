package com.quickthought;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class YAMLParseTest {

    @Test
    void testSerializeAndParseNote() {
        YAMLHandler handler = new YAMLHandler();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Note original = new Note(id, "Test Note", "This is the body.",
                List.of("java", "yaml"), now, now);

        String serialized = handler.serialize(original);
        Note parsed = handler.parse (serialized);

        assertEquals(original.getId(), parsed.getId());
        assertEquals(original.getTitle(), parsed.getTitle());
        assertEquals(original.getContent(), parsed.getContent());
        assertEquals(original.getTags(), parsed.getTags());
    }
}