# trapi configuration

# largest request in tiles
use constant MAXTILESPERREQ => 1000;	
# how old database can get before refusing (seconds)
use constant TOOLD => 1500;
# bigest tiles we keep (1 - 14)
use constant MINZOOM => 11;
# max cached filehandles
use constant MAXOPEN => 500;
# drop down to this when MAXOPEN hit
use constant KEEPOPEN => 400;
# bytes in node file before splitting
use constant SPLIT => 4096;
# directory trapi data is in
use constant TRAPIDIR => '/trapi';
# directory where the indexes are kept (absolute or relitive to TRAPIDIR) end with / if not empty
use constant DBDIR => '';
# regular expression of tags to ignore.  undef disables.
use constant IGNORETAGS => '^(?:created_by$|tiger:|gnis:|source$|attribution$|import_uuid$|time$|AND[_:]|massgis:|open[gG]eo[dD][bB]:|converted_by$|KSJ2:|uploaded_by$|source_ref$|gns:)';

# directory to save osc files in
use constant TMPDIR => "/tmp/";
# site to fetch osc.gz files from
use constant WEBSITE => "http://planet.openstreetmap.org/";
# heanet keeps daily changes longer, so use them if using old planet.  They are not up to date on hour or minute files.
# use constant WEBSITE => "http://ftp.heanet.ie/mirrors/openstreetmap.org/";
# number of seconds osc.gz files run behind
use constant OSCDELAY => 300;
# seconds to wait if within OSCDELAY
use constant WAITDELAY => 59;
# seconds to wait if error fetching osc file
use constant WAITFAIL => 62;
# tiles to garbagecollect per osc file processed.  0 disables garbagecollection
use constant GCCOUNT => 20;
# Number of times a string is repeated to get common string handling
use constant THRESH => 32;

# which tags compression to use for new tiles.  0 is old uncompressed version.
use constant TAGSVERSION => 1;

use ptdb;

1;

