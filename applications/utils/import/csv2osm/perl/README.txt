csv2osm - CSV checking converter (& uploader)
---------------------------------------------

This is an old perl script which no longer works since OSM API version 0.3

It will apply a set of user-supplied rules, to turn a CSV file into OSM.

It is not a straightfoward file converter, rather it performs an immediate
upload of the data to the OpenStreetMap server (this bit doesn't work with the
current API version)  This is normally frowned upon these days. Better to
convert to a .osm file to review prior to upload using JOSM or a bulk_upload.py

In fact this script will perform downloads to check to see if it's likely that
the object already  exists in OSM. If it thinks it might, it'll report the
likely match, and defer the upload.

Some further documentation on the wiki:
http://wiki.openstreetmap.org/wiki/Perl_Scripts#.2Fcsv2osm

TODO: change this script to straightforward file conversion.

GPL



