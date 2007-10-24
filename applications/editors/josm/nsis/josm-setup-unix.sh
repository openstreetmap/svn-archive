#!/bin/bash

# Creates an josm-install.exe File
# for working on a debian-unix system install the nsis package with
# apt-get install nsis
# replace the  /usr/share/nsis/Plugins/System.dll with the new Version from the nsis .zip File
# The old one is missing the Call:: Function
# then download launch4j from http://launch4j.sourceforge.net/

## settings ##
LAUNCH4J="java -jar launch4j/launch4j.jar"

export VERSION=latest
#export VERSION=custom

##################################################################
### Build the Complete josm + Plugin Stuff
if true; then
    echo "Build the Complete josm Stuff"
    
    echo "Compile Josm"
    cd ../core
    ant clean
    ant compile || exit -1
    cd ..
	
    echo "Compile Josm Plugins"
    cd plugins
    ant clean
    ant dist || exit -1
fi


##################################################################
### Copy the required Stuff into the download Directory
mkdir -p downloads
( 
    cd downloads
    
    # get latest josm version (and license)
    cp ../../core/LICENSE LICENSE
    cp ../../core/dist/josm-custom.jar josm-latest.jar
    
    # Get all plugins
    cp ../../plugins/dist/*.jar .
)

##################################################################
### convert jar to exe
# (makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
$LAUNCH4J ./launch4j.xml

##################################################################
### create the installer exe
# NSIS - http://nsis.sourceforge.net/Main_Page
# apt-get install nsis
makensis -DVERSION=$VERSION josm.nsi
