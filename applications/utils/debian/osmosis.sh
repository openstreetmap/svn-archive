#!/bin/sh

if [ "x$@" = "x" ] \
    || [ "x$@" = 'x--help' ] \
    || [ "x$@" = 'x-help' ] \
    || [ "x$@" = "x-h" ]  \
    || [ "x$@" = "x-?" ] \
    || [ "x$@" = "x-help" ]; then
cat <<EOF
osmosis

Example Usage

Import a planet file into a local MySQL database.

osmosis --read-xml file=~/osm/planbet/planet.osm --write-mysql host="x" database="x" user="x" password="x"

Export a planet file from a local MySQL database.

osmosis --read-mysql host="x" database="x" user="x" password="x" --write-xml file="planet.osm"

Derive a change set between two planet files.

osmosis --read-xml file="planet1.osm" --read-xml file="planet2.osm" --derive-change --write-xml-change file="planetdiff-1-2.osc"

Derive a change set between a planet file and a database.

osmosis --read-xml file="planet1.osm" --read-mysql host="x" database="x" user="x" password="x" --derive-change --write-xml-change file="planetdiff-1-2.osc"

Apply a change set to a planet file.

osmosis --read-xml file="planet1.osm" --read-xml-change file="planetdiff-1-2.osc" --apply-change --write-xml file="planet2.osm"

Sort the contents of a planet file.

osmosis --read-xml file="data.osm" --sort type="TypeThenId" --write-xml file="data-sorted.osm"

The above examples make use of the default pipe connection feature, however a simple read and write planet file command line could be written in two ways. The first example uses default pipe connection, the second explicitly connects the two components using a pipe named "mypipe". The default pipe connection will always work so long as each task is specified in the correct order.

osmosis --read-xml file="planetin.osm" --write-xml file="planetout.osm"

osmosis --read-xml file="planetin.osm" outPipe.0="mypipe" --write-xml file="planetout.osm" inPipe.0="mypipe"
[edit] Detailed Usage

Full usage details are available at: http://www.bretth.com/wiki/Wiki.jsp?page=OpenStreetMap 

EOF
else
    java -jar /usr/local/share/osmosis/osmosis.jar "$@"
fi