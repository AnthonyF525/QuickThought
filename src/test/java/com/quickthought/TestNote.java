package com.quickthought;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestNote {
    @Test
    void testNoteCreation() {
        UUID id = UUID.randomUUID();
        String title = "My First Note";
        String content = "The note content";
        List<String> tags = List.of("java", "cli");
        LocalDateTime now = LocalDateTime.now();

        Note note = new Note(id, title, content, tags, now, now);

        assertEquals(id, note.getId());
        assertEquals(title, note.getTitle());
        assertEquals(content, note.getContent());
        assertEquals(tags, note.getTags());
        assertEquals(now, note.getCreatedAt());
        assertEquals(now, note.getUpdatedAt());
    }
}
