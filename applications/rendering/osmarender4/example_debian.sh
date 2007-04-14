#!/bin/sh
# Checks to see if everything that is needed is installed, then downloads
#  a sample area
# Accepts a bbox on the command line, or goes with a default

BBOX=$1
if [ -z $BBOX ]; then
	# Default is of Southern Oxford
	BBOX="-1.25,51.71,-1.20,51.75"
	# For all of Oxford, use:
	#BBOX="-1.3586995030608724,51.69824616779909,-1.1249755249943214,51.83024250815124"
fi

XALAN=""
RENDERER=""

# Check if xalan is installed
# We can use the binary, or the .jar
dpkg -L xalan > /dev/null 2>/dev/null
HAS_XALAN=$?
dpkg -L libxalan2-java > /dev/null 2>/dev/null
HAS_XALAN_JAR=$?

if [ $HAS_XALAN == 0 ]; then
	XALAN="xalan"
elif [ $HAS_XALAN_JAR == 0 ]; then
	XALAN="java -jar /usr/share/java/xalan2.jar"
else
	# No xalan found
	echo "No xalan was found. Please run one of:"
	echo ""
	echo "   sudo apt-get install xalan"
	echo "   sudo apt-get install libxalan2-java"
	exit 1
fi


# We need a render, either librsvg2-bin or inkscape
dpkg -L librsvg2-bin > /dev/null 2>/dev/null
HAS_RSVG=$?
dpkg -L inkscape > /dev/null 2>/dev/null
HAS_INKSCAPE=$?
if [ $HAS_RSVG == 0 ]; then
	RENDERER="rsvg -f png data.svg data.png"
elif [ $HAS_INKSCAPE == 0 ]; then
	RENDERER="inkscape -D -e data.png data.svg"
else
	# No svg renderer found
	echo "No SVG renderer was found. Please run one of:"
	echo ""
	echo "   sudo apt-get install librsvg2-bin"
	echo "   sudo apt-get install inkscape"
	exit 1
fi


# Grab the .osm file, if we don't already have one
download_url='http://www.openstreetmap.org/api/0.3/map?'
download_bbox="bbox=$BBOX"
if [ ! -s data.osm ] ; then
	echo "Fetching OSM data for bounding box "
	echo " $BBOX"
	echo ""

	# Do they have a netrc for openstreetmap?
	CURL_ARGS=""
	if [[ -f $HOME/.netrc ]]; then
		grep -q 'openstreetmap.org' $HOME/.netrc
		NO_ENTRY=$?
		if [[ $NO_ENTRY -eq 0 ]]; then
			CURL_ARGS="--netrc"
		fi
	fi
	if [[ "$CURL_ARGS" == "" ]]; then
		echo "No .netrc entry found for openstreetmap"
		echo ""
		echo "Please enter your OpenStreetMap username (normally your email)"
		read USERNAME
		CURL_ARGS="-u $USERNAME"
	fi

    curl --anyauth $CURL_ARGS -o data.osm "$download_url$download_bbox"
else
	echo "Using existing data.osm file"
	echo ""
fi

# Start rendering
$XALAN -in rules/standard.xml -out data.svg -param osmfile "'data.osm'"

# svg --> png
$RENDERER
