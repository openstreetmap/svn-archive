<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

function wholly_numeric($input)
{
	return preg_match("/^-?[\d\.]+$/",$input);
}

function get_mapview ($i)
{
		$mapviews = array (
			1 =>		array("id"=>1, "scale"=>0.3),
					array("id"=>2, "scale"=>0.6),
					array("id"=>3, "scale"=>1.2),
					array("id"=>4, "scale"=>3.125),
					array("id"=>5, "scale"=>6.25),
					array("id"=>6, "scale"=>12.5),
					array("id"=>7, "scale"=>25),
					array("id"=>8, "scale"=>50),
					array("id"=>9, "scale"=>100),
					array("id"=>10, "scale"=>200),
					array("id"=>11, "scale"=>400)
						);
		return $mapviews[$i];
}
# Might be breaking the law with this one. Global Megacorp has patented this
# algorithm. Well up yours I'm doing it anyway :-)
function line_length($x1,$y1,$x2,$y2)
{
	$dx=$x2-$x1;
	$dy=$y2-$y1;
	return sqrt($dx*$dx + $dy*$dy);
}
# Returns the slope angle of a contour line; 
# always in the range -90 -> 0 -> +90.
# 08/02/05 made more generalised by passing parameters as x1,x2,y1,y2
# rather than the line array.
function slope_angle($x1,$y1,$x2,$y2)
{
	$dy = $y2-$y1;
	$dx = $x2-$x1;
	/*
	$a = rad2deg(atan2($dy,$dx));
	return round($a-(180*($a>90&&$a<270))); 
	*/
	$a = $dx ? round(rad2deg(atan($dy/$dx))) : 90;
	return $a; 
}
?>
