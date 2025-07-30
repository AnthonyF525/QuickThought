package com.quickthought;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.nio.file.*;
import java.io.*;

public class NoteManager {

    private final String workingDirectory;
    private final Map<UUID, Note> notes;
    private final YAMLParse yamlParser;

    public NoteManager(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.notes = new HashMap<>();
        this.yamlParser = new YAMLParse();

        File dir = new File(workingDirectory);
        if(!dir.exists()) {
            dir.mkdirs();
        }
    }
     
    public Note createNote(String title, String content, List<String> tags) {
        Note note = new Note(title, content, tags);
        notes.put(note.getId(), note);
        

        String yamlContent = yamlParser.serialize(note);
        Path filePath = Paths.get(workingDirectory, note.getId() + ".md");
        
        try {
            Files.write(filePath, yamlContent.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save note", e);
        }
        
        return note;
    }
    
    public List<Note> getAllNotes() {
        try {
            return Files.list(Paths.get(workingDirectory))
                .filter(path -> path.toString().endsWith(".md"))
                .map(this::loadNoteFromFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private Note loadNoteFromFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            return yamlParser.parse(content);
        } catch (IOException e) {
            return null;
        }
    }

    public Note getNote(UUID id) {
        return notes.get(id);
    }

    public boolean deleteNote(UUID id) {
        return notes.remove(id) != null;
    }

    public List<Note> searchNotes(String query) {
        List<Note> results = new ArrayList<>();
        for (Note note : notes.values()) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase()) || note.getContent().toLowerCase().contains(query.toLowerCase())) {
                results.add(note);
            }
        }
        return results;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }
}
