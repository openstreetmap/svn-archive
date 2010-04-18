<?php header('Content-Type: text/html'); ?>
<!-- Copyright (c) 2009, L. IJsselstein and others
     Yournavigation.org All rights reserved.
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"  lang="en" xml:lang="en">
    <head>
        <title>YourNavigation - Worldwide routing on OpenStreetMap data</title>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <link rel="stylesheet" href="reset.css"/>
        <link rel="stylesheet" href="yournavigation.css"/>
        <script type="text/javascript" src="jquery/jquery.js"></script>
        <script type="text/javascript" src="jquery/jquery-ui.js"></script>
        <script type="text/javascript" src="http://openlayers.org/api/OpenLayers.js" ></script>
        <script type="text/javascript" src="http://openstreetmap.org/openlayers/OpenStreetMap.js"></script>
        <script type="text/javascript" src="api/dev/yours.js"></script>
        <script type="text/javascript" src="yournavigation.js"></script>

        <script type="text/javascript">
			$(function() {
				// Initialise the map
				init();
				// Make the navigation tabs
				$("#nav_header").tabs();
				// Make the via points sortable
				$("#route_via").sortable({
					update: waypointReorderCallback
				});
			});
        </script>
    </head>
    <body class="indexbody">
		<div id="header">
			<!--
			<div id="title">
				<h1>OpenStreetMap routing service</h1>
			</div>
			-->
			<img id="left_image" src="images/yours_left.png" alt="(Y)" />
			<img id="middle_image" src="images/yours_text.png" alt="OpenStreetMap Routing Service" />
			<img id="right_image" src="images/yours_right.png" alt="(R)" />
			<div id="site_menu">
                            <a href="index.php">Home</a>
                            <a href="help.html">Help</a>
                            <a href="about.html">About</a>
                        </div>
		<div style="clear:both;"></div>
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
						<form id="route" name="route" action="#" onsubmit="return false;">
							<ul id="route_via" class="route_via">
							</ul>
							<ul>
								<li>
									<div>
										<img src="markers/yellow.png" alt="marker to" height="30" style="vertical-align:middle;"/>
										<input type="button" name="add waypoint" onclick="elementClick(this);" value="Add Waypoint" tabindex="4"/>
									</div>
								</li>
							</ul>
						</form>
						<form id="parameters" action="#">
							<!--<div id="parameters">-->
							<p>Type of transport</p>
							<ul>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="motorcar" checked="checked" />Car</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="hgv"/>Heavy goods</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="goods"/>Goods</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="psv"/>Public service</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="bicycle"/>Bicycle</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="cycleroute"/>Bicycle (routes)</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="foot"/>Foot</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="moped"/>Moped</li>
								<li><input type="radio" name="type" onclick="typeChange(this);" value="mofa"/>Mofa</li>
							</ul>
						</form>
						<form id="options" action="#">
							<p>Routing method</p>
							<ul>
								<li><input type="radio" name="method" value="fast" checked="checked" />Fastest</li>
								<li><input type="radio" name="method" value="short"/>Shortest</li>
							</ul>
							<!--</div>-->
						</form>
						<form id="calculate" action="#">
							<div id="route_action">
								<input type="button" name="calculate" onclick="elementClick(this);" value="Find route" tabindex="3"/>
								<input type="button" name="clear" onclick="elementClick(this);" value="Clear" tabindex="5"/>
								<input type="button" name="reverse" onclick="elementClick(this);" value="Reverse" tabindex="7"/>
							</div>
						</form>
						<div id="status"></div>

					</div>
					<div id="fragment-directions" class="nav_content">
						<div id="directions"></div>
					</div>
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
								<input type="button" name="export" value="Export" onclick="elementClick(this);" />
							</p>
						</form>
					</div>
				</div>
				<div id="extra">

				</div>
			</div>

			<div id="content"><div id="map"></div></div>

			<!--<div id="clearfooter">&nbsp;</div>-->
			<div style="clear:both;"></div>
			<div class="footer">
				<i>This site is sponsored by <a href="http://www.oxilion.nl/">Oxilion</a>.
<?php 
$datefile = '~/yours/planet-date.txt';
if (file_exists($datefile)) {
		$myFile = $datefile;
		$fh = fopen($myFile, 'r');
		$theData = fgets($fh);
		fclose($fh);
		echo 'Routing data from planet file:', $theData;
}
?>
				</i>
			</div>
		</div>
    </body>
</html>
