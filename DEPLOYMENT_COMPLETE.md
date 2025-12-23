# WhatsApp Chat Application - Complete Deployment Guide

## Deployment Options

Choose one of these deployment strategies:

### **Option 1: Backend on Render + Frontend on Vercel** (Recommended)
- ✅ Best performance for frontend (Vercel CDN)
- ✅ Free tier available on both platforms
- ✅ Easy deployment and updates

### **Option 2: Both on Render**
- ✅ Everything in one place
- ✅ Simpler management
- ✅ Single platform to monitor

---

## Option 1: Backend on Render + Frontend on Vercel

### Step 1: Deploy Backend to Render

1. **Push code to GitHub:**
   ```bash
   git init
   git add .
   git commit -m "Prepare for deployment"
   git remote add origin YOUR_GITHUB_REPO_URL
   git push -u origin main
   ```

2. **Deploy Backend on Render:**
   - Go to https://dashboard.render.com
   - Click **"New +"** → **"Web Service"**
   - Connect your GitHub repository
   - Configure:
     - **Name**: `whatsapp-backend`
     - **Root Directory**: `./`
     - **Runtime**: Java
     - **Build Command**: `./render-build.sh`
     - **Start Command**: `./render-start.sh`
     - **Instance Type**: Free

3. **Set Environment Variables** (in Render dashboard):
   ```
   MONGODB_CONNECTION_STRING=your_mongodb_atlas_connection_string
   MONGODB_DATABASE_NAME=whatsapp_db
   PORT=8080
   WS_PORT=8081
   JAVA_OPTS=-Xmx512m
   ```

4. **Copy your backend URL** (e.g., `https://whatsapp-backend.onrender.com`)

### Step 2: Deploy Frontend to Vercel

1. **Install Vercel CLI** (optional):
   ```bash
   npm install -g vercel
   ```

2. **Deploy to Vercel:**

   **Method A: Using Vercel Dashboard** (Easiest)
   - Go to https://vercel.com
   - Click **"Add New"** → **"Project"**
   - Import your GitHub repository
   - Configure:
     - **Framework Preset**: Other
     - **Root Directory**: `frontend`
     - **Build Command**: (leave empty)
     - **Output Directory**: `public`
   - Add Environment Variable:
     - **Key**: `BACKEND_URL`
     - **Value**: `https://YOUR-BACKEND-URL.onrender.com/api`
   - Click **"Deploy"**

   **Method B: Using Vercel CLI**
   ```bash
   cd frontend
   vercel --prod
   ```

3. **Update Backend URL in env.js:**
   - After deployment, update `frontend/public/env.js`:
   ```javascript
   window.ENV_BACKEND_URL = 'https://YOUR-BACKEND-URL.onrender.com/api';
   ```
   - Or set it in Vercel Dashboard → Your Project → Settings → Environment Variables

4. **Update CORS in Backend** (if needed):
   - Add your Vercel frontend URL to allowed origins in backend code

---

## Option 2: Both on Render

### Deploy Using render.yaml (Recommended)

1. **Push code to GitHub:**
   ```bash
   git init
   git add .
   git commit -m "Prepare for deployment"
   git remote add origin YOUR_GITHUB_REPO_URL
   git push -u origin main
   ```

2. **Deploy on Render:**
   - Go to https://dashboard.render.com
   - Click **"New +"** → **"Blueprint"**
   - Connect your GitHub repository
   - Render will detect `render.yaml` and create both services
   - Click **"Apply"**

3. **Set Environment Variable:**
   - Go to your backend service settings
   - Add environment variable:
     ```
     MONGODB_CONNECTION_STRING=your_mongodb_atlas_connection_string
     ```

4. **Update Frontend Configuration:**
   - After backend deploys, copy the backend URL
   - Update `frontend/public/env.js`:
   ```javascript
   window.ENV_BACKEND_URL = 'https://YOUR-BACKEND-URL.onrender.com/api';
   ```
   - Commit and push:
   ```bash
   git add frontend/public/env.js
   git commit -m "Update backend URL"
   git push
   ```

---

## Configuration Files

### For Vercel Deployment
- ✅ `frontend/vercel.json` - Vercel configuration
- ✅ `frontend/.vercelignore` - Files to ignore
- ✅ `frontend/public/env.js` - Environment-specific config

### For Render Deployment
- ✅ `render.yaml` - Blueprint for both services
- ✅ `render-build.sh` - Backend build script
- ✅ `render-start.sh` - Backend start script
- ✅ `.renderignore` - Files to ignore

---

## Post-Deployment Steps

### 1. Update Backend URL

**After your backend is deployed, update the frontend:**

Edit `frontend/public/env.js`:
```javascript
window.ENV_BACKEND_URL = 'https://whatsapp-backend.onrender.com/api';
```

Replace `whatsapp-backend.onrender.com` with your actual backend URL.

### 2. Test Your Deployment

1. Visit your frontend URL
2. Register a new account
3. Log in
4. Try sending messages
5. Open in another browser/tab to test real-time messaging

### 3. Monitor Services

**Render:**
- Dashboard → Your Service → Logs
- Check service health and metrics

**Vercel:**
- Dashboard → Your Project → Deployments
- View deployment logs and analytics

---

## Environment Variables Reference

### Backend (Render)

| Variable | Value | Required |
|----------|-------|----------|
| `MONGODB_CONNECTION_STRING` | Your MongoDB Atlas connection string | ✅ Yes |
| `MONGODB_DATABASE_NAME` | `whatsapp_db` | ✅ Yes |
| `PORT` | `8080` (auto-set by Render) | No |
| `WS_PORT` | `8081` | No |
| `JAVA_OPTS` | `-Xmx512m` | No |

### Frontend (Vercel)

| Variable | Value | Required |
|----------|-------|----------|
| `BACKEND_URL` | Your Render backend URL | ✅ Yes |

---

## Troubleshooting

### Backend Issues

**Service won't start:**
- Check MongoDB connection string is correct
- Verify all environment variables are set
- Check logs in Render dashboard
- Ensure Java 11 is being used

**Cannot connect to MongoDB:**
- Verify MongoDB Atlas connection string
- Check MongoDB Atlas network access (allow 0.0.0.0/0)
- Ensure database name is correct

### Frontend Issues

**Cannot connect to backend:**
- Verify backend URL in `env.js`
- Check backend is running (visit backend URL)
- Check browser console for errors
- Verify CORS is configured correctly

**Static files not loading:**
- Check Vercel build logs
- Verify `vercel.json` configuration
- Ensure file paths are correct

### WebSocket Issues

**WebSocket connection fails:**
- Verify backend WebSocket port (8081)
- Check WS URL uses `wss://` for production
- Ensure backend allows WebSocket connections
- Check firewall/network settings

---

## Free Tier Limitations

### Render Free Tier:
- ⚠️ Services spin down after 15 minutes of inactivity
- ⚠️ First request may take 30-60 seconds to wake up
- ✅ 750 hours/month free
- ✅ WebSocket support included

### Vercel Free Tier:
- ✅ Unlimited deployments
- ✅ 100 GB bandwidth/month
- ✅ Automatic HTTPS
- ✅ Global CDN

---

## Security Best Practices

1. **Environment Variables:**
   - Never commit sensitive data to GitHub
   - Use environment variables for all credentials
   - MongoDB connection strings should be environment variables

2. **CORS Configuration:**
   - Restrict CORS to your frontend domain only
   - Update backend to whitelist specific origins

3. **HTTPS:**
   - Both Render and Vercel provide automatic HTTPS
   - Always use HTTPS in production

4. **Rate Limiting:**
   - Consider adding rate limiting to API endpoints
   - Protect against abuse and DDoS

---

## Quick Reference Commands

### Git Commands
```bash
# Initial setup
git init
git add .
git commit -m "Initial commit"
git remote add origin YOUR_REPO_URL
git push -u origin main

# Update after changes
git add .
git commit -m "Update configuration"
git push
```

### Vercel CLI Commands
```bash
# Install Vercel CLI
npm install -g vercel

# Deploy to production
cd frontend
vercel --prod

# View logs
vercel logs
```

---

## URLs to Update

After deployment, update these files:

1. **`frontend/public/env.js`**
   ```javascript
   window.ENV_BACKEND_URL = 'https://YOUR-BACKEND.onrender.com/api';
   ```

2. **`frontend/vercel.json`** (if using Vercel)
   ```json
   "env": {
     "BACKEND_URL": "https://YOUR-BACKEND.onrender.com/api"
   }
   ```

---

## Need Help?

- **Render Documentation:** https://render.com/docs
- **Vercel Documentation:** https://vercel.com/docs
- **MongoDB Atlas:** https://www.mongodb.com/docs/atlas/

---

## Deployment Checklist

### Backend (Render)
- [ ] Code pushed to GitHub
- [ ] Web service created on Render
- [ ] Build and start commands configured
- [ ] Environment variables set (MongoDB connection)
- [ ] Service deployed and running
- [ ] Backend URL copied

### Frontend (Vercel or Render)
- [ ] Frontend deployment configured
- [ ] Backend URL updated in `env.js`
- [ ] Environment variables set (if using Vercel)
- [ ] Frontend deployed successfully
- [ ] Can access frontend URL

### Testing
- [ ] Frontend loads correctly
- [ ] Can register new account
- [ ] Can log in
- [ ] Can send messages
- [ ] Real-time messaging works
- [ ] WebSocket connection established

---

**Your deployment is ready!** Choose your preferred option and follow the steps above.
