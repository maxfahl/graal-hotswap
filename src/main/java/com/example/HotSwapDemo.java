package com.example;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.graalvm.polyglot.Context;

public class HotSwapDemo extends JFrame {
    private Context context;
    private final JTextPane outputArea;
    private final Path scriptPath;
    
    // Add style attributes
    private final SimpleAttributeSet jsStyle;
    private final SimpleAttributeSet swapStyle;
    private final SimpleAttributeSet errorStyle;

    public HotSwapDemo() {
        super("GraalVM Hot Swap Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Initialize styles
        jsStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(jsStyle, new java.awt.Color(0, 100, 0));  // Dark green for JS output
        
        swapStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(swapStyle, new java.awt.Color(0, 0, 150));  // Dark blue for swap messages
        
        errorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(errorStyle, new java.awt.Color(150, 0, 0));  // Dark red for errors

        // Initialize script path
        scriptPath = Paths.get("src/main/resources/script.js");

        // Create UI components
        JPanel buttonPanel = new JPanel();
        JButton runButton = new JButton("Run JavaScript");
        JButton hotSwapButton = new JButton("Hot Swap");
        JButton clearButton = new JButton("Clear");
        buttonPanel.add(runButton);
        buttonPanel.add(hotSwapButton);
        buttonPanel.add(clearButton);

        outputArea = new JTextPane();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize context
        createNewContext();

        // Add button listeners
        runButton.addActionListener(e -> runJavaScript());
        hotSwapButton.addActionListener(e -> hotSwap());
        clearButton.addActionListener(e -> clearOutput());

        // Add window listener to clean up
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (context != null) {
                    context.close();
                }
            }
        });

        setLocationRelativeTo(null);
    }

    public class Timer {
        public void setTimeout(Runnable callback, int delay) {
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Just try to evaluate an empty string - if it fails, the context is closed
                            context.eval("js", "");
                            callback.run();
                        } catch (Exception e) {
                            // Silently ignore errors from closed contexts
                        }
                    });
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();
        }
    }

    private void createNewContext() {
        if (context != null) {
            context.close();
        }
        
        // Create a custom output stream that writes to our text area
        OutputStream outputStream = new OutputStream() {
            private StringBuilder lineBuffer = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                try {
                    char c = (char) b;
                    if (c == '\n') {
                        final String line = lineBuffer.toString();
                        log("JS Console: " + line, jsStyle);
                        lineBuffer = new StringBuilder();
                    } else {
                        lineBuffer.append(c);
                    }
                } catch (Exception e) {
                    log("Error in output stream: " + e.getMessage(), errorStyle);
                }
            }

            @Override
            public void flush() throws IOException {
                if (lineBuffer.length() > 0) {
                    final String line = lineBuffer.toString();
                    log("JS Console: " + line, jsStyle);
                    lineBuffer = new StringBuilder();
                }
            }
        };

        log("Initializing new context...");

        context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowHostAccess(org.graalvm.polyglot.HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .option("js.shared-array-buffer", "true")
                .option("engine.WarnInterpreterOnly", "false")
                .allowExperimentalOptions(true)
                .out(outputStream)
                .err(outputStream)
                .build();
        
        try {
            // Bind our timer service
            context.getBindings("js").putMember("javaTimer", new Timer());
            
            String script = new String(Files.readAllBytes(scriptPath));
            script = "const setTimeout = (fn, delay) => javaTimer.setTimeout(() => fn(), delay);\n" + script;
            context.eval("js", script);
        } catch (IOException e) {
            log("Error loading script: " + e.getMessage());
        }
    }

    private void runJavaScript() {
        try {
            // Execute everything in JavaScript
            context.eval("js", 
                "sayHelloSoon()" +
                ".then(result => {" +
                "    console.log('Promise resolved with:', result);" +
                "});");
        } catch (Exception e) {
            log("Error running JavaScript: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void hotSwap() {
        createNewContext();
        log("Hot swap completed.");
    }

    private void clearOutput() {
        SwingUtilities.invokeLater(() -> {
            outputArea.setText("");
        });
    }

    private void log(String message, SimpleAttributeSet style) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = outputArea.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), message + "\n", style);
                outputArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void log(String message) {
        // Default to swap style for backward compatibility
        log(message, swapStyle);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HotSwapDemo().setVisible(true);
        });
    }
} 