# Docker Deployment Guide

## Quick Start Options

### Option 1: Docker Compose
```bash
docker-compose up -d
```

### Option 2: Windows Batch Scripts
```cmd
docker-run.bat
```

### Option 3: Makefile Commands
```bash
make build
make run
```

### Option 4: Manual Commands
```bash
docker build -t inventory-aging-app .
docker run -d -p 8080:80 --name inventory-app inventory-aging-app
```

## Access Application
Open: http://localhost:8080

## Management Commands
```bash
# Stop container
make stop
# or
docker-stop.bat

# View logs
make logs

# Restart
make restart
```