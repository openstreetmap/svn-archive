<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################

# load_hgt2() 
# Loads heights from one or more .hgt files.
# Each rectangle in the input array "rects" defines the rectangle (in sampling
# point indices) of a grid square. There's normally only one rectangle but
# may be up to 4 if two latitude/longitude lines pass through the visible area.
#
# sampling_pts is a definition of the area in terms of nationally-indexed 
# sampling points; this function fills it in.
#
# Returns an array of all the heights indexed nationally.

function load_hgt2($rects,&$sampling_pts,$dbg,$f)
{
	$sampled_hgts = array();

	# Do each input rectangle
	foreach($rects as $rect)
	{
		$gb[]=read_hgts2($rect,$sampled_hgts,$dbg,$f);
	}

	# Set the bounds of the aggregated area
	$sampling_pts['topleft'] = $gb[0]['topleft'];
	$sampling_pts['width'] =  
		($gb[count($rects)-1]['bottomright']%10801-$gb[0]['topleft']%10801)-0;
	$sampling_pts['height'] =  (int)
		($gb[count($rects)-1]['bottomright']/10801-$gb[0]['topleft']/10801)-0;
	$sampling_pts['resolution'] = $f;
	return $sampled_hgts;
}


function get_hgt_file2($ll)
{
	$hgtfile = sprintf ( "data/N%02d".($ll['long']<0 ? "W":"E").
						  "%03d.hgt",
						$ll['lat'],
						abs(floor($ll['long'])));
	return $hgtfile;
}

function read_hgts2($rect,&$hgt,$dbg,$f)
{
	# Get the .hgt file for the current rectangle
	$hgtfile=get_hgt_file2($rect);
	$fp=fopen($hgtfile,"r");	

	# Get the national index for the top left of this rectangle
	$gb=gbidx($rect,$rect['top'],$rect['left']);

	//$topleft=$gb;
	$topleft=false;
//	$ff = $f*10801;

	for($row=$rect['top']*1201; $row<=$rect['bottom']*1201; $row+=1201)
	{
		if($dbg) echo "ROW NUMBER (GB) ". floor($gb/10801). "<br/>";
		// 20/02/05 Only do every '$f' rows
		if(($gb/10801)%$f == 0)
		{
			if($dbg) echo "COUNTING<br/>";
			$width = ($rect['right']-$rect['left'])+1;#width in number of points
			fseek($fp,($row+$rect['left'])*2);
			$data = fread ($fp,$width*2);
			$datacount=0;
			for($pt=$row+$rect['left'];$pt<=$row+$rect['right']; $pt++)
			{
				// 20/02/05 Only do every '$f' columns
				if(($gb+$datacount/2)%$f == 0) 
				{
					// 20/02/05 topleft is the lowest included point
					if($topleft===false) $topleft = $gb+$datacount/2;
					$h=
		 			(ord($data[$datacount])*256+ord($data[$datacount+1])) 
					* 3.28084;
					$hgt[$gb+$datacount/2] = ($h>=1 && $h<4500) ? $h: 1;
					if($dbg) echo "HEIGHT pt ". ($gb+$datacount/2). " = ".
					$hgt[$gb+$datacount/2]."<br/>";
					// 20/02/05 bottomright is the highest included point
					$bottomright = $gb+$datacount/2;
				}
				$datacount+=2;
			}
			$data=null;
		}
		$gb += 10801;#next row: 10801=1200*9+1
	}
	# Get the national index for the bottom right of this rectangle
	# 20/02/05now done a different way - see above $bottomright=gbidx($rect, $rect['bottom'], $rect['right']);
	fclose($fp);

	# Return an array of the nationally-indexed corner points
	return array("topleft"=>$topleft,"bottomright"=>$bottomright);
}
	
# Return the "GB index" of a given SRTM sampling point. The "GB index" is
# its index within the super-rectangle 7W->2E, 59N->49N. The top left point
# is indexed 0, the top right 10800, the leftmost on the second row 10801 etc.
function gbidx($rect,$row,$col)
{
	$rowout = 12000-((($rect['lat']-49)*1200)+(1200-$row));
	$colout = ($rect['long']+7)*1200 + $col;
	return $rowout*10801 + $colout;
}

# get_bounding_rects()
# Given the latitide and longitude of the bottom left and top right of the
# visible area, this function returns an array of rectangles definining the
# SRTM point indices for all latitude/longitude squares in the visible area.
# This will normally be just one, but if, e.g., 51N and 1W both crossed the
# visible area, it could be up to 4.
function get_bounding_rects($ll, $dbg)
{
	# Get the latitude/longitude square of each rectangle
	$rects=getrects($ll);

	# Fill in the actual bounds
	for($count=0; $count<count($rects); $count++)
	//foreach($rects as $rect)
	{
		$rect=$rects[$count];
		if($dbg)echo "Doing rect.... long $rect[long] lat $rect[lat] ";
		$rect['left'] = $ll['bottomleft']['long'] > $rect['long']  ?
					floor(($ll['bottomleft']['long'] - $rect['long'])*1200): 0;
		$rect['right'] = ($ll['topright']['long'] < $rect['long']+1) ?
					1+floor(($ll['topright']['long'] - $rect['long'])*1200) :  
							1200;
	
		$rect['top'] =($ll['topright']['lat'] < $rect['lat']+1 ) ?
					floor((($rect['lat']+1)-$ll['topright']['lat'])*1200) :  0;
		$rect['bottom'] = $ll['bottomleft']['lat'] > $rect['lat']  ?
					1+floor((($rect['lat']+1)-$ll['bottomleft']['lat'])*1200) :
				  		1200;
		if($dbg)
		{
			echo "Coords: left $rect[left] right $rect[right] top ".
			"$rect[top] bottom $rect[bottom]<br/>";
		}
		$rects[$count] = $rect;
	}

	if($dbg)echo "Coords: left ".$rects[0]['left']; 
	return $rects;
}

# Given the latitude and longitude of the bottom left and top right of the
# visible map area, this function returns the appropriate number of
# rectangles specific to a given grid square. For example, if both 51N and 1W
# passed through the visible area, four rectangles would be generated, one
# for the 51N/1W square, one for the 50N/1W square etc. Only the base latitude
# and longitude are filled in at this stage. The dimensions of each rectangle
# within the visible area are filled in by the calling function.
function getrects($ll)
{
	$count=0;
	for($long=floor($ll['bottomleft']['long']); 
		$long<=floor($ll['topright']['long']); $long++)
	{
		for($lat=floor($ll['topright']['lat']);
			$lat>=floor($ll['bottomleft']['lat']); $lat--)
		{
			$rects[$count]['lat'] = $lat;
			$rects[$count++]['long'] = $long;
		}
	}
	return $rects;
}

// checked : the correct edges are being considered each time.
function get_line($ht,$hts,$sp,&$edges, $twocontours,$dbg)
{
		$go = 0;
		$lines=array();
		$prevedges=array();

		for($edge=0; $edge<3; $edge++)
		{
			// The height is between the heights of the current edge...
			if(between($ht,$hts[$edges[$edge][0]],$hts[$edges[$edge][1]]))
			{
				for($edge2=$edge+1; $edge2<4; $edge2++)
				{
					// The height is between the heights of the second edge...
					if(between
						($ht,$hts[$edges[$edge2][0]],$hts[$edges[$edge2][1]]))
					{
					  
						if($dbg)
						{
							
						echo "<strong>found!</strong> ";
						
						echo "edge$edge: heights ".$hts[$edges[$edge][0]]." ".
						$hts[$edges[$edge][1]]. " ".
						"edge$edge2: heights ".$hts[$edges[$edge2][0]]." ".
						$hts[$edges[$edge2][1]];

						echo "corner: $cnr<br/>";
						/*
						"pts: edgeA: ".$sp[$edges[$edge][0]]['x']. ",".
							$sp[$edges[$edge][0]]['y']. " ".
						"&amp; ".$sp[$edges[$edge][1]]['x']. ",".
							$sp[$edges[$edge][1]]['y']. " ".
						"edgeB: ".$sp[$edges[$edge2][0]]['x']. ",".
							$sp[$edges[$edge2][0]]['y']. " ".
						"&amp; ".$sp[$edges[$edge2][1]]['x']. ",".
							$sp[$edges[$edge2][1]]['y']. "<br/>";
							*/
						}	
						$eAh0 = $hts[$edges[$edge][0]];
						$eAh1 = $hts[$edges[$edge][1]];
						$eBh0 = $hts[$edges[$edge2][0]];
						$eBh1 = $hts[$edges[$edge2][1]];

						$eAp0x = $sp[$edges[$edge][0]]['x'];
						$eAp0y = $sp[$edges[$edge][0]]['y'];
						$eAp1x = $sp[$edges[$edge][1]]['x'];
						$eAp1y = $sp[$edges[$edge][1]]['y'];

						$eBp0x = $sp[$edges[$edge2][0]]['x'];
						$eBp0y = $sp[$edges[$edge2][0]]['y'];
						$eBp1x = $sp[$edges[$edge2][1]]['x'];
						$eBp1y = $sp[$edges[$edge2][1]]['y'];

						// We draw a line. 
						$line[0]['x'] =
					   		$eAp0x + ( (($ht-$eAh0) / ($eAh1-$eAh0))	
					 		 * ($eAp1x-$eAp0x) );

						$line[0]['y'] =
					   		$eAp0y + ( (($ht-$eAh0) / ($eAh1-$eAh0))	
					 		 * ($eAp1y-$eAp0y) );
					/*	
						if($dbg)echo "Point on edgeA ".$line[0]['x']. " ".
				   				$line[0]['y']." ";		
					*/
						$line[1]['x'] =
					   		$eBp0x + ( (($ht-$eBh0) / ($eBh1-$eBh0))	
					 		 * ($eBp1x-$eBp0x) );

						$line[1]['y'] =
					   		$eBp0y + ( (($ht-$eBh0) / ($eBh1-$eBh0))	
					 		 * ($eBp1y-$eBp0y) );
					/*	 
						if($dbg)echo "Point on edgeB ".$line[1]['x']. " ".
				   				$line[1]['y']."<br/>";		
					*/
						if($twocontours==false) 
						{
							$lines[0] = $line;
							return $lines;
						}
						else
					   	{
							add_contour
								($lines,$line,$edge,$edge2,$prevedges,$go);
						}
					}
				}
			}
		}

	return $lines;
}

# This function will be called when the special case of two opposite corners 
# having a height above a contour, and the other two below, occurs. In this
# case - and this case only - two contours of a given height will be drawn
# through a quadrangle. This special case confuses the standard algorithm
# no end :-) Thus, we need to ensure that the two contours are drawn
# on the opposite side of the quadrangle (any other combination wouldn't 
# make sense), and this function does that. Which two opposite sides 
# doesn't actually matter; in fact, it's impossible to tell which two would
# be correct.
function add_contour(&$lines,&$line,$edge1,$edge2,&$prevedges,&$go)
{
	/* Edge numbering assumed to be:

	     0 
	   +---+
	  1|   |2
	   +---+
	     3

	Don't draw contours through opposite edges. These don't apply in this
	special case. 

	*/	

	if(!(($edge1==0 && $edge2==3) || ($edge1==1 && $edge2==2)))
	{
		if($go==0)
		{
			$prevedges[0] = $edge1;
			$prevedges[1] = $edge2;
			$go = 1;
			$lines[0] = $line;
		}
		elseif( ($edge1==2 && $edge2==3 && $prevedges[0]==0 && $prevedges[1]==1)
			 || ($edge1==1 && $edge2==3 && $prevedges[0]==0 && $prevedges[1]==2)
			  && $go==1)
		{
			$lines[1] = $line;
		}
	}
}	

function between($a,$b,$c)
{
	return ($a<=max($b,$c) && $a>=min($b,$c));
}

// Does height match 2 provided edge heights - 
// if so it's a corner intersection, ignore
function corner($ht,$e00,$e01,$e10,$e11)
{
$a =   	($ht==$e00 && $ht==$e10) ||	
	 			($ht==$e00 && $ht==$e11) ||	
				($ht==$e01 && $ht==$e10) ||	
				($ht==$e01 && $ht==$e11)  ? 1:0;
	
	return $a;
}


# finds the minimum distance between a point and the list of previous points
function hgtpt_distance($pt1,$prevs)
{

	$dist=sqrt(10800*10800 + 12000*12000);
	foreach($prevs as $pt2)
	{
		$dx = $pt2%10801 - $pt1%10801;
		$dy = $pt2/10801 - $pt1/10801;
		$result= sqrt($dx*$dx + $dy*$dy);
		$dist = ($result<$dist) ? $result:$dist;
	}
	return $dist;

}

?>
