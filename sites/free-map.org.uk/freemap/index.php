<?php

require_once('inc.php');
require_once('/home/www-data/private/defines.php');

session_start();

$lat = (isset($_GET['lat'])) ? $_GET['lat']:
	((isset($_COOKIE['lat'])) ? $_COOKIE['lat'] : 51.05); 
	
$lon = (isset($_GET['lon'])) ? $_GET['lon']:
	((isset($_COOKIE['lon'])) ? $_COOKIE['lon'] : -0.72); 

$zoom = (isset($_GET['zoom'])) ? $_GET['zoom']:
	((isset($_COOKIE['zoom'])) ? $_COOKIE['zoom'] : 14); 
	

$modes = array (
					array ("Normal", "MODE_NORMAL"),
					array ("Distance", "MODE_DISTANCE")
				);
?>

<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside </title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />

<script type='text/javascript'>

var lat=<?php echo $lat; ?>;
var lon=<?php echo $lon; ?>;
var zoom=<?php echo $zoom;?>;

<?php
for($i=0; $i<count($modes); $i++)
{
	echo "var {$modes[$i][1]} = $i;\n";
}
?>

</script>

<script src="http://www.openlayers.org/api/OpenLayers.js">
</script>

<script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"> 
</script>

<script src="js/main.js"> </script>

</head>

<body onload='init()'>

<?php write_sidebar(true); ?>

<div id='main'>

<div id='menubar'>
<span><strong>Mode:</strong></span>
<?php
for($i=0; $i<count($modes); $i++)
{
	if($i)
		echo " | ";
	echo "<span id='mode$i' onclick='setMode($i)'>{$modes[$i][0]}</span>";
}
?>
</div>

<div id="map"> </div>

</div>


</body>
</html>
