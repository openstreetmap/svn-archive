<?php
$lat = (isset($_GET['lat'])) ? $_GET['lat']:51.05;
$lon = (isset($_GET['lon'])) ? $_GET['lon']:-0.72;
$zoom = (isset($_GET['lon'])) ? $_GET['zoom']:8;
?>
<html>
<head>
    <title>Freemap 0.5</title>

    <!-- based on example on osm wiki -->

    <!-- bring in the OpenLayers javascript library
         (here we bring it from the remote site, but you could
         easily serve up this javascript yourself) -->
    <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
 
    <!-- bring in the OpenStreetMap OpenLayers layers.
         Using this hosted file will make sure we are kept up
         to date with any necessary changes -->
    <script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js">
	</script>
 
    <script type="text/javascript">
        // Start position for the map 
        var lat=<?php echo $lat; ?>;
        var lon=<?php echo $lon; ?>;
        var zoom=<?php echo $zoom;?>;
 
        var map; //complex object of type OpenLayers.Map
 
        //Initialise the 'map' object
        function init() {
 
            map = new OpenLayers.Map ("map", {
                controls:[
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.LayerSwitcher(),
                    new OpenLayers.Control.PanZoomBar(),
                    new OpenLayers.Control.Attribution()],
                maxExtent: new OpenLayers.Bounds
				(-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 15,
                units: 'm',
                projection: new OpenLayers.Projection("EPSG:900913"),
                displayProjection: new OpenLayers.Projection("EPSG:4326")
            } );
 
 
            // Define the map layer
            layerFreemap = new OpenLayers.Layer.OSM
				("Freemap [new]",
				"http://www.free-map.org.uk/images/tiles/"+
				"${z}/${x}/${y}.png",{numZoomLevels:15} );
            map.addLayer(layerFreemap);
 
            if( ! map.getCenter() ){
                var lonLat = new 
				OpenLayers.LonLat(lon, lat).transform
				(new OpenLayers.Projection("EPSG:4326"), 
				map.getProjectionObject());
                map.setCenter (lonLat, zoom);
            }
        }
 
    </script>
</head>
 
<!-- body.onload is called once the page is loaded (call the init function) -->
<body onload="init();">
 
<!-- define a DIV into which the map will appear. Make it take up the whole 
window -->
    <div style="width:100%; height:100%" id="map"></div>
 
</body>
 
</html>
