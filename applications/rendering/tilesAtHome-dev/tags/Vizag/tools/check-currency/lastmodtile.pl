#!/usr/bin/perl

# This program takes one argument - the name of an osm file, 
# usually the planet file.

# After completion, it writes a list of all level-12 "slippy map" tiles
# which contain at least one node to stdout, in the form
#
# x y lastmod
#
# where lastmod is the timestamp of the most recent modification whitin 
# this tile's area.
# 
# To consume as little memory as possible, this script will read through
# the planet file three times, each time grabbing the information required,
# instead of reading it only once and gobbling up anything that might be
# needed later.
#
# Note that if you have segments which run through a level-12 tile (i.e.
# begin in tile 1, run through tile 2 and end in tile 3), then this script
# will not detect that tile 2 is affected by a change in the segment. 
# A segment would have to be longer than 10 kilometres for that to happen
# (roughly; less near the poles).
#
# Also, a change is not detected if a data change on a neighbouring
# tile results in a rendering change (e.g. tile 2 paints a big city
# name "Nottingham", of which the "No" lie on neighbouring tile 1; 
# tile 2 city name is changed to "Rottingham", tile 1 does not detect
# a change).

# Author Frederik Ramm <frederik@remote.org> / public domain

use strict;
use Time::Local 'timegm_nocheck';
use Math::Trig;

my $file = $ARGV[0]; # will need to open this multiple times
die("Please specify the name of the planet file on the command line.") if ($file eq "");

my %seg_last_modified;
my %node_last_modified;
my %tile_last_modified;

# Step 1: Read way data
#
# We are not actually interested in ways; we just use the way's last-
# modified date as the preliminary last-modified date for each of the 
# segments contained.

open(PLANET, "$file") or die "Cannot open $file for reading";
print STDERR "reading ways...\n";
my $c = 0;
while(<PLANET>) 
{
    if (/^  <way id="(\d+)"( )(timestamp=")(\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)([+-])(\d\d):(\d\d)/)
    {
        my $lm;
        if ($10 eq "+") 
        {
            $lm = timegm_nocheck($9, $8+$12, $7+$11, $6, $5-1, $4);
        }
        else
        {
            $lm = timegm_nocheck($9, $8-$12, $7-$11, $6, $5-1, $4);
        }
        if (($lm<1072911600) or ($lm>1199142000)) 
        {
            print STDERR "spurious time spec on line $. of input: $_";
        } 
        else
        {
            while(<PLANET>) 
            {
                last if (/^  <\/way>/);
                if (/^    <seg id="(\d+)/) 
                {
                    $c++;
                    $seg_last_modified{$1} = $lm if ($seg_last_modified{$1}<$lm);
                }
            }
        }
    }
    printf STDERR "\r%dm lines...",$./1000000 if($.%1000000==0);
}
close PLANET;
printf STDERR "\r%d segment references extracted from %d ways\n", 
    scalar(keys(%seg_last_modified)), $c;

# Step 2: Read segment data
#
# Find the maximum of the segment's own last-modified date and the
# last-modified date of any way that uses the segment (as pre-loaded
# in step #1). Store this date as the preliminary last-modified date
# for each of the nodes used by the segment.

print STDERR "\rreading segments...\n";
my $c = 0;
open(PLANET, "$file") or die "Cannot open $file for reading";
while(<PLANET>) 
{
    if (/^  <segment id="(\d+)" from="(\d+)" to="(\d+)" timestamp="(\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)([+-])(\d\d):(\d\d)/)
    {
        my $lm;
        if ($10 eq "+") 
        {
            $lm = timegm_nocheck($9, $8+$12, $7+$11, $6, $5-1, $4);
        }
        else
        {
            $lm = timegm_nocheck($9, $8-$12, $7-$11, $6, $5-1, $4);
        }
        if (($lm<1072911600) or ($lm>1199142000)) 
        {
            print STDERR "spurious time spec on line $. of input: $_";
        } 
        else
        {
            $c++;
            if (defined($seg_last_modified{$1}))
            {
                $lm = $seg_last_modified{$1} if ($lm< $seg_last_modified{$1});
                delete $seg_last_modified{$1};
            }
            $node_last_modified{$2}=$lm if ($node_last_modified{$2}<$lm);
            $node_last_modified{$3}=$lm if ($node_last_modified{$3}<$lm);
        }
    }
    elsif(/^  <way/)
    {
        last;
    }
    printf STDERR "\r%dm lines...",$./1000000 if($.%1000000==0);
}
close PLANET;
printf STDERR "\r%d node references extracted from %d segments\n", 
    scalar(keys(%node_last_modified)), $c;

undef %seg_last_modified;

# Step 3: Read node data
#
# Find the maximum of the node's own last-modified date and the last-
# modified date of any segment using that node; compute the tile containing
# the node; and if the tile's last-modified date is older that the node's,
# update the tile's.

open(PLANET, "$file") or die "Cannot open $file for reading";
print STDERR "reading nodes...\n";
my $c=0;
while(<PLANET>) 
{
    if (/^  <node id="(\d+)" lat="([0-9.-]+)" lon="([0-9.-]+)" timestamp="(\d{4})-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)([+-])(\d\d):(\d\d)/)
    {
        my $lm;
        if ($10 eq "+") 
        {
            $lm = timegm_nocheck($9, $8+$12, $7+$11, $6, $5-1, $4);
        }
        else
        {
            $lm = timegm_nocheck($9, $8-$12, $7-$11, $6, $5-1, $4);
        }
        if (($lm<1072911600) or ($lm>1199142000)) 
        {
            print STDERR "spurious time spec on line $. of input: $_";
        } 
        else
        {
            $c++;
            if (defined($node_last_modified{$1}))
            {
                $lm = $node_last_modified{$1} if ($lm<$node_last_modified{$1});
                delete $node_last_modified{$1};
            }
            my $tile = z12tile($2, $3);
            $tile_last_modified{$tile} = $lm if ($tile_last_modified{$tile} < $lm);
        }
    }
    elsif(/^  <segment/)
    {
        last;
    }
    printf STDERR "\r%dm lines...",$./1000000 if($.%1000000==0);
}
close PLANET;

undef %node_last_modified;

printf STDERR "\r%d tile references extracted from %d nodes\n", 
    scalar(keys(%tile_last_modified)), $c;

print STDERR "generating output...\n";

foreach my $tile(keys %tile_last_modified) 
{
    print $tile." ".$tile_last_modified{$tile}."\n";
}

# Computes level-12 tile co-ordinates for given lat/lon.
# (replace 4096 by 2 ** z if you need another zoom level)
#
# Based on PHP code by Oliver White.
sub z12tile {

    my ($lat, $lon) = @_;

    my $px = ($lon+180)/360;
    $lat = ($lat / 180 * pi);
    my $projectf = log(Math::Trig::tan($lat) + (1/cos($lat)));
    my $py = (pi - $projectf) / 2 / pi;
    return sprintf("%d %d", $px * 4096, $py * 4096);
}
