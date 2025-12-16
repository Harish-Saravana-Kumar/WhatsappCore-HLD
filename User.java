import java.util.*;

public class User {
    private String userId;
    private String profilename;
    private String phoneNumber;
    private List<String> friends;
    private List<Chat> chats;
    private List<Group> groups;
    private Date lastUpdated;

    public User(String userId, String profilename, String phoneNumber) {
        this.userId = userId;
        this.profilename = profilename;
        this.phoneNumber = phoneNumber;
        this.friends = new ArrayList<>();
        this.chats = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.lastUpdated = new Date();
        System.out.println("User Registered Successfully!\n");
    }

    public User(String userId, String profilename, String phoneNumber, List<String> friends) {
        this(userId, profilename, phoneNumber);
        this.friends = new ArrayList<>(friends);
    }

    // Friend Management
    public boolean addFriend(String friendId) {
        if (!friends.contains(friendId) && !friendId.equals(userId)) {
            friends.add(friendId);
            return true;
        }
        return false;
    }

    public boolean removeFriend(String friendId) {
        return friends.remove(friendId);
    }

    public List<String> getFriends() {
        return new ArrayList<>(friends);
    }

    // Chat Management
    public Chat createChat(String otherUserId) {
        Chat chat = new Chat(userId, otherUserId);
        chats.add(chat);
        return chat;
    }

    public void addChat(Chat chat) {
        if (!chats.contains(chat)) {
            chats.add(chat);
        }
    }

    public Chat findChatWith(String otherUserId) {
        for (Chat chat : chats) {
            if ((chat.getUserId1().equals(userId) && chat.getUserId2().equals(otherUserId)) ||
                (chat.getUserId1().equals(otherUserId) && chat.getUserId2().equals(userId))) {
                return chat;
            }
        }
        return null;
    }

    public List<Chat> getChats() {
        return new ArrayList<>(chats);
    }

    // Group Management
    public Group createGroup(String groupName) {
        Group group = new Group(groupName, userId);
        groups.add(group);
        return group;
    }

    public void addGroup(Group group) {
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }

    public Group findGroupById(String groupId) {
        for (Group group : groups) {
            if (group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    // Profile Management
    public void setProfilename(String profilename) {
        this.profilename = profilename;
        this.lastUpdated = new Date();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.lastUpdated = new Date();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getProfilename() {
        return profilename;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void displayUserProfile() {
        System.out.println("\n=== User Profile ===");
        System.out.println("User ID: " + userId);
        System.out.println("Profile Name: " + profilename);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Friends: " + friends.size());
        System.out.println("Groups: " + groups.size());
        System.out.println("Chats: " + chats.size());
        System.out.println("====================\n");
    }
}
