@echo off
echo ================================================================================
echo Calypso Certificate Demo - Build and Run
echo ================================================================================
echo.

echo [1/2] Building project...
call gradle build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Running demo...
echo.
call gradle run

echo.
echo ================================================================================
echo Demo completed
echo ================================================================================
