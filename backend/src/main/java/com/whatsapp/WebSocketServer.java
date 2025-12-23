package com.whatsapp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import org.json.JSONObject;

/**
 * WebSocket Server handles real-time messaging connections
 * Runs on a separate port (8081) from the REST API
 */
public class WebSocketServer implements Runnable {
    private static final int PORT = getEnvPort("WS_PORT", 8081);
    private ServerSocket serverSocket;
    
    private static int getEnvPort(String envName, int defaultPort) {
        String portStr = System.getenv(envName);
        if (portStr != null && !portStr.isEmpty()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid " + envName + " value, using default: " + defaultPort);
            }
        }
        return defaultPort;
    }
    private WebSocketManager manager;
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private GroupRepository groupRepository;
    private ChatRepository chatRepository;
    private boolean running = false;
    
    public WebSocketServer(WebSocketManager manager, UserRepository userRepository, 
                           MessageRepository messageRepository, GroupRepository groupRepository,
                           ChatRepository chatRepository) {
        this.manager = manager;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.chatRepository = chatRepository;
    }
    
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("✓ WebSocket Server started on ws://localhost:" + PORT);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    if (running) {
                        System.out.println("✗ Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("✗ Error starting WebSocket Server: " + e.getMessage());
        }
    }
    
    /**
     * Handle incoming client connection
     */
    private void handleClient(Socket clientSocket) {
        try {
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(bis));
            
            // Read first line to check if it's HTTP or WebSocket
            in.mark(1024);
            String firstLine = in.readLine();
            if (firstLine == null) {
                clientSocket.close();
                return;
            }
            
            // Handle HTTP health checks (HEAD, GET for /health, etc)
            if (firstLine.startsWith("HEAD") || firstLine.startsWith("GET")) {
                handleHealthCheck(clientSocket, in, firstLine);
            } 
            // Handle WebSocket upgrade (GET with Upgrade header)
            else if (firstLine.startsWith("GET")) {
                in.reset();
                handleWebSocketUpgrade(clientSocket, in);
            } 
            // Handle raw JSON (legacy support)
            else if (firstLine.startsWith("{")) {
                try {
                    JSONObject json = new JSONObject(firstLine);
                    String userId = json.getString("userId");
                    handleWebSocketConnection(clientSocket, userId);
                } catch (Exception e) {
                    System.out.println("✗ Invalid JSON from client: " + e.getMessage());
                    clientSocket.close();
                }
            } 
            // Unknown format
            else {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("✗ Error handling client: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    /**
     * Handle HTTP health check requests from Render
     */
    private void handleHealthCheck(Socket clientSocket, BufferedReader in, String firstLine) throws IOException {
        // Read and discard headers
        String line;
        while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
            // discard
        }
        
        try {
            // Send HTTP 200 response
            OutputStream out = clientSocket.getOutputStream();
            String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: 2\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" +
                            "OK";
            out.write(response.getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println("✗ Error sending health check response: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    /**
     * Handle WebSocket HTTP upgrade
     */
    private void handleWebSocketUpgrade(Socket clientSocket, BufferedReader in) throws IOException {
        String line;
        String secWebSocketKey = null;
        
        // Read all headers until empty line
        while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.startsWith("sec-websocket-key:")) {
                secWebSocketKey = line.substring("sec-websocket-key:".length()).trim();
            }
        }
        
        if (secWebSocketKey == null || secWebSocketKey.isEmpty()) {
            System.out.println("✗ No Sec-WebSocket-Key in request");
            clientSocket.close();
            return;
        }
        
        try {
            // Send WebSocket handshake response
            String acceptKey = generateWebSocketAcceptKey(secWebSocketKey);
            OutputStream out = clientSocket.getOutputStream();
            String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: " + acceptKey + "\r\n" +
                            "\r\n";
            out.write(response.getBytes());
            out.flush();
            
            // Connection is now in WebSocket mode
            // Use "guest" as default userId - the client can set it later
            handleWebSocketConnection(clientSocket, "guest_" + System.currentTimeMillis());
            
        } catch (Exception e) {
            System.out.println("✗ Error during WebSocket handshake: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
    
    /**
     * Generate WebSocket accept key
     */
    private String generateWebSocketAcceptKey(String secWebSocketKey) {
        try {
            String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String combined = secWebSocketKey + guid;
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(combined.getBytes());
            return java.util.Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Read a WebSocket frame (RFC 6455) - SIMPLIFIED
     */
    private String readWebSocketFrame(BufferedInputStream in) throws IOException {
        try {
            // Skip WebSocket frame parsing - just try to read a line
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            return line;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Handle established WebSocket connection
     */
    private void handleWebSocketConnection(Socket clientSocket, String userId) {
        try {
            // Verify user exists
            User user = userRepository.getUserById(userId);
            if (user == null) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                JSONObject error = new JSONObject();
                error.put("type", "error");
                error.put("message", "User not found");
                out.println(error.toString());
                out.close();
                clientSocket.close();
                return;
            }
            
            // Create WebSocket connection
            WebSocketConnection connection = new WebSocketConnection(clientSocket, userId, manager, 
                                                                     userRepository, messageRepository, 
                                                                     groupRepository, chatRepository);
            
            // Register connection
            manager.registerConnection(userId, connection);
            
            // Send welcome message
            JSONObject welcome = new JSONObject();
            welcome.put("type", "connected");
            welcome.put("userId", userId);
            welcome.put("message", "Connected to WebSocket server");
            connection.send(welcome.toString());
            
            // Start listening for messages
            connection.startListening();
            
        } catch (Exception e) {
            System.out.println("✗ Error handling client: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println("✗ Error closing client socket: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Stop the WebSocket server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("✗ Error stopping WebSocket Server: " + e.getMessage());
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}
