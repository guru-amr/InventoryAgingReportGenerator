# Deploy to Render

## Steps to Deploy:

### 1. Push to GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/yourusername/inventory-aging-app.git
git push -u origin main
```

### 2. Deploy on Render
1. Go to https://render.com
2. Sign up/Login with GitHub
3. Click "New" → "Web Service"
4. Connect your GitHub repository
5. Use these settings:
   - **Environment**: Docker
   - **Dockerfile Path**: ./Dockerfile
   - **Plan**: Free

### 3. Access Your App
Your app will be available at: `https://your-app-name.onrender.com`

## Files Ready for Render:
- ✅ Dockerfile
- ✅ render.yaml
- ✅ All project files

## Next Steps:
1. Create GitHub repository
2. Push code to GitHub  
3. Connect to Render
4. Deploy!