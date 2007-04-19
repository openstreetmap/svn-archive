#!/bin/sh

### download required files ###
mkdir -p downloads
cd downloads

# get latest josm version (and license)
wget -nc http://josm.eigenheimstrasse.de/download/josm-latest.jar
wget -nc http://josm.eigenheimstrasse.de/browser/LICENSE?format=raw
cp LICENSE?format=raw LICENSE

# get latest plugin (and supporting files) versions
wget -nc http://www.free-map.org.uk/downloads/josm/mappaint.jar
wget -nc http://www.free-map.org.uk/downloads/josm/elemstyles.xml
wget -nc http://www.eigenheimstrasse.de/josm/plugins/osmarender.jar
wget -nc http://chippy2005.googlepages.com/wmsplugin.jar
wget -nc http://www.eigenheimstrasse.de/josm/plugins/annotation-tester.jar

cd ..

### convert jar to exe ###
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
"/cygdrive/c/Program Files/Launch4j/launch4jc.exe" "C:\Dokumente und Einstellungen\ulfl\Eigene Dateien\proj\gps\osm\nsis\launch4j.xml"

### create the installer exe ###
# NSIS - http://nsis.sourceforge.net/Main_Page
"/cygdrive/c/Program Files/nsis/makensis.exe" openstreetmap.nsi
