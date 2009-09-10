<?php
require ("inc_head.php");
require_once ("inc_head_html.php");

$id = (int) $_GET ["id"];
$dist = (float) $_GET ["dist"];
$search = $_COOKIE ["SearchType"];

//Delete old cache data
$db = sqlite_open ($db_file);
$old = strtotime ("-1 hour");
$sql = "DELETE FROM node_cache WHERE timestamp < $old";
logdebug ("Deleting old node cache. SQL:\n$sql");
sqlite_exec ($db, $sql);

//Check for cached data
$sql = "SELECT * FROM node_cache " .
	"WHERE nodeid LIKE $id";
logdebug ("Checking for cached node data. SQL:\n$sql");
$result = sqlite_query ($db, $sql);
if (sqlite_num_rows ($result) >= 1) {
	//Get cached data
	$row = sqlite_fetch_array ($result, SQLITE_ASSOC);
	$cached_xml = stripslashes ($row ["data"]);
	$xml = simplexml_load_string ($cached_xml);
}
else {
	//Get data from OSM
	$url = "$osm_api_base/node/$id";
	$xml = simplexml_load_file ($url);
	if ($xml === False)
		death ("Error getting data from $url", "Could not get data from OpenStreetMap");
}
//Delete any existing data for this node, then cache current data
$sql = "DELETE FROM node_cache WHERE nodeid LIKE $id";
sqlite_exec ($db, $sql);
$sql = "INSERT INTO node_cache ('timestamp','nodeid','data') " .
	"VALUES (" . time () . ", '$id', '" . sqlite_escape_string ($xml->asXML ()) . "')";
logdebug ("Adding to node cache. SQL:\n$sql");
sqlite_exec ($db, $sql);
sqlite_close ($db);

$ph = array ();
node_parse ($xml->node [0], $search, $ph);

$sname = stripslashes ($ph ["name"]);
$soperator = stripslashes ($ph ["operator"]);
if ($sname != "" && $soperator != "")
	$displayname = "$sname ($soperator)";
elseif ($sname == "" && $soperator == "")
	$displayname = "[No name]";
else
	$displayname = "$sname$soperator";
echo "<p><b>$displayname</b></p>\n";

if ($_GET ['edit'] == "yes")
	echo "<p><i>Details edited. Note that it may take some time for the new details to be displayed in all views.</i></p>";

echo "<p>$dist miles away\n";
//Only display map link in JS-capable browsers
$sMap = "\n<script type='text/javascript'>\n<!--\n";
$mapurl = "http://www.openstreetmap.org/?mlat={$ph ['lat']}&mlon={$ph ['lon']}&zoom=17";
$sMap .= "document.write (\" (<a href = '$mapurl' title = 'map of $postcode'>map</a>)\")";
$sMap .= "\n// -->\n";
$sMap .= "</script>\n<br>\n";
echo $sMap;

if ($ph ['dispensing'] != '')
	echo "Dispensing: {$ph ['dispensing']}</p>\n";

if ($ph ['addr_housename'] != '')
	echo $ph ['addr_housename'] . "<br>\n";
if ($ph ['addr_street'] != '') {
	if ($ph ['addr_housenumber'] != '')
		echo $ph ['addr_housenumber'] . " ";
	echo $ph ['addr_street'] . "<br>\n";
}
if ($ph ['addr_city'] != '')
	echo $ph ['addr_city'] . "<br>\n";
if ($ph ['addr_postcode'] != '')
	echo $ph ['addr_postcode'] . "<br>\n";

if ($ph ['phone'] != '')
	echo "<p>" . $ph ['phone'] . "</p>\n";
if ($ph ['hours'] != '') {
	$hours = str_replace (array ("mo", "tu", "we", "th", "fr", "sa", "su", ";"), array ("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su", "<br>"), $ph ['hours']);
	echo "<p>Opening Hours:<br>$hours</p>\n";
}
if ($ph ['description'] != '')
	echo "<p>" . $ph ['description'] . "</p>\n";
if ($ph ['url'] != '') {
	echo "<p><a href = '";
	if (substr ($ph ['url'], 0, 7) != 'http://')
		echo "http://";
	echo $ph ['url'] . "'>Website</a></p>\n";
}
echo "<p class = 'small'>Edit <a href = 'edit_hours.php?id=$id&amp;dist=$dist&amp;name=$displayname'>opening hours</a> / ";
echo "<a href = 'edit_addr.php?id=$id&amp;dist=$dist&amp;name=$displayname'>address</a> / ";
echo "<a href = 'edit.php?id=$id&amp;dist=$dist&amp;name=$displayname'>other details</a></p>\n";

echo "<p><a href = '" . $_COOKIE ["ResultsPage"] . "'>Back to results</a><br><a href = 'index.php'>New search</a></p>\n";
require ("inc_foot.php");
?>
