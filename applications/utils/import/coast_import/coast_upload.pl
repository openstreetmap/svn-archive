#!/usr/bin/perl
#-----------------------------------------------------------------------------
# coast_upload.pl
# 
# This program will upload PGS coastlines to openstreetmap
#
# Usage: 
#   perl coast_upload.pl LatS LatN LongW LongE [datafile]
#
# Contact OJW on the Openstreetmap wiki for help using this program

use Geo::ShapeFile;
use LWP::Simple;
use osm;
use strict;

# Lat, Lat, Long, Long, Sector
my $Y1 = shift();
my $Y2 = shift();
my $X1 = shift();
my $X2 = shift();
my $Sector = shift() || "20";

createCoasts("Data/NGA_GlobalShoreline_cd".$Sector, $Y1, $Y2, $X1, $X2);
print "Done\n";


sub createCoasts(){
  # Filename, Lat, Lat, Long, Long
  my ($Filename, $Y1, $Y2, $X1, $X2) = @_;
  
  my $PW = "----"; # Password for reporting progress to the almien website
  my $ID = int(get(sprintf("http://almien.co.uk/OSM/CoastlineUpload/Update/?pg=$PW&action=start&S=%f&N=%f&W=%f&E=%f",$Y1,$Y2,$X1,$X2)));
    
  my $osm = new osm;
  
  # Enter your OSM username for downloading 
  $osm->setup("username\@domain","password","almien_coastlines");
  
  # Specify some temporary filenames the program can use
  $osm->tempfiles("temp1.txt", "temp2.txt");
  
  # Coastline tags
  my $TagDefault = "<tag k=\"source\" v=\"PGS\"/>";
  my $TagSegment = $TagDefault."<tag k=\"natural\" v=\"coastline\"/>";
  my $TagWay = $TagDefault."<tag k=\"natural\" v=\"coastline\"/>";
  
  # Logfile
  open(LOG, ">log.txt") || die("Can't create logfile");
  printf LOG "Long %f to %f, Lat %f to %f, shapefile %s, row ID %d\n",
    $X1, $X2, $Y1, $Y2, $Filename, $ID;
  printf LOG "(%f, %f)\n", $X2 - $X1, $Y2 - $Y1;
  
  my $shapefile = new Geo::ShapeFile( $Filename );
  printf "%d shapes\n", $shapefile->shapes();

  my %NodeCache;
  
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
          if( not defined $NodeCache{"$Lat,$Long"} )
          {
            $NodeCache{"$Lat,$Long"}= $osm->uploadNode($Lat, $Long, $TagDefault);
          }
          my $Node = $NodeCache{"$Lat,$Long"};
          
          printf LOG "Node #%d: %f, %f\n", $Node, $Lat, $Long;
          
          if($LastValid){
            my $Segment = $osm->uploadSegment($LastNode, $Node, $TagSegment);
            push(@Segments, $Segment) if($Segment);
            printf LOG "Segment #%d: %d, %d\n", $Segment, $LastNode, $Node;
          
            if(scalar(@Segments) > 80){
              my $Way = $osm->uploadWay($TagWay, @Segments);
              printf LOG "InterimWay %d: %s\n", $Way, join(", ", @Segments);
              @Segments = ();
            }

          }
          $LastNode = $Node;
          $FirstNode = $Node if(!$FirstNode);
          
          # Upload status to website
          if(++$Count % 100 == 0){
            get(sprintf("http://almien.co.uk/OSM/CoastlineUpload/Update/?pg=$PW&action=status&ID=%d&lat=%f&long=%f&count=%d",$ID, $Lat, $Long, $Count));
          }

          $LastValid = 1;
        }
        else
        {
          $LastValid = 0;
          $NoBreaks = 0;
        }
      }
          
      if(scalar(@Segments) > 0){
        my $Way = $osm->uploadWay($TagWay, @Segments);
        printf LOG "Way %d: %s\n", $Way, join(", ", @Segments);
      }
    }
    
    print LOG "Complete\n";
    close LOG;
    print "Upload complete\n";    

  # Report this segment as finished
  get(sprintf("http://almien.co.uk/OSM/CoastlineUpload/Update/?pg=$PW&action=finish&ID=%d",$ID));
}

__END__

=head1 NAME

B<coast_upload.pl>

=head1 DESCRIPTION

This program will upload PGS coastlines to openstreetmap

=head1 SYNOPSIS

Usage: 
   perl coast_upload.pl LatS LatN LongW LongE [datafile]

=head1 OPTIONS

Contact OJW on the Openstreetmap wiki for help using this program

=head1 COPYRIGHT

Copyright 2006, Jörg Ostertag

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

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
