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

<html>
<head>
<link rel = "stylesheet" type = "text/css" media = "all" href = "style.css" />
<title>OSM Error</title>
</head>
</body>

<h1>OSM Error: Notes</h1>
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
<li><s>Use a map to choose area (in similar manner to the export tab on the main OSM web site)</s>
</ul>

<h2>Source Code</h2>
<p>
<a href = "../download/osm-error.tar.gz">Download source code</a> (released under an <a href = "http://www.opensource.org/licenses/mit-license.php">MIT Licence</a>)
</p>

<p>
<a href = "index.php">OSM Error main page</a><br>
Back to <a href = "http://www.mappage.org/">mappage.org</a>
</p>
</head>
<body>
