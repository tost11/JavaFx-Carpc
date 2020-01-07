#!/bin/bash

#sudo apt-get install libsqlite3-dev qtbase5-dev libqt5webkit5-dev libssl-dev qttools5-dev-tools qt5keychain-dev
git clone --branch 2.3.4 --depth 1 http://github.com/owncloud/client.git oc-client
cd oc-client
git submodule init
git submodule update


mkdir build
cd build
cmake -DCMAKE_BUILD_TYPE="Release" ../
make -j$CORES
#make install
#ldconfig

cd ..
cd ..
