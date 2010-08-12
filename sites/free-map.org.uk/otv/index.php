<?php
require_once("../lib/functionsnew.php");
include('index_funcs.php');

$lat = (isset($_GET['lat'])) ? $_GET['lat']:
	(isset($_COOKIE['otvLat']) ? $_COOKIE['otvLat'] : 50);
$lon = (isset($_GET['lon'])) ? $_GET['lon']:
	(isset($_COOKIE['otvLon']) ? $_COOKIE['otvLon'] : 0);
$zoom = (isset($_GET['lon'])) ? $_GET['zoom']:
	(isset($_COOKIE['otvZoom']) ? $_COOKIE['otvZoom'] : 4);
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
    <script type='text/javascript' src='PanoRoute.js'></script>
    <script type='text/javascript' src='js/jspanoviewer.js'></script>
	<script type='text/javascript' src='js/fileuploader.js'></script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    <link rel='stylesheet' type='text/css' href='css/fileuploader.css' />
    <style type='text/css'>
    #photocanvas {background-color: yellow }
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
<a href='index.php?sbmt'>submit some photos...</a></p>

<?php
if (isset($_GET['sbmt']))
    psbmt();
else 
{
    echo "<div id='pansubmit_all' style='overflow: auto'>\n";
    display_map($mapWidth,$mapHeight);
	// for panoramas
	/*
    echo "<div id='photodiv' style='width:0px; height:400px; ".
        "position: relative; visibility: hidden'></div>";
	*/
    echo "\n<img id='panimg' height='400px' style='visibility:hidden'/>";
    echo "</div><!--pansubmit_all-->\n";
    ?>
    <div id='controls'>
    <!--
    <input type='button' id='mainctrl' 
     onclick='modeSet()' value='Align photos' />
     -->
     <select id='mainctrl' onchange='modeSet()'>
     <option>View photos</option>
     <option>Align photos</option>
     <option>Connect photos</option>
     </select>
     <input type='button' id='backtomap' value='Map' />
    </div>
    <?php
}

mysql_close($conn);
?>
</div> <!--maindiv-->
</body>
 
</html>

