#!/bin/sh

## settings ##

VERSION=latest

PROGRAM_FILES="/cygdrive/c/Program Files"

LAUNCH4J_XML="C:\Dokumente und Einstellungen\ulfl\Eigene Dateien\svn.openstreetmap.org\applications\editors\josm\nsis\launch4j.xml"


### convert jar to exe ###
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/

# delete old exe file first
rm josm.exe
"$PROGRAM_FILES/Launch4j/launch4jc.exe" "$LAUNCH4J_XML"
# using a relative path still doesn't work with launch4j 3.0.0-pre2
#"$PROGRAM_FILES/Launch4j/launch4jc.exe" ./launch4j.xml

### create the installer exe ###
# NSIS - http://nsis.sourceforge.net/Main_Page
"$PROGRAM_FILES/nsis/makensis.exe" /DVERSION=$VERSION josm.nsi

# delete the intermediate file, just to avoid confusion
rm josm.exe
