#!/bin/bash

#apt-get install -y libfreetype6-dev  zlib1g-dev libpng-dev libgtk2.0-dev librsvg2-bin protobuf-c-compiler libprotobuf-c-dev libsdl-image1.2-dev libdevil-dev libglc-dev freeglut3-dev libxmu-dev libfribidi-dev freeglut3-dev libxft-dev libglib2.0-dev libfreeimage-dev espeak gettext libqt4-dev libgps-dev libdbus-1-dev libdbus-glib-1-dev

git clone https://github.com/navit-gps/navit.git
cd navit
mkdir build
cd build
cmake ../ -DFREETYPE_INCLUDE_DIRS=/usr/include/freetype2/
make -j$CORES
#make install
cd ..
cd ..

navit -v


#apt-get install -y server-xorg x11-xserver-utils xinit
