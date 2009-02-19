#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 5;
use trapi;

unless (scalar(@ARGV) == 3) {
    die "Must specify zoom x y";
}

my ($z, $x, $y) = @ARGV;

ptdbinit("+<");

my $ptn = toptn($z, $x, $y);

gcptn($ptn);
