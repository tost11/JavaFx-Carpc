#!/bin/bash
rm boost_1_68_0.tar.gz
wget https://dl.bintray.com/boostorg/release/1.68.0/source/boost_1_68_0.tar.gz
tar xf boost_1_68_0.tar.gz
rm boost_1_68_0.tar.gz
cd boost_1_68_0
./bootstrap.sh
./bjam install -j$CORES
cd ..
