#!/bin/sh
# This is the file which should be run daily by a cron job
cd /home/namefinder/planet
rm update-daily.sh
php daily.php
./update-daily.sh
cat *.import.log *.update.log | mail -s "namefinder: update logs" someone@somewhere.com
mv *.import.log /home/namefinder/planet/donelog
mv *.update.log /home/namefinder/planet/donelog
