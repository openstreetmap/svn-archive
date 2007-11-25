<?php
session_start();

// 20/11/07 now uses Mercator, not OSGB

################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-07 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

require_once('gpxnew.php');
require_once('latlong.php');
require_once('defines.php');
require_once('Map.php');
require_once('freemap_functions.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////


class Image
{
	var $im, 
		$map, 
		$backcol,
		$trackpoint_colour,
		$waypoint_colour,
		$bottomleft_ll,
		$topright_ll;

	var $debug;

	var $trackpoints;

	function Image ($w, $s, $e, $n, $width, $height, $layers)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);
		$this->im = ImageCreateTrueColor($width,$height);
		$this->backcol = ImageColorAllocate($this->im, 220, 220, 220);
		ImageFilledRectangle($this->im,0,0,$width,$height,$this->backcol);
		ImageColorTransparent($this->im, $this->backcol);
		$this->trackpoint_colour = ImageColorAllocate($this->im,255,255,0);
		$this->waypoint_colour = ImageColorAllocate($this->im,80,0,80);

		$this->bottomleft_ll = merc_to_ll($w,$s);
		$this->topright_ll = merc_to_ll($e, $n);


		/*
		echo "trackpoints:";
		print_r($this->trackpoints);
		*/
	}

	function draw($trackid)
	{
		$conn=mysql_connect('localhost',DB_USERNAME,DB_PASSWORD);
		mysql_select_db(DB_DBASE);
		$this->draw_trackpoints($trackid);
		$this->draw_waypoints($trackid);
		mysql_close($conn);
		ImagePNG($this->im);	
	}
	
	function draw_trackpoints($trackid)
	{

			$q=
			("select * from trackpoints where lat between ".
				$this->bottomleft_ll['lat'] ." and ".
				$this->topright_ll['lat']. " and lon between ".
				$this->bottomleft_ll['lon'] ." and ".
				$this->topright_ll['lon']);

			if($trackid)
				$q .= " and trackid=$trackid";

		$result=mysql_query($q);
		//echo "QUERY : $q";
		while($row=mysql_fetch_array($result))
		{
			$merc =ll_to_merc($row['lat'],$row['lon']); 
			$p = $this->map->get_point ($merc);
			ImageFilledEllipse
				($this->im,$p['x'],$p['y'],5,5,$this->trackpoint_colour);
		}

	}

	function draw_waypoints($trackid)
	{
			$q=
			("select * from waypoints where lat between ".
				$this->bottomleft_ll['lat'] ." and ".
				$this->topright_ll['lat']. " and lon between ".
				$this->bottomleft_ll['lon'] ." and ".
				$this->topright_ll['lon']);

			if($trackid)
				$q .= " and trackid=$trackid";

			$result=mysql_query($q);
			while($row=mysql_fetch_array($result))
			{
				$merc =ll_to_merc($row['lat'],$row['lon']); 
				$p = $this->map->get_point ($merc);
				ImageFilledEllipse
				($this->im,$p['x'],$p['y'],10,10,$this->waypoint_colour);

				ImageString($this->im,3,$p['x']+8,$p['y']+8,$row['name'],
							$this->waypoint_colour);
			}
	}
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("width" => 500, 
			"height" => 500,
			"bbox" => "-85000,6595000,-80000,6600000",
			"layers" => "none",
			"trackid" => null);

$inp=array();

foreach ($defaults as $field=>$default)
{
	if(valid_input($field,$_GET[$field]))
		$inp[$field] = $_GET[$field];
	elseif(valid_input($field,$_GET[strtoupper($field)]))
		$inp[$field] = $_GET[strtoupper($field)];
	else
		$inp[$field] = $default;
}

$bbox = explode(",",$inp['bbox']);


if(isset($error))
{
	echo "<html><head><title>Error!</title></head><body>$error</body></html>";
}
else
{
	if (!isset($_GET['debug']))
		header('Content-type: image/png'); 
	
	$image = new Image(round($bbox[0]), round($bbox[1]), round($bbox[2]), 
						round($bbox[3]),
						$inp["width"],$inp["height"], $inp["layers"]);

	$image->draw($inp['trackid']);
}

function valid_input($field,$value)
{
	if($field=="width" || $field=="height")
	{
		return wholly_numeric($value);
	}
	return $value;
}
?>
