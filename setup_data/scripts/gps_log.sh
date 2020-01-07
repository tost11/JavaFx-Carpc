#/bin/bash

LOGDIR=/home/pi/gps_log/
mkdir $LOGDIR
DATE=`date '+%Y-%m-%d_%H:%M:%S'`
FILE="$LOGDIR$DATE.json"
while [ -f "$FILE" ]; do
  DATE=`date '+%Y-%m-%d_%H:%M:%S'`
  FILE="$LOGDIR$DATE.json"
done
echo "Gpsd Log will be written to: $FILE"
echo "$DATE.json" > $LOGDIR/actual.log
gpspipe -w -o $FILE
