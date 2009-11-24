<?php
require ("inc_config.php");

// Co-ordinates
$left = (float) $_GET ['left'];
$bottom = (float) $_GET ['bottom'];
$right = (float) $_GET ['right'];
$top = (float) $_GET ['top'];

// Checkbox values
$ref = (bool) $_GET ['ref'];
$name = (bool) $_GET ['name'];
$hours = (bool) $_GET ['hours'];
$source = (bool) $_GET ['source'];
$fixme = (bool) $_GET ['fixme'];
$naptan = (bool) $_GET ['naptan'];
$road = (bool) $_GET ['road'];
$pbref = (bool) $_GET ['pbref'];

// Store co-ordinates & checkbox values in cookies
$iExpireTime = time()+60*60*24*90;
setcookie ("left", $left, $iExpireTime);
setcookie ("bottom", $bottom, $iExpireTime);
setcookie ("right", $right, $iExpireTime);
setcookie ("top", $top, $iExpireTime);

setcookie ("ref", $ref, $iExpireTime);
setcookie ("name", $name, $iExpireTime);
setcookie ("hours", $hours, $iExpireTime);
setcookie ("source", $source, $iExpireTime);
setcookie ("fixme", $fixme, $iExpireTime);
setcookie ("naptan", $naptan, $iExpireTime);
setcookie ("road", $road, $iExpireTime);
setcookie ("pbref", $pbref, $iExpireTime);

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
 * $node: node/way to be checked
 * $tag: tag in $node to be checked for
 * $k/$v: define type of node to check (eg $k == "source", $v == "extrapolation")
 * Either $k or $v can be a wildcard (*)
 * $waynode - "way" or "node" to indicate whether $node is a way or a node
*/
function NoTagCheck ($node, $tag, $k, $v, $waynode) {
	global $iCount;

	if (($tag ["k"] == $k && $tag ["v"] == $v) ||
			($tag ["k"] == $k && $v == "*") ||
			($k == "*" && $tag ["v"] == $v)) {
		if ($waynode == "way") {
			$fWayLat = 0;
			$fWayLon = 0;
			GetWayLatLon ($node, $fWayLat, $fWayLon);
			$sOut = "<wpt lat='$fWayLat' lon='$fWayLon'>\n";
		}
		else
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
		// Post box reference
		if ($pbref === True)
			TagCheck ($node, $tag, "amenity", "post_box", "ref");

		// Name
		if ($name === True) {
			NodeCheck ($node, $tag, "*", "cafe", "name");
			NodeCheck ($node, $tag, "*", "restaurant", "name");
			NodeCheck ($node, $tag, "*", "pub", "name");

			NodeCheck ($node, $tag, "*", "school", "name");
			NodeCheck ($node, $tag, "*", "college", "name");
			NodeCheck ($node, $tag, "*", "library", "name");
			NodeCheck ($node, $tag, "*", "university", "name");

			NodeCheck ($node, $tag, "shop", "*", "name");
			NodeCheck ($node, $tag, "*", "post_office", "name");
			NodeCheck ($node, $tag, "amenity", "cinema", "name");
		}

		// Opening hours
		if ($hours === True) {
			NodeCheck ($node, $tag, "*", "cafe", "opening_hours");
			NodeCheck ($node, $tag, "shop", "*", "opening_hours");
			NodeCheck ($node, $tag, "*", "post_office", "opening_hours");
			NodeCheck ($node, $tag, "*", "fast_food", "opening_hours");
			NodeCheck ($node, $tag, "*", "pharmacy", "opening_hours");
			NodeCheck ($node, $tag, "*", "restaurant", "opening_hours");
			NodeCheck ($node, $tag, "*", "library", "opening_hours");
		}

		// Source
		if ($source === True) {
			NoTagCheck ($node, $tag, "source", "extrapolation");
			NoTagCheck ($node, $tag, "source", "NPE");
			NoTagCheck ($node, $tag, "source", "historical");
		}

		// FIXME tags
		if ($fixme === True)
			NoTagCheck ($node, $tag, "FIXME", "*");

		// NAPTAN import
		if ($naptan === True)
			NoTagCheck ($node, $tag, "naptan:verified", "no");
	}
}

// Check ways
foreach ($xml->way as $way) {
	if ($DEBUG)
		file_put_contents ($LOG_FILE, "Checking way {$way ["id"]}\n", FILE_APPEND);
	foreach ($way->tag as $tag) {

		// Ref
		if ($ref === True) {
			WayCheck ($way, $tag, "highway", "motorway", "ref");
			WayCheck ($way, $tag, "highway", "trunk", "ref");
			WayCheck ($way, $tag, "highway", "primary", "ref");
			WayCheck ($way, $tag, "highway", "secondary", "ref");
		}

		// Name
		if ($name === True) {
			WayCheck ($way, $tag, "highway", "residential", "name");

			WayCheck ($way, $tag, "*", "cafe", "name");
			WayCheck ($way, $tag, "*", "restaurant", "name");
			WayCheck ($way, $tag, "*", "pub", "name");

			WayCheck ($way, $tag, "*", "school", "name");
			WayCheck ($way, $tag, "*", "college", "name");
			WayCheck ($way, $tag, "*", "library", "name");
			WayCheck ($way, $tag, "*", "university", "name");

			WayCheck ($way, $tag, "shop", "*", "name");
			WayCheck ($way, $tag, "*", "post_office", "name");
			WayCheck ($way, $tag, "amenity", "cinema", "name");
		}

		// Opening hours
		if ($hours === True) {
			WayCheck ($way, $tag, "*", "cafe", "opening_hours");
			WayCheck ($way, $tag, "shop", "*", "opening_hours");
			WayCheck ($way, $tag, "*", "post_office", "opening_hours");
			WayCheck ($way, $tag, "*", "fast_food", "opening_hours");
			WayCheck ($way, $tag, "*", "pharmacy", "opening_hours");
			WayCheck ($way, $tag, "*", "restaurant", "opening_hours");
			WayCheck ($way, $tag, "*", "library", "opening_hours");
		}

		// Source
		if ($source === True) {
			NoTagCheck ($way, $tag, "source", "extrapolation");
			NoTagCheck ($way, $tag, "source", "NPE");
			NoTagCheck ($way, $tag, "source", "historical");
		}

		// FIXME etc
		if ($fixme === True)
			NoTagCheck ($way, $tag, "FIXME", "*");

		// Unknown road classification
		if ($road === True)
			NoTagCheck ($way, $tag, "highway", "road");
	}
}

echo "</gpx>\n";
?>
