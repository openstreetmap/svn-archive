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
		$routetypes,
		$mv,
		$darkred,
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
		$this->mv=$zoom;


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
		$this->darkred = $this->black;
		$this->ltyellow = ImageColorAllocate($this->im,255,255,192);
		$this->ltgreen = ImageColorAllocate($this->im,192,255,192);
		$this->contour_colour = ImageColorAllocate($this->im,192,192,0);
		$this->mint = ImageColorAllocate($this->im,0,192,64);


		//06/06/05 Replaced old brush loading with the following call
		$this->routetypes=$this->load_route_types();

		ImageFill($this->im,100,100,$this->backcol);
		$this->is_valid = true;

		// 06/06/05 removed hacky path style stuff: 
		// now in load_route_types()
		$this->debug = $dbg;
	}

	function draw()
	{
		if($this->landsat>=1)
			$this->draw_landsat();
		
		if($this->mv>=112 && $this->mv<=113)
			$this->draw_contours();
			
		$this->draw_route_outlines();
		$this->draw_routes();
		$this->draw_points_of_interest();

		if($this->mv >= 13)
			$this->draw_way_names();

		ImagePNG($this->im);
		ImageDestroy($this->im);
	}

	
	function draw_routes()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		foreach ($this->mapdata->segments as $id=>$segment)
		{
			$segment["type"] = allocatetype($segment["tags"]);

			if(!isset($this->routetypes[$segment["type"]]))
				$segment["type"] = 0;

			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$segment['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$segment['to']]);

			
			if ( (isset($this->mapdata->nodes[$segment['to']]) &&
				 isset($this->mapdata->nodes[$segment['from']]) ) &&
			
			($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 )

			{
		
				// 07/06/05 Changed this to reflect the new way
				// that dashed lines are stored in the database.
				if(!isset($this->routetypes [$segment['type']]['pathstyle']))
				{

					// 090406 outlines now done in their own function
					ImageSetThickness($this->im,
								$this->routetypes[$segment['type']]['width']);

					$colour=$this->routetypes [$segment['type']]['colour'];

					ImageLine($this->im,$p[0]['x'],$p[0]['y'],
								$p[1]['x'],$p[1]['y'],$colour);
				}
				else
				{
					ImageSetStyle($this->im, 
								$this->routetypes 
								[$segment['type']]['pathstyle']);
					ImageSetThickness($this->im, 
							$this->routetypes[$segment['type']]['width']);
					ImageLine($this->im,$p[0]['x'],$p[0]['y'],
							$p[1]['x'],$p[1]['y'],
							IMG_COLOR_STYLED);
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

	// 090406 draw route outlines first
	function draw_route_outlines()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		foreach ($this->mapdata->segments as $id=>$segment)
		{
			$segment["type"] = allocatetype($segment["tags"]);

			if(!isset($this->routetypes[$segment["type"]]))
				$segment["type"] = 0;

			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$segment['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$segment['to']]);

			
			if ( (isset($this->mapdata->nodes[$segment['to']]) &&
				 isset($this->mapdata->nodes[$segment['from']]) ) &&
			
			($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 )

			{
				if(isset($this->routetypes[$segment['type']]['outlinecolour']))
				{

					ImageSetThickness($this->im,
								$this->routetypes[$segment['type']]['width']+2);

					$colour=
						$this->routetypes [$segment['type']]['outlinecolour'];

					ImageLine($this->im,$p[0]['x'],$p[0]['y'],
								$p[1]['x'],$p[1]['y'],$colour);
				}
			}
		}	
	}

	// 06/06/05 New function. Comments for load_reps(), above, also apply 
	// here.
	function load_route_types()
	{

		// Bits 0,1 foot permissions (no=0, unofficial=1, yes=2)
		// Bits 2,3 horse permissions (ditto)
		// Bits 4,5 cycle permissions (ditto)
		// Bits 6 car permissions (off or on only)
		// Bits 7-9 class (path, unsurfaced, estate, minor, street, B, A, 
		// motorway) 

		$routetypes = array (
		// footpath 000 0 00 00 10 = 2
			2 => array ("typename"=>"footpath",
						"colour"=>ImageColorAllocate($this->im,255,0,0),
						"width"=>1,
						"dashon"=>2,
						"dashoff"=>2 ),

		
		// bridleway 000 0 10 10 10 = 42
			42 => array ("typename"=>"bridleway",
						"colour"=>ImageColorAllocate($this->im,255,0,0),
						"width"=>2,
						"dashon"=>6,
						"dashoff"=>6 ),

		// byway 001 1 10 10 10 = 2+8+32+64+128 = 234
			234 => array ("typename"=>"byway",
						"colour"=>ImageColorAllocate($this->im,255,0,0),
						"width"=>2,
						"dashon"=>0,
						"dashoff"=>0 ),

		// residential 010 1 10 10 10 = 2+8+32+64+256 = 362 
			362 => array ("typename"=>"residential",
						"colour"=>$this->backcol,
						"outlinecolour"=>ImageColorAllocate($this->im,0,0,0),
						"width"=>1,
						"dashon"=>0,
						"dashoff"=>0 ),

		// minor road 011 1 10 10 10 = 2+8+32+64+128+256= 490
			490 => array ("typename"=>"minor road",
		//				"colour"=>ImageColorAllocate($this->im,240,255,0),
						"colour"=>ImageColorAllocate($this->im,192,192,192),
//						"colour"=>$this->ltyellow,
						"outlinecolour"=>ImageColorAllocate($this->im,0,0,0),
						"width"=>2,
						"dashon"=>0,
						"dashoff"=>0 ),

		// B road 101 1 10 10 10 = 2+8+32+64+128+512 = 746 
			746 => array ("typename"=>"B road",
						"colour"=>ImageColorAllocate($this->im,253,191,111),
						"outlinecolour"=>ImageColorAllocate($this->im,0,0,0),
						"width"=>4,
						"dashon"=>0,
						"dashoff"=>0 ),

		// A road 110 1 10 10 10 = 2+8+32+64+256+512 = 874 
			874 => array ("typename"=>"A road",
						"colour"=>ImageColorAllocate($this->im,251,128,95),
						"outlinecolour"=>ImageColorAllocate($this->im,0,0,0),
						"width"=>4,
						"dashon"=>0,
						"dashoff"=>0 ),

		// permissive footpath 000 0 00 00 01 = 1
			1 => array ("typename"=>"permissive footpath",
						"colour"=>ImageColorAllocate($this->im,192,96,0),
						"width"=>1,
						"dashon"=>1,
						"dashoff"=>2 ),

		// permissive bridleway 000 0 01 01 01 = 21 
			21 => array ("typename"=>"permissive bridleway",
						"colour"=>ImageColorAllocate($this->im,192,96,0),
						"width"=>1,
						"dashon"=>1,
						"dashoff"=>6 ),

		// motorway 111 1 00 00 00 = 64+128+256+512= 960 
			960 => array ("typename"=>"motorway",
						"colour"=>ImageColorAllocate($this->im,128,155,192),
						"outlinecolour"=>ImageColorAllocate($this->im,0,0,0),
						"width"=>4,
						"dashon"=>0,
						"dashoff"=>0 ) ,

			0 => array ("typename"=>"unknown",
						"colour"=>ImageColorAllocate($this->im,128,128,128),
						"width"=>2,
						"dashon"=>0,
						"dashoff"=>0 )
		);

		foreach($routetypes as $i => $routetype)
		{
			if($routetypes[$i]['dashon'] && $routetypes[$i]['dashoff'])
			{
				$routetypes[$i]['pathstyle']=array();
				for($count2=0; $count2<$routetypes[$i]['dashon']; $count2++)
				{
					$routetypes[$i]['pathstyle'][$count2] = 
						$routetypes[$i]['colour'];
				}
				for($count2=0; $count2<$routetypes[$i]['dashoff'];$count2++)
				{
					$routetypes[$i]['pathstyle']
						[$routetypes[$i]['dashon']+$count2] = 
						IMG_COLOR_TRANSPARENT;	
				}
			}

			if($this->mv>=13)
				$routetypes[$i]['width']+=($this->mv-11)*2;
		}


		return $routetypes;
	}

 
	function draw_landsat()
	{
		/*
		$bottomleft_ll = $this->map->bottomleft;
		$topright_ll = $this->map->get_top_right();
		$img = ImageCreateFromJPEG
			("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&width=".
			$this->map->width."&height=".$this->map->height.
			"&layers=global_mosaic&styles=&srs=EPSG:4326&".
			"format=image/jpeg&bbox=$bottomleft_ll[lon],$bottomleft_ll[lat],".
			"$topright_ll[lon],$topright_ll[lat]");
		ImageCopy($this->im,$img,0,0,0,0,MAP_WIDTH,MAP_HEIGHT);
		*/
	}

	function draw_points_of_interest()
	{
		$allnamedata=array();
		$usedscale = ($this->mv > 14) ? 14: $this->mv;

		$images = array
			("hill" => array("images/peak_small.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,10,10,10)),
			"farm" => array("images/farm.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"pub" => array("images/pub.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,8,8)),
			"viewpoint" => array("images/viewpoint.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0)),
			"church" => array("images/church.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0)),
			"railway station" => array("images/station.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,8,8,8)),
			"mast" => array("images/mast.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0)),
			"car park" => array("images/carpark.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"point of interest" => array("images/interest.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"caution" => array("images/caution.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)),
			"amenity" => array("images/amenity.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"waypoint" => array("images/waypoint.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
				 -1,
				 -1,
				-1)),
			"node" => array("images/waypoint.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)),
			"tea shop" => array("images/teashop.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"restaurant" => array("images/restaurant.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,8,8)),
			"campsite" => array("images/campsite.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,8,8,8)),
			"bridge" => array("images/bridge.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0)),
			"tunnel" => array("images/tunnel.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0)),
			"barn" => array("images/barn.png",
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0))
			);
		$places = array
			("hamlet" => array(2,
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8,12,12,12)),
			"village" => array(5,
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,8,10,14,14,14)),

			"small town" => array(10,
				array(-1,-1,-1,-1,-1,-1,8,8,8,10,14,18,18,18)),
			"large town" => array(20,
				array(-1,-1,-1,8,8,8,10,10,10,14,18,24,24,24)),
			"town" => array(20,
				array(-1,-1,-1,8,8,8,10,10,10,14,18,24,24,24)),
			"suburb" => array(0,
				array(-1,-1,-1,-1,-1,-1,-1,-1,-1,8,10,14,14,14))
			);

		// 070406 changed 'type' to ['tags']['class'] for nodes
		foreach ($this->mapdata->nodes as $node)
		{
			if($node['tags']['class']!=null)
			{
				$p = $this->map->get_point($node);

				//if ( $this->map->pt_within_map ($p))
				if(1)
				{
					if(array_key_exists($node['tags']['class'],$images))
					{
						$fs = $images[$node['tags']['class']][1][$usedscale-1];
						$imgfile=$images[$node['tags']['class']][0];
						$imgsize = getimagesize($imgfile);		
						$w = $imgsize[0];
						$h = $imgsize[1];
						$this->draw_image($p['x'],$p['y'], $w,$h,$imgfile,
										$node['tags']['name'], $fs);
						if($fs>0)
						{
							$namedata['name'] = $node['tags']['name'];
							$namedata['fs'] = $fs;
							$namedata['x']= $p['x']+$w/2;
							$namedata['y']= $p['y']+$h/2;
							$allnamedata[] = $namedata;
						}
					}
					elseif(array_key_exists($node['tags']['class'],$places))
					{
						$size = $places[$node['tags']['class']][0];
						$fs = $places[$node['tags']['class']][1][$usedscale-1];

						/*
						$this->draw_place_disc($p['x'],$p['y'],$size,
									$node['tags']['name'],$fs);
									*/
						if($fs>0)
						{
							$namedata['name'] = $node['tags']['name'];
							$namedata['fs'] = $fs;
							$namedata['x']=$p['x']+$size/2;
							$namedata['y']=$p['y']+$size/2;
							$allnamedata[] = $namedata;
						}
					}
				}
			}
		}
		$this->draw_names($allnamedata);
	}

	function draw_image($x,$y,$w,$h,$imgfile,$name, $fs)
	{
		if($fs>=0)
		{
			$icon = ImageCreateFromPNG($imgfile);

			ImageCopy($this->im,$icon,$x-$w/2,$y-$h/2,0,0,$w,$h);
			ImageDestroy($icon);
		}
    }

	function draw_place_disc($x,$y,$size,$name,$fs)
	{
		if($fs>=0)
		{
			ImageArc($this->im,$x,$y,
						 	$size*$this->map->scale*100,
						   	$size*$this->map->scale*100,
						   	0,360,$this->ltyellow); 

			ImageFillToBorder ($this->im,$x,$y,
							$this->ltyellow,$this->ltyellow);	
		}
    }

	function draw_names(&$namedata)
	{
		foreach($namedata as $name)
		{
			$this->draw_name($name['x'],$name['y'],
								$name['name'],$name['fs'],$this->darkred);
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

			$segtype=$seg["type"];
			$segwidth=$this->routetypes[$segtype]['width']+2;

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

function allocatetype(&$metaData)
{
	$types["yes,no,no,no,path"] =  2;

	$types["unofficial,no,no,no,path"] =  1;
	$types["yes,yes,yes,no,path"] = 42; 
	$types["unofficial,unofficial,unofficial,no,path"] = 21; 
	$types["yes,yes,yes,yes,unsurfaced"] = 234; 
	$types["*,*,*,*,residential"] = 362; 
	$types["*,*,*,*,minor"] = 490; 
	$types["*,*,*,*,street"] = 618; 
	$types["*,*,*,*,secondary"] = 746; 
	$types["*,*,*,*,primary"] = 874; 
	$types["*,*,*,*,motorway"] = 960; 

	$k = array("foot","horse","bike","car","class");

	
	/*
	for($count=0; $count<5; $count++)
		if(!isset($metaData[$k[$count]]))
			$metaData[$k[$count]]=($k[$count]=="class" ? "" :"no");
			*/

	foreach($types as $t=>$v)
	{
		$match = true;

			
		$ta = explode(",",$t);
		for($count=0; $count<5; $count++)
		{
			if(!($ta[$count]=="*" || 
		 	(!isset($metaData[$k[$count]])&&$ta[$count]=="no") || 	
			$metaData[$k[$count]]==$ta[$count]  ))
			{
				$match=false;
			}
		}

		if($match)
		{
			return $v;
		}
	}

	// default type - residential road
	return 362; 
}

			
?>
