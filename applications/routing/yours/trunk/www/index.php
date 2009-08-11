

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Your navigation</title>
		<link rel="stylesheet" href="main.css"/>
		<!--<link rel="stylesheet" href="http://dev.jquery.com/view/tags/ui/latest/themes/flora/flora.all.css" type="text/css" media="screen" title="Flora (Default)">-->
		<link rel="stylesheet" href="tabs.css"/>
		<script src="http://openlayers.org/api/OpenLayers.js" type="text/javascript"></script>
		<script src="config.js" type="text/javascript"></script>
		<script src="routing.js" type="text/javascript"></script>
		<script src="http://openstreetmap.org/openlayers/OpenStreetMap.js" type="text/javascript"></script>
		<script src="jquery/jquery.js" type="text/javascript"></script>
		<script src="jquery/jquery-ui.js" type="text/javascript"></script>
		
		<script>
  $(document).ready(function(){
	/* Make the navigation tabs */
    $("#nav_header > ul").tabs();
  });
  </script>

	</head>
	<body onload="init();">	
		<div id="header">
			<div id="title">
				<h1>OpenStreetMap routing service</h1>
			</div>
		
			<div id="help_about">
				<a href="index.php');">Home</a>
				<a href="help.html">Help</a>
				<a href="about.html">About</a>
			</div>
		</div>
		<div id="main">
			<div id="outer">
				<div id="navigation">
					<!-- Tabs -->
					<div id="nav_header">
						<ul>
			                <li><a href="#fragment-route"><span>Route</span></a></li>
			                <li><a href="#fragment-directions"><span>Directions</span></a></li>
			                <li><a href="#fragment-info"><span>Info</span></a></li>
							<li><a href="#fragment-export"><span>Export</span></a></li>
			            </ul>
					</div>
					<div id="fragment-route">
						<form id="via" action="#">
							<ul class="route_via">
								<li>
									<div class="hover">
										<img src="markers/route1.png" height="30" align="middle"/>
										<input type="button" width="50" name="from" onclick="elementClick(this);" value="From:" tabindex="5"/><input type="text" name="from_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex="1" onfocus="this.select()"/>
									</div>
									<div class="route_plus">
										<input type="button" name="+1" onclick="elementClick(this);" value="+" tabindex="5"/>
									</div>
								</li>
								<li>
									<div class="route_coord">
										<img src="markers/route2.png" height="30" align="middle"/>
										<input type="button" width="50" name="to" onclick="elementClick(this);" value="To:" tabindex="6"/><input type="text" name="to_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex="2" onfocus="this.select()"/>
									</div>
									<div class="route_plus">
										<input type="button" name="+1" onclick="elementClick(this);" value="+" tabindex="5"/>
									</div>
								</li>
						</form>
						<form id="parameters" action="#">
						<!--<div id="parameters">-->
							<p>Parameters</p>
							<ul>
								<li><input type="radio" name="type" value="motorcar" checked="checked" />Car</li>
								<li><input type="radio" name="type" value="hgv"/>Heavy goods</li>
								<li><input type="radio" name="type" value="psv"/>Public service</li>
								<li><input type="radio" name="type" value="bicycle"/>Bicycle</li>
								<li><input type="radio" name="type" value="foot"/>Foot</li>
							</ul>
							<ul>
								<li><input type="radio" name="method" value="fast" checked="checked" />Fastest</li>
								<li><input type="radio" name="method" value="short"/>Shortest</li>
							</ul>
						<!--</div>-->
					</form>
					<form id="route" action="#">
						<div id="route_action">
							<input type="button" name="calculate" onclick="elementClick(this);" value="Find route" tabindex="3"/>
							<input type="button" name="clear" onclick="elementClick(this);" value="Clear" tabindex="4"/>
							<input type="button" name="reverse" onclick="reverseRoute(this);" value="Reverse" tabindex="7"/>
						</div>
						
						
					</form>
					<div id="status"></div>
					
					</div>
					<div id="fragment-directions" class="nav_content">1</div>
					<div id="fragment-info" class="nav_content">
						<div id="feature_info"></div>
					</div>
					<div id="fragment-export" class="nav_content">
						<form id="export" action="#">
							<p>Export</p>
							<ul>
								<li><input type="radio" name="type" value="gpx" checked="checked"  />GPS exchange format (.gpx)</li>
								<li><input type="radio" name="type" value="wpt"/>Waypoint (.wpt)</li>
							</ul>
							<p>
								<input type="button" name="export" value="Export" onclick="document.open(getRouteAs(), null, null); return false" />
							</p>
						</form>
					</div>
					
						
						
						<!--
							<form id="parameters" action="#">
							<p>Parameters</p>
							<ul>
								<li><input type="radio" name="type" value="motorcar" checked="checked"  />Car</li>
								<li><input type="radio" name="type" value="bicycle"/>Bicycle</li>
								<li><input type="radio" name="type" value="foot"/>Foot</li>
							</ul>
							<ul>
								<li><input type="radio" name="method" value="fast" checked="checked" />Fastest</li>
								<li><input type="radio" name="method" value="short"/>Shortest</li>
							</ul>
							</form>
						-->
						


						
					</div>
					<div id="extra">
						
					</div>
				</div>
				
				<div id="content">
					<div id="map"></div>
				</div>

				<div id="clearfooter">&nbsp;</div>
			</div>

		</div>
		<div id="footer">
			<i>This site is hosted on the Netherlands tileserver, sponsored by <a href="http://www.jronline.nl/">JROnline</a>. Routing data from planet file: 
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
$datefile = "planet-date.txt";
if (file_exists($datefile)) {
	$myFile = $datefile;
	$fh = fopen($myFile, 'r');
	$theData = fgets($fh);
	fclose($fh);
	echo $theData;
}
?>
			</i>		

		</div>
	</body>
</html>
