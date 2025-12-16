import java.util.*;

public class GroupManager {
    private Map<String, Group> allGroups;

    public GroupManager() {
        this.allGroups = new HashMap<>();
    }

    public Group createGroup(String groupName, String adminId) {
        Group group = new Group(groupName, adminId);
        allGroups.put(group.getGroupId(), group);
        return group;
    }

    public Group getGroupById(String groupId) {
        return allGroups.get(groupId);
    }

    public boolean addMemberToGroup(String groupId, String userId) {
        Group group = allGroups.get(groupId);
        if (group != null) {
            return group.addMember(userId);
        }
        return false;
    }

    public boolean removeMemberFromGroup(String groupId, String userId) {
        Group group = allGroups.get(groupId);
        if (group != null) {
            return group.removeMember(userId);
        }
        return false;
    }

    public void sendGroupMessage(String groupId, String senderId, String messageContent) {
        Group group = allGroups.get(groupId);
        if (group != null && group.getMembers().contains(senderId)) {
            String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8);
            Message message = new Message(messageId, senderId, messageContent);
            group.addMessage(message);
            System.out.println("Message sent to group successfully!");
        } else {
            System.out.println("User is not a member of this group!");
        }
    }

    public void sendGroupReply(String groupId, String messageId, String senderId, String replyContent) {
        Group group = allGroups.get(groupId);
        if (group != null && group.getMembers().contains(senderId)) {
            Message parentMessage = group.getMessageById(messageId);
            if (parentMessage != null) {
                group.addReply(messageId, senderId, replyContent);
                System.out.println("Reply sent to group successfully!");
            } else {
                System.out.println("Parent message not found!");
            }
        } else {
            System.out.println("User is not a member of this group!");
        }
    }

    public void displayGroupMessages(String groupId) {
        Group group = allGroups.get(groupId);
        if (group != null) {
            group.displayGroupMessages();
        }
    }

    public void displayGroupInfo(String groupId) {
        Group group = allGroups.get(groupId);
        if (group != null) {
            group.displayGroupInfo();
        }
    }
}
