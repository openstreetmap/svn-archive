#!/usr/bin/perl

# This script filters gps-points csv file by a set of polygons.
# Written by Ilya Zverev, licensed under WTFPL.
# Poly reading sub is from extract_polygon.pl by Frederik Ramm.

use strict;
use Math::Polygon::Tree;
use Getopt::Long;
use File::Basename;
use File::Path 2.07 qw(make_path);
use IO::Compress::Gzip;
use IO::File;

my $factor = 10000000;

my $help;
my $verbose;
my $infile = '-';
my $target;
my $suffix = '';
my $polylist;
my $polyfile;
my $bbox;
my $zip;
my $nodupes;

GetOptions('h|help' => \$help,
           'v|verbose' => \$verbose,
           'i|input=s' => \$infile,
           'o|target=s' => \$target,
           's|suffix=s' => \$suffix,
           'l|list=s' => \$polylist,
           'p|polyfile=s' => \$polyfile,
           'b|bbox=s' => \$bbox,
           'z|gzip' => \$zip,
           'd|nodupes' => \$nodupes,
           ) || usage();

if( $help ) {
  usage();
}

my $minlon = 999;
my $minlat = 999;
my $maxlat = -999;
my $maxlon = -999;
my $bboxset = 0;
my @polygons = ();

if( $bbox ) {
    # simply set minlat, maxlat etc from bbox parameter - no polygons
    ($minlon, $minlat, $maxlon, $maxlat) = split(",", $bbox);
    die ("badly formed bounding box - use four comma-separated values for left longitude, ".
        "bottom latitude, right longitude, top latitude") unless defined($maxlat);
    die ("max longitude is less than min longitude") if ($maxlon < $minlon);
    die ("max latitude is less than min latitude") if ($maxlat < $minlat);
    $bboxset = 1;
}

if( $polylist ) {
    open LIST, "<$polylist" or die "Cannot open $polylist: $!";
    $target = '.' if !$target;
    make_path($target);
    while(<LIST>) {
        chomp;
        add_polygon_file($_, 1);
    }
    close LIST;
} elsif( $polyfile ) {
    add_polygon_file($polyfile, 0);
} elsif( $bbox ) {
    printf STDERR "bbox -> %s\n", $target || 'STDOUT' if $verbose;
    my $fh = $target ? ($zip ? new IO::Compress::Gzip $target : IO::File->new($target, 'w'))
        : IO::File->new_from_fd(*STDOUT, '>');
    die "Cannot open file: $!" if !$fh;
    push @polygons, [$fh, poly_from_bbox()];
} else {
    usage("Please specify either bbox, polygon file or a list of them.");
}

$minlat *= $factor;
$minlon *= $factor;
$maxlat *= $factor;
$maxlon *= $factor;

open CSV, "<$infile" or die "Cannot open $infile: $!\n";
my $lastline = '';
while(<CSV>) {
    if (/^(-?\d+),(-?\d+)/)
    {
        next if $nodupes && ($lastline eq $_);
        next if $1 < $minlat || $1 > $maxlat || $2 < $minlon || $2 > $maxlon;
        for my $poly (@polygons) {
            $poly->[0]->print($_) if is_in_poly($poly->[1], $1, $2);
        }
        $lastline = $_;
    }
}
close CSV;

close $_->[0] for @polygons;

sub add_polygon_file {
    my ($p, $multi) = @_;
    my $filename = $multi ? ($target ? $target.'/'.basename($p) : basename($p)) : $target;
    $filename =~ s/\.poly$/$suffix\.csv/;
    $filename .= '.gz' if $zip && $filename !~ /\.gz$/;
    print STDERR "$p -> $filename\n" if $verbose;
    my $bp = read_poly($p);
    my $fh = $target ? ($zip ? new IO::Compress::Gzip $filename : IO::File->new($filename, 'w'))
        : IO::File->new_from_fd(*STDOUT, '>');
    die "Cannot open file: $!" if !$fh;
    push @polygons, [$fh, $bp];
}

sub is_in_poly {
    my ($borderpolys, $lat, $lon) = @_;
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
            if( $#{$currentpoints} > 0 ) {
                # close polygon if it isn't
                push(@{$currentpoints}, [$currentpoints->[0][0], $currentpoints->[0][1]])
                    if $currentpoints->[0][0] != $currentpoints->[-1][0]
                    || $currentpoints->[0][1] != $currentpoints->[-1][1];
                my $pol = Math::Polygon::Tree->new($currentpoints);
                push(@{$borderpolys}, [$pol,$invert,[$pol->bbox]]);
                printf STDERR "Added polygon: %d points (%d,%d)-(%d,%d) %s\n",
                    scalar(@{$currentpoints}), @{$borderpolys->[-1][2]},
                    ($borderpolys->[-1][1] ? "exclude" : "include") if $verbose;
            }
            undef $currentpoints;
        }
        elsif (defined($currentpoints))
        {
            /^\s+([0-9.E+-]+)\s+([0-9.E+-]+)/ or die "Incorrent line in poly: $_";
            push(@{$currentpoints}, [int($2*$factor), int($1*$factor)]);
            if( !$bboxset ) {
                $minlat = $2 if ($2 < $minlat);
                $maxlat = $2 if ($2 > $maxlat);
                $minlon = $1 if ($1 < $minlon);
                $maxlon = $1 if ($1 > $maxlon);
            }
        }
    }
    close (PF);
    return $borderpolys;
}

sub poly_from_bbox {
    my @b = (int($minlat*$factor), int($minlon*$factor), int($maxlat*$factor), int($maxlon*$factor));
    my $currentpoints = [[$b[0], $b[1]], [$b[0], $b[3]], [$b[2], $b[3]], [$b[2], $b[1]], [$b[0], $b[1]]];
    my $pol = Math::Polygon::Tree->new($currentpoints);
    my $bp = [];
    push(@{$bp}, [$pol,0,[$pol->bbox]]);
    return $bp;
}

sub usage {
    my ($msg) = @_;
    print STDERR "$msg\n\n" if defined($msg);

    my $prog = basename($0);
    print STDERR << "EOF";
This script receives CSV file of "lat,lon" and filters it by a bounding
box, an osmosis' polygon filter file or a number of them.

usage: $prog [-h] [-i infile] [-o target] [-z]
           [-b bbox] [ -p poly | -l list [-s suffix] ]

 -h        : print ths help message and exit.
 -i infile : CSV points file to process (default is STDIN).
 -o target : a file or a directory to put resulting files.
 -z        : compress output file with gzip.
 -b bbox   : limit points by bounding box (four comma-separated
             numbers: minlon,minlat,maxlon,maxlat).
 -p poly   : a polygon filter file.
 -l list   : file with names of poly files.
 -s suffix : a suffix to add to all output files.
 -d        : remove duplicate consecutive points.
 -v        : print debug messages.

All coordinates in source CSV file should be multiplied by $factor
(you can change this number in the code). Please keep the number
of polygons below 200, or unexpected problems may occur.

EOF
    exit;
}
