#!/usr/bin/perl
# Copyright 2008 Blars Blarson.  Distributed under GPL version 2, see GPL-2

use strict;
use warnings;

use constant VERBOSE => 5;		# verbosity
use trapi;

chdir TRAPIDIR or die "could not chdir ".TRAPIDIR.": $!";

ptdbinit("+<");


my $ignoretags = IGNORETAGS;

my ($id, $lat, $lon, $x, $y, $ptn, $off, @tv, $tv);
my ($nodes, $ways, $relations, $splits) = 0 x 4;
while ($_ = <>) {
    if (/^\s*\<node\s/) {
	$nodes++;
	@tv = ();
	unless (/\/\>\s*$/) {
	    while (! /\<\/node\>/s) {
		$tv = <>;
		$_ .= $tv;
		if ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
		    my $tag = $1;
		    my $val = $2;
		    push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		}
	    }
	}
	print "Node: $_" if (VERBOSE > 20);
	($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	($lat) = /\slat\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	($lon) = /\slon\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
	($x, $y) = getTileNumber($lat, $lon, MAXZOOM);
	$ptn = etoptn($x, $y);
	print "id: $id lat: $lat lon: $lon x: $x y:$y\n" if (VERBOSE > 18);
	my $nf = openptn($ptn, "+<", "nodes");
	seek $nf, 0, 2;
	if (tell($nf) >= SPLIT) {
	    if(splitptn($ptn)) {
		$splits++;
		$ptn = etoptn($x, $y);
		$nf = openptn($ptn, "+<", "nodes");
		seek $nf, 0, 2;
	    }
	}
	nodeptn($id, $ptn);
	if (@tv) {
	    my $nd = openptn($ptn, "+<", "data");
	    seek $nd, 0, 2;
	    $off = tell $nd;
	    if ($off == 0) {
		print $nd "\0";
		$off = 1;
	    }
	    print "tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 19);
	    foreach $tv (@tv) {
		print $nd "$tv\0";
	    }
	    print $nd "\0";
	} else {
	    $off = 0;
	}
	print $nf pack "NN!N!N", $id, int($lat * CONV), int($lon * CONV), $off;
    } elsif (/^\s*\<way\s+/) {
	$ways++;
	@tv = ();
	my @nodes = ();
	unless (/\/\>\s*$/) {
	    while (! /\<\/way\>/s) {
		$tv = <>;
		$_ .= $tv;
		if ($tv =~ /\<nd\s+ref\=\"(\d+)\"/) {
		    push @nodes, $1;
		} elsif ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
		    my $tag = $1;
		    my $val = $2;
		    push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		}
	    }
	}
	($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	print "Way: $_" if (VERBOSE > 20);
	unless (@nodes) {
	    print "Way $id has no nodes\n" if (VERBOSE > 0);
	    print "Way: $_" if (VERBOSE > 1);
	    next;
	}
	$ptn = nodeptn($nodes[0]);
	$ptn = toptn(0,1,1) if ($ptn eq NOPTN);
	my $wf = openptn($ptn, "+<", "ways");
	seek $wf, 0, 2;
	my $wd = openptn($ptn, "+<", "data");
	seek $wd, 0, 2;
	$off = tell $wd;
	if ($off == 0) {
	    print $wd "\0";
	    $off = 1;
	}
	print "nodes: ".scalar(@nodes)." tags: ".scalar(@tv)." off: $off\n"
	    if (VERBOSE > 19);
	my %ptns = ();
	foreach my $node (@nodes) {
	    print $wd pack "N", $node;
	    $ptns{nodeptn($node)}++;
	}
	print $wd pack "N", 0;
	foreach $tv (@tv) {
	    print $wd "$tv\0";
	}
	print $wd "\0";
	my ($uz, $ux, $uy) = fromptn($ptn);
	print "Way $id in z$uz $ux,$uy\n" if (VERBOSE > 4);
	print $wf pack "NN", $id, $off;
	wayptn($id, $ptn);
	foreach my $p (keys %ptns) {
	    if ($p ne $ptn) {
		my ($vz, $vx, $vy) = fromptn($p);
		print "  also in z$vz $vx,$vy\n" if (VERBOSE > 4);
		my $pwf = openptn($p, "+<", "ways");
		seek $pwf, 0, 2;
		print $pwf pack "NN", $id, 0;
	    }
	}
    } elsif (/^\s*\<relation\s+/) {
	$relations++;
	@tv = ();
	my @members = ();
	unless (/\/\>\s*$/) {
	    while (! /\<\/relation\>/s) {
		$tv = <>;
		$_ .= $tv;
		if ($tv =~ /\<member\s+type\=\"(\w+)\"\s+ref\=\"(\d+)\"(?:\s+role\=\"([^\"]*)\")?/) {
		    push @members, [MEMBER->{$1}, $2, $3];
		} elsif ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
		    my $tag = $1;
		    my $val = $2;
		    push @tv, $tag, $val unless ($tag =~ /$ignoretags/o);
		}
	    }
	}
	($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
	print "Relation: $_" if (VERBOSE > 20);
	my %tiles = reltiles(\@members);
	$ptn = each %tiles;
	unless (defined $ptn) {
	    print "Relation $id has no members\n" if (VERBOSE > 0);
	    print "Relation: $_" if (VERBOSE > 2);
	    next;
	}
	$ptn = toptn(0,1,1) if ($ptn eq NOPTN);
        my $rf = openptn($ptn, "+<", "relations");
	seek $rf, 0, 2;
        my $rd = openptn($ptn, "+<", "data");
	seek $rd, 0, 2;
	$off = tell $rd;
	if ($off == 0) {
	    print $rd "\0";
	    $off = 1;
	}
	print "members: ".scalar(@members)." tags: ".scalar(@tv)." off: $off\n" if (VERBOSE > 19);
	foreach my $m (@members) {
	    my ($type, $mid, $role) = @$m;
	    print $rd pack("CN", $type, $mid).$role."\0";
	}
	print $rd pack "C", 0;
	foreach $tv (@tv) {
	    print $rd "$tv\0";
	}
	print $rd "\0";
	if (VERBOSE > 4) {
	    my ($uz, $ux, $uy) = fromptn($ptn);
	    print "Relation $id in z$uz $ux,$uy\n";
	}
	print $rf pack "NN", $id, $off;
	relationptn($id, $ptn);
	while (my $p = each %tiles) {
	    next if ($p eq $ptn);
	    if (VERBOSE > 4) {
		my ($vz, $vx, $vy) = fromptn($p);
		print "  also in z$vz $vx,$vy\n";
	    }
	    my $prf = openptn($p, "+<", "relations");
	    seek $prf, 0, 2;
	    print $prf pack "NN", $id, 0;
	}
    }
}

print "Nodes: $nodes Ways: $ways Relations: $relations Splits: $splits\n"
    if (VERBOSE > 1);
cachestat() if (VERBOSE > 2);
