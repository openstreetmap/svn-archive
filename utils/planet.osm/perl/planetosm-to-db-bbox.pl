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
#             13/08/2006 corrected the regexp checks for new planet format (NW)
#             23/08/2006 Allow user to specify bounding box  (Nick W)

use strict;
use DBI;

my $dbtype = "mysql";	# mysql | pgsql
my $dbname = "nick";
my $dbhost = "localhost";
my $dbuser = "nick";
my $dbpass = "password";

my @bbox;
my $wayseg; 
my $i;

my $lastlat;
my $lastlong;

my $xml = shift;
unless($xml) {
	die <<EOT

planetosm-to-db.pl <planet.osm.xml>\t - parse planet.osm file and upload to db
planetosm-to-db.pl -schema\t\t - output database schema
planetosm-to-db.pl -empty\t\t - empty tables ready for new upload

Before running, configure the script to include your database type
(pgsql or mysql), name, host, user and password.

EOT
}
if($xml eq "-schema") {
	print &fetch_schema($dbtype);
	exit;
}

# Open our database connection
my $conn = &open_connection($dbtype,$dbname,$dbhost,$dbuser,$dbpass);

# Empty out the database if requested
if ($xml eq "-empty") {
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

#NW 230806 specify bounding box
my $bboxstr;
if ($xml eq "-bbox") {
	if (!($bboxstr = shift)) { die("You need to specify a bounding box!"); }
	@bbox = split(",",$bboxstr);
	if (!($xml = shift)) { die("Required XML file not given"); }
}

# Get our prepared statements
my $node_ps = &build_node_ps($dbtype,$conn);
my $node_tag_ps = &build_node_tag_ps($dbtype,$conn);
my $node_tag_ps_mysql = &build_node_tag_ps_mysql($dbtype,$conn);
my $seg_ps = &build_seg_ps($dbtype,$conn);
my $seg_tag_ps = &build_seg_tag_ps($dbtype,$conn);
my $seg_tag_ps_mysql = &build_seg_tag_ps_mysql($dbtype,$conn);
my $way_ps = &build_way_ps($dbtype,$conn);
my $way_seg_ps = &build_way_seg_ps($dbtype,$conn);
my $way_tag_ps = &build_way_tag_ps($dbtype,$conn);

# Should we batch inserts in transactions to help performance?
# (DB specific if this helps or hinders)
my $batch_inserts = &should_batch_inserts($dbtype,$conn);

# Counts of the numbers handled
my $node_count = 0;
my $seg_count = 0;
my $way_count = 0;
my $way_seg_count = 0;
my $line_count = 0;

my %nodes;
my %segs;

my @waysegs;
my @waytags;

# Turn on batching, if the database likes that
if($batch_inserts) {
	$conn->{AutoCommit} = 0;
}

# Process
open(XML, "<$xml");
#open(XML, "<:utf8", $xml);

my $last_id;
my $last_type;
while(my $line = <XML>) {
	$line_count++;

	# Handle batches, if the DB wants them
	if($line_count % 10000 == 0) {
		if($batch_inserts) {
			$conn->commit();
		}
	}

	# Process the line of XML
	if($line =~ /^\s*<node/) {
		$last_type = ""; #NW 230806 to cover ignored elements
		my ($id,$lat,$long) = ($line =~ /^\s*<node id="(\d+)" lat="?(\-?[\d\.]+)"? lon="?(\-?[\d\.]+e?\-?\d*)".*$/);
		unless($id) { 
			#warn "Invalid line '$line'"; 
			next; 
		}

		$lastlat = $lat;
		$lastlong = $long;

		# NW 230806 if we've specified a bounding box, only add the node if
		# we're within the bounding box
		if((!@bbox) || ($long>=$bbox[0] && $long<=$bbox[2] &&
					$lat>=$bbox[1] && $lat<=$bbox[3]))
		{
			print "Accepting node $id (lat $lat, long $long)\n";
			$nodes{$id} = $id;
			$last_id = $id;
			$last_type = "node";

			$node_ps->execute($id,$lat,$long) ;
				#or warn("Invalid line '$line' : ".$conn->errstr);

			$node_count++;
			&display_count("node", $node_count);
		}

	}
	elsif($line =~ /^\s*<segment/) {
		$last_type = ""; #NW 230806 to cover ignored elements
		my ($id,$from,$to) = ($line =~ /\s*<segment id="(\d+)" from="(\d+)" to="(\d+)"/);
		$last_id = undef; # In case it has tags

		#unless($id) { warn "Invalid line '$line'"; next; }
		#unless($nodes{$to}) { warn "No node $to for line '$line'"; next; }
		#unless($nodes{$from}) { warn "No node $from for line '$line'"; next; }

		#NW 230806 only do if the nodes exist
		if ($id && $nodes{$from} && $nodes{$to})
		{
			$seg_ps->execute($id,$from,$to);
				#or warn("Invalid line '$line' : ".$conn->errstr);

			print "Accepting segment $id (from $from, to $to)\n";
			$segs{$id} = $id;
			$last_id = $id;
			$last_type = "segment";

			$seg_count++;
			&display_count("segment", $seg_count);
		}
	}
	elsif($line =~ /^\s*<way/) {

		$last_type = ""; #NW 230806 to cover ignored elements
		# NW 230806 This assumes that the XML will be in order 
		# nodes-segments-ways. Blank the nodes to save memory - we don't need
		# them anymore
		%nodes = {};
		
		my ($id) = ($line =~ /^\s*<way id="(\d+)"/);
		if($id)
		{

		# NW 240806 do this at the end
		#$way_ps->execute($id)
		#	or warn("Invalid line '$line' : ".$conn->errstr);

			$last_id = $id;
			$last_type = "way";

			$way_count++;
			$way_seg_count = 0;
			&display_count("way", $way_count);
		}

		#NW 230806 blank waysegs and waytags
		@waysegs = ();
		@waytags = ();
	}
	# NW 230806 end way tag
	# Only do the way SQL if we found way segments
	elsif ($line =~ /way>/) {
		if(scalar(@waysegs)>0)
		{
			print "Accepting way $last_id\n";
			
			$way_ps->execute($last_id)
				or warn("Invalid way '$last_id' : ".$conn->errstr);

			foreach $wayseg (@waysegs)
			{
				print "Doing wayseg SQL: Way $last_id, wayseg $wayseg, way_seg_count $way_seg_count\n";
				$way_seg_ps->execute($last_id,$wayseg,$way_seg_count)
					or warn("Invalid wayseg way $last_id, wayseg $wayseg : ".
							$conn->errstr);
				$way_seg_count++;
			}
			for($i=0; $i<scalar(@waytags); $i+=2)
			{
				print "Doing waytag SQL: Way $last_id, key ".
					$waytags[$i]." value ". ($waytags[$i+1]). "\n";

				$way_tag_ps->execute($last_id,$waytags[$i],$waytags[$i+1])
					or warn("Invalid waytag way $last_id, key $waytags[$i] ".
							"value ".($waytags[$i+1]). " " .$conn->errstr);
			}
		}
	}
	elsif($line =~ /^\s*<seg /) {
		my ($id) = ($line =~ /^\s*<seg id="(\d+)"/);
		#unless($id) { warn "Invalid line '$line'"; next; }
		#unless($segs{$id}) { warn "Invalid segment for line '$line'"; next; }
		
		#NW 230806 store the way segment for later 
		if ($id && $segs{$id}) {
			push(@waysegs, $id);
		}
	}
	elsif($line =~ /^\s*<tag/) {
		my ($name,$value) = ($line =~ /^\s*<tag k="(.*?)" v="(.*?)"/);
		#print "Tag: key=$name, value=$value, last_type=$last_type\n";

		unless($name) { 
			warn "Invalid line '$line'"; 
			next; 
		}
		if($name =~ /^\s+$/) { 
			warn "Skipping invalid tag line '$line'"; 
			next; 
		}

		# Decode the XML elements in the name and value
		$value =~ s/\&apos\;/'/g;
		
		# If last_id isn't there, the tag we're attached to was invalid
		unless($last_id) {
			warn("Invalid previous $last_type, ignoring its tag '$line'");
			next;
		}

		if($last_type eq "node") {
			#print "Adding tags to db\n";
			if ($dbtype eq 'pgsql') {
				$node_tag_ps->execute($last_id,$name,$value);
	#				or warn("Invalid line '$line' : ".$conn->errstr);
			} else {
				$node_tag_ps_mysql->execute("$name=$value",$last_id);
#					or warn("Invalid line '$line' : ".$conn->errstr);
			}
		} elsif($last_type eq "segment") {
			#print "Adding tags to db\n";
			if ($dbtype eq 'pgsql') {
				$seg_tag_ps->execute($last_id,$name,$value);
#					or warn("Invalid line '$line' : ".$conn->errstr);
			} else {
				$seg_tag_ps_mysql->execute("$name=$value",$last_id);
#					or warn("Invalid line '$line' : ".$conn->errstr);
			}
		} elsif($last_type eq "way") {
			#print "Adding $name, $value to waytags\n";
			# NW 230806  store the way tags for later
			# Change this to use an array of arrays - need to read up on this
			push (@waytags, $name);
			push (@waytags, $value);
		}
	}
}

# End the batch, if the database likes that
if($batch_inserts) {
	$conn->commit();
	$conn->{AutoCommit} = 1;
}

# Post-processing
foreach my $sql (split "\n",&post_process($dbtype)) {
	print "$sql\n";
	$conn->do($sql);
}


########################################################################

sub display_count {
	my ($type, $count) = @_;
	if($count % 10000 == 0) {
		print "Done $count ${type}s\n";
	}
}

########################################################################

sub open_connection {
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

	# NW 240806 DB errors don't cause script to die
	my $conn = DBI->connect( $dsn, $dbuser, $dbpass, 
				{ PrintError => 0, RaiseError => 0, AutoCommit => 1 } );
	if ($dbtype eq 'pgsql') {
		$conn->do('SET client_encoding = LATIN1');
		#$conn->do('SET client_encoding = UTF8');
	}
	return $conn;
}

########################################################################

sub build_node_ps {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO nodes (id,latitude,longitude) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_node_tag_ps {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO node_tags (node,name,value) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_node_tag_ps_mysql {
	my ($dbtype,$conn) = @_;
	my $sql = "UPDATE nodes SET tags=CONCAT_WS(';',tags,?) WHERE id=?";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_ps {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO segments (id,node_a,node_b) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_tag_ps {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO segment_tags (segment,name,value) VALUES (?,?,?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_seg_tag_ps_mysql {
	my ($dbtype,$conn) = @_;
	my $sql = "UPDATE segments SET tags=CONCAT_WS(';',tags,?) WHERE id=?";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_ps {
	my ($dbtype,$conn) = @_;
	my $sql = "INSERT INTO ways (id) VALUES (?)";
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_seg_ps {
	my ($dbtype,$conn) = @_;
	my $sql;
	if ($dbtype eq 'pgsql') { $sql = "INSERT INTO way_segments (way,segment,seg_order) VALUES (?,?,?)"; }
	else					{ $sql = "INSERT INTO way_segments (id,segment_id,sequence_id) VALUES (?,?,?)"; }
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}
sub build_way_tag_ps {
	my ($dbtype,$conn) = @_;
	my $sql;
	if ($dbtype eq 'pgsql') { $sql = "INSERT INTO way_tags (way,name,value) VALUES (?,?,?)"; }
	else					{ $sql = "INSERT INTO way_tags (id,k,v) VALUES (?,?,?)"; }
	my $sth = $conn->prepare($sql);
	unless($sth) { die("Couldn't create prepared statement: ".$conn->errstr); }
	return $sth;
}

########################################################################

sub should_batch_inserts {
	my ($dbtype,$conn) = @_;
	if($dbtype eq 'pgsql') {
		# Postgres likes to get bulk inserts in batches
		return 1;
	}
	return 0;
}

########################################################################

sub fetch_schema {
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

sub empty_tables {
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
EOT
	}
}

sub post_process {
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

CREATE INDEX i_way_tags_way ON way_tags(way);

CREATE INDEX i_way_segments_way ON way_segments(way);
CREATE INDEX i_way_segments_segment ON way_segments(segment);
EOT
	} else {
		return <<EOT;
UPDATE nodes SET tags=TRIM(';' FROM tags),visible=1
UPDATE segments SET tags=TRIM(';' FROM tags),visible=1
EOT
	}
}
