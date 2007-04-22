var cvtr = new converter("OSGB");
var canvasLayer;

function init()
{
	
	// create map
	// create npe layer

	map = new OpenLayers.Map('map',
	{ maxExtent: new OpenLayers.Bounds (0,0,599999,999999),
	  maxResolution : 8, 
	  units: 'meters' }	
	);
	
					        

	var npe = new OpenLayers.Layer.WMS( "New Popular Edition", 
		"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
		{'layers': 'npe'},{buffer:1} );

	map.addLayer(npe);

	map.events.register('mouseup',map,simpleMouseUpHandler );

	document.getElementById("searchButton").onclick = placeSearch;

	map.setCenter(new OpenLayers.LonLat(easting,northing));
	map.addControl (new OpenLayers.Control.LayerSwitcher());
}


