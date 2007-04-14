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
require_once('rules.php');
require_once('Map.php');
require_once('functions.php');
require_once('Painter.php');


// 070406 changed ['name'] to ['tags']['name'] for nodes, segments and ways
// 070406 changed ['type'] to ['tags']['class'] for nodes

///////////////////// CLASS DEFINITIONS /////////////////////////


class Image
{
	var $im, 
		$map, 
		$backcol; 

	var $debug;

	var $mapdata;
	var $datasrc, $location;
	var $npe, $osm;

	function Image ($w, $s, $e, $n, $width, $height, $stylexml, $layers,
					$dsrc="db", $loc="http://www.openstreetmap.org/api/0.3/map",					 $dbg=0)
	{
		$this->map = new Map ($w,$s,$e,$n, $width, $height);
		$this->painter = new GDPainter();

		$this->datasrc = $dsrc;
		$this->location = $loc;
		$this->npe = in_array('npe',$layers);
		$this->osm = in_array('osm',$layers);

		# 14/11/04 changed ImageCreate() to ImageCreateTrueColor();
		# in GD2, when copying images (as we do for the icons), both source
		# and destination image must be either paletted or true colour.
		# (The icons are s)
		$this->backcol = 
			$this->painter->createImage($this->map->width*
				(2*$this->extensionFactor()+1),
									$this->map->height*
				(2*$this->extensionFactor()+1), 220, 220, 220);

		$bottomleft_ll = gr_to_wgs84_ll(array("e"=>$w, "n"=>$s));
		$topright_ll = gr_to_wgs84_ll(array("e"=>$e, "n"=>$n));

		if($this->osm)
		{
			$this->mapdata=grabOSM($bottomleft_ll['long'],
							   $bottomleft_ll['lat'],
							   $topright_ll['long'],
							   $topright_ll['lat'],
							   $this->datasrc, null, $this->location);
		}

		$this->styleRules = readStyleRules($stylexml);

		$this->is_valid = true;
		$this->debug = $dbg;
	}

	function draw()
	{
	
		if($this->npe)
		{
			$this->grabNpeMap($this->map->bottomleft['e'],
							$this->map->bottomleft['n']);
		}

		if($this->osm)
			$this->draw_ways();

		$this->painter->crop(
				$this->map->width*$this->extensionFactor(),
				$this->map->height*$this->extensionFactor(),
				$this->map->width,
				$this->map->height
							);

		$this->painter->renderImage();
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
			if($curStyle && $curStyle["render"]!="area")
			{
				$width = $curStyle["width"];

				foreach($this->mapdata["ways"][$id]["segs"] as $segid)
				{
					if(isset($this->mapdata["segments"][$segid]))
					{
						// specify the segment's parent way
						$this->mapdata["segments"][$segid]["way"] = $id;
						$seg = $this->mapdata["segments"][$segid];

						// TODO: LL_TO_GR
						$from_gr = wgs84_ll_to_gr
							($this->mapdata["nodes"][$seg['from']]);
						$to_gr = wgs84_ll_to_gr
							($this->mapdata["nodes"][$seg['to']]);
						$p[0] = $this->map->get_point ($from_gr);
						$p[1] = $this->map->get_point ($to_gr);
						

						if ( (isset($this->mapdata["nodes"] [$seg['to']]) 
							&& isset($this->mapdata["nodes"] [$seg['from']]) ) 
							&& $width>0	
							)

						{
							$rgb=explode(",", $curStyle["colour"]);

							if(count($rgb)==3)
							{
								$colour = $this->painter->getColour
								($rgb[0],$rgb[1],$rgb[2]);

								// 07/06/05 Changed this to reflect the new way
								// that dashed lines are stored in the database.
						   		if(isset ($curStyle["dash"]))
								{
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
		return 0.5; 
	}	

	function grabNpeMap($easting, $northing)
	{
		if($this->map->npeSuitable())
		{
			$e1 = floor($easting/1000);
			$n1 = floor($northing/1000);
			$z = $this->map->get_top_right();
			$e2 = floor($z["e"]/1000);
			$n2 = floor($z["n"]/1000);
			$efactor = 125 * ($this->map->escale/0.125);
			$nfactor = 125 * ($this->map->nscale/0.125);

			$dstx = -($this->map->escale*($easting%1000));
			
			for($ecount=$e1; $ecount<=$e2; $ecount++)
			{
				/*
				if($ecount==$e1)
				{
					$srcx = (0.125*($easting%1000));
					$srcw = 125-$srcx;
					$dstw = 125-$srcx;
				}
				elseif($ecount==$e2)
				{
					$srcx = 0;
					$srcw = 0.125*($z["e"]%1000); 
					$dstw = 0.125*($z["e"]%1000); 
				}
				else
				{
					$srcx = 0;
					$srcw = 125;
					$dstw = 125;
				}
				*/


					$dsty = -($nfactor - $this->map->nscale*($z["n"]%1000));
				for($ncount=$n2; $ncount>=$n1; $ncount--)
				{
					/*
					if($ncount==$n1)
					{
						$srcy = 0;
						$srch = 125- (0.125*($northing%1000)); 
						$dsth = 125-(0.125*($northing%1000)); 
					}
					elseif($ncount==$n2)
					{
						$srcy = 125-(0.125*($z["n"]%1000));
						$srch = 125-$srcy;
						$dsth = 125-$srcy;
					}
					else
					{
						$srcy = 0;
						$srch = 125;
						$dsth = 125;
					}
					*/
					$srcw=125;
					$srch=125;
					if($srcw>0 && $srch>0)
					{
					//$url="http://ustile.npemap.org.uk/scaled1/".
					$url="/home/dsheldon/npetiles/scaled1/".
							"$ecount/$ncount.jpg";

					$img = ImageCreateFromJPEG ($url);

					//echo "$ecount $ncount $dstx $dsty<br/>";
					/*
					ImageCopyResized($this->painter->im,$img,
					$this->cnvX($dstx),$this->cnvY($dsty),$srcx,$srcy,
						$dstw,$dsth, $srcw,$srch);
						*/
					ImageCopyResized($this->painter->im,$img,
					$this->cnvX($dstx),$this->cnvY($dsty),0,0,
						$efactor,$nfactor,125,125);
					}

					//$dsty += ($ncount==$n1) ? $dsth : 125; 
					$dsty +=  $nfactor;
				}

				//$dstx += ($ecount==$e1) ? $dstw : 125; 
				$dstx+=$efactor;
			}
		}
	}
}

function zIndexCmp($a,$b)
{
	return ($a["style"]["z-index"] > $b["style"]["z-index"]) ? 1:-1;
}

////////////////// SCRIPT BEGINS HERE /////////////////////

$defaults = array("width" => 400, 
			"height" => 400,
			"debug" => 0,
			"datasrc" => "db", 
			"location" => "http://www.openstreetmap.org/api/0.3/map",
			"layers" => "npe,osm",
			"bbox" => "487000,126000,491000,130000" );

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
$stylexml = "freemap.xml";


$image = new Image($bbox[0], $bbox[1], $bbox[2], $bbox[3],
						$inp["width"],$inp["height"],$stylexml,
						explode(",",$inp['layers']),
						$inp["datasrc"], $inp["location"],$inp["debug"]);

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
	if($field=="width" || $field=="height")
	{
		return wholly_numeric($value);
	}
	return $value;
}
?>
