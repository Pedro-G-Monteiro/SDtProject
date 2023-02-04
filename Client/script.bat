@echo OFF
SETLOCAL ENABLEDELAYEDEXPANSION
SET "destdir=.\"
SET "filename1=infile.txt"
SET "outfile=.\outfile.txt"
SET /a count=0
FOR /f "delims=" %%a IN (%filename1%) DO (
SET /a count+=1
SET "line[!count!]=%%a"
)
(
FOR /L %%a IN (%count%,-1,1) DO ECHO(!line[%%a]!
)>"%outfile%"

GOTO :EOF