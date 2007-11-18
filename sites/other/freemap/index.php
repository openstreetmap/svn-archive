<?php

require_once('common/inc.php');
require_once('common/defines.php');
require_once('common/latlong.php');
require_once('common/osmclient.php');

session_start();

//die("Freemap is temporarily off air for maintenance.");

$modes = array
	("mapnik" => array ("UI" => 
		"css,sidebar,modebar,searchbar,milometer",
				"projection" => "Mercator"),
	 "npe" => array ("UI" => "css,sidebar,searchbar,milometer,modebar", 
	 			"projection" => "OSGB")
	 );

$m = isset($_GET["mode"]) ? $_GET["mode"] : "mapnik";
$mode = (isset($modes[$m])) ? $modes[$m] : $modes["mapnik"];

$en=do_coords($mode['projection'], $_GET);

?>
<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside</title>
<?php
if(strpos($mode["UI"],"css")!==false)
	echo "<link rel='stylesheet' type='text/css' href='/css/freemap2.css' />";
?>
<link rel='alternate' type='application/rss+xml' href='/wordpress/?feed=rss2'/>
<script type='text/javascript'>
var easting = <?php echo $en["e"]; ?>;
var northing = <?php echo $en["n"]; ?>;
</script>

<?php

echo "<script type='text/javascript' src='/freemap/javascript/$m/init.js'>".
"</script>\n";
?>

<script type='text/javascript' src="/freemap/javascript/lib/converter.js" > 
</script>
<script src="http://www.openlayers.org/api/2.4/OpenLayers.js"></script>
<script type='text/javascript' src="/freemap/javascript/FreemapClient.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/main.js" > </script>
<script type='text/javascript' src="/freemap/javascript/lib/jscoord-1.0.js" > 
</script>
<script type='text/javascript' src='/freemap/javascript/BboxMarkersLayer.js'> 
</script>
<script type='text/javascript' src='/freemap/javascript/GeoRSS2i.js' > </script>
<script type='text/javascript' src='/freemap/javascript/GGKMLi.js' > </script>
<!--
<script type='text/javascript' src="/freemap/javascript/pngfix.js" > </script>
-->
<script type='text/javascript' src="/freemap/javascript/lib/Dialog.js" > 
</script>
<script type='text/javascript' src='/freemap/javascript/WalkRouteLayer.js'>
</script>
<script type='text/javascript' src='/freemap/javascript/DrawWalkroute.js'>
</script>
<script type='text/javascript' 
src='/freemap/javascript/WalkRouteMarkersLayer.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/WalkRouteDownloadManager.js'> </script>
</head>
<body onload='init()'>
<div id='main'>

<div id='menubar'>
<span><strong>Mode:</strong></span>
<span id='mode0'>Normal </span> |
<span id='mode1'>Annotate</span> |
<span id='mode2'>Delete</span> |
<span id='mode3'>Distance</span> |
<span id='mode4'>Walk route</span> 
</div>

<div id="map"> </div>

<?php 
write_searchbar(); 
write_milometer(); 
?>

</div>
</div>

<?php write_sidebar(); ?>

</body>
</html>
