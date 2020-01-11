sudo apt-get update

sudo apt-get -y install navit mpc

git clone https://github.com/tost11/JavaFx-Carpc.git

#copy navit xml to users navit config folder
cp JavaFx-Carpc/setup_data/navit/navit.xml ~/.navit/
cp JavaFxMusicPlayerPi/setup_data/scripts/change_bright.sh ~/.navit/
chmod u+x ~/.navit/change_bright.sh

#copy new icons for layout
sudo cp JavaFx-Carpc/setup_data/navit/icons/* /usr/share/navit/icons/

#download map (this ist west europe)
#on http://maps9.navit-project.org/ you can select preferd map location and size
sudo mkdir /usr/share/navit/maps/
sudo wget -O /usr/share/navit/maps/map.bin http://maps9.navit-project.org/api/map/?bbox=-17.6,34.5,42.9,70.9
