import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.*;

public class ChatRepository {
    private MongoCollection<Document> chatCollection;
    private MessageRepository messageRepository;
    private static final String COLLECTION_NAME = "chats";

    public ChatRepository() {
        this.chatCollection = DatabaseConnection.getInstance().getCollection(COLLECTION_NAME);
        this.messageRepository = new MessageRepository();
    }

    // Push Chat to Database
    public boolean saveChat(Chat chat) {
        try {
            Document chatDoc = new Document()
                    .append("chatId", chat.getChatId())
                    .append("userId1", chat.getUserId1())
                    .append("userId2", chat.getUserId2())
                    .append("messageIds", getMessageIds(chat.getMessages()))
                    .append("createdAt", chat.getCreatedAt());

            chatCollection.insertOne(chatDoc);
            
            // Save all messages
            for (Message msg : chat.getMessages()) {
                messageRepository.saveMessage(msg);
            }
            
            System.out.println("✓ Chat saved to database: " + chat.getChatId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error saving chat: " + e.getMessage());
            return false;
        }
    }

    // Pull Chat from Database
    public Chat getChatById(String chatId) {
        try {
            Document chatDoc = chatCollection.find(Filters.eq("chatId", chatId)).first();
            
            if (chatDoc != null) {
                return documentToChat(chatDoc);
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving chat: " + e.getMessage());
        }
        return null;
    }

    // Get chat between two users
    public Chat getChatBetweenUsers(String userId1, String userId2) {
        try {
            Document chatDoc = chatCollection.find(
                    Filters.or(
                            Filters.and(
                                    Filters.eq("userId1", userId1),
                                    Filters.eq("userId2", userId2)
                            ),
                            Filters.and(
                                    Filters.eq("userId1", userId2),
                                    Filters.eq("userId2", userId1)
                            )
                    )
            ).first();
            
            if (chatDoc != null) {
                return documentToChat(chatDoc);
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving chat: " + e.getMessage());
        }
        return null;
    }

    // Get all chats for a user
    public List<Chat> getChatsByUser(String userId) {
        List<Chat> chats = new ArrayList<>();
        try {
            for (Document chatDoc : chatCollection.find(
                    Filters.or(
                            Filters.eq("userId1", userId),
                            Filters.eq("userId2", userId)
                    )
            )) {
                chats.add(documentToChat(chatDoc));
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving chats: " + e.getMessage());
        }
        return chats;
    }

    // Update Chat
    public boolean updateChat(Chat chat) {
        try {
            Document updateDoc = new Document()
                    .append("messageIds", getMessageIds(chat.getMessages()));

            chatCollection.updateOne(
                    Filters.eq("chatId", chat.getChatId()),
                    new Document("$set", updateDoc)
            );
            
            // Update messages
            for (Message msg : chat.getMessages()) {
                messageRepository.saveMessage(msg);
            }
            
            System.out.println("✓ Chat updated in database: " + chat.getChatId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error updating chat: " + e.getMessage());
            return false;
        }
    }

    // Delete Chat
    public boolean deleteChat(String chatId) {
        try {
            chatCollection.deleteOne(Filters.eq("chatId", chatId));
            System.out.println("✓ Chat deleted from database: " + chatId);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error deleting chat: " + e.getMessage());
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

    private Chat documentToChat(Document doc) {
        Chat chat = new Chat(
                doc.getString("userId1"),
                doc.getString("userId2")
        );
        
        // Reconstruct messages from IDs
        @SuppressWarnings("unchecked")
        List<String> messageIds = (List<String>) doc.get("messageIds");
        if (messageIds != null) {
            for (String messageId : messageIds) {
                Message msg = messageRepository.getMessageById(messageId);
                if (msg != null) {
                    chat.addMessage(msg);
                }
            }
        }
        
        return chat;
    }
}
