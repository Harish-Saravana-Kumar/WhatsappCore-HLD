package com.whatsapp;

import java.util.*;

public class whatsappCore {
    private static User currentUser = null;
    private static ChatManager chatManager = new ChatManager();
    private static GroupManager groupManager = new GroupManager();
    private static List<User> registeredUsers = new ArrayList<>();
    
    // Database repositories
    private static UserRepository userRepository;
    private static ChatRepository chatRepository;
    private static GroupRepository groupRepository;
    private static MessageRepository messageRepository;

    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("Welcome to WhatsApp Core LLD!");
        System.out.println("Initializing MongoDB Connection...");
        System.out.println("\n");

        // Initialize database repositories
        try {
            DatabaseConnection.getInstance();
            userRepository = new UserRepository();
            chatRepository = new ChatRepository();
            groupRepository = new GroupRepository();
            messageRepository = new MessageRepository();
            
            // Load users from database
            registeredUsers.addAll(userRepository.getAllUsers());
            System.out.println("✓ Loaded " + registeredUsers.size() + " users from database\n");
        } catch (Exception e) {
            System.out.println("✗ Failed to initialize database: " + e.getMessage());
            System.out.println("Proceeding without database persistence.\n");
        }

        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            if (currentUser == null) {
                displayAuthMenu(sc);
            } else {
                displayMainMenu();
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        registerUser(sc);
                        break;
                    case 2:
                        manageFriends(sc);
                        break;
                    case 3:
                        manageChats(sc);
                        break;
                    case 4:
                        manageGroups(sc);
                        break;
                    case 5:
                        currentUser.displayUserProfile();
                        break;
                    case 6:
                        switchUser();
                        break;
                    case 7:
                        System.out.println("\n👋 Exiting WhatsApp Core. Goodbye!\n");
                        DatabaseConnection.getInstance().close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("\n Invalid choice. Please try again.\n");
                }
            }
        }
    }

    private static void displayAuthMenu(Scanner sc) {
        System.out.println("\n");
        System.out.println("      WhatsApp Core - Authentication");
        System.out.println("\n");
        System.out.println("1. Register New User");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.println("\n");
        System.out.print("Enter your choice: ");
        
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                registerUser(sc);
                break;
            case 2:
                loginUser(sc);
                break;
            case 3:
                System.out.println("\n👋 Goodbye!\n");
                System.exit(0);
                break;
            default:
                System.out.println("\nInvalid choice!\n");
        }
    }

    private static void loginUser(Scanner sc) {
        if (registeredUsers.isEmpty()) {
            System.out.println("\nNo users registered yet. Please register first.\n");
            return;
        }

        System.out.println("\n");
        System.out.println("        Available Users to Login");
        System.out.println("\n");
        
        for (int i = 0; i < registeredUsers.size(); i++) {
            User user = registeredUsers.get(i);
            System.out.println((i + 1) + ". " + user.getProfilename() + 
                             " (" + user.getPhoneNumber() + ")");
        }

        System.out.print("\nSelect user number to login: ");
        try {
            int selection = sc.nextInt();
            sc.nextLine();
            
            if (selection > 0 && selection <= registeredUsers.size()) {
                currentUser = registeredUsers.get(selection - 1);
                System.out.println("\n✅ Logged in as " + currentUser.getProfilename() + "\n");
            } else {
                System.out.println("\nInvalid selection!\n");
            }
        } catch (InputMismatchException e) {
            sc.nextLine();
            System.out.println("\nPlease enter a valid number!\n");
        }
    }

    private static void switchUser() {
        System.out.println("\n✅ Logged out from " + currentUser.getProfilename());
        currentUser = null;
    }

    private static void displayMainMenu() {
        System.out.println("\n");
        System.out.println("     WhatsApp Core - " + currentUser.getProfilename());
        System.out.println("\n");
        System.out.println("1. Register New User");
        System.out.println("2. Friends Management");
        System.out.println("3. Chat Management");
        System.out.println("4. Group Management");
        System.out.println("5. View Profile");
        System.out.println("6. Switch User / Logout");
        System.out.println("7. Exit");
        System.out.println("\n");
        System.out.print("Enter your choice: ");
    }

    private static void registerUser(Scanner sc) {
        System.out.println("\n");
        System.out.println("User Registration");
        System.out.println("\n");

        String userId = "WHP-" + UUID.randomUUID().toString().substring(0, 8);

        System.out.print("Enter Profile Name: ");
        String profileName = sc.nextLine();

        System.out.print("Enter Phone Number: ");
        String phoneNumber = sc.nextLine();

        currentUser = new User(userId, profileName, phoneNumber);
        registeredUsers.add(currentUser);

        // Push user to database
        if (userRepository != null) {
            userRepository.saveUser(currentUser);
        }

        System.out.println("\n✅ User Registered Successfully!");
        System.out.println("User ID: " + userId);
        System.out.println("Profile Name: " + profileName);
        System.out.println("Phone Number: " + phoneNumber + "\n");
    }

    private static void manageFriends(Scanner sc) {
        System.out.println("\n");
        System.out.println("Friends Management");
        System.out.println("\n");
        System.out.println("1. Add Friend");
        System.out.println("2. Remove Friend");
        System.out.println("3. View Friends");
        System.out.println("4. Back to Main Menu");
        System.out.println("\n");
        System.out.print("Enter your choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                addFriendByPhoneOrName(sc);
                break;
            case 2:
                System.out.print("Enter friend ID to remove: ");
                String friendToRemove = sc.nextLine();
                if (currentUser.removeFriend(friendToRemove)) {
                    // Update user in database
                    if (userRepository != null) {
                        userRepository.updateUser(currentUser);
                    }
                    System.out.println("✅ Friend removed successfully!\n");
                } else {
                    System.out.println("Friend not found!\n");
                }
                break;
            case 3:
                viewFriendsWithDetails();
                break;
            case 4:
                break;
            default:
                System.out.println("\nInvalid choice!\n");
        }
    }

    private static void addFriendByPhoneOrName(Scanner sc) {
        System.out.println("\n");
        System.out.println("Available Users to Add as Friend:");
        System.out.println("\n");
        List<User> availableUsers = new ArrayList<>();
        
        for (User user : registeredUsers) {
            if (!user.getUserId().equals(currentUser.getUserId()) && 
                !currentUser.getFriends().contains(user.getUserId())) {
                availableUsers.add(user);
            }
        }
        
        if (availableUsers.isEmpty()) {
            System.out.println("No users available to add as friend!\n");
            return;
        }
        
        for (int i = 0; i < availableUsers.size(); i++) {
            User user = availableUsers.get(i);
            System.out.println((i + 1) + ". " + user.getProfilename() + 
                             " | Phone: " + user.getPhoneNumber() + 
                             " | ID: " + user.getUserId());
        }
        
        System.out.print("\n Enter SELECTION NUMBER only (1, 2, 3...), NOT phone number: ");
        try {
            int selection = sc.nextInt();
            sc.nextLine();
            
            if (selection > 0 && selection <= availableUsers.size()) {
                User selectedFriend = availableUsers.get(selection - 1);
                if (currentUser.addFriend(selectedFriend.getUserId())) {
                    // Update user in database
                    if (userRepository != null) {
                        userRepository.updateUser(currentUser);
                    }
                    System.out.println("\n✅ " + selectedFriend.getProfilename() + " added as friend!\n");
                }
            } else {
                System.out.println("Invalid selection!\n");
            }
        } catch (InputMismatchException e) {
            sc.nextLine();
            System.out.println("\nPlease enter a valid number (1, 2, 3...) only!\n");
        }
    }

    private static void viewFriendsWithDetails() {
        List<String> friendIds = currentUser.getFriends();
        if (friendIds.isEmpty()) {
            System.out.println("\nNo friends yet!\n");
            return;
        }
        
        System.out.println("\n");
        System.out.println("         Your Friends");
        System.out.println("\n");
        for (String friendId : friendIds) {
            for (User user : registeredUsers) {
                if (user.getUserId().equals(friendId)) {
                    System.out.println("👤 Name: " + user.getProfilename());
                    System.out.println("📱 Phone: " + user.getPhoneNumber());
                    System.out.println("🆔 ID: " + user.getUserId());
                    System.out.println("\n");
                }
            }
        }
        System.out.println();
    }

    private static void manageChats(Scanner sc) {
        System.out.println("\n");
        System.out.println("Chat Management");
        System.out.println("\n");
        System.out.println("1. Start New Chat");
        System.out.println("2. Send Message");
        System.out.println("3. Send Reply");
        System.out.println("4. View Chat History");
        System.out.println("5. Back to Main Menu");
        System.out.println("\n");
        System.out.print("Enter your choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter friend ID to chat with: ");
                String friendId = sc.nextLine();
                Chat chat = chatManager.createOrGetChat(currentUser.getUserId(), friendId);
                currentUser.addChat(chat);
                System.out.println("Chat created successfully! Chat ID: " + chat.getChatId());
                break;

            case 2:
                System.out.print("Enter recipient ID: ");
                String recipientId = sc.nextLine();
                Chat chatToSend = chatManager.createOrGetChat(currentUser.getUserId(), recipientId);
                System.out.print("Enter message: ");
                String messageContent = sc.nextLine();
                chatManager.sendMessage(chatToSend, currentUser.getUserId(), messageContent);
                
                // Push chat and messages to database
                if (chatRepository != null) {
                    chatRepository.updateChat(chatToSend);
                }
                break;

            case 3:
                System.out.print("Enter recipient ID: ");
                String replyRecipient = sc.nextLine();
                Chat chatForReply = chatManager.createOrGetChat(currentUser.getUserId(), replyRecipient);
                System.out.print("Enter parent message ID to reply to: ");
                String parentMsgId = sc.nextLine();
                System.out.print("Enter reply message: ");
                String replyContent = sc.nextLine();
                chatManager.sendReply(chatForReply, parentMsgId, currentUser.getUserId(), replyContent);
                
                // Push updated chat to database
                if (chatRepository != null) {
                    chatRepository.updateChat(chatForReply);
                }
                break;

            case 4:
                System.out.print("Enter recipient ID: ");
                String chatRecipient = sc.nextLine();
                Chat chatToView = chatManager.createOrGetChat(currentUser.getUserId(), chatRecipient);
                chatManager.displayChat(chatToView);
                break;

            case 5:
                break;

            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void manageGroups(Scanner sc) {
        System.out.println("\n");
        System.out.println("Group Management");
        System.out.println("\n");
        System.out.println("1. Create Group");
        System.out.println("2. Add Member to Group");
        System.out.println("3. Send Group Message");
        System.out.println("4. Send Group Reply");
        System.out.println("5. View Group Messages");
        System.out.println("6. View Group Info");
        System.out.println("7. Back to Main Menu");
        System.out.println("\n");
        System.out.print("Enter your choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter group name: ");
                String groupName = sc.nextLine();
                Group group = groupManager.createGroup(groupName, currentUser.getUserId());
                currentUser.addGroup(group);
                
                // Push group to database
                if (groupRepository != null) {
                    groupRepository.saveGroup(group);
                }
                System.out.println("Group created successfully! Group ID: " + group.getGroupId());
                break;

            case 2:
                System.out.print("Enter group ID: ");
                String groupId = sc.nextLine();
                System.out.print("Enter member ID to add: ");
                String memberId = sc.nextLine();
                if (groupManager.addMemberToGroup(groupId, memberId)) {
                    // Update group in database
                    if (groupRepository != null) {
                        Group updatedGroup = groupManager.getGroupById(groupId);
                        if (updatedGroup != null) {
                            groupRepository.updateGroup(updatedGroup);
                        }
                    }
                    System.out.println("Member added successfully!");
                } else {
                    System.out.println("Failed to add member!");
                }
                break;

            case 3:
                System.out.print("Enter group ID: ");
                String groupIdMsg = sc.nextLine();
                System.out.print("Enter message: ");
                String groupMessage = sc.nextLine();
                groupManager.sendGroupMessage(groupIdMsg, currentUser.getUserId(), groupMessage);
                
                // Push updated group to database
                if (groupRepository != null) {
                    Group updatedGroup = groupManager.getGroupById(groupIdMsg);
                    if (updatedGroup != null) {
                        groupRepository.updateGroup(updatedGroup);
                    }
                }
                break;

            case 4:
                System.out.print("Enter group ID: ");
                String groupIdReply = sc.nextLine();
                System.out.print("Enter parent message ID: ");
                String parentGroupMsgId = sc.nextLine();
                System.out.print("Enter reply message: ");
                String groupReplyContent = sc.nextLine();
                groupManager.sendGroupReply(groupIdReply, parentGroupMsgId, currentUser.getUserId(), groupReplyContent);
                
                // Push updated group to database
                if (groupRepository != null) {
                    Group updatedGroup = groupManager.getGroupById(groupIdReply);
                    if (updatedGroup != null) {
                        groupRepository.updateGroup(updatedGroup);
                    }
                }
                break;

            case 5:
                System.out.print("Enter group ID: ");
                String groupIdView = sc.nextLine();
                groupManager.displayGroupMessages(groupIdView);
                break;

            case 6:
                System.out.print("Enter group ID: ");
                String groupIdInfo = sc.nextLine();
                groupManager.displayGroupInfo(groupIdInfo);
                break;

            case 7:
                break;

            default:
                System.out.println("Invalid choice!");
        }
    }
}
