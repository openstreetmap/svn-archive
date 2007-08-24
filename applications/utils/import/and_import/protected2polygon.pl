#!/usr/bin/perl -w

# Simple script to take list of way IDs representing protected areas and
# producing polygon files. The actually hard work is done by osm2polygon.pl.
#
# By Martijn van Oosterhout <kleptog@svana.org>  August 2007
# Licence: BSD


use strict;
use warnings;
use LWP::Simple;

use constant PROTECTED_FILE => "protected_areas";

open my $fh, "<", PROTECTED_FILE or die "Couldn't open ".PROTECTED_FILE." ($!)\n";

while(<$fh>)
{
  next if /^#/;
  if( /^(\d+)$/ )
  {
    my $way = $1;
    my $data = get("http://www.openstreetmap.org/api/0.4/way/$way/full");
    if( $data !~ /nl:protected_and_import/ )
    {
      print STDERR "WARNING: Way $way not correctly tagged, skipping\n";
      next;
    }
    my $polygon = filter($data);
    if( $polygon !~ /END/ )
    {
      print STDERR "WARNING: Conversion of way $way failed, skipping\n";
      next;
    }
    print $polygon;
  }
}

# This function takes OSM XML data and filters it through osm2polygon.pl.
# Due to the size of the data you can't simply use open2 because the buffers
# are not big enough, hence the double fork()
sub filter
{
  my $data = shift;
  
  my $pid = open( my $fh, "-|" );
  if( $pid == 0 )
  {
    my $pid2 = open( my $fh2, "-|" );
    if( $pid2 == 0 )
    {
      # In second level child, we write to STDOUT
      print $data;
      exit;
    }
    # In first level child, we read $fh2 and write STDOUT
    open( STDIN, "<&", $fh2 ) or die "Couldn't dup STDIN ($!)\n";
    exec( "./osm2polygon.pl", "-" );
    die "Failed to exec osm2polygon.pl ($!)\n";
    exit;
  }
  my $result = "!";  # Invert polygon
  while(<$fh>)
  {
    $result .= $_;
  }
  return $result;
}