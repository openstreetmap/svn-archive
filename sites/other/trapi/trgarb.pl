#!/usr/bin/perl
# Copyright 2008, 2009 Blars Blarson.
# Distributed under GPL version 2, see GPL-2

use strict;
use warnings;

use constant VERBOSE => 10;
use trapi;

chdir TRAPIDIR or die "could not chdir ".TRAPIDIR.": $!";

ptdbinit("+<");

my ($startz, $startx, $starty) = (0) x 3;
if (scalar(@ARGV)) {
    die "Either no arguments or z x y" unless (scalar(@ARGV) == 3);
    ($startz, $startx, $starty) = @ARGV;
}

trapi: for (my $z = $startz; $z <= MAXZOOM; $z++) {
    my $zdir;
    next unless(opendir $zdir, "z$z");
    my @x = sort {$a <=> $b} grep((/^\d+$/ && ($_ >= $startx)), readdir $zdir);
    closedir $zdir;
    foreach my $x (@x) {
	my $xdir;
	opendir $xdir,"z$z/$x" or die "Could not opendir $z/$x; $!";
	my @y = sort {$a <=> $b} grep((/^\d+$/ && ($_ >= $starty)), readdir $xdir);
	closedir $xdir;
	foreach my $y (@y) {
	    last trapi if (-f "stopfile.txt");
	    my $ptn = toptn($z, $x, $y);
	    gcptn($ptn);
	}
    }
}

exit 0;
    
