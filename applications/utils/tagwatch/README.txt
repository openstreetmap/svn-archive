

The Tagwatch script creates a statistical website that show the usage
of all Keys/Tags/Relations of the given osm files.


Dependencies:
 + Perl
 + LWP::Simple
 + MediaWiki-1.13
 + HTML-Template-2.9
 + Math::Round
 + GD
 + Inkscape
 + xsltproc

+---+----------------+-----------------------------------------------------+
| 1 | edit conf file |
|   |                |
+---+----------------+-----------------------------------------------------+
| 2 | run the script | perl run.pl everything else should work on its own
|   |                |
|   |                | Additionally all options can be pased in commandline
|   |                |  run.pl option1=val option2=val ...
|   |                | The option config_file can be used to choose another
|   |                | configuration file.
|   |                |
+---+----------------+-----------------------------------------------------+
| 3 | publish        | Just copy the html directory to a webserver
+---+----------------+-----------------------------------------------------+

Deciding what data to publish:

  Most decisions (e.g. what tags to track in detail, what photos to use,
  the descriptions, etc.) come from pages on the OpenStreetmap wiki.
  This lets people change the output in a wiki-like fashion.

  See http://wiki.openstreetmap.org/index.php/Tagwatch

ToDo List:
 + osmxapi Bounding box
 + more interface translations in the osmwiki
 + osmarender sample images for some combinations
   based either on the documentation in the wiki
   or the top usage tags
 + ...