#!/usr/bin/perl

use strict;
use warnings;
use constant VERBOSE => 0;
use trapi;

my (%nnodetags, %nnodevals, %nwaytags, %nwayvals, %nreltags, %nrelvals, %nroles);

open TF, "<", "tags.z14x16384y0" or die "Could not open tags.z14x16384y0: $!";
my ($v, $t, $vv);
my $u = 1;
my $ignoretags = IGNORETAGS;
while ($_ = <TF>) {
    chomp;
    if (/^\t\t([^\t]*)\t(\d+)$/) {
	my $val = $1;
	my $c = $2;
	$vv->{$val} = $c if ($u && $c >= 32);
    } elsif (/^\t([^\t]*)\t(\d+)$/) {
        my $tag = $1;
	my $c = $2;
	$u = ($tag !~ /$ignoretags/o);
	if ($u && $c >= 32) {
	    $t->{$tag} = $c;
	    $vv = {};
	    $v->{$tag} = $vv;
	}
    } elsif (/^nodes$/) {
	$t = \%nnodetags;
	$v = \%nnodevals;
    } elsif (/^ways$/) {
	$t = \%nwaytags;
	$v = \%nwayvals;
    } elsif (/^relations$/) {
	$t = \%nreltags;
	$v = \%nrelvals;
    } elsif (/^roles$/) {
	$t = \%nroles;
	$v = undef;
    } else {
	print STDERR "Malformed line in tags: $_";
    }
}
close TF;

foreach my $x (['node', \%nnodetags, \%nnodevals],
	       ['way', \%nwaytags, \%nwayvals],
	       ['relation', \%nreltags, \%nrelvals],
	       ['role', \%nroles, undef()]) {
    my $n;
    ($n, $t, $v) = @$x;
    print "${n}s\n";
    foreach my $tag (sort {$t->{$b} <=> $t->{$a} || $a cmp $b} keys %$t) {
	print "\t$tag\t".$t->{$tag}."\n";
	if ($v && $v->{$tag}) {
	    $vv = $v->{$tag};
	    foreach my $val (sort {$vv->{$b} <=> $vv->{$a} || $a cmp $b}
			     keys %$vv) {
		print "\t\t$val\t".$vv->{$val}."\n";
	    }
	}
    }
}


