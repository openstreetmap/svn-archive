<?php
session_start();
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
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
		$this->trackpoint_colour = ImageColorAllocate($this->im,192,0,0);

		$this->bottomleft_ll = gr_to_wgs84_ll(array("e"=>$w, "n"=>$s));
		$this->topright_ll = gr_to_wgs84_ll(array("e"=>$e, "n"=>$n));


		/*
		echo "trackpoints:";
		print_r($this->trackpoints);
		*/
	}

	function draw()
	{
		$this->draw_trackpoints();
		ImagePNG($this->im);	
	}
	
	function draw_trackpoints()
	{
		$conn=mysql_connect('localhost',DB_USERNAME,DB_PASSWORD);
		mysql_select_db(DB_DBASE);

			$q=
			("select * from trackpoints where lat between ".
				$this->bottomleft_ll['lat'] ." and ".
				$this->topright_ll['lat']. " and lon between ".
				$this->bottomleft_ll['long'] ." and ".
				$this->topright_ll['long']). " and trackid=$_SESSION[trackid]";

		$result=mysql_query($q);
		//echo "QUERY : $q";
		while($row=mysql_fetch_array($result))
		{
			$gr = wgs84_ll_to_gr 
				(array("lat"=>$row['lat'],"long"=>$row['lon']));
			$p = $this->map->get_point ($gr);
			ImageFilledEllipse($this->im,$p['x'],$p['y'],5,5,$this->trackpoint_colour);
		}

		mysql_close($conn);
	}
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("width" => 500, 
			"height" => 500,
			"bbox" => "487000,125000,492000,130000" ,
			"layers" => "none");

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

	$image->draw();
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
