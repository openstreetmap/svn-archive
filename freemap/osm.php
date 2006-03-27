<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('defines.php');

// 15/03/06 now uses 0.3 API

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
#end globals

function grabOSM($w, $s, $e, $n)
{
	// Pull out half of tiles above and to left of current tile to avoid
	// cutting off labels
	$w1 = $w - ($e-$w)/2;
	$n1 = $n + ($n-$s)/2;

	$url = "http://www.openstreetmap.org/api/0.3/map?bbox=$w1,$s,$e,$n1";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
	$resp=curl_exec($ch);
//	echo $resp;
	curl_close($ch);

	$resp2 = parseOSM(explode("\n",$resp));
	return $resp2;
}

function parseOSM($osm)
{
	global $segments, $nodes, $ways;
//	echo "parseOSM: osm = $osm<br/>";

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element","on_end_element");
	xml_set_character_data_handler($parser,"on_characters");

	foreach($osm as $line)	
	{
		if (!xml_parse($parser,$line))
			return false;	
	}

	xml_parser_free($parser);
	return array("segments"=>$segments, "nodes"=>$nodes, "ways"=>$ways);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element($parser,$element,$attrs)
{
	global $inDoc, $inNode, $inSegment, $segments, $nodes, $curID, 
			$curNode, $curSeg, $inWay, $curWay, $waySegs;

	if($element=="OSM" || $element=="osm")
	{
		$inDoc = true;
	}
	elseif($inDoc)
	{
		if($element=="NODE")
		{
			$inNode=true;
			$curNode = array();
			$curNode["lat"] = $attrs["LAT"];
			$curNode["long"] = $attrs["LON"];
			$curID = $attrs["ID"];
		}
		elseif($element=="SEGMENT")
		{
			$inSegment=true;
			$curSeg = array();
			$curSeg['from']['lat'] = $nodes[$attrs["FROM"]]["lat"];
			$curSeg['to']['lat'] = $nodes[$attrs["TO"]]["lat"];
			$curSeg['from']['long'] = $nodes[$attrs["FROM"]]["long"];
			$curSeg['to']['long'] = $nodes[$attrs["TO"]]["long"];
			$curSeg['tags'] = array();
			$curSeg['tags']['foot'] = 'no';
			$curSeg['tags']['horse'] = 'no';
			$curSeg['tags']['bike'] = 'no';
			$curSeg['tags']['car'] = 'no';
			$curSeg['tags']['class'] = '';
			$curID = $attrs["ID"];
		}
		elseif($element=="WAY")
		{
			$inWay = true;
			$waySegs = array();
			$curWay = array();
			$curWay['tags'] = array();
			$curWay['tags']['foot'] = 'no';
			$curWay['tags']['horse'] = 'no';
			$curWay['tags']['bike'] = 'no';
			$curWay['tags']['car'] = 'no';
			$curWay['tags']['class'] = '';
			$curID = $attrs["ID"];
		}
		elseif($element=="SEG" && $inWay)
		{
			$waySegs[] = $attrs["ID"];
		}
			
		// 0.3
		elseif($element=="TAG")
		{
			if($inNode)
			{
				if($attrs["K"]=='name')
				{
					$curNode['name'] = $attrs['V'];
				}
				elseif($attrs["K"]=='class')
				{
					$curNode['type'] = $attrs['V'];
				}
			}
			elseif($inSegment)
			{
				if($attrs["K"]=='name')
				{
					$curSeg['name'] = $attrs["V"];
				}
				else
				{
					$curSeg['tags'][$attrs["K"]] = $attrs["V"];
				}
			}
			elseif ($inWay)
			{
				if($attrs["K"]=='name')
				{
					$curWay['name'] = $attrs["V"];
				}
				else
				{
					$curWay['tags'][$attrs["K"]] = $attrs["V"];
				}
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
		$nodes[$curID] = $curNode; // 0.3 UID->ID
	}
	elseif($element=='SEGMENT')
	{
		$inSegment = false;
		$segments[$curID] = $curSeg;
	}
	elseif($element=='WAY')
	{
		$inWay = false;
		$ways[$curID] = $curWay;

		# segments belonging to a way take on the way's attributes
		foreach($waySegs as $segID)
			$segments[$segID]['tags'] = $curWay['tags'];
	}
}

function on_characters ($parser, $characters)
{
}

function get_meta_data(&$metaData,$tags)
{

	$tags_arr = explode(";", $tags);
	foreach($tags_arr as $tag)
	{
		$keyval = explode("=", $tag);
		$metaData[$keyval[0]] = $keyval[1];
	}
}

?>
