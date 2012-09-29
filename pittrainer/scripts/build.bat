@echo off

pushd .

echo compiling...
cd ..\src
javac com\gpl\pittrainer\*.java

if ERRORLEVEL 1 (
   echo .
   echo BUILD ERRORS
   echo .
   pause
) else (
   echo creating jar...
   jar cf ..\bin\PitTrainer.jar com
)

popd
