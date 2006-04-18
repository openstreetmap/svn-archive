<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################


require_once('osm.php');
require_once('latlong.php');
require_once('defines.php');
require_once('contours.php');
require_once('gpxnew.php');
require_once('dataset.php');
require_once('rules.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////

class Map
{
	var $bottomleft, 
		$topright,
		$latscale, // pixels per latitude unit
		$lonscale, // pixels per longitude unit
		$width, // pixels
		$height; // pixels

	function Map ($w, $s, $e, $n, $width, $height)
	{
		$this->bottomleft["long"] = $w;
		$this->bottomleft["lat"] = $s;
		$this->lonscale = $width/($e-$w);
		$this->latscale = $height/($n-$s);
		$this->width = $width; 
		$this->height = $height; 
	}

	function is_valid()
	{
		return $this->width>0 && $this->height>0;
	}

	function get_x($e)
	{
		return round(($e - $this->bottomleft['long']) * $this->lonscale);
	}
	
	function get_y($n)
	{
		return $this->height-
				round(($n-$this->bottomleft['lat']) * $this->latscale);
	}

	function get_point($ll)
	{
		return array ("x" => $this->get_x($ll["long"]), 
						"y" => $this->get_y($ll["lat"]) );
	}

	function get_latlon($pt)
	{
		$lon = $this->bottomleft['long'] +$pt['x']/$this->lonscale;
		$lat = $this->bottomleft['lat'] + 
			($this->height-$pt['y'])/$this->latscale;
		return array ('long' => $lon, 'lat' => $lat); 
	}

	function get_centre()
	{
		return $this->get_latlon
				(array('x'=>$this->width/2,'y'=>$this->height/2));
	}

	function within_map($latlon)
	{
		$pt['x'] = round($this->get_x($latlon['long']));
		$pt['y'] = round($this->get_y($latlon['lat']));
		return $this->pt_within_map($pt);
	}

	function pt_within_map($pt)
	{
		return $pt['x']>=0 && $pt['y']>=0 && 
				$pt['x']<$this->width && $pt['y']<$this->height;
	}

	function pt_right_of_map ($pt)
	{
		return $pt['x'] >= $this->width;
	}

	function pt_below_map ($pt)
	{
		return $pt['y'] >= $this->height;
	}

	function set_scale($newlonscale, $newlatscale)
	{
		$this->lonscale = $newlonscale;
		$this->latscale = $newlatscale;
	}

	// Return the lat-lon of the bottom left coordinate at a new scale
	// while keeping the centre constant
	function get_new_bottom_left($newlonscale, $newlatscale)
	{
		// Get the centre as a lat-lon
		$centre=$this->get_centre();
			// Coordinates of the new bottom left with respect to the centre
			// as the origin
			$pt['x'] = -$this->width/2; 
			$pt['y'] = $this->height/2;

			// Convert these to a lat-lon and return
			$new_bottom_left['long']=
				$centre['long']+round($pt['x']/($newlonscale/1000));
			$new_bottom_left['lat']=$centre['lat']-
				round($pt['y']/($newlatscale/1000));
		return $new_bottom_left; 
	}

	function centreToBottomLeft()
	{
		$pt['x'] = -$this->width/2; 
		$pt['y'] = 3*($this->height/2);

		// Convert these to a lat-lon and return
		return $this->get_latlon($pt);
	}

	function get_top_right()
	{
		return $this->get_latlon(array("x"=>$this->width,"y"=>0));
	}
}

class Image
{
	var $im, 
		$map, 
		$backcol, 
		$mapview,
		$zoom,
		$black,
		$gold,
		$ltyellow,
		$ltgreen,
		$contour_colour,
		$mint;

	var $debug;

	var $mapdata;
	var $tp;

	function Image ($w, $s, $e, $n, $width, $height,  $zoom, $ls=0, 
					$tp=0, $dbg=0)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);
		$this->zoom=$zoom;


		$this->mapdata = new Dataset();
		$this->mapdata->grab_direct_from_database($w, $s, $e, $n);
		
		// Make all segments inherit tags from their parent way
		$this->mapdata->give_segs_waytags();


		$this->landsat = $ls;

		# 14/11/04 changed ImageCreate() to ImageCreateTrueColor();
		# in GD2, when copying images (as we do for the icons), both source
		# and destination image must be either paletted or true colour.
		# (The icons are s)
		$this->im = ImageCreateTrueColor($this->map->width,$this->map->height);
		
		$this->backcol = ImageColorAllocate($this->im,220,220,220);
		$this->gold = ImageColorAllocate($this->im,255,255,0);
		$this->black = ImageColorAllocate($this->im,0,0,0);
		$this->ltyellow = ImageColorAllocate($this->im,255,255,192);
		$this->ltgreen = ImageColorAllocate($this->im,192,255,192);
		$this->contour_colour = ImageColorAllocate($this->im,192,192,0);
		$this->mint = ImageColorAllocate($this->im,0,192,64);


		//06/06/05 Replaced old brush loading with the following call
		//$this->segmenttypes=$this->load_segment_types();
		//130406 replaced again with style rules
		$this->styleRules = readStyleRules("freemap.xml");

		ImageFill($this->im,100,100,$this->backcol);
		$this->is_valid = true;

		// 06/06/05 removed hacky path style stuff: 
		// now in load_segment_types()
		$this->debug = $dbg;
		if($this->debug==1)
		{
			echo "<h1>Before processing</h1>\n";
			print_r($this->styleRules);
			echo "<br/>";
//			$this->processStyleRules();
			echo "<h1>After processing</h1>\n";
			print_r($this->styleRules);
			echo "<br/>";
		}
	}

	function draw()
	{
		if($this->landsat>=1)
			$this->draw_landsat();
		
		if($this->zoom>=12 && $this->zoom<=13)
			$this->draw_contours();
			
		$this->draw_segment_outlines();
		$this->draw_segments();
		$this->draw_points_of_interest();


		if($this->zoom >= 113)
			$this->draw_way_names();

		ImagePNG($this->im);
		ImageDestroy($this->im);
	}

	
	function draw_segments()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map

		$ids = array_keys($this->mapdata->segments);
		foreach ($ids as $id)
		{

			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['to']]);

			
			if ( (isset($this->mapdata->nodes
					[$this->mapdata->segments[$id]['to']]) 
						&&
				 isset($this->mapdata->nodes
				 	[$this->mapdata->segments[$id]['from']]) ) &&
			
			($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 )

			{
				$rgb=explode(",",
						$this->mapdata->segments[$id]["style"]["colour"]);

				if(count($rgb)==3)
				{
					$colour = ImageColorAllocate
							($this->im, $rgb[0],$rgb[1],$rgb[2]);
					$width = $this->getZoomLevelValue
							($this->mapdata->segments[$id]["style"]["width"]);

					ImageSetThickness($this->im, $width);
					// 07/06/05 Changed this to reflect the new way
					// that dashed lines are stored in the database.
					if(isset($this->mapdata->segments[$id]["style"]["dash"]))
					{

						$dash = 
							$this->makeDashPattern
							($this->mapdata->segments[$id]["style"]["dash"],
							$colour);
						ImageSetStyle($this->im,  $dash);


						ImageLine($this->im,$p[0]['x'],$p[0]['y'],
							$p[1]['x'],$p[1]['y'],
							IMG_COLOR_STYLED);
					}
					else
					{
						// 090406 outlines now done in their own function
						ImageLine($this->im,$p[0]['x'],$p[0]['y'],
								$p[1]['x'],$p[1]['y'],$colour);
					}
				}

				// do something with segment names here...
				// now only draw way names
				/*
				$this->draw_segment_name ($segment, 
								$segment['tags']['name'], false);
				*/
			}
		}	
	}

	// 090406 draw segment outlines first
	function draw_segment_outlines()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		$ids = array_keys($this->mapdata->segments);
		foreach ($ids as $id)
		{
					
			$this->mapdata->segments[$id]["style"] = 
				getStyle($this->styleRules,
							$this->mapdata->segments[$id]["tags"]);
			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['to']]);


			if ( (isset($this->mapdata->nodes[$this->mapdata->segments
								[$id]['to']]) &&
				 isset($this->mapdata->nodes[$this->mapdata->segments
				 						[$id]['from']]) ) &&
			
			($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 )

			{
				if($this->mapdata->segments[$id]["style"]["casing"]!=null)	
				{
					$rgb=explode(",",
						$this->mapdata->segments[$id]["style"]["casing"]);
					if(count($rgb)==3)
					{
						$colour = ImageColorAllocate
						($this->im, $rgb[0],$rgb[1],$rgb[2]);
						$width = $this->getZoomLevelValue
							($this->mapdata->segments[$id]["style"]["width"])
							+2;
					}
					ImageSetThickness($this->im, $width);
					ImageLine($this->im,$p[0]['x'],$p[0]['y'],
								$p[1]['x'],$p[1]['y'],$colour);
				}
			}
		}	
	}

	function draw_landsat()
	{
	}

	function draw_points_of_interest()
	{
		$allnamedata=array();

		// 070406 changed 'type' to ['tags']['class'] for nodes
		$ids = array_keys($this->mapdata->nodes);
		foreach ($ids as $id)
		{
			$this->mapdata->nodes[$id]["style"] = 
					getStyle($this->styleRules,
								$this->mapdata->nodes[$id]["tags"]);
			$p = $this->map->get_point($this->mapdata->nodes[$id]);

			//if ( $this->map->pt_within_map ($p))
			if(isset($this->mapdata->nodes[$id]["style"]["text"]))
			{
				$text = $this->getZoomLevelValue
							($this->mapdata->nodes[$id]["style"]["text"]);

				if($this->mapdata->nodes[$id]["style"]["image"])
				{
					$imgfile=$this->mapdata->nodes[$id]["style"]["image"];
					$imgsize=getimagesize($imgfile);
					$w = $imgsize[0];
					$h = $imgsize[1];


					if($text>=0)
					{
						$this->draw_image($p['x'],$p['y'], $w,$h,$imgfile,
										$this->mapdata->nodes
											[$id]['tags']['name']);
					}
				}
				// Names are displayed after everything else, so save 
				// them now, and draw them later in one go
				if($text>0)
				{
					$namedata['name'] = 
							$this->mapdata->nodes[$id]['tags']['name'];
					$namedata['fontsize'] = $text; 
					$namedata['x']= $p['x']+$w/2;
					$namedata['y']= $p['y']+$h/2;
					$allnamedata[] = $namedata;
				}
			}
		}
		$this->draw_names($allnamedata);
	}

	function draw_image($x,$y,$w,$h,$imgfile,$name)
	{
		
		$icon = ImageCreateFromPNG($imgfile);

		ImageCopy($this->im,$icon,$x-$w/2,$y-$h/2,0,0,$w,$h);
		ImageDestroy($icon);
    }

	function draw_names(&$namedata)
	{
		foreach($namedata as $name)
		{
			$this->draw_name($name['x'],$name['y'],
								$name['name'],$name['fontsize'],
								$this->black);
		}
	}

	# 16/11/04 new version for truetype fonts.
	function draw_name($x,$y,$name,$fontsize,$colour)
	{
		
		$name_arr = explode(' ',$name);
		
		for($count=0; $count<count($name_arr)-1; $count++)
		{
			ImageTTFText($this->im, $fontsize, 0, $x, $y, $colour, 
							TRUETYPE_FONT,
							$name_arr[$count]);

			// Get the height of the next word, so we know how far down to
			// draw it.	
			$bbox = ImageTTFBBox($fontsize,0,TRUETYPE_FONT,$name_arr[$count+1]);
			$y += ($bbox[1]-$bbox[7])+FONT_MARGIN;
		}
			
		// Finally draw the last word
		@ImageTTFText($this->im, $fontsize, 0, $x, $y, $colour, 
							TRUETYPE_FONT, $name_arr[$count]);
	} 

	// name drawing stuff
	// 290306 add true/false success code, also add $force to force drawing
	// even if the segment isn't long enough
	//
	// Parameters: segment, name and force (force the name to be written even
	// if it occupies more screen space than the segment)
	// 
	// name not necessarily the segment name: it may be just one word of a 
	// way name. 
	// eg. in a way, draw different words of the way name on different segments

	function draw_segment_name (&$seg, $name, $force)
	{
		$succ=false;

		if($name)
		{
			$bbox=ImageTTFBBox(8,0,TRUETYPE_FONT,$name);
			$text_width = line_length($bbox[6],$bbox[7],$bbox[4],$bbox[5]);
			$text_height = line_length($bbox[6],$bbox[7],$bbox[0],$bbox[1]);

			// Work out position from provided segment
			$p = array();
			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$seg['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$seg['to']]);
			$len = line_length($p[0]['x'],$p[0]['y'],$p[1]['x'],$p[1]['y']);

			$av['x'] = $p[0]['x'] + (($p[1]['x'] - $p[0]['x'])/2);
			$av['y'] = $p[0]['y'] + (($p[1]['y'] - $p[0]['y'])/2);

			$segwidth=$seg["style"]["width"]+2;

			if(preg_match("/^[A-Z]+[0-9]+$/",$name))
			{
				$x1=$av['x']-$text_width/2;
				$y1=$av['y']-$text_height/2;
				$x2=$av['x']+$text_width/2;
				$y2=$av['y']+$text_height/2;
				ImageFilledRectangle($this->im,$x1,$y1,$x2,$y2, $this->black);
				ImageTTFText($this->im, 8, 0,  
							$x1, $y2, $this->gold,
							TRUETYPE_FONT, $name);
				$succ=true;
			}
			elseif($len*1.25>=$text_width || $force)
			{
				$this->interior_angle_text($p, 8, $this->black, $name,
							$segwidth, $text_height);
				$succ=true;
			}
		}
		return $succ;
	}

	// For drawing angle text within a segment
	// For this, we need the segment width and the text height to correctly
	// centre the text within the segment
	function interior_angle_text ($p, $fontsize, $colour, $text,
								$segwidth, $text_height)
	{
		$i = ($p[1]['x'] > $p[0]['x']) ? 0:1;
		$p[$i]['y'] = $p[$i]['y'] + $segwidth/2 + $text_height/2;
		$this->angle_text($p,$fontsize,$colour,$text);
	}

	function draw_way_names()
	{
		foreach($this->mapdata->ways as $way)
		{
			if($way['tags']['name'] && $way['tags']['name']!="")
				$this->draw_way_name($way);
		}
	}
				
	// Draw the name of a way
	function draw_way_name (&$way)
	{
		// middle segment
		$i = count($way['segs']) / 2;
		//$curseg = $this->mapdata->segments[$way['segs'][$i]];

		// quick and messy way to get the most suitable segment
		$bbox=ImageTTFBBox(8,0,TRUETYPE_FONT,$name);
		$text_width = line_length($bbox[6],$bbox[7],$bbox[4],$bbox[5]);
		$text_height = line_length($bbox[6],$bbox[7],$bbox[0],$bbox[1]);

		$maxlength=0;
		$longestseg=null;
		$p = array();
		$segcount=0;
		$lastdiff=99999;

		foreach ($way['segs'] as $seg)
		{
			$segment = $this->mapdata->segments[$seg];
			// Work out position from provided segment
			$p[0] = $this->map->get_point ($segment['from']);
			$p[1] = $this->map->get_point ($segment['to']);
			$length = line_length($p[0]['x'],$p[0]['y'],$p[1]['x'],$p[1]['y']);

			// how near the middle of the way are we?
			$diff = abs($i-$segcount);

			// we're trying to find a suitably long segment near the middle of
			// the way
			if ($diff<$lastdiff && $length>=$text_width)
			{
				$maxlength=$length;
				$longestseg=$segment;
				$lastdiff=$diff;
			}

			$segcount++;
		}

		if($longestseg)
			$this->draw_segment_name($longestseg,$way['tags']['name'],false);

		// try to draw name on whole segment
		// if fail, then loop through segments starting at the current one,
		// drawing a different word on each

		/*
		if(!$this->draw_segment_name($curseg,$way['tags']['name'],false))
		{
			$namewords = explode(" ",$way['tags']['name']);
			$wc=0;
			$cont=true;
			while($cont)	
			{
				$curseg = $this->mapdata->segments[$way['segs'][$i]];

				$this->draw_segment_name($curseg,$namewords[$wc],true);

				if (++$i>=count($way['segs']) || ++$wc==count($namewords))
					$cont=false;
			}
		}
		*/
	}

	function angle_text ($p, $fontsize, $colour, $text)
	{
		$angle=slope_angle($p[0]['x'], $p[0]['y'], $p[1]['x'], $p[1]['y']);
		$i = ($p[1]['x'] > $p[0]['x']) ? 0:1;
		ImageTTFText($this->im, $fontsize, -$angle, $p[$i]['x'], $p[$i]['y'],
						$colour, TRUETYPE_FONT, $text);

		return $i;
	}

	// goes through all the style rules and makes the colours and text size
	// for this zoom level
	// do here, rather than in the rules file parser, to avoid having to
	// messily pass the image reference and zoom level over to the parser
	function processStyleRules()
	{
		for($count=0; $count<count($this->styleRules); $count++)
		{
			if($this->styleRules[$count]["colour"])
			{
				$rgb=explode(",",$this->styleRules[$count]["colour"]);

				$this->styleRules[$count]["colour"]=
					ImageColorAllocate($this->im, $rgb[0],$rgb[1],$rgb[2]);
				$this->styleRules[$count]["dash"]=
						$this->makeDashPattern
							($this->styleRules[$count]["dash"],
							$this->styleRules[$count]["colour"]);
			}
			if($this->styleRules[$count]["casing"])
			{	
				$rgb=explode(",",$this->styleRules[$count]["casing"]);
				$this->styleRules[$count]["casing"]=
					ImageColorAllocate($this->im, $rgb[0],$rgb[1],$rgb[2]);
			}
			if($this->styleRules[$count]["width"])
			{
				$this->styleRules[$count]["width"] = $this->getZoomLevelValue
							($this->styleRules[$count]["width"]);
			}
			if($this->styleRules[$count]["text"])
			{
				$this->styleRules[$count]["text"] = $this->getZoomLevelValue
						($this->styleRules[$count]["text"]);
			}
		}
	}

	function makeDashPattern($dash, $colour)
	{
		if($dash && $colour)
		{
			list($on,$off)=explode(",",$dash);
			$dashpattern=array();
			for($count2=0; $count2<$on; $count2++)
				$dashpattern[$count2] = $colour;
			for($count2=0; $count2<$off;$count2++)
				$dashpattern[$on+$count2] = IMG_COLOR_TRANSPARENT;
			return $dashpattern;
		}
		return null;
	}

	// for attributes which vary depending on zoom level,
	// e.g. line width, feature text
	function getZoomLevelValue($valuelist)
	{
		$values=explode(",",$valuelist);
		return ($this->zoom<=count($values)) ? $values[$this->zoom-1]:
												$values[count($values)-1];
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
					
					ImageLine($this->im,$line_pts[$count][0]['x'],
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

?>
