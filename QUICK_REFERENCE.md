# MongoDB Integration - Quick Reference

## 🚀 Quick Start

### Build & Run
```bash
# Windows
cd H:\HLD
run.bat

# Linux/Mac
cd /path/to/HLD
chmod +x run.sh
./run.sh

# Manual
mvn clean install
mvn compile
mvn exec:java -Dexec.mainClass="whatsappCore"
```

---

## 📊 Database Schema at a Glance

### Collections Structure
```
whatsapp_db/
├── users/
│   ├── userId (unique)
│   ├── profilename
│   ├── phoneNumber
│   ├── friends (array)
│   └── lastUpdated
│
├── chats/
│   ├── chatId (unique)
│   ├── userId1
│   ├── userId2
│   ├── messageIds (array)
│   └── createdAt
│
├── messages/
│   ├── messageId (unique)
│   ├── senderId
│   ├── content
│   ├── timestamp
│   ├── parentMessageId (optional)
│   └── isReply
│
└── groups/
    ├── groupId (unique)
    ├── groupName
    ├── adminId
    ├── members (array)
    ├── messageIds (array)
    └── createdAt
```

---

## 🔌 Connection Details

**Connection String:**
```
mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0
```

**Database:** `whatsapp_db`

**Cluster:** cluster0.jtf357u.mongodb.net

---

## 📝 Repository Methods Cheat Sheet

### UserRepository
| Method | Purpose |
|--------|---------|
| `saveUser(User)` | Create new user |
| `getUserById(String)` | Get user by ID |
| `getAllUsers()` | Get all users |
| `updateUser(User)` | Update user info |
| `deleteUser(String)` | Delete user |

### ChatRepository
| Method | Purpose |
|--------|---------|
| `saveChat(Chat)` | Create new chat |
| `getChatById(String)` | Get chat by ID |
| `getChatBetweenUsers(String, String)` | Get specific chat |
| `getChatsByUser(String)` | Get user's chats |
| `updateChat(Chat)` | Update chat messages |
| `deleteChat(String)` | Delete chat |

### MessageRepository
| Method | Purpose |
|--------|---------|
| `saveMessage(Message)` | Create new message |
| `getMessageById(String)` | Get message by ID |
| `getMessagesBySender(String)` | Get sender's messages |
| `getAllMessages()` | Get all messages |
| `deleteMessage(String)` | Delete message |

### GroupRepository
| Method | Purpose |
|--------|---------|
| `saveGroup(Group)` | Create new group |
| `getGroupById(String)` | Get group by ID |
| `getGroupsByUser(String)` | Get user's groups |
| `getAllGroups()` | Get all groups |
| `updateGroup(Group)` | Update group |
| `deleteGroup(String)` | Delete group |

---

## 🔄 Common Operations

### Register User
```java
String userId = "WHP-" + UUID.randomUUID().toString().substring(0, 8);
User user = new User(userId, "John Doe", "9876543210");
userRepository.saveUser(user);
```

### Send Message
```java
Chat chat = chatManager.createOrGetChat(userId1, userId2);
chatManager.sendMessage(chat, userId1, "Hello!");
chatRepository.updateChat(chat);  // Push to DB
```

### Create Group
```java
Group group = groupManager.createGroup("Team Alpha", adminId);
groupRepository.saveGroup(group);  // Push to DB
```

### Add Friend
```java
currentUser.addFriend(friendId);
userRepository.updateUser(currentUser);  // Push to DB
```

---

## 📂 Project Structure

```
H:\HLD\
├── whatsappCore.java              # Main app (entry point)
├── User.java                       # User entity
├── Chat.java                       # Chat entity
├── Message.java                    # Message entity
├── Group.java                      # Group entity
├── ChatManager.java                # Chat operations
├── GroupManager.java               # Group operations
├── DatabaseConnection.java         # DB connection (Singleton)
├── UserRepository.java             # User CRUD
├── ChatRepository.java             # Chat CRUD
├── MessageRepository.java          # Message CRUD
├── GroupRepository.java            # Group CRUD
├── pom.xml                         # Maven config
├── application.properties          # Configuration
├── run.bat                         # Windows startup script
├── run.sh                          # Linux/Mac startup script
├── README.md                       # Main documentation
├── MONGODB_INTEGRATION_GUIDE.md   # Detailed guide
└── QUICK_REFERENCE.md             # This file
```

---

## 🎯 Data Flow Diagram

```
USER INPUT
    ↓
APPLICATION LOGIC (whatsappCore)
    ↓
ENTITY OBJECTS (User, Chat, Message, Group)
    ↓
REPOSITORY CLASSES (UserRepository, ChatRepository, etc.)
    ↓
MONGODB DRIVER (DatabaseConnection)
    ↓
MONGODB ATLAS (whatsapp_db)
    ↓
CLOUD PERSISTENCE
```

---

## 🔐 Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.11.1</version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.9</version>
</dependency>
```

---

## 🛠️ Useful Commands

### Maven Commands
```bash
# Download dependencies
mvn clean install

# Compile only
mvn compile

# Run application
mvn exec:java -Dexec.mainClass="whatsappCore"

# Skip tests (faster)
mvn clean install -DskipTests
```

### Git Commands (if using version control)
```bash
git init
git add .
git commit -m "Initial commit with MongoDB integration"
git branch mongodb-integration
```

---

## 📊 Example Queries in MongoDB Compass

### Find all users
```javascript
db.users.find({})
```

### Find user by ID
```javascript
db.users.findOne({ userId: "WHP-12345678" })
```

### Get all messages in a chat
```javascript
db.messages.find({ messageId: { $in: [...chatMessageIds] } })
```

### Get user's groups
```javascript
db.groups.find({ members: "WHP-12345678" })
```

---

## ✅ Checklist

### Before Running
- [ ] Java 11+ installed
- [ ] Maven installed
- [ ] Internet connection available
- [ ] MongoDB Atlas account active
- [ ] Cluster status is "RUNNING"
- [ ] IP whitelist includes your machine

### After Build
- [ ] No Maven errors
- [ ] All dependencies downloaded
- [ ] No compilation errors
- [ ] pom.xml has MongoDB driver

### After Running
- [ ] Application starts successfully
- [ ] "Connected to MongoDB Atlas" message shown
- [ ] Users can register
- [ ] Messages can be sent
- [ ] Data appears in MongoDB Atlas dashboard

---

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Maven not found | Install Maven from maven.apache.org |
| Java not found | Install Java 11+ from oracle.com |
| Cannot connect to MongoDB | Check internet, IP whitelist, connection string |
| ClassNotFoundException | Run `mvn clean install` |
| "No such file" | Check working directory is `H:\HLD` |
| Out of memory | Increase heap: `mvn -Xmx1024m compile` |

---

## 📞 Quick Help

**MongoDB Connection Issues?**
1. Check cluster status in Atlas dashboard
2. Verify IP whitelist includes your machine's IP
3. Test with MongoDB Compass if possible
4. Check network connectivity

**Build Issues?**
1. Delete `target/` folder
2. Run `mvn clean install`
3. Check Java version: `java -version`
4. Check Maven version: `mvn -version`

**Runtime Issues?**
1. Check application logs
2. Verify all repositories are initialized
3. Check database quota in Atlas
4. Review error messages carefully

---

**Last Updated**: December 16, 2025
**MongoDB Driver**: Version 4.11.1 (Java Sync)
**Java Target**: Java 11+
**Maven Target**: 3.6.0+
