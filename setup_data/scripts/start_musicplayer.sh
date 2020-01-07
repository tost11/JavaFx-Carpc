#!/bin/bash

ret=-1
while [ $ret != 0 ] ; do
    cd /home/pi/Java-Carpc/build
    java -jar Player.jar
    ret=$?
    cd ../../
    if [ $ret == 2 ]; then #start navit
    	navit
    fi
    if [ $ret == 3 ]; then #firmware update
	    cd ./Java-Carpc
	    git reset --hard HEAD
	    git pull
	    if [ $? == 0 ]; then
	        ./pack.sh #bzw. pack_pi4.sh
	    fi
        cd ..
    fi
done
