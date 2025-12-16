import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DatabaseConnection {
    private static DatabaseConnection instance = null;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String CONNECTION_STRING = 
        "mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "whatsapp_db";

    private DatabaseConnection() {
        try {
            this.mongoClient = MongoClients.create(CONNECTION_STRING);
            this.database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("✓ Connected to MongoDB Atlas successfully!");
        } catch (Exception e) {
            System.out.println("✗ Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}
