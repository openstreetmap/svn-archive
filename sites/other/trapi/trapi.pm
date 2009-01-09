# trapi configuration

use constant MAXTILESPERREQ => 1000;	# largest request in tiles
use constant TOOLD => 1500;		# how old database can get before refusing (seconds)
use constant MINZOOM => 11;		# bigest tiles we keep
use constant MAXOPEN => 500;		# max cached filehandles
use constant KEEPOPEN => 400;		# drop down to this when MAXOPEN hit
use constant SPLIT => 4096;		# bytes in node file before splitting
use constant TRAPIDIR => '/trapi';	# directory trapi data is in
use constant DBDIR => '';		# directory where the indexes are kept (absolute or relitive to TRAPIDIR) end with / if not empty
use constant IGNORETAGS => '^(?:created_by$|tiger:|gnis:|source$|attribution$|import_uuid$|time$|AND[_:]|massgis:|open[gG]eo[dD][bB]:|converted_by$)';	# regular expression of tags to ignore

use constant TMPDIR => "/tmp/";
use constant WEBSITE => "http://planet.openstreetmap.org/";  # site to fetch osc.gz files from
# use constant WEBSITE => "http://ftp.heanet.ie/mirrors/openstreetmap.org/";
use constant OSCDELAY => 300;		# number of seconds osc.gz files run behind

use ptdb;

1;

