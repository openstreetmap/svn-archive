<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('defines.php');
require_once('database.php');
//define('OSM_LOGIN','NOT_NEEDED');

// 15/03/06 now uses 0.3 API
// 03/04/06 will now read local XML; takes bounding box to reject out-of-range
// data

#globals
$inNode = $inSegment = $inWay = $inDoc =  false;
$segments = array();
$nodes = array();
$ways = array();
$curID = 0;
$curNode = null;
$curSeg = null;
$curWay = null;
$waySegs = null;
$w = null;
$s = null;
$e = null;
$n = null;
#end globals

/*
$stuff = grabOSM(-0.75,51.02,-0.7,51.07,true);
print_r($stuff);
*/

// $datamode can be:
// 0 = grab data from OSM using the API 
// 1 = grab data from a local XML file
// 2 = grab data from the database
// $location is either an API URL or a local XML file

function grabOSM($w0, $s0, $e0, $n0, $datamode=0, 
				$location="http://www.openstreetmap.org/api/0.3/map")
{
	// Pull out half of tiles above and to left of current tile to avoid
	// cutting off labels
	$w1 = $w0 - ($e0-$w0)/2;
	$n1 = $n0 + ($n0-$s0)/2;

	switch($datamode)
	{
		case 1:
			$resp2 = parseOSM($location,$w0,$s0,$e0,$n0);
			break;

		case 0:
			$url = "$location?bbox=$w1,$s0,$e0,$n1";
			$ch=curl_init ($url);
			curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
			curl_setopt($ch,CURLOPT_HEADER,false);
			curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
			$resp=curl_exec($ch);
		//	echo $resp;
			curl_close($ch);
			$resp2 = parseOSM(explode("\n",$resp));
			break;

		case 2:
			$resp2 = grab_direct_from_database($w0,$s0,$e0,$n0);
			break;
	
	}
	return $resp2;
}

function parseOSM($osm,$west=null,$south=null,$east=null,$north=null)
{
	global $segments, $nodes, $ways, $w, $s, $e, $n; 
//	echo "parseOSM: osm = $osm<br/>";

	// 030406 setup bounding box for XML
//	echo "parseOSM: $osm $west $south $east $north<br/>";

	$w=$west;
	$s=$south;
	$e=$east;
	$n=$north;

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element","on_end_element");
	xml_set_character_data_handler($parser,"on_characters");


	if(is_array($osm))
	{
		foreach($osm as $line)	
		{
			if (!xml_parse($parser,$line))
				return false;	
		}
	}
	elseif(is_string($osm))
	{
		$fp = fopen($osm,"r");
		$count=0;
		while($line=fread($fp,4096))
		{
			if(!xml_parse($parser,$line))
			{
				fclose($fp);
				return false;
			}
			$count++;
		}
		fclose($fp);
	}

	xml_parser_free($parser);

	return array("segments"=>$segments, "nodes"=>$nodes, "ways"=>$ways);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element($parser,$element,$attrs)
{
	global $inDoc, $inNode, $inSegment, $segments, $nodes, $curID, 
			$curNode, $curSeg, $inWay, $curWay, $waySegs,
			$w, $s, $e, $n;

	if($element=="OSM" || $element=="osm")
	{
		$inDoc = true;
	}
	elseif($inDoc)
	{
		if($element=="NODE")
		{
			$inNode=true;
			$curNode=null;
			if(include_data($attrs["LAT"],$attrs["LON"],$w,$s,$e,$n))
			{
				$curNode = array();
				$curNode["lat"] = $attrs["LAT"];
				$curNode["long"] = $attrs["LON"];
				$curID = $attrs["ID"];
//				echo "Including node $curID\n";
			}
		}
		elseif($element=="SEGMENT")
		{
			$inSegment=true;
			$curSeg=null;
			if(isset($nodes[$attrs["FROM"]]) && isset($nodes[$attrs["TO"]]) &&
				(	 include_data($nodes[$attrs["FROM"]]["lat"],
					 			  $nodes[$attrs["FROM"]]["long"],
								  $w,$s,$e,$n) || 
					 include_data($nodes[$attrs["TO"]]["lat"],
					 			  $nodes[$attrs["TO"]]["long"],
								  $w,$s,$e,$n) )
			  )
			{
				//echo "INCLUDING SEGMENT<br/>";
				$curSeg = array();

				/*
				echo $nodes[$attrs["FROM"]]["lat"]." ";
				echo $nodes[$attrs["FROM"]]["long"]." ";
				echo $nodes[$attrs["TO"]]["long"]." ";
				echo $nodes[$attrs["TO"]]["long"]."<br/>";
				*/


				$curSeg['from'] = $attrs["FROM"];
				$curSeg['to'] = $attrs["TO"];
				$curSeg['tags'] = array();
				/*
				$curSeg['tags']['foot'] = 'no';
				$curSeg['tags']['horse'] = 'no';
				$curSeg['tags']['bike'] = 'no';
				$curSeg['tags']['car'] = 'no';
				$curSeg['tags']['class'] = '';
				*/
				$curID = $attrs["ID"];
//				echo "Including segment $curID\n";
			}
			else if(isset($nodes[$attrs["FROM"]]) 
			&& isset($nodes[$attrs["TO"]]))
			{
				/*
				echo "NOT INCLUDING SEGMENT<br/>";

				echo $nodes[$attrs["FROM"]]["lat"]." ";
				echo $nodes[$attrs["FROM"]]["long"]." ";
				echo $nodes[$attrs["TO"]]["long"]." ";
				echo $nodes[$attrs["TO"]]["long"]."<br/>";
				*/
			}
		}
		elseif($element=="WAY")
		{
			$inWay = true;
			$waySegs = array();
			$curWay = array();
			$curWay['tags'] = array();
			/*
			$curWay['tags']['foot'] = 'no';
			$curWay['tags']['horse'] = 'no';
			$curWay['tags']['bike'] = 'no';
			$curWay['tags']['car'] = 'no';
			$curWay['tags']['class'] = '';
			*/
			$curID = $attrs["ID"];
		}
		elseif($element=="SEG" && $inWay)
		{
			$waySegs[] = $attrs["ID"];
		}
			
		// 0.3
		elseif($element=="TAG")
		{
//			echo "IN TAG<br/>";
//			print_r($attrs);
// 			echo "<p></p>";
			// 070406 now all tags are put in the 'tags' array
			if($inNode && $curNode) 
			{
				//echo "Adding node<br/>";
				$curNode['tags'][$attrs['K']]=$attrs['V'];
			}
			elseif ($inSegment && $curSeg)
			{
				//echo "Adding segment<br/>";
				$curSeg['tags'][$attrs["K"]] = $attrs["V"];
			}
			elseif($inWay && $curWay)
			{
				//echo "Adding way<br/>";
				$curWay['tags'][$attrs["K"]] = $attrs["V"];
			}
		}
	}
}

function on_end_element($parser, $element)
{
	global $inNode, $nodes, $inSegment, $segments, $curSeg, $curNode, $curID,
			$inWay, $curWay, $ways, $waySegs;

	if($element=='NODE')
	{
		$inNode = false;
		if($curNode)
		{
			$nodes[$curID] = $curNode; // 0.3 UID->ID
		}
	}
	elseif($element=='SEGMENT')
	{
		$inSegment = false;
		if($curSeg)
		{
			$segments[$curID] = $curSeg;
		}
	}
	elseif($element=='WAY')
	{
		$inWay = false;

		//290306 keep a record of the IDs of the segment within a way
		$curWay['segs'] = $waySegs;
		# segments belonging to a way take on the way's attributes
		# 070406 no longer done to maximise flexibility of this function
		# In Freemap, now moved to classes.php, after we have got the data
		/*
		foreach($waySegs as $segID)
			$segments[$segID]['tags'] = $curWay['tags'];
			*/
		$ways[$curID] = $curWay;
	}
}


function on_characters ($parser, $characters)
{
}

function include_data($lat, $lon, $w, $s, $e, $n)
{
//	echo "LAT: $lat LON: $lon W:$w S:$s E:$e N:$n<br/>";
	// Only test to include if we have a bounding box
	if($w && $s && $e && $n)
	{
		return ($lat>=$s && $lat<=$n && $lon>=$w && $lon<=$e);
		//return true;
	}
	return true;
}

?>
