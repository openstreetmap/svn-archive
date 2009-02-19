#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 0;
use trapi;

# ptdbinit("<");

if ($ARGV[0] =~ /^z(\d\d?)\/(\d+)\/(\d+)\b/) {
    my ($s, $w, $n, $e) = Project($2, $3, $1);
    print "bbox=$s,$w,$n,$e\n";
} elsif ($ARGV[0] =~ /^\d+$/) {
    my ($s, $w, $n, $e) = Project($ARGV[0], $ARGV[1], $ARGV[2]);
    print "SWNE: $s, $w, $n, $e\n";
} else {
    my ($x, $y) = getTileNumber($ARGV[0], $ARGV[1], $ARGV[2]);
    print "$x $y\n";
}

