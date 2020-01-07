#!/bin/bash
if ! [ -z "$1" ]; then
  let v=$(cat /sys/class/backlight/rpi_backlight/brightness)
  let v=$v+$1

  if (( $v > 255)); then
    let v=255
  fi

  if (( $v < 10)); then
    let v=10
  fi
  echo $v | sudo tee /sys/class/backlight/rpi_backlight/brightness > /dev/null
fi
