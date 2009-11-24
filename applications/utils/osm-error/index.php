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
<style>
body {
	font-family: sans-serif;
}
input.text {
	font-family: sans-serif;
	width: 12ex;
}
td {
	text-align: center;
}
</style>
<title>OSM Error</title>
<script>
function select (state) {
	var oElements = document.getElementById ("frmError")
	for (var i = 0; i < oElements.length; i++)
		if (oElements.elements [i].type == "checkbox")
			oElements.elements [i].checked = state
}
</script>
</head>
<body>

<h1>OSM Error</h1>

<p>
Enter a set of co-ordinates, tick the issues you would like highlighted, and click Download. This will download a GPX file with a waypoint for each error found. The GPX file can then be loaded onto a GPS, to make it easier to find the issues that need attention when you are next out mapping.<br>
</p>

<form action = "error.php" method = "get" id = "frmError">
<table>
<tr>
<td><input name = "top" value = "<?=$top;?>" class = "text" onchange = "CreateDownloadLink ()" id = "top"></td>
</tr>
<tr>
<td><input name = "left" value = "<?=$left;?>" class = "text" onchange = "CreateDownloadLink ()" id = "left">
<input name = "right" value = "<?=$right;?>" class = "text" onchange = "CreateDownloadLink ()" id = "right"></td>
</tr>
<tr>
<td><input name = "bottom" value = "<?=$bottom;?>" class = "text" onchange = "CreateDownloadLink ()" id = "bottom"></td>
</tr>
</table>

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
<small><a href = "#" onclick = "select (true)">select all</a> : <a href = "#" onclick = "select (false)">select none</a></small>

<p><input type = "submit" value = "Download"></p>

<h2>Notes</h2>
<ul>
<li>Each waypoint name has a number prefix, to ensure that the name is unique
<script>
document.write ('<li>The download link can be bookmarked, or used with tools like <a href = "http://www.gnu.org/software/wget/">wget</a>')
</script>
<li>Some GPS units may truncate the waypoint names
<li>If an error is found on a way, the waypoint will be positioned at the first node in the way
</ul>

<h2>To Do</h2>
<ul>
<li>Add descriptions to waypoints
<li><s>Allow user to choose which things to check for</s>
<li><s>Store co-ordinates in cookies</s>
<li>Use a map to choose area (in similar manner to the export tab on the main OSM web site)
</ul>

<p><hr>
<a href = "../download/osm-error.tar.gz">Download source code</a> (released under an <a href = "http://www.opensource.org/licenses/mit-license.php">MIT Licence</a>)<br>
Back to <a href = "http://www.mappage.org/">mappage.org</a>
</p>

</body>
</html>
