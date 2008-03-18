use strict;
use warnings;

# Helper code for or/p
#
# Algorithm invented by bobkare and described here:
# http://bob.cakebox.net/poly-center.php
#
# Implemented in Perl by Frederik Ramm <frederik@remote.org>
#
# The only external entry point is find_area_center (given an OSM way,
# returns the area center lat/lon). Everything else is helper stuff.
#
# FIXME:
# Does not support polygons with holes (relations) yet. If a centre point
# has to be found for an area that contains holes, the hole edges have
# to be tested against when doing intersection checks (see "FIXME holes" 
# below).
#
# Someone might want to make a proper module of this.
# 

# -------------------------------------------------------------------
# sub find_area_center($way)
#
# finds the ideal centre point for an area where to place a text or
# icon.
#
# This code required a total of over 800 lines of code in the original
# osmarender.xsl. The algorithm has been re-implemented according to 
# the description on http://bob.cakebox.net/poly-center.php.
#
# FIXME:
# All this is less than optimal. The code should at least work with
# projected data, but the whole idea of finding one *node* at the
# center of an area to place a text or icon is wrong; one would have
# to estimate the bounding box of the text or icon and then find a
# location inside the area where the box fits. For large areas, the 
# icon or text might also have to be repeated.
#
# We generally use two-element array refs (x,y or lon,lat) to represent
# points.
#
sub find_area_center
{
    my $way = shift;
    my $nodes = $way->{'nodes'};
    my $nodecount = scalar(@$nodes);
    my $avgx;
    my $avgy;
    my $avgdist;
    my $midpoints = [];
    # usually last node equals first node, disregard
    $nodecount-- if ($nodes->[0] == $nodes->[$nodecount-1]);
    for (my $i = 0; $i<$nodecount; $i++)
    {
        my $from = ($i==0) ? $nodes->[$nodecount-1] : $nodes->[$i-1];
        my $through = $nodes->[$i];
        my $vertex = [ $through->{'lon'}, $through->{'lat'} ];
        my $to = ($i==$nodecount-1) ? $nodes->[0] : $nodes->[$i+1];

        $avgx += $vertex[0] / $nodecount;
        $avgy += $vertex[1] / $nodecount;

        #debug(sprintf("evaluating node $i, vertex is %8.5f,%8.5f", @$vertex));
        #debug(sprintf("from: %8.5f,%8.5f", $from->{"lon"}, $from->{"lat"}));
        #debug(sprintf("to: %8.5f,%8.5f", $to->{"lon"}, $to->{"lat"}));

        my $from_pt = [ $from->{"lon"}, $from->{"lat"} ];
        my $to_pt = [ $to->{"lon"}, $to->{"lat"} ];

        my $from_angle = atan2($from_pt->[1]-$vertex->[1], $from_pt->[0]-$vertex->[0]) + pi;
        my $to_angle = atan2($to_pt->[1]-$vertex->[1], $to_pt->[0]-$vertex->[0]) + pi;

        my $angle = ($from_angle+$to_angle)/2;
        #debug ("from $from_angle to $to_angle angle: $angle");
        $a = 180 - $vertex->[0];
        $b = $a * tan($angle);
        
        my $linepoint = [ $vertex->[0] + $a, $vertex->[1] + $b ];

        my $intcount = 0;
        my $nearest_off_dist= 999;
        my $nearest_on_dist= 999;
        my $nearest_off = [];
        my $nearest_on = [];

        #debug(sprintf("linepoint is %8.5f,%8.5f", @$linepoint));

        # start intersection detection for all other edges of the polygon.
        # FIXME holes
        for (my $j = 0; $j<$nodecount; $j++)
        {
            my $segment_start = $nodes->[$j];
            my $segment_end = ($j==$nodecount-1) ? $nodes->[0] : $nodes->[$j+1];
            next if ($segment_start == $through);
            next if ($segment_end == $through);

            my $start_pt = [ $segment_start->{'lon'}, $segment_start->{'lat'} ];
            my $end_pt = [ $segment_end->{'lon'}, $segment_end->{'lat'} ];

            #debug(sprintf("  other segment %8.5f,%8.5f - %8.5f,%8.5f", @$start_pt, @$end_pt));
            
            my $intersection = get_line_intersection($vertex, $linepoint, 
                $start_pt, $end_pt);
            next unless($intersection);
            #debug(sprintf("     intersection at %8.5f,%8.5f", @$intersection));

            next unless point_on_line($intersection, $start_pt, $end_pt);
            my $distance = dist($intersection, $vertex);
            #debug(sprintf("      distance is %8.5f", $distance));
            if (point_on_line($intersection, $vertex, $linepoint))
            {
                $intcount++;
                if ($distance < $nearest_on_dist)
                {
                    $nearest_on_dist = $distance;
                    $nearest_on = $intersection;
                }
            }
            else
            {
                if ($distance < $nearest_off_dist)
                {
                    $nearest_off_dist = $distance;
                    $nearest_off = $intersection;
                }
            }
        }

        my $point = ($intcount%2) ? $nearest_on : $nearest_off;
        $point->[0] = ($point->[0] + $vertex->[0]) / 2;
        $point->[1] = ($point->[1] + $vertex->[1]) / 2;
        $point->[2] = dist($point, $vertex);
        #debug(sprintf("select mid %8.5f,%8.5f",  @$point));
        push(@$midpoints, $point);
        $avgdist += $point->[2]/$nodecount;
    }

    # find best of points
    #
    # fixme: in some cases we want to return the "medium point"
    # directly and not the best fit...?
    
    my $bestpoint;
    my $bestscore;
    foreach (@$midpoints)
    {
        my $dist = dist($_, [ $avgx, $avgy ]);
        my $score = 2 * $_->[2] - $avgdist - $dist;
        if (!defined($bestpoint) || $score > $bestscore)
        {
            $bestpoint = $_;
            $bestscore = $score;
        }
    }
    # return value expected as lat,lon and we have been using x,y
    # debug(sprintf("best is %8.5f,%8.5f",  $bestpoint->[1], $bestpoint->[0]));
    return [ $bestpoint->[1], $bestpoint->[0] ];
}

sub get_line_intersection
{
    my ($a1, $a2, $b1, $b2) = @_;

    my $am = get_slope($a1, $a2);
    my $bm = get_slope($b1, $b2);

    # both segments are the same line
    return undef if ($bm == $am);

    # first segment is vertical
    if (!defined($am))
    {
        my $xi = $a1->[0];
        my $yi = $bm * ($a1->[0]-$b1->[0]);
        return [ $xi, $yi ];
    }
    # second segment is vertical
    if (!defined($bm))
    {
        my $xi = $b1->[0];
        my $yi = $am * ($b1->[0]-$a1->[0]);
        return [ $xi, $yi ];
    }

    my $ab = get_intercept($a1, $am);
    my $bb = get_intercept($b1, $bm);

    my $xi = ($ab - $bb) / ($bm - $am);
    my $yi = (((-$am) * $bb) + ($bm * $ab)) / ($bm - $am);
    return [ $xi, $yi ];
}

sub get_slope 
{
    my ($p1, $p2) = @_;
    my $div = $p2->[0] - $p1->[0];
    return undef if ($div==0);
    return ($p2->[1] - $p1->[1]) / $div;
}

sub point_on_line 
{
    # assumes that intersection check was positive!
    my ($pt, $lstart, $lend) = @_;
    my $left = min($lstart->[0], $lend->[0]);
    my $right = max($lstart->[0], $lend->[0]);
    my $bottom = min($lstart->[1], $lend->[1]);
    my $top = max($lstart->[1], $lend->[1]);

    #debug(sprintf("    point on line: pt=%8.5f,%8.5f left=$left right=$right top=$top bot=$bottom", @$pt));

    return ($pt->[0] >= $left) && ($pt->[0] <= $right) &&
        ($pt->[1] >= $bottom) && ($pt->[1] <= $top);
}

sub get_intercept 
{
    my ($point, $m) = @_;
    return $point->[1] - $m * $point->[0];
}

sub dist 
{
    my ($p1, $p2) = @_;
    return sqrt(($p2->[0]-$p1->[0])**2 + ($p2->[1]-$p1->[1])**2);
}


sub min
{
    my ($a, $b) = @_;
    return ($a<$b) ? $a : $b;
}
sub max
{
    my ($a, $b) = @_;
    return ($a>$b) ? $a : $b;
}

sub test
{
    my $w = { id => "1",
          nodes => [
            { "lat" => 0, "lon" => 1 },
            { "lat" => 10, "lon" => 5 },
            { "lat" => 15, "lon" => 10 },
            { "lat" => 10, "lon" => 15 },
            { "lat" => 0, "lon" => 19 },
            { "lat" => 10, "lon" => 10 },
          ]
         };
    my $c = find_area_center($w);
    exit;
}

1;
