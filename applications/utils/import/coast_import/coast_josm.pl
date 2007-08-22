#!/usr/bin/perl
#-----------------------------------------------------------------------------
# coast.pl
# 
# This program will create a JOSM osm file of PGS coastlines
#
# Usage: 
#   perl coast.pl LatS LatN LongW LongE [datafile]
#
#

use Geo::ShapeFile;
use strict;

# Lat, Lat, Long, Long, Sector
my $Y1 = shift();
my $Y2 = shift();
my $X1 = shift();
my $X2 = shift();
my $Sector = shift() || "13WLong";

  if ($Y1 > $Y2) {
      my $Y0 = $Y1;
      $Y1 = $Y2;
      $Y2 = $Y0;
  }
  if ($X1 > $X2) {
      my $X0 = $X1;
      $X1 = $X2;
      $X2 = $X0;
  }

open(OSM, ">coast+$X1+$X2+$Y1+$Y2.osm")
    or die "Could not open osm file for writing: $!";
my $next = 0;
print OSM "<?xml version='1.0' encoding='UTF-8'?>\n";
print OSM "<osm version='0.4' generator='coast.pl'>\n";

  my $Filename = "Data/NGA_GlobalShoreline_cd".$Sector;

  # Coastline tags
  my $TagDefault = "<tag k=\"source\" v=\"PGS\"/>";
  my $TagWay = $TagDefault."<tag k=\"natural\" v=\"coastline\"/>";
  
  # Logfile
  open(LOG, ">log.txt") || die("Can't create logfile");
  printf LOG "Long %f to %f, Lat %f to %f, shapefile %s\n",
    $X1, $X2, $Y1, $Y2, $Filename;
  printf LOG "(%f, %f)\n", $X2 - $X1, $Y2 - $Y1;
  
  my $shapefile = new Geo::ShapeFile( $Filename );
  printf "%d shapes\n", $shapefile->shapes();

  my $Count = 0;
  for(1 .. $shapefile->shapes()) {
    my $shape = $shapefile->get_shp_record($_);
    my $LastValid = 0;
    my $NoBreaks = 1;
    my $FirstNode = 0;
    my $LastNode = 0;
    my @Segments;
    
    foreach my $Point($shape->points()){
      my $Long = $Point->X();
      my $Lat = $Point->Y();
    
      my $InArea = (($Lat > $Y1) && ($Lat < $Y2) && ($Long > $X1) && ($Long < $X2));
      
      if($InArea){
          my $Node = NewNode($Lat, $Long, $TagDefault);
          printf LOG "Node #%d: %f, %f\n", $Node, $Lat, $Long;
          
          if($LastValid){
            my $Segment = NewSegment($LastNode, $Node, '');
            push(@Segments, $Segment) if($Segment);
            printf LOG "Segment #%d: %d, %d\n", $Segment, $LastNode, $Node;
          
            if(scalar(@Segments) > 80){
              my $Way = NewWay($TagWay, @Segments);
              printf LOG "InterimWay %d: %s\n", $Way, join(", ", @Segments);
              @Segments = ();
            }

          }
          $LastNode = $Node;
          $FirstNode = $Node if(!$FirstNode);
          
          $LastValid = 1;
        }
        else
        {
          $LastValid = 0;
          $NoBreaks = 0;
        }
      }
          
      if(scalar(@Segments) > 0){
        my $Way = NewWay($TagWay, @Segments);
        printf LOG "Way %d: %s\n", $Way, join(", ", @Segments);
      }
    }
    
print OSM "</osm>\n";
close OSM or die "Error closing osm file: $!";
    print LOG "Complete\n";
    close LOG;
    print "Done\n";

exit 0;

sub NewWay() {
    my ($Tags, @Segments) = @_;
    $next--;
    print OSM "<way id=\"$next\" action='create' visible='true'>\n";
    foreach my $Segment (@Segments) {
	print OSM "  <seg id=\"$Segment\" />\n";
    }
    print OSM "  $Tags\n</way>\n";
    return $next;
}

sub NewSegment() {
    my ($Node1, $Node2, $Tags) = @_;
    $next--;
    print OSM "<segment id=\"$next\" visible='true' from=\"$Node1\" to=\"$Node2\"/>\n";
    return $next;
}

my %NodeCache;
sub NewNode() {
    my ($Lat, $Lon, $Tags) = @_;
    if( defined $NodeCache{"$Lat,$Lon"} )
    {
      return $NodeCache{"$Lat,$Lon"};
    }
    $next--;
    print OSM "<node id=\"$next\" visible='true' lat=\"$Lat\" lon=\"$Lon\">\n  $Tags\n</node>\n";
    return $next;
}

__END__

=head1 NAME

B<coast.pl>

=head1 DESCRIPTION

This program will create a JOSM osm file of PGS coastlines

=head1 SYNOPSIS

Usage: 
   perl coast.pl LatS LatN LongW LongE [datafile]

=head1 OPTIONS

=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag
Copyright 2007 Blars Blarson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License version 2
as published by the Free Software Foundation

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=head1 AUTHOR

OJW

=head1 SEE ALSO

http://www.openstreetmap.org/

=cut
