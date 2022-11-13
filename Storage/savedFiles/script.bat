@echo OFF
SETLOCAL ENABLEDELAYEDEXPANSION
SET "destdir=C:\Users\Usuario\OneDrive\Ambiente de Trabalho\Projeto\Storage\savedFiles"
SET "filename1=teste.txt"
SET "outfile=%destdir%\output.txt"
SET /a count=0
FOR /f "delims=" %%a IN (%filename1%) DO (
SET /a count+=1
SET "line[!count!]=%%a"
)
(
FOR /L %%a IN (%count%,-1,1) DO ECHO(!line[%%a]!
)>"%outfile%"

GOTO :EOF