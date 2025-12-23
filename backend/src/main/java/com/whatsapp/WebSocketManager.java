package com.whatsapp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 * WebSocket Manager handles real-time messaging connections
 * Manages user connections, broadcasting messages, and group communications
 */
public class WebSocketManager {
    private static final Map<String, WebSocketConnection> activeConnections = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> groupConnections = new ConcurrentHashMap<>();
    private MessageRepository messageRepository;
    
    public WebSocketManager(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    /**
     * Register a user's WebSocket connection
     */
    public synchronized void registerConnection(String userId, WebSocketConnection connection) {
        activeConnections.put(userId, connection);
        System.out.println("✓ User " + userId + " connected via WebSocket");
        broadcastUserStatus(userId, "online");
    }
    
    /**
     * Remove a user's WebSocket connection
     */
    public synchronized void removeConnection(String userId) {
        if (activeConnections.remove(userId) != null) {
            System.out.println("✗ User " + userId + " disconnected from WebSocket");
            broadcastUserStatus(userId, "offline");
        }
    }
    
    /**
     * Add user to group for receiving group messages
     */
    public synchronized void addUserToGroup(String groupId, String userId) {
        groupConnections.computeIfAbsent(groupId, k -> new ArrayList<>()).add(userId);
    }
    
    /**
     * Remove user from group
     */
    public synchronized void removeUserFromGroup(String groupId, String userId) {
        List<String> members = groupConnections.get(groupId);
        if (members != null) {
            members.remove(userId);
        }
    }
    
    /**
     * Send direct/private message to a specific user ONLY
     */
    public void sendPrivateMessage(String userId, String messageJson) {
        WebSocketConnection connection = activeConnections.get(userId);
        if (connection != null && connection.isConnected()) {
            try {
                connection.send(messageJson);
            } catch (IOException e) {
                System.out.println("✗ Error sending private message to user: " + e.getMessage());
                removeConnection(userId);
            }
        }
    }
    
    /**
     * Send direct message to a specific user (legacy - sends only to recipient)
     */
    public void sendMessageToUser(String userId, String messageContent, String senderId) {
        WebSocketConnection connection = activeConnections.get(userId);
        if (connection != null && connection.isConnected()) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("type", "direct_message");
            messageJson.put("senderId", senderId);
            messageJson.put("content", messageContent);
            messageJson.put("timestamp", System.currentTimeMillis());
            
            try {
                connection.send(messageJson.toString());
            } catch (IOException e) {
                System.out.println("✗ Error sending message to user: " + e.getMessage());
                removeConnection(userId);
            }
        }
    }
    
    /**
     * Broadcast message to all members of a group
     */
    public void broadcastToGroup(String groupId, String messageContent, String senderId) {
        List<String> members = groupConnections.get(groupId);
        if (members == null || members.isEmpty()) {
            System.out.println("✗ No members connected in group: " + groupId);
            return;
        }
        
        JSONObject messageJson = new JSONObject();
        messageJson.put("type", "group_message");
        messageJson.put("groupId", groupId);
        messageJson.put("senderId", senderId);
        messageJson.put("content", messageContent);
        messageJson.put("timestamp", System.currentTimeMillis());
        
        String messageStr = messageJson.toString();
        for (String memberId : members) {
            WebSocketConnection connection = activeConnections.get(memberId);
            if (connection != null && connection.isConnected()) {
                try {
                    connection.send(messageStr);
                } catch (IOException e) {
                    System.out.println("✗ Error broadcasting to member: " + e.getMessage());
                    removeConnection(memberId);
                }
            }
        }
    }
    
    /**
     * Broadcast user status (online/offline) to all connected users
     */
    public void broadcastUserStatus(String userId, String status) {
        JSONObject statusJson = new JSONObject();
        statusJson.put("type", "user_status");
        statusJson.put("userId", userId);
        statusJson.put("status", status);
        statusJson.put("timestamp", System.currentTimeMillis());
        
        String statusStr = statusJson.toString();
        for (WebSocketConnection connection : activeConnections.values()) {
            if (connection.isConnected()) {
                try {
                    connection.send(statusStr);
                } catch (IOException e) {
                    System.out.println("✗ Error broadcasting status: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Send typing indicator
     */
    public void broadcastTypingStatus(String userId, String chatId, boolean isTyping) {
        JSONObject typingJson = new JSONObject();
        typingJson.put("type", "typing_status");
        typingJson.put("userId", userId);
        typingJson.put("chatId", chatId);
        typingJson.put("isTyping", isTyping);
        
        String typingStr = typingJson.toString();
        for (WebSocketConnection connection : activeConnections.values()) {
            if (connection.isConnected()) {
                try {
                    connection.send(typingStr);
                } catch (IOException e) {
                    System.out.println("✗ Error broadcasting typing status: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Send message delivery confirmation
     */
    public void sendDeliveryConfirmation(String messageId, String deliveredTo) {
        JSONObject confirmJson = new JSONObject();
        confirmJson.put("type", "message_delivered");
        confirmJson.put("messageId", messageId);
        confirmJson.put("deliveredTo", deliveredTo);
        confirmJson.put("timestamp", System.currentTimeMillis());
        
        for (WebSocketConnection connection : activeConnections.values()) {
            if (connection.isConnected()) {
                try {
                    connection.send(confirmJson.toString());
                } catch (IOException e) {
                    System.out.println("✗ Error sending delivery confirmation: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get all active connections count
     */
    public int getActiveConnectionsCount() {
        return activeConnections.size();
    }
    
    /**
     * Check if a user is online
     */
    public boolean isUserOnline(String userId) {
        WebSocketConnection connection = activeConnections.get(userId);
        return connection != null && connection.isConnected();
    }
    
    /**
     * Get list of online users
     */
    public List<String> getOnlineUsers() {
        return new ArrayList<>(activeConnections.keySet());
    }
}
