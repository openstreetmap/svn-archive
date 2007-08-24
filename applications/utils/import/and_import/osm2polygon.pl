#!/usr/bin/perl

# Simple script to take an OSM file with a single way and convert it to a
# polygon file for use with extract-polygon.pl
#
# By Martijn van Oosterhout <kleptog@svana.org>  August 2007
# Licence: BSD

use strict;
use warnings;

use lib '../../perl_lib';
use Geo::OSM::OsmXML;

if( $#ARGV != 0 )
{
  die "osm2polygon.pl <infile>\n";
}

my $infile = shift;

my $OSM = new Geo::OSM::OsmXML();
$OSM->load($infile);

if( scalar(keys %OsmXML::Ways) != 1 )
{
  die "Expecting exactly one way (found ".scalar(keys %OsmXML::Ways).")\n";
}

my ($way) = values(%OsmXML::Ways);
my @segments = split(/,/, $way->{"segments"});

my $lastnode = undef;

my @points;

foreach my $segmentID(@segments){
  my $segment = $OsmXML::Segments{$segmentID};
          
  if( defined $lastnode && $segment->{"from"} != $lastnode )
  {
    die "Way not contiguous near node $segment->{from}\n";
  }
  $lastnode = $segment->{"to"};
  my $node = $OsmXML::Nodes{$segment->{"from"}};
  push @points, [$node->{lat}, $node->{lon}];
}

if( $OsmXML::Segments{$segments[0]}->{from} != $OsmXML::Segments{$segments[-1]}->{to} )
{
  my $n1 = $OsmXML::Nodes{$OsmXML::Segments{$segments[0]}->{from}};
  my $n2 = $OsmXML::Nodes{$OsmXML::Segments{$segments[-1]}->{to}};
  if( $n1->{lon} != $n2->{lon} or $n1->{lat} != $n2->{lat} )
  {
    die "Way not closed\n";
  }
}

print "1\n",
      (map { "   $_->[1] $_->[0]\n" } @points),
      "   $points[0]->[1] $points[0]->[0]\n",
      "END\n";
       
