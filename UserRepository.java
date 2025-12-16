import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.*;

public class UserRepository {
    private MongoCollection<Document> userCollection;
    private static final String COLLECTION_NAME = "users";

    public UserRepository() {
        this.userCollection = DatabaseConnection.getInstance().getCollection(COLLECTION_NAME);
    }

    // Push User to Database
    public boolean saveUser(User user) {
        try {
            Document userDoc = new Document()
                    .append("userId", user.getUserId())
                    .append("profilename", user.getProfilename())
                    .append("phoneNumber", user.getPhoneNumber())
                    .append("friends", user.getFriends())
                    .append("lastUpdated", new Date());

            userCollection.insertOne(userDoc);
            System.out.println("✓ User saved to database: " + user.getUserId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error saving user to database: " + e.getMessage());
            return false;
        }
    }

    // Pull User from Database
    public User getUserById(String userId) {
        try {
            Document userDoc = userCollection.find(Filters.eq("userId", userId)).first();
            
            if (userDoc != null) {
                @SuppressWarnings("unchecked")
                List<String> friends = (List<String>) userDoc.get("friends");
                if (friends == null) friends = new ArrayList<>();

                User user = new User(
                        userDoc.getString("userId"),
                        userDoc.getString("profilename"),
                        userDoc.getString("phoneNumber"),
                        friends
                );
                return user;
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    // Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            for (Document userDoc : userCollection.find()) {
                @SuppressWarnings("unchecked")
                List<String> friends = (List<String>) userDoc.get("friends");
                if (friends == null) friends = new ArrayList<>();

                User user = new User(
                        userDoc.getString("userId"),
                        userDoc.getString("profilename"),
                        userDoc.getString("phoneNumber"),
                        friends
                );
                users.add(user);
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving all users: " + e.getMessage());
        }
        return users;
    }

    // Update User
    public boolean updateUser(User user) {
        try {
            Document updateDoc = new Document()
                    .append("profilename", user.getProfilename())
                    .append("phoneNumber", user.getPhoneNumber())
                    .append("friends", user.getFriends())
                    .append("lastUpdated", new Date());

            userCollection.updateOne(
                    Filters.eq("userId", user.getUserId()),
                    new Document("$set", updateDoc)
            );
            System.out.println("✓ User updated in database: " + user.getUserId());
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error updating user: " + e.getMessage());
            return false;
        }
    }

    // Delete User
    public boolean deleteUser(String userId) {
        try {
            userCollection.deleteOne(Filters.eq("userId", userId));
            System.out.println("✓ User deleted from database: " + userId);
            return true;
        } catch (Exception e) {
            System.out.println("✗ Error deleting user: " + e.getMessage());
            return false;
        }
    }
}
