# JavaFx-Carpc
Carpc wirtten in JavaFx, 

![GitHub Logo](/images/car_img_1.jpg)

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

### Feature Owncloud

### Feature Gps-Upload

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
