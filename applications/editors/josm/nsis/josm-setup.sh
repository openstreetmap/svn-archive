#!/bin/sh

## settings ##

VERSION=latest

PROGRAM_FILES="/cygdrive/c/Program Files"

LAUNCH4J_XML="C:\Dokumente und Einstellungen\ulfl\Eigene Dateien\svn.openstreetmap.org\applications\editors\josm\nsis\launch4j.xml"


### download required files ###
mkdir -p downloads
cd downloads

# get latest josm version (and license)
wget -N http://josm.openstreetmap.de/download/josm-latest.jar
wget -N http://josm.openstreetmap.de/browser/trunk/LICENSE?format=raw
cp LICENSE?format=raw LICENSE

# get latest plugin (and supporting files) versions
#cp ../../plugins/annotation-tester/annotation-tester.jar .
#wget -N http://josm.openstreetmap.de/download/plugins/tagging-preset-tester.jar
cp ../../plugins/mappaint/mappaint.jar .
# wget -N http://svn.openstreetmap.org/applications/editors/josm/plugins/mappaint/mappaint.jar
cp ../../plugins/namefinder/namefinder.jar .
# wget --N http://svn.openstreetmap.org/applications/editors/josm/plugins/namefinder/namefinder.jar
#cp ../../plugins/osmarender/osmarender.jar .
#wget -N http://josm.openstreetmap.de/download/plugins/osmarender.jar
cp ../../plugins/validator/validator.jar .
#wget -N http://personales.ya.com/osmfrsantos/validator/latest/validator.jar
#cp ../../plugins/wmsplugin/wmsplugin.jar .
wget -N http://chippy2005.googlepages.com/wmsplugin.jar

cd ..

### convert jar to exe ###
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
"$PROGRAM_FILES/Launch4j/launch4jc.exe" "$LAUNCH4J_XML"

### create the installer exe ###
# NSIS - http://nsis.sourceforge.net/Main_Page
"$PROGRAM_FILES/nsis/makensis.exe" /DVERSION=$VERSION josm.nsi
