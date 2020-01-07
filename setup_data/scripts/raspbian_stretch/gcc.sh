#!/bin/bash

#!! important increase swap size in: /etc/dphys-swapfile

rm gcc-8.1.0.tar.gz #whenn already esists maybe coruppted
wget https://ftpmirror.gnu.org/gcc/gcc-8.1.0/gcc-8.1.0.tar.gz
tar xf gcc-8.1.0.tar.gz
rm gcc-8.1.0.tar.gz #cleanup
cd gcc-8.1.0
contrib/download_prerequisites
mkdir build
cd build
#../configure -v --enable-languages=c,c++ --prefix=/usr/local/gcc-8.1.0 --program-suffix=-8.1.0 --with-arch=armv6 --with-fpu=vfp --with-float=hard --build=arm-linux-gnueabihf --host=arm-linux-gnueabihf --target=arm-linux-gnueabihf
#../configure -v --enable-languages=c,c++ --prefix=/usr/local/gcc-8.1.0 --with-arch=armv6 --with-fpu=vfp --with-float=hard --build=arm-linux-gnueabihf --host=arm-linux-gnueabihf --target=arm-linux-gnueabihf
../configure -v --enable-languages=c,c++ --prefix=/usr/local --with-arch=armv6 --with-fpu=vfp --with-float=hard --build=arm-linux-gnueabihf --host=arm-linux-gnueabihf --target=arm-linux-gnueabihf
make -j$CORES
#make install-strip

cd ..
cd ..

#update-alternatives --install /usr/bin/gcc gcc /usr/local/gcc-8.1.0/bin/gcc-8.1.0 100
#update-alternatives --install /usr/bin/g++ g++ /usr/local/gcc-8.1.0/bin/g++-8.1.0 100
#update-alternatives --install /usr/bin/cpp cpp /usr/local/gcc-8.1.0/bin/cpp-8.1.0 100

#export PATH=/usr/local/gcc-8.1.0/bin:$PATH
#echo 'export PATH=/usr/local/gcc-8.1.0/bin:$PATH' >> /home/pi/.bashrc
#echo 'export PATH=/usr/local/gcc-8.1.0/bin:$PATH' >> /root/.bashrc

#export LD_LIBRARY_PATH=/usr/local/gcc-8.1.0/lib
#echo 'export LD_LIBRARY_PATH=/usr/local/gcc-8.1.0/lib' >> /home/pi/.bashrc
#echo 'export LD_LIBRARY_PATH=/usr/local/gcc-8.1.0/lib' >> /root/.bashrc

gcc -v
g++ -v
