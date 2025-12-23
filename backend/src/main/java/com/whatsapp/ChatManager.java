package com.whatsapp;

import java.util.*;

public class ChatManager {
    private Map<String, Chat> allChats;

    public ChatManager() {
        this.allChats = new HashMap<>();
    }

    public Chat createOrGetChat(String userId1, String userId2) {
        String chatKey1 = userId1 + "-" + userId2;
        String chatKey2 = userId2 + "-" + userId1;

        if (allChats.containsKey(chatKey1)) {
            return allChats.get(chatKey1);
        } else if (allChats.containsKey(chatKey2)) {
            return allChats.get(chatKey2);
        } else {
            Chat newChat = new Chat(userId1, userId2);
            allChats.put(chatKey1, newChat);
            return newChat;
        }
    }

    public void sendMessage(Chat chat, String senderId, String messageContent) {
        String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8);
        Message message = new Message(messageId, senderId, messageContent);
        chat.addMessage(message);
        System.out.println("Message sent successfully!");
    }

    public void sendReply(Chat chat, String messageId, String senderId, String replyContent) {
        Message parentMessage = chat.getMessageById(messageId);
        if (parentMessage != null) {
            chat.addReply(messageId, senderId, replyContent);
            System.out.println("Reply sent successfully!");
        } else {
            System.out.println("Parent message not found!");
        }
    }

    public void displayChat(Chat chat) {
        chat.displayChatHistory();
    }
}
