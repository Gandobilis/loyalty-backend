@echo off
REM Quick deployment script for development environment
REM This is a convenience wrapper around the PowerShell script

echo Starting Terraform deployment for DEV environment...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0scripts\deploy.ps1" dev %1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Deployment failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Deployment completed successfully!
pause
