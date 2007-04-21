dafif2osm.pl
============

This scripts attempts to convert the DAFIF data to OSM format.
There are several caveats which mean this data SHOULD NOT be uploaded 
straight to OSM:
- It may conflict with existing data
- A source tag should be added
- The DAFIF data might be wrong

This script is meant more as guide. Some of these issues may be fixed
by coding, others by manual operations on the resulting data.

I have included the 2 relevant files from DAFIF download.
Unfortunately the data is no longer available for downlod from the
original site. I am not certain of the exact version of these files,
I think it is from one of the dafif_0609 editions.

The 'copyright.txt' file is a duplicate of that present in the 
original DAFIF download.

To run the script:

$ ./dafif2osm.pl ARPT.TXT RWY.TXT airports.osm
$ josm airports.osm




