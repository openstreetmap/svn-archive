OsmGenerate
===========

Usage:
        osmgenerate <num-ways> <segments-per-way>


This is a trival tool to generate OSM data of an arbitrary size.
This may be used to test the performance and scalability of a variety of OSM tools.

e.g. osmgenerate 1000 4 > fake1k.osm
     josm fake1k.osm

This is intended as a tool for developers. If you want something other than a simple
grid of ways then you will have to edit the source!

Feel free to add command line switches for more complex data patterns into SVN :-)

