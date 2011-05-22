"AreaPoints" Osmosis Plugin  -- Christoph Wagner -  Apr 2011

This plugin generates additional points for closed ways, called usually "areas".

For every closed way (first point is the same as the last point) it computes the center of the bounding box and places a new node there.
The node gets all tags from the closed way a version number and a changeset-id of 0 and an id that is the complement of the way-id.
So a way with ID=23 produces a node with ID=-23.

All generated nodes will be attached at the end of the objectstream.


== Installation ==

Download the areapointsPlugin.jar file from ...somewhere  or run the ant build.xml to create it afresh under a new 'build' directory.

Place areapointsPlugin.jar in your osmosis 'lib' directory (for example lib/default) where it will be automatically added to your osmosis classpath.

== Running ==

Just use "--areapoints" or "--a2p" as argument in your pipeline and it works as explained above.
The task doesn't take any parameters for the moment.
