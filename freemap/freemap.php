<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

// WMS compliant (ish) Freemap renderer
// 010906 This now deals with all of Landsat, SRTM, OSM polygons (areas) and
// regular OSM ways. This allows us to layer it all in the correct order, e.g.
// areas first, then SRTM, then OSM data. Areas could be moved into a separate
// script, and then everything composited with OpenLayers, but the problem
// with that is that it would be inefficient (we would have to grab data from
// the database twice from two scripts, rather than just once from one script)

require_once('osmxml.php');
require_once('latlong.php');
require_once('defines.php');
require_once('gpxnew.php');
require_once('dataset.php');
require_once('rules.php');
require_once('Map.php');
require_once('contours.php');
require_once('functions.php');
require_once('Painter.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////


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
	var $landsat, $poly, $srtm, $datasrc, $location;

	function Image ($w, $s, $e, $n, $width, $height, $stylexml,
					$lsat=0, $pl=0, $con=1, $dsrc="db", 
					$loc="http://www.openstreetmap.org/api/0.3/map", $dbg=0)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);
		//$this->zoom=$zoom;
		$this->painter = new MagickPainter();

		$this->landsat = $lsat;
		$this->poly = $pl;
		$this->srtm = $con;

		$this->datasrc = $dsrc;
		$this->location = $loc;

		// 150506 recalculate zoom from input pixel and longitude width
		// see Steve's email of 01/02/06
		$this->zoom = round(log((360*$width)/($e-$w),2)-9);

		# 14/11/04 changed ImageCreate() to ImageCreateTrueColor();
		# in GD2, when copying images (as we do for the icons), both source
		# and destination image must be either paletted or true colour.
		# (The icons are s)
		$this->backcol = 
			$this->painter->createImage($this->map->width*
				(2*$this->extensionFactor()+1),
									$this->map->height*
				(2*$this->extensionFactor()+1), 220, 220, 220);
		
		$this->gold = $this->painter->getColour(255,255,0);
		$this->black = $this->painter->getColour(0,0,0);
		$this->ltyellow = $this->painter->getColour(255,255,192);
		$this->ltgreen = $this->painter->getColour(192,255,192);
		$this->contour_colour = $this->painter->getColour(192,192,0);
		$this->mint = $this->painter->getColour(0,192,64);


		if($this->zoom>=10)
		{
			//$this->mapdata = new Dataset();
			//$this->mapdata->grab_direct_from_database($w, $s, $e, $n, $zoom);
			$this->mapdata=grabOSM($w,$s,$e,$n,$this->datasrc, $this->zoom,
										$this->location);
		
			// Make all segments inherit tags from their parent way
			//$this->mapdata->give_segs_waytags();
			//06/06/05 Replaced old brush loading with the following call
			//$this->segmenttypes=$this->load_segment_types();
			//130406 replaced again with style rules
			$this->styleRules = readStyleRules($stylexml);
		}

		$this->is_valid = true;

		// 06/06/05 removed hacky path style stuff: 
		// now in load_segment_types()
		$this->debug = $dbg;
	}

	function draw()
	{
		
		if($this->zoom>=10)
		{
		//$this->load_segment_styles();
		$this->load_way_styles();

		// WARNING !!! This screws up the keys of associative arrays !!!
		//usort($this->mapdata["segments"],"zIndexCmp");

		// Draw Landsat if required, first
		if($this->landsat==1)
			$this->draw_landsat();

		// Then areas
		/*
		if($this->poly==1)
			$this->draw_areas();
		*/

		// Contours should come here
		if($this->srtm==1)
			$this->draw_contours();

		$this->draw_way_outlines();
		$this->draw_ways();
		//$this->draw_unwayed_segments();
		$this->draw_points_of_interest();


		if($this->zoom >= 13)
			$this->draw_way_names();

		}
	
		$this->painter->crop(
				$this->map->width*$this->extensionFactor(),
				$this->map->height*$this->extensionFactor(),
				$this->map->width,
				$this->map->height
							);

		$this->painter->renderImage();
	}

	
	function draw_unwayed_segments()
	{

		# Only attempt to draw the line if at least one of the points
		# is within the map

		$ids = array_keys($this->mapdata["segments"]);
		foreach ($ids as $id)
		{
			// only do segments which don't belong to ways
			if(!isset($this->mapdata["segments"][$id]["way"]))
			{

				$width = 1;
				$p[0] = $this->map->get_point
				($this->mapdata["nodes"][$this->mapdata["segments"][$id]['from']]);
				$p[1] = $this->map->get_point
				($this->mapdata["nodes"][$this->mapdata["segments"][$id]['to']]);

			
				if ( (isset($this->mapdata["nodes"]
					[$this->mapdata["segments"][$id]['to']]) 
						&&
				 isset($this->mapdata["nodes"]
				 	[$this->mapdata["segments"][$id]['from']]) ) &&
				$width>0	
					 )

				{
					$rgb = array (128,128,128);

					$colour = $this->painter->getColour
							( $rgb[0],$rgb[1],$rgb[2]);


					$this->painter->drawLine(
					$this->cnvX($p[0]['x']), 
					$this->cnvY($p[0]['y']),
					$this->cnvX($p[1]['x']),
					$this->cnvY($p[1]['y']),
								$colour, $width);
				}
			}
		}	
	}

	function draw_ways()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		$ids = array_keys($this->mapdata["ways"]);
		foreach ($ids as $id)
		{
			$curStyle = getStyle($this->styleRules,
							$this->mapdata["ways"][$id]["tags"]);
			// Not areas
			if($curStyle["render"]!="area")
			{
				$width = $this->getZoomLevelValue
							($curStyle["width"]);

				foreach($this->mapdata["ways"][$id]["segs"] as $segid)
				{
					//echo "<strong>wayseg:</strong> $segid<br/>";
					if(isset($this->mapdata["segments"][$segid]))
					{
						//echo "<strong>Exists</strong><br/>";

						// specify the segment's parent way
						$this->mapdata["segments"][$segid]["way"] = $id;
						$seg = $this->mapdata["segments"][$segid];
						$p[0] = $this->map->get_point 
							($this->mapdata["nodes"][$seg['from']]);
						$p[1] = $this->map->get_point 
							($this->mapdata["nodes"][$seg['to']]);

			
						if ( (isset($this->mapdata["nodes"] [$seg['to']]) 
							&& isset($this->mapdata["nodes"] [$seg['from']]) ) &&
							$width>0	)

						{
							$rgb=explode(",", $curStyle["colour"]);

							if(count($rgb)==3)
							{
								$colour = $this->painter->getColour
								($rgb[0],$rgb[1],$rgb[2]);

								//ImageSetThickness($this->im, $width);
								// 07/06/05 Changed this to reflect the new way
								// that dashed lines are stored in the database.
						   		if(isset ($curStyle["dash"]))
								{
//									echo "DASHED LINE: data = $curStyle[dash] $rgb[0] $rgb[1] $rgb[2] $width<br/>";
									$this->painter->drawDashedLine	
									($this->cnvX($p[0]['x']),
									 $this->cnvY($p[0]['y']),
									 $this->cnvX($p[1]['x']),
									 $this->cnvY($p[1]['y']),
							  	     $curStyle["dash"], 
									 $colour,
									 $width);
								}

								else
								{
									//090406 outlines now done in their own 
									// function
									$this->painter->drawLine	
									($this->cnvX($p[0]['x']),
									 $this->cnvY($p[0]['y']),
									 $this->cnvX($p[1]['x']),
									 $this->cnvY($p[1]['y']),
									 $colour,
									 $width);
								}
							}
						}
					}
				}
			}
		}	
	}

	function draw_areas()
	{
		$ids = array_keys($this->mapdata["ways"]);
		foreach ($ids as $id)
		{
			$curStyle = getStyle($this->styleRules,
							$this->mapdata["ways"][$id]["tags"]);
			// Areas only
			if($curStyle["render"]=="area")
			{
				// This will only work if all segments are aligned in the
				// same direction!
				$curarea = array();
				foreach($this->mapdata["ways"][$id]["segs"] as $segid)
				{
					if(isset($this->mapdata["segments"][$segid]))
					{
						$seg = $this->mapdata["segments"][$segid];
						$this->mapdata["segments"][$segid]["way"] = $id;
						if(isset($this->mapdata["nodes"][$seg['from']]) &&
						   isset($this->mapdata["nodes"][$seg['to']]))
						{
							$p[0] = $this->map->get_point 
								($this->mapdata["nodes"][$seg['from']]);
							$p[1] = $this->map->get_point 
								($this->mapdata["nodes"][$seg['to']]);
	
							if(count($curarea)==0)
							{
								$curarea[] = $this->cnvX($p[0]['x']);
								$curarea[] = $this->cnvY($p[0]['y']);
							}
							$curarea[] = $this->cnvX($p[1]['x']);
							$curarea[] = $this->cnvY($p[1]['y']);
						}	
					}
				}

				$rgb=explode(",", $curStyle["colour"]);

				if(count($rgb)==3)
				{
					$colour = $this->painter->getColour
								($rgb[0],$rgb[1],$rgb[2]);
					$this->painter->drawPolygon($curarea, $colour);
				}
			}
		}	
	}
	
	function load_segment_styles()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		$ids = array_keys($this->mapdata["segments"]);
		foreach ($ids as $id)
		{
					
			$this->mapdata["segments"][$id]["style"] = 
				getStyle($this->styleRules,
							$this->mapdata["segments"][$id]["tags"]);
		}
	}

	function load_way_styles()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		$ids = array_keys($this->mapdata["ways"]);
		foreach ($ids as $id)
		{
			/*
			$this->mapdata["ways"][$id]["style"] = 
				getStyle($this->styleRules,
							$this->mapdata["ways"][$id]["tags"]);
							*/
		}
	}

	// 090406 draw segment outlines first

	function draw_way_outlines()
	{
		$ids = array_keys($this->mapdata["ways"]);
		foreach ($ids as $id)
		{
			$curStyle = getStyle($this->styleRules,
							$this->mapdata["ways"][$id]["tags"]);
			$width = $this->getZoomLevelValue ($curStyle["width"]);
					
			foreach($this->mapdata["ways"][$id]['segs'] as $segid)
			{
				$seg = $this->mapdata["segments"][$segid];

				if($seg)
				{
					$p[0] = $this->map->get_point 
						($this->mapdata["nodes"][$seg['from']]);
					$p[1] = $this->map->get_point
						($this->mapdata["nodes"][$seg['to']]);


					if ($width>0 &&  
						(isset($this->mapdata["nodes"][$seg['to']]) &&
				 	 	 isset($this->mapdata["nodes"][$seg['from']]) ) 
					 )

					{
						if($curStyle["casing"]!=null)	
						{
							$rgb=explode(",", 
								$curStyle["casing"]);
							if(count($rgb)==3)
							{
								$colour = $this->painter->getColour 
									($rgb[0],$rgb[1],$rgb[2]);
							}
							$this->painter->drawLine(
								$this->cnvX($p[0]['x']),
								$this->cnvY($p[0]['y']),
								$this->cnvX($p[1]['x']),
								$this->cnvY($p[1]['y']),
								$colour, $width+2);
						}
					}
				}
			}
		}	
	}

	function draw_points_of_interest()
	{
		$allnamedata=array();

		// 070406 changed 'type' to ['tags']['class'] for nodes
		$ids = array_keys($this->mapdata["nodes"]);
		foreach ($ids as $id)
		{
			$w = 0;
			$h = 0;
			$this->mapdata["nodes"][$id]["style"] = 
					getStyle($this->styleRules,
								$this->mapdata["nodes"][$id]["tags"]);
			$p = $this->map->get_point($this->mapdata["nodes"][$id]);

			//if ( $this->map->pt_within_map ($p))
			if(isset($this->mapdata["nodes"][$id]["style"]["text"]))
			{
				$text = $this->getZoomLevelValue
							($this->mapdata["nodes"][$id]["style"]["text"]);

				if($this->mapdata["nodes"][$id]["style"]["image"])
				{
					$imgfile=$this->mapdata["nodes"][$id]["style"]["image"];

					if($text>=0)
					{
						$this->painter->drawImage($this->cnvX($p['x']),
												$this->cnvY($p['y']),$imgfile,
													"png");
					}
				}
				// Names are displayed after everything else, so save 
				// them now, and draw them later in one go
				if($text>0)
				{
					$namedata['name'] = 
							$this->mapdata["nodes"][$id]['tags']['name'];
					$namedata['fontsize'] = $text; 
					$namedata['x']= $p['x']+$w/2;
					$namedata['y']= $p['y']+$h/2;
					$allnamedata[] = $namedata;
				}
			}
		}
		$this->draw_names($allnamedata);
	}

	function draw_names(&$namedata)
	{
		foreach($namedata as $name)
		{
			$this->draw_name($this->cnvX($name['x']),$this->cnvY($name['y']),
								$name['name'],$name['fontsize'],
								$this->black);
		}
	}

	# 16/11/04 new version for truetype fonts.
	function draw_name($x,$y,$name,$fontsize,$colour)
	{
		$this->painter->drawMultiword($x,$y,$name,$fontsize,$colour);		
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
			list($text_width,$text_height) = 
					$this->painter->getTextDimensions(8,$name);

			// Work out position from provided segment
			$p = array();
			$p[0] = $this->map->get_point
				($this->mapdata["nodes"][$seg['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata["nodes"][$seg['to']]);
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

				$this->painter->drawFilledRectangle
							($this->cnvX($x1),$this->cnvY($y1),
										$this->cnvX($x2),$this->cnvY($y2), 
										$this->black);
				$this->painter->drawText( $this->cnvX($x1), $this->cnvY($y2), 
											8,$name, $this->gold);
				$succ=true;
			}
			elseif($len*1.25>=$text_width || $force)
			{
				// 010906 only put the text inside the segment for wide
				// segments. Otherwise draw on top.
				if($segwidth>=8)
				{
					$this->interior_angle_text($p, 8, $this->black, $name,
							$segwidth, $text_height);
				}
				else
				{
					$this->painter->angleText($this->cnvX($p[0]['x']),
											  $this->cnvY($p[0]['y']),
											  $this->cnvX($p[1]['x']),
											  $this->cnvY($p[1]['y']),
											  8, $this->black, $name);
				}
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
		$this->painter->angleText($this->cnvX($p[0]['x']),
											  $this->cnvY($p[0]['y']),
											  $this->cnvX($p[1]['x']),
											  $this->cnvY($p[1]['y']),
											  $fontsize, $colour, $text);
	}

	function draw_way_names()
	{
		foreach($this->mapdata["ways"] as $way)
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
		//$curseg = $this->mapdata["segments"][$way['segs'][$i]];

		// quick and messy way to get the most suitable segment
			
		list($text_width,$text_height) = 
					$this->painter->getTextDimensions(8,$name);

		$maxlength=0;
		$longestseg=null;
		$p = array();
		$segcount=0;
		$lastdiff=99999;

		foreach ($way['segs'] as $seg)
		{
			$segment = $this->mapdata["segments"][$seg];
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
				$curseg = $this->mapdata["segments"][$way['segs'][$i]];

				$this->draw_segment_name($curseg,$namewords[$wc],true);

				if (++$i>=count($way['segs']) || ++$wc==count($namewords))
					$cont=false;
			}
		}
		*/
	}

	// for attributes which vary depending on zoom level,
	// e.g. line width, feature text
	function getZoomLevelValue($valuelist)
	{
		$values=explode(",",$valuelist);
		return ($this->zoom<=count($values)) ? $values[$this->zoom-1]:
												$values[count($values)-1];
	}

	function cnvX($x)
	{
		return $x+$this->map->width*$this->extensionFactor();
	}

	function cnvY($y)
	{
		return $y+$this->map->height*$this->extensionFactor();
	}

	function extensionFactor()
	{
		return ($this->zoom<13) ? 0.5 : 0.5*pow(2,$this->zoom-13);
	}	

	function draw_landsat()
	{
		$bottomleft_ll = $this->map->bottomleft;
		$topright_ll = $this->map->get_top_right();

		$this->painter->drawImage ($this->cnvX(0), $this->cnvY(0),
//				("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=".
				("http://landsat.openstreetmap.org:3128/wms.cgi?".
								"request=GetMap&width=".
	 	                        $this->map->width."&height=".$this->map->height.
	 	                        "&layers=modis,global_mosaic&styles=".
								"&srs=EPSG:4326&".
	 	                        "format=image/jpeg&bbox=$bottomleft_ll[long],".
								"$bottomleft_ll[lat],".
								"$topright_ll[long],$topright_ll[lat]".
								"jpeg") );
	}

	// SRTM STUFF BEGINS HERE

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



		for($ht=$start_ht; $ht<=$end_ht; $ht+=$interval)
		{

				
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

					$colour = ($ht%($interval*5)) ?
						$this->contour_colour : $this->mint;
					if( (!isset($last_pt[$ht]) || 
						(hgtpt_distance($pt,$last_pt[$ht])>20))  )
					{
						# 08/02/05 changed parameters for slope_angle()
						# 12/02/05 put all the text drawing code in angle_text()
						$this->painter->angleText($this->cnvX
								($line_pts[$count][0]['x']),
									  $this->cnvY($line_pts[$count][0]['y']),
									  $this->cnvX($line_pts[$count][1]['x']),
									  $this->cnvY($line_pts[$count][1]['y']),
											  8, $colour, $ht);

						$last_pt[$ht][] = $pt;
					}
		
					$this->painter->drawLine(
						$this->cnvX($line_pts[$count][0]['x']),
						$this->cnvY($line_pts[$count][0]['y']),
						$this->cnvX($line_pts[$count][1]['x']),
						$this->cnvY($line_pts[$count][1]['y']), $colour, 1);
				}
			}	
		}
	}

	// SRTM STUFF ENDS HERE
}

function zIndexCmp($a,$b)
{
	return ($a["style"]["z-index"] > $b["style"]["z-index"]) ? 1:-1;
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("width" => 400, 
			"height" => 320,
			"debug" => 0,
			// data source - "db", "osm" or "file"
			"datasrc" => "db", 
			// API URL or local filename
			"location" => "http://www.openstreetmap.org/api/0.3/map",
			"bbox" => "-0.75,51.02,-0.7,51.07",
			"layers" => "areas,srtm,osm" );

$layer_array = explode(",",$layers);

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

$layer_array = explode(",",$inp['layers']);
$bbox = explode(",",$inp['bbox']);

if($bbox[0]<-180 || $bbox[0]>180 || $bbox[2]<-180 || $bbox[2]>180 ||
	 $bbox[1]<-90 || $bbox[1]>90 || $bbox[3]<-90 || $bbox[3]>90)
{
	$error = "Invalid latitude and/or longitude!";
}

$landsat=(in_array("landsat",$layer_array)) ? 1:0;
$poly=(in_array("areas",$layer_array))  ? 1:0;
$srtm=(in_array("srtm",$layer_array)) ? 1:0;
$stylexml = "freemap.xml";

$image = new Image($bbox[0], $bbox[1], $bbox[2], $bbox[3],
						$inp["width"],$inp["height"],$stylexml,
						$landsat,$poly,$srtm,$inp["datasrc"],
						$inp["location"],$inp["debug"]);

if(isset($error))
{
	echo "<html><head><title>Error!</title></head><body>$error</body></html>";
}
else
{
	if (!isset($_GET['debug']))
		header('Content-type: image/png'); 
	
	$image->draw();
}

function valid_input($field,$value)
{
	if($field=="bbox")
	{
		return preg_match("/^[\.\-\d]+,[\.\-\d]+,[\.\-\d]+,[\.\-\d]+$/",$value);
	}
	elseif($field=="width" || $field=="height")
	{
		return wholly_numeric($value);
	}
	return $value;
}
?>
