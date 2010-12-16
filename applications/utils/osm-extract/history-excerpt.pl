#!/usr/bin/perl
#
# Takes a history planet file and excerpts an area from it.
#
# Written by Frederik Ramm <frederik@remote.org> - public domain.
#
# This is loosely based on planetosm-excerpt-area, written by Nick Burch in 2006.
#
# Input is read from stdin, so the history planet may be decompressed and
# piped in. The output of this script is a valid OSM XML file which differs 
# from normal OSM XML files in that it may have more than one version of the 
# same object.
#
# This script does not use a proper XML parser, and is geared specifically
# to the format currently produced by the history dump writer. Most imporantly,
# that writer puts the whole file in one long line with no whitespace between
# XML tags.
# 
# The bbox is specified osmosis-style on the command line. Example:
#
# bzcat history-planet.osm.bz2 | perl history-excerpt.pl left=10 right=11.5 top=49 bottom=48 > excerpt.osm
#
# Criteria for inclusion:
#
# * A node is included if it falls inside the specified bounding box.
#
#   - Once a node has been included, all future versions will be included
#     even if they move outside the bounding box; but previous versions 
#     will not be included.
#
# * A way is included if it references a node of which at least one version
#   has been included. Temporality is not taken into account, i.e. the way
#   version is included even if the referenced node only moved into the 
#   area of interest later.
#
#   - If ways reference nodes that have not been included, these references
#     will be removed from the way.
#   - If less than two nodes remain in a way version, it will not be written.
#     This means that it is possible for a way to be present e.g. only in 
#     versions 1,6,7.
#
# * Relations are treated the same as ways, but cascading relations are not
#   supported - all members of type "relation" will be dropped from relations.

use strict;
use warnings;

use Getopt::Long;
use Bit::Vector;
use POSIX;

my $left;
my $right;
my $top;
my $bottom;

my $indent = "  ";
my $buffersize = 1024 * 1024 * 1024;

while(my $arg = shift)
{
    if ($arg =~ /^(left|right|top|bottom)=(-?\d*(\.?\d+)?)$/)
    {
        eval('$'.$1.'='.$2);
    }
    else
    {
        usage();
    }
}

usage() if (!defined($left) or $left < -180 or $left > 180);
usage() if (!defined($right) or $right < -180 or $right > 180 or $right <= $left);
usage() if (!defined($bottom) or $bottom < -90 or $bottom > 90);
usage() if (!defined($top) or $top < -90 or $top > 90 or $top <= $bottom);

# Counts of the numbers handled
my $node_count = 0;
my $way_count = 0;
my $rel_count = 0;
my $tag_count = 0;

# We assume IDs to be up to 1500 million for nodes, 250 million for ways
my $nodes = Bit::Vector->new( 1500 * 1000 * 1000 );
my $ways = Bit::Vector->new( 250 * 1000 * 1000 );

# Hold the id and type of the last valid main tag
my $last_id = -1;
my $last_type;
my $last_copied_id = 0;

# Hold the node and tags list for a way
my $way_line;
my @way_tags;
my @way_nodes;
# Something similar for relations 
my $rel_line;
my @rel_tags;
my @rel_members;

# loop over the input file data

my $buffer = "";
my $eof = 0;
my $currentpos = 0;

while(1)
{
    my $lt = index($buffer, '><', $currentpos);
    last if ($lt == -1 && $eof);
    while ($lt == -1)
    {
        $buffer = substr($buffer, $currentpos);
        $currentpos = 0;
        my $br = sysread(STDIN, $buffer, $buffersize, length($buffer));
        if ($br == 0)
        {
            $eof = 1;
            $buffer .= '<';
        }
        $lt = index($buffer, '><');
        $lt = length($buffer) - 2 if ($eof && $lt==-1);
    }
    my $tag = substr($buffer, $currentpos, $lt + 1 - $currentpos);
    my $twolet = substr($tag, 0, 3);
    $currentpos = $lt + 1;

	$tag_count++;

	# process the tag
	if ($twolet eq "<no")
    {
		my ($id, $lat, $lon) = ($tag =~ /^<node.*\sid=['"](\d+)['"].+lat=['"]?(\-?[\d\.]+)['"]?.+lon=['"]?(\-?[\d\.]+e?\-?\d*)['"]?/);
        if ($id != $last_id)
        {
            $last_id = -1; # In case it has tags we need to exclude
            $last_type = "node";

            unless($id) { warn "Invalid tag '$tag'"; next; }

            # Do we need to exclude this node?
            if ($lon < $left || $lon > $right || $lat < $bottom || $lat > $top)
            {
                next;
            }
        }

		# Output the node
		print "$tag\n";

		$nodes->Bit_On($id);
		$last_id = $id;

		$node_count++;
	}
	elsif ($twolet eq "<wa")  
    {
		my ($id) = ($tag =~ /^<way.*\sid=[\'\"](\d+)[\'\"]/);

        $last_id = -1; # In case it has tags we need to exclude
        $last_type = "way";

        unless($id) { warn "Invalid line '$tag'"; next; }

        if (($tag =~ /\/>\s*$/) && $id == $last_copied_id)
        {
            print $tag."\n";
            next;
        }

        # Save ID and line, will add later
        $last_id = $id;
        $way_line = $tag;

		$way_count++;

		# Blank way children lists
		@way_tags = ();
		@way_nodes = ();
	}
	elsif ($twolet eq "</w")
    {
		my $way_id = $last_id;
		$last_id = -1;

		next unless($way_id);
		next if(scalar(@way_nodes)<2);

		# Record this id
		$ways->Bit_On($way_id);

		# Output way
		print $way_line."\n";

		# Output way nodes
		foreach my $wn (@way_nodes) 
        {
			print $indent.$wn->{line};
		}

		# Add way tags
		foreach my $wt (@way_tags) 
        {
			print $indent.$wt->{line};
		}

		# Finish way
		print $tag."\n";
        $last_copied_id = $way_id;
	}
	elsif ($twolet eq "<nd")
    {
		my ($ref) = ($tag =~ /^<nd ref=[\'\"](\d+)[\'\"]/);
		next unless($last_id > 0);
		unless($ref) { warn "Invalid line '$tag'"; next; }
		next unless($nodes->contains($ref));

		# save, only add later
		my %wn;	
		$wn{'line'} = $tag."\n";
		$wn{'ref'} = $ref;

		push (@way_nodes,\%wn);
	}
	elsif ($twolet eq "<re")
    {
		my ($id) = ($tag =~ /^<relation.*\sid=[\'\"](\d+)[\'\"]/);
		$last_id = -1; # In case it has tags we need to exclude
		$last_type = "relation";

		unless($id) { warn "Invalid line '$tag'"; next; }

        if (($tag =~ /\/>\s*$/) && $id == $last_copied_id)
        {
            print $tag."\n";
            next;
        }

		# save ID and line, will add later
		$last_id = $id;
		$rel_line = $tag;

		$rel_count++;

		# blank relation children lists
		@rel_tags = ();
		@rel_members = ();
	}
	elsif ($twolet eq "</r")
    {
		my $rel_id = $last_id;
		$last_id = -1;

		next unless($rel_id);
		next unless(@rel_members);

		# Output relation
		print $rel_line."\n";

		# Output ways and nodes
		foreach my $rm (@rel_members) 
        {
			print $indent.$rm->{line};
		}

		# Add relation tags
		foreach my $rt (@rel_tags) 
        {
			print $indent.$rt->{line};
		}

		# Finish relation
		print $tag."\n";
        $last_copied_id = $rel_id;
	}
	elsif ($twolet eq "<me")
    {
		my ($type,$ref,$role) = ($tag =~ /^<member type=[\'\"](.*?)[\'\"] ref=[\'\"](\d+)[\'\"] role=[\'\"](.*?)[\'\"]/);
		next unless($last_id > 0);
		unless($type && $ref) { warn "Invalid line '$tag'"; next; }

		if ($type eq "node") 
        {
			next unless($nodes->contains($ref));
		} 
        elsif($type eq "way") 
        {
			next unless($ways->contains($ref));
		} 
        else 
        {
			warn("Skipping unknown type '$type' for line '$tag'");
			next;
		}

		# Save, only add later
		my %rm;	
		$rm{'line'} = $tag."\n";
		$rm{'type'} = $type;
		$rm{'ref'} = $ref;
		$rm{'role'} = $role;

		push (@rel_members,\%rm);
	}
	elsif ($twolet eq "<ta")
    {
		my ($name,$value) = ($tag =~ /^<tag k=[\'\"](.*?)[\'\"] v=[\'\"](.*?)[\'\"]/);
		unless($name) 
        { 
			unless($tag =~ /k="" v=""/) 
            {
				warn "Invalid line '$tag'"; 
			}
			next; 
		}
		if ($name =~ /^\s+$/) { warn "Skipping invalid tag line '$tag'"; next; }

		# If last_id isn't there, the element we're attached to was invalid
		next unless($last_id > 0);

		if ($last_type eq "node") 
        {
			print $indent.$tag."\n";
		} 
        elsif ($last_type eq "way") 
        {
			# Save, only add if way has nodes
			my %wt;	
			$wt{'line'} = $tag."\n";
			$wt{'name'} = $name;
			$wt{'value'} = $value;
			push (@way_tags,\%wt);
		} 
        elsif ($last_type eq "relation") 
        {
			# Save, only add if relation has nodes/ways
			my %rt;	
			$rt{'line'} = $tag."\n";
			$rt{'name'} = $name;
			$rt{'value'} = $value;
			push (@rel_tags,\%rt);
		}
	}	
	elsif ($twolet eq "<?x")
    {
		print $tag."\n";
	}
	elsif ($twolet eq "<os" || $twolet eq "</o")
    {
		print $tag."\n";
	}
	elsif ($twolet eq "</n")
    {
		print $tag."\n" if ($last_id > 0);
	}
	elsif ($twolet eq "<ch" or $twolet eq "</c")
    {
		# ignore
	}
	else
    {
	    print STDERR "Unknown line $tag\n";
	};
}

sub usage
{
    print STDERR "usage: $0 filename.osm left=x bottom=x right=x top=x\n\n";
    print STDERR "  excerpts the given bounding box from the given history file, and\n";
    print STDERR "  writes the result to standard output.\n";
    exit;
}
