#-----------------------------------------------------------------------------
# Generate a map image from OSM tiles
#
# Copyright 2007
#  * Oliver White
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
#-----------------------------------------------------------------------------
package getTileArea;
use GD;
use strict;
use osm::getTile;
use osm::tileCoords;

sub createArea{
  my ($Area, $Filename) = @_;
  
  my ($X, $Y) = tileCoords::LLZ_to_XY(
    $Area->{lat},
    $Area->{long},
    $Area->{zoom});
  
  my $Image = GD::Image->new(
    $Area->{width},
    $Area->{height},
    1);

  my $BackgroundColour = $Image->colorAllocate(255,255,255); 
  $Image->filledRectangle(
    0,
    0,
    $Area->{width},
    $Area->{height},
    $BackgroundColour);
  
  my $GridSize = 1;
  my $Tilesize = getTile::size();
  
  my ($XA, $YA) = (int($X), int($Y));
  my ($OffsetX, $OffsetY) = ($X - $XA, $Y - $YA);
  my ($ImgCentreX, $ImgCentreY) = ($Area->{width}/2, $Area->{height}/2);
  
  if(0){
    print "Location is $X, $Y\n";
    print "Centre tile is $XA, $YA\n";
    print "Offset by $OffsetX, $OffsetY\n";
    print "Image centred on $ImgCentreX, $ImgCentreY\n";
  }
  
  
  for(my $xi = -$GridSize; $xi <= $GridSize; $xi++){
    for(my $yi = -$GridSize; $yi <= $GridSize; $yi++){

      my $ToX = $ImgCentreX + ((-$OffsetX + $xi) * $Area->{size});
      my $ToY = $ImgCentreY + ((-$OffsetY + $yi) * $Area->{size});

      if($ToX > -$Tilesize && $ToX < $Area->{width}
        && $ToY > -$Tilesize && $ToY < $Area->{height}){
        
        my $PartData = getTile::tile($XA + $xi, $YA + $yi, $Area->{zoom});
        if(defined $PartData){
          
          my $PartImage = GD::Image->newFromPngData($PartData, 1);
          if($PartImage){
          
            $Image->copyResampled(
              $PartImage, 
              $ToX,
              $ToY, 
              0, 
              0, 
              $Area->{size},
              $Area->{size},
              $Tilesize, 
              $Tilesize);
            }
          }
        }
      #printf "%1.3f, %1.3f (%1.1f, %1.1f)\n", $ToX, $ToY, $XA, $YA;
    }
  }

  # Debug: mark centre
  if(0){
    my $OverlayColour = $Image->colorAllocate(0,0,0); 
    $Image->line(0, $ImgCentreY, $Area->{width}, $ImgCentreY, $OverlayColour);
    $Image->line($ImgCentreX, 0, $ImgCentreX, $Area->{height},$OverlayColour);
  }
  
  savePng($Image, $Filename);
}

sub savePng{
  my ($Image, $Filename) = @_;
  
  open(my $fp , '>', $Filename) || return;
  binmode $fp;
  print $fp $Image->png;
  close $fp;
}
1