#!/usr/bin/perl

# All rights to this package are hereby disclaimed and its contents released 
# into the public domain by the author. Where this is not possible, you may 
# use this file under the same terms as Perl itself. 

# Originally written by Frederik Ramm <frederik@remote.org>, 14 July, 2009
# Modified by Simon Wood <simon@mungewell.org>, 14 July 2009
# Adapted to "bus stop" use case by Frederik Ramm <frederik@remote.org>, 29 September 2018


# Usage:
# bus_stop.pl SOURCE.OSM 

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

# how many metres a bus stop must be away to paint a node in red.
# 0 metres is always green, and everything between 0 and $RED_METRES
# is painted in a colour ramp
my $RED_METRES = 500;

my ($width, $height, $scale);
my ($maxlat, $minlat, $maxlon, $minlon);
my $start_time;
my $nodes = {};
my @busstop_ids;
my @busstop_coords;

my $input_file = $ARGV[0];

parse_file($input_file);
analyze_graph();

timer_start("find_nearest_nodes for bus stops");
foreach (@busstop_coords)
{
    push(@busstop_ids, find_nearest_node(@$_));
}
timer_stop();

flood_fill();
draw_graph();


# -------------------------------------------------------------------------------
# find_nearest_node()
#
# finds and returns the node (id) that is nearest to given position.
# (does not use proper great circle formula, just EPSG4326 distance)
# -------------------------------------------------------------------------------

sub find_nearest_node
{
    my ($lat, $lon) = @_;
    my $best_dist = 999999;
    my $best_node;

    foreach my $node(values %$nodes)
    {
        my $d = ($node->{lat}-$lat)**2 + ($node->{lon}-$lon)**2;
        if ($d < $best_dist)
        {
            $best_dist = $d;
            $best_node = $node->{id};
        }
    }
    return $best_node;
}

# -------------------------------------------------------------------------------
# draw_graph()
#
# makes a PNG file from the graph.
# -------------------------------------------------------------------------------

sub draw_graph
{
    timer_start("draw_graph");
    $width = 2048;
    $scale = $width / ($maxlon-$minlon) / 10000;
    $height = (ProjectF($maxlat) - ProjectF($minlat)) * 180 / pi * 10000 * $scale;

    my $pic = new GD::Image($width, $height);
    my $gray = $pic->colorAllocate(128, 128, 128);
    my $white = $pic->colorAllocate(255, 255, 255);
    my $black = $pic->colorAllocate(0, 0, 0);

    # define a 20-color ramp from green to red
 	my @colors;
	unshift @colors, $pic->colorAllocate(240,0,0);
	unshift @colors, $pic->colorAllocate(240,6,0);
	unshift @colors, $pic->colorAllocate(243,27,0);
	unshift @colors, $pic->colorAllocate(248,69,0);
	unshift @colors, $pic->colorAllocate(250,90,0);
	unshift @colors, $pic->colorAllocate(254,131,0);
	unshift @colors, $pic->colorAllocate(254,150,0);
	unshift @colors, $pic->colorAllocate(253,168,0);
	unshift @colors, $pic->colorAllocate(251,206,0);
	unshift @colors, $pic->colorAllocate(250,225,0);
	unshift @colors, $pic->colorAllocate(250,243,0);
	unshift @colors, $pic->colorAllocate(212,244,0);
	unshift @colors, $pic->colorAllocate(189,241,0);
	unshift @colors, $pic->colorAllocate(167,238,0);
	unshift @colors, $pic->colorAllocate(122,232,0);
	unshift @colors, $pic->colorAllocate(99,229,0);
	unshift @colors, $pic->colorAllocate(83,224,0);
	unshift @colors, $pic->colorAllocate(49,214,0);
	unshift @colors, $pic->colorAllocate(33,209,0);
	unshift @colors, $pic->colorAllocate(0,200,0);

    $pic->filledRectangle(0, 0, $width, $height, $white);

    # draw basic nodes grey lines
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

    # draw coloured nodes depending on their "dist" value
    foreach my $node(values %$nodes)
    {
        next unless defined($node->{dist});
        my $d=int($node->{dist})/$RED_METRES*scalar @colors;
        $d=scalar @colors - 1 if ($d>=scalar @colors);
        my $pos1 = project($node);
        $pic->filledRectangle($pos1->[0]-2, $pos1->[1]-2, $pos1->[0]+2, $pos1->[1]+2, $colors[$d]);
    }

    open(PNG, ">graph.png") or die;
    print PNG $pic->png;
    close(PNG);
    timer_stop();
}

# -------------------------------------------------------------------------------
# analyze_graph()
#
# computes max/min lon/lat.
# -------------------------------------------------------------------------------

sub analyze_graph
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
# recursive flood-filling of the graph. starts at given start nodes and assigns
# distance of 0; then visits all neighbours and updates their respective dists
# with the currnet dist plus dist to get there. If a neighbour already has a
# distance, overwrite it only if the distance we come in with is smaller.
# Add any node that we modified the distance of to the list of nodes to be 
# processed. This will automatically terminate once we do not reach any nodes
# that have not yet been reached (or we reach them but cannot reduce distance).
# -------------------------------------------------------------------------------

sub flood_fill
{
    my ($startnode, $remain_dist) = @_;
    timer_start("flood_fill");
    
    # initialize queue with all bus stops, setting their distance to 0
    my @queue = map( { $nodes->{$_}->{dist} = 0; $nodes->{$_} } @busstop_ids);

    # take one node at a time from start of queue and process. note that 
    # as a result, new nodes can be added to the end of the queue
    while (my $node = shift @queue)
    {
        my $d = $node->{dist};
        # process all neighbours i.e. nodes connected to this by a path
        foreach my $neigh(@{$node->{nb}})
        {
            next if ($neigh->{len}<=0);
            # this is the distance value we would set for this node
            my $newd = $d + $neigh->{len};
            # if the neighbour has no distance set yet, or has a larger distance,
            # set distance and add neighbour to queue for further processing
            if (!defined($neigh->{nd}{dist}) || $newd < $neigh->{nd}{dist})
            {
                $neigh->{nd}{dist} = $newd;
                push(@queue, $neigh->{nd});
            }
        }
    }
    timer_stop();
}

# -------------------------------------------------------------------------------
# parse_file
#
# this is ugly as hell, essentially parses an OSM XML file and records all nodes
# connected to highways and makes a list of their neighbours on the network.
# the resulting data structure is a perl hash that, for each node, records its
# lat and lon and id, as well as an array of (pointers to) neighbour nodes.
# ways are only processed here to identify the neighbours of a node, they are not
# part of the final data structure.
# -------------------------------------------------------------------------------

sub parse_file
{
    my $filename = shift;
    my $in = new IO::File $filename, "r" or die "cannot open $filename";
    my $buffer;
    my $edge = {};
    my $waynodes;
    my $hwtype;

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
		# need to check for "action='delete'"
                $waynodes = [];
                undef $hwtype;
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
                    $edge->{$waynodes->[$i-1]}{$waynodes->[$i]} = 1;
                    $edge->{$waynodes->[$i]}{$waynodes->[$i-1]} = 1;
                }
            }
        }
    }
    timer_stop();

    # ways now parsed.

    $in->seek(0, SEEK_SET);
    undef $buffer;

    timer_start("parsing nodes");
    my ($lastnodeid, $lat, $lon);
    while(<$in>)
    {
        $buffer .= $_;
        while($buffer =~ /^\s*([^<]*?)\s*(<[^>]*>)(.*)$/)
        {
	    # need to check for "action='delete'"
            $buffer = $3;
            my $xmltag = $2;
            if ($xmltag =~ /^\s*<node.*\sid=["'](\d+)['"].*lat=["']([0-9.Ee-]+)["'].*lon=["']([0-9.Ee-]+)["']/)
            {
                $lastnodeid=$1;
                $lat=$2;
                $lon=$3;
                if (defined($edge->{$lastnodeid}))
                {
                    # we want this node
                    $nodes->{$lastnodeid} = { lat=>$lat, lon=>$lon, id=>$lastnodeid };
                }
            }
            elsif ($xmltag =~ /^\s*<tag k=['"]highway["'].*v=["']bus_stop['"]/)
            {
                if (defined($nodes->{$lastnodeid}))
                {
                    # node is already connected to road network
                    push (@busstop_ids, $lastnodeid);
                }
                else
                {
                    # unconnected node, must find nearest node later
                    push (@busstop_coords, [ $lat, $lon ]);
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

