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
    private static final int PORT = 8080;
    private static Map<String, User> sessionUsers = new HashMap<>();
    
    public RestServer() {
        this.userRepository = new UserRepository();
        this.chatRepository = new ChatRepository();
        this.messageRepository = new MessageRepository();
        this.groupRepository = new GroupRepository();
    }
    
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
            
            // Static file handler for HTML, CSS, JS
            server.createContext("/", new StaticFileHandler());
            
            // API endpoints
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/users", new GetUsersHandler());
            server.createContext("/api/chats", new GetChatsHandler());
            server.createContext("/api/chats/send", new SendMessageHandler());
            server.createContext("/api/groups", new GetGroupsHandler());
            server.createContext("/api/groups/create", new CreateGroupHandler());
            server.createContext("/api/friends", new GetFriendsHandler());
            server.createContext("/api/friends/add", new AddFriendHandler());
            server.createContext("/api/profile", new GetProfileHandler());
            
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println("✓ REST Server started on http://localhost:" + PORT);
            System.out.println("✓ Open your browser: http://localhost:" + PORT);
            
        } catch (IOException e) {
            System.out.println("✗ Error starting REST Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Static file handler
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            try {
                String filePath = "frontend/public" + path;
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
                    response.put("profileName", foundUser.getProfileName());
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
            
            List<User> allUsers = userRepository.getAllUsers();
            JSONArray usersArray = new JSONArray();
            
            for (User user : allUsers) {
                JSONObject userObj = new JSONObject();
                userObj.put("userId", user.getUserId());
                userObj.put("profileName", user.getProfileName());
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
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String messageId = UUID.randomUUID().toString();
                String senderId = json.getString("senderId");
                String content = json.getString("content");
                String chatId = json.optString("chatId", "");
                
                Message message = new Message(messageId, senderId, content);
                boolean success = messageRepository.saveMessage(message);
                
                JSONObject response = new JSONObject();
                response.put("success", success);
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
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(body);
                
                String groupName = json.getString("groupName");
                String adminId = json.getString("adminId");
                
                Group newGroup = new Group(UUID.randomUUID().toString(), groupName, adminId);
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
                        friendObj.put("profileName", friend.getProfileName());
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
            
            String query = exchange.getRequestURI().getQuery();
            String userId = query != null ? query.replace("userId=", "") : "";
            
            User user = userRepository.getUserById(userId);
            JSONObject response = new JSONObject();
            
            if (user != null) {
                response.put("userId", user.getUserId());
                response.put("profileName", user.getProfileName());
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
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
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
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
