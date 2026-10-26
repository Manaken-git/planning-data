@echo off
chcp 65001 >nul
title Plannif Lycee - Serveur Local

echo ================================================================
echo           PLANNIF LYCEE - MODE AUTONOME / NOMADE
echo ================================================================
echo.

set DIR=%~dp0
cd /d "%DIR%"

set JAVA_CMD=java
if exist "%DIR%jre\bin\java.exe" (
    set JAVA_CMD="%DIR%jre\bin\java.exe"
    echo [OK] Utilisation de la machine Java embarquee (cle USB).
) else (
    where java >nul 2>&1
    if errorlevel 1 (
        echo [ERREUR] Java n'a pas ete trouve sur ce poste !
        echo Veuillez installer Java (JDK 21+) ou copier un dossier 'jre' dans ce repertoire.
        echo.
        pause
        exit /b 1
    )
    echo [OK] Utilisation du Java systeme.
)

if not exist "%DIR%data" (
    mkdir "%DIR%data"
)

echo.
echo [1/2] Demarrage du moteur de planning et de l'interface web...
start "Plannif Lycee Backend" /min %JAVA_CMD% -Xmx2g -jar "%DIR%plannif-app.jar"

echo [2/2] En attente du demarrage du serveur...
timeout /t 5 /nobreak >nul

echo.
echo Ouverture de l'interface dans votre navigateur : http://localhost:8080/
start http://localhost:8080/

echo.
echo ================================================================
echo  L'application est active et accessible sur http://localhost:8080/
echo  Pour arreter le serveur, executez Arreter-Planning.bat ou fermez la tache.
echo ================================================================
echo.
pause
