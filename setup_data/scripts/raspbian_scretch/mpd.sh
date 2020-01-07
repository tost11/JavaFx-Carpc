#!/bin/bash

# debian jessi
#apt-get install -y libpcre3-dev libmad0-dev libmpg123-dev libid3tag0-dev libflac-dev libvorbis-dev libopus-dev libadplug-dev libaudiofile-dev libsndfile1-dev libfaad-dev libfluidsynth-dev libgme-dev libmikmod2-dev libmodplug-dev libmpcdec-dev libwavpack-dev libwildmidi-dev libsidplay2-dev libsidutils-dev libresid-builder-dev libsamplerate0-dev libsoxr-dev libbz2-dev libmms-dev libzzip-dev libcurl4-gnutls-dev libyajl-dev libexpat-dev libasound2-dev libao-dev libjack-jackd2-dev libpulse-dev libshout3-dev libsndio-dev libnfs-dev libsmbclient-dev libupnp-dev libavahi-client-dev libsqlite3-dev libsystemd-dev libwrap0-dev libicu-dev libgcrypt-dev libchromaprint-dev libghc-openal-dev libtwolame-dev libshine-dev libmpdclient-dev libsidplayfp-dev

# debian skretch
apt-get install -y libpcre3-dev libmad0-dev libmpg123-dev libid3tag0-dev libflac-dev libvorbis-dev libopus-dev libadplug-dev libaudiofile-dev libsndfile1-dev libfaad-dev libfluidsynth-dev libgme-dev libmikmod-dev libmodplug-dev libmpcdec-dev libwavpack-dev libwildmidi-dev libsidplay2-dev libsidutils-dev libresid-builder-dev libsamplerate0-dev libsoxr-dev libbz2-dev libmms-dev libzzip-dev libcurl4-gnutls-dev libyajl-dev libexpat1-dev libasound2-dev libao-dev libjack-jackd2-dev libpulse-dev libshout3-dev libsndio-dev libnfs-dev libsmbclient-dev libupnp-dev libavahi-client-dev libsqlite3-dev libsystemd-dev libwrap0-dev libicu-dev libgcrypt20-dev libchromaprint-dev libopenal-dev libtwolame-dev libshine-dev libmpdclient-dev libsidplayfp-dev libavformat-dev libcdio-paranoia-dev libmp3lame-dev

rm v0.21.x.zip
wget https://github.com/MusicPlayerDaemon/MPD/archive/v0.21.x.zip
unzip -o v0.21.x.zip
rm v0.21.x.zip
cd MPD-0.21.x
mkdir build
cd build

build_dir=build_out

meson ../ $build_dir --buildtype=debugoptimized -Db_ndebug=true
meson configure $build_dir
cd $build_dir
ninja
ninja install
cd ..
mpd --version

cd ..
