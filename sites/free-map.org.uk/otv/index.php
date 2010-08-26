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
    <script type='text/javascript' src='main.js'></script>
    <!-- Prototype and JSPanoviewer don't like each other-->
    <script type='text/javascript' 
    src='../freemap/javascript/prototype/prototype.js'></script>
    <!--    -->
    <script type='text/javascript' src='GeoRSSExt.js'></script>
    <script type='text/javascript' src='RouteJSON.js'></script>
    <script type='text/javascript' src='OSM2.js'></script>
    <script type='text/javascript' src='HTTP2.js'></script>
    <script type='text/javascript' src='js/jspanoviewer.js'></script>
	<script type='text/javascript' src='js/fileuploader.js'></script>
	<script type='text/javascript' src='canvas/cpv.js'></script>
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
    <!--
    <input type='button' id='mainctrl' 
     onclick='modeSet()' value='Align photos' />
     -->
	 <!--
     <select id='mainctrl' onchange='modeSet()'>
     <option value='0'>View photos</option>
     <option value='1'>Rotate photos</option>
     <option value='2'>Make new route</option>
     <option value='3'>Move photos, add photos to route</option>
     <option value='4'>Delete</option>
     </select>
	 -->
	 <input type='radio' id='mode0' name='modes' checked='checked' 
	 onclick='modeSet(this)'/>View
	 <input type='radio' id='mode1' name='modes' onclick='modeSet(this)'/>Rotate
	 <input type='radio' id='mode2' name='modes' onclick='modeSet(this)'/>
	 New route
	 <input type='radio' id='mode3' name='modes' onclick='modeSet(this)'/>Move
	 <input type='radio' id='mode4' name='modes' onclick='modeSet(this)'/>Delete
     <input type='button' id='backtomap' value='Map' />
    </div>
    <?php
}

mysql_close($conn);
?>
</div> <!--maindiv-->
<div id='status' style='height:200px; overflow:auto'></div>
</body>
 
</html>

