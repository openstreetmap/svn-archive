#!/usr/bin/perl

BEGIN {
    my $dir = $0;
    $dir =~s,[^/]+/[^/]+$,,;
    unshift(@INC,"$dir/../perl_lib");

    unshift(@INC,"../perl_perl_lib");
    unshift(@INC,"~/svn.openstreetmap.org/applications/rendering/tilesAtHome");
    unshift(@INC,"$ENV{HOME}/svn.openstreetmap.org/applications/rendering/tilesAtHome");
}


use strict;
use warnings;

use tahproject;

