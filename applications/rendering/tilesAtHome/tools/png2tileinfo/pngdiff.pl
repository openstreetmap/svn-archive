
#!/usr/bin/perl -w

# This program compares two oceantiles.png-files and lists
# the differences.

# The files must be 4096x4096 pixel files for zoom-12.

# based on code by Martijn van Oosterhout <kleptog@gmail.com>
# written by Steinar Hamre <steinarh@pvv.ntnu.no>

use strict;
use LWP::Simple;

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

  # Resolve not Allocate so we don't run out of colors
  $color = $image->colorResolve(0,0,255) if($type == TILETYPE_SEA);
  $color = $image->colorResolve(0,255,0) if($type == TILETYPE_LAND);
  $color = $image->colorResolve(255,255,255) if($type == TILETYPE_TILE);
  die if not defined $color;
  print STDERR "Setting $x,$y to $color\n";
  $image->setPixel($x,$y, $color);
}



if ($#ARGV < 1) {
    print "Usage: pngdiff.pl oldfile.png newfile.png\n";
    exit(0);
}

my $oldfile=$ARGV[0];
my $newfile=$ARGV[1];

open my $world_fh, "<$oldfile" or die "Couldn't open $oldfile ($!)\n";
my $world_im = GD::Image->newFromPng( $world_fh, 1 );
close $world_fh;

open my $newworld_fh, "<$newfile" or die "Couldn't open $newfile ($!)\n";
my $newworld_im = GD::Image->newFromPng( $newworld_fh, 1 );
close $newworld_fh;

# Autoflush on
$|=1;

my @typenames = ('unknown', 'land', 'sea', 'mixed');
for my $y (0..4095)
{
  my $tmp = 0;
  my $str = "";
  for my $x (0 .. 4095)
  {
    my $type = get_type($world_im,$x,$y);
    my $ntype = get_type($newworld_im,$x,$y);
    if ($ntype != $type) {
	my $ntypen=$typenames[$ntype];
	print "$x,$y,12,$ntypen\n";
    }
  }
}
