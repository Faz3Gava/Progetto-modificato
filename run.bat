@echo off
echo =========================================
echo    Starting CityLogic JavaFX Edition    
echo =========================================

where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven 'mvn' was not found in PATH.
    echo Please install Apache Maven and JDK 17+ to run this desktop application.
    pause
    exit /b 1
)

mvn clean javafx:run
pause
