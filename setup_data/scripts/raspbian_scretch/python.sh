#!/bin/bash

apt-get install libffi-dev libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev

rm Python-3.7.1.tar.xz
wget https://www.python.org/ftp/python/3.7.1/Python-3.7.1.tar.xz
tar xf Python-3.7.1.tar.xz
rm Python-3.7.1.tar.xz
cd Python-3.7.1
./configure --enable-optimizations
make -j$CORES
make install

curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
#must be run as root
python3 get-pip.py
python3 -m pip install --upgrade pip setuptools

cd ..

