#!/usr/bin/perl
#

use strict;

my %places = (
	oxford    => [ 51.752, -1.25794 ],
	charlbury => [ 51.8726, -1.481 ],
);

my $place = shift;
if($place && $place =~ /^\-?\d+\.\d+/) {
	my $long = shift;
	my $lat = $place;
	$place = $lat.",".$long;

	my @ll = ($lat,$long);
	$places{$place} = \@ll;
}

my $need_help = 0;
if($place eq "-h") { $need_help = 1; }
unless($place && $places{$place}) { $need_help = 1; }

if($need_help) {
	print "Use:\n";
	print "   osm-activity.pl <place>\n";
	print "\nWhere <place> is one of:\n";
	print "\t".join(", ", keys %places)."\n";
	print "\nSee README.txt for more information\n";
	exit 1;
}

# Download the feed
my $url = "http://www.openstreetmap.org/feeds/nodes.rss?latitude=".$places{$place}->[0]."&longitude=".$places{$place}->[1];
print "Handling place $place:\n";
print "  Fetching $url\n";

open(RSS, "wget -q -O - '$url' |");

# Read it
my $rss;
while(<RSS>) { $rss .= $_; }
close RSS;

# Process it
my @items = ($rss =~ /(<item>.*?<\/item>)/sg);
my @allusers = ($rss =~ /last edited by (.*?),/sg);

my ($firstedit) = ($items[0] =~ /<pubDate>(.*?)<\/pubDate>/);
my ($lastedit) = ($items[-1] =~ /<pubDate>(.*?)<\/pubDate>/);

# Get unique users
my %users;
foreach my $u (@allusers) {
	$users{$u}++;
}

# Report
print "\n";
print "There were ".scalar(@items)." edits in the period\n";
print "There were ".(scalar keys %users)." users active in the period\n";
print "\n";
print "The first edit was at $firstedit\n";
print "The last edit was at $lastedit\n";
print "\n";
foreach my $u (sort keys %users) {
	my $du = $u;
	while(length($du) lt 50) { $du .= " "; }
	print "  $du - ".$users{$u}." edits\n";
}
