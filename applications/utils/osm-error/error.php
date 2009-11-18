<?php
require ("inc_config.php");

// Co-ordinates
$left = (float) $_GET ['left'];
$bottom = (float) $_GET ['bottom'];
$right = (float) $_GET ['right'];
$top = (float) $_GET ['top'];

// Get OSM data
$xml = simplexml_load_file ("$osm_xapi_base/map?bbox=$left,$bottom,$right,$top");
if ($xml === False)
	die ("There was a problem getting data from OSM");

// Write header to download XML
header("Content-Type: application/gpx+xml");
header("Content-Disposition: attachment; filename=osm_error.gpx;");

// Initialise $iCount
$iCount = 1;

/*
 * Get the lat/lon for a way
 * $way: way to get lat/lon for
 * &$lat: returned latitude
 * &$lon: returned longitude
*/
function GetWayLatLon ($way, &$fWayLat, &$fWayLon) {
	global $xml;

	// Get lat/lon of first node
	$nodeid = $way->nd [0]["ref"];
	// Loop through nodes in XML
	foreach ($xml->node as $node) {
		// If node is part of the way, get lat & lon
		if ((int) $node ["id"] == $nodeid) {
			$fWayLat += (float) $node ["lat"];
			$fWayLon += (float) $node ["lon"];
			// Found node - return from function
			return;
		}
	}
}

/*
 * Check a node or way has a given tag
 * $node: the node or way to be checked
 * $checkfor: the key to check for
 * Returns true if the key exists, false if not
*/
function TagCheck ($node, $checkfor) {
	foreach ($node->tag as $tag)
		if ($tag ["k"] == $checkfor)
			return True;
	return False;
}

/*
 * Check a node or way does *not* have a tag.
 * If tag does exist, writes a waypoint
 * $node: node to be checked
 * $tag: tag in $node to be checked for
 * $k/$v: define type of node to check (eg $k == "source", $v == "extrapolation")
 * Either $k or $v can be a wildcard (*)
*/
function NoTagCheck ($node, $tag, $k, $v) {
	global $iCount;

	if (($tag ["k"] == $k && $tag ["v"] == $v) ||
			($tag ["k"] == $k && $v == "*") ||
			($k == "*" && $tag ["v"] == $v)) {
		$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
		$sOut .= "<name>" . $iCount++ . " $k - {$tag ["v"]}</name>\n</wpt>\n";
		echo $sOut;
	}
}

/*
 * Check a node has a tag.
 * If tag does not exist, writes a waypoint
 * $node: node to be checked
 * $tag: tag in $node to be checked
 * $k/$v: define type of node to check (eg $k == "shop", $v == "supermarket")
 * Either $k or $v can be a wildcard (*)
 * $checkfor: tag to check for existence of (eg "opening_hours")
*/
function NodeCheck ($node, $tag, $k, $v, $checkfor) {
	global $iCount;

	if (($tag ["k"] == $k && $tag ["v"] == $v) ||
			($tag ["k"] == $k && $v == "*") ||
			($k == "*" && $tag ["v"] == $v))
		if (TagCheck ($node, $checkfor) === False) {
			$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
			if ($v == "*")
				$sOut .= "<name>" . $iCount++ . " $k - $checkfor</name>\n</wpt>\n";
			else
				$sOut .= "<name>" . $iCount++ . " $v - $checkfor</name>\n</wpt>\n";
			echo $sOut;
		}
}

/*
 * Check a way has a tag.
 * If tag does not exist, writes a waypoint
 * $node: node to be checked
 * $tag: tag in $way to be checked
 * $k/$v: define type of way to check (eg $k == "highway", $v == "residential")
 * Either $k or $v can be a wildcard (*)
 * $checkfor: tag to check for existence of (eg "name")
*/
function WayCheck ($way, $tag, $k, $v, $checkfor) {
	global $iCount;

	if (($tag ["k"] == $k && $tag ["v"] == $v) ||
			($tag ["k"] == $k && $v == "*") ||
			($k == "*" && $tag ["v"] == $v))
		if (TagCheck ($way, $checkfor) === False) {
			$lat = 0;
			$lon = 0;
			GetWayLatLon ($way, $lat, $lon);
			$sOut = "<wpt lat='$lat' lon='$lon'>\n";
			if ($v == "*")
				$sOut .= "<name>" . $iCount++ . " $k - $checkfor</name>\n</wpt>\n";
			else
				$sOut .= "<name>" . $iCount++ . " $v - $checkfor</name>\n</wpt>\n";
			echo $sOut;
		}
}

//Write file header
echo "<?xml version='1.0' encoding='UTF-8'?>\n";
echo <<<END
<gpx
  version="1.0"
  creator="OSM-Error - http://www.mappage.org/osmerror/"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns="http://www.topografix.com/GPX/1/0"
  xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
END;
echo "\n";

// Check nodes
foreach ($xml->node as $node) {
	if ($DEBUG)
		file_put_contents ($LOG_FILE, "Checking node {$node ["id"]}\n", FILE_APPEND);
	foreach ($node->tag as $tag) {
		// Post boxes
		TagCheck ($node, $tag, "amenity", "post_box", "ref");

		// Name
		NodeCheck ($node, $tag, "amenity", "cafe", "name");
		NodeCheck ($node, $tag, "amenity", "pub", "name");
		NodeCheck ($node, $tag, "shop", "*", "name");

		// Opening hours
		NodeCheck ($node, $tag, "amenity", "cafe", "opening_hours");
		NodeCheck ($node, $tag, "shop", "*", "opening_hours");
		NodeCheck ($node, $tag, "*", "fast_food", "opening_hours");
		NodeCheck ($node, $tag, "*", "pharmacy", "opening_hours");

		// Source
		NoTagCheck ($node, $tag, "source", "extrapolation");

		//FIXME tags
		NoTagCheck ($node, $tag, "FIXME", "*");
	}
}

// Check ways
foreach ($xml->way as $way) {
	if ($DEBUG)
		file_put_contents ($LOG_FILE, "Checking way {$way ["id"]}\n", FILE_APPEND);
	foreach ($way->tag as $tag) {
		// Name
		WayCheck ($way, $tag, "highway", "residential", "name");
		WayCheck ($way, $tag, "shop", "*", "name");

		// Opening hours
		WayCheck ($way, $tag, "shop", "*", "opening_hours");

		// Source
		NoTagCheck ($node, $tag, "source", "extrapolation");

		//FIXME tags
		NoTagCheck ($node, $tag, "FIXME", "*");
	}
}

echo "</gpx>\n";
?>
