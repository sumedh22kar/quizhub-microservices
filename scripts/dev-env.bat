@echo off

set "JAVA_HOME=D:\Java 21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ============================================
echo        QuizHub Development Environment
echo ============================================
echo JAVA_HOME=%JAVA_HOME%
echo.

java -version
echo.
mvn -version
echo.

cd /d %~dp0..
cmd