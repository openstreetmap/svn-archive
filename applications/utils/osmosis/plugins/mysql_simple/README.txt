"MySqlSimplePlugin" Osmosis Plugin  -- Christoph Wagner -  Jun 2011

This plugin writes OSM-Data as SQL to a dumpfile.

the whole thing was inspired by:
http://goblor.de/wp/2009/10/16/openstreetmap-projekt-teil-1-openstreetmap-daten-in-mysql-datenbank-einlesen/

it uses the schema that is described there.

== Installation ==

Download the mysqlsimplePlugin.jar file from ...somewhere  or run the ant build.xml to create it afresh under a new 'build' directory.

Place mysqlsimplePlugin.jar in your osmosis 'lib' directory (for example lib/default) where it will be automatically added to your osmosis classpath.

== Running ==

Just use "--mysql-simple-dump" or "--wmsd" as argument in your pipeline and specify an outputfile.
You may also use - for stdout.

Example:
osmosis --rx data.osm --wmsd data.sql

To get it in to your mysql database use mysql commandline utility, for example:
mysql osm --user=dbuser -p < data.sql
