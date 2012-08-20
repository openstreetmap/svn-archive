#!/usr/bin/perl

# This script filters gps-points csv file by a set of polygons.
# Written by Ilya Zverev, licensed under WTFPL.
# Poly reading sub is from extract_polygon.pl by Frederik Ramm.

use strict;
use Math::Polygon;
use Getopt::Long;
use File::Basename;
use File::Path 2.07 qw(make_path);
use IO::Compress::Gzip;
use IO::File;

my $factor = 10000000;

my $help;
my $verbose;
my $infile = '-';
my $target = '.';
my $suffix = '';
my $polylist;
my $zip;
my $nodupes;

GetOptions('h|help' => \$help,
           'v|verbose' => \$verbose,
           'i|input=s' => \$infile,
           'o|target=s' => \$target,
           's|suffix=s' => \$suffix,
           'l|list=s' => \$polylist,
           'z|gzip' => \$zip,
           'd|nodupes' => \$nodupes,
           ) || usage();

if( $help ) {
  usage();
}

usage("Please specify list file with -l") if !$polylist;

open LIST, "<$polylist" or die "Cannot open $polylist: $!";
make_path($target);
my @polygons = ();
while(<LIST>) {
    chomp;
    my $p = $_;
    my $filename = $target.'/'.basename($p);
    $filename =~ s/\.poly$/$suffix\.csv/;
    $filename .= '.gz' if $zip;
    print STDERR "$p -> $filename\n" if $verbose;
    my $bp = read_poly($p);
    my $fh = $zip ? new IO::Compress::Gzip $filename : IO::File->new($filename, 'w');
    die "Cannot open file: $!" if !$fh;
    push @polygons, [$fh, $bp];
}
close LIST;

open CSV, "<$infile" or die "Cannot open $infile: $!\n";
my $lastline = '';
while(<CSV>) {
    if (/^(-?\d+),(-?\d+),(-?\d+(?:\.\d+)?)/)
    {
        next if $nodupes && ($lastline eq $_);
        for my $poly (@polygons) {
            $poly->[0]->print($_) if is_in_poly($poly->[1], $1, $2);
        }
        $lastline = $_;
    }
}
close CSV;

close $_->[0] for @polygons;

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
            my $pol = Math::Polygon->new(points => $currentpoints);
            push(@{$borderpolys}, [$pol,$invert,[$pol->bbox]]) unless $pol->nrPoints == 0;
                printf STDERR "Added polygon: %d points (%d,%d)-(%d,%d) %s\n",
                    $borderpolys->[-1][0]->nrPoints,
                    @{$borderpolys->[-1][2]},
                    ($borderpolys->[-1][1] ? "exclude" : "include") if $pol->nrPoints > 0 && $verbose;
            undef $currentpoints;
        }
        elsif (defined($currentpoints))
        {
            /^\s+([0-9.E+-]+)\s+([0-9.E+-]+)/ or die "Incorrent line in poly: $_";
            push(@{$currentpoints}, [int($2*$factor), int($1*$factor)]);
        }
    }
    close (PF);
    return $borderpolys;
}

sub usage {
    my ($msg) = @_;
    print STDERR "$msg\n\n" if defined($msg);

    my $prog = basename($0);
    print STDERR << "EOF";
This script receives CSV file of "lat,lon" and filters it by bounding
box or osmosis' polygon file.

usage: $prog [-h] [-i infile] [-o target] [-l list] [-s suffix] [-z]

 -h        : print ths help message and exit.
 -i infile : CSV points file to process (default is STDIN).
 -o target : directory to put resulting files.
 -z        : compress output file with gzip.
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
