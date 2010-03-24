<?php

require_once('inc.php');
require_once('/home/www-data/private/defines.php');
require_once('latlong.php');

session_start();

$inp=array();

$inp["lat"] = (isset($_GET['lat'])) ? $_GET['lat']:
	((isset($_COOKIE['lat'])) ? $_COOKIE['lat'] : 51); 
	
$inp["lon"] = (isset($_GET['lon'])) ? $_GET['lon']:
	((isset($_COOKIE['lon'])) ? $_COOKIE['lon'] : -1); 

$inp["zoom"] = (isset($_GET['zoom'])) ? $_GET['zoom']:
	((isset($_COOKIE['zoom'])) ? $_COOKIE['zoom'] : 8); 
	
$en=do_coords("Google", $inp);

?>

<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside </title>
<link rel='stylesheet' type='text/css' href='/freemap/css/freemap2.css' />
<script type='text/javascript'>
var easting = <?php echo $en["e"]; ?>;
var northing = <?php echo $en["n"]; ?>;
var zoom = <?php echo $inp["zoom"]; ?>;
var loggedIn= <?php echo isset($_SESSION['gatekeeper'])?"true;\n":"false;\n"; ?>
</script>
<script src="http://www.openlayers.org/api/2.5/OpenLayers.js"></script>
<script type='text/javascript' src="/freemap/javascript/FreemapClient.js" > 
</script>
<script type='text/javascript' src='/freemap/javascript/lib/get_osm_url.js'>
</script>
<script type='text/javascript' src='/freemap/javascript/init.js'>
</script>

<script type='text/javascript' src="/freemap/javascript/lib/converter.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/lib/jscoord-1.0.js" > 
</script>
<script type='text/javascript' src='/freemap/javascript/BboxMarkersLayer.js'> 
</script>
<script type='text/javascript' src='/freemap/javascript/GeoRSS2i.js' > </script>
<script type='text/javascript' src='/freemap/javascript/GGKMLi.js' > </script>
<script type='text/javascript' src="/freemap/javascript/lib/Dialog.js" > 
</script>
</head>
<body onload='init()'>

<?php write_sidebar(true); ?>

<div id='main'>

<div id='menubar'>
<span><strong>Mode:</strong></span>
<span id='mode0'>Normal </span> |
<span id='mode1'>Annotate</span> |
<span id='mode2'>Delete</span> |
<span id='mode3'>Distance</span> 
</div>

<div id="map"> </div>

<?php 
?>

</div>
</div>


</body>
</html>
