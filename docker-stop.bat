@echo off
echo Stopping container...
docker stop inventory-app
docker rm inventory-app
echo Container stopped and removed.
pause