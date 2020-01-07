#!/bin/bash

#inst_path=/usr/local/bin/ninja-1.7.2

rm v1.8.2.tar.gz
wget https://github.com/ninja-build/ninja/archive/v1.8.2.tar.gz
tar xf v1.8.2.tar.gz
rm  v1.8.2.tar.gz
cd ninja-1.8.2/
./configure.py --bootstrap
#mkdir $inst_path
#mkdir $inst_path/bin
#cp ninja $inst_path/bin
cp ninja /use/local/bin

#export PATH==$inst_path/bin:$PATH
#echo 'export PATH=$inst_path/bin:$PATH' >> /home/pi/.bashrc
#echo 'export PATH=$inst_path/bin:$PATH' >> /root/.bashrc

cd ..

