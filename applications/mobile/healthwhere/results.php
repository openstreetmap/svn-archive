<?php
//Get start time for log
$iStartTime = time ();
//Store search terms in cookies
if ($_GET ["SearchType"] == "postcode")
	setcookie ("Postcode", trim ($_GET ["txtPostcode"]));
if ($_GET ["txtLatitude"] != "")
	setcookie ("Latitude", (float) $_GET ["txtLatitude"]);
if ($_GET ["txtLongitude"] != "")
	setcookie ("Longitude", (float) $_GET ["txtLongitude"]);
setcookie ("Distance", (float) $_GET ["txtDistance"]);
setcookie ("ResultsPage", $_SERVER ["REQUEST_URI"]);
setcookie ("HourOffset", $_GET ["selHourOffset"]);

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

if ($_GET ["txtPostcode"] != "") {
	$pcOuter = "";
	$pcInner = "";
	//Set $user_lat and $user_lon to invalid values
	$user_lat = 400;
	$user_lon = 400;
	$db = sqlite_open ($db_file);
	//Parse postcode
	$pc = strtolower (trim ($_GET ["txtPostcode"]));
	$asPC = explode (" ", $pc);
	if (count ($asPC == 2)) {
		//$pc includes a space, so we now have inner & outer
		$pcOuter = $asPC [0];
		$pcInner = $asPC [1];
	}
	else {
		//$pc does not include a space. Have to best guess inner & outer
		//If last three characters are digit & 2 letters, then they are inner
		//If not, there is no inner, just an outer (eg S1)
		if (ereg ('([a-z][0-9]?[0-9]?)([0-9][a-z][a-z])', $pc, $regs) !== False) {
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

	if ($user_lat == 400) {
		// Get lat/lon from Ernest Marples
		$csv = file_get_contents ("http://ernestmarples.com/?p=" .
			urlencode ($pc) . "&f=csv");
		$latlon = explode (",", $csv);
		if (strtolower ($latlon [0]) == $pc) {
			//Successfully got co-ordinates. Parse CSV
			$user_position = $latlon [0];
			//casting to floats strips newlines
			$user_lat = (float) $latlon [1];
			$user_lon = (float) $latlon [2];
			//Add to local DB
			$sql = "INSERT INTO postcodes ('outward','inward','lat','lon','source') " .
				"VALUES ('$pcOuter','$pcInner',$user_lat,$user_lon,'Ernest Marples')";
			if (sqlite_exec ($db, $sql))
				logdebug ("Saved Postcode $pcOuter $pcInner from Earnest Marples in local DB");
			else
				logdebug ("Error saving postcode $pcOuter $pcInner in local DB");
		}
		else {
			//Unable to get postcode from Ernest Marples or local DB. Fail back to sector
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

//Get data
$url = "$osm_xapi_base/node[amenity|healthcare=pharmacy][bbox=$left,$bottom,$right,$top]";
$xml = simplexml_load_file ($url);
if ($xml === False)
	death ("Error getting data from $url", "Could not get data from OpenStreetMap");
//counters for log
$iCountFound = 0;
$iCountTimes = 0;

//Add pharmacies to an array
$asPharmacies = array ();
foreach ($xml->node as $node) {
	//Get latitude & longitude
	$phlat = (float) $node ["lat"];
	$phlon = (float) $node ["lon"];

	$dist = round (distance ($user_lat, $user_lon, $phlat, $phlon, True), 1);
	if ($dist <= $maxdist) {
		//Parse node to get details
		$ph = array ();
		node_parse ($node, $ph);

		//Get ID
		$id = $node ["id"];

		//If $dist is a round number, add a ".0" suffix
		if ($dist == round ($dist, 0))
			$dist = "$dist.0";
		//pad $dist with leading zeroes for sorting
		$sorting = str_pad ($dist, 5, "0", STR_PAD_LEFT);
		//Prefix $sorting with 0 (open) or 1 (closed/don't know)
		if (OpenClosed ($ph ["hours"]))
			$sorting = "0$sorting";
		else
			$sorting = "1$sorting";
		//Add $sorting as a comment, so array can be sorted by distance
		$sPharmacy = "<!-- $sorting -->";
		$url = "detail.php?id=$id&amp;dist=$dist";
		$sPharmacy .= "<a href = '$url' title = 'Details'>";
		$sname = stripslashes ($ph ["name"]);
		$soperator = stripslashes ($ph ["operator"]);

		if ($sname != "" && $soperator != "")
			$sPharmacy .= "$sname</a> ($soperator) ($dist)<br>";
		elseif ($sname == "" && $soperator == "")
			$sPharmacy .= "[No name]</a> ($dist)<br>";
		else
			$sPharmacy .= "$sname$soperator</a> ($dist)<br>";
		if ($ph ["addr_housename"] != "")
			$sPharmacy .= "{$ph ['addr_housename']}<br>\n";
		if ($ph ["addr_street"] != "") {
			if ($ph ["addr_housenumber"] != "")
				$sPharmacy .= "{$ph ['addr_housenumber']} ";
			$sPharmacy .= "{$ph ['addr_street']}<br>\n";
		}
		if ($ph ["phone"] != "")
			$sPharmacy .= "{$ph ['phone']}<br>\n";
		// Increment counters
		if ($ph ["hours"] != "")
			$iCountTimes++;
		$iCountFound++;
		$asPharmacies [] = $sPharmacy;
	}
}

//Write results to log
if ($_GET ["txtPostcode"] != "")
	$SearchTerm = "Postcode,{$_GET ["txtPostcode"]},,";
else
	$SearchTerm = "Lat/Lon,{$_GET ['txtLatitude']},{$_GET ['txtLongitude']},";
$SearchTerm .= "{$_GET ['txtDistance']},";
$iTime = time () - $iStartTime;
$log_string = date ("Y-m-d,H:i:s,") . $SearchTerm . count ($asPharmacies) . "," . $iTime;
file_put_contents ($access_log, "$log_string\n", FILE_APPEND);

require_once ("inc_head_html.php");

echo "<p>" . count ($asPharmacies) . " found within $maxdist miles";
if ($_GET ["txtPostcode"] != "") {
	$postcode = htmlentities ($_GET ["txtPostcode"]);
	//Only display map link in JS-capable browsers
	$sMap = "\n<script type='text/javascript'>\n<!--\n";
	$sMap .= "document.write (\" of <a href = 'http://www.openstreetmap.org/?mlat=$user_lat&mlon=$user_lon&zoom=17' title = 'map of $postcode'>$postcode</a>\")";
	$sMap .= "\n// -->\n";
	$sMap .= "</script>\n";
	$sMap .= "<noscript>\n of $postcode\n</noscript>\n";
	echo $sMap;
}
echo "<br><a href = 'index.php'>Search again</a><br>\n";
echo "Click on a name to see details</p>\n";

// Sort array
sort ($asPharmacies);
echo "<p><b>Open</b></p>\n";
foreach ($asPharmacies as $sPharm)
	if (substr ($sPharm, 0, 6) == "<!-- 0")
		echo "<p>$sPharm</p>\n";

echo "<p><b>Closed or Unknown</b></p>\n";
foreach ($asPharmacies as $sPharm)
	if (substr ($sPharm, 0, 6) == "<!-- 1")
		echo "<p>$sPharm</p>\n";

echo "<p><a href = 'index.php'>Search again</a></p>\n";

require ("inc_foot.php");
?>
