var c;
var mode=0;

function init()
{
	map = new OpenLayers.Map('map',
	{ maxExtent: new OpenLayers.Bounds (0,0,599999,999999),
	  maxResolution : 8, 
	  units: 'meters' }	
	);

	var npe = new OpenLayers.Layer.WMS( "New Popular Edition", 
		"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
		{'layers': 'npe'},{buffer:1} );
							
												        
	map.addLayer ( npe );

	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/common/modifyOSM.php',
	'http://www.free-map.org.uk/freemap/common/fetch.php',easting,northing,null,
				"OSGB",
				"distUnits","distTenths","units","resetDist",null);

	map.addControl (new OpenLayers.Control.LayerSwitcher());

	c.setupEvents();

	c.parent.setDefaultIcon
		('http://www.free-map.org.uk/images/marker.png',
		 new OpenLayers.Size(24,24) );

	c.parent.addIcon ("pub", 
				'http://www.free-map.org.uk/images/pub.png',
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("viewpoint", 
	'http://www.free-map.org.uk/images/viewpoint.png', 
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("farm", 
	'http://www.free-map.org.uk/images/farm.png', 
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("church", 
	'http://www.free-map.org.uk/images/church.png', 
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("peak", 
		'http://www.free-map.org.uk/images/POIeditor/peak.png',
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("hamlet", 
		'http://www.free-map.org.uk/images/POIeditor/place.png',
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("village", 
		'http://www.free-map.org.uk/images/POIeditor/place.png',
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("town", 
		'http://www.free-map.org.uk/images/POIeditor/place.png',
				new OpenLayers.Size(16,16));
	c.parent.addIcon ("city", 
		'http://www.free-map.org.uk/images/POIeditor/place.png',
				new OpenLayers.Size(16,16));
	initModeBar();
}

function initModeBar()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).onclick = function(e)
			{ var el = getEventElement(e);
			  setMode(parseInt(el.id.substring(4))); doBgCols(); }
		count++;
	}
}

function setMode(m)
{
	c.parent.setMode(m);
}

function doBgCols()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).style.backgroundColor = 
			(count==c.parent.mode) ? '#8080ff': '#000080';
		count++;
	}
}

function getEventElement(e)
{
	if(e.srcElement)
		return e.srcElement;
	return e.target;
}
