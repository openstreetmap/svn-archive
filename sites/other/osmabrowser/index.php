<?php
session_start();

if(!isset($_SESSION['lon']) || isset($_GET['lon']))
	$_SESSION['lon'] = (isset($_GET['lon'])) ? $_GET['lon'] : -0.725;
if(!isset($_SESSION['lat']) || isset($_GET['lat']))
	$_SESSION['lat'] = (isset($_GET['lat'])) ? $_GET['lat'] : 51.05; 
if(!isset($_SESSION['scale']) || isset($_GET['scale']))
	$_SESSION['scale'] = (isset($_GET['scale'])) ? $_GET['scale'] : 1; 
if(!isset($_SESSION['latspan']) || isset($_GET['latspan']))
	$_SESSION['latspan'] = (isset($_GET['latspan'])) ? $_GET['latspan'] : 0.025;
if(!isset($_SESSION['lonspan']) || isset($_GET['lonspan']))
	$_SESSION['lonspan'] = (isset($_GET['lonspan'])) ? $_GET['lonspan'] : 0.05;
if(!isset($_SESSION['img']) || isset($_GET['img']))
	$_SESSION['img'] = (isset($_GET['img'])) ? $_GET['img'] : 'svg';

$bbox = ($_SESSION['lon']-$_SESSION['lonspan']/2).",".
		($_SESSION['lat']-$_SESSION['latspan']/2).",".  
		($_SESSION['lon']+$_SESSION['lonspan']/2).",".  
		($_SESSION['lat']+$_SESSION['latspan']/2);

writehtmlhead();
echo "<body>\n";
writesvg($bbox);
writehtmlcontrols($bbox);

?>
</body></html>

<?php
function writehtmlhead()
{
	?>
	<html>
	<head>
	<style type='text/css'>
	body { font-family: luxi sans, lucida, helvetica, arial, sans-serif }
	form {display:inline }
	#svg { width:720px; height:640px; position:absolute;
	overflow:auto;}
	#controls { position:absolute; left:736px; font-size: 80% }
	.entry { width: 50px ; }
	#controls h1,#controls h2, #controls h3 { text-align: center }
	#navigation { text-align: center }
	img { border-style: none }
	</style>
	</head>
	<?php
}

function writehtmlcontrols($bbox)
{
	$lat = $_SESSION['lat'];
	$lon = $_SESSION['lon'];
	$latspan = $_SESSION['latspan'];
	$lonspan = $_SESSION['lonspan'];
	echo "<div id='controls'>\n";
	echo "<h1>Osmabrowser v0.0.4</h1> \n";
	echo "<div id='navigation'>\n";
	echo "<a href='index.php?lat=$lat&amp;lon=".($lon-$lonspan)."'>".
		"<img src='/images/osmabrowser/arrow_left.png' alt='west'/></a>\n";
	echo "<a href='index.php?lat=$lat&amp;lon=".($lon+$lonspan)."'>".
		"<img src='/images/osmabrowser/arrow_right.png' alt='east'/></a>\n";
	echo "<a href='index.php?lon=$lon&amp;lat=".($lat-$latspan)."'>".
		"<img src='/images/osmabrowser/arrow_down.png' alt='south'/></a>\n";
	echo "<a href='index.php?lon=$lon&amp;lat=".($lat+$latspan)."'>".
		"<img src='/images/osmabrowser/arrow_up.png' alt='north'/></a>\n";
	
	if($_SESSION["img"]=="svg")
	{
		echo "<a href='index.php?img=png'><img src='/images/osmabrowser/png.png' alt='PNG image'".
			 "/></a>\n";
	}
	else
	{
		echo "<a href='index.php?img=svg'><img src='/images/osmabrowser/svg.png' alt='SVG image'".
			 "/></a>\n";
	}
	/*

	echo "<a href='index.php?lon=$lon&amp;lat=$lat&amp;scale=".
			($_SESSION['scale']*2)."'>".
		"<img src='/images/osmabrowser/magnify.png' alt='zoom in'/></a>\n";
	echo "<a href='index.php?lon=$lon&amp;lat=$lat&amp;scale=".
			($_SESSION['scale']/2)."'>".
		"<img src='/images/osmabrowser/shrink.png' alt='zoom out'/></a>\n";
	echo "<a href='index.php?html=0'>".
		"<img src='/images/osmabrowser/svg.png' alt='SVG only (no HTML)'/></a>".
		"\n";
		*/
	echo "</div>\n";
	?>
	<div class='panel'>
	<h2>Go to</h2>
	<h3>Place search</h3>
	<form method='post' action='geocoder.php'>
	<label for='place'>Place</label>
	<input name='place' id='place' class='entry' />
	<label for='country'>Country</label>
	<input name='country' id='country' value='uk' class='entry' />
	<input type='submit' value='Go!' />
	</form>
	<h3>Latitude/longitude</h3>
	<form method='get' action='index.php'>
	<label for='lat'>Lat</label>
	<input name='lat' id='lat' class='entry' />
	<label for='lon'>Lon</label>
	<input name='lon' id='lon' class='entry' />
	<input type='submit' value='Go!' />
	</form>
	</div>

	<div class='panel'>
	<h2>Settings</h2>
	<form method='get' action='index.php'>
	<label for='latspan'>Latitude span</label>
	<input name='latspan' id='latspan' class='entry'/> <br/>
	<label for='latspan'>Longitude span</label>
	<input name='lonspan' id='lonspan' class='entry'/> <br/>
	<input type='submit' value='Go!' />
	</form>
	</div>
	<?php

	echo "</div>\n";
}

function writesvg($bbox)
{
echo "<div id='svg'>\n";
if ($_SESSION['img']=='svg')
{
	echo "<object data='gensvg.php?bbox=$bbox&scale=$_SESSION[scale]' ".
	  "type='image/svg+xml' width='720' ".
	 "height='720'> </object>";
}
else
{
	echo "<img src='/freemap/freemap.php?bbox=$bbox&".
		 "WIDTH=720&HEIGHT=720' alt='PNG map for $bbox' />\n";
}
echo "</div>\n";
}
