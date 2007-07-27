#!/bin/sh

## settings ##

VERSION=0.0.9

PROGRAM_FILES="/cygdrive/c/Program Files"

LAUNCH4J_XML="C:\Dokumente und Einstellungen\ulfl\Eigene Dateien\svn.openstreetmap.org\applications\editors\josm\nsis\launch4j.xml"


### download required files ###
mkdir -p downloads
cd downloads

# get latest josm version (and license)
wget -nc http://josm.eigenheimstrasse.de/download/josm-latest.jar
wget -nc http://josm.eigenheimstrasse.de/browser/LICENSE?format=raw
cp LICENSE?format=raw LICENSE

# get latest plugin (and supporting files) versions
cp ../../plugins/mappaint/mappaint.jar .
cp ../../plugins/namefinder/namefinder.jar .
#cp ../../plugins/osmarender/osmarender.jar .
#cp ../../plugins/annotation-tester/annotation-tester.jar .
#cp ../../plugins/wmsplugin/wmsplugin.jar .

# wget -N http://svn.openstreetmap.org/applications/editors/josm/plugins/mappaint/mappaint.jar
# wget -N http://www.free-map.org.uk/downloads/josm/mappaint.jar
# wget -N http://www.free-map.org.uk/downloads/josm/elemstyles.xml
wget -N http://www.eigenheimstrasse.de/josm/plugins/osmarender.jar
wget -N http://www.eigenheimstrasse.de/josm/plugins/annotation-tester.jar
wget -N http://chippy2005.googlepages.com/wmsplugin.jar
wget -N http://personales.ya.com/frsantos/validator.jar
wget -N http://thomas.walraet.net/tways/tways-0.2.jar

cd ..

### convert jar to exe ###
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
"$PROGRAM_FILES/Launch4j/launch4jc.exe" "$LAUNCH4J_XML"

### create the installer exe ###
# NSIS - http://nsis.sourceforge.net/Main_Page
"$PROGRAM_FILES/nsis/makensis.exe" /DVERSION=$VERSION josm.nsi
