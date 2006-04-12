<?php

# dataset.php
# class representing the set of nodes, segments and ways

require_once('defines.php');
require_once('osm.php');

class Dataset
{
	var $nodes, $segments, $ways;

	function Dataset()
	{
		$this->nodes = $this->segments = $this->ways = array();
	}

	function grab_direct_from_database ($west, $south, $east, $north)
	{

		// Pull out half of tiles above and to left of current tile to avoid
		// cutting off labels
		$west = $west - ($east-$west)/2;
		$north = $north + ($north-$south)/2;
		$east = $east + ($east-$west)/2;
		$sout = $south - ($north-$south)/2;

		$conn=mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
		mysql_select_db(DB_DBASE);

		// Taken straight from streets.pl and dao.rb

		$sql = "select id, latitude, longitude, visible, tags from ".
			   "(select * from (select nodes.id, nodes.latitude, ".
			   "nodes.longitude, nodes.visible, nodes.tags from nodes, ".
			   "nodes as a where a.latitude > $south  and a.latitude < $north ".
			   "and a.longitude > $west and a.longitude < $east and ".
			   "nodes.id = a.id order by nodes.timestamp desc) as b ".
			   "group by id) as c where visible = true and latitude > $south ".
			   "and latitude < $north  and longitude > $west and ".
			   "longitude < $east";

		$clause=null;

		$result = mysql_query($sql);
		while ($row = mysql_fetch_array($result)) 
		{

			$curNode["lat"] = $row["latitude"];
			$curNode["long"] = $row["longitude"];
			$curNode["tags"] = $this->get_tag_array($row["tags"]);
			$this->nodes[$row["id"]] = $curNode;
	
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
			
		$sql = "SELECT segment.id, segment.node_a, segment.node_b, ".
		       "segment.tags FROM ( select * from (SELECT * FROM segments ".
			   "where node_a IN ($clause) OR node_b IN ($clause) ORDER BY ".
			   "timestamp DESC) as a group by id) as segment where ".
			   "visible = true";

		$result = mysql_query($sql);

		while($row=mysql_fetch_array($result))
		{
			$curSeg["from"] = $row["node_a"];
			$curSeg["to"] = $row["node_b"];
			$segnodes[] = $row["node_a"];
			$segnodes[] = $row["node_b"];
			$curSeg["tags"] = $this->get_tag_array($row["tags"]);
			$this->segments[$row["id"]] = $curSeg;
		}


		// This query should get segments which pass through the bbox - but have
		// no nodes within it - see mailing list discussion 30/03/06
		// NOT in any pre-existing OSM code!
/*
		$result = mysql_query(
    		"SELECT s.id,s.node_a,s.node_b,s.tags FROM segments as s, nodes ".
			"as a, nodes as b where s.node_a=a.id and s.node_b=b.id and ".
			"( ((a.latitude between $south and $north) or (b.latitude ".
			"between $south and $north) or (a.latitude<$south and ".
			"b.latitude>$north) or (b.latitude<$south and a.latitude>$north)) ".
			"and ((a.longitude between $west and $east) or (b.longitude ".
			"between $west and $east) or (a.longitude<$west and ".
			"b.longitude>$east) or (b.longitude<$west and a.longitude>$east)))"
						 );

		while($row=mysql_fetch_array($result))
		{
			$curSeg["from"] = $row["node_a"];
			$curSeg["to"] = $row["node_b"];
			$segnodes[] = $row["node_a"];
			$segnodes[] = $row["node_b"];
			$curSeg["tags"] = $this->get_tag_array($row["tags"]);
			$this->segments[$row["id"]] = $curSeg;
		}


		// We need to get nodes outside the bounding box which belong to the
		// segments we have just grabbed.

		//echo "<br/>";
		$newnodes = array_diff (array_unique($segnodes),
						array_keys($this->nodes));

		// Form an SQL clause for the nodes we need to get
		$clause = "(". implode(",",$newnodes) . ")";

		#	echo "CLAUSE: $clause<br/>";

		$sql = "select id,latitude,longitude,tags from nodes where ".
			"id in $clause and visible=1";
		$result=mysql_query($sql);
		while($row=mysql_fetch_array($result))
		{
			$curNode["lat"] = $row["latitude"];
			$curNode["long"] = $row["longitude"];
			$curNode["tags"] = $this->get_tag_array($row["tags"]);
			$this->nodes[$row["id"]] = $curNode;
		}
		*/
		$this->get_ways_from_segments(array_keys($this->segments));

	}

	// Adapted from same function in dao.rb

	function get_ways_from_segments($segment_ids)
	{
		$type="way";
		$segment_clause = "(".implode(",",$segment_ids).")";
		$sql =  
     		"select g.id from ${type}s as g, 
        (select id, max(version) as version from ${type}s where id in ".
		" (select distinct a.id from ".
            "(select id, max(version) as version from ${type}_segments where ".
			"id in (select id from ${type}_segments where segment_id in ".
			"${segment_clause}) group by id) as a ".
          ") group by id".
         ") as b where g.id = b.id and g.version = b.version and g.visible=1 ";
          
		$result=mysql_query($sql);
		while($row=mysql_fetch_array($result))
			$this->get_way($row["id"]); 
	}

	// Adapted from same function in dao.rb

	function get_way($way_id,$version=null) 
	{
		$type="way";
      	$clause = ' order by version desc limit 1;';
	  	if($version)
      		$clause = " and version = ${version} ";

      	$result = mysql_query
		("select version, visible, timestamp from ${type}s where id=$way_id ".
	 	 $clause);

      	$version = 0;
      	$visible = true;
      	$timestamp = '';
	  	while($row=mysql_fetch_array($result))
	  	{
        	$version = $row['version'];
        	$timestamp = $row['timestamp'];
        	$visible = $row['visible'];
	  	}

      	if(!$version) return null;

	  	$sql= "select k,v from ${type}_tags where id = $way_id and ".
	  		"version = $version";

      	$result = mysql_query($sql);
      	$this->ways[$way_id]["tags"] = array();
	  	while($row=mysql_fetch_array($result))
			$this->ways[$way_id]["tags"][$row["k"]]=$row["v"];

      	# if looking at an old version then get segments as they were then
      	$tclause = '';

	  	// not sure about this
      	//$tclause = " and timestamp <= '${timestamp}' " unless version.nil?

      	$result = mysql_query( 
	  	"select id as n from (select * from (select segments.id, visible, ".
		"timestamp from ${type}_segments left join segments on ".
		"${type}_segments.segment_id = segments.id where ${type}_segments.id ".
		"= $way_id and version = ${version} ${tclause} order by id, ".
		"timestamp desc) as a group by id) as b where visible = 1;" );

	  	$this->ways[$way_id]["segs"]=array();
	  	while($row=mysql_fetch_array($result))
	  		$this->ways[$way_id]["segs"][] = $row["n"];
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

	function give_segs_waytags()
	{
		foreach($this->ways as $way)
		{
			foreach($way["segs"] as $wayseg)
			{
				$this->segments[$wayseg]["tags"] = $way["tags"];
			}
		}
	}

	function dump()
	{
		foreach ($this->nodes as $id=>$node)
		{
			echo "ID: $id<br/>";
			echo "Lat: $node[lat]<br/>";
			echo "Lon: $node[long]<br/>";
			if(isset($node['tags']))
			{
				echo "Tags: <br/>";
				foreach ($node['tags'] as $k=>$v)
					echo "    key=$k, value=$v<br/>";
			}
		}
		foreach ($this->segments as $id=>$segment)
		{
			echo "ID: $id<br/>";
			echo "From: $segment[from]<br/>";
			echo "To: $segment[to]<br/>";
			if(isset($segment['tags']))
			{
				echo "Tags: <br/>";
				foreach ($segment['tags'] as $k=>$v)
					echo "    key=$k, value=$v<br/>";
			}
		}

		foreach ($this->ways as $id=>$way)
		{
			echo "ID: $id<br/>";
			if(isset($way['tags']))
			{
				echo "Tags:<br/>";
				foreach ($way['tags'] as $k=>$v)
					echo "    key=$k, value=$v<br/>";
			}
			echo "Segs:<br/>";
			foreach ($way['segs'] as $seg)
				echo "    $seg<br/>";
		}
	}

	// $datamode can be:
	// 0 = grab data from OSM using the API 
	// 1 = grab data from a local XML file
	// $location is either an API URL or a local XML file

	function grabOSM($w0, $s0, $e0, $n0, $netgrab=true, 
				$location="http://www.openstreetmap.org/api/0.3/map")
	{
		// Pull out half of tiles above and to left of current tile to avoid
		// cutting off labels
		$w1 = $w0 - ($e0-$w0)/2;
		$n1 = $n0 + ($n0-$s0)/2;
		$e1 = $e0 + ($e0-$w0)/2;
		$s1 = $s0 - ($n0-$s0)/2;
		$dset = null;

		switch($netgrab)
		{
			case false:
				$data = parseOSM($location,$w1,$s1,$e1,$n1);
				break;

			case true:
				$url = "$location?bbox=$w1,$s1,$e1,$n1";
				$ch=curl_init ($url);
				curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
				curl_setopt($ch,CURLOPT_HEADER,false);
				curl_setopt($ch,CURLOPT_USERPWD,OSM_LOGIN);
				$resp=curl_exec($ch);
				curl_close($ch);
				$data = parseOSM(explode("\n",$resp));
				break;
		}
		$this->nodes = $data["nodes"];
		$this->segments = $data["segments"];
		$this->ways = $data["ways"];
	}
}
?>
