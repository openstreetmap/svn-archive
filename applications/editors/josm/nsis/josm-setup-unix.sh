#!/bin/bash

# Creates an josm-install.exe File
# for working on a debian-unix system install the nsis package with
# apt-get install nsis
# replace the  /usr/share/nsis/Plugins/System.dll with the new Version from the nsis .zip File
# The old one is missing the Call:: Function
# then download launch4j from http://launch4j.sourceforge.net/

## settings ##
LAUNCH4J="java -jar launch4j/launch4j.jar"

svncorerevision=`svnversion ../core`
svnpluginsrevision=`svnversion ../plugins`
svnrevision="$svncorerevision-$svnpluginsrevision"

#export VERSION=latest
export VERSION=custom-${svnrevision}

echo "Creating Windows Instller for josm-$VERSION"

##################################################################
### Build the Complete josm + Plugin Stuff
if true; then
    (
	echo "Build the Complete josm Stuff"
	
	echo "Compile Josm"
	cd ../core
	ant -q clean
	ant -q compile || exit -1
	cd ..
	
	echo "Compile Josm Plugins"
	cd plugins
	ant -q clean
	ant -q dist || exit -1
	) || exit -1
fi


echo 
echo "##################################################################"
echo "### Copy the required Stuff into the download Directory"
mkdir -p downloads
( 
    cd downloads
    
    # get latest josm version (and license)
    cp ../../core/LICENSE LICENSE
    cp ../../core/dist/josm-custom.jar josm-latest.jar
    
    # Get all plugins
    cp ../../plugins/dist/*.jar .
)

echo 
echo "##################################################################"
echo "### convert jar to exe with launch4j"
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
$LAUNCH4J ./launch4j.xml

echo 
echo "##################################################################"
echo "### create the installer exe with makensis"
# NSIS - http://nsis.sourceforge.net/Main_Page
# apt-get install nsis
makensis -DVERSION=$VERSION josm.nsi
