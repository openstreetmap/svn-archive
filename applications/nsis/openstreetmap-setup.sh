#!/bin/sh

## settings ##

VERSION=0.0.6

PROGRAM_FILES="/cygdrive/c/Program Files"

LAUNCH4J_XML="C:\Dokumente und Einstellungen\ulfl\Eigene Dateien\proj\gps\osm\svn.openstreetmap.org\applications\nsis\launch4j.xml"


### download required files ###
mkdir -p downloads
cd downloads

# get latest josm version (and license)
wget -nc http://josm.eigenheimstrasse.de/download/josm-latest.jar
wget -nc http://josm.eigenheimstrasse.de/browser/LICENSE?format=raw
cp LICENSE?format=raw LICENSE

# get latest plugin (and supporting files) versions
wget -nc http://svn.openstreetmap.org/applications/editors/josm/plugins/mappaint/
# wget -nc http://www.free-map.org.uk/downloads/josm/mappaint.jar
# wget -nc http://www.free-map.org.uk/downloads/josm/elemstyles.xml
wget -nc http://www.eigenheimstrasse.de/josm/plugins/osmarender.jar
wget -nc http://www.eigenheimstrasse.de/josm/plugins/annotation-tester.jar
wget -nc http://chippy2005.googlepages.com/wmsplugin.jar

cd ..

### convert jar to exe ###
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
"$PROGRAM_FILES/Launch4j/launch4jc.exe" "$LAUNCH4J_XML"

### create the installer exe ###
# NSIS - http://nsis.sourceforge.net/Main_Page
"$PROGRAM_FILES/nsis/makensis.exe" /DVERSION=$VERSION openstreetmap.nsi
