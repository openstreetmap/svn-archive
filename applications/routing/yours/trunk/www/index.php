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

            $(document).ready(function(){
                /* Make the navigation tabs */
                $("#nav_header > ul").tabs();
            });

        </script>
    </head>
    <body class="indexbody" onload="init();">
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
							<ul class="route_via">
								<li id="waypointFrom">
										<input class="marker"  type="image" src="markers/route-start.png" alt="Start marker" title="Click to position start on the map" name="from" onclick="elementClick(this);" />
										<input class="box" type="text" name="from_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex="1" onfocus="this.select()" />
										<img id="from_image" src="images/blank.gif" alt="" title="" style="vertical-align:middle;" name="from_image" />
										<div id="from_message" style="display:inline"></div>
								</li>
								<li id="WaypointTo">
									<div>
										<input class="marker" type="image" src="markers/route-stop.png" alt="Finish marker" title="Click to position finish on the map" name="to" onclick="elementClick(this);" />
										<input class="box" type="text" name="to_text" onclick="elementClick(this);" onchange="elementChange(this);" value="e.g. Street, City" tabindex="2" onfocus="this.select()" />
										<img id="to_image" src="images/blank.gif" alt="" title="" style="vertical-align:middle;" name="to_image" />
										<div id="to_message" style="display:inline"></div>
									</div>
								</li>
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
							<p>Parameters</p>
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
				<i>This site is sponsored by <a href="http://www.oxilion.nl/">Oxilion</a>. Routing data from planet file:
<?php 
$datefile = "~/yours/planet-date.txt";
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
		</div>
    </body>
</html>
