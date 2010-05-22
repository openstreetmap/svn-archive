<?php
/*
Code to get co-ordinates from map taken from http://maposmatic.org/ and
copyright (c) 2009 Étienne Loks <etienne.loks_AT_peacefrogsDOTnet>
Other code copyright (c) 2009-2010 Russ Phillips <russ AT phillipsuk DOT org>

This file is part of OSM Error.

OSM Error is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

require ("inc_config.php");

// Co-ordinates
$left = (float) $_GET ['lon_upper_left'];
$bottom = (float) $_GET ['lat_bottom_right'];
$right = (float) $_GET ['lon_bottom_right'];
$top = (float) $_GET ['lat_upper_left'];

// Checkbox values
$ref = (bool) $_GET ['ref'];
$name = (bool) $_GET ['name'];
$hours = (bool) $_GET ['hours'];
$source = (bool) $_GET ['source'];
$fixme = (bool) $_GET ['fixme'];
$naptan = (bool) $_GET ['naptan'];
$road = (bool) $_GET ['road'];
$pbref = (bool) $_GET ['pbref'];

// Waypoint name length
$namelen = (int) $_GET ['namelen'];

// Store values in cookies
$iExpireTime = time ()+60*60*24*365;
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

setcookie ("namelen", $namelen, $iExpireTime);

// Get OSM data
$xml = simplexml_load_file ($osm_api_base . "?bbox=$left,$bottom,$right,$top");
if ($xml === False)
	die ("There was a problem getting data from OSM. Go back and try a smaller area.");

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
		// If node is first part of the way, get lat & lon
		if ((int) $node ["id"] == $nodeid) {
			$fWayLat = (float) $node ["lat"];
			$fWayLon = (float) $node ["lon"];
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
		if (strtolower ($tag ["k"]) == strtolower ($checkfor))
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
function NoTagCheck ($node, $tag, $k, $v, $waynode, $name, $shortname) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$k = strtolower ($k);
	$v = strtolower ($v);
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if (($tagk == $k && $tagv == $v) ||
			($tagk == $k && $v == "*") ||
			($k == "*" && $tagv == $v)) {
		if ($waynode == "way") {
			$fWayLat = 0;
			$fWayLon = 0;
			GetWayLatLon ($node, $fWayLat, $fWayLon);
			$sOut = "<wpt lat='$fWayLat' lon='$fWayLon'>\n";
		}
		else
			$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
		$sErrText =  "$k - " . $tag ["v"];
		if ($namelen == 6)
			$sOut .= "<name>$shortname" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>$name" . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>$sErrText (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>$sErrText</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
	}
}

/*
 * Check a node has a source=OS_OpenData tag
 * If tag exists, writes a waypoint
 * $node: node to be checked
 * $tag: tag in $node to be checked
*/
function NodeCheckOS ($node, $tag) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if ($tagk == "source" && substr ($tagv, 0, 11) == "os_opendata") {
		$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
		if ($namelen == 6)
			$sOut .= "<name>SrcOS" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>Source OS " . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>Source OS (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>Sourced from OS data ({$tag ['v']})</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
	}
}

/*
 * Check a way has a source=OS_OpenData tag
 * If tag exists, writes a waypoint
 * $way: way to be checked
 * $tag: tag in $way to be checked
*/
function WayCheckOS ($way, $tag) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if ($tagk == "source" && substr ($tagv, 0, 11) == "os_opendata") {
		$lat = 0;
		$lon = 0;
		GetWayLatLon ($way, $lat, $lon);
		$sOut = "<wpt lat='$lat' lon='$lon'>\n";
		if ($namelen == 6)
			$sOut .= "<name>SrcOS" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>Source OS " . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>Source OS (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>Sourced from OS data ({$tag ['v']})</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
	}
}

/* **************************************************************** */
/*
 * Check a node has "fixme" in the description
 * If it exists, write a waypoint
 * $node: node to be checked
 * $tag: tag in $node to be checked
*/
function NodeFixmeDescription ($node, $tag) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if ($tagk == "description" && strstr ($tagv, "fixme") !== False) {
		$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
		if ($namelen == 6)
			$sOut .= "<name>FIXME" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>FIXME " . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>FIXME (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>Fixme in description - {$tag ['v']}</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
	}
}

/*
 * Check a way has "fixme" in the description
 * If it exists, write a waypoint
 * $way: way to be checked
 * $tag: tag in $way to be checked
*/
function WayFixmeDescription ($way, $tag) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if ($tagk == "description" && strstr ($tagv, "fixme") !== False) {
		$lat = 0;
		$lon = 0;
		GetWayLatLon ($way, $lat, $lon);
		$sOut = "<wpt lat='$lat' lon='$lon'>\n";
		if ($namelen == 6)
			$sOut .= "<name>FIXME" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>FIXME " . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>FIXME (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>Fixme in description - {$tag ['v']}</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
	}
}
/* **************************************************************** */

/*
 * Check a node has a tag.
 * If tag does not exist, writes a waypoint
 * $node: node to be checked
 * $tag: tag in $node to be checked
 * $k/$v: define type of node to check (eg $k == "shop", $v == "supermarket")
 * Either $k or $v can be a wildcard (*)
 * $checkfor: tag to check for existence of (eg "opening_hours")
*/
function NodeCheck ($node, $tag, $k, $v, $checkfor, $name, $shortname) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$k = strtolower ($k);
	$v = strtolower ($v);
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if (($tagk == $k && $tagv == $v) ||
			($tagk == $k && $v == "*") ||
			($k == "*" && $tagv == $v))
		if (TagCheck ($node, $checkfor) === False) {
			$sOut = "<wpt lat='" . $node ["lat"] . "' lon='" . $node ["lon"] . "'>\n";
			if ($v == "*")
				$sErrText = "$k - $checkfor";
			else
				$sErrText = "$v - $checkfor";
		if ($namelen == 6)
			$sOut .= "<name>$shortname" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>$name" . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>$sErrText (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>$sErrText</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
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
function WayCheck ($way, $tag, $k, $v, $checkfor, $name, $shortname) {
	global $iCount, $DEBUG, $LOG_FILE;
	//Make check case-insensitive
	$k = strtolower ($k);
	$v = strtolower ($v);
	$tagk = strtolower ($tag ["k"]);
	$tagv = strtolower ($tag ["v"]);

	if (($tagk == $k && $tagv == $v) ||
			($tagk == $k && $v == "*") ||
			($k == "*" && $tagv == $v))
		if (TagCheck ($way, $checkfor) === False) {
			$lat = 0;
			$lon = 0;
			GetWayLatLon ($way, $lat, $lon);
			$sOut = "<wpt lat='$lat' lon='$lon'>\n";
			if ($v == "*")
				$sErrText = "$k - $checkfor";
			else
				$sErrText = "$v - $checkfor";
		if ($namelen == 6)
			$sOut .= "<name>$shortname" . $iCount++ . "</name>\n";
		elseif ($namelen == 14)
			$sOut .= "<name>$name" . $iCount++ . "</name>\n";
		else
			$sOut .= "<name>$sErrText (" . $iCount++ . ")</name>\n";
		$sOut .= "<desc>$sErrText</desc>\n";
		$sOut .= "</wpt>\n";
		echo $sOut;
		if ($DEBUG)
			file_put_contents ($LOG_FILE, "\t$sErrText\n\tLat/Lon: {$node ['lat']}, {$node ['lon']}\n", FILE_APPEND);
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
			NodeCheck ($node, $tag, "amenity", "post_box", "ref", "Postbox ref", "pb ref");

		// Name
		if ($name === True) {
			NodeCheck ($node, $tag, "*", "cafe", "name", "Cafe name ", "Name");
			NodeCheck ($node, $tag, "*", "restaurant", "name", "Restnt name", "Name");
			NodeCheck ($node, $tag, "*", "pub", "name", "Pub name ", "Name");

			NodeCheck ($node, $tag, "*", "school", "name", "School name", "Name");
			NodeCheck ($node, $tag, "*", "college", "name", "Coll name ", "Name");
			NodeCheck ($node, $tag, "*", "library", "name", "Liby name ", "Name");
			NodeCheck ($node, $tag, "*", "university", "name", "Uni name ", "Name");

			NodeCheck ($node, $tag, "shop", "*", "name", "Shop name ", "Name");
			NodeCheck ($node, $tag, "*", "post_office", "name", "POffice nme", "Name");
			NodeCheck ($node, $tag, "amenity", "cinema", "name", "Cinema name", "Name");
		}

		// Opening hours
		if ($hours === True) {
			NodeCheck ($node, $tag, "*", "cafe", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "shop", "*", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "*", "post_office", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "*", "fast_food", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "*", "pharmacy", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "*", "restaurant", "opening_hours", "Open hours ", "OHours");
			NodeCheck ($node, $tag, "*", "library", "opening_hours", "Open hours ", "OHours");
		}

		// Source
		if ($source === True) {
			NoTagCheck ($node, $tag, "source", "extrapolation", "node", "Src extrap", "Source");
			NoTagCheck ($node, $tag, "source", "NPE", "node", "Src NPE ", "SrcNPE");
			NoTagCheck ($node, $tag, "source", "historical", "node", "Src Hist ", "SrcHis");
			NodeCheckOS ($node, $tag);
		}

		// FIXME tags
		if ($fixme === True)
			NoTagCheck ($node, $tag, "FIXME", "*", "node", "Fixme ", "Fixme");

		// NAPTAN import
		if ($naptan === True)
			NoTagCheck ($node, $tag, "naptan:verified", "no", "node", "Naptan vrfy", "Naptan");
	}
}

// Check ways
foreach ($xml->way as $way) {
	if ($DEBUG)
		file_put_contents ($LOG_FILE, "Checking way {$way ["id"]}\n", FILE_APPEND);
	foreach ($way->tag as $tag) {

		// Ref
		if ($ref === True) {
			WayCheck ($way, $tag, "highway", "motorway", "ref", "Motrway ref", "Ref");
			WayCheck ($way, $tag, "highway", "trunk", "ref", "Trunk ref", "Ref");
			WayCheck ($way, $tag, "highway", "primary", "ref", "Primry ref ", "Ref");
			WayCheck ($way, $tag, "highway", "secondary", "ref", "Secndry ref", "Ref");
		}

		// Name
		if ($name === True) {
			WayCheck ($way, $tag, "highway", "residential", "name", "Resid name ", "Name");

			WayCheck ($way, $tag, "*", "cafe", "name", "Cafe name ", "Name");
			WayCheck ($way, $tag, "*", "restaurant", "name", "Restnt name", "Name");
			WayCheck ($way, $tag, "*", "pub", "name", "Pub name ", "Name");

			WayCheck ($way, $tag, "*", "school", "name", "School name", "Name");
			WayCheck ($way, $tag, "*", "college", "name", "Coll name ", "Name");
			WayCheck ($way, $tag, "*", "library", "name", "Liby name ", "Name");
			WayCheck ($way, $tag, "*", "university", "name", "Uni name ", "Name");

			WayCheck ($way, $tag, "shop", "*", "name", "Shop name ", "Name");
			WayCheck ($way, $tag, "*", "post_office", "name", "POffice nme", "Name");
			WayCheck ($way, $tag, "amenity", "cinema", "name", "Cinema name", "Name");
		}

		// Opening hours
		if ($hours === True) {
			WayCheck ($way, $tag, "*", "cafe", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "shop", "*", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "*", "post_office", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "*", "fast_food", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "*", "pharmacy", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "*", "restaurant", "opening_hours", "Open hours ", "OHours");
			WayCheck ($way, $tag, "*", "library", "opening_hours", "Open hours ", "OHours");
		}

		// Source
		if ($source === True) {
			NoTagCheck ($way, $tag, "source", "extrapolation", "way", "Src extrap", "Source");
			NoTagCheck ($way, $tag, "source", "NPE", "way", "Src NPE ", "SrcNPE");
			NoTagCheck ($way, $tag, "source", "historical", "way", "Src Hist ", "SrcHis");
			NoTagCheck ($way, $tag, "source", "historical", "way", "Src Hist ", "SrcHis");
			WayCheckOS ($way, $tag);
		}

		// FIXME etc
		if ($fixme === True)
			NoTagCheck ($way, $tag, "FIXME", "*", "way", "Fixme ", "Fixme");

		// Unknown road classification
		if ($road === True)
			NoTagCheck ($way, $tag, "highway", "road", "way", "Road type ", "Road");
	}
}

echo "</gpx>\n";
?>
