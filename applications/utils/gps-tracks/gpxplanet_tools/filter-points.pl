#!/usr/bin/perl

# This script filters gps-points csv file by a polygon
# Written by Ilya Zverev, licensed under WTFPL.
# Poly reading sub is from extract_polygon.pl by Frederik Ramm.

use strict;
use Math::Polygon;
use Getopt::Long;
use File::Basename;

my $borderpolys;
my $factor = 10000000;
my $minlon = 999;
my $minlat = 999;
my $maxlat = -999;
my $maxlon = -999;

my $help;
my $infile = '-';
my $outfile = '-';
my $bbox;
my $polyfile;
my $nodupes = 0;

GetOptions('h|help' => \$help,
           'i|input=s' => \$infile,
           'o|output=s' => \$outfile,
           'b|bbox=s' => \$bbox,
           'p|poly=s' => \$polyfile,
           'd|nodupes' => \$nodupes,
           ) || usage();

if( $help ) {
  usage();
}

if( $polyfile ) {
    $borderpolys = read_poly($polyfile);
}
elsif( $bbox ) {
    # simply set minlat, maxlat etc from bbox parameter - no polygons
    ($minlat, $minlon, $maxlat, $maxlon) = split(",", $bbox);
    die ("badly formed bounding box - use four comma-separated values for ".
        "bottom left latitude, bottom left longitude, top right latitude, ".
        "top right longitude") unless defined($maxlon);
    die ("max longitude is less than min longitude") if ($maxlon < $minlon);
    die ("max latitude is less than min latitude") if ($maxlat < $minlat);
}
else {
    die "Please specify either bounding box or polygon file.";
}

$minlat *= $factor;
$minlon *= $factor;
$maxlat *= $factor;
$maxlon *= $factor;

open CSV, "<$infile" or die "Cannot open $infile: $!\n";
open OUT, ">$outfile" or die "Cannot open $outfile for writing: $!\n";
my $lastline = '';
while(<CSV>) {
    if (/^(-?\d+),(-?\d+),(-?\d+(?:\.\d+)?)/)
    {
        print OUT if (!$nodupes || ($lastline ne $_)) && is_in_poly($1, $2);
        $lastline = $_;
    }
}
close OUT;
close CSV;

sub is_in_poly {
    my ($lat, $lon) = @_;
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
}

sub read_poly {
    my $polyfile = shift;
    my $borderpolys = [];
    my $currentpoints;
    open (PF, "<$polyfile") || die "Could not open $polyfile: $!";

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
            push(@{$borderpolys}, [$pol,$invert,[$pol->bbox]]) unless $pol->nrPoints == 0;
                printf STDERR "Added polygon: %d points (%d,%d)-(%d,%d) %s\n",
                    $borderpolys->[-1][0]->nrPoints,
                    @{$borderpolys->[-1][2]},
                    ($borderpolys->[-1][1] ? "exclude" : "include") unless $pol->nrPoints == 0;
            undef $currentpoints;
        }
        elsif (defined($currentpoints))
        {
            /^\s+([0-9.E+-]+)\s+([0-9.E+-]+)/ or die "Incorrent line in poly: $_";
            push(@{$currentpoints}, [int($2*$factor), int($1*$factor)]);
            $minlat = $2 if ($2 < $minlat);
            $maxlat = $2 if ($2 > $maxlat);
            $minlon = $1 if ($1 < $minlon);
            $maxlon = $1 if ($1 > $maxlon);
        }
    }
    close (PF);
    return $borderpolys;
}

sub usage {
    my $prog = basename($0);
    print STDERR << "EOF";
This script receives CSV file of "lat,lon" and filters it by bounding
box or osmosis' polygon file.

usage: $prog [-h] [-i infile] [-o outfile] [-b bbox] [-p poly]

 -h         : print ths help message and exit.
 -i infile  : CSV points file to process (default is STDIN).
 -o outfile : directory to store bitiles.
 -b bbox    : limit points by bounding box (four comma-separated
              numbers: minlon,minlat,maxlon,maxlat).
 -p poly    : limit points by polygon in .poly file.
 -d         : remove duplicate consecutive points.

All coordinates in source CSV file should be multiplied by $factor
(you can change this number in the code).

EOF
    exit;
}
