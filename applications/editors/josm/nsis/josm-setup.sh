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
cp ../../plugins/dist/mappaint.jar .
# wget -N http://svn.openstreetmap.org/applications/editors/josm/plugins/mappaint/mappaint.jar
cp ../../plugins/dist/namefinder.jar .
# wget --N http://svn.openstreetmap.org/applications/editors/josm/plugins/namefinder/namefinder.jar
#cp ../../plugins/osmarender/osmarender.jar .
#wget -N http://josm.openstreetmap.de/download/plugins/osmarender.jar
cp ../../plugins/dist/validator.jar .
#wget -N http://personales.ya.com/osmfrsantos/validator/latest/validator.jar
cp ../../plugins/dist/wmsplugin.jar .
#wget -N http://chippy2005.googlepages.com/wmsplugin.jar

cd ..

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
