#/bin/sh
echo run me with user and pass
java -cp dist/OSMApplet.jar processing.core.PApplet org.openstreetmap.processing.OSMApplet $1 $2
