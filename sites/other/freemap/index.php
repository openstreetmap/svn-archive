<?php

require_once('common/inc.php');
require_once('common/defines.php');
require_once('common/latlong.php');
require_once('common/osmclient.php');

session_start();

$modes = array
	("mapnik" => array ("UI" => 
		"css,sidebar,modebar,searchbar,milometer,popups",
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

<script type='text/javascript' src="/freemap/javascript/converter.js" > 
</script>
<script src="http://www.openlayers.org/api/2.2/OpenLayers.js"></script>
<script type='text/javascript' src="/freemap/javascript/GeoRSSClient.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/FreemapClient.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/main.js" > </script>
<script type='text/javascript' src="/freemap/javascript/jscoord-1.0.js" > </script>
<script type='text/javascript' src='/freemap/javascript/BboxMarkersLayer.js'> 
</script>
<script type='text/javascript' src='/freemap/javascript/GeoRSS2i.js' > </script>
<script type='text/javascript' src='/freemap/javascript/GGKMLi.js' > </script>
<script type='text/javascript' src="/freemap/javascript/pngfix.js" > </script>
</head>
<body onload='init()'>
<div id='main'>
<?php

if(strpos($mode["UI"],"modebar")!==false)
{
	?>
	<div id='menubar'>
	<span><strong>Mode:</strong></span>
	<span id='mode0'>Normal </span> |
	<span id='mode1'>Annotate</span> |
	<span id='mode2'>Delete</span> |
	<span id='mode3'>Distance</span> 
	</div>
	<?php
}

if(strpos($mode["UI"],"popups")!==false)
	write_inputbox(); 

?>

<div id="map"> </div>

<?php 
if(strpos($mode["UI"],"searchbar")!==false)
	write_searchbar(); 
if(strpos($mode["UI"],"milometer")!==false)
	write_milometer(); 
?>

</div>
</div>
<?php 
if(strpos($mode["UI"],"sidebar")!==false)
	write_sidebar(true); 
?>
</body>
</html>
