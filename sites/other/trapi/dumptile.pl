#!/usr/bin/perl

use strict;
use warnings;
use constant VERBOSE => 999;
use trapi;

ptdbinit('<');

my $argl = join(' ', @ARGV);
my @tiles = $argl =~ /^(?:z?(0|1\d)\D+(\d+)\D+(\d+)\D*)+$/;
while (my $z = shift @tiles) {
    my $x = shift @tiles;
    my $y = shift @tiles;
my $ptn = toptn($z, $x, $y);
my $df = openptn($ptn, 'data');
my $nf = openptn($ptn, 'nodes');
my $wf = openptn($ptn, 'ways');
my $rf = openptn($ptn, 'relations');

seek $df, 0, 0;
my $tv = getvnum($df);
print "Tags version $tv\n\n";

my %seen;
seek $nf, 0, 0;
while (my ($n, $lat, $lon, $off) = readnode($nf)) {
    last unless(defined $n);
    unless ($n) {
	print "Empty node\n";
	next;
    }
    $lat /= CONV;
    $lon /= CONV;
    print "Node $n at $lat,$lon offset $off\n";
    if (exists $seen{$n}) {
	print "  !!! seen before\n";
    }
    $seen{$n}++;
    if ($off) {
	seek $df, $off, 0;
	my @tv = readtags($df, NODE);
	while (my $tag = shift(@tv)) {
	    my $val = shift(@tv);
	    if (defined $val) {
		print "  tag=\"$tag\" val=\"$val\"\n";
	    } else {
		print "  tag=\"$tag\" !!! val= UNDEFINED\n";
	    }
	}
    }
}

%seen=();
print "\n";
seek $wf, 0, 0;
while (my ($w, $off) = readway($wf)) {
    last unless(defined $w);
    unless ($w) {
	print "Empty way\n";
	next;
    }
    print "Way $w offset $off\n";
    if (exists $seen{$w}) {
	print "  !!! seen before\n";
    }
    $seen{$w}++;
    if ($off) {
	seek $df, $off, 0;
	my @nodes = readwaynodes($df);
	print "  ".scalar(@nodes)." nodes\n";
	foreach my $n (@nodes) {
	    print "  node $n\n";
	}
	my @tv = readtags($df, WAY);
	while (my $tag = shift(@tv)) {
	    my $val = shift(@tv);
	    if (defined $val) {
		print "  tag=\"$tag\" val=\"$val\"\n";
	    } else {
		print "  tag=\"$tag\" !!! val= UNDEFINED\n";
	    }
	}
    }
}

%seen=();
print "\n";
seek $rf, 0, 0;
while (my ($r, $off) = readrel($rf)) {
    last unless(defined $r);
    unless ($r) {
	print "Empty relation\n";
	next;
    }
    print "Relation $r offset $off\n";
    if (exists $seen{$r}) {
	print "  !!! seen before\n";
    }
    $seen{$r}++;
    if ($off) {
	seek $df, $off, 0;
	my @m = readmemb($rf);
        foreach my $m (@m) {
	    my ($type, $mid, $role) = @$m;
	    if ($type < 4) {
		print "  member ".(MEMBERTYPE)[$type]." id=$mid role=\"$role\"\n";
	    } else {
		print "  meber !!! type $type id=$mid role=\"$role\"\n";
	    }
	}
	my @tv = readtags($df, RELATION);
	while (my $tag = shift(@tv)) {
	    my $val = shift(@tv);
	    if (defined $val) {
		print "  tag=\"$tag\" val=\"$val\"\n";
	    } else {
		print "  tag=\"$tag\" !!! val= UNDEFINED\n";
	    }
	}
    }
}
}
