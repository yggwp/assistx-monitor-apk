@echo off
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%CLASSPATH%" (
    echo Downloading gradle wrapper jar...
    cd /d "%APP_HOME%"
    curl -sL -o gradle\wrapper\gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar 2>nul
)
java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
