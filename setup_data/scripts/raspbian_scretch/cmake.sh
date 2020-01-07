#!/bin/bash

rm xf cmake-3.13.0.tar.gz
wget https://github.com/Kitware/CMake/releases/download/v3.13.0/cmake-3.13.0.tar.gz
tar xf cmake-3.13.0.tar.gz
rm xf cmake-3.13.0.tar.gz
cd cmake-3.13.0
mkdir build
cd build
#cmake -DCMAKE_INSTALL_PREFIX=/usr/local/cmake-3.13.0 ../
cmake -DCMAKE_INSTALL_PREFIX=/usr/local ../
make -j$CORES
#make install

#export PATH==$inst_path/bin:$PATH
#echo 'export PATH=$inst_path/bin:$PATH' >> /home/pi/.bashrc
#echo 'export PATH=$inst_path/bin:$PATH' >> /root/.bashrc

cd ..
cd ..
cmake --version
