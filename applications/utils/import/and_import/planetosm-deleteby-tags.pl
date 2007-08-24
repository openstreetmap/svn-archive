#!/usr/bin/perl
# Takes a planet.osm, and creates an OSM that *deletes* just the bits that
# have certain tags
#
# Requires several passes over the file, so can't work with a stream from
#  STDIN. Normally run on an area excerpt, or data downloaded from the API
#
# For now, all configuration is done in the code. In future, we'll want to
#  split this out into a rules file
#
# Licence: GPL
#
# Martijn van Oosterhout
#     v0.01   18/08/2007
# Based on planetosm-excerpt-tags.pl
# Nick Burch
#     v0.01   01/11/2006

use strict;
use warnings;

###########################################################################
#                BEGIN USER CONFIGURATION BLOCK                           #
###########################################################################

# With these, give a tag name, and optionally a tag value
# If you only want to match on name, not value, put in undef for the value

# We will get all Nodes required by Segments (and Ways)
# We can optionally also get other Nodes, based on their tags
# This list is the tags that identify nodes to be deleted.

# They won't be deleted if they are needed by a way that isn't deleted, so
# you can list tags to be ignored here.
my @node_sel_tags = (
	['place',undef], 
	['railway','station'],
	
	# These are often added by accident, so we attempt to delete them here if possible
	['highway','motorway'],
	['highway','motorway_link'],
	['highway','trunk'],
	['highway','trunk_link'],
	['highway','primary'],
	['highway','primary_link'],
	['highway','secondary'],
	['highway','tertiary'],
	['highway','unclassified'],
	['highway','residential'],
	['highway','pedestrian'],
	['highway','service'],
	['highway','bridge'],
	['highway','mini_roundabout'],
	['railway',undef],
	['waterway',undef],
	['natural',undef],
);

# We will get all Segments required by Ways
# However, we don't care about segment tags here, this line is ignored.
my @seg_sel_tags = ();

# Specify which ways to get, based on their tags
# These tags are the one that identify ways to be deleted
my @way_sel_tags = (
	['railway','rail'],
	['railway','light_rail'],
#	['landuse',undef],
	['highway','motorway'],
	['highway','motorway_link'],
	['highway','trunk'],
	['highway','trunk_link'],
	['highway','primary'],
	['highway','primary_link'],
	['highway','secondary'],
	['highway','tertiary'],
	['highway','unclassified'],
	['highway','residential'],
	['highway','pedestrian'],
	['highway','service'],
	
	['waterway','river'],
	['waterway','canal'],
	['waterway','riverbank'],
	['natural','water'],
	['natural','coastline'], 
	
	# Silly stuff that shouldn't be there anyway
	['highway','bridge'],
	['highway','mini_roundabout'],
);

###########################################################################
#               END OF USER CONFIGURATION BLOCK                           #
###########################################################################



BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/perl_lib");

    unshift(@INC,"../../perl_lib");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/utils/perl_lib");
}

my %deleted;

use Getopt::Long;

use Geo::OSM::Planet;
use Pod::Usage;

# We need Bit::Vector, as perl hashes can't handle the sort of data we need
use Bit::Vector;

our $man=0;
our $help=0;
our $output = "josm";
our $remainsfile;
my $bbox_opts='';

my $VERBOSE;

Getopt::Long::Configure('no_ignore_case');
GetOptions ( 
	     'verbose+'         => \$VERBOSE,
	     'v+'               => \$VERBOSE,
	     'MAN'              => \$man, 
	     'man'              => \$man, 
	     'h|help|x'         => \$help, 
	     'o|output=s'       => \$output,
	     'r|remains=s'      => \$remainsfile,
	     ) or pod2usage(1);

pod2usage(1) if $help;
pod2usage(-verbose=>2) if $man;

if( $output !~ /^(josm|osmchange)$/ )
{
    die "Output must be either --output=josm or --output=osmchange\n";
}

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
my $wanted_nodes = Bit::Vector->new( 50 * 1000 * 1000 );
my $wanted_segs = Bit::Vector->new( 50 * 1000 * 1000 );
my $found_segs = Bit::Vector->new( 50 * 1000 * 1000 );

# Any node listed in the remains file cannot be deleted
if ($remainsfile)
{
    open my $fh, "<$remainsfile" or die "Couldn't open $remainsfile ($!)\n";
    while(<$fh>)
    {
        if( /^(\d+)/ )
        {
            $wanted_nodes->Bit_On($1);
        }
    }
}
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
			  if( $output eq "josm" )
			  {
			      print $line;
                          }
                          else
                          {
                              print qq(<osmChange version="0.3" generator="planetosm-deleteby-tags">\n);
                          }
			}
		}
		elsif($line =~ /^\s*\<\/osm\>/ ) {
			if($pass == 3) {
			  if( $output eq "josm" )
			  {
			      print $line;
                          }
                          else
                          {
                              print qq(</osmChange>\n);
                          }
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
processXML(undef,sub {
        # Track segments used, so we can identify incomplete ways later
        my ($id,$from,$to,$tagsRef,$main_line,$line) = @_;
        $found_segs->Bit_On($id);
  }, sub {
	my ($id,$tagsRef,$segsRef,$main_line,$line) = @_;

	# Test the tags, to see if we want this
	if(&$wayTagHelper(@$tagsRef)) {
		# Bingo, matched
		# Record the segments we want to get (also track completeness of way)
		my $complete = 1;
		foreach my $seg (@$segsRef) {
			if( not $found_segs->contains($seg) )
			{ $complete = 0; last }
		}

		# Output
                if( $complete )
                {
                        print qq(<delete version="0.3">\n  <way id="$id">\n) if $output eq "osmchange"; 
                        print qq(<way id="$id" action="delete" >\n) if $output eq "josm";
                        &printTags(@$tagsRef);
                        print qq(</way>\n);
                        print qq(</delete>\n) if $output eq "osmchange";
                        $deleted{ways}++;
                }
                else
                {
                        if( $output eq "josm" )
                        {
                            my $a = $main_line;
                            $a =~ s/way /way action="modify" /;
                            print $a;
                        }
                        else
                        {
                            print qq(<modify version="0.3">\n  $main_line\n);
                        }
                        foreach my $seg (@$segsRef) {
                                if( not $found_segs->contains($seg) ) {
                                        print "    <seg id=\"$seg\" />\n";
                                }
                        }
                        &printTags(@$tagsRef);
                        print $line;
                        print qq(</modify>\n) if $output eq "osmchange";
                }
	} else {
	        # Want to keep this way, so mark segments used
		foreach my $seg (@$segsRef) {
			$wanted_segs->Bit_On($seg);
		}
	}
});

# Now for segments
my $segTagHelper = &buildTagMatcher(@seg_sel_tags);
processXML(undef, sub {
	my ($id,$from,$to,$tagsRef,$main_line,$line) = @_;
	my $wanted = 0;

        # Does a way want it?
        if($wanted_segs->contains($id)) {
                # A way wants it
                $wanted = 1;
        }

	if(not $wanted) {
	        if( $output eq "josm" )
	        {
	                print qq(<segment id="$id" from="$from" to="$to" action="delete" >\n);
                }
                else
                {
                        print qq(<delete version="0.3">\n), $main_line;
                }
	        &printTags(@$tagsRef);
	        if( $line ne $main_line )
	        {
	            print $line;
                }
	        if( $output eq "osmchange" )
	        {
                        print qq(</delete>\n);
	        }
                $deleted{segs}++;
	} else {
		# Record the nodes we want to keep
		$wanted_nodes->Bit_On($from);
		$wanted_nodes->Bit_On($to);
	}
}, undef);

# Now for nodes
my $nodeTagHelper = &buildTagMatcher(@node_sel_tags);
processXML(sub {
	my ($id,$lat,$long,$tagsRef,$main_line,$line) = @_;
	my $wanted = 0;

        return if($wanted_nodes->contains($id));

	# Test the tags, to see if we don't want this
	# This could presumably fail if the node is actually in use, but you can't win 'em all...
	if(&$nodeTagHelper(@$tagsRef)) {
		# Bingo, matched
	        if( $output eq "josm" )
	        {
                    print qq(<node id="$id" lat="$lat" lon="$long" action="delete">\n);
                }
                else
                {
                    print qq(<delete version="0.3">\n), $main_line;
                }
                &printTags(@$tagsRef);
	        if( $line ne $main_line )
	        {
	            print $line;
                }
                if( $output eq "osmchange" )
                { print qq(</delete>\n) }
                $deleted{nodes}++;
                return;
	}

	# Delete also not useful nodes, they just clutter the place up...
	my $useful = 0;
	foreach my $tag (@$tagsRef)
	{
	  next if $tag->[0] =~ /^(created_by|source|converted_by|name|ele|time)$/;
	  $useful = 1;
	  last;
        }
	if(not $useful) {
	        if( $output eq "josm" )
	        {
                    print qq(<node id="$id" lat="$lat" lon="$long" action="delete">\n);
                }
                else
                {
                    print qq(<delete version="0.3">\n), $main_line;
                }
                &printTags(@$tagsRef);
	        if( $line ne $main_line )
	        {
	            print $line;
                }
                if( $output eq "osmchange" )
                { print qq(</delete>\n) }
                $deleted{nodes}++;
	}
}, undef, undef);

print STDERR "Deleted: nodes:$deleted{nodes}, segs:$deleted{segs}, ways:$deleted{ways}\n";

# All done

##################################################################
# Usage/manual

__END__

=head1 NAME

B<planetosm-deleteby-tags.pl>

=head1 DESCRIPTION

=head1 SYNOPSIS

B<Common usages:>

B<planetosm-deleteby-tags.pl> <planet.osm.xml> > output.osm

parse a given planet.osm and output an OSM file that will delete any object
with the given tags, as well as thus orphaned segments and ways.

Note: As a sideeffect it will also delete any unwayed segments and nodes
that don't have any useful tags.

=head1 AUTHOR

Martijn van Oosterhout

based on script by Nick Burch

=head1 COPYRIGHT

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
