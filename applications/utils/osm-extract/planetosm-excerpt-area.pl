#e!lusr/bin/perl
# Takes a planet.osm, and extracts just the bits that relate to one area
#
# Nick Burch
#     v0.01   31/10/2006

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl");

    unshift(@INC,"./perl_lib");
    unshift(@INC,"../../perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/applications/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/applications/utils/perl_lib");
}

use strict;
use warnings;

use Getopt::Long;

use Geo::OSM::Planet;
use Pod::Usage;

# We need Bit::Vector, as perl hashes can't handle the sort of data we need
use Bit::Vector;

our $man=0;
our $help=0;
my $bbox_opts='';

my $VERBOSE;

print STDERR <<EOF;
Note that this script is not (yet?) 0.5 compatible. For bounding box excerpts
with 0.5 style planet files, use the -b option with the polygon extract
script!

EOF

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'verbose+'         => \$VERBOSE,
	     'v+'               => \$VERBOSE,
	     'MAN'              => \$man, 
	     'man'              => \$man, 
	     'h|help|x'         => \$help, 

	     'bbox=s'		=> \$bbox_opts,
	     ) or pod2usage(1);

pod2usage(1) if $help;
pod2usage(1) unless $bbox_opts;
pod2usage(-verbose=>2) if $man;


# Grab the filename
my $xml = shift||'';
if (!$xml)
{
    print STDERR "Input file name must be given on command line - reading \n";
    print STDERR "from stdin not supported.\n";
    pod2usage(1);
}

# Should we warn for things we skip due to the bbox?
my $warn_bbox_skip = 0;

# Exclude nodes not in this lat,long,lat,long bounding box
my @bbox = ();
warn "Only outputting things within $bbox_opts\n";
@bbox = split(/,/, $bbox_opts);

# Check that things are in the right order
check_bbox_valid(@bbox);


# Check we can load the file
unless( -f $xml || $xml eq "-" ) {
	die("Planet.osm file '$xml' could not be found\n");
}

if ( $xml ne "-" && ! -s $xml ) {
    die " $xml has 0 size\n";
}


# Counts of the numbers handled
my $node_count = 0;
my $seg_count = 0;
my $way_count = 0;
my $line_count = 0;

# We assume IDs to be up to 50 million
my $nodes = Bit::Vector->new( 250 * 1000 * 1000 );
my $segs = Bit::Vector->new( 250 * 1000 * 1000 );

# Process
open(XML, "<$xml") or die("$!");
#open(XML, "<:utf8","$xml") or die("$!");

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

	#&display_count("line",$line_count);

	# Process the line of XML
	if($line =~ /^\s*<node/) {
		my ($id,$lat,$long) = ($line =~ /^\s*<node[^>]+id=['"](\d+)['"][^>]+lat=['"]?(\-?[\d\.]+)['"]?[^>]+lon=['"]?(\-?[\d\.]+e?\-?\d*)['"]?/);
		$last_id = undef; # In case it has tags we need to exclude
		$last_type = "node";

		unless($id) { warn "Invalid line '$line'"; next; }

		# Do we need to exclude this node?
		if(@bbox) {
			if($lat > $bbox[0] && $lat < $bbox[2] &&
				$long > $bbox[1] && $long < $bbox[3]) {
				# This one's inside the bbox
			} else {
				if($warn_bbox_skip) {
					warn("Skipping node at $lat $long as not in bbox\n");
				}
				next;
			}
		}

		# Output the node
		print $line;

		$nodes->Bit_On($id);
		$last_id = $id;

		$node_count++;
		#&display_count("node", $node_count);
	}
	elsif($line =~ /^\s*<segment/) {
		my ($id,$from,$to) = ($line =~ /^\s*<segment[^>]+id=['"](\d+)['"][^>]+from=['"](\d+)['"][^>]+to=['"](\d+)['"]/);
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

		# Output
		print $line;

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
		#&display_count("way", $way_count);

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

		# Output way
		print $way_line;

		# Output way segments
		my $way_seg_count = 0;
		foreach my $ws (@way_segs) {
			print $ws->{line};
		}
		# Add way tags
		foreach my $wt (@way_tags) {
			print $wt->{line};
		}

		# Finish way
		print $line;
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
		unless($name) { 
			unless($line =~ /k="" v=""/) {
				warn "Invalid line '$line'"; 
			}
			next; 
		}
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
			print $line;
		} elsif($last_type eq "segment") {
			print $line;
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
		print $line;
	}
	elsif($line =~ /^\s*\<osm / || $line =~ /^\s*\<\/osm\>/ ) {
		print $line;
	}
	elsif($line =~ /^\s*\<\/node\>/) {
		if($last_id) {
			print $line;
		}
	}
	elsif($line =~ /^\s*\<\/segment\>/) {
		if($last_id) {
			print $line;
		}
	}
	else {
	    print STDERR "Unknown line $line\n";
	};
}


########################################################################


sub check_bbox_valid {
	my @bbox = @_;
	unless($bbox[0] < $bbox[2]) {
		die("1st lat ($bbox[0]) must be smaller than second ($bbox[2])");
	}
	unless($bbox[1] < $bbox[3]) {
		die("1st long ($bbox[1]) must be smaller than second ($bbox[3])");
	}
}


##################################################################
# Usage/manual

__END__

=head1 NAME

B<planetosm-excerpt-area.pl>

=head1 DESCRIPTION

=head1 SYNOPSIS

B<Common usages:>


B<planetosm-excerpt-area.pl> -bbox 54,-1.5,55,-1.4 planet.osm.xml > excerpt.osm

parse planet.osm file, and output the parts between the bbox

=head1 OPTIONS

=over 2

=item B<--bbox>

planetosm-excerpt-area.pl -bbox 10,-3.5,11,-3 planet.osm.xml
	Only output things inside the bounding box 
     (min lat, min long, max lat, max long)

=back

=head1 COPYRIGHT

=head1 AUTHOR

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
