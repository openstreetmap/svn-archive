<?php

/// Wrapper para cs2cs - permite convertir un array de coordenadas a otro array de coordenadas.

/// FIXME: escape shell args
function cs2cs($x,$y,$srs_in='epsg:4258',$srs_out='epsg:4326')
{
	$r = shell_exec("echo \"$x $y\" | cs2cs -f %.20f +init=$srs_in +to +init=$srs_out");
	sscanf($r,"%f %f",$x,$y);
	return (array($x,$y));
}


function epsg4230_to_latlong($x,$y)
{
	return cs2cs($x,$y,'epsg:4230');
}


function epsg4258_to_latlong($x,$y)
{
	return cs2cs($x,$y,'epsg:4258');
}

// $r = utm31_to_latlong(438899.6,4591936.33);
// print_r($r);
//
// $r = utm31_to_latlong(442426.69,4596424.53);
// print_r($r);

// $r = epsg4230_to_latlong(-6.59729,43.52425);
// $r = epsg4258_to_latlong(-3.710829858779907,40.44465952237447);
// print_r($r);

?>