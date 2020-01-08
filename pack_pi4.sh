#!/bin/bash

#PATH_TO_FX=/home/pi/armv6hf-sdk/lib/
#PATH_TO_FX=/usr/share/openjfx/lib

rm -R build
mkdir build

git submodule update --init --recursive

javac --module-path $PATH_TO_FX --add-modules javafx.controls -d build -sourcepath 'src/main/java/:json-simple/src/main/java/:java-mpd/src/main/java/' src/main/java/de/tostsoft/carpc/PlayerMain.java

echo "-> build new executable jar file"
cd build

echo "Main-Class: de.tostsoft.carpc.PlayerMain" > manifest.txt
echo "Class-Path: ." >> manifest.txt
jar -cvfm Player.jar manifest.txt
