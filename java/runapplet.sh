#/bin/sh
echo run me with user and pass
java -cp dist/OSMApplet.jar processing.core.PApplet org.openstreetmap.processing.OsmApplet --user=$1 --pass=$2
