<?php
################################################################################
# This file forms part of the OpenStreetMap source code.                       #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################


// WMS compliant SRTM renderer
// does not include OSM data, this has been moved to osm.php
// contains code from drawmap.php and classes.php - i.e. the Image class and
// the HTTP input reading code have been merged in the one file.

require_once('latlong.php');
require_once('defines.php');
require_once('contours.php');
require_once('Map.php');
require_once('functions.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////


class SRTMRenderer
{
	var $im, 
		$map, 
		$backcol, 
		$zoom,
		$contour_colour,
		$mint;

	var $debug;

	function SRTMRenderer ($w, $s, $e, $n, $width, $height, $dbg=0)
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
		$this->contour_colour = ImageColorAllocate($this->im,192,192,0);
		$this->mint = ImageColorAllocate($this->im,0,192,64);
		ImageFill($this->im,100,100,$this->backcol);
	}

	function draw()
	{
		$this->draw_contours();
		ImagePNG($this->im);
		ImageDestroy($this->im);
	}

	
	function angle_text ($p, $fontsize, $colour, $text)
	{
		$angle=slope_angle($p[0]['x'], $p[0]['y'], $p[1]['x'], $p[1]['y']);
		$i = ($p[1]['x'] > $p[0]['x']) ? 0:1;
		ImageTTFText($this->im, $fontsize, -$angle, $p[$i]['x'], $p[$i]['y'],
						$colour, TRUETYPE_FONT, $text);

		return $i;
	}

	function draw_contours()
	{
		$ll['bottomleft'] = $this->map->bottomleft;
		$ll['topright'] = $this->map->get_top_right();

		// Get the bounding rectangles for all lat/long squares 
		$rects = get_bounding_rects ($ll, $this->debug);

		// tested to here - OK

		// Get the sampled heights from the .hgt file
		$sampling_pts=array();
		$sampled_hgts=load_hgt2($rects,$sampling_pts, $this->debug, 1);

		// 250406 test for false return if hgt file couldn't be loaded
		if($sampled_hgts!==false)
		{
			// Get the screen coordinates of the sampling points
			$screen_pts = $this->get_screen_pts ($sampling_pts, $rects[0]);

			if($this->debug)
			{
				echo "DIMENSIONS : ";
				print_r($sampling_pts);
			}

			// Do each row of the sampling points

			$last_pt = array();	
	
			for($row=$sampling_pts['topleft']; 
				$row<$sampling_pts['topleft']+$sampling_pts['height']*10801;
				$row+=10801*$sampling_pts['resolution'])
			{
				// Do each point of the current row
				for($pt=$row; $pt<$row+$sampling_pts['width']; 
					$pt+=$sampling_pts['resolution']) 
				{
					{
						$this->do_contours($pt,$sampled_hgts,$screen_pts, 50,
									$last_pt, $sampling_pts['resolution']);
					}
				}
			}
		}
	}


	/* 14/02/05 original get_screen_pts(), not taking account of multiple
		grid squares, removed */

	function get_screen_pts ($sampling_pts, $topleftrect)
	{
		$frac = 0.0008333333333*$sampling_pts['resolution']; # resolution/1200
		$cur_ll['long']=$topleftrect['long']+$topleftrect['left']/1200;
		$origlong=$cur_ll['long'];
		$cur_ll['lat']=($topleftrect['lat']+1)-
				($topleftrect['top']/1200);

		$intlatplus1 = ceil($ll['bottomleft']['lat']);
		$intlong = floor($ll['bottomleft']['long']);


		for($row=$sampling_pts['topleft']; 
			$row<=$sampling_pts['topleft']+$sampling_pts['height']*10801;
			$row+=10801*$sampling_pts['resolution'])
		{
			for($pt=$row; $pt<=$row+$sampling_pts['width']; 
				$pt+=$sampling_pts['resolution'])
			{
			
				$screen_pts[$pt]['x']=round($this->map->get_x($cur_ll['long']));
				$screen_pts[$pt]['y']=round($this->map->get_y($cur_ll['lat']));

				$cur_ll['long'] += $frac;
			}
			$cur_ll['lat'] -= $frac;
			$cur_ll['long'] = $origlong;
		}
		return $screen_pts;
	}

	function do_contours ($pt, $hgt, $screen_pts, $interval, 
							&$last_pt, $f)
	{
		$edges = array ( array($pt,$pt+$f), array($pt,$pt+10801*$f), 
						 array ($pt+$f,$pt+10802*$f), 
						 array($pt+10801*$f,$pt+10802*$f));
		
		$quadpts = array ($pt, $pt+$f, $pt+10801*$f, $pt+10802*$f);

		$start_ht = min ($hgt[$pt], $hgt[$pt+$f], 
						 $hgt[$pt+10801*$f], $hgt[$pt+10802*$f] );
		$start_ht = (ceil($start_ht/$interval)) * $interval;
		$end_ht = max ($hgt[$pt], $hgt[$pt+$f], 
						 $hgt[$pt+10801*$f], $hgt[$pt+10802*$f] );
		$end_ht = (floor($end_ht/$interval)) * $interval;


		if($this->debug)	
		{
			echo "<p>pts : ".$pt." ".($pt+1)." ".($pt+10801)." ".
				($pt+10802)."<br/>heights:";	
			echo $hgt[$pt]. " ".$hgt[$pt+1]." ".$hgt[$pt+10801]." ".
				$hgt[$pt+10802]."<br/>";
			echo "start ht, end ht: $start_ht $end_ht<br/>";
		}


		for($ht=$start_ht; $ht<=$end_ht; $ht+=$interval)
		{
			if($this->debug) echo "${ht}ft: ";

				
			# See add_contour() in contours.php.
			$two_contours = (
			($hgt[$pt]<$ht && $hgt[$pt+$f]>$ht && 
			 $hgt[$pt+10802*$f]<$ht && $hgt[$pt+10801*$f] > $ht) 
			 ||
			($hgt[$pt]>$ht && $hgt[$pt+$f]<$ht && 
			 $hgt[$pt+10802*$f]>$ht && $hgt[$pt+10801*$f] < $ht)
				) ; 

			$line_pts = get_line($ht,$hgt,$screen_pts,$edges,
									$two_contours,$this->debug);


			// draw line
			if(count($line_pts)!=0)
			{
				for($count=0; $count<count($line_pts); $count++)
				{
					if($this->debug==1)
					{
						echo "Got line $count: ".$line_pts[$count][0]['x'].","
						.$line_pts[$count][0]['y'].
						" ".$line_pts[$count][1]['x'].",".
						$line_pts[$count][1]['y']."<br/>";
					}

					$colour = ($ht%($interval*5)) ?
						$this->contour_colour : $this->mint;
					if( (!isset($last_pt[$ht]) || 
						(hgtpt_distance($pt,$last_pt[$ht])>20))  )
					{
						# 08/02/05 changed parameters for slope_angle()
						# 12/02/05 put all the text drawing code in angle_text()
						$this->angle_text ($line_pts[$count], 8, $colour, $ht);

						$last_pt[$ht][] = $pt;
					}
					
					ImageLine($this->im,
						$line_pts[$count][0]['x'],
						$line_pts[$count][0]['y'],
						$line_pts[$count][1]['x'],
						$line_pts[$count][1]['y'], $colour); 
					ImageSetThickness($this->im,1);
				}
			}	
			elseif($this->debug==1)
					echo "<strong>Line doesn't intersect rect.</strong><br/>";
		}
		if($this->debug) echo "</p>";
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
	$image = new SRTMRenderer($bbox[0], $bbox[1], $bbox[2], $bbox[3],
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
