@echo off
echo 🌐 Opening Smart Inventory Aging Analytics...
echo.

REM Check if server is running
curl -s http://localhost:8080 >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️  Server not detected on localhost:8080
    echo 💡 Please run start-server.bat first
    echo.
    pause
    exit /b 1
)

echo ✅ Server detected! Opening application...
start "" "Frontend\index.html"

echo.
echo 🎯 Application opened in your default browser
echo 📁 You can also manually open: Frontend\index.html
echo.
pause