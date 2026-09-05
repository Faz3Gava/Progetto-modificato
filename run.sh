#!/bin/bash
# Convenient runner script for CityLogic JavaFX
echo "========================================="
echo "   Starting CityLogic JavaFX Edition    "
echo "========================================="

if ! command -v mvn &> /dev/null; then
    echo "Error: 'mvn' (Maven) is not found in PATH."
    echo "Please install Maven and JDK 17+ to run this desktop application."
    exit 1
fi

mvn clean javafx:run
