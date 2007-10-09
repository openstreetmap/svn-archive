#!/usr/bin/perl

# This script extracts all nodes, segments, and ways lying inside a polygon
#
# The script first compiles a polygon from the data, then simplifies it,
# and then processes the planet file. In processing the file, first a
# bounding box is used to exclude all nodes that lie outside, and the
# more expensive polygon check is only made for those inside.
#
# Author Frederik Ramm <frederik@remote.org> / public domain
# Author Keith Sharp <kms@passback.co.uk> / public domain

# Progress meter, exclusions, et al all added by Martijn van Oosterhout
# <kleptog@svana.org> for the AND import (August 2007) / public domain

# THIS IS THE API 0.5 VERSION (that's 0.4 minus segments plus relations). 
# Also adapted to used Bit::Vector instead of hash to save memory. Big 
# change is that this now supports "referential integrity", i.e. you 
# can request it to behave like the API does, giving you all nodes for
# each way returned, not only those within the bounding box.
#
# Get this from CPAN!
use Math::Polygon;
use Getopt::Long;
use File::Basename;
use Bit::Vector;
use IO::Handle;
use IO::File;

use strict;

my $borderpolys;
my $currentpoints;
my $minlon = 999;
my $minlat = 999;
my $maxlat = -999;
my $maxlon = -999;

# assume 500 million nodes,
my $nodes_copied = Bit::Vector->new(500*1000*1000);
my $nodes_needed = Bit::Vector->new(500*1000*1000);

# and 50 million ways and relations.
my $ways_copied = Bit::Vector->new(50*1000*1000);
my $ways_needed = Bit::Vector->new(50*1000*1000);

my $relations_copied = Bit::Vector->new(50*1000*1000);
my $relations_needed = Bit::Vector->new(50*1000*1000);

# this puts total memory allocation at 1,2 billion bits, which should
# be just below 250 MB including overhead and should be sufficient for
# planet files in the area of 5 to 10 GB (compressed size).

my $help;
my $polyfile;
my $infile;
my $outfile;
my $remainsfile;
my $compress;
my $verbose = 0;
my $references = 0;

my $prog = basename($0);

my $filepos_nodes = 1;
my $filepos_ways;
my $filepos_relations;

my $input_file_handle;
my $input_is_seekable;
my $assume_input_sorted = 1;
my $deep_relations = 1;
my $sort_output = 0;
my $bbox;
my $area = 0;

my $output_file_handle_nodes;
my $output_file_handle_ways;
my $output_file_handle_rels;

sub usage 
{
    my ($msg) = @_;
    print STDERR "$msg\n" if defined($msg);

    print STDERR << "EOF";

This Perl script will process a planet.osm file and extract the nodes,
ways, and relations falling within a polygon.

usage: $prog [-h] [-c number] [-i file] [-o file] [-r file] -p file [-d]

 -h       : print ths help message and exit.
 -a num   : ignore sub-polygons of area "num" or smaller (in degrees squared)
 -c num   : reduce the number of points in the polygon by num %.  This 
            only takes effect if the polgon has more than 100 points.
 -b bbox  : use bounding box instead of polygon (bbox being four comma-
            separated numbers: bllon,bllat,trlon,trlat).
 -p file  : file containing the polygon definition.
 -i file  : OSM planet file to process.
 -o file  : OSM planet file to output.
 -r       : preserve referential integrity. This requires multiple passes
            and thus will be slower, ESPECIALLY if you use a compressed
            input file (needs to be decompressed multiple times). 
 -s       : sort output; this is only meaningful in conjunction with -r
            as output without -r will be sorted anyway.

If you do not supply an output file, will write to STDOUT. Reading from
STDIN is not yet supported.

A polygon file should be a plain text file structured like this: Each 
polygon begins with a number in the first column; the following rows 
begin with whitespace followed by a whitespace-separated coordinate 
pair (longitude, then latitude). The polygon ends when a line contains
only "END". Other polygons may then follow.

For example a polygon defining Great Britain and Ireland (and some of the 
smaller islands) would be:

1
    -0.6450E+01    0.4980E+02
    -0.2000E+01    0.4890E+02
    -0.1850E+01    0.4925E+02
    -0.2080E+01    0.4973E+02
     0.1350E+01    0.5090E+02
     0.2250E+01    0.5258E+02
    -0.0500E+01    0.6130E+02
    -0.8920E+01    0.5785E+02
    -0.1140E+02    0.5130E+02
    -0.6450E+01    0.4980E+02
END

Multiple polygons can be described by adding additional sections to the
polygon file.

EOF
    exit;
}

GetOptions ('h|help' => \$help,
            'c|compress=i' => \$compress,
            'a|area=n' => \$area,
            'v|verbose' => \$verbose,
            'i|input=s' => \$infile,
            'o|output=s' => \$outfile,
            'p|polygon=s' => \$polyfile,
            'b|bbox=s' => \$bbox,
            's|sort' => \$sort_output,
            'r|references' => \$references,
            ) || usage ();

if ($help) {
    usage();
}

if ((!$polyfile && !$bbox) || ($polyfile && $bbox))
{
    usage("exactly one of -b and -p must be given");
}

if ($sort_output && !$outfile)
{
    usage("you must specify an output file if you want sorting");
}

if (!$infile)
{
    usage("you must specify an input file - cannot work with stdin");
}

if ($polyfile)
{
    $borderpolys = [];
    open (PF, "$polyfile") || die "Could not open file: $polyfile: $!";

    my $invert;
    # initialize border polygon.
    while(<PF>)
    {
        if (/^(!?)\d/)
        {
            $invert = ($1 eq "!") ? 1 : 0;
            $currentpoints = [];
        }
        elsif (/^END/)
        {
            my $pol = Math::Polygon->new(points => $currentpoints);
            if (($compress > 0 && $compress < 100) && $pol->nrPoints > 99) {
                my $simp = $pol->simplify($pol->nrPoints*(100-$compress)/100);
                push(@{$borderpolys}, [$simp,$invert,[$simp->bbox]]) if ($simp->area()>$area);
            } else {
            push(@{$borderpolys}, [$pol,$invert,[$pol->bbox]]);
            }
            undef $currentpoints;
            if( $verbose )
            {
                printf STDERR "Added polygon: %d points (%.2f,%.2f)-(%.2f,%.2f) %s\n",
                    $borderpolys->[-1][0]->nrPoints,
                    @{$borderpolys->[-1][2]},
                    ($borderpolys->[-1][1] ? "exclude" : "include");
            }
        }
        elsif (defined($currentpoints))
        {
            /^\s+([0-9.E+-]+)\s+([0-9.E+-]+)/ or die;
            push(@{$currentpoints}, [$2, $1]);
            $minlat = $2 if ($2 < $minlat);
            $maxlat = $2 if ($2 > $maxlat);
            $minlon = $1 if ($1 < $minlon);
            $maxlon = $1 if ($1 > $maxlon);
        }
    }
    close (PF);
}
else
{
    # simply set minlat, maxlat etc from bbox parameter - no polygons
    ($minlat, $minlon, $maxlat, $maxlon) = split(",", $bbox);
    die ("badly formed bounding box - use four comma-separated values for ".
        "bottom left latitude, bottom left longitude, top right latitude, ".
        "top right longitude") unless defined($maxlon);
    die ("max longitude is less than min longitude") if ($maxlon < $minlon);
    die ("max latitude is less than min latitude") if ($maxlat < $minlat);
}

if ($outfile) 
{
    if ($sort_output)
    {
        $output_file_handle_nodes = new IO::File(">$outfile.n") or die ("cannot open $outfile.n: $!");
        $output_file_handle_ways = new IO::File(">$outfile.w") or die ("cannot open $outfile.w: $!");
        $output_file_handle_rels = new IO::File(">$outfile.r") or die ("cannot open $outfile.r: $!");
    }
    else
    {
        $output_file_handle_nodes = new IO::File(">$outfile") or die ("cannot open $outfile: $!");
        $output_file_handle_ways = $output_file_handle_nodes;
        $output_file_handle_rels = $output_file_handle_nodes;
    }
} 
else 
{
    $output_file_handle_nodes = new IO::Handle;
    $output_file_handle_nodes->fdopen(fileno(STDOUT),"w") || die "Cannot write to standard output: $!";
    $output_file_handle_ways = $output_file_handle_nodes;
    $output_file_handle_rels = $output_file_handle_nodes;
}

if ($remainsfile) 
{
    open (RF, ">$remainsfile") || die "Could not open file: $remainsfile: $!";
}

print $output_file_handle_nodes <<EOF;
<?xml version="1.0" encoding="UTF-8"?>
<osm version="0.5" generator="extract-polygon.pl">
EOF

my $copy;
my $waybuf;
my $count = 0;

my $polygon_node_selector = sub {
    my ($id, $lat, $lon) = @_;
    return 0 if (($lat < $minlat) || ($lat > $maxlat));
    return 0 if (($lon < $minlon) || ($lon > $maxlon));
    return 1 unless defined($borderpolys);
    my $ll = [$lat, $lon];
    my $rv = 0;
    foreach my $p (@{$borderpolys})
    {
        my($poly,$invert,$bbox) = @$p;
        next if ($ll->[0] < $bbox->[0]) or ($ll->[0] > $bbox->[2]);
        next if ($ll->[1] < $bbox->[1]) or ($ll->[1] > $bbox->[3]);

        if ($poly->contains($ll))
        {
            # If this polygon is for exclusion, we immediately bail and go for the next point
            if($invert)
            {
                return 0;
            }
            $rv = 1; 
            # do not exit here as an exclusion poly may still be there 
        }
    }
    return $rv;
};

# ---------------------------------------------------------------------------
# First Extraction 
# ---------------------------------------------------------------------------

# This starts copying stuff from the planet file to output, recording IDs
# as it goes along.

# first, the nodes;
# this returns a list of nodes having been copied in $used_nodes
select_nodes($polygon_node_selector, $nodes_copied);

# then, the ways; 

my $way_selector = sub {
    my ($way_id, $node_id) = @_;
    return ($nodes_copied->contains($node_id));
};

# this returns a list of ways having been copied in $used_ways, 
# and additionaly a list of all nodes these ways use in $nodes_needed

select_ways($way_selector, $ways_copied, $nodes_needed);

# finally, the relations;

my $relation_selector = sub {
    my ($relation_id, $member_type, $member_id) = @_;
    return 1 if ($member_type eq "node" && $nodes_copied->contains($member_id));
    return 1 if ($member_type eq "way" && $ways_copied->contains($member_id));
    return 0;
};

# returns a list of relations having been copied in $relations_copied,
# and additionally lists of nodes,ways,relations required.

select_relations($relation_selector, $relations_copied);

# use this if you want to allow relations to request the copying of their members:
# select_relations($relation_selector, $relations_copied, $nodes_needed, $ways_needed, $relations_needed);

# ok, time to pause. 
#
# what we now have is:
# * all nodes in the bounding box
# * all ways using at least one of these nodes
# * all relations having one of the nodes or ways as their member.
#
# That's sufficient for non-referential-integrity mode:

end_output() unless $references;

# ---------------------------------------------------------------------------
# Follow-On Extractions
# ---------------------------------------------------------------------------

# if we want referential integrity, we have to re-visit everything, this
# time collecting referenced objects not collected the first time.

my $node_selector = sub {
    my ($id, $lat, $lon) = @_;
    return $nodes_needed->contains($id) && !$nodes_copied->contains($id);
};

select_nodes($node_selector, $nodes_copied);

# if you want to allow relations to request copying of their members, proceed
# like this:

#my $way_selector = sub {
#    my ($way_id, $node_id) = @_;
#    return 0 if ($ways_copied->contains($way_id));
#    # this could be used to return all ways that use the copied nodes but 
#    # are not themselves copied:
#    # return 1 if $nodes_copied->contains($node_id) 
#    return $ways_needed->contains($node_id);
#};
#
#select_ways($way_selector, $ways_copied, $nodes_needed);
#
#my $relation_selector = sub {
#    my ($relation_id, $member_type, $member_id) = @_;
#    return 0 if ($relations_copied->contains($relation_id));
#    return 1 if ($member_type eq "node" && $nodes_copied->contains($member_id));
#    return 1 if ($member_type eq "way" && $ways_copied->contains($member_id));
#    return 0;
#};
#
#select_relations($relation_selector, $relations_copied, $nodes_needed, $ways_needed, $relations_needed);

# you may do this in a loop to fetch things recursively, using a termination 
# condition like:
# last if ($relations_needed->subset($relations_copied) &&
#   $ways_needed->subset($ways_copied) &&
#   $nodes_needed->subset($nodes_copied));

end_output();

sub select_nodes
{
    my ($selector, $copied) = @_;
    my $lastpos;

    # jump to a suitable position of input
    if (defined($filepos_nodes) && $input_is_seekable)
    {
        seek($input_file_handle, $filepos_nodes, 0);
    }
    else
    {
        reopen_input();
    }

    while(<$input_file_handle>) 
    {
        if ((/\s*<way/) && $assume_input_sorted)
        {
            $filepos_ways = $lastpos;
            last;
        }
        last if /^\s*<\/osm>/;

        if (/^\s*<osm.*version="([^"]+)"/)
        {
            die "this program supports 0.5 style files only; your input file has version $1"
                unless $1 eq "0.5";
        }
        # Note: we allow a minus in the ID to allow incomplete files also    
        elsif (/^\s*<node.*id=["'](-?\d+)['"].*lat=["']([Ee0-9.-]+)["'] lon=["']([Ee0-9.-]+)["']/)
        {
            $copy = &$selector($1, $2, $3);
            if ($copy)
            {
                $copied->Bit_On($1);
                print $output_file_handle_nodes $_;
            }
        }
        elsif ($copy)
        {
            print $output_file_handle_nodes $_;
        }
        $lastpos = tell($input_file_handle) if $input_is_seekable;
    }
}

sub select_ways
{
    my ($selector, $copied, $noderefs) = @_;
    my $nodes_in_current_way = [];
    my $copy = 0;
    my $waybuf;
    my $wid;
    my $lastpos;

    # jump to a suitable position of input
    if (defined($filepos_ways) && $input_is_seekable)
    {
        seek($input_file_handle, $filepos_ways, 0);
    }

    while(<$input_file_handle>) 
    {
        if ((/\s*<relation/) && $assume_input_sorted)
        {
            $filepos_relations = $lastpos;
            last;
        }
        last if /^\s*<\/osm>/;

        if (/^\s*<way\s+id=["']([0-9-]+)['"]/)
        {
            $copy = 0;
            $waybuf = $_;
            $nodes_in_current_way = [];
            $wid = $1;
        }
        elsif (/^\s*<nd ref=['"](.*)["']/)
        {
            if (!$copy)
            {
                # if way contains used node, and is not being copied yet, copy.
                if (&$selector($wid, $1))
                {
                    print $output_file_handle_ways $waybuf;
                    $copied->Bit_On($wid);
                    $copy = 1;
                    foreach my $nid(@$nodes_in_current_way)
                    {
                        $noderefs->Bit_On($nid);
                    }
                }
            }
            if ($copy)
            {
                print $output_file_handle_ways $_;
                $noderefs->Bit_On($1);
            }
            else
            {
                $waybuf .= $_;
                push(@$nodes_in_current_way, $1);
            }
        }
        elsif ($copy)
        {
            print $output_file_handle_ways $_;
        }
        $lastpos = tell($input_file_handle) if $input_is_seekable;
    }
}

sub select_relations
{
    my ($selector, $copied, $noderefs, $wayrefs, $relrefs) = @_;
    my $members_in_current_relation = [];
    my $copy = 0;
    my $relbuf;
    my $rid;
    my $lastpos;
    my $refs = { "node" => $noderefs, "way" => $wayrefs, "relation" => $relrefs };

    # jump to a suitable position of input
    if (defined($filepos_relations) && $input_is_seekable)
    {
        seek($input_file_handle, $filepos_relations, 0);
    }

    while(<$input_file_handle>) 
    {
        last if /^\s*<\/osm>/;

        if (/^\s*<relation\s+id=["']([0-9-]+)['"]/)
        {
            $copy = 0;
            $relbuf = $_;
            $members_in_current_relation = [];
            $rid = $1;
        }
        elsif (/^\s*<member /)
        {
            my ($ref, $type);
            if (/ref=['"](.*?)['"]\s.*type=['"](.*?)['"]/)
            {
                ($ref, $type) = ($1, $2);
            }
            elsif (/type=['"](.*?)['"]\s.*ref=['"](.*?)['"]/)
            {
                ($ref, $type) = ($2, $1);
            }
            else
            {
                die "cannot parse member description: $_";
            }

            if (!$copy)
            {
                # if relation contains used object, and is not being copied yet, copy.
                if (&$selector($rid, $type, $ref))
                {
                    $copied->Bit_On($rid);
                    print $output_file_handle_rels $relbuf;
                    $copy = 1;
                    foreach my $mmb(@$members_in_current_relation)
                    {
                        my ($t, $r) = split(":", $mmb);
                        $refs->{$t}->Bit_On($r);
                    }
                }
            }

            if ($copy)
            {
                print $output_file_handle_rels $_;
                $refs->{$type}->Bit_On($ref) if defined($refs->{$type});
            }
            else
            {
                $relbuf .= $_;
                push(@$members_in_current_relation, "$type:$ref");
            }
        }
        elsif ($copy)
        {
            print $output_file_handle_rels $_;
        }
        $lastpos = tell($input_file_handle) if $input_is_seekable;
    }
}

sub reopen_input
{
    undef $input_file_handle;
    $input_file_handle = new IO::File;
    if ($infile =~ /\.bz2/) 
    {
        $input_file_handle->open("bzcat $infile |") || die "Could not bzcat file: $infile: $!";
        $input_is_seekable = 0;
    } 
    elsif ($infile =~ /\.gz/) 
    {
        $input_file_handle->open("zcat $infile |") || die "Could not zcat file: $infile: $!";
        $input_is_seekable = 0;
    } 
    else 
    {
        $input_file_handle->open($infile) || die "Could not open file: $infile: $!";
        $input_is_seekable = 1;
    }
}

sub end_output
{
    print $output_file_handle_rels "</osm>\n";
    $output_file_handle_nodes->close();
    $output_file_handle_ways->close();
    $output_file_handle_rels->close();
    if ($sort_output)
    {
        # fixme do this with seek from within perl
        system("cat $outfile.w >> $outfile.n");
        system("cat $outfile.r >> $outfile.n");
        unlink("$outfile.w");
        unlink("$outfile.r");
        rename("$outfile.n","$outfile");
    }
    exit;
}
