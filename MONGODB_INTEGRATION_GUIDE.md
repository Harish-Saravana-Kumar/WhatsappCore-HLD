# WhatsApp Core LLD - MongoDB Integration Guide

## 📋 Overview
This project integrates **MongoDB Atlas** with a Java-based WhatsApp clone application using the MongoDB Java Driver (not Mongoose, which is for Node.js).

Your MongoDB Atlas Connection String:
```
mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0
```

Database Name: `whatsapp_db`

---

## 🗄️ Database Architecture

### Collections in MongoDB

#### 1. **users** Collection
Stores user information
```json
{
  "_id": ObjectId,
  "userId": "WHP-12345678",
  "profilename": "John Doe",
  "phoneNumber": "+91-9876543210",
  "friends": ["WHP-87654321", "WHP-11111111"],
  "lastUpdated": ISODate("2025-12-16T10:30:00Z")
}
```

#### 2. **messages** Collection
Stores all messages from chats and groups
```json
{
  "_id": ObjectId,
  "messageId": "MSG-abcd1234",
  "senderId": "WHP-12345678",
  "content": "Hello, how are you?",
  "timestamp": ISODate("2025-12-16T10:30:00Z"),
  "parentMessageId": null,
  "isReply": false
}
```

#### 3. **chats** Collection
Stores 1-on-1 chat information
```json
{
  "_id": ObjectId,
  "chatId": "CHAT-xyz12345",
  "userId1": "WHP-12345678",
  "userId2": "WHP-87654321",
  "messageIds": ["MSG-abcd1234", "MSG-defg5678"],
  "createdAt": ISODate("2025-12-16T09:00:00Z")
}
```

#### 4. **groups** Collection
Stores group information
```json
{
  "_id": ObjectId,
  "groupId": "GRP-group123",
  "groupName": "Work Team",
  "adminId": "WHP-12345678",
  "members": ["WHP-12345678", "WHP-87654321", "WHP-22222222"],
  "messageIds": ["MSG-abcd1234", "MSG-defg5678"],
  "createdAt": ISODate("2025-12-16T08:00:00Z")
}
```

---

## 🚀 Setup Instructions

### Prerequisites
- Java 11 or higher
- Maven 3.6.0 or higher
- MongoDB Atlas account (already created)

### Step 1: Add MongoDB Driver Dependency

The `pom.xml` already includes the MongoDB Java Driver:
```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.11.1</version>
</dependency>
```

### Step 2: Build the Project

```bash
cd H:\HLD
mvn clean install
```

### Step 3: Run the Application

```bash
mvn compile
mvn exec:java -Dexec.mainClass="whatsappCore"
```

Or compile and run manually:
```bash
javac -cp "path/to/mongodb-driver-sync.jar;." *.java
java -cp "path/to/mongodb-driver-sync.jar;." whatsappCore
```

---

## 📝 Code Structure & Database Operations

### DatabaseConnection.java
**Singleton pattern** for managing MongoDB connection
- Initializes connection to MongoDB Atlas
- Provides access to database and collections
- Automatically closes connection on application exit

```java
DatabaseConnection.getInstance();  // Get singleton instance
```

### Repository Classes

#### UserRepository.java
**CRUD operations for Users**
- `saveUser(User)` - Push user to database (INSERT)
- `getUserById(String)` - Pull user from database (READ)
- `getAllUsers()` - Fetch all users (READ ALL)
- `updateUser(User)` - Update user info (UPDATE)
- `deleteUser(String)` - Delete user (DELETE)

#### MessageRepository.java
**CRUD operations for Messages**
- `saveMessage(Message)` - Push message to database (INSERT)
- `getMessageById(String)` - Pull message from database (READ)
- `getMessagesBySender(String)` - Get messages by sender (READ)
- `getAllMessages()` - Fetch all messages (READ ALL)
- `deleteMessage(String)` - Delete message (DELETE)

#### ChatRepository.java
**CRUD operations for Chats**
- `saveChat(Chat)` - Push chat to database (INSERT)
- `getChatById(String)` - Pull chat from database (READ)
- `getChatBetweenUsers(String, String)` - Get specific chat (READ)
- `getChatsByUser(String)` - Get all chats for a user (READ)
- `updateChat(Chat)` - Update chat with new messages (UPDATE)
- `deleteChat(String)` - Delete chat (DELETE)

#### GroupRepository.java
**CRUD operations for Groups**
- `saveGroup(Group)` - Push group to database (INSERT)
- `getGroupById(String)` - Pull group from database (READ)
- `getGroupsByUser(String)` - Get all groups for a user (READ)
- `getAllGroups()` - Fetch all groups (READ ALL)
- `updateGroup(Group)` - Update group info (UPDATE)
- `deleteGroup(String)` - Delete group (DELETE)

---

## 🔄 Data Flow in Operations

### User Registration
1. User enters profile name and phone number
2. New User object created with unique userId
3. **Push to Database**: `userRepository.saveUser(user)`
4. User added to local registeredUsers list

### Sending a Message
1. User selects "Send Message"
2. Message object created with unique messageId
3. Message added to Chat object
4. **Push to Database**: `chatRepository.updateChat(chat)`
   - All messages in chat are saved to `messages` collection
   - Chat metadata updated in `chats` collection

### Adding a Friend
1. User selects friend from available list
2. Friend ID added to user's friends list
3. **Push to Database**: `userRepository.updateUser(currentUser)`

### Creating a Group
1. User enters group name
2. New Group object created with unique groupId
3. **Push to Database**: `groupRepository.saveGroup(group)`
4. Group added to user's groups list

---

## 📊 Data Persistence Strategy

### On Startup
```java
// Load users from database
registeredUsers.addAll(userRepository.getAllUsers());
```

### On User Actions
Each operation that modifies data triggers a database update:
- Register User → `userRepository.saveUser()`
- Add Friend → `userRepository.updateUser()`
- Send Message → `chatRepository.updateChat()`
- Create Group → `groupRepository.saveGroup()`
- Add Group Member → `groupRepository.updateGroup()`

### On Application Exit
```java
DatabaseConnection.getInstance().close();
```

---

## 🔍 Example Operations

### Push Data (CREATE/UPDATE)
```java
// Save new user
User newUser = new User("WHP-12345", "Alice", "9876543210");
userRepository.saveUser(newUser);

// Send message and update chat
Chat chat = chatManager.createOrGetChat(userId1, userId2);
chatManager.sendMessage(chat, userId1, "Hello!");
chatRepository.updateChat(chat);
```

### Pull Data (READ)
```java
// Get user by ID
User user = userRepository.getUserById("WHP-12345");

// Get all users
List<User> allUsers = userRepository.getAllUsers();

// Get chat between two users
Chat chat = chatRepository.getChatBetweenUsers(userId1, userId2);

// Get user's chats
List<Chat> userChats = chatRepository.getChatsByUser(userId);
```

---

## ⚠️ Important Notes

1. **Connection String**: Uses your provided Atlas credentials
2. **Database Name**: `whatsapp_db` (auto-created on first write)
3. **Collections**: Auto-created when first document is inserted
4. **Unique IDs**: All entities use UUID-based unique identifiers
5. **Timestamps**: All documents store creation/update timestamps
6. **Network**: Requires internet connection for Atlas connectivity

---

## 🛠️ Troubleshooting

### Connection Issues
- Verify MongoDB Atlas cluster is active
- Check IP whitelist includes your machine's IP
- Ensure connection string is correct

### Build Issues
- Ensure Java 11+ installed: `java -version`
- Ensure Maven installed: `mvn -version`
- Run: `mvn clean install`

### Runtime Issues
- Check database logs for errors
- Verify all repositories are initialized before use
- Ensure sufficient MongoDB Atlas quota

---

## 📚 File Summary

| File | Purpose |
|------|---------|
| `DatabaseConnection.java` | MongoDB connection management (Singleton) |
| `UserRepository.java` | User CRUD operations |
| `MessageRepository.java` | Message CRUD operations |
| `ChatRepository.java` | Chat CRUD operations |
| `GroupRepository.java` | Group CRUD operations |
| `whatsappCore.java` | Main application with integrated DB calls |
| `pom.xml` | Maven configuration with MongoDB driver dependency |

---

## 🎯 Next Steps

1. Run `mvn clean install` to download dependencies
2. Execute the application
3. Create users and test database operations
4. Check MongoDB Atlas dashboard for data insertion
5. Verify data persistence across application restarts

---

**Connection String (for reference)**:
```
mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0
```

**Database**: `whatsapp_db`
