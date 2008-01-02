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



#
#
#
my ($world_fh, $tileinfo_fh);
our $world_im;


#
#
#
sub get_type 
{
  my($image, $x, $y) = @_;

  my($r,$g,$b) = $image->rgb( $image->getPixel( $x,$y ) );
  
  return TILETYPE_LAND if $r == 0 && $g == 255 && $b == 0;
  return TILETYPE_SEA if $r == 0 && $g == 0   && $b == 255;
  return TILETYPE_TILE if $r == 255 && $g == 255 && $b == 255;
  return TILETYPE_UNKNOWN if $r == 0 && $g == 0 && $b == 0;
  
  die "Wierd tiletype at [$x,$y]: ($r,$g,$b)\n";
}


#
#
#
sub set_type
{
  my($image, $x, $y, $type) = @_;
  my $color;

  $color = $image->colorResolve(0,0,255) if($type == TILETYPE_SEA);
  $color = $image->colorResolve(0,255,0) if($type == TILETYPE_LAND);
  $color = $image->colorResolve(255,255,255) if($type == TILETYPE_TILE);
  $image->setPixel($x,$y, $color);
}


#
#
#
if ($#ARGV > -1)
{
    if($ARGV[0] eq "check")
    {
	if ($#ARGV < 2)
	{
	    print "Usage: png2tileinfo.pl check <x> <y>\n";
	}
	else
	{
	    my @typenames = ('unknown', 'land', 'sea', 'mixed');
	    my ($x, $y) = ($ARGV[1], $ARGV[2]);

	    open $world_fh, "<oceantiles_12.png" or die;
	    # use binmode so it works on windows too
	    binmode $world_fh;

	    $world_im = GD::Image->newFromPng( $world_fh, 1 );

	    my $png_val = get_type($world_im, $x, $y);

	    print "oceantiles_12.png($x, $y) = $png_val ($typenames[$png_val])\n";
	}

	exit 0;
    }

    if ($ARGV[0] eq "set")
    {
	if ($#ARGV < 3)
	{
	    print "Usage: png2tileinfo.pl set <x> <y> [land|sea|mixed]\n";
	}
	else
	{
	    my ($x, $y) = ($ARGV[1], $ARGV[2]);
	    my $newtype;

	    open $world_fh, "<oceantiles_12.png" or die;
	    # use binmode so it works on windows too
	    binmode $world_fh;
	    $world_im = GD::Image->newFromPng( $world_fh, 1 );
	    close $world_fh;
    
	    $newtype = TILETYPE_LAND if ($ARGV[3] eq "land");
	    $newtype = TILETYPE_SEA  if ($ARGV[3] eq "sea");
	    $newtype = TILETYPE_TILE if ($ARGV[3] eq "mixed");

	    set_type($world_im, $x, $y, $newtype);

	    open $world_fh, ">oceantiles_12.png" or die;
	    # use binmode so it works on windows too
	    binmode $world_fh;
	    print $world_fh $world_im->png;
	    close $world_fh;
	}

	exit 0;
    }
}


# Convert the resulting file in any case...
open $world_fh, "<oceantiles_12.png" or die;
open $tileinfo_fh, ">oceantiles_12.dat" or die;
# use binmode so it works on windows too
binmode $world_fh;
binmode $tileinfo_fh;
print STDERR "Writing output to ./oceantiles_12.dat\n";
$world_im = GD::Image->newFromPng( $world_fh, 1 );

for my $y (0..4095)
{
  my $tmp = 0;
  my $str = "";
  for my $x (0 .. 4095)
  {
    my $type = get_type($world_im,$x,$y);
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

