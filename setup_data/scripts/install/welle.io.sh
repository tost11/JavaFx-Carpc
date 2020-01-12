sudo apt-get install -y cmake libmp3lame-dev libmpg123-dev libfftw3-dev libfaad-dev librtlsdr-dev

git clone https://github.com/AlbrechtL/welle.io.git
cd welle.io/
mkdir build
cd build

cmake .. -DRTLSDR=1 -DBUILD_WELLE_IO=OFF -DBUILD_WELLE_CLI=ON
make -j4

sudo make install

#copy 'welle-cli -c 5C -C 1 -w 7979 &' to rc.local

#enable in carpc config.properties 'enable_radio=true'
