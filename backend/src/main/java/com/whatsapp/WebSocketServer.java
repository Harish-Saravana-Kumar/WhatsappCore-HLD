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
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // Read first line to check if it's HTTP upgrade or raw JSON
            String firstLine = in.readLine();
            if (firstLine == null) {
                clientSocket.close();
                return;
            }
            
            // Check if this is an HTTP WebSocket upgrade request
            if (firstLine.startsWith("GET") || firstLine.startsWith("POST")) {
                // This is an HTTP request - handle WebSocket upgrade
                handleWebSocketUpgrade(clientSocket, in, firstLine);
            } else {
                // This is raw JSON - legacy support
                try {
                    JSONObject json = new JSONObject(firstLine);
                    String userId = json.getString("userId");
                    handleWebSocketConnection(clientSocket, userId);
                } catch (Exception e) {
                    System.out.println("✗ Error handling client: " + e.getMessage());
                    clientSocket.close();
                }
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
     * Handle WebSocket HTTP upgrade
     */
    private void handleWebSocketUpgrade(Socket clientSocket, BufferedReader in, String firstLine) throws IOException {
        // Read all headers
        String line;
        String secWebSocketKey = null;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("sec-websocket-key:")) {
                secWebSocketKey = line.substring("sec-websocket-key:".length()).trim();
            }
        }
        
        if (secWebSocketKey == null) {
            clientSocket.close();
            return;
        }
        
        // Send WebSocket handshake response
        String acceptKey = generateWebSocketAcceptKey(secWebSocketKey);
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("HTTP/1.1 101 Switching Protocols");
        out.println("Upgrade: websocket");
        out.println("Connection: Upgrade");
        out.println("Sec-WebSocket-Accept: " + acceptKey);
        out.println();
        out.flush();
        
        // Extract userId from first WebSocket frame
        try {
            BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
            String messageData = readWebSocketFrame(bis);
            if (messageData != null) {
                JSONObject json = new JSONObject(messageData);
                String userId = json.optString("userId");
                if (userId != null && !userId.isEmpty()) {
                    handleWebSocketConnection(clientSocket, userId);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error reading WebSocket frame: " + e.getMessage());
        }
        clientSocket.close();
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
     * Read a WebSocket frame (RFC 6455)
     */
    private String readWebSocketFrame(BufferedInputStream in) throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        
        if (b1 == -1 || b2 == -1) return null;
        
        boolean masked = (b2 & 0x80) != 0;
        int payloadLen = b2 & 0x7F;
        
        if (payloadLen == 126) {
            byte[] lenBytes = new byte[2];
            in.read(lenBytes);
            payloadLen = ((lenBytes[0] & 0xFF) << 8) | (lenBytes[1] & 0xFF);
        } else if (payloadLen == 127) {
            byte[] lenBytes = new byte[8];
            in.read(lenBytes);
            payloadLen = (int) (((lenBytes[4] & 0xFF) << 24) | ((lenBytes[5] & 0xFF) << 16) | 
                               ((lenBytes[6] & 0xFF) << 8) | (lenBytes[7] & 0xFF));
        }
        
        byte[] maskingKey = new byte[4];
        if (masked) {
            in.read(maskingKey);
        }
        
        byte[] payload = new byte[payloadLen];
        in.read(payload);
        
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
            }
        }
        
        return new String(payload, java.nio.charset.StandardCharsets.UTF_8);
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
