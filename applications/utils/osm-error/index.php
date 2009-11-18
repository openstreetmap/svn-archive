<html>
<head>
<style>
body {
	font-family: sans-serif;
}
input.text {
	font-family: sans-serif;
	width: 10ex;
}
td {
	text-align: center;
}
</style>
<title>OSM-Error</title>
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
Enter a set of co-ordinates, then click Download. This will download a GPX file with a waypoint for each error found.<br>
</p>

<form action = "error.php" method = "get">
<table>
<tr>
<td><input name = "top" value = "53.4355" class = "text" onchange = "CreateDownloadLink ()" id = "top"></td>
</tr>
<tr>
<td><input name = "left" value = "-1.2293" class = "text" onchange = "CreateDownloadLink ()" id = "left">
<input name = "right" value = "-1.1845" class = "text" onchange = "CreateDownloadLink ()" id = "right"></td>
</tr>
<tr>
<td><input name = "bottom" value = "53.4137" class = "text" onchange = "CreateDownloadLink ()" id = "bottom"></td>
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
<li>To Do: Add descriptions to waypoints
<li>To Do: Allow user to choose which things to check for
<li>To Do: Store co-ordinates in cookies
</ul>

<p><hr>
<a href = "../download/osm-error.tar.gz">Download OSM-Error v0.1</a> source code (released under an <a href = "http://www.opensource.org/licenses/mit-license.php">MIT Licence</a>)<br>
Back to <a href = "http://www.mappage.org/">mappage.org</a>
</p>

</body>
</html>
