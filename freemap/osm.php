<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

// WMS compliant OSM renderer
// does not include SRTM contours, these have been moved to srtm.php
// contains code from drawmap.php and classes.php - i.e. the Image class and
// the HTTP input reading code have been merged in the one file.

require_once('osmxml.php');
require_once('latlong.php');
require_once('defines.php');
require_once('gpxnew.php');
require_once('dataset.php');
require_once('rules.php');
require_once('Map.php');
require_once('functions.php');


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
	var $tp;

	function Image ($w, $s, $e, $n, $width, $height,
					$tp=0, $dbg=0)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);
		//$this->zoom=$zoom;

		// 150506 recalculate zoom from input pixel and longitude width
		// see Steve's email of 01/02/06
		$this->zoom = round(log((360*$width)/($e-$w),2)-9);

		# 14/11/04 changed ImageCreate() to ImageCreateTrueColor();
		# in GD2, when copying images (as we do for the icons), both source
		# and destination image must be either paletted or true colour.
		# (The icons are s)
		$this->im = ImageCreateTrueColor($this->map->width*
				(2*$this->extensionFactor()+1),
									$this->map->height*
				(2*$this->extensionFactor()+1));
		
		$this->backcol = ImageColorAllocate($this->im,220,220,220);
		$this->gold = ImageColorAllocate($this->im,255,255,0);
		$this->black = ImageColorAllocate($this->im,0,0,0);
		$this->ltyellow = ImageColorAllocate($this->im,255,255,192);
		$this->ltgreen = ImageColorAllocate($this->im,192,255,192);
		$this->contour_colour = ImageColorAllocate($this->im,192,192,0);
		$this->mint = ImageColorAllocate($this->im,0,192,64);


		if($this->zoom>=10)
		{
		$this->mapdata = new Dataset();
		$this->mapdata->grab_direct_from_database($w, $s, $e, $n, $zoom);
		
		// Make all segments inherit tags from their parent way
		$this->mapdata->give_segs_waytags();
		//06/06/05 Replaced old brush loading with the following call
		//$this->segmenttypes=$this->load_segment_types();
		//130406 replaced again with style rules
		$this->styleRules = readStyleRules("freemap.xml");
		}
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
		
		if($this->zoom>=10)
		{
		$this->load_segment_styles();

		usort($this->mapdata->segments,"zIndexCmp");

						
		$this->draw_segment_outlines();
		$this->draw_segments();
		$this->draw_points_of_interest();


		if($this->zoom >= 113)
			$this->draw_way_names();

		}
		
		$im2 = ImageCreateTrueColor($this->map->width,
											$this->map->height);

		ImageFill($im2,100,100,$this->backcol);
		/*
		ImageTTFText($im2, 8, 0, $this->map->width/2, 
								$this->map->height/2, $this->black, 
							TRUETYPE_FONT, $this->zoom);
							*/
		ImageCopy($im2,$this->im,0,0,$this->map->width*$this->extensionFactor(),
					$this->map->height*$this->extensionFactor(),
					$this->map->width,
					$this->map->height);
		ImagePNG($im2);
		ImageDestroy($im2);
		ImageDestroy($this->im);
	}

	
	function draw_segments()
	{

		# Only attempt to draw the line if at least one of the points
		# is within the map

		$ids = array_keys($this->mapdata->segments);
		foreach ($ids as $id)
		{

			$width = $this->getZoomLevelValue
							($this->mapdata->segments[$id]["style"]["width"]);
			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['to']]);

			
			if ( (isset($this->mapdata->nodes
					[$this->mapdata->segments[$id]['to']]) 
						&&
				 isset($this->mapdata->nodes
				 	[$this->mapdata->segments[$id]['from']]) ) &&
				$width>0	
		/*
			($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 */
					 )

			{
				$rgb=explode(",",
						$this->mapdata->segments[$id]["style"]["colour"]);

				if(count($rgb)==3)
				{
					$colour = ImageColorAllocate
							($this->im, $rgb[0],$rgb[1],$rgb[2]);

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


						ImageLine($this->im,$this->cnvX($p[0]['x']),
									$this->cnvY($p[0]['y']),
							$this->cnvX($p[1]['x']),$this->cnvY($p[1]['y']),
							IMG_COLOR_STYLED);
					}
					else
					{
						// 090406 outlines now done in their own function
						ImageLine($this->im,$this->cnvX($p[0]['x']),
									$this->cnvY($p[0]['y']),
								$this->cnvX($p[1]['x']),$this->cnvY($p[1]['y']),
								$colour);

					}
				}

				/*
				ImageSetThickness($this->im,20);
				ImageLine($this->im,-52,48,48,-52,$this->black);
				*/

				// do something with segment names here...
				// now only draw way names
				if($this->zoom >= 13)	
				{
					$this->draw_segment_name ($this->mapdata->segments[$id], 
								$this->mapdata->segments[$id]['tags']['name'], 
								false);
				}
				
			}
		}	
	}

	function load_segment_styles()
	{
		# Only attempt to draw the line if at least one of the points
		# is within the map
		$ids = array_keys($this->mapdata->segments);
		foreach ($ids as $id)
		{
					
			$this->mapdata->segments[$id]["style"] = 
				getStyle($this->styleRules,
							$this->mapdata->segments[$id]["tags"]);
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
			$width = $this->getZoomLevelValue
							($this->mapdata->segments[$id]["style"]["width"]);
					
			$p[0] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['from']]);
			$p[1] = $this->map->get_point
				($this->mapdata->nodes[$this->mapdata->segments[$id]['to']]);


			if ($width>0 &&  
				(isset($this->mapdata->nodes[$this->mapdata->segments
								[$id]['to']]) &&
				 isset($this->mapdata->nodes[$this->mapdata->segments
				 						[$id]['from']]) ) 
		/*
			&& ($this->map->pt_within_map ($p[0]) || 
				     $this->map->pt_within_map ($p[1]) ) 
					 */
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
					}
					ImageSetThickness($this->im, $width+2);
					ImageLine($this->im,$this->cnvX($p[0]['x']),
								$this->cnvY($p[0]['y']),
								$this->cnvX($p[1]['x']),
								$this->cnvY($p[1]['y']),$colour);

				}
			}
		}	
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

		ImageCopy($this->im,$icon,$this->cnvX($x-$w/2),$this->cnvY($y-$h/2),0,0,$w,$h);
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
			ImageTTFText($this->im, $fontsize, 0, $this->cnvX($x), 
								$this->cnvY($y), $colour, 
							TRUETYPE_FONT,
							$name_arr[$count]);

			// Get the height of the next word, so we know how far down to
			// draw it.	
			$bbox = ImageTTFBBox($fontsize,0,TRUETYPE_FONT,$name_arr[$count+1]);
			$y += ($bbox[1]-$bbox[7])+FONT_MARGIN;
		}
			
		// Finally draw the last word
		@ImageTTFText($this->im, $fontsize, 0, $this->cnvX($x), 
								$this->cnvY($y), $colour, 
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
				ImageFilledRectangle($this->im,$this->cnvX($x1),$this->cnvY($y1),
										$this->cnvX($x2),$this->cnvY($y2), $this->black);
				ImageTTFText($this->im, 8, 0,  
							$this->cnvX($x1), $this->cnvY($y2), $this->gold,
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
		ImageTTFText($this->im, $fontsize, -$angle, $this->cnvX($p[$i]['x']), 
			$this->cnvY($p[$i]['y']),
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
}

function zIndexCmp($a,$b)
{
	return ($a["style"]["z-index"] > $b["style"]["z-index"]) ? 1:-1;
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("WIDTH" => 400, 
			"HEIGHT" => 320,
			"tp" => 0,
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
	$image = new Image($bbox[0], $bbox[1], $bbox[2], $bbox[3],
						$inp["WIDTH"],$inp["HEIGHT"], 
						$inp["tp"],$inp["debug"]);
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
