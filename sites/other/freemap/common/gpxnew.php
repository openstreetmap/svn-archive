<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-07 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
require_once('defines.php');
//header("Content-type: text/xml");

#globals
$inTrkpt =  false;
$inDoc =  false;
$inTrk =  false;
$inWpt = false;
$inWptName=false;
$trackpoints = array();
$curWpt = null;
$waypoints = array();
#end globals

function grabGPX($w, $s, $e, $n)
{
	$url = "http://www.openstreetmap.org/api/0.3/trackpoints?bbox=$w,$s,$e,$n";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
	$resp=curl_exec($ch);
//	echo $resp;
	curl_close($ch);

	$resp2 = parseGPX(explode("\n",$resp));
	return $resp2;
}

function parseGPX($gpx)
{
	global $trackpoints, $waypoints;

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element_gpx",
				"on_end_element_gpx");
	xml_set_character_data_handler($parser,"on_characters_gpx");

	foreach($gpx as $line)	
	{
		if (!xml_parse($parser,$line))
			return false;	
	}

	xml_parser_free($parser);
	return array ("trackpoints"=>$trackpoints, "waypoints"=>$waypoints);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element_gpx($parser,$element,$attrs)
{
	global $inDoc, $inTrk, $inTrkpt, $trackpoints, $inWpt, $curWpt, $inWptName;

	if($element=="GPX")
	{
		$inDoc = true;
	}
	elseif($inDoc)
	{
		if($element=="TRK")
		{
			$inTrk=true;
		}
		elseif($element=="TRKPT" && $inTrk)
		{
			$inTrkpt=true;
			foreach($attrs as $name => $value)
			{
				if($name=="LAT")
					$curPt["lat"] = $value; 
				elseif($name=="LON")
					$curPt["long"] = $value; 
			}
			$trackpoints[] = $curPt;
		}
		elseif($element=="WPT")
		{
			$inWpt = true;
			$curWpt =array();
			foreach($attrs as $name => $value)
			{
				if($name=="LAT")
					$curWpt["lat"] = $value; 
				elseif($name=="LON")
					$curWpt["lon"] = $value; 
			}
		}
		elseif($element=="NAME" && $inWpt)
		{
			$inWptName=true;
		}
	}
}

function on_end_element_gpx($parser,$element)
{
	global $inDoc, $inTrk, $inTrkpt, $trackpoints, $waypoints, $curWpt, $inWpt,
			$inWptName;

	if($element=="TRKPT")
	{
		$inTrkpt=false;
	}
	elseif($inTrk && $element=="TRK")
	{
		$inTrk = false;
	}
	elseif($inWpt && $element=="WPT")
	{
		$inWpt = false;
		$waypoints[] = $curWpt;
	}
	elseif($inWptName && $element=="NAME")
		$inWptName = false;
	elseif($inDoc && $element=="GPX")
		$inDoc = false;
}

function on_characters_gpx($parser, $characters)
{
	global $inWptName, $curWpt;
	if($inWptName==true)
		$curWpt['name'] = $characters;
}
?>
