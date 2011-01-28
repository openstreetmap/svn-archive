csv2osm - basic conversion from comma-seperated file to .osm
------------------------------------------------------------

This ruby script will perform basic file conversion from csv to .osm format

The output format is suitable for loading into JOSM. It may or may not be
suitable for upload to the OpenStreetMap server. This script does not perform
upload to OSM. It also does not do any checks for suitability for upload to the
the OSM database. Objects are assigned negative ids which means the file
*could* be uploaded to OSM via JOSM or bulk_upload.py, however it is up to the
user to decide if that is a good idea, and this should only be done in
compliance with the import guidelines:
http://wiki.openstreetmap.org/wiki/Import/Guidelines

The script will load config.rb for config settings if necessary, but can be run
with the defaults already in there. By default it is configured to expect a file
name as a command line argument, and output the .osm xml to stdout. This allows
you to call it with a command such as:

ruby csv2osm.rb < my_input_file.csv  > my_output_file.osm

If you specify an @output_file in the config (rather than nil for standard out)
then the script will put progress/status/warning information to standard out.

By default it will expect a header row, and use that for the generated key
names (:add_to_mapping mode)  Edit config.rb to add additional output keys,
or to override reading of the header row (pick out you own selection of columns)

Some keys names are special, and so map onto attributes of the output <node>
elements rather than tags. Special key names are: $NODElat, $NODElon, $NODEuid,
$NODEuser, $NODEvisible, and $NODEversion. $NODElat and $NODElon are important.
They should be WGS84 coordinates. If you fail to map anything to these key
names, then you have invalid nodes (no location!). Conversely uid, user,
visible, and version are optional and would normally not be mapped.

For the input, the script uses ruby's csv parsing library which handles various
quirks of csv format files. Values may be with or without quotes.


GPL
