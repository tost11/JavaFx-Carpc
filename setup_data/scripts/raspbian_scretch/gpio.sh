#!/bin/bash

git clone git://git.drogon.net/wiringPi
cd wiringPi
./build
gpio -v
cd ..
