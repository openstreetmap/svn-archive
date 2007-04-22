<?php
header("Content-type: text/xml");

require_once('../common/osmxml.php');

echo "<?xml version='1.0'?>\n";
echo "<rss version='2.0' xmlns:georss='http://www.georss.org/georss'>\n";
echo "<channel>\n";

$bbox = isset($_GET['bbox']) ? explode(",",$_GET['bbox']) :
	(isset($_GET['BBOX']) ? explode(",",$_GET['BBOX']): null);

if(is_array($bbox) && count($bbox)==4)
{
	$specifiedTags = array ( array("place","village"), 
							array("place","town"),
							array("place","city"), 
							array("amenity","pub"), 
							array("place","hamlet"),
							array("tourism","viewpoint"), 
							array("natural","peak"),
							array("residence","farm"),
							array("amenity","place_of_worship") );

	$data = grabOSM ($bbox[0], $bbox[1], $bbox[2], $bbox[3],  "osm", false, 
						$specifiedTags);
	foreach($data["nodes"] as $id=>$node)
	{
		echo "<item>\n";
		$title = "";
		if($node["tags"] && $node["tags"]["name"])
			$title .= $node["tags"]["name"];
		if($class = get_feature_class($node["tags"]))
		{
			$title .= "($class)";
			echo "<georss:featuretypetag>$class</georss:featuretypetag>\n";
		}
		echo "<title>$title</title>\n";
		if($node["tags"] && $node["tags"]["note"])
		{
			echo "<description>".$node["tags"]["note"]."</description>";
		}
		echo "<georss:point>$node[lat] $node[long]</georss:point>\n";
		echo "<guid>$id</guid>\n";
		echo "<link>http://www.openstreetmap.org/api/0.3/node/$id</link>";
		echo "</item>\n";
	}
}

echo "</channel>\n";
echo "</rss>\n";


function get_feature_class($tags)
{
	$classes = array ("pub" => array("amenity","pub"),
					  "peak" => array("natural","peak"),
					  "hamlet" => array("place","hamlet"),
					  "village" => array("place","village"),
					  "town" => array("place","town"),
					  "city" => array("place","city"),
					  "farm" => array("residence","farm"),
			  		  "church" => array("amenity","place_of_worship"),
					  "viewpoint" => array("tourism","viewpoint") );

	foreach($tags as $k=>$v)
	{
		foreach($classes as $class=>$kv) {
			if($k==$kv[0] && $v==$kv[1])
				return $class;
		}
	}
	return null;
}


?>
