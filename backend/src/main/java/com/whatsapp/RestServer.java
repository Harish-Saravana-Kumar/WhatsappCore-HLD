package com.whatsapp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class RestServer {
    private HttpServer server;
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;
    private GroupRepository groupRepository;
    private WebSocketManager webSocketManager;
    private WebSocketServer webSocketServer;
    private static final int PORT = getEnvPort("PORT", 8080);
    private static final int WS_PORT = getEnvPort("WS_PORT", 8081);
    private static Map<String, User> sessionUsers = new HashMap<>();
    
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
    
    public RestServer() {
        this.userRepository = new UserRepository();
        this.chatRepository = new ChatRepository();
        this.messageRepository = new MessageRepository();
        this.groupRepository = new GroupRepository();
        this.webSocketManager = new WebSocketManager(messageRepository);
    }
    
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            
            // Static file handler for HTML, CSS, JS
            server.createContext("/", new StaticFileHandler());
            
            // API endpoints
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/users", new GetUsersHandler());
            server.createContext("/api/chats", new GetChatsHandler());
            server.createContext("/api/chats/send", new SendMessageHandler());            
            server.createContext("/api/chats/messages", new GetMessagesHandler());            
            server.createContext("/api/groups", new GetGroupsHandler());
            server.createContext("/api/groups/create", new CreateGroupHandler());
            server.createContext("/api/friends", new GetFriendsHandler());
            server.createContext("/api/friends/add", new AddFriendHandler());
            server.createContext("/api/profile", new GetProfileHandler());
            server.createContext("/api/ws/status", new WebSocketStatusHandler());
            
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println("✓ REST Server started on http://localhost:" + PORT);
            System.out.println("✓ Open your browser: http://localhost:" + PORT);
            
            // Start WebSocket Server
            webSocketServer = new WebSocketServer(webSocketManager, userRepository, messageRepository, groupRepository, chatRepository);
            new Thread(webSocketServer).start();
            
        } catch (IOException e) {
            System.out.println("✗ Error starting REST Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // WebSocket Status Handler
    private class WebSocketStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            if ("GET".equals(exchange.getRequestMethod())) {
                JSONObject response = new JSONObject();
                response.put("type", "websocket_status");
                response.put("active_connections", webSocketManager.getActiveConnectionsCount());
                response.put("online_users", webSocketManager.getOnlineUsers());
                response.put("websocket_server", "ws://localhost:" + WS_PORT);
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Static file handler
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            try {
                String filePath = "../../frontend/public" + path;
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
                
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                
                exchange.sendResponseHeaders(200, fileContent.length);
                exchange.getResponseBody().write(fileContent);
                exchange.close();
            } catch (FileNotFoundException e) {
                sendError(exchange, 404, "File not found");
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
    
    // Register handler
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String profileName = json.getString("profileName");
                String phoneNumber = json.getString("phoneNumber");
                
                User newUser = new User(UUID.randomUUID().toString(), profileName, phoneNumber);
                boolean success = userRepository.saveUser(newUser);
                
                JSONObject response = new JSONObject();
                if (success) {
                    sessionUsers.put(phoneNumber, newUser);
                    response.put("success", true);
                    response.put("userId", newUser.getUserId());
                    response.put("message", "✓ Registration successful");
                } else {
                    response.put("success", false);
                    response.put("message", "✗ Failed to register user");
                }
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Login handler
    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String phoneNumber = json.getString("phoneNumber");
                List<User> allUsers = userRepository.getAllUsers();
                User foundUser = null;
                
                for (User user : allUsers) {
                    if (user.getPhoneNumber().equals(phoneNumber)) {
                        foundUser = user;
                        break;
                    }
                }
                
                JSONObject response = new JSONObject();
                if (foundUser != null) {
                    sessionUsers.put(phoneNumber, foundUser);
                    response.put("success", true);
                    response.put("userId", foundUser.getUserId());
                    response.put("profileName", foundUser.getProfilename());
                    response.put("message", "✓ Login successful");
                } else {
                    response.put("success", false);
                    response.put("message", "✗ User not found");
                }
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Get users handler
    private class GetUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            List<User> allUsers = userRepository.getAllUsers();
            JSONArray usersArray = new JSONArray();
            
            for (User user : allUsers) {
                JSONObject userObj = new JSONObject();
                userObj.put("userId", user.getUserId());
                userObj.put("profileName", user.getProfilename());
                userObj.put("phoneNumber", user.getPhoneNumber());
                usersArray.put(userObj);
            }
            
            JSONObject response = new JSONObject();
            response.put("users", usersArray);
            
            sendResponse(exchange, response.toString());
        }
    }
    
    // Get chats handler
    private class GetChatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String userId = query != null ? query.replace("userId=", "") : "";
            
            List<Chat> chats = chatRepository.getChatsByUser(userId);
            JSONArray chatsArray = new JSONArray();
            
            for (Chat chat : chats) {
                JSONObject chatObj = new JSONObject();
                chatObj.put("chatId", chat.getChatId());
                chatObj.put("userId1", chat.getUserId1());
                chatObj.put("userId2", chat.getUserId2());
                chatObj.put("messageCount", chat.getMessages().size());
                chatsArray.put(chatObj);
            }
            
            JSONObject response = new JSONObject();
            response.put("chats", chatsArray);
            
            sendResponse(exchange, response.toString());
        }
    }
    
    // Send message handler
    private class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String messageId = UUID.randomUUID().toString();
                String senderId = json.getString("senderId");
                String content = json.getString("content");
                String chatId = json.optString("chatId", "");
                String groupId = json.optString("groupId", "");
                String receiverId = json.optString("receiverId", "");
                
                Message message = new Message(messageId, senderId, content);
                
                JSONObject response = new JSONObject();
                
                // Handle group message
                if (!groupId.isEmpty()) {
                    Group group = groupRepository.getGroupById(groupId);
                    if (group != null) {
                        group.addMessage(message);
                        groupRepository.updateGroup(group);
                        response.put("success", true);
                    } else {
                        response.put("success", false);
                        response.put("message", "Group not found");
                    }
                }
                // Handle private message with friend
                else if (!receiverId.isEmpty()) {
                    Chat chat = chatRepository.getPrivateChat(senderId, receiverId);
                    if (chat == null) {
                        // Create new private chat if it doesn't exist
                        chat = new Chat(senderId, receiverId);
                        chatRepository.saveChat(chat);
                    }
                    
                    chat.addMessage(message);
                    messageRepository.saveMessage(message);
                    chatRepository.updateChat(chat);
                    
                    response.put("success", true);
                    response.put("chatId", chat.getChatId());
                }
                // Handle regular chat message
                else if (!chatId.isEmpty()) {
                    Chat chat = chatRepository.getChatById(chatId);
                    if (chat != null) {
                        chat.addMessage(message);
                        messageRepository.saveMessage(message);
                        chatRepository.updateChat(chat);
                        response.put("success", true);
                    } else {
                        response.put("success", false);
                        response.put("message", "Chat not found");
                    }
                } else {
                    // Save message anyway
                    messageRepository.saveMessage(message);
                    response.put("success", true);
                }
                
                response.put("messageId", messageId);
                response.put("timestamp", System.currentTimeMillis());
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Get groups handler
    private class GetGroupsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String userId = query != null ? query.replace("userId=", "") : "";
            
            List<Group> groups = groupRepository.getGroupsByUser(userId);
            JSONArray groupsArray = new JSONArray();
            
            for (Group group : groups) {
                JSONObject groupObj = new JSONObject();
                groupObj.put("groupId", group.getGroupId());
                groupObj.put("groupName", group.getGroupName());
                groupObj.put("memberCount", group.getMembers().size());
                groupsArray.put(groupObj);
            }
            
            JSONObject response = new JSONObject();
            response.put("groups", groupsArray);
            
            sendResponse(exchange, response.toString());
        }
    }
    
    // Create group handler
    private class CreateGroupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            enableCORS(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String groupName = json.getString("groupName");
                String adminId = json.getString("adminId");
                
                Group newGroup = new Group(groupName, adminId);
                
                // Add ALL members from the array (addMember prevents duplicates)
                if (json.has("members")) {
                    JSONArray membersArray = json.getJSONArray("members");
                    for (int i = 0; i < membersArray.length(); i++) {
                        String memberId = membersArray.getString(i);
                        newGroup.addMember(memberId);
                    }
                }
                
                boolean success = groupRepository.saveGroup(newGroup);
                
                JSONObject response = new JSONObject();
                response.put("success", success);
                response.put("groupId", newGroup.getGroupId());
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Get friends handler
    private class GetFriendsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String userId = query != null ? query.replace("userId=", "") : "";
            
            User user = userRepository.getUserById(userId);
            JSONArray friendsArray = new JSONArray();
            
            if (user != null) {
                for (String friendId : user.getFriends()) {
                    User friend = userRepository.getUserById(friendId);
                    if (friend != null) {
                        JSONObject friendObj = new JSONObject();
                        friendObj.put("friendId", friend.getUserId());
                        friendObj.put("profileName", friend.getProfilename());
                        friendObj.put("phoneNumber", friend.getPhoneNumber());
                        friendsArray.put(friendObj);
                    }
                }
            }
            
            JSONObject response = new JSONObject();
            response.put("friends", friendsArray);
            
            sendResponse(exchange, response.toString());
        }
    }
    
    // Add friend handler
    private class AddFriendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String userId = json.getString("userId");
                String friendPhoneNumber = json.getString("friendPhoneNumber");
                
                User user = userRepository.getUserById(userId);
                User friendUser = null;
                List<User> allUsers = userRepository.getAllUsers();
                
                for (User u : allUsers) {
                    if (u.getPhoneNumber().equals(friendPhoneNumber)) {
                        friendUser = u;
                        break;
                    }
                }
                
                JSONObject response = new JSONObject();
                if (user != null && friendUser != null) {
                    if (user.addFriend(friendUser.getUserId())) {
                        userRepository.updateUser(user);
                        response.put("success", true);
                        response.put("message", "✓ Friend added");
                    } else {
                        response.put("success", false);
                        response.put("message", "✗ Already a friend");
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "✗ User not found");
                }
                
                sendResponse(exchange, response.toString());
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        }
    }
    
    // Get profile handler
    private class GetProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String userId = query != null ? query.replace("userId=", "") : "";
            
            User user = userRepository.getUserById(userId);
            JSONObject response = new JSONObject();
            
            if (user != null) {
                response.put("userId", user.getUserId());
                response.put("profileName", user.getProfilename());
                response.put("phoneNumber", user.getPhoneNumber());
                response.put("friendCount", user.getFriends().size());
                response.put("chatCount", chatRepository.getChatsByUser(userId).size());
                response.put("groupCount", groupRepository.getGroupsByUser(userId).size());
            }
            
            sendResponse(exchange, response.toString());
        }
    }
    
    // Helper methods
    private void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
    
    private void handleOptions(HttpExchange exchange) throws IOException {
        enableCORS(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
    
    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }
    
    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        JSONObject error = new JSONObject();
        error.put("error", message);
        String response = error.toString();
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }
    
    // Get messages handler
    private class GetMessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enableCORS(exchange);
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            
            String query = exchange.getRequestURI().getQuery();
            String chatId = null;
            String groupId = null;
            String userId1 = null;
            String userId2 = null;
            
            if (query != null) {
                if (query.contains("chatId=")) {
                    chatId = query.split("chatId=")[1].split("&")[0];
                }
                if (query.contains("groupId=")) {
                    groupId = query.split("groupId=")[1].split("&")[0];
                }
                if (query.contains("userId1=")) {
                    userId1 = query.split("userId1=")[1].split("&")[0];
                }
                if (query.contains("userId2=")) {
                    userId2 = query.split("userId2=")[1].split("&")[0];
                }
            }
            
            List<Message> messages = new ArrayList<>();
            
            // If groupId is provided, get messages from group
            if (groupId != null && !groupId.isEmpty()) {
                Group group = groupRepository.getGroupById(groupId);
                if (group != null) {
                    messages = group.getMessages();
                }
            } 
            // If chatId is provided, get messages from private chat
            else if (chatId != null && !chatId.isEmpty()) {
                Chat chat = chatRepository.getChatById(chatId);
                if (chat != null) {
                    messages = chat.getMessages();
                }
            }
            // If both userIds are provided, get or create a private chat and get messages
            else if (userId1 != null && userId2 != null && !userId1.isEmpty() && !userId2.isEmpty()) {
                Chat chat = chatRepository.getPrivateChat(userId1, userId2);
                if (chat != null) {
                    messages = chat.getMessages();
                }
            }
            
            // Sort messages by timestamp (chronologically)
            messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
            
            JSONArray messagesArray = new JSONArray();
            for (Message msg : messages) {
                JSONObject msgObj = new JSONObject();
                msgObj.put("messageId", msg.getMessageId());
                msgObj.put("senderId", msg.getSenderId());
                msgObj.put("content", msg.getContent());
                msgObj.put("timestamp", msg.getTimestamp().getTime());
                msgObj.put("isReply", msg.isReply());
                messagesArray.put(msgObj);
            }
            
            JSONObject response = new JSONObject();
            response.put("messages", messagesArray);
            
            sendResponse(exchange, response.toString());
        }
    }
    
    public static void main(String[] args) {
        RestServer server = new RestServer();
        server.start();
        
        // Keep server running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted: " + e.getMessage());
        }
    }
}
