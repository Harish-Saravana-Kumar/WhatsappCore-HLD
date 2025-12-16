# WhatsApp Core LLD with MongoDB Integration - Complete Implementation Summary

## 🎯 Project Overview

A **Low-Level Design (LLD)** of WhatsApp with full **MongoDB Atlas integration** using Java MongoDB Driver for persistent data storage in the cloud.

**Technology Stack:**
- **Language**: Java 11+
- **Database**: MongoDB Atlas (Cloud)
- **Build Tool**: Maven
- **Database Driver**: MongoDB Java Driver (Sync)
- **Pattern**: Repository Pattern with Singleton for DB Connection

---

## 📦 Project Files Created

### Core Application Files
1. **whatsappCore.java** - Main application with integrated database operations
2. **User.java** - User entity class
3. **Chat.java** - Chat entity for 1-on-1 messaging
4. **Message.java** - Message entity with reply support
5. **Group.java** - Group entity for group messaging
6. **ChatManager.java** - Manages chat operations
7. **GroupManager.java** - Manages group operations

### Database Layer Files
1. **DatabaseConnection.java** - Singleton for MongoDB connection
2. **UserRepository.java** - User CRUD operations
3. **ChatRepository.java** - Chat CRUD operations
4. **MessageRepository.java** - Message CRUD operations
5. **GroupRepository.java** - Group CRUD operations

### Configuration & Build Files
1. **pom.xml** - Maven project configuration with dependencies
2. **application.properties** - Configuration properties
3. **run.bat** - Windows batch script to build and run
4. **run.sh** - Linux/Mac bash script to build and run

### Documentation Files
1. **MONGODB_INTEGRATION_GUIDE.md** - Complete integration guide
2. **README.md** - This file

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────┐
│         WhatsApp Core Application               │
│  (whatsappCore.java - Main Entry Point)         │
└─────────────────┬───────────────────────────────┘
                  │
        ┌─────────┴──────────┬──────────┬──────────┐
        │                    │          │          │
    ┌───▼──────┐  ┌─────────▼──┐ ┌───▼──────┐ ┌─▼───────────┐
    │   User   │  │   Chat     │ │ Message  │ │   Group     │
    │ Manager  │  │ Manager    │ │ Manager  │ │ Manager     │
    └─────┬────┘  └──────┬─────┘ └────┬─────┘ └─┬───────────┘
          │               │            │         │
          └───────────────┼────────────┼─────────┘
                          │            │
                ┌─────────▼────┬───────▼──────┐
                │              │              │
          ┌─────▼──────┐  ┌───▼──────┐  ┌──▼─────────┐
          │ Repository │  │ Repository │  │ Repository │
          │  Classes   │  │  Classes  │  │  Classes  │
          └─────┬──────┘  └───┬──────┘  └──┬─────────┘
                │             │           │
                └─────────────┼───────────┘
                              │
          ┌───────────────────▼────────────────┐
          │   DatabaseConnection (Singleton)   │
          │      (MongoDB Atlas Driver)        │
          └───────────────────┬────────────────┘
                              │
          ┌───────────────────▼────────────────┐
          │     MongoDB Atlas Cloud            │
          │   (whatsapp_db database)           │
          │  - users collection                │
          │  - chats collection                │
          │  - messages collection             │
          │  - groups collection               │
          └────────────────────────────────────┘
```

---

## 📊 Data Model

### Users Collection
```
{
  userId: String (Primary Key)
  profilename: String
  phoneNumber: String
  friends: Array of String (User IDs)
  lastUpdated: Date
}
```

### Messages Collection
```
{
  messageId: String (Primary Key)
  senderId: String (User ID)
  content: String
  timestamp: Date
  parentMessageId: String (Optional - for replies)
  isReply: Boolean
}
```

### Chats Collection
```
{
  chatId: String (Primary Key)
  userId1: String (User ID)
  userId2: String (User ID)
  messageIds: Array of String (Message IDs)
  createdAt: Date
}
```

### Groups Collection
```
{
  groupId: String (Primary Key)
  groupName: String
  adminId: String (User ID)
  members: Array of String (User IDs)
  messageIds: Array of String (Message IDs)
  createdAt: Date
}
```

---

## 🔄 CRUD Operations Implementation

### Repository Pattern Usage

Each entity has its own repository class implementing CRUD operations:

#### User Operations
| Operation | Method | Type |
|-----------|--------|------|
| Create | `saveUser(User)` | INSERT |
| Read | `getUserById(String)` | SELECT |
| Read All | `getAllUsers()` | SELECT * |
| Update | `updateUser(User)` | UPDATE |
| Delete | `deleteUser(String)` | DELETE |

#### Similar for Chat, Message, and Group repositories

### Database Persistence Points

**Data is pushed to database when:**
1. User registration → `userRepository.saveUser()`
2. Adding friend → `userRepository.updateUser()`
3. Sending message → `chatRepository.updateChat()` or `messageRepository.saveMessage()`
4. Creating group → `groupRepository.saveGroup()`
5. Adding group member → `groupRepository.updateGroup()`

**Data is pulled from database when:**
1. Application startup → `userRepository.getAllUsers()`
2. Looking up user → `userRepository.getUserById()`
3. Viewing chat history → `chatRepository.getChatBetweenUsers()`
4. Joining group → `groupRepository.getGroupById()`

---

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6.0 or higher
- Internet connection (for MongoDB Atlas)

### Installation & Execution

#### Option 1: Using Batch Script (Windows)
```bash
cd H:\HLD
run.bat
```

#### Option 2: Using Bash Script (Linux/Mac)
```bash
cd /path/to/HLD
chmod +x run.sh
./run.sh
```

#### Option 3: Manual Maven Commands
```bash
cd H:\HLD
mvn clean install
mvn compile
mvn exec:java -Dexec.mainClass="whatsappCore"
```

---

## 💾 MongoDB Atlas Connection Details

**Connection String:**
```
mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0
```

**Database Name:** `whatsapp_db`

**Collections:**
- `users` - Stores user profiles
- `chats` - Stores 1-on-1 conversations
- `messages` - Stores all messages
- `groups` - Stores group information

---

## 📝 Application Features

### User Management
- ✅ Register new users
- ✅ Login/Logout
- ✅ View user profile
- ✅ Add/Remove friends
- ✅ Persistent storage in MongoDB

### Chat Management
- ✅ Start new 1-on-1 chats
- ✅ Send messages
- ✅ Send replies to messages
- ✅ View chat history
- ✅ Persistent message storage

### Group Management
- ✅ Create groups
- ✅ Add/Remove members
- ✅ Send group messages
- ✅ Send replies in groups
- ✅ View group info and messages
- ✅ Persistent group storage

### Database Features
- ✅ Cloud-based persistence (MongoDB Atlas)
- ✅ Automatic data synchronization
- ✅ Data loaded on application startup
- ✅ Real-time database updates
- ✅ Connection pooling and error handling

---

## 🔧 Key Classes & Methods

### DatabaseConnection.java
- `getInstance()` - Get singleton connection
- `getDatabase()` - Access MongoDB database
- `getCollection(String)` - Get specific collection
- `close()` - Close connection

### UserRepository.java
- `saveUser(User)` - Push new user to DB
- `getUserById(String)` - Pull user from DB
- `getAllUsers()` - Get all users from DB
- `updateUser(User)` - Update user in DB
- `deleteUser(String)` - Delete user from DB

### Similar structure for ChatRepository, MessageRepository, GroupRepository

### whatsappCore.java
- `main()` - Application entry point with DB initialization
- `registerUser()` - Register user with DB save
- `manageFriends()` - Manage friends with DB updates
- `manageChats()` - Chat operations with DB persistence
- `manageGroups()` - Group operations with DB persistence

---

## 📈 Data Flow Examples

### Example 1: User Registration
```
User Input → Create User Object → saveUser() → MongoDB Insert → Confirmation
```

### Example 2: Sending a Message
```
User sends message → Message Object Created → Chat Updated → updateChat() 
→ MongoDB Update (both chats & messages collections) → Confirmation
```

### Example 3: Application Startup
```
App Start → Initialize DBConnection → Load Users (getAllUsers()) 
→ Populate registeredUsers List → Ready for Operations
```

---

## ⚠️ Important Considerations

1. **Network Dependency**: Application requires internet connection for MongoDB Atlas
2. **Connection Pooling**: MongoDB driver automatically manages connection pool
3. **Error Handling**: Repository classes catch and log database errors
4. **Data Consistency**: Messages stored separately but referenced by ID in chats/groups
5. **Timestamps**: All entities include creation/update timestamps
6. **Unique IDs**: UUID-based generation ensures global uniqueness

---

## 🐛 Troubleshooting

### Build Issues
```
ERROR: Maven not found
Solution: Install Maven from https://maven.apache.org/download.cgi

ERROR: Java not found
Solution: Install Java 11+ from https://www.oracle.com/java/technologies/downloads/
```

### Connection Issues
```
ERROR: Cannot connect to MongoDB Atlas
Solution: 
- Verify cluster is running
- Check IP whitelist in MongoDB Atlas
- Verify connection string is correct
- Check internet connection
```

### Runtime Issues
```
ERROR: ClassNotFoundException for MongoDB driver
Solution: Run mvn clean install to download dependencies
```

---

## 📚 File Dependencies

```
whatsappCore.java
  ├── User.java
  ├── Chat.java
  ├── Message.java
  ├── Group.java
  ├── ChatManager.java
  ├── GroupManager.java
  ├── UserRepository.java
  ├── ChatRepository.java
  ├── MessageRepository.java
  ├── GroupRepository.java
  └── DatabaseConnection.java
       └── MongoDB Java Driver
           └── MongoDB Atlas Cloud
```

---

## 🎓 Learning Resources

### MongoDB Java Driver
- [Official Documentation](https://www.mongodb.com/docs/drivers/java/sync/current/)
- [API Reference](https://mongodb.github.io/mongo-java-driver/4.11/)

### MongoDB Atlas
- [Getting Started](https://www.mongodb.com/docs/atlas/getting-started/)
- [Connection Guide](https://www.mongodb.com/docs/atlas/driver-connection/)

### Java Best Practices
- Singleton Pattern
- Repository Pattern
- Exception Handling
- Connection Pooling

---

## 🎯 Next Steps

1. ✅ Build the project: `mvn clean install`
2. ✅ Run the application: `mvn exec:java -Dexec.mainClass="whatsappCore"`
3. ✅ Create test users and data
4. ✅ Verify data in MongoDB Atlas dashboard
5. ✅ Test data persistence across app restarts
6. ✅ Monitor database operations and performance

---

## 📞 Support

For issues or questions:
1. Check [MONGODB_INTEGRATION_GUIDE.md](MONGODB_INTEGRATION_GUIDE.md) for detailed guide
2. Review MongoDB official documentation
3. Check application logs for error messages
4. Verify MongoDB Atlas cluster status

---

**Created**: December 16, 2025
**MongoDB Connection**: `mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0`
**Database**: `whatsapp_db`
**Driver**: MongoDB Java Driver (Sync) v4.11.1
