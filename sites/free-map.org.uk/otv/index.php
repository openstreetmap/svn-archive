<?php
require_once("../lib/functionsnew.php");
include('index_funcs.php');

$lat = (isset($_GET['lat'])) ? $_GET['lat']:50.9;
$lon = (isset($_GET['lon'])) ? $_GET['lon']:-1.4;
$zoom = (isset($_GET['lon'])) ? $_GET['zoom']:14;
$uploading = (isset($_GET['sbmt'])) ? "true":"false";

$mapWidth=($uploading=="true")?320:1024;
$mapHeight=400;

session_start();
?>
<html>
<head>
    <title>OpenTrailView</title>

    <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
 
    <script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js">
    </script>
 
    <script type="text/javascript">
        // Start position for the map 
        var lat=<?php echo $lat; ?>;
        var lon=<?php echo $lon; ?>;
        var zoom=<?php echo $zoom;?>;
        var uploading=<?php echo $uploading;?>;
 
    </script>
	<script type='text/javascript' src='main.js'></script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
</head>
 
<!-- body.onload is called once the page is loaded (call the init function) -->
<body onload="init()">

<div id='maindiv'>
<h1>OpenTrailView</h1>
<div id='login'>

<?php
$conn=dbconnect("otv");
if(isset($_SESSION['gatekeeper']))
{
    $username=get_col("users",$_SESSION['gatekeeper'],"username");
    echo "Logged in as $username <a href='user.php?action=logout'>Log out</a>";
}
else
{
    echo "<a href='user.php?action=login&redirect=/otv/index.php'>Login</a>";
}
?>
</div><!--login-->
<p>StreetView for the world's trails and footpaths!
Read <a href='howto.html'>how to contribute</a> or
<a href='index.php?sbmt'>submit some panoramas...</a></p>

<?php
if (isset($_GET['sbmt']))
	psbmt();
else 
{
	echo "<div id='pansubmit_all'>\n";
	display_map($mapWidth,$mapHeight);
	echo "<div id='photodiv' style='width:0px;  height:400px; overflow: auto'>";
	echo "\n<img id='panimg' height='400px'/></div>".
		"<!--photodiv-->\n";
	echo "</div>\n";
	?>
	<div id='controls'>
	<input type='button' id='mainctrl' 
	 onclick='modeSet()' value='Align photos' />
	</div>
	<?php
}

mysql_close($conn);
?>
</div> <!--maindiv-->
</body>
 
</html>

