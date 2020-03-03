# JavaFx-Carpc
Carpc wirtten in JavaFx, 

![Car Image](/images/car_img_1.jpg)

## Language
Java (open-jdk 8, openjfx)

## Features
- Plays Musik via [Mpd](https://www.musicpd.org)
- Shows Navigation via [Navit](https://www.navit-project.org)
- Plays DAB+ Radio via [Welle.io](https://www.welle.io)
- Uploads GPS Tracks via http(s)
- Updates Musik via [owncloudcmd](https://doc.owncloud.org/desktop/1.8/owncloudcmd.1.html)

## Dependencies
- java-mpd
- json-simple

## Hardware
- Raspberrypi 3 or 4, (maby 2 never tested)
- Raspberrypi 7-Inch Touch Screen Display
- StromPi for 12V power Supply (or whaterver you whant to use)
- RTL2832 USB dongle (or whatever welle.io and your county supports)
- GPS USB dongle (or whatever gpsd supports)

## How to use
TODO

## Setup

### JavaFx-Carpc
Clone and repository and build project
replace or define $PATH_TO_FX with your installation path of javafx
```bash
git clone https://github.com/tost11/JavaFx-Carpc.git
cd JavaFx-Carpc/
git submodule update --init --recursive
mkdir build
javac --module-path $PATH_TO_FX --add-modules javafx.controls -d build -sourcepath 'src/main/java/:json-simple/src/main/java/:java-mpd/src/main/java/' src/main/java/de/tostsoft/carpc/PlayerMain.java
cd build
echo "Main-Class: de.tostsoft.carpc.PlayerMain" > manifest.txt
echo "Class-Path: ." >> manifest.txt
jar -cvfm Player.jar manifest.txt
```
After that switch int to build folder and Start the Application.
Whenn connected via ssh you need to export display via 'export DISPLAY=:0'

### MPD
MPD is a musik Player that can be controllerd by a text interface over Network. So it is possible to control the musik played on your Car-Radio by Handy, PC, or whatever device you are using.
The recomended MPD version is 0.21.*. On previous versions download of album covers via text interface will not work and it will not be shown in the Player.

#### Setup on Raspian Buster
Just install mpd from you packet manager an you're fine

#### Setup on Raspian Stretch
First of all you don't want to do that !!!
Are you still here ?
Mhm ok...
Do you realy need the album covers? if not install last version from packet manager and be happy.
If not you have to compile mpd yourself and that means you need an newer version of: gcc, cmake, boost, pyhton, meson, ninja.
For that have a look at the Scripts in the [setup_data/scripts/raspian_stretch] folder. That will keep your pi busy for a howl day. For faster compiling you cann disable pyhton tests. Also you have to increase the virtual RAM of the rpy for compiling gcc. I changed it one 1GB and it worked fine.

#### Configuration
For starting mpd with the pi user you have to crate an .mpd folder in /home/pi and copy the default config file from /etc/mpd.conf. After that you change or may not change these Paramters.

- music_directory         "home/pi/owncloud"
- playlist_directory      "/home/pi/owncloud/Playlists-Musikbot"
- user                    "pi"
- bind_to_address         "0.0.0.0"
- db_file                 "/home/pi/mpd/tag_cache"
- log_file                "/home/pi/.mpd/mpd.log"
- pid_file                "/home/pi/.mpd/pid"
- state_file              "/home/pi/.mpd/state"

Also i prefer changing audio output to pulse instead of alsa

### Feature Owncloud
With this feature you cann download music data from a your own server. There fore you have to define the url and the destination folder in the config file as discribed beneth. Also you have to install the [owncloud-cmd](https://doc.owncloud.com/desktop/advanced_usage/command_line_client.html) and define user and password in a .netrc described [here](https://man.cx/netrc(4)). Also you have to define an empty exlude list file.
```bash
sudo apt-get install -y owncloud-client-cmd
sudo mkdir /etc/owncloud-client
sudo touch /etc/owncloud-client/sync-exclude.lst
```

### Feature Gps-Logging/Upload

### Feature Navit
For navigation is the Software [Navit](https://www.navit-project.org) used.
You cann install it via package manager or build it yourself.
On my rpy3, I needet to to build it manually for Raspian Stretch for some reason (check scritps folder for that).
Navit loads all important information from navit.xml. You can check out mine [here](/setup_data/navit/nativ.xml).
Most of it comes from [this side](http://ozzmaker.com/navigating-navit-raspberry-pi)
Its also a very good tutorial how to get navit running.

### Feature DAB+ via Welli-IO
For using the DAB+ Feature you have to compile welle-io yourself and add start script to boot process. Also you have to enable feature via config paramters.
```bash
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
```
Depending on your country you have to replace the -c paramter '5C' with the one for your location

## Configuration
There is a config file for dis/enableing Features and set up needet Folders and Files

### Comandline Paramters
- update -> just updates what is possible (gps,owncloud,mpd) witout showing gui
- debug -> sets log level to debug
- config=file -> changes used config file

### Config File Paramters
- ip (string) -> Default mpd IP (hostname or ip)
- ip_1 (string) -> Frist possible mpd ip for dropdown in settings
- ip_2 (string)-> Second possible mpd ip for dropdown in settings
- ip_* (string)-> You get it right ? Change number for next ip, max is 100
- width (int) -> width of window (not optimized for other resolution)
- height (int) -> height of window (not iptimized for other resolution)
- lighting_file (string) -> file on rpy where brightniss is saved
- lighting_min_value (int) -> minimal possible selectable brightness
- lighting_step_value (int) -> value of increasing brightness on one klick
- wlan_file (string)-> file on rpy where wlan status is stored
- wlan_interface (string) -> used wlan interface
- enable_owncloud (boolean)-> enables owncloud feature
- owncloud_host (string) -> url to owncloud bzw nextcloud server
- owncloud_dir (string) -> folder where data will sync
- enable_gps (string) -> enables gps upload feature
- gps_dir (string) -> folder where pgs data is loged
- gps_upload_url (string) -> url where gps data will be uploaded
- gps_upload_keystore (string) -> keystore with with private key
- gps_upload_keystore_pw (string) -> first password for keystore
- gps_upload_keystore_pw_2 (string) -> second password for keystore
- enable_radio (boolean) -> enables radio feature

### Images

![Image 0](/images/image_0.PNG)

![Image 0](/images/image_1.PNG)

![Image 0](/images/image_2.PNG)

![Image 0](/images/image_3.PNG)
