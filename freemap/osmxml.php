<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");
require_once('defines.php');
//require_once('database.php');
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

// $datasrc can be:
// "osm" = grab data from OSM using the API 
// "file" = grab data from a local XML file
// "db" = grab data from the database
// $location is either an API URL or a local XML file

function grabOSM($w0, $s0, $e0, $n0, $datasrc="db", $zoom=null, 
				$location="http://www.openstreetmap.org/api/0.3/map")
{
	$resp2=array("nodes"=>array(),"segments"=>array(),"ways"=>array());
	// Pull out half of tiles above and to left of current tile to avoid
	// cutting off labels
	// Pull out half of tiles above and to left of current tile to avoid
	// cutting off labels

	// 0.5 is acceptable at zoom 13 and less
	$factor = ($zoom) ? 0.5 : (($zoom<13) ? 0.5 : 0.5*pow(2,$zoom-13));

	$w1 = $w0 - ($e0-$w0)*$factor;
	$n1 = $n0 + ($n0-$s0)*$factor;
	$e1= $e0 + ($e0-$w0)*$factor;
	$s1 = $s0 - ($n0-$s0)*$factor;

	switch($datasrc)
	{

		case "db": // grab from local database
			grab_direct_from_database($resp2,$w1,$s1,$e1,$n1);
			break;

		case "osm": // grab from live OSM API
			$url = "$location?bbox=$w1,$s0,$e0,$n1";
			$ch=curl_init ($url);
			curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
			curl_setopt($ch,CURLOPT_HEADER,false);
			curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
			$resp=curl_exec($ch);
			//echo $resp;
			curl_close($ch);
			$resp2 = parseOSM(explode("\n",$resp));
			break;

		case "file": // grab from local file
			$resp2 = parseOSM($location,$w0,$s0,$e0,$n0);
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
				$curNode['tags']=array();
				$curID = $attrs["ID"];
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

function grab_direct_from_database (&$data, $west, $south, $east, $north)
{

	$conn=mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	// Taken straight from streets.pl and dao.rb
	$sql = "select id, latitude, longitude, visible, tags from ".
	   "nodes where latitude > $south  and latitude < $north ".
	   "and longitude > $west and longitude < $east ";

	$clause=null;

	$result = mysql_query($sql);
	while ($row = mysql_fetch_array($result)) 
	{

		$curNode["lat"] = $row["latitude"];
		$curNode["long"] = $row["longitude"];
		$curNode["tags"] = get_tag_array($row["tags"]);
		$data["nodes"][$row["id"]] = $curNode;

		if (! $clause) 
		{
			$clause .= $row["id"];
		} 
		else 
		{
			$clause .= ',' . $row["id"];
		}
	}

	// Also need nodes outside the bbox which belong to a segment inside

	// Taken straight from streets.pl

	/* 090406 Replace this call by the one below designed to get segments 
	which pass through the bounding box but with no nodes within
	*/
	

	$sql = "SELECT id, node_a, node_b, ".
	   "tags FROM segments ".
	   "where node_a IN ($clause) OR node_b IN ($clause) ".
	   "and visible = 1";

	$result = mysql_query($sql);

	// suppress error message
	while($row=@mysql_fetch_array($result))
	{
		$curSeg["from"] = $row["node_a"];
		$curSeg["to"] = $row["node_b"];
		$segnodes[] = $row["node_a"];
		$segnodes[] = $row["node_b"];
		$curSeg["tags"] = get_tag_array($row["tags"]);
		$data["segments"][$row["id"]] = $curSeg;
	}

	get_ways_from_segments($data,array_keys($data["segments"]));

}

// Adapted from same function in dao.rb

function get_ways_from_segments(&$data,$segment_ids)
{
	$type="way";
	$segment_clause = "(".implode(",",$segment_ids).")";

	$sql = "select id from ${type}_segments where segment_id in ".
	"${segment_clause} group by id";

	$result=mysql_query($sql) or die(mysql_error());
	while($row=@mysql_fetch_array($result))
	{
		get_way($data,$row["id"]); 
	}
}

// Adapted from same function in dao.rb

function get_way(&$data,$way_id,$version=null) 
{
	$type="way";
	$data["ways"][$way_id] = array();

	$sql= "select k,v from ${type}_tags where id = $way_id";

	// again remove version stuff and ".  "version = $version";

	$result = mysql_query($sql);
	$data["ways"][$way_id]["tags"] = array();
	while($row=mysql_fetch_array($result))
		$data["ways"][$way_id]["tags"][$row["k"]]=$row["v"];

	// might this be quicker?  - NO
	//$data["ways"][$way_id]["tags"] =$this->get_tag_array($row["tags"]);


	$result = mysql_query("select segment_id as n from ${type}_segments ".
				"where id = $way_id");


	$data["ways"][$way_id]["segs"]=array();
	while($row=mysql_fetch_array($result))
		$data["ways"][$way_id]["segs"][] = $row["n"];
}

function get_tag_array($tagstring)
{
	$tags=array();
	$t = explode(";",$tagstring);
	foreach ($t as $t1)
	{
		$t2 = explode("=",$t1);
		if($t2 && $t2[0] && $t2[0]!="")
			$tags[$t2[0]] = $t2[1];
	}
	return $tags;
}

?>
