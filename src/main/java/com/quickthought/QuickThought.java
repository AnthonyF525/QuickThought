package com.quickthought;

import java.util.Scanner;


public class QuickThought {
    public static void main(String[] args) {
        if (args.length == 0) {
            printBanner();
            
            System.out.println("The Hour glass starts!");
            printBanner2();
            System.out.println("Use 'help' to see available commands");
            System.out.println("Press '0' and Enter to exit, or type a command:");
            System.out.println();
            
            Scanner scanner = new Scanner(System.in);
            String notesDirectory = System.getProperty("quickthought.dir",
                System.getProperty("user.dir") + "/notes");

            System.out.println("Notes Directory: " + notesDirectory);
            
            CLIHandler cliHandler = new CLIHandler(notesDirectory);
            
            String currentDirectory = System.getProperty("user.dir");
                System.out.println(currentDirectory + "****"); 

            while (true) {
                System.out.print("quickthought> ");
                String input = scanner.nextLine().trim();
                
                if ("0".equals(input) || "exit".equals(input) || "quit".equals(input)) {
                    System.out.println("The Hourglass turns...");
                    printBanner3();
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
    
    private static void printBanner() {
        String cyan = "\033[36m";
        String reset = "\033[0m";
    
        System.out.println(cyan + "/**");
        System.out.println("/**");
        System.out.println("*  _____                                                  _____ ");
        System.out.println("* ( ___ )                                                ( ___ )");
        System.out.println("*  |   |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|   | ");
        System.out.println("*  |   | _______       _____      ______                  |   | ");
        System.out.println("*  |   | __  __ \\___  ____(_)________  /__                |   | ");
        System.out.println("*  |   | _  / / /  / / /_  /_  ___/_  //_/                |   | ");
        System.out.println("*  |   | / /_/ // /_/ /_  / / /__ _  ,<                   |   | ");
        System.out.println("*  |   | \\___\\_\\\\__,_/ /_/  \\___/ /_/|_|                  |   | ");
        System.out.println("*  \\   /                                                  \\   / ");
        System.out.println("*  /   \\ _____________                      ______ _____  /   \\ ");
        System.out.println("*  |   | ___  __/__  /___________  ________ ___  /___  /_ |   | ");
        System.out.println("*  |   | __  /  __  __ \\  __ \\  / / /_  __ `/_  __ \\  __/ |   | ");
        System.out.println("*  |   | _  /   _  / / / /_/ / /_/ /_  /_/ /_  / / / /_   |   | ");
        System.out.println("*  |   | /_/    /_/ /_/\\____/\\__,_/ _\\__, / /_/ /_/\\__/   |   | ");
        System.out.println("*  |   |                            /____/                |   | ");
        System.out.println("*  |___|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|___| ");
        System.out.println("* (_____)                                                (_____)");
        System.out.println("*/");
        System.out.println();
        
        System.out.println("*/" + reset);
        System.out.println();
    }

    private static void printBanner2() {
        System.out.println("+====+");
        System.out.println("|(::)|");
        System.out.println("| )( |");
        System.out.println("|(..)|");
        System.out.println("+====+");
    }

    private static void printBanner3() {
        System.out.println(".____.");
        System.out.println("|(__)|");
        System.out.println("| )( |");
        System.out.println("|(::)|");
        System.out.println("-''''-");
    }
}