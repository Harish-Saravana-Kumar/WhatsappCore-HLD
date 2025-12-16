import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.*;

public class MessageRepository {
    private MongoCollection<Document> messageCollection;
    private static final String COLLECTION_NAME = "messages";

    public MessageRepository() {
        this.messageCollection = DatabaseConnection.getInstance().getCollection(COLLECTION_NAME);
    }

    // Push Message to Database
    public boolean saveMessage(Message message) {
        try {
            Document messageDoc = new Document()
                    .append("messageId", message.getMessageId())
                    .append("senderId", message.getSenderId())
                    .append("content", message.getContent())
                    .append("timestamp", message.getTimestamp())
                    .append("parentMessageId", message.getParentMessageId())
                    .append("isReply", message.isReply());

            messageCollection.insertOne(messageDoc);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error saving message: " + e.getMessage());
            return false;
        }
    }

    // Pull Message from Database
    public Message getMessageById(String messageId) {
        try {
            Document messageDoc = messageCollection.find(Filters.eq("messageId", messageId)).first();
            
            if (messageDoc != null) {
                return documentToMessage(messageDoc);
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving message: " + e.getMessage());
        }
        return null;
    }

    // Get messages by sender
    public List<Message> getMessagesBySender(String senderId) {
        List<Message> messages = new ArrayList<>();
        try {
            for (Document messageDoc : messageCollection.find(Filters.eq("senderId", senderId))) {
                messages.add(documentToMessage(messageDoc));
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }

    // Get all messages
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        try {
            for (Document messageDoc : messageCollection.find()) {
                messages.add(documentToMessage(messageDoc));
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving all messages: " + e.getMessage());
        }
        return messages;
    }

    // Delete Message
    public boolean deleteMessage(String messageId) {
        try {
            messageCollection.deleteOne(Filters.eq("messageId", messageId));
            System.out.println("✓ Message deleted from database: " + messageId);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error deleting message: " + e.getMessage());
            return false;
        }
    }

    // Helper method to convert Document to Message
    private Message documentToMessage(Document doc) {
        String messageId = doc.getString("messageId");
        String senderId = doc.getString("senderId");
        String content = doc.getString("content");
        String parentMessageId = doc.getString("parentMessageId");

        if (parentMessageId != null && !parentMessageId.isEmpty()) {
            return new Message(messageId, senderId, content, parentMessageId);
        } else {
            return new Message(messageId, senderId, content);
        }
    }
}
