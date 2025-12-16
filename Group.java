import java.util.*;

public class Group {
    private String groupId;
    private String groupName;
    private String adminId;
    private List<String> members;
    private List<Message> messages;
    private Date createdAt;

    public Group(String groupName, String adminId) {
        this.groupId = "GRP-" + UUID.randomUUID().toString().substring(0, 8);
        this.groupName = groupName;
        this.adminId = adminId;
        this.members = new ArrayList<>();
        this.members.add(adminId);
        this.messages = new ArrayList<>();
        this.createdAt = new Date();
    }

    public boolean addMember(String userId) {
        if (!members.contains(userId)) {
            members.add(userId);
            return true;
        }
        return false;
    }

    public boolean removeMember(String userId) {
        if (!userId.equals(adminId) && members.contains(userId)) {
            members.remove(userId);
            return true;
        }
        return false;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void addReply(String messageId, String senderId, String replyContent) {
        if (!members.contains(senderId)) {
            System.out.println("User is not a member of this group!");
            return;
        }
        String replyId = "MSG-" + UUID.randomUUID().toString().substring(0, 8);
        Message reply = new Message(replyId, senderId, replyContent, messageId);
        messages.add(reply);
    }

    public Message getMessageById(String messageId) {
        for (Message msg : messages) {
            if (msg.getMessageId().equals(messageId)) {
                return msg;
            }
        }
        return null;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getAdminId() {
        return adminId;
    }

    public List<String> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void displayGroupInfo() {
        System.out.println("\n=== Group Info ===");
        System.out.println("Group ID: " + groupId);
        System.out.println("Group Name: " + groupName);
        System.out.println("Admin: " + adminId);
        System.out.println("Members: " + members);
        System.out.println("Total Messages: " + messages.size());
        System.out.println("==================\n");
    }

    public void displayGroupMessages() {
        System.out.println("\n=== Group Messages: " + groupName + " ===");
        if (messages.isEmpty()) {
            System.out.println("No messages yet.");
            return;
        }
        for (Message msg : messages) {
            System.out.println(msg);
        }
        System.out.println("========================================\n");
    }
}
