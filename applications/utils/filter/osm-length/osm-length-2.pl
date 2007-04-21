#!/usr/bin/perl
use strict;

# Script that computes lengths of ways in an OSM file.
#
# Reads the OSM file on stdin.
# Writes an OSM file to stdout in whcih all ways carry an additional
# tag: <d length="1234.567">, specifying the length in metres.
# The "incomplete=true" attribute is added to <d> if not all segments
# referenced by that way were present in the input file (in that case,
# the sum you get is only for the segments present).
#
# On stderr, outputs a list of all highway types encountered together 
# with total length.
#
# Does not count unwayed segments. Segments where one node is missing
# are ignored.
#
# Stores all node positions in a memory hash and will thus be unable
# to process the whole planet file unlesss you have 8 Gig of RAM or so.
#
# Author: Frederik Ramm <frederik@remote.org>. My contribution is 
# Public Domain but I grabbed the Haversine formula in calc_distance 
# from the original osm-length script, written by Jochen Topf, which
# is GPL, so the whole of this script is GPL also - if you need a
# true PD variant, re-implement the Haversine yourself.

my $nodes = {};
my $seglen = {};
my $waysum = {};

use constant PI => 4 * atan2 1, 1;
use constant DEGRAD => PI / 180;
use constant RADIUS => 6367000.0;

my $waylen;
my $hw;
my $warning;

while(<>) 
{
    if (/^\s*<node.*id=["'](\d+)['"].*lat=["']([0-9.-]+)["'] lon=["']([0-9.-]+)["']/)
    {
        $nodes->{$1}=[$2,$3];
    }
    elsif (/^\s*<segment id=['"](\d+)["'] from=['"](\d+)["'] to=["'](\d+)["']/)
    {
        if (defined($nodes->{$2}) && defined($nodes->{$3}))
        {
            $seglen->{$1}=calc_distance($nodes->{$2}, $nodes->{$3});
        }
    }
    elsif (/^\s*<way /)
    {
        $waylen = 0;
        undef $hw;
        undef $warning;
    }
    elsif (defined($waylen) && /k=.highway.\s*v=["'](.*?)["']/)
    {
        $hw = $1;
    }
    elsif (/^\s*<seg id=['"](.*)["']/)
    {
        if (defined($seglen->{$1}))
        {
            $waylen += $seglen->{$1};
        }
        else
        {
            $warning = 1;
        }
    }
    elsif ((/^\s*<\/way/) && defined($hw))
    {
        printf "   <d length='%f' %s/>\n", $waylen, $warning ? "incomplete='true' " : "";
        $waysum->{$hw} += $waylen;
    }
    print;
}


print STDERR "highway length sums (metres):\n";
my $oa;
foreach my $hw(sort { $waysum->{$b} <=> $waysum->{$a} } keys(%$waysum))
{
    printf STDERR "%-15s %10dm\n", $hw, $waysum->{$hw};
    $oa += $waysum->{$hw};
}
printf STDERR  "%-15s %10dm\n", "TOTAL", $oa;

sub calc_distance {
    my ($p1, $p2) = @_;

    my ($lat1, $lon1, $lat2, $lon2) = ($p1->[0] * DEGRAD, $p1->[1] * DEGRAD, $p2->[0] * DEGRAD, $p2->[1] * DEGRAD);

    my $dlon = ($lon2 - $lon1);
    my $dlat = ($lat2 - $lat1);
    my $a = (sin($dlat/2))**2 + cos($lat1) * cos($lat2) * (sin($dlon/2))**2;
    my $c = 2 * atan2(sqrt($a), sqrt(1-$a)) ;
    return RADIUS * $c;
}
