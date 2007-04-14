<?php
################################################################################
# This file forms part of the OpenStreetMap source code.                       #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################


// WMS compliant walk route renderer 

require_once('latlong.php');
require_once('defines.php');
require_once('contours.php');
require_once('Map.php');
require_once('functions.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////


class WalkRouteMap
{
	var $im, 
		$map, 
		$backcol, 
		$zoom,
		$contour_colour,
		$mint;

	var $debug;

	function WalkRouteMap ($w, $s, $e, $n, $width, $height, $dbg=0)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);

		// 150506 recalculate zoom from input pixel and longitude width
		// see Steve's email of 01/02/06
		$this->zoom = round(log((360*$width)/($e-$w),2)-9);



		# 14/11/04 changed ImageCreate() to ImageCreateTrueColor();
		# in GD2, when copying images (as we do for the icons), both source
		# and destination image must be either paletted or true colour.
		# (The icons are s)
		$this->im = ImageCreateTrueColor($this->map->width,
									$this->map->height);
		$this->backcol = ImageColorAllocate($this->im,220,220,220);
		$this->yellow = ImageColorAllocate($this->im,255,255,0);
		ImageFill($this->im,100,100,$this->backcol);
		ImageColorTransparent($this->im,$this->backcol);
	}

	function draw()
	{
		$this->draw_walk_routes();
		ImagePNG($this->im);
		ImageDestroy($this->im);
	}

	
	function draw_walk_routes()
	{
		$conn = mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
		mysql_select_db(DB_DBASE);
		$topright = $this->map->get_top_right();

		$result=mysql_query
		("select * from walkroutes as w, walkroutepoints as p ".
		 "where w.id=p.id and p.lat between ".
		 $this->map->bottomleft["lat"]. " and $topright[lat] and ".
		 "p.lon between ".
		 $this->map->bottomleft["long"]. " and $topright[long] group by w.id");

		while($row=mysql_fetch_array($result))
		{
			$prev = null;
			$result2=mysql_query
				("select * from walkroutepoints where id=$row[id] ".
				 "order by point");
			while($row2=mysql_fetch_array($result2))
			{
				$ll = array ("lat"=>$row["lat"],"long"=>$row["lon"]);
				$p = $this->map->get_point($ll);
				if($prev)
					ImageLine($this->im,$prev["x"],$prev["y"],$p["x"],$p["y"]);
				$prev=$p;
			}
		}
	}
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("WIDTH" => 400, 
			"HEIGHT" => 320,
			"debug" => 0 );

$inp=array();

foreach ($defaults as $field=>$default)
{
	$inp[$field]=wholly_numeric($_GET[$field]) ?  $_GET[$field] : $default;
}

$bbox = explode(",",$_GET['BBOX']);
if(count($bbox)!=4)
{
	$error = "You need to supply a bounding box!";
}
elseif($bbox[0]<-180 || $bbox[0]>180 || $bbox[2]<-180 || $bbox[2]>180 ||
	 $bbox[1]<-90 || $bbox[1]>90 || $bbox[3]<-90 || $bbox[3]>90)
{
	$error = "Invalid latitude and/or longitude!";
}
else
{
	foreach($bbox as $i)
	{
		if(!wholly_numeric($i))	
			$error = "Invalid input. Goodbye!";
	}
}

if(!isset($error))
{
	$image = new WalkRouteMap($bbox[0], $bbox[1], $bbox[2], $bbox[3],
						$inp["WIDTH"],$inp["HEIGHT"], $inp["debug"]);
}

if (isset($error))
{
	echo "<html><body><strong>Error:</strong>$error</body></html>";
}
else
{
	
	if (!isset($_GET['debug']))
		header('Content-type: image/png'); 
	
	$image->draw();
}
?>
