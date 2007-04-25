#!/usr/bin/perl -w

# This program generates the "oceantiles_12.dat" file as used by
# lowzoom.pl and close-areas.pl.
#
# It takes a 4096x4096 pixel PNG file as input; the pixels in the 
# PNG file may have one of the four colors
#
# white - coastline intersects with this tile
# green - no coastline intersect, land tile
# blue -  no coastline intersect, sea tile
# black - unknown

# written by Martijn van Oosterhout <kleptog@gmail.com>
# with minor changes by Frederik Ramm <frederik@remote.org>

use GD;
use strict;
use bytes;

use constant TILETYPE_UNKNOWN => 0;
use constant TILETYPE_LAND => 1;
use constant TILETYPE_SEA => 2;
use constant TILETYPE_TILE => 3;

my($world_fh,$tileinfo_fh);

open $world_fh, "<oceantiles_12.png" or die;
open $tileinfo_fh, ">oceantiles_12.dat" or die;
my $world_im = GD::Image->newFromPng( $world_fh, 1 );

sub get_type 
{
  my($x,$y) = @_;

  my($r,$g,$b) = $world_im->rgb( $world_im->getPixel( $x,$y ) );
  
  return TILETYPE_LAND if $r == 0 && $g == 255 && $b == 0;
  return TILETYPE_SEA if $r == 0 && $g == 0   && $b == 255;
  return TILETYPE_TILE if $r == 255 && $g == 255 && $b == 255;
  return TILETYPE_UNKNOWN if $r == 0 && $g == 0 && $b == 0;
  
  die "Wierd tiletype at [$x,$y]: ($r,$g,$b)\n";
}

for my $y (0..4095)
{
  my $tmp = 0;
  my $str = "";
  for my $x (0 .. 4095)
  {
    my $type = get_type($x,$y);
    $tmp = ($tmp << 2) | $type;
    
    if( ($x&3) == 3)
    {
      my $byte = chr $tmp;
      $str .= $byte;
      $tmp=0;
    }
  }
  print $tileinfo_fh $str;
}
  
close $tileinfo_fh;

