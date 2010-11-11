<?php

/*
$w = -640000;
$s = 6434000;
$e = 192000;
$n = 7310000;
*/
$w = -90000;
$s = 6590000; 
$e = -80000; 
$n = 6600000; 

$scale = (isset($argv[1]))? $argv[1] : 0.001; // pixels/m
$step =  (isset($argv[2]))? $argv[2] : 5000; // step(m)
$boxsize = $step*$scale;

/*
$im = ImageCreate(($e-$w)*$scale,($n-$s)*$scale);
$cols = array();
for($count=0; $count<10; $count++)
	$cols[$count]=ImageColorAllocate($im,$count*25.5,0,10-($count*25.5));
*/

$conn=pg_connect("dbname=freemap");

for($wcount=$w; $wcount<$e; $wcount+=$step)
{
	for($scount=$s; $scount<$n; $scount+=$step)
	{
		$n_row = get_row_count($wcount,$scount,
								$wcount+$step, $scount+$step)
		$n_row = ($n_row > 100) ? $n_row: 100;
		echo "w $wcount s $scount, num ROWS = $n_row);
		/* 
			$x = ($wcount-$w) * $scale;
			$y = ($n-$scount) * $scale;
			ImageFilledRectangle ($im,$x,$y,$x+$boxsize,$y+$boxsize,
					$cols[$n_row/10]);
		*/
	}
}

/*
$cities = get_cities();
foreach ($cities as $city)
{
	$px_x = ($city['x'] - $w) * $scale;
	$px_y = ($n - $city['y']) * $scale;
	ImageFilledEllipse($im,$px_x,$px_y,10,10);
	ImageText($im,$city['name']);
}

ImagePNG($im);
ImageDestroy($im);
*/

pg_close($conn);

function get_row_count($w,$s,$e,$n)
{
	$q = "SELECT COUNT(*) FROM planet_osm_line ".
		"WHERE (Centroid(way)).x BETWEEN $w AND $e ".
		"AND (Centroid(way)).y BETWEEN $s AND $n ".
		"AND (designation LIKE '%footpath%' OR designation ".
		"LIKE '%bridleway%' OR designation LIKE '%byway%' ".
		"OR foot='designated' OR horse='designated' OR ".
		"highway LIKE '%byway%' OR ".
		"(highway='bridleway' AND horse <> 'permissive'))";
	echo $q;
	$result = pg_query($q);
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
	print_r($row);
	pg_free_result($result);
}

function get_cities()
{
	$details=array();
	$q = "SELECT way.x,way.y,name FROM planet_osm_point WHERE place='city'";
	$result=pg_query($q);
	while($row=pg_fetch_array($result,null,PGSQL_ASSOC))
	{
		$details[] = $row;
	}
	return $details;
}
		


?>
