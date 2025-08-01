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
        if (args.length == 0) {
            return new ParsedCommand("help", new HashMap<>());
        }

        String command = args[0].toLowerCase();
        Map<String, String> options = new HashMap<>();
        List<String> positionalArgs = new ArrayList<>();

        // Handle command aliases first
        command = normalizeCommand(command);

        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                // Technical flag format
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    options.put(key, args[i + 1]);
                    i++;
                } else {
                    options.put(key, "true");
                }
            } else {
                positionalArgs.add(args[i]);
            }
        }

        // Handle user-friendly syntax for each command
        handleUserFriendlySyntax(command, options, positionalArgs);

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
            case "import": // Add this line
                return executeImport(parsedCommand.getOptions());
            case "edit": // ← ADD THIS NEW CASE
                return executeEdit(parsedCommand.getOptions());
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

        List<String> tagList = tags.isEmpty() ? new ArrayList<>() : Arrays.asList(tags.split(","));

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
        System.out.println(" Natural Language Commands:");
        System.out.println("  new \"My Title\" \"Content here\"              # Create a new note");
        System.out.println("  create \"My Title\" \"Content here\"           # Same as new");
        System.out.println("  add \"Shopping List\" \"Buy milk, eggs\"       # Same as new");
        System.out.println();
        System.out.println("  open abc123                                  # Edit note by ID");
        System.out.println("  edit \"My Note Title\"                        # Edit note by title");
        System.out.println("  modify abc123                                # Same as edit");
        System.out.println();
        System.out.println("  show abc123                                  # Read note by ID");
        System.out.println("  view \"My Note Title\"                        # Read note by title");
        System.out.println("  display abc123                               # Same as show");
        System.out.println();
        System.out.println("  find \"important stuff\"                      # Search for text");
        System.out.println("  search \"meeting notes\"                     # Same as find");
        System.out.println("  locate \"project ideas\"                     # Same as find");
        System.out.println();
        System.out.println("  list all                                     # List with details");
        System.out.println("  ls verbose                                   # Same as list all");
        System.out.println("  list                                         # Simple list");
        System.out.println();
        System.out.println("  load myfile.md                               # Import a file");
        System.out.println("  import myfile.md                             # Same as load");
        System.out.println();
        System.out.println("  stats                                        # Show statistics");
        System.out.println("  info                                         # Same as stats");
        System.out.println();
        System.out.println("  Technical Commands (still work):");
        System.out.println("  create --title \"Title\" --content \"Content\"");
        System.out.println("  edit --id abc123 | --title \"Note Title\"");
        System.out.println("  read --id abc123");
        System.out.println("  search --query \"text\"");
        System.out.println("  list --verbose");
        System.out.println("  import --file myfile.md");
        System.out.println();
        System.out.println(" Quick Examples:");
        System.out.println("  quickthought new \"Shopping\" \"Buy groceries\"");
        System.out.println("  quickthought open abc123");
        System.out.println("  quickthought find \"shopping\"");
        System.out.println("  quickthought show \"Shopping\"");
        System.out.println("  quickthought list all");
        
        return true;
    }

    private boolean executeImport(Map<String, String> options) {
        String filePath = options.get("file");
        if (filePath == null) {
            System.out.println("Error: --file required for import");
            System.out.println("Usage: import --file path/to/note.md");
            return false;
        }

        System.out.println("DEBUG: Trying to import: " + filePath);

        try {
            java.nio.file.Path sourceFile = java.nio.file.Paths.get(filePath);
            if (!java.nio.file.Files.exists(sourceFile)) {
                System.out.println("Error: File not found: " + filePath);
                return false;
            }

            String content = java.nio.file.Files.readString(sourceFile);
            System.out.println("DEBUG: File content length: " + content.length());
            Note note = noteManager.parseAndCreateNote(content);

            System.out.println("Note imported successfully!");
            System.out.println("Title: " + note.getTitle());
            System.out.println("ID: " + note.getId().toString().substring(0, 8));
            System.out.println("Tags: " + note.getTags());

            return true;
        } catch (Exception e) {
            System.out.println("Error importing file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeEdit(Map<String, String> options) {
        String id = options.get("id");
        String title = options.get("title");

        if (id == null && title == null) {
            System.out.println("Error: Specify either --id or --title");
            System.out.println("Usage:");
            System.out.println("edit --id <note-id>");
            System.out.println("edit --title <note-title>");
            return false;
        }

        try {
            Note noteToEdit = null;

            if (id != null) {
                // Find note by ID
                noteToEdit = findNoteById(id);
            } else {
                // Find note by title
                noteToEdit = findNoteByTitle(title);
            }

            if (noteToEdit == null) {
                System.out.println("Note not found");
                return false;
            }

            // Get the file path
            String filePath = workingDirectory + "/" + noteToEdit.getId() + ".md";

            System.out.println("Opening note for editing: " + noteToEdit.getTitle());
            System.out.println("File: " + noteToEdit.getId().toString().substring(0, 8) + ".md");

            // Open in nano editor
            EditorTest.manualTestNanoEditor(filePath);

            System.out.println("Edit session completed");
            System.out.println("Note: Changes are automatically saved");

            return true;

        } catch (Exception e) {
            System.out.println("Error editing note: " + e.getMessage());
            return false;
        }
    }

    private Note findNoteById(String id) {
        List<Note> notes = noteManager.getAllNotes();
        return notes.stream()
                .filter(note -> note.getId().toString().startsWith(id))
                .findFirst()
                .orElse(null);
    }

    private Note findNoteByTitle(String title) {
        List<Note> notes = noteManager.getAllNotes();
        return notes.stream()
                .filter(note -> note.getTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private String normalizeCommand(String command) {
        switch (command.toLowerCase()) {
            case "new":
            case "add":
            case "make":
                return "create";
            case "show":
            case "view":
            case "display":
            case "get":
                return "read";
            case "open":
            case "modify":
            case "change":
            case "update":
                return "edit";
            case "find":
            case "lookup":
            case "locate":
                return "search";
            case "ls":
            case "all":
                return "list";
            case "load":
            case "bring":
                return "import";
            case "info":
            case "statistics":
                return "stats";
            default:
                return command;
        }
    }

    // Add this helper method
    private void handleUserFriendlySyntax(String command, Map<String, String> options, List<String> positionalArgs) {
        switch (command) {
            case "create":
                if (!options.containsKey("title") && !positionalArgs.isEmpty()) {
                    options.put("title", positionalArgs.get(0));
                    if (positionalArgs.size() > 1) {
                        options.put("content", String.join(" ", positionalArgs.subList(1, positionalArgs.size())));
                    }
                }
                break;

            case "read":
            case "edit":
                if (!options.containsKey("id") && !options.containsKey("title") && !positionalArgs.isEmpty()) {
                    String firstArg = positionalArgs.get(0);
                    // If it looks like an ID (starts with alphanumeric), treat as ID
                    if (firstArg.matches("[a-fA-F0-9-]{6,}")) {
                        options.put("id", firstArg);
                    } else {
                        // Otherwise treat as title
                        options.put("title", firstArg);
                    }
                }
                break;

            case "search":
                if (!options.containsKey("query") && !positionalArgs.isEmpty()) {
                    options.put("query", String.join(" ", positionalArgs));
                }
                break;

            case "import":
                if (!options.containsKey("file") && !positionalArgs.isEmpty()) {
                    options.put("file", positionalArgs.get(0));
                }
                break;

            case "list":
                if (!positionalArgs.isEmpty()) {
                    String listOption = positionalArgs.get(0).toLowerCase();
                    if (listOption.equals("verbose") || listOption.equals("all") ||
                            listOption.equals("detailed") || listOption.equals("full")) {
                        options.put("verbose", "true");
                    }
                }
                break;
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

    public String getCommand() {
        return command;
    }

    public Map<String, String> getOptions() {
        return options;
    }
}