<?php
//Get start time for log
$iStartTime = time ();
//Store search terms in cookies
if ($_GET ["txtPostcode"] != "") {
	$txtPostcode = trim ($_GET ["txtPostcode"]);
	setcookie ("Postcode", $txtPostcode);
}
if ($_GET ["txtLatitude"] != "")
	setcookie ("Latitude", (float) $_GET ["txtLatitude"]);
if ($_GET ["txtLongitude"] != "")
	setcookie ("Longitude", (float) $_GET ["txtLongitude"]);
setcookie ("Distance", (float) $_GET ["txtDistance"]);
setcookie ("ResultsPage", $_SERVER ["REQUEST_URI"]);
setcookie ("HourOffset", $_GET ["selHourOffset"]);

if ($_GET ["btnSubmit"] == "Find pharmacies")
	$search = "pharmacy";
else
	$search = "hospital";
setcookie ("SearchType", $search);

require ("inc_head.php");
require ("inc_openclosed.php");

/*
 * http://snipplr.com/view/2531/calculate-the-distance-between-two-coordinates-latitude-longitude
*/
function distance ($lat1, $lng1, $lat2, $lng2, $miles = true) {
	$pi80 = M_PI / 180;
	$lat1 *= $pi80;
	$lng1 *= $pi80;
	$lat2 *= $pi80;
	$lng2 *= $pi80;

	$r = 6372.797; // mean radius of Earth in km
	$dlat = $lat2 - $lat1;
	$dlng = $lng2 - $lng1;
	$a = sin($dlat / 2) * sin($dlat / 2) + cos($lat1) * cos($lat2) * sin($dlng / 2) * sin($dlng / 2);
	$c = 2 * atan2(sqrt($a), sqrt(1 - $a));
	$km = $r * $c;

	return ($miles ? ($km * 0.621371192) : $km);
}

//Add pharmacies/hospitals to an array
function AddPharmHospital (&$asPharmacies, &$ph, &$node, $user_lat, $user_lon, $phlat, $phlon, $waynode) {
	global $maxdist, $search;
	$dist = round (distance ($user_lat, $user_lon, $phlat, $phlon, True), 1);

	if ($dist <= $maxdist) {
		//Parse node to get details
		$ph = array ();
		node_parse ($node, $search, $ph);

		//Get ID
		$id = $node ["id"];

		//If $dist is a round number, add a ".0" suffix
		if ($dist == round ($dist, 0))
			$dist = "$dist.0";
		//pad $dist with leading zeroes for sorting
		$sorting = str_pad ($dist, 5, "0", STR_PAD_LEFT);
		//Prefix $sorting with 0 (open) or 1 (closed/don't know)
		$bOpen = OpenClosed ($ph ["hours"]);
		if ($bOpen === True)
			$sorting = "0$sorting";
		else
			$sorting = "1$sorting";
		//Add $sorting as a comment, so array can be sorted by distance
		$sPharmacy = "<!-- $sorting -->";
		$url = "detail.php?id=$id&amp;dist=$dist&amp;waynode=$waynode";
		$sPharmacy .= "<a href = '$url' title = 'Details'>";

		//Get details
		$sname = stripslashes ($ph ["name"]);
		$soperator = stripslashes ($ph ["operator"]);

		if ($sname != "" && $soperator != "")
			$sPharmacy .= "$sname</a> ($soperator)<br>";
		elseif ($sname == "" && $soperator == "")
			$sPharmacy .= "[No name]</a><br>";
		else
			$sPharmacy .= "$sname$soperator</a><br>";
		if ($bOpen === True)
			$sPharmacy .= "<b>Open</b>, ";
		elseif ($bOpen === False)
			$sPharmacy .= "<b>Closed</b>, ";
		$sPharmacy .= "$dist miles away<br>";
		if ($search == "hospital" && $ph ["emergency"] != "")
			$sPharmacy .= "Emergency: {$ph ["emergency"]}<br>";
		if ($ph ["phone"] != "")
			$sPharmacy .= "{$ph ['phone']}<br>\n";
		// Increment counters
		if ($ph ["hours"] != "")
			$iCountTimes++;
		$iCountFound++;
		$asPharmacies [] = $sPharmacy;
	}
}

if ($txtPostcode == "" && ($_GET ["txtLatitude"] == "" || $_GET ["txtLongitude"] == "")) {
	echo "<p>You must enter a postcode or a latitude <i>and</i> longitude</p>\n";
	echo "<p><a href = 'index.php'>Back</a></p>\n";
	require ("inc_foot.php");
	exit;
}

if ($txtPostcode != "") {
	$pcOuter = "";
	$pcInner = "";
	//Set $user_lat and $user_lon to invalid values
	$user_lat = 400;
	$user_lon = 400;
	$db = sqlite_open ($db_file);
	//Parse postcode
	$pc = strtolower ($txtPostcode);
	$asPC = explode (" ", $pc);
	if (count ($asPC) == 2) {
		//$pc includes a space, so we now have inner & outer
		$pcOuter = $asPC [0];
		$pcInner = $asPC [1];
	}
	else {
		//$pc does not include a space. Have to best guess inner & outer
		//If last three characters are digit & 2 letters, then they are inner
		//If not, there is no inner, just an outer (eg S1)
		if (ereg ('([a-z][a-z]?[0-9][0-9]?)([0-9][a-z][a-z])', $pc, $regs) !== False) {
			$pcOuter = $regs [1];
			$pcInner = $regs [2];
		}
		else {
			$pcOuter = $pc;
			$pcInner = "";
		}
	}
	//Check for lat/lon in local DB
	$sql = "SELECT * FROM postcodes WHERE outward LIKE '$pcOuter' AND inward LIKE '$pcInner'";
	$result = sqlite_query ($db, $sql, SQLITE_ASSOC);
	if (sqlite_num_rows ($result) > 0) {
		$row = sqlite_fetch_array ($result, SQLITE_ASSOC);
		$user_lat = $row ['lat'];
		$user_lon = $row ['lon'];
		logdebug ("Fetched postcode $pcOuter $pcInner from local DB. Source: {$row ['source']}");
	}
	else {
		//Unable to get full postcode from local DB. Fail back to sector
		$sector = substr ($pcInner, 0, 1);
		$sql = "SELECT avg(lat) as avglat, avg(lon) as avglon FROM postcodes " .
			"WHERE outward LIKE '$pcOuter' AND inward LIKE '$sector%'";
		$result = sqlite_query ($db, $sql, SQLITE_ASSOC);
		if (sqlite_num_rows ($result) > 0) {
			$row = sqlite_fetch_array ($result, SQLITE_ASSOC);
			$user_lat = $row ['avglat'];
			$user_lon = $row ['avglon'];
			logdebug ("Fetched postcode $pcOuter $sector from local DB (averaged)");
		}
		else {
			//Unable to get sector from local DB. Fall back to outer only
			$sql = "SELECT avg(lat) as avglat, avg(lon) as avglon FROM postcodes " .
				"WHERE outward LIKE '$pcOuter'";
			$result = sqlite_query ($db, $sql, SQLITE_ASSOC);
			if (sqlite_num_rows ($result) > 0) {
				$row = sqlite_fetch_array ($result, SQLITE_ASSOC);
				$user_lat = $row ['avglat'];
				$user_lon = $row ['avglon'];
				logdebug ("Fetched postcode $pcOuter from local DB (averaged)");
			}
			else
				death ("Could not get lat/lon for $pc",	"Unable to get position for postcode $pc.");
		}
	}
	sqlite_close ($db);
}
else {
	$user_lat = (float) $_GET ['txtLatitude'];
	$user_lon = (float) $_GET ['txtLongitude'];
}
$maxdist = (float) $_GET ['txtDistance'];

// Default maximum distance
if ($maxdist == 0)
	$maxdist = DEFAULT_MAX_DIST;
//Limit maximum distance
if ($maxdist > MAX_DIST)
	$maxdist = MAX_DIST;

// Limit search based on lat & lon
$bottom = $user_lat - ($maxdist / 35);
$top = $user_lat + ($maxdist / 35);
$left = $user_lon - ($maxdist / 35);
$right = $user_lon + ($maxdist / 35);

//Get data from OSM
$url = "$osm_xapi_base/node[amenity|healthcare=$search][bbox=$left,$bottom,$right,$top]";
$xml = simplexml_load_file ($url);
$url = "$osm_xapi_base/way[amenity|healthcare=$search][bbox=$left,$bottom,$right,$top]";
$wayxml = simplexml_load_file ($url);
if ($xml === False || $wayxml === False)
	death ("Error getting data from $url", "Could not get data from OpenStreetMap");

//counters for log
$iCountFound = 0;
$iCountTimes = 0;

//Add pharmacies/hospitals to an array
$asPharmacies = array ();
//Check nodes
foreach ($xml->node as $node) {
	//Get latitude & longitude
	$phlat = (float) $node ["lat"];
	$phlon = (float) $node ["lon"];

	AddPharmHospital ($asPharmacies, $ph, $node, $user_lat, $user_lon, $phlat, $phlon, "node");
}
//Check ways
foreach ($wayxml->way as $way) {
	//Get latitude & longitude

	// Number of nodes found in the way
	$iNodeCount = 0;
	// Variables to hold sum of latitudes & longitudes
	$fLatSum = 0;
	$fLonSum = 0;

	// Get nodes belonging to the way
	foreach ($way->nd as $ndref) {
		$nodeid = (int) $ndref ["ref"];
		// Loop through nodes in XML
		foreach ($wayxml->node as $node) {
			// If node is part of the way, get lat & lon
			if ((int) $node ["id"] == $nodeid) {
				$iNodeCount++;
				$fLatSum += (float) $node ["lat"];
				$fLonSum += (float) $node ["lon"];
			}
		}
	}

	// Get average lat/lon
	$phlat = $fLatSum / $iNodeCount;
	$phlon = $fLonSum / $iNodeCount;
	AddPharmHospital ($asPharmacies, $ph, $way, $user_lat, $user_lon, $phlat, $phlon, "way");
}

//Write results to log
if ($txtPostcode != "")
	$SearchTerm = "Postcode,{$txtPostcode},,";
else
	$SearchTerm = "Lat/Lon,{$_GET ['txtLatitude']},{$_GET ['txtLongitude']},";
$SearchTerm .= "{$_GET ['txtDistance']},";
$iTime = time () - $iStartTime;
$log_string = date ("Y-m-d,H:i:s,") . $SearchTerm . count ($asPharmacies) . "," . $iTime . ",$search";
file_put_contents ($access_log, "$log_string\n", FILE_APPEND);

require_once ("inc_head_html.php");

$numfound = count ($asPharmacies);
echo "<p>" . $numfound;
if ($numfound == 1 && $search == "pharmacy")
	echo " pharmacy";
elseif ($numfound != 1 && $search == "pharmacy")
	echo " pharmacies";
elseif ($numfound == 1 && $search == "hospital")
	echo " hospital";
else
	echo " hospitals";
echo " found within $maxdist miles";
if ($txtPostcode != "")
	$locationtext = strtoupper (htmlentities ($txtPostcode));
else
	$locationtext = "your location";

//Only display map link in JS-capable browsers
$sMap = "\n<script type='text/javascript'>\n<!--\n";
$sMap .= "document.write (\" of <a href = 'http://www.openstreetmap.org/?mlat=$user_lat&mlon=$user_lon&zoom=17' title = 'map of $locationtext'>$locationtext</a>\")";
$sMap .= "\n// -->\n";
$sMap .= "</script>\n";
$sMap .= "<noscript>\n of $locationtext\n</noscript>\n";
echo $sMap;

echo "<br><a href = 'index.php'>Search again</a><br>\n";
echo "Click on a name to see details</p>\n";

// Sort array
sort ($asPharmacies);
foreach ($asPharmacies as $sPharm)
	echo "<p>$sPharm</p>\n";

echo "<p><a href = 'index.php'>Search again</a></p>\n";

require ("inc_foot.php");
?>
