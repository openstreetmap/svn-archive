This java application allows to upload gpx tracks to openstreetmap.org 
without user interaction (batch mode) via the API (version 4.0).

Usage:
java GpxUpload <description> <tags> <files*>
Osm username and password can be defined as system properties 
by -Dusername=<username> and -Dpassword=<password> or if not given, 
josm's preference file is read.
Any messages are printed to stderror, only the filename that was sent 
successfully is printed to stdout, so you may use the output of this 
program in a pipe for other calls.

Examples:
java GpxUpload "taking a ride in Graz, Austria" "graz austria" gpxfile.gpx

java GpxUpload "taking a ride in Graz, Austria" "graz austria" gpxfiles*.gpx | xargs -i mv '{}' /home/cdaller/targetdir

Christof Dallermassl
christof@dallermassl.at