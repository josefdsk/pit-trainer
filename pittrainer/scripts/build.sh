#!/usr/bin/sh

prevdir=`pwd`

echo compiling...
cd ../src
javac com/gpl/pittrainer/*.java

if [ $? -ne 0 ]; then
   echo .
   echo BUILD ERRORS
   echo .
   pause
else
   echo creating jar...
   jar cf ../bin/PitTrainer.jar com
fi

cd $prevdir
