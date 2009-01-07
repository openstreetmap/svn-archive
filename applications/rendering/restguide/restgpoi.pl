#!/usr/bin/perl

# Copyright 2008 Blars Blarson
# Distributed under GPL version 2.0

use strict;
use warnings;

my $filename = $ARGV[0];

open OSM,"<",$filename or die "Could not open $filename: $!";

my ($id, $lat, $lon, %tv, $tv);
my (@places);
while ($_ = <OSM>) {
    if (/^\s*\<node\s/) {
      %tv = ();
      unless (/\/\>\s*$/) {
	while (! /\<\/node\>/s) {
	  $tv = <OSM>;
          $_ .= $tv;
	  if ($tv =~ /\<tag\s+k\=\"([^\"]*)\"\s+v\=\"([^\"]*)\"/) {
	      my $tag = $1;
	      my $val = $2;
	      $tv{$tag} = $val;
	  } elsif ($tv =~ /\<tag\s+k\=\'([^\']*)\'\s+v\=\'([^\']*)\'/) {
	      my $tag = $1;
	      my $val = $2;
	      $tv{$tag} = $val;
	  }
	}
      }
#      print "Node: $_";
      ($id) = /\sid\=[\"\']?(\d+)[\"\']?\b/;
      ($lat) = /\slat\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
      ($lon) = /\slon\=[\"\']?(-?\d+(?:\.\d+)?)[\"\']?\b/;
      if (($tv{'amenity'} && ($tv{'amenity'} =~
		  /^(?:restaurant|fast_food|cafe|hotel|motel|pharmacy|hospital|bank)$/))
	  || ($tv{'tourism'} &&
	      ($tv{'tourism'} =~ /^(?:hotel|motel)$/))
	  || $tv{'shop'} ) {
	push @places, [$id, $lat, $lon,
		       $tv{'amenity'} || $tv{'tourism'} || $tv{'shop'},
		       $tv{'name'} || ''];
      }

    }
}

close OSM;

my $pf = $filename;
$pf =~ s/\.osm$//;
$pf .= ".poi";

open POI,">",$pf or die "Could not open $pf: $!";
my $n = 0;
foreach my $poi (sort { ${$b}[1] <=> ${$a}[1] || ${$a}[2] <=> ${$b}[2] } @places) {
  my @poi = @$poi;
  $n++;
  print POI "$n\t$poi[0]\t$poi[1]\t$poi[2]\t$poi[3]\t$poi[4]\n";
}

