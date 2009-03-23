<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>OpenStreetMap routing service</title>
    <link rel="stylesheet" href="main.css">
    <script src="http://openlayers.org/api/OpenLayers.js" type="text/javascript"></script>
    <script src="config.js" type="text/javascript"></script>
    <script src="routing.js" type="text/javascript"></script>
    <script src="http://openstreetmap.org/openlayers/OpenStreetMap.js"></script>
  </head>
  <body onload="init();">
	<div id="header">
    	<center>
			<h1>OpenStreetMap routing service</h1>
			<div id="controls">
				<form name="route">
					<table>
						<tr><td>
							<!--<font color=red>Note: Routing in the Northeastern part of the American continent isn't possible due to server limitations.</font>-->
							<font color=red>Note: Namefinder service currently not available or very slow.</font>
						</td></tr>
					</table>
		      		<table>
						<tr>
							<td><input type="button" name="from" onclick="elementClick(this);" value="From:" tabindex=5></td>
							<td><input type="text" name="from_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex=1 onfocus="this.select()"></td>
							<td><input type="button" name="to" onclick="elementClick(this);" value="To:" tabindex=6></td>
							<td><input type="text" name="to_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex=2 onfocus="this.select()"></td>
							<td><input type="button" name="calculate" onclick="elementClick(this);" value="Find route" tabindex=3></td>
			        		<td><input type="button" name="clear" onclick="elementClick(this);" value="Clear" tabindex=4></td>
			        		<td><input type="button" name="reverse" onclick="reverseRoute(this);" value="Reverse" tabindex=7></td>
			        		<td><div id="status"></div></td>
						</tr>
					</table>
				</form>
			</div>
		</center>
	</div>

	<div id="main1">
		<div id="main2">
			<div id="navigation">
				<div class="menu-block">
					<p class="menu-header">Site menu</p>
					<ul>
						<li><a href="index.php">Home</a></li>
						<li><a href="help.html">Help</a></li>
						<li><a href="about.html">About</a></li>
					</ul>
				</div>
				<div class="menu-block">
					<form name="parameters">
						<p class="menu-header">Routing</p>
						<ul>
							<li><input type="radio" name="type" value="motorcar" checked>Car</a></li>
							<li><input type="radio" name="type" value="bicycle">Bicycle</a></li>
							<li><input type="radio" name="type" value="foot">Foot</a></li>
						</ul>
						<ul>
							<li><input type="radio" name="method" value="fast" checked>Fastest</a></li>
							<li><input type="radio" name="method" value="short">Shortest</a></li>
						</ul>
					</form>
				</div>
				<div class="menu-block">
					<form name="export" >
						<p class="menu-header">Export</p>
						<ul>
							<li><input type="radio" name="type" value="gpx" checked>GPS exchange format (.gpx)</a></li>
							<li><input type="radio" name="type" value="wpt">Waypoint (.wpt)</a></li>
						</ul>
						<!--<input type="button" name="export" onclick="document.open(getRouteAs()); return false" value="Save">-->
						<input type="button" name="export" onclick="document.open(getRouteAs(), null, null); return false" value="Save">
					</form>
				</div>
			</div>
			<div id="right">
				<div id="feature_info"></div>
			</div>
			
			<div id="middle">
				<div id="map"></div>
			</div>
			<div class="cleaner">&nbsp;</div>
		</div>
	</div>
		
	<div id="footer">
		<p>
			<i>This site is hosted on the Netherlands tileserver, sponsored by <a href="http://www.oxilion.nl/">Oxilion</a>. Routing data from planet file: 
<?php 
/*
	$output = array();
	exec("stat /home/lambertus/planet.openstreetmap.org/planet-latest.osm.bz2", $output);
	foreach ($output as $line) {
		$parts = explode(" ", $line);
		if ($parts[0] == 'Modify:') {
			echo $parts[1]."\n";
		}
	}
*/
$datefile = "../../planet/yours/planet-date.txt";
if (file_exists($datefile)) {
	$myFile = $datefile;
	$fh = fopen($myFile, 'r');
	$theData = fgets($fh);
	fclose($fh);
	echo $theData;
}
?>
			. Please report any routing problems <a href="http://wiki.openstreetmap.org/index.php/YOURS/weird_routes">here</a>.</i>
		</p>
		<div id="edit"></div>
	</div>

</body>
</html>
