package com.quickthought;

import java.util.Scanner;

public class QuickThought {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("QuickThought - The reaction time to take notes!");
            System.out.println("Use 'help' to see available commands");
            System.out.println("Press '0' and Enter to exit, or type a command:");
            
            Scanner scanner = new Scanner(System.in);
            String notesDirectory = System.getProperty("quickthought.dir", 
                System.getProperty("user.home") + "/.quickthought");
            
            CLIHandler cliHandler = new CLIHandler(notesDirectory);
            
            while (true) {
                System.out.print("quickthought> ");
                String input = scanner.nextLine().trim();
                
                if ("0".equals(input) || "exit".equals(input) || "quit".equals(input)) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                if (input.isEmpty()) {
                    continue;
                }
                
                
                String[] commandArgs = parseInput(input);
                boolean success = cliHandler.handleCommand(commandArgs);
                
                if (!success) {
                    System.out.println("Command failed. Type 'help' for available commands.");
                }
            }
            
            scanner.close();
            return;
        }
        
       
        String notesDirectory = System.getProperty("quickthought.dir", 
            System.getProperty("user.dir") + "/notes");
        
        CLIHandler cliHandler = new CLIHandler(notesDirectory);
        boolean success = cliHandler.handleCommand(args);
        
        System.exit(success ? 0 : 1);
    }
    
    private static String[] parseInput(String input) {
        
        java.util.List<String> args = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : input.toCharArray()) {
            if (c == '"' || c == '\'') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            args.add(current.toString());
        }
        
        return args.toArray(new String[0]);
    }
}