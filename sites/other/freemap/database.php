<?php
//streets.php
//grabs things from the database and stores them in a Freemap data structure
//adapted from streets.pl

// Structure of returned data:
// nodes = lat, long and tags, associative array by node ID
// segments = from, to and tags, associative array by segment ID
// ways = segs and tags, associative array by way ID

require_once('defines.php');

function grab_direct_from_database ($west, $south, $east, $north)
{
	$nodes=array();
	$segs=array();
	$ways=array();

	$conn=mysql_connect("localhost",DB_USERNAME,DB_PASSWORD);
	mysql_select_db(DB_DBASE);

	// Taken straight from streets.pl and dao.rb

	$sql = "select id, latitude, longitude, visible, tags from (select * from (select nodes.id, nodes.latitude, nodes.longitude, nodes.visible, nodes.tags from nodes, nodes as a where a.latitude > $south  and a.latitude < $north  and a.longitude > $west and a.longitude < $east and nodes.id = a.id order by nodes.timestamp desc) as b group by id) as c where visible = true and latitude > $south and latitude < $north  and longitude > $west and longitude < $east";

	$clause=null;

//	echo "Nodes SQL is : $sql<br/>";

	$result = mysql_query($sql);
	while ($row = mysql_fetch_array($result)) 
	{

		$curNode["lat"] = $row["latitude"];
		$curNode["long"] = $row["longitude"];
		$curNode["tags"] = get_tag_array($row["tags"]);
		$nodes[$row["id"]] = $curNode;
	
		if (! $clause) {
			$clause .= $row["id"];
		} else {
			$clause .= ',' . $row["id"];
		}
	}

	// Also need nodes outside the bbox which belong to a segment inside

	//select stuff from all nodes NOT IN THE BBOX where node id IN (all segment node as and node bs not the bbox)

	// Taken straight from streets.pl

	/* 090406 Replace this call by the one below designed to get segments which
	   pass through the bounding box but with no nodes within
	   */
	$sql = "SELECT segment.id, segment.node_a, segment.node_b, segment.tags FROM ( select * from (SELECT * FROM segments where node_a IN ($clause) OR node_b IN ($clause) ORDER BY timestamp DESC) as a group by id) as segment where visible = true";
//	echo "Segments SQL is : $sql<br/>";

	$result = mysql_query($sql);

	while($row=mysql_fetch_array($result))
	{
		$curSeg["from"] = $row["node_a"];
		$curSeg["to"] = $row["node_b"];
		$segnodes[] = $row["node_a"];
		$segnodes[] = $row["node_b"];
		$curSeg["tags"] = get_tag_array($row["tags"]);
		$segs[$row["id"]] = $curSeg;
	}
/*
	// This query should get segments which pass through the bbox - but have
	// no nodes within it - see mailing list discussion 30/03/06
	// NOT in any pre-existing OSM code!
	$result = mysql_query(
    "SELECT s.id,s.node_a,s.node_b,s.tags FROM segments as s, nodes as a, nodes as b where s.node_a=a.id and s.node_b=b.id and ( ((a.latitude between $south and $north) or (b.latitude between $south and $north) or (a.latitude<$south and b.latitude>$north) or (b.latitude<$south and a.latitude>$north)) and ((a.longitude between $west and $east) or (b.longitude between $west and $east) or (a.longitude<$west and b.longitude>$east) or (b.longitude<$west and a.longitude>$east)))"
						 );

	
	while($row=mysql_fetch_array($result))
	{
		$curSeg["from"] = $row["node_a"];
		$curSeg["to"] = $row["node_b"];
		$segnodes[] = $row["node_a"];
		$segnodes[] = $row["node_b"];
		$curSeg["tags"] = get_tag_array($row["tags"]);
		$segs[$row["id"]] = $curSeg;
	}

*/

	// We need to get nodes outside the bounding box which belong to the
	// segments we have just grabbed.
//	echo "SEGNODES is:";
//	print_r($segnodes);

	/*
	//echo "<br/>";
	$newnodes = array_diff (array_unique($segnodes),array_keys($nodes));

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
		$curNode["tags"] = get_tag_array($row["tags"]);
		$nodes[$row["id"]] = $curNode;
	}

	   */
	//$ways = get_multis_from_segments(array_keys($segs));
	$ways = array();

	return array ("nodes"=>$nodes, "segments"=>$segs, "ways"=>$ways);
}

// Adapted from same function in dao.rb

function get_multis_from_segments($segment_ids, $type="way")
{
	$segment_clause = "(".implode(",",$segment_ids).")";
	$sql =  
     "select g.id from ${type}s as g, 
        (select id, max(version) as version from ${type}s where id in 
          (select distinct a.id from
            (select id, max(version) as version from ${type}_segments where id in (select id from ${type}_segments where segment_id in ${segment_clause}) group by id) as a
          ) group by id
         ) as b where g.id = b.id and g.version = b.version and g.visible = 1 ";
          
	$multis = array();

	//echo "Ways sql is : $sql<br/>";
	$result=mysql_query($sql);
	while($row=mysql_fetch_array($result))
	{
		$multis[$row["id"]] = get_multi($row["id"]); 
	}
	return $multis;
}

// Adapted from same function in dao.rb

function get_multi($multi_id, $type="way", $version=null)
{
      $clause = ' order by version desc limit 1;';
	  if($version)
      	$clause = " and version = ${version} ";

      $result = mysql_query
	  ("select version, visible, timestamp from ${type}s where id = $multi_id ".
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

	  $sql= "select k,v from ${type}_tags where id = $multi_id and ".
	  		"version = $version";

      $result = mysql_query($sql);
      $tags = array();
	  while($row=mysql_fetch_array($result))
		$tags[$row["k"]]=$row["v"];

      # if looking at an old version then get segments as they were then
      $tclause = '';

	  // not sure about this
      //$tclause = " and timestamp <= '${timestamp}' " unless version.nil?

      $result = mysql_query( 
	  "select id as n from (select * from (select segments.id, visible, timestamp from ${type}_segments left join segments on ${type}_segments.segment_id = segments.id where ${type}_segments.id = $multi_id and version = ${version} ${tclause} order by id, timestamp desc) as a group by id) as b where visible = 1;" );

	  $segs=array();
	  while($row=mysql_fetch_array($result))
	  	$segs[] = $row["n"];

	  return array ("tags"=>$tags, "segs"=>$segs);
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
