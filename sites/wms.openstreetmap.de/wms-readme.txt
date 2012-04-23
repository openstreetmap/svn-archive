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
with the exeption of some metadata fields in layers:
 "copyright"     - This is used for layer specific copyright messages rendered as watermark.
 "-wms-disabled" - If true, WMS is disabled for this layer.
 "-terms-of-use" - Additional terms-of-use text that is displayed on the overview index page.

