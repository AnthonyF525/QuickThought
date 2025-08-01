package com.quickthought;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;
import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException; 
import java.util.Base64;

public class CLIHandler {

    private final String workingDirectory;
    private final NoteManager noteManager;
    private String masterPassword = null;
    private boolean encryptionEnabled = false;

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
            case "encrypt":
                return executeEncrypt(parsedCommand.getOptions());
            case "lock":
                return executeLock();
            case "unlock":
                return executeUnlock();
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
        List<Note> notes = getAllNotesIncludingEncrypted();  

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
        String title = options.get("title");

        if (id == null && title == null) {
            System.out.println("  Error: Specify either --id or --title");
            System.out.println("  Usage:");
            System.out.println("   read --id <note-id>");
            System.out.println("   read --title <note-title>");
            System.out.println("   show abc123");
            System.out.println("   view \"My Note Title\"");
            return false;
        }

        try {
            Note note = null;

            if (id != null) {
                // Find note by ID (supports partial matching)
                note = findNoteById(id);
            } else {
                // Find note by exact title
                note = findNoteByTitle(title);
            }

            if (note == null) {
                if (id != null) {
                    System.out.println(" Note not found with ID: " + id);
                } else {
                    System.out.println(" Note not found with title: " + title);
                }
                return false;
            }

            // Display the note
            System.out.println(" Title: " + note.getTitle());
            System.out.println(" Content: " + note.getContent());
            System.out.println("  Tags: " + note.getTags());
            System.out.println(" Created: " + note.getCreatedAt());
            System.out.println(" Updated: " + note.getUpdatedAt());
            System.out.println(" ID: " + note.getId().toString().substring(0, 8));

            return true;

        } catch (Exception e) {
            System.out.println(" Error reading thought: " + e.getMessage());
            return false;
        }
    }

    private boolean executeSearch(Map<String, String> options) {
        String query = options.get("query");
        if (query == null) {
            System.out.println("Error: --query required for search command");
            return false;
        }

        // Change this line:
        List<Note> allNotes = getAllNotesIncludingEncrypted();  
        List<Note> results = allNotes.stream()
            .filter(note -> note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                           note.getContent().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());

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
        System.out.println("QuickThought - Let your thoughts run free!");
        System.out.println(" Encryption Commands:");
        System.out.println("  encrypt abc123                              # Encrypt note by ID");
        System.out.println("  encrypt \"My Note\"                          # Encrypt note by title");
        System.out.println("  unlock                                       # Enter master password");
        System.out.println("  lock                                         # Clear password from memory");
        System.out.println();
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

        try {
            java.nio.file.Path sourceFile = java.nio.file.Paths.get(filePath);
            if (!java.nio.file.Files.exists(sourceFile)) {
                System.out.println("Error: File not found: " + filePath);
                return false;
            }

            String content = java.nio.file.Files.readString(sourceFile);
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
        List<Note> notes = getAllNotesIncludingEncrypted();
        
        
        for (Note note : notes) {
            String noteId = note.getId().toString();
            System.out.println("  - " + noteId.substring(0, Math.min(8, noteId.length())) + " : " + note.getTitle());
        }
        
        // Try partial match
        Optional<Note> match = notes.stream()
            .filter(note -> {
                String noteId = note.getId().toString().toLowerCase();
                String searchId = id.toLowerCase();
                boolean matches = noteId.startsWith(searchId);
                if (matches) {
                    System.out.println(" Found match: " + noteId);
                }
                return matches;
            })
            .findFirst();
        
        if (match.isPresent()) {
            return match.get();
        } else {
            System.out.println(" No note found starting with: " + id);
            return null;
        }
    }

    private Note findNoteByTitle(String title) {
        List<Note> notes = getAllNotesIncludingEncrypted();  
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
            case "secure":           
            case "protect":
            case "lock-note":        
                return "encrypt";
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
            case "encrypt":
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


    // Add this method to CLIHandler
    private boolean verifyPassword(String password) {
        try {
            File passwordTestFile = new File(workingDirectory + "/.quickthought_auth");

            if (passwordTestFile.exists()) {
                // Existing setup - verify password
                String testData = new String(java.nio.file.Files.readAllBytes(passwordTestFile.toPath()));
                return NoteEncryption.testPassword(testData, password);
            } else {
                // First time setup - create password verification file
                String testString = "QuickThought_Password_Test_" + System.currentTimeMillis();
                String encryptedTest = NoteEncryption.encrypt(testString, password);
                java.nio.file.Files.write(passwordTestFile.toPath(), encryptedTest.getBytes());

                System.out.println(" Master password set successfully!");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Password verification error: " + e.getMessage());
            return false;
        }
    }

    // Add this method to clear password from memory
    private void lockSession() {
        masterPassword = null;
        encryptionEnabled = false;
        System.out.println(" Session locked - password cleared from memory");
    }

    private boolean executeEncrypt(Map<String, String> options) {
        String noteId = options.get("id");
        String noteTitle = options.get("title");

        if (noteId == null && noteTitle == null) {
            System.out.println(" Error: Specify either --id or --title");
            System.out.println(" Usage: encrypt --id <note-id> OR encrypt --title <note-title>");
            return false;
        }

        if (!promptForPassword()) {
            return false;
        }

        try {
            Note note = null;
            if (noteId != null) {
                note = findNoteById(noteId);
            } else {
                note = findNoteByTitle(noteTitle);
            }

            if (note == null) {
                System.out.println(" Note not found");
                return false;
            }

            // Read the original file
            String originalPath = workingDirectory + "/" + note.getId() + ".md";
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(originalPath)));

            // Encrypt the content
            String encryptedContent = NoteEncryption.encrypt(content, masterPassword);

            // Save as encrypted file
            String encryptedPath = workingDirectory + "/" + note.getId() + ".md.enc";
            java.nio.file.Files.write(java.nio.file.Paths.get(encryptedPath), encryptedContent.getBytes());

            // Delete original file
            java.nio.file.Files.delete(java.nio.file.Paths.get(originalPath));

            System.out.println(" Note encrypted successfully");
            System.out.println(" File: " + note.getId().toString().substring(0, 8) + ".md.enc");

            return true;

        } catch (Exception e) {
            System.out.println(" Encryption failed: " + e.getMessage());
            return false;
        }
    }

    private boolean executeLock() {
        lockSession();
        return true;
    }

    private boolean executeUnlock() {
        masterPassword = null; // Clear existing password
        return promptForPassword();
    }

    private List<Note> getAllNotesIncludingEncrypted() {
        List<Note> allNotes = new ArrayList<>();
        
        try {
            Path workingPath = Paths.get(workingDirectory);
            
            if (!Files.exists(workingPath)) {
                return allNotes;
            }
            
            Files.list(workingPath)
                .filter(path -> path.toString().endsWith(".md") || 
                               path.toString().endsWith(".md.enc")) 
                .forEach(path -> {
                    try {
                        Note note = loadNoteFromFile(path);
                        if (note != null) {
                            allNotes.add(note);
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Could not load note from " + path.getFileName());
                    }
                });
            
        } catch (IOException e) {
            System.err.println("Error reading notes directory: " + e.getMessage());
        }
        
        return allNotes;
    }

    private Note loadNoteFromFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            String fileName = filePath.getFileName().toString();
            
            // Check if this is an encrypted file by extension
            if (fileName.endsWith(".md.enc")) {
                if (!encryptionEnabled || masterPassword == null) {
                    // Return a placeholder note for encrypted files when locked
                    return createEncryptedPlaceholder(filePath);
                }
                // Decrypt the content if we have the password
                content = decryptContent(content);
                if (content == null) {
                    return createEncryptedPlaceholder(filePath); // Decryption failed
                }
            }
            
            // Parse the YAML content to create a Note object
            return parseNoteFromYaml(content, filePath);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            return null;
        }
    }

    public boolean isEncryptedFile(String content) {
        // Base64 encrypted content detection
        try {
            // If it's valid Base64 and looks like encrypted data, treat as encrypted
            Base64.getDecoder().decode(content.trim());
            return content.trim().length() > 100 && !content.contains("---"); // Not YAML
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Note createEncryptedPlaceholder(Path filePath) {
        // Extract ID from filename (remove both .md and .enc extensions)
        String fileName = filePath.getFileName().toString();
        String idString = fileName.replace(".md.enc", "").replace(".md", "");
        
        try {
            // Create a placeholder note that shows it's encrypted
            Note placeholder = new Note("[ENCRYPTED] " + idString.substring(0, 8), 
                                  "This note is encrypted. Use 'unlock' command to access.", 
                                  Arrays.asList("encrypted", "locked"));
            return placeholder;
        } catch (Exception e) {
            return null; // Invalid filename format
        }
    }

    private String decryptContent(String encryptedContent) {
        try {
            return NoteEncryption.decrypt(encryptedContent, masterPassword);
        } catch (Exception e) {
            System.err.println("Failed to decrypt content: " + e.getMessage());
            return null;
        }
    }

    private Note parseNoteFromYaml(String yamlContent, Path filePath) {
        try {
            return noteManager.parseNoteFromYaml(yamlContent);
        } catch (Exception e) {
            System.err.println("Error parsing YAML from file: " + filePath);
            return null;
        }
    }

    private boolean promptForPassword() {
        if (masterPassword != null) {
            return true;
        }

        Console console = System.console();
        char[] passwordArray;
        
        if (console != null) {
            passwordArray = console.readPassword(" Enter master password: ");
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print(" Enter master password: ");
            String password = scanner.nextLine();
            passwordArray = password.toCharArray();
        }

        if (passwordArray == null || passwordArray.length == 0) {
            System.out.println(" Password cannot be empty");
            return false;
        }

        String password = new String(passwordArray);
        Arrays.fill(passwordArray, ' '); // Clear from memory

        if (verifyPassword(password)) {
            masterPassword = password;
            encryptionEnabled = true;
            System.out.println(" Authentication successful");
            return true;
        } else {
            System.out.println(" Incorrect password");
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

    public String getCommand() {
        return command;
    }

    public Map<String, String> getOptions() {
        return options;
    }
}