##################################################################
package Utils::Geometry;
##################################################################

use Exporter; require DynaLoader; require AutoLoader;
@ISA = qw(Exporter DynaLoader);
@EXPORT = qw(
 distance_point_point_Km
 distance_degree_point_point
 angle_north
 angle_north_relative
 distance_line_point_Km
 distance_line_point
);

# ------------------------------------------------------------------
# Distance in Km between 2 geo points with lat/lon
# Wild estimation of Earth radius 40.000Km
# At the poles we are completely off, since we assume the 
# lat/lon both have 40000Km radius, which is completely wrong
# if you are not at the aequator
sub distance_point_point_Km($$) {
    my $p1 = shift;
    my $p2 = shift;
    no warnings 'deprecated';

    return 999999999 
	unless defined($p1) && defined($p2);

    my $lat1 = $p1->{'lat'};
    my $lon1 = $p1->{'lon'};
    my $lat2 = $p2->{'lat'};
    my $lon2 = $p2->{'lon'};

    return 999999999 
	unless defined($lat1) && defined($lon1);
    return 999999999 
	unless defined($lat2) && defined($lon2);

    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;
    return sqrt($delta_lat*$delta_lat+$delta_lon*$delta_lon)*40000/360;
    
}

# ------------------------------------------------------------------
# Distance between 2 geo points with lat/lon, 
# result: (delta_lat,delta_lon) in degrees
sub distance_degree_point_point($$) {
    my $p1 = shift;
    my $p2 = shift;

    return (999999999,999999999)
	unless defined($p1) && defined($p2);

    my $lat1 = $p1->{lat};
    my $lon1 = $p1->{lon};
    my $lat2 = $p2->{lat};
    my $lon2 = $p2->{lon};
    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;
    return $delta_lat,$delta_lon;
    
}

# ------------------------------------------------------------------
# Angle from North
# East is  +0 ..   180
# West is  -0 .. - 180
sub angle_north($$){
    my $p1 = shift;
    my $p2 = shift;

    my $lat1 = $p1->{lat};
    my $lon1 = $p1->{lon};
    my $lat2 = $p2->{lat};
    my $lon2 = $p2->{lon};
    
    # Distance
    my $delta_lat=$lat1-$lat2;
    my $delta_lon=$lon1-$lon2;

    # Angle
    my $angle = - rad2deg(atan2($delta_lat,$delta_lon));
    return $angle;
}

# ------------------------------------------------------------------
# Angle from North relative
# so if you exchange the two points of the segment the angle keeps the same
# result is between  +0 ..  180
sub angle_north_relative($$){
    my $p1 = shift;
    my $p2 = shift;
    my $angle = angle_north($p1,$p2);
    $angle += 180 if $angle < 0;
    $angle -= 180 if $angle >180;
    return $angle;
}

# ------------------------------------------------------------------
# Minimal Distance between line and point in degrees
sub distance_line_point_Km($$$$$$) {
    return distance_line_point(@_)*40000/360;
}
# ------------------------------------------------------------------
# Minimal Distance between line and point in degrees
sub distance_line_point($$$$$$) {
    my $x1 = shift;
    my $y1 = shift;
    my $x2 = shift;
    my $y2 = shift;

    my $xp = shift;
    my $yp = shift;


    printf STDERR "distance_line_point(%f,%f, %f,%f,   %f,%f)\n", $x1, $y1, $x2, $y2,  $xp, $yp
	if ( $debug >10 ) ;

    my $dx1p = $x1 - $xp;
    my $dx21 = $x2 - $x1;
    my $dy1p = $y1 - $yp;
    my $dy21 = $y2 - $y1;
    my $frac = $dx21 * $dx21 + $dy21 * $dy21;

    if ( $frac == 0 ) {
	return(sqrt(($x1-$xp)*($x1-$xp) + ($y1-$yp)*($y1-$yp)));
    }

    my $lambda = -($dx1p * $dx21 + $dy1p * $dy21) / $frac;
    printf STDERR "distance_line_point(): lambda_1: %f\n",$lambda
	if ( $debug > 10 );

    $lambda = min(max($lambda,0.0),1.0);
    
    printf STDERR "distance_line_point(): lambda: %f\n",$lambda
	if ( $debug > 10 ) ;

    my $xsep = $dx1p + $lambda * $dx21;
    my $ysep = $dy1p + $lambda * $dy21;
    return sqrt($xsep * $xsep + $ysep * $ysep);
}
