<?php
require_once("../lib/functionsnew.php");
include('otv_funcs.php');

$lat = (isset($_GET['lat'])) ? $_GET['lat']:
	(isset($_COOKIE['otvLat']) ? $_COOKIE['otvLat'] : 50.88);
$lon = (isset($_GET['lon'])) ? $_GET['lon']:
	(isset($_COOKIE['otvLon']) ? $_COOKIE['otvLon'] : -1.5);
$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']:
	(isset($_COOKIE['otvZoom']) ? $_COOKIE['otvZoom'] : 14);
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
    <script type='text/javascript' src='js/main.js'></script>
    <!-- Prototype and JSPanoviewer don't like each other-->
    <script type='text/javascript' 
    src='http://www.free-map.org.uk/javascript/prototype.js'></script>
    <!--    -->
    <script type='text/javascript' src='js/GeoRSSExt.js'></script>
	<script type='text/javascript' src='js/fileuploader.js'></script>
	<script type='text/javascript' src='js/cpv.js'></script>
	<script type='text/javascript' src='js/PanController.js'></script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    <link rel='stylesheet' type='text/css' href='css/fileuploader.css' />
    <style type='text/css'>
    #pancanvas {background-color: yellow; }
    </style>
</head>
 
<!-- body.onload is called once the page is loaded (call the init function) -->
<?php
if(isset($_GET['sbmt']))
	echo "<body>\n";
else
	echo "<body onload='init()' onunload='savePageState()'>\n";
?>

<div id='maindiv'>
<h1>OpenTrailView</h1>
<div id='login'>

<?php
if(isset($_SESSION['gatekeeper']))
{
	$conn=pg_connect("dbname=gis user=gis");
	$result=pg_query("SELECT username FROM users WHERE id=".
				"$_SESSION[gatekeeper]");
	$row=pg_fetch_array($result,null,PGSQL_ASSOC);
    echo "Logged in as $row[username] ".
		"<a href='/common/user.php?action=logout'>Log out</a>";
	pg_close($conn);
}
else
{
    echo "<a href='".
		"/common/user.php?action=login&redirect=/index.php'>Login</a>";
}
?>
</div><!--login-->
<p>StreetView for the world's trails and footpaths!
Read <a href='howto.html'>how to contribute</a> or
<a href='psbmt.php'>submit some photos...</a></p>

<?php
if(true)
{
    echo "<div id='pansubmit_all' >\n";
    display_map($mapWidth,$mapHeight);
	echo "\n<div id='imgdiv' style='visibility:hidden; width:800px; height:0px'>";
    //echo "\n<img id='panimg'  style='visibility:hidden'/>";
	echo "\n</div>\n";
    echo "\n<canvas id='pancanvas'  style='visibility:hidden' ".
		" width='1024' height='400'></canvas>";
    echo "</div><!--pansubmit_all-->\n";
    ?>
    <div id='controls'>
	 <input type='radio' id='mode0' name='modes' checked='checked' 
	 onclick='modeSet(this)'/>View
	 <input type='radio' id='mode1' name='modes' onclick='modeSet(this)'/>
	 Preview
	 <input type='radio' id='mode2' name='modes' onclick='modeSet(this)'/>Rotate
	 <input type='radio' id='mode3' name='modes' onclick='modeSet(this)'/>Move
     <input type='button' id='backtomap' value='Map' />
    </div>
    <?php
}

?>
</div> <!--maindiv-->
<div id='status' style='height:200px; overflow:auto'></div>
</body>
 
</html>

