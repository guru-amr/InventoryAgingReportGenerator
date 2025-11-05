@echo off
echo Building Docker image...
docker build -t inventory-aging-app .

echo Starting container...
docker run -d -p 8080:80 --name inventory-app inventory-aging-app

echo Application running at http://localhost:8080
pause