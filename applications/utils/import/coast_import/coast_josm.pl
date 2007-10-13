#!/usr/bin/perl

# This program creates an OSM file compatible with JOSM, based 
# on PGS coastlines.
# 
# It can be used to create OSM 0.5 compatible files (default) 
# or OSM 0.4 compatible files (set $five=0).
#
# This script also supports simplification of the PGS data; see
# comment below for details. 
#
# Usage: 
#   perl coast_josm.pl LatS LatN LongW LongE [datafile]
#
# See http://wiki.openstreetmap.org/index.php/Running_the_coastline_upload
# for further usage information.
#
# This script written by Frederik Rammm <frederik@remote.org>. It mimicks
# earlier GPL licensed work by OJW, Joerg Ostertag, Blars Blarson, but
# being entirely re-written is now Public Domain.

use strict;

use Geo::ShapeFile;

use constant PI => 4 * atan2 1, 1;
use constant DEGRAD => PI / 180;
use constant RADIUS => 6367000.0;
use constant MAXINT => 1<<31-1;

my %node_cache;

unless(scalar(@ARGV)>3)
{
    print "Usage: perl coast_josm.pl LatS LatN LongW LongE [datafile]";
    exit 0;
}

my ($bllat, $trlat, $bllon, $trlon, $datafile) = @ARGV;

if ($bllat > $trlat) 
{
    my $tmp = $bllat;
    $bllat = $trlat;
    $trlat = $tmp;
}

if ($bllon > $trlon) 
{
    my $tmp = $bllon;
    $bllon = $trlon;
    $trlon = $tmp;
}

# this controls simplification. the simplification mechanism works like this: 
# whenever a way has at least one node which can be removed without changing
# the length of the way more than $max_error metres, the node whose removal
# means the smalles change in total length is removed. the process is repeated
# until removing any node would increase the length by more than $max_error
# metres.
#
# values between 5 and 20 give good results. use 0 to disable simplification.
my $max_error = 12;

# change this to 0 if you want OSM 0.4 compliant output (with segments)
my $five = 1;

open(OSM, ">coast+$bllat+$trlat+$bllon+$trlon.osm")
    or die "Could not open osm file for writing: $!";

unless($five)
{
    open(OSMSEG, "+>.seg.tmp") or die "could not open .seg.tmp for writing: $!";
    unlink(".seg.tmp");
}
open(OSMWAY, "+>.way.tmp") or die "could not open .way.tmp for writing: $!";
unlink(".way.tmp");

my $current_id = 0;
print OSM "<?xml version='1.0' encoding='UTF-8'?>\n";
printf OSM "<osm version='0.%d' generator='coast_josm.pl'>\n", $five ? 5 : 4;

my $filename = "Data/NGA_GlobalShoreline_cd".$datafile;

open(LOG, ">log.txt") or die("Cannot create logfile");
printf LOG "Lon %f to %f, Lat %f to %f, shapefile %s\n",
    $bllon, $trlon, $bllat, $trlat, $filename;
printf LOG "(%f, %f)\n", $trlon - $bllon, $trlat - $bllat;

my $shapefile = new Geo::ShapeFile($filename);
printf "%d shapes\n", $shapefile->shapes();

for (my $i=1; $i<=$shapefile->shapes(); $i++) 
{
    my $shape = $shapefile->get_shp_record($i);
    my $currentpoints = [];

    foreach my $point($shape->points())
    {
        my $lon = sprintf("%.7f", $point->X());
        my $lat = sprintf("%.7f", $point->Y());

        if (($lat > $bllat) && ($lat < $trlat) && ($lon > $bllon) && ($lon < $trlon) && scalar @{$currentpoints} < 249)
        {
            push(@$currentpoints, [$lat,$lon]);
        }
        elsif (($lat > $bllat) && ($lat < $trlat) && ($lon > $bllon) && ($lon < $trlon) && scalar @{$currentpoints} == 249)
        {
            push(@$currentpoints, [$lat,$lon]);
            write_way($currentpoints);
            $currentpoints = [];
            push(@$currentpoints, [$lat,$lon]);
        }
        else
        {
            write_way($currentpoints);
            $currentpoints = [];
        }
    }
    write_way($currentpoints);
}

unless($five)
{
    seek OSMSEG, 0, 0;
    print OSM while(<OSMSEG>);
    close(OSMSEG);
}

seek OSMWAY, 0, 0;
print OSM while(<OSMWAY>);
close(OSMWAY);

print OSM "</osm>\n";
close OSM;
print LOG "Complete\n";
close LOG;
print "Done\n";

exit 0;

sub write_way 
{
    my $points = shift;
    return unless (scalar(@$points) > 1);

    printf LOG "new way %d nodes\n", scalar(@$points);

    my $eliminated = 0;
    my $min_error;
    if ((scalar(@$points) > 2) && ($max_error > 0))
    { 
        $points = simplify_way($points);
    }

    $current_id--;
    print OSMWAY "<way id=\"$current_id\" action='create' visible='true'>\n";

    my $lastpoint = $points->[0];
    printf OSMWAY "  <nd ref=\"%d\" />\n", write_node(@$lastpoint) if ($five);
    for (my $i=1; $i<scalar(@$points); $i++)
    {
        my $thispoint = $points->[$i];
        next if ($thispoint == $lastpoint);
        my $thisnode = write_node(@$thispoint);
        if ($five)
        {
            printf OSMWAY "  <nd ref=\"%d\" />\n", $thisnode;
        }
        else
        {
            my $lastnode = write_node(@$lastpoint);
            printf OSMWAY "  <seg id=\"%d\" />\n", write_segment($lastnode, $thisnode);
        }
        $lastpoint = $thispoint;
    }

    print OSMWAY "  <tag k=\"source\" v=\"PGS\" />\n";
    print OSMWAY "  <tag k=\"natural\" v=\"coastline\" />\n";
    print OSMWAY "</way>\n";
    return $current_id;
}

sub write_segment() 
{
    my ($from, $to) = @_;
    $current_id--;
    print OSMSEG "<segment id=\"$current_id\" action='create' visible='true' from=\"$from\" to=\"$to\"/>\n";
    return $current_id;
}

sub write_node() 
{
    my ($lat, $lon) = @_;
    if (defined $node_cache{"$lat,$lon"})
    {
        return $node_cache{"$lat,$lon"};
    }
    $current_id--;
    print OSM "<node id=\"$current_id\" action='create' visible='true' lat=\"$lat\" lon=\"$lon\" />\n";
    $node_cache{"$lat,$lon"} = $current_id;
    return $current_id;
}

# Haversine formula. Returns distance in metres between two points.
sub calc_distance 
{
    my ($p1, $p2) = @_;

    my ($lat1, $lon1, $lat2, $lon2) = 
        ($p1->[0] * DEGRAD, $p1->[1] * DEGRAD, $p2->[0] * DEGRAD, $p2->[1] * DEGRAD);

    my $dlon = ($lon2 - $lon1);
    my $dlat = ($lat2 - $lat1);
    my $a = (sin($dlat/2))**2 + cos($lat1) * cos($lat2) * (sin($dlon/2))**2;
    my $c = 2 * atan2(sqrt($a), sqrt(1-$a)) ;
    return RADIUS * $c;
}

sub simplify_way 
{
    my $src = shift;
    my $first = { "point" => $src->[0],
        "dist_to_next" => 0,
        "dist_to_prev" => 0,
        "error" => MAXINT
    };

    my $list = $first;

    # build double linked list
    for (@$src) 
    {
        next if ($_ == $first);
        my $item = { "point" => $_,
            "dist_to_next" => 0,
            "dist_to_prev" => calc_distance($list->{"point"}, $_),
            "error" => MAXINT,
            "prev" => $list
        };
        $list->{"dist_to_next"} = $item->{"dist_to_prev"};
        $list->{"next"} = $item;
        if (defined($list->{"prev"}))
        {
            compute_error($list);
        }
        $list = $item;
    }

    my $eliminated;
    my $min_err;

    while(1)
    {
        my $min_el;
        undef $min_err;
        for (my $i = $first; $i->{"next"}; $i=$i->{"next"})
        {
            next unless defined ($i->{"prev"});
            if ($i->{"error"} < $min_err || !defined($min_err))
            {
                $min_err = $i->{"error"};
                $min_el = $i;
            }
        }

        last if (($min_err > $max_error) || (!defined($min_err)));

        # if element found, eliminate
        if (defined($min_err) && $min_err < $max_error)
        {
            my $prev = $min_el->{"prev"};
            my $current_id = $min_el->{"next"};
            $prev->{"dist_to_next"} = calc_distance($prev->{"point"}, $min_el->{"point"}) + 
                calc_distance($current_id->{"point"}, $min_el->{"point"});
            $prev->{"next"} = $current_id;
            $current_id->{"dist_to_prev"} = $prev->{"dist_to_next"};
            $current_id->{"prev"} = $prev;
            compute_error($current_id);
            compute_error($prev);
            $eliminated++;
        }
    };

    my $newpts = [];
    for (my $i = $first; defined($i); $i=$i->{"next"})
    {
        push(@$newpts, $i->{"point"});
    }

    printf LOG "eliminated %d nodes, smallest error was %d\n", 
        $eliminated, $min_err;

    return $newpts;
}

sub compute_error
{
    my $listel = shift;
    if (!defined($listel->{"prev"}) || !defined($listel->{"next"}))
    {
        $listel->{"error"} = MAXINT;
    }
    else
    {
        $listel->{"error"} = $listel->{"dist_to_next"} + $listel->{"dist_to_prev"} - 
            calc_distance($listel->{"prev"}->{"point"}, 
                $listel->{"next"}->{"point"});
    }
}
