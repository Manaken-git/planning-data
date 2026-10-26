@echo off
chcp 65001 >nul
title Plannif Lycee - Sauvegarde de la Base

set DIR=%~dp0
cd /d "%DIR%"

if not exist "%DIR%backups" (
    mkdir "%DIR%backups"
)

for /f "tokens=1-3 delims=/" %%a in ("%date%") do set D=%%c-%%b-%%a
for /f "tokens=1-2 delims=:" %%a in ("%time%") do set T=%%a-%%b
set T=%T: =0%
set BACKUP_DIR=%DIR%backups\backup_%D%_%T%

echo Sauvegarde des donnees dans %BACKUP_DIR%...
mkdir "%BACKUP_DIR%"
xcopy "%DIR%data\*" "%BACKUP_DIR%\" /Y /E /I >nul

echo [OK] Sauvegarde terminee avec succes !
echo Vos donnees sont securisees dans le dossier backups.
timeout /t 3 >nul
