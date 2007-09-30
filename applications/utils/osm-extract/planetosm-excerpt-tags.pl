#!/usr/bin/perl
# Takes a planet.osm, and extracts just the bits that have certain tags
#
# Requires several passes over the file, so can't work with a stream from
#  STDIN. Normally run on an area excerpt, or data downloaded from the API
#
# For now, all configuration is done in the code. In future, we'll want to
#  split this out into a rules file
#
# Nick Burch
#     v0.01   01/11/2006

use strict;
use warnings;

print STDERR <<EOF;
Note that this script is not (yet?) 0.5 compatible. For bounding box excerpts
with 0.5 style planet files, use the -b option with the polygon extract
script!

EOF

###########################################################################
#                BEGIN USER CONFIGURATION BLOCK                           #
###########################################################################

# With these, give a tag name, and optionally a tag value
# If you only want to match on name, not value, put in undef for the value

# We will get all Nodes required by Segments (and Ways)
# We can optionally also get other Nodes, based on their tags
my @node_sel_tags = (
	['place','town'], 
    ['place','city'],
	['railway',undef],
);

# We will get all Segments required by Ways
# We can optionally also get other Segments, based on their tags
my @seg_sel_tags = ();

# Specify which ways to get, based on their tags
my @way_sel_tags = (
	['railway',undef],
	['highway','motorway'],
#	['waterway','river'],
#	['natural','coastline'], # Gives really huge .osm files
);

###########################################################################
#               END OF USER CONFIGURATION BLOCK                           #
###########################################################################



BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl_lib");

    unshift(@INC,"./perl_lib");
    unshift(@INC,"../perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/utils/perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl_lib");
}

use Getopt::Long;

use Geo::OSM::Planet;
use Pod::Usage;

# We need Bit::Vector, as perl hashes can't handle the sort of data we need
use Bit::Vector;

our $man=0;
our $help=0;
my $bbox_opts='';

my $VERBOSE;

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'verbose+'         => \$VERBOSE,
	     'v+'               => \$VERBOSE,
	     'MAN'              => \$man, 
	     'man'              => \$man, 
	     'h|help|x'         => \$help, 
	     ) or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;


# Grab the filename
my $xml = shift||'';
pod2usage(1) unless $xml;

# Check we can load the file
if($xml eq "-") {
	die("Sorry, reading from stdin is not supported, as we have to make several passes\n");
}
unless( -f $xml) {
	die("Planet.osm file '$xml' could not be found\n");
}

unless( -s $xml ) {
    die " $xml has 0 size\n";
}


# We assume IDs to be up to 50 million
my $wanted_nodes = Bit::Vector->new( 250 * 1000 * 1000 );
my $wanted_segs = Bit::Vector->new( 250 * 1000 * 1000 );


# Sub to open xml
sub openXML {
	open(XML, "<$xml") or die("$!");
	#open(XML, "<:utf8","$xml") or die("$!");
}
# Sub to close xml
sub closeXML {
	close XML;
}

# Sub to build sub to do tag matching
sub buildTagMatcher {
	my @rules = @_;
	return sub {
		my @tagsToTest = @_;
		foreach my $tagToTest (@tagsToTest) {
			my ($name,$value) = @$tagToTest;
			foreach my $r (@rules) {
				my ($rname,$rvalue) = @$r;
				if($rvalue) {
					# Check the rule name+value with the supplied name+value
					if($rname eq $name && $rvalue eq $value) { return 1; }
				} else {
					# Check the rule name with the supplied name
					if($rname eq $name) { return 1; }
				}
			}
		}
		# No match on any of the tags
		return 0;
	};
}

# To print out a series of tags as xml
sub printTags {
	my @tags = @_;
	foreach my $tagSet (@tags) {
		print "    <tag k=\"$tagSet->[0]\" v=\"$tagSet->[1]\" />\n";
	}
}


# Sub to process the file, against a bunch of helper subroutines
my $pass = 0;
sub processXML {
	my ($nodeH, $segH, $wayH) = @_;
	openXML();
	$pass++;

	# Process the file, giving tags to the helpers that like them

	# Hold the main line, tags and segs of the tag
	my $main_line;
	my $main_type;
	my $wanted;
	my @tags;
	my @segs;

	my $startNewTag = sub{
		$wanted = 0;
		@tags = ();
		@segs = ();
	};

	while(my $line = <XML>) {
		if($line =~ /^\s*<node/) {
			$main_line = $line;
			$main_type = "node";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*<segment/) {
			$main_line = $line;
			$main_type = "segment";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*\<way/) {
			$main_line = $line;
			$main_type = "way";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}

		if($line =~ /^\s*\<tag/) {
			my ($name,$value) = ($line =~ /^\s*\<tag k=[\'\"](.*?)[\'\"] v=[\'\"](.*?)[\'\"]/);
			unless($name) { 
				unless($line =~ /k="\s*" v="\s*"/) {
					warn "Invalid line '$line'"; 
				}
				next; 
			}
			my @tag = ($name,$value);
			push @tags, \@tag;
		}
		elsif($line =~ /^\s*\<seg /) {
			my ($id) = ($line =~ /^\s*\<seg id=[\'\"](\d+)[\'\"]/);
			unless($main_type eq "way") { warn "Got seg when in $main_type\n"; next; }
			unless($id) { warn "Invalid line '$line'"; next; }
			push @segs, $id;
		}

		# Do the decisions when closing tags - can be self closing
		elsif($line =~ /^\s*<\/?node/) {
			my ($id,$lat,$long) = ($main_line =~ /^\s*<node id=['"](\d+)['"] lat=['"]?(\-?[\d\.]+)['"]? lon=['"]?(\-?[\d\.]+e?\-?\d*)['"]?/);

			unless($id) { warn "Invalid node line '$main_line'"; next; }
			unless($main_type eq "node") { warn "$main_type ended with $line"; next; }
			if($nodeH) {
				&$nodeH($id,$lat,$long,\@tags,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*<\/?segment/) {
			my ($id,$from,$to) = ($main_line =~ /^\s*<segment id=['"](\d+)['"] from=['"](\d+)['"] to=['"](\d+)['"]/);

			unless($id) { warn "Invalid segment line '$main_line'"; next; }
			unless($main_type eq "segment") { warn "$main_type ended with $line"; next; }
			if($segH) {
				&$segH($id,$from,$to,\@tags,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*\<\/?way/) {
			my ($id) = ($main_line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);

			unless($id) { warn "Invalid way line '$main_line'"; next; }
			unless($main_type eq "way") { warn "$main_type ended with $line"; next; }
			if($wayH) {
				&$wayH($id,\@tags,\@segs,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*\<\?xml/) {
			if($pass == 1) {
				print $line;
			}
		}
		elsif($line =~ /^\s*\<osm /) {
			if($pass == 1) {
				print $line;
			}
		}
		elsif($line =~ /^\s*\<\/osm\>/ ) {
			if($pass == 3) {
				print $line;
			}
		}
		else {
			print STDERR "Unknown line $line\n";
		};
	}

	# All done
	closeXML();
}


# First up, call for ways
my $wayTagHelper = &buildTagMatcher(@way_sel_tags);
processXML(undef,undef, sub {
	my ($id,$tagsRef,$segsRef,$main_line,$line) = @_;

	# Test the tags, to see if we want this
	if(&$wayTagHelper(@$tagsRef)) {
		# Bingo, matched
		# Record the segments we want to get
		foreach my $seg (@$segsRef) {
			$wanted_segs->Bit_On($seg);
		}

		# Output
		print $main_line;
		foreach my $seg (@$segsRef) {
			print "    <seg id=\"$seg\" />\n";
		}
		&printTags(@$tagsRef);
		print $line;
	} else {
		# Not wanted, skip
	}
});

# Now for segments
my $segTagHelper = &buildTagMatcher(@seg_sel_tags);
processXML(undef, sub {
	my ($id,$from,$to,$tagsRef,$main_line,$line) = @_;
	my $wanted = 0;

	# Test the tags, to see if we want this
	if(&$segTagHelper(@$tagsRef)) {
		# Bingo, matched
		$wanted = 1;
	} else {
		# Does a way want it?
		if($wanted_segs->contains($id)) {
			# A way wants it
			$wanted = 1;
		}
	}

	if($wanted) {
		# Record the nodes we want to get
		$wanted_nodes->Bit_On($from);
		$wanted_nodes->Bit_On($to);

		# Output
		print $main_line;
		unless($main_line =~ /\/>/) {
			&printTags(@$tagsRef);
			print $line;
		}
	} else {
		# Not wanted, skip
	}
}, undef);

# Now for nodes
my $nodeTagHelper = &buildTagMatcher(@node_sel_tags);
processXML(sub {
	my ($id,$lat,$long,$tagsRef,$main_line,$line) = @_;
	my $wanted = 0;

	# Test the tags, to see if we want this
	if(&$nodeTagHelper(@$tagsRef)) {
		# Bingo, matched
		$wanted = 1;
	} else {
		# Does a segment want it?
		if($wanted_nodes->contains($id)) {
			# A segment wants it
			$wanted = 1;
		}
	}

	if($wanted) {
		# Output
		print $main_line;
		unless($main_line =~ /\/>/) {
			&printTags(@$tagsRef);
			print $line;
		}
	} else {
		# Not wanted, skip
	}
}, undef, undef);


# All done

##################################################################
# Usage/manual

__END__

=head1 NAME

B<planetosm-excerpt-tags.pl>

=head1 DESCRIPTION

=head1 SYNOPSIS

B<Common usages:>


B<planertosm-excerpt-tags.pl> <planet.osm.xml> > excerpt.osm

parse an excerpted planet.osm file, and output the parts that match certain
tags.

=head1 AUTHOR

=head1 COPYRIGHT


=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
