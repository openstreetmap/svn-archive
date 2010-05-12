<?php
/*
Healthwhere, a web service to find local pharmacies and hospitals
Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

require ("inc_head.php");
require_once ("inc_head_html.php");

$id = (int) $_GET ["id"];
$dist = (float) $_GET ["dist"];
$search = $_COOKIE ["SearchType"];
$waynode = $_GET ['waynode'];

//Get data from OSM
if ($waynode == 'node')
	$url = "$osm_api_base/node/$id";
else
	$url = "$osm_api_base/way/$id";
$xml = simplexml_load_file ($url);
if ($xml === False)
	death ("Error getting data from $url", "Could not get data from OpenStreetMap");

$ph = array ();
if ($waynode == 'node')
	node_parse ($xml->node [0], $search, $ph);
else
	node_parse ($xml->way [0], $search, $ph);

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

if ($ph ['dispensing'] != '') {
	$dispensing = strtolower ($ph ['dispensing']);
	if ($dispensing == "yes" || $dispensing == "true" || $dispensing == "1")
		echo "<p>Dispensing: Yes</p>\n";
	elseif ($dispensing == "no" || $dispensing == "false" || $dispensing == "0")
		echo "<p>Dispensing: No</p>\n";
}

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

if ($waynode == 'node') {
	echo "<p class = 'small'>Edit <a href = 'edit_hours.php?id=$id&amp;dist=$dist&amp;name=" .
		urlencode ($displayname) . "&amp;waynode=$waynode'>opening hours</a> / ";
	echo "<a href = 'edit_addr.php?id=$id&amp;dist=$dist&amp;name=" .
		urlencode ($displayname) . "&amp;waynode=$waynode'>address</a> / ";
	echo "<a href = 'edit.php?id=$id&amp;dist=$dist&amp;name=" .
		urlencode ($displayname) . "&amp;waynode=$waynode'>other details</a></p>\n";
}

echo "<p><a href = '" . $_COOKIE ["ResultsPage"] . "'>Back to results</a><br><a href = 'index.php'>New search</a></p>\n";
require ("inc_foot.php");
?>
