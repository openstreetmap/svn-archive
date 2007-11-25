<?php
require_once('../../common/latlong.php');
$lat = isset($_GET['lat']) ? $_GET['lat']: 51.05;
$lon = isset($_GET['lon']) ? $_GET['lon']: -0.72;
$merc = ll_to_merc($lat,$lon);
?>
<html>
<head>
<title>OSMAJAX</title>
<script type='text/javascript'>
var easting = <?php echo $merc['e']; ?>;
var northing = <?php echo $merc['n']; ?>; 
</script>
<script src="http://www.openlayers.org/api/2.4/OpenLayers.js"></script>
<script type='text/javascript' src='init.js'> </script>
<script type='text/javascript' src='Osmajax.js'> </script>
<script type='text/javascript' src='ajax.js'> </script>
<script type='text/javascript' src='OSM.js'> </script>
<script type='text/javascript' src='OSMItem.js'> </script>
<script type='text/javascript' src='GeometriedOSMItem.js'> </script>
<script type='text/javascript' src='OSMNode.js'> </script>
<script type='text/javascript' src='OSMWay.js'> </script>
<script type='text/javascript' src='OSMFeature.js'> </script>
<script type='text/javascript' src='DrawOSMFeature.js'> </script>
<script type='text/javascript' src='routetypes.js'> </script>
<script type='text/javascript' src='ChangeFeatureDialog.js'> </script>
<script type='text/javascript' src='../lib/converter.js'> </script>
<script type='text/javascript' src='../lib/jscoord-1.0.js'> </script>
<script type='text/javascript' src='../lib/Dialog.js'> </script>
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
