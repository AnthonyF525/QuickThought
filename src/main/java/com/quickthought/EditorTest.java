package com.quickthought;

import java.io.IOException;
import java.util.Scanner;

public class EditorTest {
    
    public static void manualTestNanoEditor(String filePath) {
        try {
            System.out.println("Opening file in nano editor: " + filePath);
            
            // Build the nano command
            ProcessBuilder processBuilder = new ProcessBuilder("nano", filePath);
            processBuilder.inheritIO(); // This allows nano to interact with the terminal
            
            // Start the process
            Process process = processBuilder.start();
            
            // Wait for the editor to close
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("Editor closed successfully");
            } else {
                System.out.println("Editor closed with exit code: " + exitCode);
            }
            
        } catch (IOException e) {
            System.err.println("Failed to start nano editor: " + e.getMessage());
            System.err.println("Make sure nano is installed and available in PATH");
            
            // Fallback: offer to open with default system editor
            offerAlternativeEditor(filePath);
            
        } catch (InterruptedException e) {
            System.err.println("Editor process was interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    private static void offerAlternativeEditor(String filePath) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Would you like to try opening with system default editor? (y/n): ");
        
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("y") || response.equals("yes")) {
            openWithSystemEditor(filePath);
        }
    }
    
    private static void openWithSystemEditor(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("mac")) {
                processBuilder = new ProcessBuilder("open", "-t", filePath);
            } else if (os.contains("windows")) {
                processBuilder = new ProcessBuilder("notepad", filePath);
            } else {
                // Linux/Unix - try common editors
                processBuilder = new ProcessBuilder("gedit", filePath);
            }
            
            processBuilder.start();
            System.out.println("Opened file with system editor");
            
        } catch (IOException e) {
            System.err.println("Failed to open with system editor: " + e.getMessage());
            System.out.println("You can manually edit the file at: " + filePath);
        }
    }
    
    // Method for testing editor functionality without actually opening an editor
    public static void testEditorAvailability() {
        try {
            ProcessBuilder pb = new ProcessBuilder("nano", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("✓ Nano editor is available");
            } else {
                System.out.println("✗ Nano editor not found");
            }
            
        } catch (IOException | InterruptedException e) {
            System.out.println("✗ Nano editor not available: " + e.getMessage());
        }
    }
}
