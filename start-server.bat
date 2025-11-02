@echo off
echo 🚀 Starting Smart Inventory Aging Analytics Server...
echo.

cd Backend

echo 📦 Compiling Java application...
mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed! Please check for errors.
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo 🌐 Starting server on http://localhost:8080...
echo.
echo 💡 Tips:
echo    - Open Frontend/index.html in your browser
echo    - Use sample-inventory-data.csv for testing
echo    - Press Ctrl+C to stop the server
echo.

mvn exec:java -Dexec.mainClass="InventoryAgingReportGenerator"

pause