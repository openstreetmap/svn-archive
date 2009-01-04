#!/usr/bin/perl

# Copyright 2008 Blars Blarson
# Distributed under GPL version 2.0

use strict;
use warnings;

my $filename = $ARGV[0];
my $pf = $filename;
$pf =~ s/\.osm$//;
$pf .= ".poi";

my %subs;
open POI,"<",$pf or die "Could not open $pf: $!";
while ($_ = <POI>) {
  my ($n, $id) = split /\t/;
  $subs{$id} = $n;
}
close POI;

open OSM,"<",$filename or die "Could not open $filename: $!";
my $sf = $filename;
$sf =~ s/\.osm$//;
$sf .= ".sub.osm";
open SUB,">",$sf or die "Could not open $sf: $!";

my ($id);
while ($_ = <OSM>) {
    if (/^\s*\<node\s/) {
      ($id) = /\sid\=[\"\']?(-?\d+)[\"\']?\b/;
      if (exists $subs{$id} && ! (/\/\>\s*$/)) {
	my $found = 0;
        print SUB $_;
	while (! /\<\/node\>/) {
	  $_ = <OSM>;
	  if (/\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
	      my $tag = $1;
	      if ($tag eq 'name') {
		print SUB "  <tag k=\"name\" v=\"$subs{$id}\" />\n";
                $found = 1;
	      } else {
		print SUB $_;
	      }
	  } elsif (/\<tag\s+k\=\'([^\']*)\'\s+v\=\'([^\']*)\'/) {
	      my $tag = $1;
	      if ($tag eq 'name') {
		print SUB "  <tag k=\"name\" v=\"$subs{$id}\" />\n";
                $found = 1;
	      } else {
	        print SUB $_;
	      }
	  } elsif (/\<\/node\>/) {
              if (!$found) {
                  print SUB "  <tag k=\"name\" v=\"$subs{$id}\" />\n";
              }
              print SUB $_;
              last;
	  } else {
	    print SUB $_;
	  }
	}
      } else {
	print SUB $_;
      }
    } else {
      print SUB $_;
    }
}

close OSM;
