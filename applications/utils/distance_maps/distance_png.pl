#!/usr/bin/perl

# All rights to this package are hereby disclaimed and its contents released 
# into the public domain by the author. Where this is not possible, you may 
# use this file under the same terms as Perl itself. 

# Originally written by Frederik Ramm <frederik@remote.org>, 14 July, 2009
# Modified by Simon Wood <simon@mungewell.org>, 14 July 2009

# Usage:
# distance.pl SOURCE.OSM <target_lat> <target_lon> [<interpolation>]
#
# interpolation  0 = all nodes in SOURCE.OSM
# interpolation -1 = junction/terminus nodes in SOURCE.OSM
# interpolation >0 = all nodes in SOURCE.OSM, plus interpolated nodes seperated
#                    by no more than 'X'm

# NODES:
#   * $nodes = { "1234" => { "lat" => 1.2, "lon" => "2.3", "nb" => [
#       { "node" => ..., "len" => 1.2345 }, ... }, ... }
#     
# WAYS:
#   * not stored, only processed to build the node neighbour
#     array.
#
# -----------------------------------------------------------------------------

use strict;
use warnings;
use bytes;

use GD;
use IO::Seekable;
use Math::Trig qw(great_circle_distance deg2rad pi tan sec);

my ($maxlat, $minlat, $maxlon, $minlon);
my ($width, $height, $scale);
my $start_time;
my $nodes = {};

my $output_file = "output.png";
my $input_file = $ARGV[0];
my $start_lat = $ARGV[1];
my $start_lon = $ARGV[2];

my $max_between = $ARGV[3] + 0;
my $newnode = -1;

# read into memory
parse_file($input_file);
analyze_graph();

# must have an OSM node to start at, but only lat/lon given above, so 
# find nearest (caution 
my $startnode = find_nearest_node($start_lat, $start_lon);
# the 2000 is the distance in metres from starting point
flood_fill($startnode, 2000);
draw_graph(20000);

# -------------------------------------------------------------------------------
# draw_graph()
#
# makes a PNG file from the graph.
# 
# parameters:
# * startnode - node reference to highlight in graph
# -------------------------------------------------------------------------------

sub draw_graph
{
    timer_start("draw_graph");
    $width = 1024;
    $scale = $width / ($maxlon-$minlon) / 10000;
    $height = (ProjectF($maxlat) - ProjectF($minlat)) * 180 / pi * 10000 * $scale;
    my $maxdist = shift;

    my $pic = new GD::Image($width, $height);
    my $gray = $pic->colorAllocate(128, 128, 128);
    my $white = $pic->colorAllocate(255, 255, 255);
    my $black = $pic->colorAllocate(0, 0, 0);

    my $colors = [
        $pic->colorAllocate(255, 0, 0),
        $pic->colorAllocate(128, 0, 0),
    ];
    my $colindex = 0;

    $pic->filledRectangle(0, 0, $width, $height, $white);

    # draw grey lines
    foreach my $node(values %$nodes)
    {
        my $pos1 = project($node);
        $pic->filledRectangle($pos1->[0]-1, $pos1->[1]-1, $pos1->[0]+1, $pos1->[1]+1, $gray);
        foreach my $other(@{$node->{nb}})
        {
            my $pos2 = project($other->{nd});
            $pic->line(@$pos1, @$pos2, $gray);
        }
    }

    $colindex=0;
    foreach my $node(values %$nodes)
    {
        next unless defined($node->{dist});
        if ($max_between>-1 || @{$node->{nb}}!=2) 
        {
            my $color = $colors->[$colindex + 1];

            if (@{$node->{nb}} < 2 )
            {
                # select brighter color for terminus nodes
                $color = $colors->[$colindex + 0];
            }


            my $pos1 = project($node);
            $pic->filledRectangle($pos1->[0]-2, $pos1->[1]-2, $pos1->[0]+2, $pos1->[1]+2, $color);
        }
    }

    open(PNG, ">$output_file") or die;
    print PNG $pic->png;
    close(PNG);
    timer_stop();
}

# -------------------------------------------------------------------------------
# analyze_graph()
#
# computes max/min lon/lat.
# -------------------------------------------------------------------------------
sub analyze_graph()
{
    timer_start("analyze_graph");
    $minlon = 999; 
    $minlat = 999;
    $maxlat = -999;
    $maxlon = -999;

    foreach my $node(values %$nodes)
    {
        $maxlat = $node->{lat} if ($node->{lat} > $maxlat);
        $minlat = $node->{lat} if ($node->{lat} < $minlat);
        $maxlon = $node->{lon} if ($node->{lon} > $maxlon);
        $minlon = $node->{lon} if ($node->{lon} < $minlon);
    }
    timer_stop();

}

# -------------------------------------------------------------------------------
# find_nearest_node()
#
# finds and returns the node (reference) that is nearest to given position.
# (does not use proper great circle formula, just EPSG4326 distance)
# -------------------------------------------------------------------------------
sub find_nearest_node
{
    timer_start("find_nearest_node");
    my ($lat, $lon) = @_;
    my $best_dist = 999999;
    my $best_node;

    foreach my $node(values %$nodes)
    {
        my $d = ($node->{lat}-$lat)**2 + ($node->{lon}-$lon)**2;
        if ($d < $best_dist)
        {
            $best_dist = $d;
            $best_node = $node;
        }
    }
    timer_stop();
    return $best_node;
}

sub timer_start
{
    my $current_task = shift;
    $start_time = time();
    $|=1;
    print "$current_task...";
}

sub timer_stop
{
    my $elaps = time()-$start_time;
    printf " %ds\n", $elaps;
}

sub project
{
    my $node = shift;
    return [
        $width - ($maxlon-$node->{lon})*10000*$scale, 
        $height + (ProjectF($minlat)-ProjectF($node->{lat})) * 180/pi * 10000 * $scale 
    ];
}

sub ProjectF
{
    my $Lat = shift() / 180 * pi;
    my $Y = log(tan($Lat) + sec($Lat));
    return($Y);
}

# -------------------------------------------------------------------------------
# flood_fill()
#
# recursive flood-filling of the graph. starts at given start node and assigns
# remaining dist; then visits all neighbours and updates their respective dists
# with the remaining dist minus dist to get there.
# -------------------------------------------------------------------------------
sub flood_fill
{
    my ($startnode, $remain_dist) = @_;
    timer_start("flood_fill");
    my $queue = [ $startnode ];
    $startnode->{dist} = 0;
    while (my $node = shift @$queue)
    {
        my $d = $node->{dist};
        foreach my $neigh(@{$node->{nb}})
        {
            next if ($neigh->{len}<=0);
            my $newd = $d + $neigh->{len};
            # we still have distance left to travel to that node
            if (!defined($neigh->{nd}{dist}) || $newd < $neigh->{nd}{dist})
            {
                # the node has not been visited, or has been visited
                # on a longer journey.
                $neigh->{nd}{dist} = $newd;
                push(@$queue, $neigh->{nd});
            }
        }
    }
    timer_stop();
}

sub parse_file
{
    my $filename = shift;
    my $in = new IO::File $filename, "r" or die "cannot open $filename";
    my $buffer;
    my $edge = {};
    my $waynodes;
    my $hwtype;
    my $weight_forward;
    my $weight_backward;

    timer_start("parsing ways");
    while(<$in>)
    {
        $buffer .= $_;
        while($buffer =~ /^\s*([^<]*?)\s*(<[^>]*>)(.*)$/)
        {
            $buffer = $3;
            my $xmltag = $2;
            if ($xmltag =~ /^\s*<way /)
            {
                $waynodes = [];
                undef $hwtype;
                $weight_forward=1;
                $weight_backward=1;
            }
            elsif ($xmltag =~ /^\s*<\/osm/)
            {
                last;
            }
            elsif ($xmltag =~ /^\s*<relation/)
            {
                last;
            }
            elsif ($xmltag =~ /^\s*<tag k=['"]highway["'].*v=["'](.*)['"]/)
            {
                $hwtype = $1;
            }
            elsif ($xmltag =~ /^\s*<tag k=['"]oneway["'].*v=["'](.*)['"]/)
            {
                $weight_backward=-1;
            }
            elsif ($xmltag =~ /^\s*<nd ref=['"](\d+)["']/)
            {
                push(@$waynodes, $1);
            }
            elsif ($xmltag =~ /^\s*<\/way/)
            {
                next unless defined $hwtype;
                # this is where you would check whether $hwtype contains a way
                # you want considered, and do "next" if not
                for (my $i=1; $i<scalar(@$waynodes); $i++)
                {
                    # you could also assign different weights to different kinds of 
                    # ways but for now we treat them all the same
                    $edge->{$waynodes->[$i-1]}{$waynodes->[$i]} = $weight_forward;
                    $edge->{$waynodes->[$i]}{$waynodes->[$i-1]} = $weight_backward;
                }
            }
        }
    }
    timer_stop();

    # ways now parsed.

    $in->seek(0, SEEK_SET);
    undef $buffer;

    timer_start("parsing nodes");
    while(<$in>)
    {
        $buffer .= $_;
        while($buffer =~ /^\s*([^<]*?)\s*(<[^>]*>)(.*)$/)
        {
            $buffer = $3;
            my $xmltag = $2;
            if ($xmltag =~ /^\s*<node.*\sid=["'](\d+)['"].*lat=["']([0-9.Ee-]+)["'].*lon=["']([0-9.Ee-]+)["']/)
            {
                if (defined($edge->{$1}))
                {
                    # we want this node
                    $nodes->{$1} = { lat=>$2, lon=>$3, id=>$1 };
                }
            }
            elsif ($xmltag =~ /^\s*<way /)
            {
                last;
            }
        }
    }
    timer_stop();

    $in->close();

    # nodes now parsed, but lengths of edges still need to be computed.

    timer_start("computing edge lengths");
    foreach my $nid(keys %$edge)
    {
        my $node = $nodes->{$nid};
        die "node $nid referenced in way but not present" unless defined $node;
        foreach my $othernid(keys(%{$edge->{$nid}}))
        {
            my $other = $nodes->{$othernid};
            die "node $othernid referenced in way but not present" unless defined $other;
            my $dist = great_circle_distance(deg2rad($node->{lon}), deg2rad(90-$node->{lat}),
                deg2rad($other->{lon}), deg2rad(90-$other->{lat}), 6378135); # gives metres
            $nodes->{$nid}{nb} = [] unless defined $nodes->{$nid}{nb};
            push(@{$nodes->{$nid}{nb}}, { nd => $other, len => $dist * $edge->{$nid}{$othernid} });
        }
    }
    timer_stop();
}

