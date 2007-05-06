<?php

require_once('common/inc.php');
require_once('common/defines.php');
require_once('common/latlong.php');
require_once('common/osmclient.php');

session_start();

if(isset($_POST["osmusername"]) && isset($_POST["osmpassword"]))
{
	if(check_osm_login ($_POST["osmusername"],$_POST["osmpassword"]))
	{
		$_SESSION["osmusername"] = $_POST["osmusername"];
		$_SESSION["osmpassword"] = $_POST["osmpassword"];
	}
	else
	{
		echo "Invalid OSM username/password";
	}
}

$en=do_coords("OSGB", $_GET);


?>
<html>
<head>
<title>FREEMAP - OpenStreetMap maps for the countryside</title>
<link rel='alternate' type='application/rss+xml' href='/wordpress/?feed=rss2'/>

<script type='text/javascript'>
var easting = <?php echo $en["e"]; ?>;
var northing = <?php echo $en["n"]; ?>;
</script>

<script src='http://www.openlayers.org/dev/lib/OpenLayers.js'></script>
<script src='/freemap/javascript/vector/init.js'></script>
<script src='/freemap/javascript/vector/OSM.js'></script>
<script src='/freemap/javascript/vector/OSMFeature.js'></script>
<script src='/freemap/javascript/vector/OSMItem.js'></script>
<script src='/freemap/javascript/vector/GeometriedOSMItem.js'></script>
<script src='/freemap/javascript/vector/OSMNode.js'></script>
<script src='/freemap/javascript/vector/OSMSegment.js'></script>
<script src='/freemap/javascript/vector/OSMWay.js'></script>
<script src='/freemap/javascript/vector/routetypes.js'></script>
<script src='/freemap/javascript/vector/DrawOSMFeature.js'></script>
<script src='/freemap/javascript/vector/ajax.js'></script>
<script type='text/javascript' src="/freemap/javascript/jscoord-1.0.js" > 
</script>
<script type='text/javascript' src="/freemap/javascript/converter.js" > 
</script>
<style type='text/css'>
#status { background-color: #ffffc0; }
</style>
</head>
<body onload='init()'>

<?php

if(isset($_SESSION["osmusername"]))
{
	echo "<p id='osmloginmsg'><em>Logged into OSM as ".
 		"$_SESSION[osmusername]</em> ".
		"<a href='common/osmlogout.php'>Log out</a> ".
		"<a href='/freemap/index.php'>Map</a></p>";
		
	write_editcontrols();
	?>
	<p id="status"></p>
	<div id="map"> </div>
	<?php
}


else
{
	write_osmloginform();	
	?>
	<p><strong>This is an experimental feature!</strong> Osmajax is in
	very early development. It's almost certainly got bugs. If you 
	create OSM features using osmajax, I would recommend you check them
	in JOSM afterwards.</p>
	<?php
}
?>

</body>
</html>
