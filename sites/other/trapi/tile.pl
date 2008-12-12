#!/usr/bin/perl

use strict;
use warnings;

use ptdb;

# ptdbinit("<");

if ($ARGV[0] =~ /^\d+$/) {
  my ($s, $w, $n, $e) = Project($ARGV[0], $ARGV[1], $ARGV[2]);
  print "SWNE: $s, $w, $n, $e\n";
} else {
    my ($x, $y) = getTileNumber($ARGV[0], $ARGV[1], $ARGV[2]);
    print "$x $y\n";
}

