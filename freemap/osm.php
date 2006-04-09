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
$writeout=false;
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
				$location="http://www.openstreetmap.org/api/0.3/map",
				$wo=false)
{
	// Pull out half of tiles above and to left of current tile to avoid
	// cutting off labels
	$w1 = $w0 - ($e0-$w0)/2;
	$n1 = $n0 + ($n0-$s0)/2;

	switch($datamode)
	{
		case 1:
			$resp2 = parseOSM($location,$w0,$s0,$e0,$n0,$wo);
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

function parseOSM($osm,$west=null,$south=null,$east=null,$north=null,
					$wo=false)
{
	global $segments, $nodes, $ways, $w, $s, $e, $n, $writeout;
//	echo "parseOSM: osm = $osm<br/>";

	// 030406 setup bounding box for XML
//	echo "parseOSM: $osm $west $south $east $north<br/>";

	$w=$west;
	$s=$south;
	$e=$east;
	$n=$north;

	// Write OSM to stdout rather than store in memory?
	$writeout=$wo;
	/*
	if($writeout)
		echo "<osm version='0.3'>\n";
		*/

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

	/*
	if($writeout)
	{
		echo "</osm>\n";
		return false;
	}
	*/

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
			$inWay, $curWay, $ways, $waySegs, $writeout;

	if($element=='NODE')
	{
		$inNode = false;
		if($curNode)
		{
			if($writeout)
				//nodeToOSM($curID,$curNode);
				write_sql_node($curID, $curNode['lat'], $curNode['long'], 
									1, tagstring($curNode['tags']));
			$nodes[$curID] = $curNode; // 0.3 UID->ID
		}
	}
	elseif($element=='SEGMENT')
	{
		$inSegment = false;
		if($curSeg)
		{
			if($writeout)
			{
				write_sql_segment($curID, $curSeg['from'], $curSeg['to'], 
									1, tagstring($curSeg['tags']));
				//segToOSM($curID,$curSeg);
			}
			else
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
		if($writeout)
			write_sql_multi($curID, 1, $curWay['tags'],
									$curWay['segs']);

			//wayToOSM($curID,$curWay);
		else
			$ways[$curID] = $curWay;
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

function toOSM($osmver,$dataset)
{
	echo "<?xml version='1.0'?>\n";
	echo "<osm version='$osmver'>\n";
	foreach($dataset['nodes'] as $id=>$node)
		nodeToOSM($id,$node);
	foreach($dataset['segments'] as $id=>$seg)
		segToOSM($id,$seg);
	foreach($dataset['ways'] as $id=>$way)
		wayToOSM($id,$way);
	echo "</osm>\n";
}


function nodeToOSM($id,$node)
{
	echo "<node id='$id' lat='$node[lat]' lon='$node[long]'>\n";
	foreach ($node['tags'] as $k=>$v)
		echo "<tag k='$k' v='$v' />\n";
	echo "</node>\n";
}

function segToOSM($id,$seg)
{
	echo "<segment id='$id' from='$seg[from]' to='$seg[to]'>\n";
	foreach ($seg['tags'] as $k=>$v)
		echo "<tag k='$k' v='$v' />\n";
	echo "</segment>\n";
}

function wayToOSM($id, $way)
{
	echo "<way id='$id'>\n";
	foreach ($way['tags'] as $k=>$v)
		echo "<tag k='$k' v='$v' />\n";
	foreach ($way['segs'] as $seg)
		echo "<seg id='$seg' />\n";
	echo "</way>\n";
}

function write_sql_node($id, $lat, $lon, $user_id, $tags)
{

   	$q= 
	("insert into meta_nodes (timestamp, user_id) values (NOW(), ${user_id})" );

	echo "$q;\n";

    $q=( "insert into nodes (id, latitude, longitude, timestamp, user_id, visible, tags) values ( $id, ${lat}, ${lon}, NOW(), ${user_id}, 1, '$tags')" );
	echo "$q;\n";
}

function write_sql_segment($id, $node_a_id, $node_b_id, $user_id, $tags)
{

    $sql = 
		"insert into meta_segments (timestamp, user_id) values (NOW() , ".
		"${user_id})";
	echo "$sql;\n";

    $sql = "insert into segments (id, node_a, node_b, timestamp, user_id, visible, tags) values ($id, ${node_a_id}, ${node_b_id}, NOW(), ${user_id},1, '$tags')";

	echo "$sql;\n";

}

function write_sql_multi($id, $user_id, $tags, $segs, $type="way",$multi_id=0)
{
    echo ( "set @ins_time = NOW();" );
	echo "\n";
    echo( "set @user_id = ${user_id};" );
	echo "\n";


    echo( "insert into meta_${type}s (user_id, timestamp) values (@user_id, @ins_time)" );
	echo ";\n";
    echo( "set @id = $id ");
	echo ";\n";

    echo( "insert into ${type}s (id, user_id, timestamp) values (@id, @user_id, @ins_time)" );
	echo ";\n";
    echo( "set @version = last_insert_id()");
	echo ";\n";

    $tags_sql = "insert into ${type}_tags(id, k, v, version) values ";
    $first = true;
	foreach($tags as $k=>$v)
	{
	  if(!$first)
      	$tags_sql .= ',';
	  else
      	$first = false;
	  $tags_sql .= "(@id, '$k', '$v', @version)";
	}

    echo( $tags_sql );
	echo ";\n";

    $segs_sql="insert into ${type}_segments (id, segment_id, version) values ";

    $first = true;
	foreach($segs as $n)
	{
	  if(!$first)
      	$segs_sql .= ',' ;
	  else
      	$first = false;
      $segs_sql .= "(@id, '$n', @version)";
	}

    echo( $segs_sql );
	echo ";\n";
}
function tagstring($tags)
{
	$first=true;
	$ts="";
	if(is_array($tags))
	{
		foreach($tags as $k=>$v)
		{
			if(!$first)
				$ts.=";";
			else
				$first=false;
		
			$ts .= "$k=$v";
		}
	}
	return $ts;
}
?>
