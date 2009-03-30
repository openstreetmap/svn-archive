#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

# download needed daily/hourly/minute osm change files
# and put file names on stdout

use strict;
use warnings;


use LWP::Simple;
use Time::Local;
use constant VERBOSE => 5;
use trapi;

use constant MINUTE => 60;
use constant HOUR => 3600;
use constant DAY => (24 * HOUR);
use constant USEDAY => (12 * HOUR);
use constant USEHOUR => (30 * MINUTE);

chdir TRAPIDIR or die "Could not chdir TRAPIDIR: $!";


sub process($$$) {
    my ($type, $f, $e) = @_;
    my $fn = "$f-$e.osc.gz";
    exit 0 if (-f "stopfile.txt");
    my $return = mirror(WEBSITE."$type/$fn", TMPDIR.$fn);
    if (is_error($return)) {
	print STDERR "Error fetching $fn: $return\n";
	return 0;
    } else {
	print TMPDIR."$fn\n";
	return 1;
    }
}


open STAMP, "<", "timestamp" or die "Could not open timestamp";
my $stamp = <STAMP>;
chomp $stamp;
close STAMP;

$| = 1;

my @t = $stamp =~ /^(\d{4})(\d\d)(\d\d)(\d\d)?(\d\d)?$/;
my $t = timegm(0, $t[4]?$t[4]:0, $t[3]?$t[3]:0, $t[2], $t[1]-1, $t[0]-1900);

while ($t + USEDAY < time) {
    # more than USEDAY behind -- process dailys
    my @f = gmtime($t);
    my $e = timegm(0, 0, 0, $f[3], $f[4], $f[5]) + DAY;
    last if ($e > time);
    my $f = sprintf("%04d%02d%02d", $f[5]+1900, $f[4]+1, $f[3]);
    my @g = gmtime($e);
    my $g = sprintf("%04d%02d%02d", $g[5]+1900, $g[4]+1, $g[3]);
    last unless(process("daily",$f,$g));
    $t = $e;
}

while ($t + USEHOUR < time) {
    # more than USEHOUR behind -- process hourlys
    my @f = gmtime($t);
    my $e = timegm(0, 0, $f[2], $f[3], $f[4], $f[5]) + HOUR;
    last if ($e > time);
    my $f = sprintf("%04d%02d%02d%02d", $f[5]+1900, $f[4]+1, $f[3], $f[2]);
    my @g = gmtime($e);
    my $g = sprintf("%04d%02d%02d%02d", $g[5]+1900, $g[4]+1, $g[3], $g[2]);
    last unless(process("hourly",$f,$g));
    $t = $e;
}

for (;;) {
    # process minute
    my @f = gmtime($t);
    my $e = timegm(0, $f[1], $f[2], $f[3], $f[4], $f[5]) + MINUTE;
#    printf "e: %d time: %d\n", $e, time;
    sleep(WAITDELAY) if ($e > time - OSCDELAY);
    my $f = sprintf("%04d%02d%02d%02d%02d", $f[5]+1900, $f[4]+1, $f[3], $f[2], $f[1]);
    my @g = gmtime($e);
    my $g = sprintf("%04d%02d%02d%02d%02d", $g[5]+1900, $g[4]+1, $g[3], $g[2], $g[1]);
    if (process("minute",$f,$g)) {
	$t = $e;
    } else {
	sleep(WAITFAIL);
    }
}

    
