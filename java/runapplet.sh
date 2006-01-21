#/bin/sh
echo run me with user and pass
java -cp dist/osm.jar:dist/OSMApplet.jar:dist/core.jar:dist/commons-logging.jar:dist/MinML2.jar:dist/commons-codec-1.3.jar:dist/commons-httpclient-3.0-rc3.jar processing.core.PApplet org.openstreetmap.processing.OSMApplet $1 $2
