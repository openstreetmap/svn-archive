#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 0;

use trapi;

chdir TRAPIDIR or die "Could not chdir TRAPIDIR: $!";

ptdbinit("<");

my ($ptn, $n, $w);
my (%pw, %pn, %pr, %tiles);

my ($bbs, $bbw, $bbn, $bbe);

print "Content-Type: text/xml; charset=utf8\n\n";

$_ = $ARGV[0];
if (/map\?bbox\=(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)\,(-?\d+(?:\.\d*)?)$/) {
# print "WSEN: $1, $2, $3, $4\n";
    my ($west,$south) = getTileNumber($2,$1,14);
    my ($east,$north) = getTileNumber($4,$3,14);
# print "WSEN: $west, $south, $east, $north\n";
    my ($x, $y);
    for($y=$north; $y <= $south; $y++) {
	for($x=$west; $x <= $east; $x++) {
	    $tiles{etoptn($x,$y)} = 1;
	}
    }
    ($bbs, $bbw, undef, undef) = Project($west, $south, 14);
    (undef, undef, $bbn, $bbe) = Project($east, $north, 14);
} elsif (/map\?tile\=(\d+)\,(\d+)\,(\d+)$/) {
    my ($z,$x,$y) = ($1, $2, $3);
    if ($z >= MAXZOOM) {
	my $zdiff = $z - MAXZOOM;
	$x >>= $zdiff;
	$y >>= $zdiff;
	$tiles{etoptn($x, $y)} = 1;
	($bbs, $bbw, $bbn, $bbe) = Project($x, $y, MAXZOOM);
    } else {
	($bbs, $bbw, $bbn, $bbe) = Project($x, $y, $z);
	my $zdiff = MAXZOOM - $z;
	$x <<= $zdiff;
	$y <<= $zdiff;
	my $n = (1<< $zdiff) - 1;
	foreach my $xx (0 .. $n) {
	    foreach my $yy (0 .. $n) {
		$tiles{etoptn($x+$xx,$y+$yy)} = 1;
	    }
	}
    }
    
} elsif (/node\/(\d+)$/) {
    my $node = $1;
    $ptn = nodeptn($node);
    $pn{$ptn} = {$node => 1};
} elsif (/way\/(\d+)$/) {
    my $way = $1;
    $ptn = wayptn($way);
    $pw{$ptn} = {$way => 1};
} elsif (/relation\/(\d+)$/) {
    my $rel = $1;
    $ptn = relationptn($rel);
    $pr{$ptn} = {$rel => 1};
} else {
    die "Unknown request $_";
}

print "<?xml version='1.0' encoding='UTF-8'?>\n";
print "<osm version=\"0.5\" generator=\"Trapi 0.3\">\n";
if ($bbs) {
    print "<bound box=\"$bbs,$bbw,$bbn,$bbe\" origin=\"http://www.openstreetmap.org/api/0.5\"/>\n";
}

foreach $ptn (keys %tiles) {
    my $nd = openptn($ptn, "data");
    my $wf = openptn($ptn, "ways");
    my $rf = openptn($ptn, "relations");
    
# first we go through the ways, looking for ones stored remotely or with nodes
# not in the tile
    seek $wf, 0, 0;
    while (my ($tw, $off) = readway($wf)) {
	last unless (defined $tw);
	next unless($tw);
	if ($off == 0) {
	    # way stored remotly
	    $w = wayptn($tw);
# print "Remote way $tw\n";
	    unless (exists $tiles{$w}) {
		$pw{$w} //= {};
		$pw{$w}->{$tw} = 1;
	    }
	} else {
	    seek $nd, $off, 0;
	    my @nodes = readwaynodes($nd);
	    foreach my $tn (@nodes) {
		$n = nodeptn($tn);
		unless (exists $tiles{$n}) {
		    # node stored remotly
		    $pn{$n} //= {};  
		    $pn{$n}->{$tn} = 1;
		}
	    }
	}
    }
    seek $rf, 0, 0;
    while (my ($tr, $off) = readrel($rf)) {
	last unless (defined $tr);
	next unless($tr);
	if ($off == 0) {
	    my $r = relationptn($tr);
	    unless (exists $tiles{$r}) {
		$pr{$r} //= {};
		$pr{$r}->{$tr} = 1;
	    }
	}
    }
}

# now we go through the remote ways, looking for nodes not in the tile
foreach my $tp (keys %pw) {
    my $pwf = openptn($tp, "ways");
    my $pd = openptn($tp, "data");
    seek $pwf, 0, 0;
    while (my ($tw, $off) = readway($pwf)) {
	last unless (defined $tw);
	next unless($tw);
	if (exists $pw{$tp}->{$tw}) {
	    print "reading way data from $off\n" if (VERBOSE > 99);
	    seek $pd, $off, 0;
	    my @nodes = readwaynodes($pd);
	    foreach my $tn (@nodes) {
		$n = nodeptn($tn);
		unless (exists $tiles{$n}) {
		    # node stored remotly
		    $pn{$n} //= {};
		    $pn{$n}->{$tn} = 1;
		}
	    }
	}
    }
}

# print nodes in the tile
foreach $ptn (keys %tiles) {
    my $nf = openptn($ptn, "nodes");
    my $nd = openptn($ptn, "data");
    
    my ($z, $x, $y) = fromptn($ptn);
    print "<-- nodes from z$z $x $y >\n";
    seek $nf, 0, 0;
    while (my ($tn, $lat, $lon, $off) = readnode($nf)) {
	last unless (defined $tn);
	next unless($tn);
	$lat /= 10000000;
	$lon /= 10000000;
	print "<node id=\"$tn\" lat=\"$lat\" lon=\"$lon\" ";
	if ($off == 0) {
	    print "/>\n";
	} else {
	    print ">\n";
	    seek $nd, $off, 0;
	    my @tv = readtags($nd, NODE);
	    while (my $key = shift @tv) {
                my $val = shift @tv;
                print "  <tag k=\"$key\" v=\"$val\"/>\n";
            }
	    print "</node>\n";
	}
    }
}

# print the nodes used by ways

foreach my $tp (keys %pn) {
    my ($tz, $tx, $ty) = fromptn($tp);
    print "<-- some nodes from z$tz $tx $ty >\n";
    my $pnf = openptn($tp, "nodes");
    my $pd = openptn($tp, "data");
    seek $pnf, 0, 0;
    while(my ($tn, $lat, $lon, $off) = readnode($pnf)) {
	last unless (defined $tn);
	next unless($tn);
	if (exists $pn{$tp}->{$tn}) {
	    $lat /= 10000000;
	    $lon /= 10000000;
	    print "<node id=\"$tn\" lat=\"$lat\" lon=\"$lon\" ";
	    if ($off == 0) {
		print "/>\n";
	    } else {
		print ">\n";
		seek $pd, $off, 0;
		my @tv = readtags($pd, NODE);
		while (my $key = shift @tv) {
		    my $val = shift @tv;
		    print "  <tag k=\"$key\" v=\"$val\"/>\n";
		}
		print "</node>\n";
	    }
	}
    }
}

# print ways
foreach $ptn (keys %tiles) {
    my $nd = openptn($ptn, "data");
    my $wf = openptn($ptn, "ways");
    
    my ($z, $x, $y) = fromptn($ptn);
    print "<-- ways from z$z $x $y >\n";
    seek $wf, 0, 0;
    while(my ($tw, $off) = readway($wf)) {
	last unless (defined $tw);
	next unless ($tw && $off);
	print "<way id=\"$tw\">\n";
	seek $nd, $off, 0;
	my @nodes = readwaynodes($nd);
	foreach my $tn (@nodes) {
	    print "  <nd ref=\"$tn\"/>\n";
	}
	my @tv = readtags($nd, WAY);
	while (my $key = shift @tv) {
	    my $val = shift @tv;
	    print "  <tag k=\"$key\" v=\"$val\"/>\n";
	}
	print "</way>\n";
    }
}

foreach my $tp (keys %pw) {
    my ($tz, $tx, $ty) = fromptn($tp);
    print "<-- some ways from z$tz $tx $ty >\n";
    my $pwf = openptn($tp, "ways");
    my $pd = openptn($tp, "data");
    seek $pwf, 0, 0;
    while (my 	($tw, $off) = readway($pwf)) {
	last unless (defined $tw);
	next unless($tw && $off);
	if (exists $pw{$tp}->{$tw}) {
	    print "reading way data from $off\n" if (VERBOSE > 99);
	    print "<way id=\"$tw\">\n";
	    seek $pd, $off, 0;
	    my @nodes = readwaynodes($pd);
	    foreach my $tn (@nodes) {
		print "  <nd ref=\"$tn\"/>\n";
	    }
	    my @tv = readtags($pd, WAY);
	    while (my $key = shift @tv) {
		my $val = shift @tv;
		print "  <tag k=\"$key\" v=\"$val\"/>\n";
	    }
	    print "</way>\n";
	}
    }
}

# print relations

foreach $ptn (keys %tiles) {
    my $nd = openptn($ptn, "data");
    my $rf = openptn($ptn, "relations");
    
    my ($z, $x, $y) = fromptn($ptn);
    print "<-- relations from z$z $x $y>\n";
    seek $rf, 0, 0;
    while (my ($tr, $off) = readrel($rf)) {
	last unless (defined $tr);
	next unless ($tr && $off);
	print "<relation id=\"$tr\">\n";
	seek $nd, $off, 0;
	my @members = readmemb($nd);
	foreach my $m (@members) {
	    my ($type, $mid, $role) = @$m;
	    print "  <member type=\"".(MEMBERTYPE)[$type].
		"\" ref=\"$mid\" role=\"$role\"/>\n";
	}
	my @tv = readtags($nd, RELATION);
	while (my $key = shift @tv) {
	    my $val = shift @tv;
	    print "  <tag k=\"$key\" v=\"$val\"/>\n";
	}
	print "</relation>\n";
    }
}

foreach my $tp (keys %pr) {
    my ($tz, $tx, $ty) = fromptn($tp);
    print "<-- some relations from z$tz $tx $ty >\n";
    my $prf = openptn($tp, "relations");
    my $pd = openptn($tp, "data");
    seek $prf, 0, 0;
    while (my ($tr, $off) = readrel($prf)) {
	last unless (defined $tr);
	next unless($tr && $off);
	if (exists $pr{$tp}->{$tr}) {
	    print "<relation id=\"$tr\">\n";
	    seek $pd, $off, 0;
	    my @members = readmemb($pd);
	    foreach my $m (@members) {
		my ($type, $mid, $role) = @$m;
		print "  <member type=\"".(MEMBERTYPE)[$type].
		    "\" ref=\"$mid\" role=\"$role\"/>\n";
	    }
	    my @tv = readtags($pd, RELATION);
	    while (my $key = shift @tv) {
		my $val = shift @tv;
		print "  <tag k=\"$key\" v=\"$val\"/>\n";
	    }
	    print "</relation>\n";
	}
    }
}

print "</osm>\n";
