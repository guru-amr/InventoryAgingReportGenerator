@echo off
echo Building full application Docker image...
docker build -t inventory-aging-full .

echo Starting full application container...
docker run -d -p 80:80 -p 8080:8080 --name inventory-full-app inventory-aging-full

echo Full application running at:
echo Frontend: http://localhost
echo Backend API: http://localhost:8080
pause