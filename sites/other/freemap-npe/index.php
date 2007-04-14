<?php
session_start();

$_SESSION['e'] = isset($_GET['e']) ? $_GET['e']: 
	(isset($_SESSION['e'])  ? $_SESSION['e'] : 489600);
$_SESSION['n'] = isset($_GET['n']) ? $_GET['n']: 
	(isset($_SESSION['n']) ? $_SESSION['n'] : 128500);
?>
<html>
<head>
<script type='text/javascript' 
src='http://www.openlayers.org/api/2.2-rc2/OpenLayers.js'>
</script>
<script type='text/javascript'>
var easting = <?php echo $_SESSION['e']; ?>;
var northing = <?php echo $_SESSION['n']; ?>;
</script>
<script type='text/javascript' src='main.js'> </script>
<style type='text/css'>
body { font-family: helvetica,arial,sans-serif }
#map { position:absolute; top:0px; left:0px; width:750px; height:500px;
		overflow:auto}
#fmap { position:absolute; top:0px; left:0px; width:750px; height:500px;
z-index:100;}
#rightbar { left: 750px; position:absolute; top:0px;}
a { text-decoration: none }
</style>
</head>

<body onload='init()'>

<div id='map'> </div>
<div id='rightbar'>
<p><img src='/images/freemap_halfsize.png' alt='the new freemap'/></p>
<p><strong>freemap-npe alpha</strong>
Using New Popular Edition maps from
<a href='http://www.npemap.org.uk'>npemap.org.uk</a> -
maps are not for commercial use</p>
<p>Path data from
<a href='http://www.openstreetmap.org'>OpenStreetMap</a>
</p>
<p>Slippy map from
<a href='http://www.openlayers.org'>OpenLayers</a></p>
<div>
<!--
<img src='/images/osmabrowser/arrow_up.png' alt='north' 
onclick="updateGrid('up')" />
<img src='/images/osmabrowser/arrow_down.png' alt='south' 
onclick="updateGrid('down')" />
<img src='/images/osmabrowser/arrow_left.png' alt='west' 
onclick="updateGrid('left')" />
<img src='/images/osmabrowser/arrow_right.png' alt='east' 
onclick="updateGrid('right')" />
<img src='/images/osmabrowser/magnify.png' alt='zoom in' onclick="zoomin()" />
<img src='/images/osmabrowser/shrink.png' alt='zoom out' onclick="zoomout()" />
-->
</div>
<div>
<label for='search'>Search</label>
<input name='search' id='search' />
<input type='button' value='Go!' onclick='placeSearch()'/>
</div>

<div>
<img src="/images/fmapkey.png" alt="Key to map"/>
</div>

</div>
</body>
</html>
