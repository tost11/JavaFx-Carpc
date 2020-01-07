#!/bin/bash
if ! [ -z "$1" ]; then
	echo $1 | sudo tee /sys/class/backlight/rpi_backlight/brightness > /dev/null
fi
