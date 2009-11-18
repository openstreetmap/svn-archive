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
function CreateDownloadLink () {
	sLink = "error.php?left=" + document.getElementById ("left").value
	sLink += "&amp;bottom=" + document.getElementById ("bottom").value
	sLink += "&amp;right=" + document.getElementById ("right").value
	sLink += "&amp;top=" + document.getElementById ("top").value
	document.getElementById ("download").innerHTML = "<a href = '" + sLink + "'>Download</a>"

	SetCookies ()
}
</script>

</head>
<body onload = "CreateDownloadLink ()">

<h1>OSM Error</h1>

<p>
Enter a set of co-ordinates, then click Download. This will download a GPX file with a waypoint for each error found. The GPX file can then be loaded onto a GPS, to make it easier to find the issues that need attention when you are next out mapping.<br>
</p>

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
?>

<form action = "error.php" method = "get">
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
<tr>
<td><span id = "download">
<noscript>
<input type = "submit" value = "Download">
</noscript>
</span></td>
</tr>
</table>

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
<li>Allow user to choose which things to check for
<li><s>Store co-ordinates in cookies</s>
<li>Use a map to choose area (in similar manner to the export tab on the main OSM web site)
</ul>

<p><hr>
<a href = "../download/osm-error.tar.gz">Download source code</a> (released under an <a href = "http://www.opensource.org/licenses/mit-license.php">MIT Licence</a>)<br>
Back to <a href = "http://www.mappage.org/">mappage.org</a>
</p>

</body>
</html>
