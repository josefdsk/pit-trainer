@echo off

set JAVA_HOME=..\thirdpty\jre\win32\j2re1.4.2_09
set PATH=%JAVA_HOME%\bin;%JAVA_HOME%\bin\client;%PATH%

start /b %JAVA_HOME%\bin\javaw.exe -Xms200M -Xmx300M -cp ..\bin\PitTrainer.jar com.gpl.pittrainer.PitTrainer
