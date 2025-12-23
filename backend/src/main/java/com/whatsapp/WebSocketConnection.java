package com.whatsapp;

import java.io.*;
import java.net.Socket;
import java.util.*;
import org.json.JSONObject;

/**
 * Represents a WebSocket connection for a user
 * Handles sending and receiving messages
 */
public class WebSocketConnection {
    private Socket socket;
    private String userId;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;
    private WebSocketManager manager;
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private GroupRepository groupRepository;
    private ChatRepository chatRepository;
    
    public WebSocketConnection(Socket socket, String userId, WebSocketManager manager, 
                               UserRepository userRepository, MessageRepository messageRepository, 
                               GroupRepository groupRepository, ChatRepository chatRepository) {
        this.socket = socket;
        this.userId = userId;
        this.manager = manager;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.chatRepository = chatRepository;
        this.connected = true;
        
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("✗ Error initializing WebSocket connection: " + e.getMessage());
            this.connected = false;
        }
    }
    
    /**
     * Start listening for incoming messages from this connection
     */
    public void startListening() {
        new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                System.out.println("✗ WebSocket connection error: " + e.getMessage());
            } finally {
                close();
            }
        }).start();
    }
    
    /**
     * Process incoming WebSocket message
     */
    private void handleMessage(String messageStr) {
        try {
            JSONObject json = new JSONObject(messageStr);
            String type = json.getString("type");
            
            switch (type) {
                case "direct_message":
                    handleDirectMessage(json);
                    break;
                case "group_message":
                    handleGroupMessage(json);
                    break;
                case "typing":
                    handleTypingStatus(json);
                    break;
                case "join_group":
                    handleJoinGroup(json);
                    break;
                case "leave_group":
                    handleLeaveGroup(json);
                    break;
                case "status_update":
                    handleStatusUpdate(json);
                    break;
                default:
                    System.out.println("✗ Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.out.println("✗ Error processing message: " + e.getMessage());
        }
    }
    
    /**
     * Handle direct message (user-to-user) - PRIVATE
     */
    private void handleDirectMessage(JSONObject json) {
        String receiverId = json.getString("receiverId");
        String content = json.getString("content");
        
        // Verify receiver exists
        User receiver = userRepository.getUserById(receiverId);
        if (receiver == null) {
            sendError("✗ Recipient user not found");
            return;
        }
        
        // Get or create a private chat between these two users
        Chat chat = chatRepository.getPrivateChat(userId, receiverId);
        if (chat == null) {
            chat = new Chat(userId, receiverId);
            chatRepository.saveChat(chat);
        }
        
        // Create and save message to the chat
        Message message = new Message(UUID.randomUUID().toString(), userId, content);
        chat.addMessage(message);
        messageRepository.saveMessage(message);
        chatRepository.updateChat(chat);
        
        // Send message ONLY to the specific recipient (NOT broadcast)
        JSONObject privateMessage = new JSONObject();
        privateMessage.put("type", "direct_message");
        privateMessage.put("senderId", userId);
        privateMessage.put("messageId", message.getMessageId());
        privateMessage.put("content", content);
        privateMessage.put("chatId", chat.getChatId());
        privateMessage.put("timestamp", System.currentTimeMillis());
        
        // Only send to the intended recipient
        manager.sendPrivateMessage(receiverId, privateMessage.toString());
        
        // Send confirmation back to sender
        JSONObject confirmation = new JSONObject();
        confirmation.put("type", "message_sent");
        confirmation.put("messageId", message.getMessageId());
        confirmation.put("chatId", chat.getChatId());
        confirmation.put("timestamp", System.currentTimeMillis());
        
        try {
            send(confirmation.toString());
        } catch (IOException e) {
            System.out.println("✗ Error sending confirmation: " + e.getMessage());
        }
    }
    
    /**
     * Handle group message
     */
    private void handleGroupMessage(JSONObject json) {
        String groupId = json.getString("groupId");
        String content = json.getString("content");
        
        // Get group and add message
        Group group = groupRepository.getGroupById(groupId);
        if (group != null && group.getMembers().contains(userId)) {
            Message message = new Message(UUID.randomUUID().toString(), userId, content);
            group.addMessage(message);
            messageRepository.saveMessage(message);
            groupRepository.updateGroup(group);
            
            // Broadcast to all group members
            manager.broadcastToGroup(groupId, content, userId);
        } else {
            sendError("Not a member of this group");
        }
    }
    
    /**
     * Handle user typing status
     */
    private void handleTypingStatus(JSONObject json) {
        String chatId = json.getString("chatId");
        boolean isTyping = json.getBoolean("isTyping");
        manager.broadcastTypingStatus(userId, chatId, isTyping);
    }
    
    /**
     * Handle joining a group
     */
    private void handleJoinGroup(JSONObject json) {
        String groupId = json.getString("groupId");
        manager.addUserToGroup(groupId, userId);
        
        JSONObject joinNotification = new JSONObject();
        joinNotification.put("type", "user_joined_group");
        joinNotification.put("groupId", groupId);
        joinNotification.put("userId", userId);
        joinNotification.put("timestamp", System.currentTimeMillis());
        
        manager.broadcastToGroup(groupId, joinNotification.toString(), "system");
    }
    
    /**
     * Handle leaving a group
     */
    private void handleLeaveGroup(JSONObject json) {
        String groupId = json.getString("groupId");
        manager.removeUserFromGroup(groupId, userId);
        
        JSONObject leaveNotification = new JSONObject();
        leaveNotification.put("type", "user_left_group");
        leaveNotification.put("groupId", groupId);
        leaveNotification.put("userId", userId);
        leaveNotification.put("timestamp", System.currentTimeMillis());
        
        manager.broadcastToGroup(groupId, leaveNotification.toString(), "system");
    }
    
    /**
     * Handle status update
     */
    private void handleStatusUpdate(JSONObject json) {
        String status = json.getString("status");
        manager.broadcastUserStatus(userId, status);
    }
    
    /**
     * Send message to client
     */
    public void send(String message) throws IOException {
        if (connected && out != null) {
            out.println(message);
            out.flush();
        }
    }
    
    /**
     * Send error message to client
     */
    private void sendError(String errorMessage) {
        JSONObject error = new JSONObject();
        error.put("type", "error");
        error.put("message", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        
        try {
            send(error.toString());
        } catch (IOException e) {
            System.out.println("✗ Error sending error message: " + e.getMessage());
        }
    }
    
    /**
     * Close the connection
     */
    public void close() {
        connected = false;
        manager.removeConnection(userId);
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("✗ Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Check if connection is still active
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    public String getUserId() {
        return userId;
    }
}
