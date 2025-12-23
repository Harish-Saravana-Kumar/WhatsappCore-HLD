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
            
            // Read first message which should contain userId
            String firstMessage = in.readLine();
            if (firstMessage == null) {
                clientSocket.close();
                return;
            }
            
            JSONObject json = new JSONObject(firstMessage);
            String userId = json.getString("userId");
            
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
