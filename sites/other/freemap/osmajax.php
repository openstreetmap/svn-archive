<?php
require_once('common/latlong.php');
require_once('common/functionsnew.php');
require_once('common/freemap_functions.php');

if(isset($_GET['trackid']))
{
	$conn=dbconnect();
	list($lat,$lon) = get_track_centre_point($_GET['trackid']);
	mysql_close($conn);
}
else
{
	$lat = isset($_GET['lat']) ? $_GET['lat']: 51.05;
	$lon = isset($_GET['lon']) ? $_GET['lon']: -0.72;
}
$merc = ll_to_merc($lat,$lon);
$trackid_js = (isset($_GET['trackid'])) ? $_GET['trackid']: 'null';
?>
<html>
<head>
<title>OSMAJAX</title>
<script type='text/javascript'>
var easting = <?php echo $merc['e']; ?>;
var northing = <?php echo $merc['n']; ?>; 
<?php
echo "var trackid = $trackid_js\n";
?>
</script>
<script src="http://www.openlayers.org/api/2.4/OpenLayers.js"></script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-freemap/init.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/Osmajax.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/ajax.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/OSM.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/OSMItem.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/GeometriedOSMItem.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/OSMNode.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/OSMWay.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/OSMFeature.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/DrawOSMFeature.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/routetypes.js'> </script>
<script type='text/javascript' 
src='/freemap/javascript/osmajax-0.5/ChangeFeatureDialog.js'> </script>
<script type='text/javascript' src='/freemap/javascript/lib/converter.js'> 
</script>
<script type='text/javascript' src='/freemap/javascript/lib/jscoord-1.0.js'> 
</script>
<script type='text/javascript' 
src='/freemap/javascript/lib/Dialog.js'> </script>
</head>
<style type='text/css'>
#map { width:640px; height:480px;}
#status { width:640px; background-color: #000080; color:white; 
			font-size:120%}
</style>
</head>
<body onload='init()'>
<div id="status">Welcome to OSMAJAX</div>
<div id="map"></div>
<div id="editpanel">
<input type='button' id='loginbtn' value='login'/>
<input type='button' id='navbtn' value='navigate' disabled='disabled'/>
<input type='button' id='selbtn' value='select'/>
<input type='button' id='drwbtn' value='draw' disabled='disabled'/>
<input type='button' id='drwpointbtn' value='draw point' disabled='disabled' />
<input type='button' id='chngbtn' value='change' disabled='disabled' />
<input type='button' id='delbtn' value='delete' disabled='disabled' />
</div>
<div>
<label for="search">Search (UK only):</label>
<input id="search"/>
<input type="button" value="Go!" onclick="placeSearch()"/>
</div>
</body>
</html>
