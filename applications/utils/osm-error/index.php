<!--
Code to get co-ordinates from map taken from http://maposmatic.org/ and
copyright (c) 2009 Étienne Loks <etienne.loks_AT_peacefrogsDOTnet>
Other code copyright (c) Russ Phillips <russ AT phillipsuk DOT org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->

<?php
// Get saved values from cookies if present
if (isset ($_COOKIE ['left']))
	$left = $_COOKIE ['left'];
else
	$left = "";
if (isset ($_COOKIE ['bottom']))
	$bottom = $_COOKIE ['bottom'];
else
	$bottom = "";
if (isset ($_COOKIE ['right']))
	$right = $_COOKIE ['right'];
else
	$right = "";
if (isset ($_COOKIE ['top']))
	$top = $_COOKIE ['top'];
else
	$top = "";

if (isset ($_COOKIE ['ref']))
	$ref = " checked";
else
	$ref = "";
if (isset ($_COOKIE ['name']))
	$name = " checked";
else
	$name = "";
if (isset ($_COOKIE ['hours']))
	$hours = " checked";
else
	$hours = "";
if (isset ($_COOKIE ['source']))
	$source = " checked";
else
	$source = "";
if (isset ($_COOKIE ['fixme']))
	$fixme = " checked";
else
	$fixme = "";
if (isset ($_COOKIE ['naptan']))
	$naptan = " checked";
else
	$naptan = "";
if (isset ($_COOKIE ['road']))
	$road = " checked";
else
	$road = "";
if (isset ($_COOKIE ['pbref']))
	$pbref = " checked";
else
	$pbref = "";
?>
<html>
<head>
<link rel = "stylesheet" type = "text/css" media = "all" href = "style.css" />
<title>OSM Error</title>
<script>
function select (state) {
	var oElements = document.getElementById ("frmError")
	for (var i = 0; i < oElements.length; i++)
		if (oElements.elements [i].type == "checkbox")
			oElements.elements [i].checked = state
}
</script>
<!-- OpenLayers javascript library -->
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<!-- OpenStreetMap OpenLayers layers -->
<script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"></script>
<script src = "map.js"></script>
</head>
<body onload = "init ()">

<div style="width:49%; height:100%; float: left;" id="text">

<h1>OSM Error</h1>

<p>
Enter a set of co-ordinates, tick the issues you would like highlighted, and click Download. This will download a GPX file with a waypoint for each error found. The GPX file can then be loaded onto a GPS, to make it easier to find the issues that need attention when you are next out mapping.<br>
</p>

<form action = "error.php" method = "get" id = "frmError">
<p class = "mid">
<input name = "lat_upper_left" value = "<?=$top;?>" class = "text" id = "lat_upper_left"><br>
<input name = "lon_upper_left" value = "<?=$left;?>" class = "text" id = "lon_upper_left">
<input name = "lon_bottom_right" value = "<?=$right;?>" class = "text" id = "lon_bottom_right"><br>
<input name = "lat_bottom_right" value = "<?=$bottom;?>" class = "text" id = "lat_bottom_right">
</p>

<p>A waypoint will be created for each of the following that is found:</p>
<p>
<input type = "checkbox" name = "ref" id = "ref" <?=$ref?>>&nbsp;<label for = "ref">Motorways, trunk, primary and secondary roads without a &quot;ref&quot; tag</label><br>
<input type = "checkbox" name = "name" id = "name" <?=$name?>>&nbsp;<label for = "name">Various things without names</label><br>
<input type = "checkbox" name = "hours" id = "hours" <?=$hours?>>&nbsp;<label for = "hours">Shops etc without opening hours</label><br>
<input type = "checkbox" name = "source" id = "source" <?=$source?>>&nbsp;<label for = "source">Anything with &quot;source&quot; set to &quot;extrapolation&quot;, &quot;NPE&quot; or &quot;historical&quot;</label><br>
<input type = "checkbox" name = "fixme" id = "fixme" <?=$fixme?>>&nbsp;<label for = "fixme">Anything with a &quot;FIXME&quot; tag</label><br>
<input type = "checkbox" name = "naptan" id = "naptan" <?=$naptan?>>&nbsp;<label for = "naptan">Any node tagged with &quot;naptan:verified=no&quot;</label><br>
<input type = "checkbox" name = "road" id = "road" <?=$road?>>&nbsp;<label for = "road">Any way tagged with &quot;highway=road&quot;</label><br>
<input type = "checkbox" name = "pbref" id = "pbref" <?=$pbref?>>&nbsp;<label for = "ref">Postboxes without a &quot;ref&quot; tag</label><br>
</p>
<p>
<small><a href = "#" onclick = "select (true)">select all</a> : <a href = "#" onclick = "select (false)">select none</a></small>
</p>
<p class = "mid">
<input type = "submit" value = "Download">
</p>

<p>
<a href = "notes.php">Notes, source code, etc</a><br>
Back to <a href = "http://www.mappage.org/">mappage.org</a>
</p>
</div>

<div style="width:49%; height:100%; float: right;" id="map"></div>

</body>
</html>
