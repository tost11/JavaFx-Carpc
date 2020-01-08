sudo apt-get update

sudo apt-get install navit

git clone https://github.com/tost11/JavaFx-Carpc.git

#copy navit xml to users navit config folder
cp JavaFx-Carpc/setup_data/navit/navit.xml ~/.navit/

#copy new icons for layout
sudo cp JavaFx-Carpc/setup_data/navit/icons/* /usr/share/navit/icons/

#download map (this ist west europe)
#on http://maps9.navit-project.org/ you can select preferd map location and size
sudo wget -O /usr/share/navit/maps/map.bin http://maps9.navit-project.org/api/map/?bbox=-17.6,34.5,42.9,70.9
