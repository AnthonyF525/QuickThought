package com.quickthought;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class CLIHandler {

    private final String workingDirectory;
    private final NoteManager noteManager;

    public CLIHandler(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.noteManager = new NoteManager(workingDirectory);
    }

    public ParsedCommand parseArgs(String[] args) {
        if(args.length == 0) {
            return new ParsedCommand("help", new HashMap<>());
        }

        String command = args[0];
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i +1].startsWith("--")) {
                    options.put(key,args[i + 1]);
                    i++;
                } else {
                    options.put(key, "true");
                }
            }
        }
        return new ParsedCommand(command, options);
    }

    public boolean execute(ParsedCommand parsedCommand) {
        switch (parsedCommand.getCommand()) {
            case "create":
                return executeCreate(parsedCommand.getOptions());
            case "list":
                return executeList(parsedCommand.getOptions());
            case "read":
                return executeRead(parsedCommand.getOptions());
            case "search":
                return executeSearch(parsedCommand.getOptions());
            case "stats":
                return executeStats(parsedCommand.getOptions());
            case "import":  // Add this line
                return executeImport(parsedCommand.getOptions());
            case "help":
            case "--help":
                return executeHelp();
            default:
                System.out.println("unknown line:" + parsedCommand.getCommand());
                return false;
        }
    }

    public boolean handleCommand(String[] args) {
        try {
            ParsedCommand parsed = parseArgs(args);
            return execute(parsed);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean executeCreate(Map<String, String> options) {
        String title = options.get("title");
        String content = options.getOrDefault("content", "");
        String tags = options.getOrDefault("tags", "");

        if (title == null) {
            System.out.println("Error: --title needed!");
            return false;
        }

        List<String> tagList = tags.isEmpty() ?
            new ArrayList<>():
            Arrays.asList(tags.split(","));

        Note note = noteManager.createNote(title, content, tagList);
        System.out.println("Thought created successfully with ID: " + note.getId());
        return true;
    }

    private boolean executeList(Map<String, String> options) {
        List<Note> notes = noteManager.getAllNotes();
        
        if (notes.isEmpty()) {
            System.out.println("No notes found.");
            return true;
        }
        
        System.out.println("Found " + notes.size() + " note(s):");
        for (Note note : notes) {
            System.out.println("- [" + note.getId().toString().substring(0, 8) + "] " + note.getTitle());
            if (options.containsKey("verbose")) {
                System.out.println("  Content: " + note.getContent());
                System.out.println("  Tags: " + note.getTags());
                System.out.println("  Created: " + note.getCreatedAt());
            }
        }
        return true;
    }

    private boolean executeRead(Map<String, String> options) {
        String id = options.get("id");
        if (id == null) {
            System.out.println("Error: --id required for read command");
            return false;
        }
        
        try {
            UUID noteId = UUID.fromString(id);
            Note note = noteManager.getNote(noteId);
            
            if (note == null) {
                System.out.println("Note not found with ID: " + id);
                return false;
            }
            
            System.out.println("Title: " + note.getTitle());
            System.out.println("Content: " + note.getContent());
            System.out.println("Tags: " + note.getTags());
            System.out.println("Created: " + note.getCreatedAt());
            System.out.println("Updated: " + note.getUpdatedAt());
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid ID format");
            return false;
        }
        
        return true;
    }

    private boolean executeSearch(Map<String, String> options) {
        String query = options.get("query");
        if (query == null) {
            System.out.println("Error: --query required for search command");
            return false;
        }
        
        List<Note> results = noteManager.searchNotes(query);
        
        if (results.isEmpty()) {
            System.out.println("No notes found matching: " + query);
            return true;
        }
        
        System.out.println("Found " + results.size() + " note(s) matching '" + query + "':");
        for (Note note : results) {
            System.out.println("- [" + note.getId().toString().substring(0, 8) + "] " + note.getTitle());
        }
        
        return true;
    }

    private boolean executeStats(Map<String, String> options) {
        List<Note> notes = noteManager.getAllNotes();
        
        System.out.println("=== QuickThought Statistics ===");
        System.out.println("Total notes: " + notes.size());
        
        if (!notes.isEmpty()) {
            int totalContent = notes.stream()
                .mapToInt(note -> note.getContent().length())
                .sum();
            
            System.out.println("Average content length: " + (totalContent / notes.size()) + " characters");
            
            // Count unique tags
            Set<String> allTags = notes.stream()
                .flatMap(note -> note.getTags().stream())
                .collect(Collectors.toSet());
            
            System.out.println("Unique tags: " + allTags.size());
            System.out.println("Working directory: " + workingDirectory);
        }
        
        return true;
    }

    private boolean executeHelp() {
        System.out.println("QuickThought - A simple note-taking CLI");
        System.out.println();
        System.out.println("Usage: quickthought <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  create --title <title> [--content <content>] [--tags <tag1,tag2>]");
        System.out.println("  list [--verbose]");
        System.out.println("  read --id <note-id>");
        System.out.println("  search --query <search-term>");
        System.out.println("  import --file <path-to-file>");
        System.out.println("  stats");
        System.out.println("  help");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  quickthought create --title \"My Note\" --content \"Note content\"");
        System.out.println("  quickthought list --verbose");
        System.out.println("  quickthought search --query \"important\"");
        
        return true;
    }

    private boolean executeImport(Map<String, String> options) {
        String filePath = options.get("file");
        if (filePath == null) {
            System.out.println("Error: --file required for import");
            System.out.println("Usage: import --file path/to/note.md");
            return false;
        }
    
        try {
            java.nio.file.Path sourceFile = java.nio.file.Paths.get(filePath);
            if (!java.nio.file.Files.exists(sourceFile)) {
                System.out.println("Error: File not found: " + filePath);
                return false;
            }
        
            String content = java.nio.file.Files.readString(sourceFile);
            Note note = noteManager.parseAndCreateNote(content);  // You'll need to add this method to NoteManager
        
            System.out.println("Note imported successfully!");
            System.out.println("Title: " + note.getTitle());
            System.out.println("ID: " + note.getId().toString().substring(0, 8));
            System.out.println("Tags: " + note.getTags());
        
            return true;
        } catch (Exception e) {
            System.out.println("Error importing file: " + e.getMessage());
            return false;
        }
    }
}

class ParsedCommand {
    private final String command;
    private final Map<String, String> options;

    public ParsedCommand(String command, Map<String, String> options) {
        this.command = command;
        this.options = options;
    }

    public String getCommand () { 
        return command; 
    }
    public Map<String, String> getOptions() { 
        return options; 
    }
}