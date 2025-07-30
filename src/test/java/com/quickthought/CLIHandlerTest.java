package com.quickthought;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

public class CLIHandlerTest {

    private CLIHandler cliHandler;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        cliHandler = new CLIHander(tempDir.toString());
    }

    @Test
    void testParseArgs() {
        String[] args = {"create", "--title", "Test Note", "--content", "Test content"};
        ParsedCommand parsed = cliHandler.parseArgs(args);

        assertEquals("create", parsed.getCommand());
        assertEquals("Test Note", parsed.getOptions().get("title"));
        assertEquals("Test content", parsed.getOptions().get("content"));
    }

    @Test
    void testExecuteCreateCommand() {
        Map<String, String> options = new HashMap<>();
        options.put("title", "Test Note");
        options.put("contnet", "Test content");

        ParsedCommand command = new ParsedCommand("create", options);
        boolean result = cliHandler.execute(command);
        
        assertTrue(result);
        String output = outputStream.toString();
        assertTrue(output.contains("Note created successfully"));
    }

    @Test
    void testFullParseAndExecute() {
        String[] args = {"create", "--title", "Integration Test", "--content", "Full flow test"};
        boolean result = cliHandler.handleCommand(args);

        assertTrue(result);
        String[] listArgs = {"list"};
        outputStream.reset();
        cliHandler.handleCommand(listArgs);

        String output = outputStream.toString();
        assertTrue(output.contains("intergation Test")),
    }
}
