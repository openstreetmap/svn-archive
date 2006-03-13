#/bin/sh
echo run me with user and pass
java -cp dist/OSMApplet.jar:dist/MinML2.jar:dist/commons-codec-1.3.jar:dist/commons-httpclient-3.0-rc3.jar:dist/commons-logging.jar:dist/core.jar:dist/plugin.jar processing.core.PApplet org.openstreetmap.processing.OsmApplet --user=$1 --pass=$2
