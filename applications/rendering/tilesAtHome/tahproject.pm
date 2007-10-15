use strict;

# =====================================================================
# The following is duplicated from tilesGen.pl
# =====================================================================

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
# Project Y to latitude bounds
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
# Convert Y units in mercator projection to latitudes in degrees
#-----------------------------------------------------------------------------
sub ProjectMercToLat($)
{
    my $MercY = shift();
    return(RadToDeg(atan(sinh($MercY))));
}

#-----------------------------------------------------------------------------
# Project X to longitude bounds
#-----------------------------------------------------------------------------
sub ProjectL 
{
    my ($X, $Zoom) = @_;
    
    my $Unit = 360 / (2 ** $Zoom);
    my $Long1 = -180 + $X * $Unit;
    return(($Long1, $Long1 + $Unit));  
}

#-----------------------------------------------------------------------------
# Angle unit-conversions
#-----------------------------------------------------------------------------
sub DegToRad($){return pi * shift() / 180;}
sub RadToDeg($){return 180 * shift() / pi;}


1;
