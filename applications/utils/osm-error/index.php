<!--
Code to get co-ordinates from map taken from http://maposmatic.org/ and
copyright (c) 2009 Étienne Loks <etienne.loks_AT_peacefrogsDOTnet>
Other code copyright (c) Russ Phillips <russ AT phillipsuk DOT org>

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
if (isset ($_COOKIE ['namelen']))
	$namelen = (int) $_COOKIE ['namelen'];
else
	$namelen = 14;
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<meta http-equiv = "Content-Type" content = "text/html;charset=utf-8">
<link rel = "stylesheet" type = "text/css" media = "all" href = "style.css">
<title>OSM Error</title>
<script type = "text/javascript">
function select (state) {
	var oElements = document.getElementById ("frmError")
	for (var i = 0; i < oElements.length; i++)
		if (oElements.elements [i].type == "checkbox")
			oElements.elements [i].checked = state
	updateForm ()
}
</script>
<!-- OpenLayers javascript library -->
<script src = "http://www.openlayers.org/api/OpenLayers.js" type = "text/javascript"></script>
<!-- OpenStreetMap OpenLayers layers -->
<script src = "http://www.openstreetmap.org/openlayers/OpenStreetMap.js" type = "text/javascript"></script>
<script src = "map.js" type = "text/javascript"></script>
</head>
<body onload = "init ()">

<div style = "width: 49%; height: 100%; float: left;" id = "text">

<h1>OSM Error</h1>

<p>
Download a GPX file with a waypoint for each error found. The GPX file can then be loaded onto a GPS, to make it easier to find the issues that need attention when you are next out mapping.<br>
</p>

<form action = "error.php" method = "get" id = "frmError">
<p class = "mid">
<input name = "lat_upper_left" value = "<?=$top;?>" class = "text" id = "lat_upper_left" onchange = "updateMap()"><br>
<input name = "lon_upper_left" value = "<?=$left;?>" class = "text" id = "lon_upper_left" onchange = "updateMap()">
<input name = "lon_bottom_right" value = "<?=$right;?>" class = "text" id = "lon_bottom_right" onchange = "updateMap()"><br>
<input name = "lat_bottom_right" value = "<?=$bottom;?>" class = "text" id = "lat_bottom_right" onchange = "updateMap()">
</p>

<p>A waypoint will be created for each of the following that is found:</p>
<p>
<input type = "checkbox" onchange = "updateForm ()" name = "ref" id = "ref" <?=$ref?>>&nbsp;<label for = "ref"><a href = "http://wiki.openstreetmap.org/wiki/Tag:highway%3Dmotorway">Motorways</a>, <a href = "http://wiki.openstreetmap.org/wiki/Tag:highway%3Dtrunk">trunk</a>, <a href = "http://wiki.openstreetmap.org/wiki/Tag:highway%3Dprimary">primary</a> and <a href = "http://wiki.openstreetmap.org/wiki/Tag:highway%3Dsecondary">secondary</a> roads without a &quot;<a href = "http://wiki.openstreetmap.org/wiki/Key:ref">ref</a>&quot; tag</label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "road" id = "road" <?=$road?>>&nbsp;<label for = "road">Any way tagged with &quot;<a href = "http://wiki.openstreetmap.org/wiki/Tag:highway%3Droad">highway=road</a>&quot;</label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "name" id = "name" <?=$name?>>&nbsp;<label for = "name">Various things without <a href = "http://wiki.openstreetmap.org/wiki/Key:name">names</a></label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "hours" id = "hours" <?=$hours?>>&nbsp;<label for = "hours">Shops etc without <a href = "http://wiki.openstreetmap.org/wiki/Key:opening_hours">opening hours</a></label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "source" id = "source" <?=$source?>>&nbsp;
<label for = "source">Anything with &quot;<a href = "http://wiki.openstreetmap.org/wiki/Key:source">source</a>&quot; set to
&quot;extrapolation&quot;,
&quot;<a href = "http://wiki.openstreetmap.org/wiki/NPE">NPE</a>&quot;,
&quot;<a href = "http://wiki.openstreetmap.org/wiki/Ordnance_Survey_Opendata#Attributing_OS">OS_OpenData StreetView</a>&quot;
or
&quot;historical&quot;</label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "fixme" id = "fixme" <?=$fixme?>>&nbsp;<label for = "fixme">Anything with a &quot;<a href = "http://wiki.openstreetmap.org/wiki/Key:fixme">fixme</a>&quot; tag</label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "naptan" id = "naptan" <?=$naptan?>>&nbsp;<label for = "naptan">Any node tagged with &quot;<a href = "http://wiki.openstreetmap.org/wiki/NaPTAN/Surveying_and_Merging_NaPTAN_and_OSM_data">naptan:verified=no</a>&quot;</label><br>
<input type = "checkbox" onchange = "updateForm ()" name = "pbref" id = "pbref" <?=$pbref?>>&nbsp;<label for = "ref"><a href = "http://wiki.openstreetmap.org/wiki/Tag:amenity%3Dpost_box" onchange = "updateForm ()">Postboxes</a> without a &quot;ref&quot; tag</label><br>
</p>
<p>
<small><a href = "#" onclick = "select (true)">select all</a> : <a href = "#" onclick = "select (false)">select none</a></small>
</p>

<p>Length of waypoint names:</p>
<p>
<?
if ($namelen == 6)
	$checked = "checked";
else
	$checked = "";
echo "<input type = 'radio' $checked name = 'namelen' value = '6' id = 'name6'>&nbsp;<label for = 'name6'>Limit waypoint name to 6 characters</label><br>\n";
if ($namelen == 14)
	$checked = "checked";
else
	$checked = "";
echo "<input type = 'radio' $checked name = 'namelen' value = '14' id = 'name14'>&nbsp;<label for = 'name14'>Limit waypoint name to 14 characters</label><br>\n";
if ($namelen == 0)
	$checked = "checked";
else
	$checked = "";
echo "<input type = 'radio' $checked name = 'namelen' value = '0' id = 'name0'>&nbsp;<label for = 'name0'>Do not limit waypoint name length</label><br>\n";
?>
</p>
<p class = "mid">
<input type = "submit" value = "Download" id = "btnSubmit">
<span id = "spLink"></span>
</p>
</form>

<h2>Notes</h2>
<ul>
<li>Each waypoint name has a number suffix, to ensure that the name is unique
<li>The download link can be bookmarked, or used with tools like <a href = "http://www.gnu.org/software/wget/">wget</a>
<li>If an error is found on a way, the waypoint will be positioned at the first node in the way
<li><a href = "../download/osm-error.tar.gz">Download source code</a> (released under <a href = "gpl.txt">GNU General Public License</a>)
<li><a href = "README.html">About OSM Error</a>
</ul>

<p>
<a href = "http://www.mappage.org/">mappage.org</a>
</p>
</div>

<div style = "width: 49%; height: 50em; border: thin solid black; float: right;" id = "map"></div>

</body>
</html>
