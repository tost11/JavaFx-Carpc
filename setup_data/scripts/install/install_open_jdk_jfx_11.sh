sudo apt-get update

sudo apt-get install openjdk-11-jdk openjfx

git clone https://github.com/tost11/JavaFx-Carpc.git
cd JavaFx-Carpc/

#make build skript executable
chmod u+x pack_pi4.sh

#export your javafx installation path and copy also to bashr.rc or profile.rc for percistance
PATH_TO_FX=/usr/share/openjfx/lib

./pack_pi4.sh

#exort display 0 as default
export DISPLAY=:0

#check if working
java -jar --module-path $PATH_TO_FX --add-modules javafx.controls build/Player.jar
