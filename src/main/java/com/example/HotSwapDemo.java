package com.example;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class HotSwapDemo extends JFrame {
    private Context context;
    private final JTextArea outputArea;
    private final Path scriptPath;

    public HotSwapDemo() {
        super("GraalVM Hot Swap Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Initialize script path
        scriptPath = Paths.get("src/main/resources/script.js");

        // Create UI components
        JPanel buttonPanel = new JPanel();
        JButton runButton = new JButton("Run JavaScript");
        JButton hotSwapButton = new JButton("Hot Swap");
        buttonPanel.add(runButton);
        buttonPanel.add(hotSwapButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize context
        createNewContext();

        // Add button listeners
        runButton.addActionListener(e -> runJavaScript());
        hotSwapButton.addActionListener(e -> hotSwap());

        setLocationRelativeTo(null);
    }

    private void createNewContext() {
        if (context != null) {
            context.close();
        }
        context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();
        
        try {
            String script = new String(Files.readAllBytes(scriptPath));
            context.eval("js", script);
            log("New context created and script loaded.");
        } catch (IOException e) {
            log("Error loading script: " + e.getMessage());
        }
    }

    private void runJavaScript() {
        try {
            Value function = context.getBindings("js").getMember("sayHelloSoon");
            Value result = function.execute();
            result.invokeMember("then", (Runnable) () -> 
                SwingUtilities.invokeLater(() -> 
                    log("Promise resolved!")
                )
            );
            log("JavaScript function called.");
        } catch (Exception e) {
            log("Error running JavaScript: " + e.getMessage());
        }
    }

    private void hotSwap() {
        createNewContext();
        log("Hot swap completed - new context created with fresh script.");
    }

    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HotSwapDemo().setVisible(true);
        });
    }
} 