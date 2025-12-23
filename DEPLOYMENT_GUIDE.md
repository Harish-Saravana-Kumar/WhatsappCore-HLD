# WhatsApp Core LLD - Deployment Guide

## 📋 Project Structure

```
WhatsApp Core LLD/
├── backend/
│   ├── src/
│   │   └── main/java/com/whatsapp/
│   │       ├── RestServer.java          (HTTP API Server)
│   │       ├── WebSocketServer.java     (Real-time messaging)
│   │       ├── DatabaseConnection.java  (MongoDB Atlas)
│   │       └── [other domain classes]
│   ├── pom.xml                          (Maven configuration)
│   └── Dockerfile                       (Docker build)
├── frontend/
│   └── public/
│       ├── index.html
│       ├── styles.css
│       └── script.js
├── render.yaml                          (Render deployment config)
├── build.sh / build.bat                 (Build scripts)
└── start.sh / start.bat                 (Start scripts)
```

---

## 🏗️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Backend | Java | 17 |
| Build Tool | Maven | 3.9.6 |
| Database | MongoDB Atlas | Latest |
| Frontend | HTML/CSS/JS | - |
| Container | Docker | - |
| Deployment | Render | - |

---

## 🚀 Deployment Methods

### Method 1: Render (Recommended - Production)

**Automatic deployment via GitHub:**

1. Go to https://render.com
2. Sign in with GitHub
3. Click "New +" → "Web Service"
4. Select your repository
5. Render automatically reads `render.yaml` and deploys

**What happens:**
- Reads `render.yaml` configuration
- Builds Docker image using `backend/Dockerfile`
- Runs Java application in container
- Exposes on `https://whatsapp-backend-xxx.onrender.com`

**Environment:**
- Backend: Docker runtime (free tier)
- Frontend: Static site (free tier)
- Automatic HTTPS, domain, and monitoring

---

### Method 2: Local Development

**Prerequisites:**
- Java 17+ installed
- Maven 3.9.6+ installed
- MongoDB Atlas account with connection string

**Windows:**
```bash
.\build.bat          # Build the project
.\start.bat          # Start the server
```

**Mac/Linux:**
```bash
./build.sh           # Build the project
./start.sh           # Start the server
```

**Server will run on:**
- REST API: http://localhost:8080
- WebSocket: ws://localhost:8081

---

## 🔧 Configuration

### Environment Variables

Add these in Render dashboard:

| Variable | Value | Required |
|----------|-------|----------|
| `PORT` | 8080 | No (default) |
| `WS_PORT` | 8081 | No (default) |
| `MONGODB_CONNECTION_STRING` | From MongoDB Atlas | No (hardcoded) |
| `MONGODB_DATABASE_NAME` | whatsapp_db | No (default) |
| `JAVA_OPTS` | -Xmx512m | No (default) |

### MongoDB Atlas Connection

Current connection string (in `DatabaseConnection.java`):
```
mongodb+srv://malarharish007_db_user:niFy0jtVgiiRIqde@cluster0.jtf357u.mongodb.net/?appName=Cluster0
```

---

## 🐳 Docker Build

The `Dockerfile` uses multi-stage build:

1. **Build Stage**: Uses `maven:3.9.6-eclipse-temurin-17`
   - Compiles Java code
   - Runs Maven `clean package`
   - Creates JAR file

2. **Runtime Stage**: Uses `eclipse-temurin:17-jre`
   - Copies JAR and dependencies
   - Exposes ports 8080, 8081
   - Runs Java application

---

## 📝 Key Files

| File | Purpose |
|------|---------|
| `render.yaml` | Render deployment configuration |
| `backend/Dockerfile` | Docker build instructions |
| `backend/pom.xml` | Maven dependencies and plugins |
| `build.sh / build.bat` | Local build scripts |
| `start.sh / start.bat` | Local start scripts |
| `backend/src/main/java/com/whatsapp/RestServer.java` | Main API server |
| `backend/src/main/java/com/whatsapp/WebSocketServer.java` | Real-time messaging |
| `backend/src/main/java/com/whatsapp/DatabaseConnection.java` | MongoDB connection |

---

## ✅ Deployment Checklist

- [x] Java 17 configured in `pom.xml`
- [x] Dockerfile created with Maven and Java 17
- [x] `render.yaml` configured for Docker
- [x] MongoDB Atlas connection string set
- [x] Ports configured (8080, 8081)
- [x] Local build tested and working
- [x] All changes committed to GitHub

---

## 🔍 Troubleshooting

### Build Fails on Render
- Check Docker build logs in Render dashboard
- Verify `backend/Dockerfile` exists
- Ensure `render.yaml` has `dockerfilePath: ./backend/Dockerfile`

### MongoDB Connection Fails
- Check MongoDB Atlas connection string
- Verify IP whitelist includes Render server IP
- Test connection locally first

### Ports Already in Use (Local)
- Kill process: `lsof -ti:8080 | xargs kill -9` (Mac/Linux)
- Change ports in `RestServer.java`

### JAR Not Found
- Run `build.sh` or `build.bat` first
- Check `backend/target/whatsapp-lld-1.0.0.jar` exists

---

## 📚 Resources

- [Render Documentation](https://render.com/docs)
- [Docker Documentation](https://docs.docker.com)
- [Maven Documentation](https://maven.apache.org)
- [Java 17 Documentation](https://docs.oracle.com/en/java/javase/17/)
- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com)

---

## 🎯 Next Steps

1. **Deploy to Render:**
   - Push all changes to GitHub
   - Create Web Service on Render
   - Monitor deployment logs

2. **Test Application:**
   - Access REST API at `https://whatsapp-backend-xxx.onrender.com`
   - Test WebSocket connection
   - Verify MongoDB integration

3. **Deploy Frontend:**
   - Create Static Site on Render
   - Point to `frontend/public` folder
   - Configure backend URL in environment

---

**Last Updated:** December 23, 2025
