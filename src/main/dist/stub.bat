set JCMD="javaw"

if exist %~dp0\java\bin\javaw.exe (

set JAVA_HOME=%~dp0\java
set JCMD=%~dp0\java\bin\javaw.exe

)

:start "%JCMD%" --add-opens=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED %JAVA_OPTS% -jar "%~dpn0.jar"

start %JCMD% %JAVA_OPTS% -jar "%~dpn0.jar"

exit /B %errorlevel%
