#!/usr/bin/perl
use strict;
use warnings;

use Getopt::Std;

my %val;
getopts('h:', \%val);

# helper script to convert svg file to png and move them to the right place
#
# usage:
# cd plugins/roadsigns/images_src/
# ./convert.pl -h 36 foo.svg

my $h = $val{"h"};
if (!$h) {
    $h = "40";
}

foreach my $i (0..$#ARGV) {
    my $f = $ARGV[$i];
    my $f2 = $f;
    $f2 =~ s/\.svg$/.png/;
    system("inkscape", "-f", $f, "-e", "../images/$f2", "-h", "$h");
}
