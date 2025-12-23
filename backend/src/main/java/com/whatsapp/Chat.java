package com.whatsapp;

import java.util.*;

public class Chat {
    private String chatId;
    private String userId1;
    private String userId2;
    private List<Message> messages;
    private Date createdAt;

    public Chat(String userId1, String userId2) {
        this.chatId = "CHAT-" + UUID.randomUUID().toString().substring(0, 8);
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.messages = new ArrayList<>();
        this.createdAt = new Date();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void addReply(String messageId, String senderId, String replyContent) {
        String replyId = "MSG-" + UUID.randomUUID().toString().substring(0, 8);
        Message reply = new Message(replyId, senderId, replyContent, messageId);
        messages.add(reply);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public Message getMessageById(String messageId) {
        for (Message msg : messages) {
            if (msg.getMessageId().equals(messageId)) {
                return msg;
            }
        }
        return null;
    }

    public String getChatId() {
        return chatId;
    }

    public String getUserId1() {
        return userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public String getOtherUserId(String userId) {
        return userId.equals(userId1) ? userId2 : userId1;
    }

    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void displayChatHistory() {
        System.out.println("\n=== Chat History (Chat ID: " + chatId + ") ===");
        if (messages.isEmpty()) {
            System.out.println("No messages yet.");
            return;
        }
        for (Message msg : messages) {
            System.out.println(msg);
        }
        System.out.println("=============================================\n");
    }
}
