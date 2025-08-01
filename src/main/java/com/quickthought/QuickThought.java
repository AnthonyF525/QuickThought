package com.quickthought;

import java.io.File;
import java.util.Scanner;

public class QuickThought {
    public static void main(String[] args) {
        clearTerminal();

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

            CLIHandler cliHandler = new CLIHandler(notesDirectory);

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

                if ("test-editor".equals(input)) {
                    testNanoIntegration();
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
        char quoteChar = '"';

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                    args.add(current.toString());
                    current = new StringBuilder();
                    continue;
                } else {
                    current.append(c);
                }
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
        System.out.println("*  _____                                                  _____ ");
        System.out.println("* ( ___ )                                                ( ___ )");
        System.out.println("*  |   |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|   | ");
        System.out.println("*  |   | _______       _____      ______                  |   | ");
        System.out.println("*  |   | __  __ \\___  ____(_)________  /__                |   | ");
        System.out.println("*  |   | _  / / /  / / /_  /_  ___/_  //_/                |   | ");
        System.out.println("*  |   | / /_/ // /_/ /_  / / /__ _  ,<                   |   | ");
        System.out.println("*  |   | \\___\\_\\\\__,_/ /_/  \\___/ /_/|_|                  |   | ");
        System.out.println("*  \\   /                                                  \\   / ");
        System.out.println("*   > <                                                    > < ");
        System.out.println("*  /   \\ _____________                      ______ _____  /   \\ ");
        System.out.println("*  |   | ___  __/__  /___________  ________ ___  /___  /_ |   | ");
        System.out.println("*  |   | __  /  __  __ \\  __ \\  / / /_  __ `/_  __ \\  __/ |   | ");
        System.out.println("*  |   | _  /   _  / / / /_/ / /_/ /_  /_/ /_  / / / /_   |   | ");
        System.out.println("*  |   | /_/    /_/ /_/\\____/\\__,_/ _\\__, / /_/ /_/\\__/   |   | ");
        System.out.println("*  |   |                            /____/                |   | ");
        System.out.println("*  |___|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|___| ");
        System.out.println("* (_____)                                                (_____)");

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

    private static void testNanoIntegration() {
        try {
            // Use your actual notes directory
            String notesDirectory = System.getProperty("user.dir") + "/notes";
            File dir = new File(notesDirectory);

            if (!dir.exists()) {
                System.out.println("Notes directory doesn't exist: " + notesDirectory);
                return;
            }

            // Find an existing note file to edit
            File[] noteFiles = dir.listFiles((d, name) -> name.endsWith(".md"));

            if (noteFiles == null || noteFiles.length == 0) {
                System.out.println("No note files found to edit");
                System.out.println("Create a note first: create \"Test\" \"Content\"");
                return;
            }

            // Use the first note file
            String filePath = noteFiles[0].getAbsolutePath();
            System.out.println("Testing nano editor with: " + noteFiles[0].getName());

            EditorTest.manualTestNanoEditor(filePath);

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
        }
    }

    private static void clearTerminal() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("windows")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Mac/Linux - try multiple approaches
                
                // Method 1: Use system clear command
                try {
                    new ProcessBuilder("clear").inheritIO().start().waitFor();
                } catch (Exception e) {
                    // Method 2: ANSI escape sequences
                    System.out.print("\033[2J");    // Clear entire screen
                    System.out.print("\033[H");     // Move cursor to top-left
                    System.out.flush();
                    
                    // Method 3: Alternative ANSI codes
                    System.out.print("\033[2J\033[3J\033[H");
                    System.out.flush();
                }
            }
            
            // Additional flush to ensure clearing happens
            System.out.flush();
            
        } catch (Exception e) {
            // Fallback - print many newlines to push content up
            for (int i = 0; i < 100; i++) {
                System.out.println();
            }
        }
    }
}