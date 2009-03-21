#!/bin/bash

# Creates an josm-setup-xy.exe File
#
# for working on a debian-unix system install the nsis package with
# apt-get install nsis
# replace the  /usr/share/nsis/Plugins/System.dll with the Version from the nsis .zip File
# The one comming with the debian package is missing the Call:: Function
# See also /usr/share/doc/nsis/README.Debian 
#
# Then download launch4j from http://launch4j.sourceforge.net/ 
# wget http://mesh.dl.sourceforge.net/sourceforge/launch4j/launch4j-3.0.0-pre2-linux.tgz
# and unpack it to /usr/share/launch4j

## settings ##

# trying to find launch4j
if [ -s /cygdrive/c/Programme/Launch4j/launch4jc.exe ]; then
    # Windows under cygwin
    LAUNCH4J="/cygdrive/c/Programme/Launch4j/launch4jc.exe"
elif [ -s /usr/share/launch4j/launch4j.jar ]; then
    # as described above
    LAUNCH4J="java -jar /usr/share/launch4j/launch4j.jar"
else
    # launch4j installed locally under this nsis folder
    LAUNCH4J="java -jar ./launch4j/launch4j.jar"
fi
echo Using launch4j: $LAUNCH4J

# trying to find makensis
if [ -s /cygdrive/c/Programme/nsis/makensis.exe ]; then
    # Windows under cygwin
    MAKENSIS="/cygdrive/c/Programme/nsis/makensis.exe"
else
    # UNIX like
    MAKENSIS=makensis
fi
echo Using NSIS: $MAKENSIS

svncorerevision=`svnversion ../core`
svnpluginsrevision=`svnversion ../plugins`
svnrevision="$svncorerevision-$svnpluginsrevision"

#export VERSION=latest
export VERSION=custom-${svnrevision}                                         

echo "Creating Windows Installer for josm-$VERSION"

echo 
echo "##################################################################"
echo "### Download and unzip the webkit stuff"
wget -nc http://josm.openstreetmap.de/download/windows/webkit-image.zip
mkdir -p webkit-image
cd webkit-image
unzip -o ../webkit-image.zip
cd ..

echo 
echo "##################################################################"
echo "### Build the Complete josm + Plugin Stuff"
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
echo "### convert jar to exe with launch4j"
# (an exe file makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
# delete old exe file first
rm -f josm.exe
$LAUNCH4J "launch4j.xml"

if ! [ -s josm.exe ]; then
    echo "NO Josm File Created"
    exit -1
fi

echo 
echo "##################################################################"
echo "### create the josm-installer-${VERSION}.exe with makensis"
# NSIS - http://nsis.sourceforge.net/Main_Page
# apt-get install nsis
$MAKENSIS -V2 -DVERSION=$VERSION josm.nsi

# keep the intermediate file, for debugging
rm -f josm-intermediate.exe
mv josm.exe josm-intermediate.exe
