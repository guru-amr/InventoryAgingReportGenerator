#!/bin/bash

# Start Java backend
echo "Starting Java backend..."
mvn exec:java -Dexec.mainClass="InventoryAgingReportGenerator" &

# Wait for backend to start
sleep 5

# Start nginx
echo "Starting nginx..."
nginx -g "daemon off;" &

# Keep container running
wait