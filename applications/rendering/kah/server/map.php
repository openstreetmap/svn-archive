<html>
<head>
  <script src="http://openlayers.org/api/OpenLayers.js"></script>
  <script src="http://openstreetmap.org/openlayers/OpenStreetMap.js"></script>

  <script type="text/javascript">
  <?php
    printf("var zoom = %d;\n", $_GET['zoom']);
    printf("var lat = %f;\n", $_GET['lat']);
    printf("var lon = %f;\n", $_GET['lon']);
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
  function init()
  {
    extraUrlParams = "";
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
    //map.addLayer(layerTilesAtHome);

    <?php
    include("config.php");
    foreach(getLayers() as $Layer)
      {
      printf("layer = new OpenLayers.Layer.OSM(\"%s (>z12)\",\n", $Layer);
      printf("\"./?layer=%s&loc=\",\n", $Layer);
      printf("{\nisBaseLayer: true,\ntype:'png',\n},\n");
      printf("{'buffer':1});\n");
      printf("map.addLayer(layer);\n");
      }
    ?>

    var lonLat = new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection("EPSG:900913"));

    map.setCenter(lonLat, zoom);
  }
        
</script>
<title>OpenStreetMap Kosmos layers</title>
</head><body onload="init();">

<div style="width:100%; height:100%" id="map">

</div>

</body>
</html>