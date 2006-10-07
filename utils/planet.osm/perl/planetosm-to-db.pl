#!/usr/bin/perl
# Takes a planet.osm, and loads it into a database
#
# PostgreSQL support ($dbtype='pgsql'):
#	nodes,node_tags,segments,segment_tags,ways,way_tags,way_segments tables
#	includes schema files at the end
#
# MySQL support ($dbtype='mysql'):
#	same table layout as main OSM server
#
# Nick Burch
#     v0.01   23/07/2006
#     v0.02   26/07/2006 mysql added (Richard Fairhurst)

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl");

    unshift(@INC,"../perl");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl");
}

use strict;
use warnings;

use DBI;
use Getopt::Long;

use Utils::Debug;
use Geo::OSM::Planet;
use Pod::Usage;

# We need Bit::Vector, as perl hashes can't handle the sort of data we need
use Bit::Vector;

sub do_node_tag($$$$); # {}
sub do_segment_tag($$$$); # {}
sub do_way_tag($$$$); # {}
sub fetch_schema($); # {}
sub build_node_ps($$); # {}
sub build_node_tag_ps($$); # {}
sub build_node_tag_ps_mysql($$); # {}
sub build_seg_ps($$); # {}
sub build_seg_tag_ps($$); # {}
sub build_seg_tag_ps_mysql($$); # {}
sub build_way_ps($$); # {}
sub build_way_seg_ps($$); # {}
sub build_way_tag_ps($$); # {}
sub should_batch_inserts($$); # {}
sub check_bbox_valid(@); # {}
sub display_count($$); #{}

my $dbtype = $ENV{DBTYPE} || "pgsql";	# mysql | pgsql
my $dbname = $ENV{DBNAME} || "osm";
my $dbhost = $ENV{DBHOST} || "localhost";
my $dbuser = $ENV{DBUSER} || "";
my $dbpass = $ENV{DBPASS} || "";

our $man=0;
our $help=0;
my $do_empty=0;
my $do_schema=0;
my $do_bbox='';
my $do_exbbox='';

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'debug+'           => \$DEBUG,      
	     'd+'               => \$DEBUG,      
	     'verbose+'         => \$VERBOSE,
	     'v+'               => \$VERBOSE,
	     'MAN'              => \$man, 
	     'man'              => \$man, 
	     'h|help|x'         => \$help, 

	     'no-mirror'        => \$Utils::LWP::Utils::NO_MIRROR,
	     'proxy=s'          => \$Utils::LWP::Utils::PROXY,

	     'empty'            => \$do_empty,
	     'schema'		=> \$do_schema,
	     'bbox:s'		=> \$do_bbox,
	     'exbbox:s'		=> \$do_exbbox,
	     'ebbox:s'		=> \$do_exbbox,

	     'dbtype' => \$dbtype,
	     'dbname' => \$dbname,
	     'dbhost' => \$dbhost,
	     'dbuser' => \$dbuser,
	     'dbpass' => \$dbpass,
	     ) or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

# Grab the filename
my $xml = shift||'';

# Should we warn for things we skip due to the bbox?
my $warn_bbox_skip = 0;

if($do_schema) {
	print fetch_schema($dbtype);
	exit;
}

# Exclude nodes within this lat,long,lat,long bounding box
my @exclude_bbox = ();
my @only_bbox = ();

if($do_bbox) {
	@only_bbox = split(/,/, $do_bbox);
}
if($do_exbbox ) {
	@exclude_bbox = split(/,/, $do_exbbox);
}

# Check that things are in the right order
if(@only_bbox) {
	check_bbox_valid(@only_bbox);
}
if(@exclude_bbox) {
	check_bbox_valid(@exclude_bbox);
}

if ( ! $xml ) {
    $xml = mirror_planet();
};

# Give them help, if they need it
unless( $do_empty || $xml) {
     pod2usage(1);
}

our $PARSING_DISPLAY_TIME=0;
our $PARSING_START_TIME=time();

# Open our database connection
my $conn = &open_connection($dbtype,$dbname,$dbhost,$dbuser,$dbpass);

# Empty out the database if requested
if ( $do_empty ) {
	# Skip over errors
	$conn->{PrintError} = 1;
	$conn->{RaiseError} = 0;

	# Process
	foreach my $sql (split "\n",&empty_tables($dbtype)) {
		print "$sql\n";
		$conn->do($sql);
	}

	# Also zap and indexes we create in post process
	my $post_sql = &post_process($dbtype);
	my @indexes = ($post_sql =~ /^CREATE INDEX (.*?) /gm);
	foreach my $index (@indexes) {
		my $sql = "DROP INDEX $index";
		print "$sql\n";
		$conn->do($sql);
	}
	exit;
}

# Check we can load the file
unless( -f $xml || $xml eq "-" ) {
	die("Planet.osm file '$xml' could not be found\n");
}

if ( $xml ne "-" && ! -s $xml ) {
    die " $xml has 0 size\n";
}

# Get our prepared statements
my $node_ps = build_node_ps($dbtype,$conn);
my $node_tag_ps = build_node_tag_ps($dbtype,$conn);
my $node_tag_ps_mysql = build_node_tag_ps_mysql($dbtype,$conn);
my $seg_ps = build_seg_ps($dbtype,$conn);
my $seg_tag_ps = build_seg_tag_ps($dbtype,$conn);
my $seg_tag_ps_mysql = build_seg_tag_ps_mysql($dbtype,$conn);
my $way_ps = build_way_ps($dbtype,$conn);
my $way_seg_ps = build_way_seg_ps($dbtype,$conn);
my $way_tag_ps = build_way_tag_ps($dbtype,$conn);

# Should we batch inserts in transactions to help performance?
# (DB specific if this helps or hinders)
my $batch_inserts = should_batch_inserts($dbtype,$conn);

# Counts of the numbers handled
my $node_count = 0;
my $seg_count = 0;
my $way_count = 0;
my $line_count = 0;

# We assume IDs to be up to 50 million
my $nodes = Bit::Vector->new( 50 * 1000 * 1000 );
my $segs = Bit::Vector->new( 50 * 1000 * 1000 );

# Turn on batching, if the database likes that
if($batch_inserts) {
	$conn->{AutoCommit} = 0;
}

# Process
open(XML, "<$xml");
#open(XML, "<:utf8", $xml);

# Hold the id and type of the last valid main tag
my $last_id;
my $last_type;

# Hold the segment and tags list for a way
# (We only add the way+segments+tags if has valid segments)
my $way_line;
my @way_tags;
my @way_segs;

# Loop over the data
while(my $line = <XML>) {
	$line_count++;

	# Handle batches, if the DB wants them
	if($line_count % 10000 == 0) {
	    if($batch_inserts) {
		$conn->commit();
	    }
	}
	display_count("line",$line_count);

	# Process the line of XML
	if($line =~ /^\s*<node/) {
		my ($id,$lat,$long) = ($line =~ /^\s*<node id=['"](\d+)['"] lat=['"]?(\-?[\d\.]+)['"]? lon=['"]?(\-?[\d\.]+e?\-?\d*)['"]?/);
		$last_id = undef; # In case it has tags we need to exclude
		$last_type = "node";

		unless($id) { warn "Invalid line '$line'"; next; }

		# Do we need to exclude this node?
		if(@exclude_bbox) {
			if($lat > $exclude_bbox[0] && $lat < $exclude_bbox[2] &&
				$long > $exclude_bbox[1] && $long < $exclude_bbox[3]) {
				if($warn_bbox_skip) {
					warn("Skipping node at $lat $long as in bbox\n");
				}
				next;
			}
		}
		if(@only_bbox) {
			if($lat > $only_bbox[0] && $lat < $only_bbox[2] &&
				$long > $only_bbox[1] && $long < $only_bbox[3]) {
				# This one's inside the bbox
			} else {
				if($warn_bbox_skip) {
					warn("Skipping node at $lat $long as not in bbox\n");
				}
				next;
			}
		}

		# Add the node
		$node_ps->execute($id,$lat,$long) 
			or warn("Invalid line '$line' : ".$conn->errstr);

		$nodes->Bit_On($id);
		$last_id = $id;

		$node_count++;
		&display_count("node", $node_count);
	}
	elsif($line =~ /^\s*<segment/) {
		my ($id,$from,$to) = ($line =~ /^\s*<segment id=['"](\d+)['"] from=['"](\d+)['"] to=['"](\d+)['"]/);
		$last_id = undef; # In case it has tags we need to exclude
		$last_type = "segment";

		unless($id) { warn "Invalid line '$line'"; next; }

		unless($nodes->contains($to)) { 
			if($warn_bbox_skip) {
				warn "No node $to for line '$line'"; 
			}
			next; 
		}
		unless($nodes->contains($from)) { 
			if($warn_bbox_skip) {
				warn "No node $from for line '$line'"; 
			}
			next; 
		}

		$seg_ps->execute($id,$from,$to)
			or warn("Invalid line '$line' : ".$conn->errstr);

		$segs->Bit_On($id);
		$last_id = $id;

		$seg_count++;
		#&display_count("segment", $seg_count);
	}
	elsif($line =~ /^\s*\<way/) {
		my ($id) = ($line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);
		$last_id = undef; # In case it has tags we need to exclude
		$last_type = "way";

		unless($id) { warn "Invalid line '$line'"; next; }

		# Save ID and line, will add later
		$last_id = $id;
		$way_line = $line;

		$way_count++;
		&display_count("way", $way_count);

		# Blank way children lists
		@way_tags = ();
		@way_segs = ();
	}
	elsif($line =~ /^\s*\<\/way/) {
		my $way_id = $last_id;
		$last_id = undef;

		unless($way_id) { 
			# Invalid way, skip
			next; 
		}

		unless(@way_segs) {
			if($warn_bbox_skip) {
				warn("Skipping way with no valid segments with id '$way_id'");
			}
			next;
		}

		# Add way
		$way_ps->execute($way_id)
			or warn("Invalid line '$way_line' : ".$conn->errstr);

		# Add way segments
		my $way_seg_count = 0;
		foreach my $ws (@way_segs) {
			$way_seg_count++;
			$way_seg_ps->execute($way_id,$ws->{id},$way_seg_count)
				or warn("Invalid line '$ws->{line}' : ".$conn->errstr);
		}
		# Add way tags
		foreach my $wt (@way_tags) {
			do_way_tag($way_id,$wt->{name},$wt->{value},$wt->{line});
		}
	}
	elsif($line =~ /^\s*\<seg /) {
		my ($id) = ($line =~ /^\s*\<seg id=[\'\"](\d+)[\'\"]/);
		unless($last_id) { next; }
		unless($id) { warn "Invalid line '$line'"; next; }
		unless($segs->contains($id)) { 
			if($warn_bbox_skip) {
				warn "Invalid segment for line '$line'"; 
			}
			next; 
		}

		# Save, only add later
		my %ws;	
		$ws{'line'} = $line;
		$ws{'id'} = $id;

		push (@way_segs,\%ws);
	}
	elsif($line =~ /^\s*\<tag/) {
		my ($name,$value) = ($line =~ /^\s*\<tag k=[\'\"](.*?)[\'\"] v=[\'\"](.*?)[\'\"]/);
		unless($name) { warn "Invalid line '$line'"; next; }
		if($name =~ /^\s+$/) { warn "Skipping invalid tag line '$line'"; next; }

		# Decode the XML elements in the name and value
		$value =~ s/\&apos\;/\'/g;
		
		# If last_id isn't there, the tag we're attached to was invalid
		unless($last_id) {
			if($warn_bbox_skip) {
				warn("Invalid previous $last_type, ignoring its tag '$line'");
			}
			next;
		}

		if($last_type eq "node") {
			do_node_tag($last_id,$name,$value,$line);
		} elsif($last_type eq "segment") {
			do_segment_tag($last_id,$name,$value,$line);
		} elsif($last_type eq "way") {
			# Save, only add if way has segments
			my %wt;	
			$wt{'line'} = $line;
			$wt{'name'} = $name;
			$wt{'value'} = $value;

			push (@way_tags,\%wt);
		}
	    }	
	elsif($line =~ /^\s*\<\?xml/) {
	}
	elsif($line =~ /^\s*\<osm /) {
	}
	elsif($line =~ /^\s*\<\/node\>/) {
	}
	else {
	    print STDERR "Unknown line $line\n";
	};
}

# End the batch, if the database likes that
if($batch_inserts) {
	$conn->commit();
	$conn->{AutoCommit} = 1;
} else {
    enable_keys($dbtype,$conn,"nodes");
    enable_keys($dbtype,$conn,"segments");
    enable_keys($dbtype,$conn,"ways");
}

# Post-processing
foreach my $sql (split "\n",&post_process($dbtype)) {
	print "$sql\n";
	$conn->do($sql);
}


########################################################################

sub do_node_tag($ $ $ $) {
	my ($last_id,$name,$value,$line) = @_;

	if ($dbtype eq 'pgsql') {
		do_tag_add($node_tag_ps,$last_id,$name,$value,$line);
	} else {
		do_tag_append($node_tag_ps_mysql,$last_id,$name,$value,$line);
	}
}
sub do_segment_tag($ $ $ $) {
	my ($last_id,$name,$value,$line) = @_;

	if ($dbtype eq 'pgsql') {
		do_tag_add($seg_tag_ps,$last_id,$name,$value,$line);
	} else {
		do_tag_append($seg_tag_ps_mysql,$last_id,$name,$value,$line);
	}
}
sub do_way_tag($ $ $ $) {
	my ($last_id,$name,$value,$line) = @_;

	do_tag_add($way_tag_ps,$last_id,$name,$value,$line);
}

# Postgres style "one row per tag" tag addition
sub do_tag_add($ $ $ $ $) {
	my ($tag_ps,$last_id,$name,$value,$line) = @_;

	$tag_ps->execute($last_id,$name,$value)
		or warn("Invalid line '$line' : ".$conn->errstr);
}
# MySQL style "; between tags" tag addition
sub do_tag_append($ $ $ $ $) {
	my ($tag_ps,$last_id,$name,$value,$line) = @_;

	$tag_ps->execute("$name=$value",$last_id)
		or warn("Invalid line '$line' : ".$conn->errstr);
}

########################################################################

sub display_count($$) {
    my ($type, $count) = @_;
    if ( ( $VERBOSE || $DEBUG ) &&
	 ( time()-$PARSING_DISPLAY_TIME >2)
	 )  {
	$PARSING_DISPLAY_TIME= time();
	print "Done $count ${type}s ";
	print "$type:";
	my $max_id = estimated_max_id($type);
	#print STDERR mem_usage();
	print STDERR time_estimate($PARSING_START_TIME,$count,$max_id);
	print STDERR "lines\r";
	print STDERR "\n" if $DEBUG>4;
    }
}

sub check_bbox_valid(@) {
	my @bbox = @_;
	unless($bbox[0] < $bbox[2]) {
		die("1st lat ($bbox[0]) must be smaller than second ($bbox[2])");
	}
	unless($bbox[1] < $bbox[3]) {
		die("1st long ($bbox[1]) must be smaller than second ($bbox[3])");
	}
}
########################################################################

sub open_connection($$$$$) {
	my ($dbtype,$dbname,$dbhost,$dbuser,$dbpass) = @_;
	my $dsn;

	if($dbtype eq "pgsql") {
		$dsn = "dbi:Pg:dbname=$dbname";
		$dsn .= ";host=$dbhost" if $dbhost;
	} elsif ($dbtype eq 'mysql') {
		$dsn = "DBI:mysql:$dbname";
		$dsn.= ";host=$dbhost" if $dbhost;
	} else {
		die("Unknown database type '$dbtype'");
	}

	my $conn = DBI->connect( $dsn, $dbuser, $dbpass, 
				{ PrintError => 0, RaiseError => 1, AutoCommit => 1 } );
	if ($dbtype eq 'pgsql') {
		$conn->do('SET client_encoding = LATIN1');
		#$conn->do('SET client_encoding = UTF8');
	}
	return $conn;
}

########################################################################

sub build_node_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO nodes (id,latitude,longitude) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_node_tag_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO node_tags (node,name,value) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_node_tag_ps_mysql($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "UPDATE nodes SET tags=CONCAT_WS(';',tags,?) WHERE id=?";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO segments (id,node_a,node_b) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_tag_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO segment_tags (segment,name,value) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_tag_ps_mysql($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "UPDATE segments SET tags=CONCAT_WS(';',tags,?) WHERE id=?";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO ways (id) VALUES (?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_seg_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql;
	if ($dbtype eq 'pgsql') { $sql = "INSERT INTO way_segments (way,segment,seg_order) VALUES (?,?,?)"; }
	else					{ $sql = "INSERT INTO way_segments (id,segment_id,sequence_id) VALUES (?,?,?)"; }
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_tag_ps($$) {
	my ($dbtype,$conn) = @_;
	my $sql;
	if ($dbtype eq 'pgsql') { $sql = "INSERT INTO way_tags (way,name,value) VALUES (?,?,?)"; }
	else					{ $sql = "INSERT INTO way_tags (id,k,v) VALUES (?,?,?)"; }
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}

########################################################################

sub should_batch_inserts($$) {
	my ($dbtype,$conn) = @_;
	if($dbtype eq 'pgsql') {
		# Postgres likes to get bulk inserts in batches
		return 1;
	}
	return 0;
}

########################################################################

sub fetch_schema($) {
	my ($dbtype) = @_;
	if($dbtype eq "pgsql") {
		# Note - indexes created in post process
		return <<"EOT";
CREATE SEQUENCE s_nodes;
CREATE SEQUENCE s_segments;
CREATE SEQUENCE s_ways;

CREATE TABLE nodes (
	id INTEGER NOT NULL DEFAULT NEXTVAL('s_nodes'),
	latitude REAL NOT NULL,
	longitude REAL NOT NULL,
	CONSTRAINT pk_nodes_id PRIMARY KEY (id)
);

CREATE TABLE node_tags (
	node INTEGER NOT NULL,
	name VARCHAR(255) NOT NULL,
	value VARCHAR(255) NOT NULL,
	CONSTRAINT pk_node_tags PRIMARY KEY (node,name),
	CONSTRAINT fk_node FOREIGN KEY (node) REFERENCES nodes (id)
);

CREATE TABLE segments (
	id INTEGER NOT NULL DEFAULT NEXTVAL('s_segments'),
	node_a INTEGER NOT NULL,
	node_b INTEGER NOT NULL,
	CONSTRAINT pk_segments_id PRIMARY KEY (id),
	CONSTRAINT fk_segments_a FOREIGN KEY (node_a) REFERENCES nodes (id),
	CONSTRAINT fk_segments_b FOREIGN KEY (node_b) REFERENCES nodes (id)
);

CREATE TABLE segment_tags (
	segment INTEGER NOT NULL,
	name VARCHAR(255) NOT NULL,
	value VARCHAR(255) NOT NULL,
	CONSTRAINT pk_segment_tags PRIMARY KEY (segment,name),
	CONSTRAINT fk_segment FOREIGN KEY (segment) REFERENCES segments (id)
);

CREATE TABLE ways (
	id INTEGER NOT NULL DEFAULT NEXTVAL('s_ways'),
	CONSTRAINT pk_ways_id PRIMARY KEY (id)
);

CREATE TABLE way_tags (
	way INTEGER NOT NULL,
	name VARCHAR(255) NOT NULL,
	value VARCHAR(255) NOT NULL,
	CONSTRAINT pk_way_tags PRIMARY KEY (way,name),
	CONSTRAINT fk_way FOREIGN KEY (way) REFERENCES ways (id)
);

CREATE TABLE way_segments (
	way INTEGER NOT NULL,
	segment INTEGER NOT NULL,
	seg_order INTEGER NOT NULL,
	CONSTRAINT pk_way_segments PRIMARY KEY (way,seg_order),
	CONSTRAINT fk_ws_way FOREIGN KEY (way) REFERENCES ways (id),
	CONSTRAINT fk_ws_seg FOREIGN KEY (segment) REFERENCES segments (id)
);
EOT
	} elsif ($dbtype eq 'mysql') {
		return "Please refer to http://trac.openstreetmap.org/browser/sql/mysql-schema.sql\n";
	} else {
		die("Unknown database type '$dbtype'");
	}
}

sub empty_tables($) {
	my ($dbtype)=@_;
	if ($dbtype eq 'pgsql') {
		# Will only work on 8.1, do deletes on earlier versions
		return <<EOT;
TRUNCATE TABLE way_tags, way_segments, ways, segment_tags, segments, node_tags, nodes
EOT
	} else {
		return <<EOT;
TRUNCATE TABLE nodes
TRUNCATE TABLE segments
TRUNCATE TABLE ways
TRUNCATE TABLE way_tags
TRUNCATE TABLE way_segments
TRUNCATE TABLE current_nodes
TRUNCATE TABLE current_segments
TRUNCATE TABLE current_ways
TRUNCATE TABLE current_way_tags
TRUNCATE TABLE current_way_segments
TRUNCATE TABLE meta_nodes
TRUNCATE TABLE meta_segments
TRUNCATE TABLE meta_ways
EOT
	}
}

sub post_process($) {
	my ($dbtype)=@_;
	if ($dbtype eq 'pgsql') {
		# Enable the indexes, which we turn off during a bulk load
		return <<EOT;
CREATE INDEX i_nodes_lat ON nodes(latitude);
CREATE INDEX i_nodes_long ON nodes(longitude);

CREATE INDEX i_node_tags_node ON node_tags(node);
CREATE INDEX i_node_tags_name ON node_tags(name);
CREATE INDEX i_node_tags_value ON node_tags(value);

CREATE INDEX i_segments_node_a ON segments(node_a);
CREATE INDEX i_segments_node_b ON segments(node_b);

CREATE INDEX i_segment_tags_segment ON segment_tags(segment);
CREATE INDEX i_segment_tags_name ON segment_tags(name);
CREATE INDEX i_segment_tags_value ON segment_tags(value);

CREATE INDEX i_way_tags_way ON way_tags(way);
CREATE INDEX i_way_tags_name ON way_tags(name);
CREATE INDEX i_way_tags_value ON way_tags(value);

CREATE INDEX i_way_segments_way ON way_segments(way);
CREATE INDEX i_way_segments_segment ON way_segments(segment);
EOT
	} else {
		return <<EOT;
UPDATE nodes SET tags=TRIM(';' FROM tags),visible=1
UPDATE segments SET tags=TRIM(';' FROM tags),visible=1
INSERT INTO current_ways (id,visible) SELECT id,1 FROM ways
INSERT INTO current_way_tags (id,k,v) SELECT id,k,v FROM way_tags
INSERT INTO current_way_segments (id,segment_id,sequence_id) SELECT id,segment_id,sequence_id FROM way_segments
INSERT INTO current_segments (id,node_a,node_b,tags,visible) SELECT id,node_a,node_b,tags,1 FROM segments
INSERT INTO current_nodes (id,latitude,longitude,tags,visible) SELECT id,latitude,longitude,tags,1 FROM nodes
INSERT INTO meta_nodes (id) SELECT id FROM nodes
INSERT INTO meta_segments (id) SELECT id FROM segments
INSERT INTO meta_ways (id) SELECT id FROM ways
EOT
	}
}



##################################################################
# Usage/manual

__END__

=head1 NAME

B<planertosm-to-db.pl>

=head1 DESCRIPTION

=head1 SYNOPSIS

B<Common usages:>


B<planertosm-to-db.pl> <planet.osm.xml> - parse planet.osm file and upload to db

=head1 OPTIONS

=over 2

=item B<--schema> - output database schema

=item B<--empty> - empty tables ready for new upload

=item B<--bbox>

planetosm-to-db.pl -bbox 10,-3.5,11,-3 <planet.osm.xml>
	Only add things inside the bounding box 
     (min lat, min long, max lat, max long)

=item B<--exbox>

planetosm-to-db.pl -exbbox 20,-180,80,-45 <planet.osm.xml>

Add everything except those inside the bounding box
min lat, min long, max lat, max long)


Use "20,-180,80,-45" to exclude the Tiger data


=item B<--dbtype>

mysql | pgsql

 ENV DBTYPE to  mysql | pgsql
       
 default: pgsql

=item B<--dbname>

 ENV DBNAME
 default: osm

=item B<--dbhost>

 ENV DBHOST
 default: localhost

=item B<--dbuser>

 ENV DBUSER
 default: ""

=item B<--dbpass>

 ENV DBPASS
 default: ""

=back

Before running, think about your database type
(pgsql or mysql), name, host, user and password and 
provide them at commandline or set the Environment Variables.

