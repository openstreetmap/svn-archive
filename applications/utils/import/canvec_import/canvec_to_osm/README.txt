The adjacent file canvec_to_osm_feature contains a list of dictionaries describing the features to be converted. The
contained elements are used to compose the rules, shp, and osm file names. During execution, a PYC file is generated
out of this file, which is harmless.
This script has been being tested with Python 2.6 on Linux, and incidentally with Python 2.5 on Windows.
The file canvec_to_osm_features.py must be in the same dir.

Requirements:
- Python 2.6 (recommended) or 2.5: http://www.python.org/
- Java Runtime Environment: http://www.java.com/
- recent version of shp-to-osm: http://redmine.yellowbkpk.com/projects/list_files/geo

Installation:
- create a directory to put canvec-to-osm.py and canvec_to_osm_features.py in
- extract the contents of rules.zip into a directory named "rules"
- put the shp-to-osm JAR file in a directory named "bin"
- eventually change the configuration variables

Usage: canvec-to-osm.py [-c] nts_tile

Options:
  --version      show program's version number and exit
  -h, --help     show this help message and exit
  -v, --verbose  print many messages
  -q, --quiet    be quiet
  -l, --log      log output of shp-to-osm
  -c, --cache    use local canvec cache only
The NTS tile has to be given in the form 999X or 999X00. In the first case, all 16 tiles will be processed.

History
- 11/11/2009 0.1   First release

