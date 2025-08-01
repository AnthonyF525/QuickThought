package com.quickthought;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessBuilderTest {

    private String testDirectory;
    private String testFilePath; 

    @BeforeEach
    public void setUp() throws IOException {
        // temp Test directory
        testDirectory = System.getProperty("java.io.tmpdir") + "/quickthought-test";
        testFilePath = testDirectory + "/test-note.md";

        File dir = new File(testDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //test note file
        String testContent = "---\n" +
                            "id: test-123\n" +
                           "title: Test Note\n" +
                           "tags: []\n" +
                           "created_at: '2025-07-31T10:00:00'\n" +
                           "updated_at: '2025-07-31T10:00:00'\n" +
                           "---\n" +
                           "This is test content for editing.";

        Files.write(Paths.get(testFilePath), testContent.getBytes());
    }

    @AfterEach
    public void tearDown() throws IOException {
        //clean up!
        Files.deleteIfExists(Paths.get(testFilePath));
        Files.deleteIfExists(Paths.get(testDirectory));
    }

    @Test
    public void testProcessBuilderCreation() {
        // Test that we can create a ProcessBuilder for nano
        ProcessBuilder pb = new ProcessBuilder("nano", testFilePath);
        assertNotNull(pb, "ProcessBuilder should be created successfully");
        assertEquals("nano", pb.command().get(0), "First command should be 'nano'");
        assertEquals(testFilePath, pb.command().get(1), "Second argument should be file path");
    }
    
    @Test
    public void testEditorCommand() {
        // Test the editor command without actually opening nano
        // (since automated tests can't interact with nano)
        
        String[] editorCommand = {"nano", testFilePath};
        ProcessBuilder pb = new ProcessBuilder(editorCommand);
        
        // Verify the command is set up correctly
        assertEquals(2, pb.command().size(), "Command should have 2 parts");
        assertEquals("nano", pb.command().get(0));
        assertTrue(pb.command().get(1).endsWith("test-note.md"));
    }
    
    @Test
    public void testFileExists() throws IOException {
        // Test that our test file exists and can be read
        assertTrue(Files.exists(Paths.get(testFilePath)), "Test file should exist");
        
        String content = Files.readString(Paths.get(testFilePath));
        assertTrue(content.contains("Test Note"), "File should contain test content");
        assertTrue(content.contains("This is test content"), "File should contain test content");
    }
    
    // Manual test method (for interactive testing)
    public static void manualTestNanoEditor(String filePath) {
        try {
            System.out.println("Opening nano editor for: " + filePath);
            System.out.println("Edit the file, save (Ctrl+O), and exit (Ctrl+X)");
            
            ProcessBuilder pb = new ProcessBuilder("nano", filePath);
            pb.inheritIO(); // Allow user interaction
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("Nano editor closed successfully!");
            } else {
                System.out.println("Nano editor exited with code: " + exitCode);
            }
            
        } catch (Exception e) {
            System.out.println("Error opening nano: " + e.getMessage());
        }
    }
    
}
