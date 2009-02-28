#!/usr/bin/perl

use strict;
use warnings;

use constant VERBOSE => 5;
use trapi;

chdir TRAPIDIR or die "Could not chdir ".TRAPIDIR.": $!";
ptdbinit("<");

my ($startz, $startx, $starty) = (0) x 3;
if (scalar(@ARGV)) {
    die "Either no arguments or z x y" unless (scalar(@ARGV) == 3);
    ($startz, $startx, $starty) = @ARGV;
}


my (%nnodetags, %nnodevals, %nwaytags, %nwayvals, %nreltags, %nrelvals, %nroles);
my $tiles = 0;

sub checkpoint($$$) {
    my ($z, $x, $y) = @_;
    open TF, ">", "tags.z${z}x${x}y${y}" or die "Could not open tags: $!";

    my ($tag, $count, $role, $val, $vc);
    print TF "nodes\n";
    while (($tag,$count) = each %nnodetags) {
	next unless ($tag =~ /^[^\t]*$/);
	print TF "\t$tag\t$count\n";
	if (exists $nnodevals{$tag}) {
	    my %nv = %{$nnodevals{$tag}};
	    while (($val, $vc) = each %nv) {
		next unless ($val =~ /^[^\t]*$/);
		print TF "\t\t$val\t$vc\n";
	    }
	}
    }
    print TF "ways\n";
    while (($tag,$count) = each %nwaytags) {
	next unless ($tag =~ /^[^\t]*$/);
	print TF "\t$tag\t$count\n";
	if (exists $nwayvals{$tag}) {
	    my %wv = %{$nwayvals{$tag}};
	    while (($val, $vc) = each %wv) {
		next unless ($val =~ /^[^\t]*$/);
		print TF "\t\t$val\t$vc\n";
	    }
	}
    }
    print TF "relations\n";
    while (($tag,$count) = each %nreltags) {
	next unless ($tag =~ /^[^\t]*$/);
	print TF "\t$tag\t$count\n";
	if (exists $nrelvals{$tag}) {
	    my %rv = %{$nrelvals{$tag}};
	    while (($val, $vc) = each %rv) {
		next unless ($val =~ /^[^\t]*$/);
		print TF "\t\t$val\t$vc\n";
	    }
	}
    }
    print TF "roles\n";
    while (($role, $count) = each %nroles) {
	next unless ($role =~ /^[^\t]*$/);
	print TF "\t$role\t$count\n";
    }
    close TF;
}

open TF, "<", DBDIR."tags.".TAGSVERSION
    or die "Could not open tags.".TAGSVERSION.": $!";
my ($v);
while ($_ = <TF>) {
    chomp;
    if (/^\t\t/) {
	# skip vals
    } elsif (/^\t([^\t]*)\t(\d+)$/) {
	my $tag = $1;
	my $c = $2;
	$v->{$tag} = {} if ($v && $c >= THRESH);
    } elsif (/^nodes$/) {
	$v = \%nnodevals;
    } elsif (/^ways$/) {
	$v = \%nwayvals;
    } elsif (/^relations$/) {
	$v = \%nrelvals;
    } elsif (/^roles$/) {
	$v = undef;
    } else {
	die "Malformed line in tags: $_";
    }
}
close TF;

outer:  for (my $z = $startz; $z <= MAXZOOM; $z++) {
    my $zdir;
    next unless(opendir $zdir, "z$z");
    my @x = sort {$a <=> $b} grep ((/^\d+$/ && ($_ >= $startx)), readdir $zdir);
    closedir $zdir;
    foreach my $x (@x) {
	my $xdir;
	opendir $xdir,"z$z/$x" or die "Could not opendir z$z/$x; $!";
	my @y = sort {$a <=> $b} grep((/^\d+$/ && ($_ >= $starty)), readdir $xdir);
	closedir $xdir;
	foreach my $y (@y) {
	    last outer if (-f "stopfile.txt");
	    my $ptn = toptn($z, $x, $y);
	    my $df = openptn($ptn, 'data');
	    my $nf = openptn($ptn, 'nodes');
	    while (my ($n, $lat, $lon, $off) = readnode($nf)) {
		last unless (defined $n);
		next unless ($off);
		seek $df, $off, 0;
		my @tv = readtags($df, NODE);
		while (my $tag = shift @tv) {
		    my $val = shift @tv;
		    $nnodetags{$tag}++;
		    $nnodevals{$tag}->{$val}++
			if (exists $nnodevals{$tag});
		}
	    }
	    my $wf = openptn($ptn, 'ways');
	    while (my ($w, $off) = readway($wf)) {
		last unless (defined $w);
		next unless ($off);
		seek $df, $off, 0;
		my @nodes = readwaynodes($df);
		my @tv = readtags($df, WAY);
		while (my $tag = shift @tv) {
		    my $val = shift @tv;
		    $nwaytags{$tag}++;
		    $nwayvals{$tag}->{$val}++
			if (exists $nwayvals{$tag});
		}
	    }
	    my $rf = openptn($ptn, 'relations');
	    while (my ($r, $off) = readrel($rf)) {
		last unless (defined $r);
		next unless ($off);
		seek $df, $off, 0;
		my @members = readmemb($df);
		foreach my $m (@members) {
		    my ($type, $mid, $role) = @$m;
		    $nroles{$role}++;
		}
		my @tv = readtags($df, RELATION);
		while (my $tag = shift @tv) {
		    my $val = shift @tv;
		    $nreltags{$tag}++;
		    $nrelvals{$tag}->{$val}++
			if (exists $nrelvals{$tag});
		}
	    }
	    if (-f 'stopfile.txt') {
		checkpoint($z,$x,$y);
		exit 0;
	    }
	    checkpoint($z,$x,$y) unless(++$tiles % 100000);
	}
    }
}

checkpoint(MAXZOOM,1<<MAXZOOM,0);
