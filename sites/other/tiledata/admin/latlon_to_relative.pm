use Math::Trig;
use strict;

my $deg2rad = 0.0174532925;
my $pi = 3.1415926535;
my $res = 1 / 2**31;

my $tres = 2**(31 - 15);

sub latlon2relativeXY
{
  my ($lat,$lon) = @_;
  my $x = ($lon + 180.0) / 360.0;
  
  # TODO: check lat is within range, and log isn't 0
  my $y = (1.0 - log(tan($lat * $deg2rad) + sec($lat * $deg2rad)) / $pi) / 2.0;
  
  $x = int($x / $res);
  $y = int($y / $res);
  
  my $tx = int($x / $tres);
  my $ty = int($y / $tres);
  
  return($x,$y, $tx,$ty);
}

1