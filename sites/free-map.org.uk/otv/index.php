<?php
require_once("../lib/functionsnew.php");
include('index_funcs.php');

$lat = (isset($_GET['lat'])) ? $_GET['lat']:50.89;
$lon = (isset($_GET['lon'])) ? $_GET['lon']:-1.52;
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
    <!-- Prototype and JSPanoviewer don't like each other
    <script type='text/javascript' 
    src='../freemap/javascript/prototype/prototype.js'></script>
    -->
    <script type='text/javascript' src='PanoRoute.js'></script>
    <script type='text/javascript' src='js/jspanoviewer.js'></script>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    <style type='text/css'>
    #photocanvas {background-color: yellow }
    </style>
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
<a href='index.php?sbmt'>submit some photos...</a></p>

<?php
if (isset($_GET['sbmt']))
    psbmt();
else 
{
    echo "<div id='pansubmit_all'>\n";
    display_map($mapWidth,$mapHeight);
    echo "<div id='photodiv' style='width:0px; height:400px; ".
        "position: relative'></div>";
    //echo "\n<img id='panimg' height='400px'/>";
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

