@echo off
chcp 65001 >nul
title Plannif Lycee - Arret du Serveur

echo Arret de l'instance Plannif Lycee en cours...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
    echo Processus %%a arrete avec succes.
)
echo Application arretee.
timeout /t 2 >nul
