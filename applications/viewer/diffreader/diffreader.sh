#!/bin/sh
#bash script to be run by cron to ensure that diffreader.rb process stays up
#Check if diffreader appears already be running. If so we exit
PID=`ps -eo 'tty pid args' | grep 'diffreader.rb' | grep -v grep | tr -s ' ' | cut -f2 -d ' '`
if [ -n "$PID" ]
then
   echo "'diffreader.rb' Process is already Running with PIS=$PID ...exiting. `date`"
else
   ruby diffreader.rb
fi
