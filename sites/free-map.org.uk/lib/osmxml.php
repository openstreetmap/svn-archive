<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('/home/www-data/private/defines.php');
//require_once('database.php');
//define('OSM_LOGIN','NOT_NEEDED');

// 15/03/06 now uses 0.3 API
// 03/04/06 will now read local XML; takes bounding box to reject out-of-range
// data

#globals
$inNode = $inWay = $inDoc =  false;
$nodes = array();
$ways = array();
$curID = 0;
$curNode = null;
$curWay = null;
$nds = null;
$doNodes=true;
$doWays=true;
#end globals

function parseOSM($osm,$dn=true,$dw=true)
{
	global $nodes, $ways, $doNodes, $doWays;

	$doNodes = $dn;
	$doWays = $dw;

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
	else
	{
		if (!xml_parse($parser,$osm))
			return false;	
	}
		
	xml_parser_free($parser);

	return array("nodes"=>$nodes, "ways"=>$ways);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element($parser,$element,$attrs)
{
	global $inDoc, $inNode, $nodes, $curID, 
			$curNode, $inWay, $curWay, $nds, $doNodes, $doWays;

	if($element=="OSM" || $element=="osm")
	{
		$inDoc = true;
	}
	elseif($inDoc)
	{
		if($element=="NODE" && $doNodes)
		{
			$inNode=true;
			$curNode=null;
			$curNode = array();
			$curNode["lat"] = $attrs["LAT"];
			$curNode["lon"] = $attrs["LON"];
			$curNode['tags']=array();
			$curID = $attrs["ID"];
		}
		elseif($element=="WAY" && $doWays)
		{
			$inWay = true;
			$nds = array();
			$curWay = array();
			$curWay['tags'] = array();
			$curID = $attrs["ID"];
		}
		elseif($element=="ND" && $inWay)
		{
			$nds[] = $attrs["REF"];
		}
		elseif($element=="TAG")
		{
			// 070406 now all tags are put in the 'tags' array
			if($inNode && $curNode) 
			{
				$curNode['tags'][$attrs['K']]=$attrs['V'];
			}
			elseif($inWay && $curWay)
			{
				$curWay['tags'][$attrs["K"]] = $attrs["V"];
			}
		}
	}
}

function on_end_element($parser, $element)
{
	global $inNode, $nodes, $curNode, $curID, $inWay, $curWay, $ways, $nds,
				$doWays, $doNodes;

	if($element=='NODE' && $doNodes)
	{
		$inNode = false;
		if($curNode) 
		{
			$nodes[$curID] = $curNode; // 0.3 UID->ID
		}
	}
	elseif($element=='WAY' && $inWay && $doWays)
	{
		$inWay = false;

		if($curWay)
		{
			$curWay['nds'] = $nds;
			$ways[$curID] = $curWay;
		}
	}
}


function on_characters ($parser, $characters)
{
}


?>
