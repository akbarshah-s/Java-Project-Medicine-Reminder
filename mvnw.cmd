@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for Windows
@REM ----------------------------------------------------------------------------

@IF "%OPTION_REQUEST_FORMAT%"=="SYSTEM" (
  @SET "JVM_CONFIG_DIR=\.mvn"
)

@set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use LOCAL setvars.
@setlocal

@REM Re-evaluate MAVEN_CMD_LINE_ARGS to make sure strings with quotes are correctly passed
@set MAVEN_CMD_LINE_ARGS=%*

@REM Execute Maven
@set MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
@if not exist %MAVEN_JAVA_EXE% set MAVEN_JAVA_EXE=java

%MAVEN_JAVA_EXE% -version >NUL 2>&1
@if %ERRORLEVEL% NEQ 0 (
  echo Error: JAVA_HOME is not set properly or java command not found.
  goto error
)

%MAVEN_JAVA_EXE% -cp "%~dp0\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
@if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
