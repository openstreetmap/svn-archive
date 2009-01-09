#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

use strict;
use warnings;

use constant VERBOSE => 10;
use trapi;

chdir TRAPIDIR or die "could not chdir ".TRAPIDIR.": $!";

ptdbinit("+<");
my ($pz) = pack "N", 0;
my ($b, $devnull);
my ($orig, $new) = (0, 0);

open $devnull, "<", "/dev/null";

my ($trapi);
opendir $trapi,"." or die "Could not opendir .: $!";
trapi: while (my $z = readdir $trapi) {
    next unless ($z =~ /^z0$/);
    chdir $z or die "Could not chdir $z: $!";
    my $zdir;
    opendir $zdir,"." or die "Could not opendir $z: $!";
    while (my $x = readdir $zdir) {
	next unless ($x =~ /^\d+$/);
	chdir $x or die "Could not chdir $z/$x: $!";
	my $xdir;
	opendir $xdir,"." or die "Could not opendir $z/$x; $!";
	while (my $y = readdir $xdir) {
	    next unless ($y =~ /^\d+$/);
	    last trapi if (-f "/trapi/stopfile.txt");
	    chdir $y or die "Could not chdir $z/$x/$y: $!";
	    print "Processing $z/$x/$y\n";
	    my $ptn = toptn($z, $x, $y);

# chdir "z$z/$x/$y" or die "Could not chdir";
my ($df, $ndf, $nf, $nnf, $wf, $nwf, $rf, $nrf);
if (open $df, "<", "data") {
    open $ndf, ">", "data.new";
    print $ndf "\0";
} else {
    $df = $devnull;
    $ndf = undef;
}
if (open $nf, "<", "nodes") {
    open $nnf, ">", "nodes.new";
} else {
    $nf = $devnull;
    $nnf = undef;
}
if (open $wf, "<", "ways") {
    open $nwf, ">", "ways.new";
} else {
    $wf = $devnull;
    $nwf = undef;
}
if (open $rf, "<", "relations") {
    open $nrf, ">", "relations.new";
} else {
    $rf = $devnull;
    $nrf = undef;
}

	    my %seen;
while (read $nf, $b, 16) {
    my ($n, $lat, $lon, $off) = unpack "NN!N!N", $b;
    next unless ($n);
    if (exists $seen{$n}) {
	print "Duplicate node $n\n";
	next;
    }
    my $noff = 0;
    if ($off) {
	seek $df, $off, 0;
	$noff = tell $ndf;
	my $tag;
	while (defined($tag = gets($df)) && ($tag ne "")) {
	    my $val = gets($df);
	    print $ndf "$tag\0$val\0";
	}
	print $ndf "\0";
    }
    print $nnf pack "NN!N!N", $n, $lat, $lon, $noff;
    $seen{$n} = 1;
    my $oldptn = nodeptn($n);
    if (defined $oldptn) {
	if ($oldptn ne $ptn) {
	    my ($uz, $ux, $uy) = fromptn($oldptn);
	    print "  node $n is actually in tile z$z $x,$y not z$uz $ux,$uy\n";
	    nodeptn($n, $ptn);
	}
    } else {
	print "  node $n is in tile z$z $x,$y not deleted\n";
	nodeptn($n, $ptn);
    }
}
	    %seen = ();
while (read $wf, $b, 8) {
    my ($w, $off) = unpack "NN", $b;
    next unless ($w);
    if (exists $seen{$w}) {
	print "Duplicate way $w\n";
	next;
    }
    my $noff = 0;
    if ($off) {
	seek $df, $off, 0;
	$noff = tell $ndf;
	my $n;
	while (read $df, $n, 4) {
	    print $ndf $n;
	    last if ($n eq $pz);
	}
	my $tag;
	while (defined($tag = gets($df)) && ($tag ne "")) {
	    my $val = gets($df);
	    print $ndf "$tag\0$val\0";
	}
	print $ndf "\0";
    }
    $seen{$w} = 1;
    my $oldptn = wayptn($w);
    if (defined $oldptn) {
	if ($off && ($ptn ne $oldptn)) {
	    my ($ux, $uy, $uz) = fromptn($oldptn);
	    print "  way $w is in z$z $x,$y not z$uz $ux,$uy\n";
	    wayptn($w, $ptn);
	}
	print $nwf pack "NN", $w, $noff;
    } else {
	if ($off) {
	    print "  way $w is in z$z $x,$y not deleted\n";
	    wayptn($w, $ptn);
	    print $nwf pack "NN", $w, $noff;
	} else {
	    print "  way $w is deleted\n";
	}
    }
}
	    %seen = ();
while (read $rf, $b, 8) {
    my ($r, $off) = unpack "NN", $b;
    next unless ($r);
    if (exists $seen{$r}) {
	print "Duplicate relation $r\n";
	next;
    }
    my $noff = 0;
    if ($off) {
	seek $df, $off, 0;
	$noff = tell $ndf;
	my $n;
	while (read $df, $n, 5) {
	    my ($type, $mid) = unpack "CN", $n;
	    last unless($type);
	    print $ndf $n;
	    my $role = gets($df);
	    print $ndf "$role\0";
	}
	seek $df, -4, 1;
	print $ndf "\0";
	my $tag;
	while (defined($tag = gets($df)) && $tag ne "") {
	    my $val = gets($df);
	    print $ndf "$tag\0$val\0";
	}
	print $ndf "\0";
    }
    my $oldptn = relationptn($r);
    if (defined $oldptn) {
	if ($off && ($ptn ne $oldptn)) {
	    my ($uz, $ux, $uy) = fromptn($oldptn);
	    print "  relation $r is in z$z $x,$y not z$uz $ux,$uy\n";
	    relationptn($r, $ptn);
	}
	print $nrf pack "NN", $r, $noff;
    } else {
	if ($off) {
	    print "  relation $r is in z$z $x,$y not deleted\n";
	    relationptn($r, $ptn);
	    print $nrf pack "NN", $r, $noff;
	} else {
	    print "  relation $r is deleted, not in z$z $x,$y\n";
	}
    }
}
	    if (defined $ndf) {
		$orig += (stat $df)[7];
		$new += tell $ndf;
		rename "data.new","data";
	    }
	    if (defined $nnf) {
		$orig += tell $nf;
		$new += tell $nnf;
		rename "nodes.new","nodes";
	    }
	    if (defined $nwf) {
		$orig += tell $wf;
		$new += tell $nwf;
		rename "ways.new","ways";
	    }
	    if (defined $nrf) {
		$orig += tell $rf;
		$new += tell $nrf;
		rename "relations.new","relations";
	    }
	
	    chdir "..";
	}
	chdir "..";
    }
    chdir "..";
}

my $shrink = (1 - ($new / $orig)) * 100 ;
print "Orig: $orig New: $new  Shrink: $shrink\%\n";

exit 0;
    
