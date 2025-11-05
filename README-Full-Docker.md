# Complete Dockerized Inventory Aging Application

## Full Stack Deployment

### Quick Start
```bash
# Build and run complete application
docker-compose up -d

# Or use Windows script
docker-run-full.bat
```

### Access Points
- **Frontend**: http://localhost (port 80)
- **Backend API**: http://localhost:8080

### What's Included
- Java Spark backend server
- HTML/CSS/JS frontend
- Nginx reverse proxy
- File upload handling
- Excel processing

### Manual Commands
```bash
# Build image
docker build -t inventory-aging-full .

# Run container
docker run -d -p 80:80 -p 8080:8080 --name inventory-full-app inventory-aging-full

# View logs
docker logs inventory-full-app

# Stop
docker stop inventory-full-app && docker rm inventory-full-app
```

### Architecture
```
Browser → Nginx (port 80) → Static Files
       ↘ Proxy → Java Backend (port 8080) → Excel Processing
```