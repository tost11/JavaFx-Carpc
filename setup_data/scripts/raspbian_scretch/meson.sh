rm master.zip
wget https://github.com/mesonbuild/meson/archive/master.zip
unzip -o master.zip
rm master.zip
cd meson-master
python3 setup.py install
cd ..
