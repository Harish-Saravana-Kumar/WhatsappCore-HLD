# WhatsApp Chat Application - Render Deployment Guide

This guide will help you deploy your WhatsApp chat application to Render.

## Prerequisites

1. **GitHub Account**: Your code must be in a GitHub repository
2. **Render Account**: Sign up at https://render.com
3. **MongoDB Atlas**: Your MongoDB connection string (already configured in application.properties)

## Deployment Steps

### 1. Push Your Code to GitHub

```bash
# Initialize git repository if not already done
git init
git add .
git commit -m "Prepare for Render deployment"

# Create a new repository on GitHub and push
git remote add origin YOUR_GITHUB_REPOSITORY_URL
git branch -M main
git push -u origin main
```

### 2. Deploy to Render

#### Option A: Using render.yaml (Recommended - Deploy Both Services)

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Blueprint"**
3. Connect your GitHub repository
4. Render will automatically detect the `render.yaml` file
5. Click **"Apply"** to create both services

#### Option B: Manual Deployment (Deploy Services Separately)

**Backend Service:**
1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Configure:
   - **Name**: `whatsapp-backend`
   - **Runtime**: Java
   - **Build Command**: `./render-build.sh`
   - **Start Command**: `./render-start.sh`
   - **Instance Type**: Free

**Frontend Service:**
1. Click **"New +"** → **"Static Site"**
2. Connect your GitHub repository
3. Configure:
   - **Name**: `whatsapp-frontend`
   - **Build Command**: `echo "No build needed"`
   - **Publish Directory**: `frontend/public`

### 3. Configure Environment Variables

In your backend service settings, add these environment variables:

**Required:**
- `MONGODB_CONNECTION_STRING`: Your MongoDB Atlas connection string
- `MONGODB_DATABASE_NAME`: `whatsapp_db`

**Optional (already set in render.yaml):**
- `PORT`: `8080` (Render sets this automatically)
- `WS_PORT`: `8081`
- `JAVA_OPTS`: `-Xmx512m`

### 4. Update Frontend URLs

After your backend is deployed:

1. Copy your backend URL from Render (e.g., `https://whatsapp-backend.onrender.com`)
2. Update `frontend/public/config.js`:

```javascript
const CONFIG = {
    API_URL: window.location.hostname === 'localhost' 
        ? 'http://localhost:8080/api'
        : 'https://YOUR-BACKEND-URL.onrender.com/api',
    
    WS_URL: window.location.hostname === 'localhost'
        ? 'ws://localhost:8081'
        : 'wss://YOUR-BACKEND-URL.onrender.com:8081'
};
```

3. Commit and push the changes:
```bash
git add frontend/public/config.js
git commit -m "Update API URLs for production"
git push
```

### 5. Important Notes

#### Free Tier Limitations
- Free services spin down after 15 minutes of inactivity
- First request after inactivity may take 30-60 seconds
- Consider upgrading to a paid plan for production use

#### WebSocket Connections
- WebSocket connections work on Render's free tier
- The backend uses port 8081 for WebSocket connections
- CORS is configured to allow cross-origin requests

#### Database
- MongoDB Atlas is recommended (already configured)
- Make sure your MongoDB Atlas cluster allows connections from anywhere (0.0.0.0/0) or add Render's IP addresses
- Connection string should be stored as an environment variable for security

#### CORS Configuration
- The backend is configured to accept requests from any origin
- In production, you should restrict this to your frontend domain

### 6. Testing Your Deployment

1. Visit your frontend URL (e.g., `https://whatsapp-frontend.onrender.com`)
2. Create a test account
3. Try logging in and sending messages
4. Open in multiple browsers to test real-time messaging

### 7. Monitoring and Logs

- View logs in Render dashboard → Your Service → Logs
- Monitor service health in the dashboard
- Set up alerts for service failures (available in paid plans)

### 8. Troubleshooting

**Backend won't start:**
- Check MongoDB connection string is correct
- Verify environment variables are set
- Check logs for Java compilation errors

**Frontend can't connect to backend:**
- Verify backend URL in `config.js`
- Check CORS settings
- Ensure backend service is running

**WebSocket connection fails:**
- Verify WS_PORT is set correctly
- Check WebSocket URL uses `wss://` for production
- Ensure port 8081 is accessible

## Files Created for Deployment

- `render.yaml` - Blueprint for deploying both services
- `render-build.sh` - Build script for backend
- `render-start.sh` - Start script for backend
- `.renderignore` - Files to ignore during deployment
- `frontend/public/config.js` - Configuration for API URLs

## Security Recommendations for Production

1. Store MongoDB connection string as environment variable (already done)
2. Restrict CORS to your frontend domain only
3. Add rate limiting to API endpoints
4. Use HTTPS for all connections
5. Implement proper authentication tokens
6. Add input validation and sanitization

## Need Help?

- Render Documentation: https://render.com/docs
- MongoDB Atlas: https://www.mongodb.com/docs/atlas/
- Java on Render: https://render.com/docs/deploy-java

---

**Your deployment is ready!** Follow the steps above to deploy your WhatsApp clone to Render.
