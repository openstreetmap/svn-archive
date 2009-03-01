#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 999;

use trapi;

ptdbinit('+<');

my $id = $ARGV[0];

my $ptn = relationptn($id);

my $rf = openptn($ptn, 'relations');

while (my ($r, $off) = readrel($rf)) {
    last unless (defined $r);
    next unless ($r == $id);
    seek $rf, -8, 1;
    printrel($rf, 0, 0);
    last;
}


