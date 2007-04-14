#/bin/sh
#
# Execute the Openstreetmap.org java applet from the command line

if [ -z "$2" ] ; then
    echo "Usage: $0 <emailaddr> <password>"
    exit 1
fi

user="$1"; shift
pass="$1"; shift

if [ ! -d ./dist ] ; then
    echo "You must first build the applet using ant"
    nothing=`which ant`
    if [ $? -ne 0 ] ; then 
        echo "It appears you don't have ant installed."
        echo "Install ant then run ant in the current directory"
    fi
    nothing=`which javac`
    if [ $? -ne 0 ] ; then
        echo "It appears you don't have a java development kit installed."
        echo "Install a java development kit before running ant in the current directory"
    fi    
    exit 1   
fi

[ -n "$JAVA" ] || JAVA=java

distdir=`dirname $0`/dist

echo distdir is $distdir

OSMCLASSPATH="$distdir/OSMApplet.jar:$distdir/MinML2.jar:$distdir/commons-codec-1.3.jar:$distdir/commons-httpclient-3.0-rc3.jar:$distdir/commons-logging.jar:$distdir/core.jar:$distdir/plugin.jar:$distdir/thinlet.jar"

exec $JAVA $JAVAOPTS \
    -cp $OSMCLASSPATH \
    processing.core.PApplet \
    org.openstreetmap.processing.OsmApplet \
    --user="$user" --pass="$pass" $@
