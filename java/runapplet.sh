#/bin/sh
#
# Execute the Openstreetmap.org java applet from the command line

if [ -z "$2" ] ; then
    echo "Usage: $0 <emailaddr> <password>"
    exit 1
fi

[ -n "$JAVA" ] || JAVA=java

distdir=`dirname $0`/dist

OSMCLASSPATH="$distdir/OSMApplet.jar:$distdir/MinML2.jar:$distdir/commons-codec-1.3.jar:$distdir/commons-httpclient-3.0-rc3.jar:$distdir/commons-logging.jar:$distdir/core.jar:$distdir/plugin.jar:$distdir/thinlet.jar"

exec $JAVA $JAVAOPTS \
    -cp $OSMCLASSPATH \
    processing.core.PApplet \
    org.openstreetmap.processing.OsmApplet \
    --user=$1 --pass=$2
