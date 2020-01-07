copy navit.xml to /etc/navit or if self compiled to  /usr/local/lib/share/navit
or to ~/.navit whenn not root

copy icon folder to /etc/... ore /usr/... like above


add path to run.sh in /etc/X11/xinit/xinitrc
and comment '. /etc/X11/Xsession'

#add user pi to tty group
sudo usermod -a -G tty pi
#or run
sudo chmod ug+s /usr/lib/xorg/Xorg

#add to 'sudo vi /etc/udev/rules.d/99-com.rules' these lines
SUBSYSTEM=="input*", PROGRAM="/bin/sh -c '\
        chown -R root:input /sys/class/input/*/ && chmod -R 770 /sys/class/input/*/;\
'"

#set user anybody in /etc/X11/xinit

#download map bsp:
sudo wget -O map.bin http://maps9.navit-project.org/api/map/?bbox=3.4,44.5,18.6,55.1

#install gpsd
apt-get install gpsd
