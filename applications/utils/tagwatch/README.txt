Constructs an index of the tags used in OpenStreetMap data

+---+----------------+-------------------------------------------------+
| 1 | process data   | Parses an OSM file (e.g. planet.osm) looking
|   |                | for tags, and counting how often each one is
|   |                | used
+---+----------------+-------------------------------------------------+
| 2 | get photos     | Downloads sample photos of each tag, resizes
|   |                | them, and stores them with a special filename
+---+----------------+-------------------------------------------------+
| 3 | construct html | Generates HTML files listing all the tags used,
|   |                | displaying photos, descriptions, and sample 
|   |                | renderings
+---+----------------+-------------------------------------------------+
| 4 | make samples   | Takes a list of requests (from construct.pl) 
|   |                | for sample renderings, and generates them 
|   |                | using osmarender
+---+----------------+-------------------------------------------------+
| 5 | publish        | Just copy the html directory to a webserver
+---+----------------+-------------------------------------------------+


Deciding what data to publish:

  Most decisions (e.g. what tags to track in detail, what photos to use,
  the descriptions, etc.) come from pages on the OpenStreetmap wiki.
  This lets people change the output in a wiki-like fashion.
  
  See http://wiki.openstreetmap.org/index.php/Tagwatch
  
