#!/bin/bash

rm -R build
mkdir build

git submodule update --init --recursive

javac -d build -sourcepath 'src/main/java/:json-simple/src/main/java/:java-mpd/src/main/java/'  src/main/java/de/tostsoft/carpc/PlayerMain.java

echo "-> build new executable jar file"
cd build

echo "Main-Class: de.tostsoft.carpc.PlayerMain" > manifest.txt
echo "Class-Path: ." >> manifest.txt
echo "" >> manifest.txt
jar -cvfm Player.jar manifest.txt
