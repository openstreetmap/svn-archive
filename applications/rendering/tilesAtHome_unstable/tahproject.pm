use strict;
use Math::Trig;

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================

# Setup map projection
my $LimitY = ProjectF(RadToDeg(atan(sinh(pi)))); #atan(sinh(pi)) = 85.0511 deg
my $LimitY2 = ProjectF(RadToDeg(atan(sinh(-&pi))));
my $RangeY = $LimitY - $LimitY2;

#-----------------------------------------------------------------------------
# Project latitude in degrees to Y coordinates in mercator projection
#-----------------------------------------------------------------------------
sub ProjectF 
{
    my $Lat = DegToRad(shift());
    my $Y = log(tan($Lat) + sec($Lat));
    return($Y);
}
#-----------------------------------------------------------------------------
# Project tile Y to latitude bounds
#-----------------------------------------------------------------------------
sub Project 
{
    my ($Y, $Zoom) = @_;
    
    my $Unit = 1 / (2 ** $Zoom);
    my $relY1 = $Y * $Unit;
    my $relY2 = $relY1 + $Unit;
    
    $relY1 = $LimitY - $RangeY * $relY1;
    $relY2 = $LimitY - $RangeY * $relY2;
    
    my $Lat1 = ProjectMercToLat($relY1);
    my $Lat2 = ProjectMercToLat($relY2);
    return(($Lat1, $Lat2));
}

#-----------------------------------------------------------------------------
# Project tile X to longitude bounds
#-----------------------------------------------------------------------------
sub ProjectL 
{
    my ($X, $Zoom) = @_;
    
    my $Unit = 360 / (2 ** $Zoom);
    my $Long1 = -180 + $X * $Unit;
    return(($Long1, $Long1 + $Unit));  
}

#-----------------------------------------------------------------------------
# Convert Y units in mercator projection to latitudes in degrees (not tile coordinate Y)
#-----------------------------------------------------------------------------
sub ProjectMercToLat($)
{
    my $MercY = shift();
    return(RadToDeg(atan(sinh($MercY))));
}

#-----------------------------------------------------------------------------
# Angle unit-conversions
#-----------------------------------------------------------------------------
sub DegToRad {return pi * shift() / 180;}
sub RadToDeg {return 180 * shift() / pi;}


1;
