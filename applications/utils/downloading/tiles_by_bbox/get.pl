# OJW 2006  GNU GPL v3 or later
use Math::Trig;
use strict;

my ($W,$S,$E,$N) = split(/,/, shift());
my $Z = shift();

my $Usage = "usage: $0 W,S,E,N zoom\n";

die($Usage) if($Z < 8 || $Z > 18);

my $P2 = 2 ** $Z;
my $Y1 = int(Lat2Y($N) * $P2);
my $Y2 = int(Lat2Y($S) * $P2);
my $X1 = int(Long2X($W) * $P2);
my $X2 = int(Long2X($E) * $P2);

die($Usage) if($X2 < $X1 || $Y2 <= $Y1);


for(my $y = $Y1; $y <= $Y2; $y++){
  for(my $x = $X1; $x <= $X2; $x++){
    my $URL = sprintf("http://%s/%d/%d/%d.png", "tile.openstreetmap.org/mapnik",$Z,$x, $y);
    
    my $File = sprintf("tile_%d_%d_%d.png", $Z,$x,$y);
    
    printf("wget -O %s %s\n", $File, $URL);
  }
}
sub Long2X{
  my $Long = shift();
  return(($Long + 180.0) / 360.0);
}
sub Lat2Y{
  my $Lat = shift();
  my $LimitY = ProjectF(85.0511);
  my $Y = ProjectF($Lat);
  my $PY = ($LimitY - $Y) / (2 * $LimitY);
  return($PY);
}
sub ProjectF{
  my $Lat = shift();
  $Lat = deg2rad($Lat);
  my $Y = log(tan($Lat) + (1/cos($Lat)));
  return($Y);
}
sub deg2rad{
  return(shift() * 0.0174532925);
}
