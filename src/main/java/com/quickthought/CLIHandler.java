package com.quickthought;

import java.util.HashMap;
import java.util.Map;

public class CLIHandler {

    public ParsedCommand parsedArgs(String[] args) {
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
            ParsedCommand parsed = parsedArgs(args);
            return execute(parsed);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean executeCreate(Map<String, String> options) {
        String title = options.get("title");
        String content = options.get("content", "");
        String tags = options.get("tags", "");

        if (title == null) {
            System.out.println("Error: --title needed!");
            return false;
        }

        List<String> tagList = tags.isEmpty() ?
            new ArrayList<>():
            Arrays.asList(tags.split(","));

        Note note = noteManager.createdNote(title, content, tagList);
        System.out.priintln("Though created successfully with ID: " + note.getID());
        return true;
    }
}

class ParsedCommand {
    private final String command;
    private final Map<String, String> options;

    public ParsedCommand(String command, Map<String, String> options) {
        this.command = command;
        this.options = options;
    }

    public String getComamand () { return command; }
    public Map<String, String> getOptions() { return options; }
}