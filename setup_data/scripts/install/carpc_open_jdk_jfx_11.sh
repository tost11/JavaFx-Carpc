sudo apt-get update

sudo apt-get install -y openjdk-11-jdk openjfx

git clone https://github.com/tost11/JavaFx-Carpc.git
cd JavaFx-Carpc/

#make build skript executable
chmod u+x pack_pi4.sh

mkdir ~/.carpc
mkdir ~/.carpc/covers

#export your javafx installation path and copy also to bashr.rc or profile.rc for percistance
export PATH_TO_FX=/usr/share/openjfx/lib

./pack_pi4.sh

#exort display 0 as default
export DISPLAY=:0

#check if working
java -jar --module-path $PATH_TO_FX --add-modules javafx.controls build/Player.jar
#when pressing buttons is not possible you have to add -Djdk.gtk.version=2 to java arguments

#edit /etc/X11/xinit/xinitrc
#add your starts script befor booting into desktop manager (. /etc/X11/Xsession)

#add user pi to tty group
sudo usermod -a -G tty pi
#maby you have to add pi user to video and autio group
# sudo adduser pi video
# sudo adduser pi audio
#or run
sudo chmod ug+s /usr/lib/xorg/Xorg

#add to 'sudo vi /etc/udev/rules.d/99-com.rules' these lines
#	SUBSYSTEM=="input*", PROGRAM="/bin/sh -c '\
#	        chown -R root:input /sys/class/input/*/ && chmod -R 770 /sys/class/input/*/;\
#	'"

#maby you have to add user pi user to autio and video group

#finaly add 'su - pi startx &' to /etc/rc.local