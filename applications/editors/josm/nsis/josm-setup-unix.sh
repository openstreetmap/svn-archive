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
elif [ -s ../launch4j/launch4j.jar ]; then
    LAUNCH4J="java -jar ../launch4j/launch4j.jar"
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
    MAKENSIS=/usr/bin/makensis
fi
echo Using NSIS: $MAKENSIS

if [ -n "$1" ]; then
  export VERSION=$1
  export JOSM_BUILD="no"
  export WEBKIT_DOWNLAD="no"
  export JOSM_FILE="/var/www/josm.openstreetmap.de/download/josm-tested.jar"
else
  svncorerevision=`svnversion ../core`
  svnpluginsrevision=`svnversion ../plugins`
  svnrevision="$svncorerevision-$svnpluginsrevision"

  export VERSION=custom-${svnrevision}
  export JOSM_BUILD="yes"
  export WEBKIT_DOWNLOAD="yes"
  export JOSM_FILE="..\core\dist\josm-custom.jar"
fi

echo "Creating Windows Installer for josm-$VERSION"

echo 
echo "##################################################################"
echo "### Download and unzip the webkit stuff"
if [ "x$WEBKIT_DOWNLOAD" == "xyes" ]; then
    wget --continue --timestamping http://josm.openstreetmap.de/download/windows/webkit-image.zip
fi
mkdir -p webkit-image
cd webkit-image
unzip -o ../webkit-image.zip
cd ..

echo 
echo "##################################################################"
echo "### Build the Complete josm + Plugin Stuff"
if [ "x$JOSM_BUILD" == "xyes" ]; then
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

/bin/cp $JOSM_FILE josm-tested.jar
echo 
echo "##################################################################"
echo "### convert jar to exe with launch4j"
# (an exe file makes attaching to file extensions a lot easier)
# launch4j - http://launch4j.sourceforge.net/
# delete old exe file first
/bin/rm -f josm.exe
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
/bin/rm josm-intermediate.exe 2>/dev/null >/dev/null
/bin/rm -f josm-tested.jar 2>/dev/null >/dev/null
/bin/mv josm.exe josm-intermediate.exe 2>/dev/null >/dev/null
