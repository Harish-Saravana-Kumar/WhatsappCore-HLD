package com.whatsapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.*;

public class GroupRepository {
    private MongoCollection<Document> groupCollection;
    private MessageRepository messageRepository;
    private static final String COLLECTION_NAME = "groups";

    public GroupRepository() {
        this.groupCollection = DatabaseConnection.getInstance().getCollection(COLLECTION_NAME);
        this.messageRepository = new MessageRepository();
    }

    // Push Group to Database
    public boolean saveGroup(Group group) {
        try {
            Document groupDoc = new Document()
                    .append("groupId", group.getGroupId())
                    .append("groupName", group.getGroupName())
                    .append("adminId", group.getAdminId())
                    .append("members", group.getMembers())
                    .append("messageIds", getMessageIds(group.getMessages()))
                    .append("createdAt", group.getCreatedAt());

            groupCollection.insertOne(groupDoc);
            
            // Save all messages
            for (Message msg : group.getMessages()) {
                messageRepository.saveMessage(msg);
            }
            
            System.out.println("✓ Group saved to database: " + group.getGroupId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error saving group: " + e.getMessage());
            return false;
        }
    }

    // Pull Group from Database
    public Group getGroupById(String groupId) {
        try {
            Document groupDoc = groupCollection.find(Filters.eq("groupId", groupId)).first();
            
            if (groupDoc != null) {
                return documentToGroup(groupDoc);
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving group: " + e.getMessage());
        }
        return null;
    }

    // Get all groups for a user
    public List<Group> getGroupsByUser(String userId) {
        List<Group> groups = new ArrayList<>();
        try {
            for (Document groupDoc : groupCollection.find(
                    Filters.in("members", userId)
            )) {
                groups.add(documentToGroup(groupDoc));
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving groups: " + e.getMessage());
        }
        return groups;
    }

    // Get all groups
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        try {
            for (Document groupDoc : groupCollection.find()) {
                groups.add(documentToGroup(groupDoc));
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving all groups: " + e.getMessage());
        }
        return groups;
    }

    // Update Group
    public boolean updateGroup(Group group) {
        try {
            Document updateDoc = new Document()
                    .append("groupName", group.getGroupName())
                    .append("members", group.getMembers())
                    .append("messageIds", getMessageIds(group.getMessages()));

            groupCollection.updateOne(
                    Filters.eq("groupId", group.getGroupId()),
                    new Document("$set", updateDoc)
            );
            
            // Update messages
            for (Message msg : group.getMessages()) {
                messageRepository.saveMessage(msg);
            }
            
            System.out.println("✓ Group updated in database: " + group.getGroupId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error updating group: " + e.getMessage());
            return false;
        }
    }

    // Delete Group
    public boolean deleteGroup(String groupId) {
        try {
            groupCollection.deleteOne(Filters.eq("groupId", groupId));
            System.out.println("✓ Group deleted from database: " + groupId);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error deleting group: " + e.getMessage());
            return false;
        }
    }

    // Helper methods
    private List<String> getMessageIds(List<Message> messages) {
        List<String> messageIds = new ArrayList<>();
        for (Message msg : messages) {
            messageIds.add(msg.getMessageId());
        }
        return messageIds;
    }

    private Group documentToGroup(Document doc) {
        @SuppressWarnings("unchecked")
        List<String> members = (List<String>) doc.get("members");
        
        String groupId = doc.getString("groupId");
        String groupName = doc.getString("groupName");
        String adminId = doc.getString("adminId");
        
        // Use constructor that takes groupId to avoid regenerating it
        Group group = new Group(groupId, groupName, adminId);
        
        // Set all members from database
        if (members != null) {
            for (String member : members) {
                group.addMember(member);
            }
        }
        
        // Reconstruct messages from IDs
        @SuppressWarnings("unchecked")
        List<String> messageIds = (List<String>) doc.get("messageIds");
        if (messageIds != null) {
            for (String messageId : messageIds) {
                Message msg = messageRepository.getMessageById(messageId);
                if (msg != null) {
                    group.addMessage(msg);
                }
            }
        }
        
        return group;
    }
}
