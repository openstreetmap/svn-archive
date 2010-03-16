The Mapserver configuration at wms.openstreetmap.de

wms.openstreetmap.de can be accessed via the following URL:

http://wms.openstreetmap.de/wms

Tiles are available via:

http://wms.openstreetmap.de/tms/<layername>/<z>/<x>/<y>.png

Copyright Watermarks are always added to the requested layers. Requests for
more thane one layer are denied.


Implementation:

The Mapserver is implemented as a python wsgi-script inside the Apache
server.  It is using methods from umn mapserver (python mapscript). 
Configuration is done by a map-file which is read by the wsgi-script.

In addition to the script itself tiles are cached using Apache
mod_disk_cache.


Configuration:

Layers are added using osmwms.map. This is (more or less) an ordinary mapfile
with the exeption of the "copyright" metadata in layers. This is used for
layer specific copyright messages rendered as watermark. Files need to user
EPSG:4326 projection. In Addition to the mapfile new layers should also be
added manually to the index.html shown at the servers root directory.
