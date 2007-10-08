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

###########################################################################
#                BEGIN USER CONFIGURATION BLOCK                           #
###########################################################################

# With these, give a tag name, and optionally a tag value
# If you only want to match on name, not value, put in undef for the value

# We will get all Nodes required by Ways and Relations
# We can optionally also get other Nodes, based on their tags
my @node_sel_tags = (
	['place','town'], 
    ['place','city'],
	['railway',undef],
);

# We will get all Ways required by Relations
# We can optionally also get other Ways, based on their tags
my @way_sel_tags = (
	['railway',undef],
	['highway','motorway'],
#	['waterway','river'],
#	['natural','coastline'], # Gives really huge .osm files
);

# Specify which Relations to get, based on their tags
my @rel_sel_tags = (
#	['type','multipolygon'],
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


# We assume IDs to be up to 250 million
my $wanted_nodes = Bit::Vector->new( 250 * 1000 * 1000 );
my $wanted_ways  = Bit::Vector->new( 250 * 1000 * 1000 );


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
	my ($nodeH, $wayH, $relH) = @_;
	openXML();
	$pass++;

	# Process the file, giving tags to the helpers that like them

	# Hold the main line, tags and segs of the tag
	my $main_line;
	my $main_type;
	my $wanted;
	my @tags;
	my @nodes;
	my @rel_ways;
	my @rel_nodes;

	my $startNewTag = sub{
		$wanted = 0;
		@tags = ();
		@nodes = ();
		@rel_ways = ();
		@rel_nodes = ();
	};

	while(my $line = <XML>) {
		if($line =~ /^\s*<node/) {
			$main_line = $line;
			$main_type = "node";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*\<way/) {
			$main_line = $line;
			$main_type = "way";
			&$startNewTag();
			unless($line =~ /\/>\s*$/) { next; }
		}
		elsif($line =~ /^\s*<relation/) {
			$main_line = $line;
			$main_type = "relation";
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
		elsif($line =~ /^\s*\<nd /) {
			my ($ref) = ($line =~ /^\s*\<nd ref=[\'\"](\d+)[\'\"]/);
			unless($main_type eq "way") { warn "Got nd when in $main_type\n"; next; }
			unless($ref) { warn "Invalid line '$line'"; next; }
			push @nodes, $ref;
		}
		elsif($line =~ /^\s*\<member /) {
			my ($type,$ref,$role) = ($line =~ /^\s*\<member type=[\'\"](.*?)[\'\"] ref=[\'\"](\d+)[\'\"] role=[\'\"](.*)[\'\"]/);
			unless($main_type eq "relation") { warn "Got member when in $main_type\n"; next; }
			unless($type && $ref) { warn "Invalid line '$line'"; next; }

			my %m;
			$m{'type'} = $type;
			$m{'ref'} = $ref;
			$m{'role'} = $role;
			if($type eq "node") {
				push @rel_nodes, \%m;
			} elsif($type eq "way") {
				push @rel_ways, \%m;
			} else {
				warn("Got unknown member type '$type' in '$line'"); next;
			}
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
		elsif($line =~ /^\s*\<\/?way/) {
			my ($id) = ($main_line =~ /^\s*\<way id=[\'\"](\d+)[\'\"]/);

			unless($id) { warn "Invalid way line '$main_line'"; next; }
			unless($main_type eq "way") { warn "$main_type ended with $line"; next; }
			if($wayH) {
				&$wayH($id,\@tags,\@nodes,$main_line,$line);
			}
		}
		elsif($line =~ /^\s*<\/?relation/) {
			my ($id) = ($main_line =~ /^\s*\<relation id=[\'\"](\d+)[\'\"]/);

			unless($id) { warn "Invalid relation line '$main_line'"; next; }
			unless($main_type eq "relation") { warn "$main_type ended with $line"; next; }
			if($relH) {
				&$relH($id,\@tags,\@rel_nodes,\@rel_ways,$main_line,$line);
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


# First up, call for relations
my $relTagHelper = &buildTagMatcher(@rel_sel_tags);
processXML(undef, undef, sub {
	my ($id,$tagsRef,$relNodesRef,$relWaysRef,$main_line,$line) = @_;

	# Test the tags, to see if we want this
	if(&$relTagHelper(@$tagsRef)) {
		# Bingo, matched
		# Record the ways and nodes we want to get
		foreach my $wref (@$relWaysRef) {
			$wanted_ways->Bit_On($wref->{'ref'});
		}
		foreach my $nref (@$relNodesRef) {
			$wanted_nodes->Bit_On($nref->{'ref'});
		}

		# Output
		print $main_line;
		foreach my $wref (@$relWaysRef) {
			print "    <member type=\"$wref->{'type'}\" ref=\"$wref->{'ref'}\" role=\"$wref->{'role'}\" />\n";
		}
		foreach my $nref (@$relNodesRef) {
			print "    <member type=\"$nref->{'type'}\" ref=\"$nref->{'ref'}\" role=\"$nref->{'role'}\" />\n";
		}
		&printTags(@$tagsRef);
		print $line;
	} else {
		# Not wanted, skip
	}
});

# Now for ways
my $wayTagHelper = &buildTagMatcher(@way_sel_tags);
processXML(undef, sub {
	my ($id,$tagsRef,$nodesRef,$main_line,$line) = @_;
	my $wanted = 0;

	# Test the tags, to see if we want this
	if(&$wayTagHelper(@$tagsRef)) {
		# Bingo, matched
		$wanted = 1;
	} else {
		# Does a relation want it?
		if($wanted_ways->contains($id)) {
			# A relation wants it
			$wanted = 1;
		}
	}

	if($wanted) {
		# Record the nodes we want to get
		foreach my $node (@$nodesRef) {
			$wanted_nodes->Bit_On($node);
		}

		# Output
		print $main_line;
		foreach my $node (@$nodesRef) {
			print "    <nd ref=\"$node\" />\n";
		}
		&printTags(@$tagsRef);
		print $line;
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
		# Does a relation or way want it?
		if($wanted_nodes->contains($id)) {
			# Something wants it
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
