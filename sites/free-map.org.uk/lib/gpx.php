<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-07 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");

#globals
$inTrkpt =  false;
$inDoc =  false;
$inTrk =  false;
$inWpt = false;
$inWptName=false;
$inTime = false;
$trackpoints = array();
$curWpt = null;
$waypoints = array();
$curPt = null;
#end globals

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
    return array ("trk"=>$trackpoints, "wp"=>$waypoints);
}

#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element_gpx($parser,$element,$attrs)
{
    global $inDoc, $inTrk, $inTrkpt, $trackpoints, $inWpt, $curWpt, $inWptName,
            $inTime, $curPt;

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
			$curPt = array();
            foreach($attrs as $name => $value)
            {
                if($name=="LAT")
                    $curPt["lat"] = $value; 
                elseif($name=="LON")
                    $curPt["lon"] = $value; 
            }
        }
        elseif($element=="TIME" && $inTrkpt)
        {
            $inTime=true;
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
            $inWptName, $inTime, $curPt;

    if($element=="TRKPT")
    {
        $inTrkpt=false;
		$trackpoints[] = $curPt;
    }
    elseif($inTrk && $element=="TRK")
    {
        $inTrk = false;
    }
    elseif($element=="TIME" && $inTrkpt)
    {
        $inTime=false;
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
    global $inWptName, $curWpt, $inTime, $curPt;
    if($inWptName==true)
        $curWpt['name'] = $characters;
	else if($inTime==true)
		$curPt['time'] = strtotime($characters);
}
?>
