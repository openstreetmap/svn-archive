<?php
if(!array_key_exists('gpx', $_GET))
{
  # No GPX requested - so redirect to an index where one can be selected
  header("Location:list.php");
  exit;
}

# By default, use lat/long/zoom from query parameters
$z = floor($_GET['zoom'] + 0);
$lat = $_GET['lat'] + 0;
$lon = $_GET['lon'] + 0;

# However, if they don't exist:
if(!array_key_exists('lat', $_GET))
{
  # Lat/long unknown, so look it up from the GPX file rather than
  # just starting with a view of the whole world!
  
  include("gpx.php");
  $LeaveFilepointerOpen = 1;
  $Data = getMeta(getGpx($_GET["gpx"] + 0), $LeaveFilepointerOpen);
  
  if(!$Data['exists'] || !$Data['valid'])
    {
    header("Location:list.php?error=nofile");
    exit;
    }
  if($Data['points'] < 1)
    {
    header("Location:list.php?error=nopoints");
    exit;
    }
  $fp = $Data['fp'];
  
  # Read the first point
  list($spare, $rx, $ry) = unpack("N2", fread($fp, 8));
  $Resolution = 1 / pow(2.0, 31);
  $rx *= $Resolution;
  $ry *= $Resolution;
  
  # Convert to lat/long
  $lat = mercatorToLat(M_PI * (1.0 - 2.0 * $ry));
  $lon = -180.0 + 360.0 * $rx;
  $z = 14;
  
  fclose($fp);
}
?>
<html>
<head>
    <script src="http://openlayers.org/api/OpenLayers.js"></script> 
    <script src="http://openstreetmap.org/openlayers/OpenStreetMap.js"></script>

    <script type="text/javascript">
        <?php

        print " var lat = $lat;\n var lon = $lon;\n var zoom = $z;\n";
        
        $Base = '';
        $Title = "OpenStreetMap tracklog viewer";
        $Tiles = 'tile.php?';
        $gpx = 0;
        if(array_key_exists('gpx', $_GET))
        {
          $gpx = floor($_GET['gpx'] + 0);
          $Base = sprintf("?gpx=%d", $gpx);
          $Tiles .= sprintf("gpx=%d&t=", $gpx);
          $Title = sprintf("Tracklog #%d", $gpx);
        }
          #$Tiles .= sprintf("t=", $gpx);
        
        print "var routeServer = '$Tiles'\n";
        print "var extraUrlParams = '$Base';\n";
        ?>
        
	if (zoom==0)
	{
	 zoom = 2;
	 lon = 1.0996;
	 lat = 35.5862;
	}

	lat=parseFloat(lat)
	lon=parseFloat(lon)
	zoom=parseInt(zoom)
	        
        var map; //complex object of type OpenLayers.Map 

        //Initialise the 'map' object
        function init() {
          
            map = new OpenLayers.Map ("map", {
                controls:[
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.Permalink('',extraUrlParams,''),
                     new OpenLayers.Control.LayerSwitcher(),
                    new OpenLayers.Control.PanZoomBar()],
                maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 19,
                units: 'meters',
                projection: new OpenLayers.Projection("EPSG:900913"),
                displayProjection: new OpenLayers.Projection("EPSG:4326")
            } );
                
            
            // Base map
            layerTilesAtHome = new OpenLayers.Layer.OSM.Osmarender("Osmarender");

            // GPX overlay
            route = new OpenLayers.Layer.OSM(
              "Route",
              routeServer,
              {
                isBaseLayer: false, 
                type:'png', 
              },
              {'buffer':1});


            map.addLayer(layerTilesAtHome);
            map.addLayer(route);

            var lonLat = new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));

            map.setCenter (lonLat, zoom);
        }
        
    </script>
    
<?php
print "<title>$Title</title>\n";
?>
</head><body onload="init();">

<div style="width:100%; height:100%" id="map">
<!-- <div style="position:absolute; bottom:10px;width:700px;"><form action='./' method='get'>
<input type='text' size='10' name='gpx' value='<?php print $gpx ?>' />
<input type='submit' value='View GPX' />
<a href="http://openstreetmap.org/traces/">List of GPX traces</a>
</form></div> -->

</div>

</body>
</html>