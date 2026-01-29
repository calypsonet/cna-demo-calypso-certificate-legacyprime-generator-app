#!/bin/bash

echo "================================================================================"
echo "Calypso Certificate Demo - Build and Run"
echo "================================================================================"
echo ""

echo "[1/2] Building project..."
./gradlew build
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "[2/2] Running demo..."
echo ""
./gradlew run

echo ""
echo "================================================================================"
echo "Demo completed"
echo "================================================================================"
