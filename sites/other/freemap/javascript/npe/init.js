var c;
var mode=0;

function init()
{
	map = new OpenLayers.Map('map',
			{ maxExtent: new OpenLayers.Bounds(0,0,599999,999999),
				maxResolution:8 ,
				tileSize: new OpenLayers.Size(500,500),
				  	  units: 'meters' }	
					  	);

	var npe = new OpenLayers.Layer.WMS( "New Popular Edition", 
			"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
							{layers:'npe'},{buffer:1} );
	
							
												        
	map.addLayer ( npe );

	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/common/ajaxserver.php',
   'http://www.free-map.org.uk/freemap/common/georss.php',easting,northing,null,
				"OSGB",
				"distUnits","distTenths","units","resetDist",
				'http://www.free-map.org.uk/freemap/common/ggproxy.php');

	map.addControl (new OpenLayers.Control.LayerSwitcher());

	c.setupEvents();

	c.parent.setDefaultIcon
		('http://www.free-map.org.uk/images/marker.png',
		 new OpenLayers.Size(24,24) );

	c.parent.addIcon ("hazard","Caution or path blockage", 
				'http://www.free-map.org.uk/images/hazardmarker.png',
				new OpenLayers.Size(24,24));
	c.parent.addIcon ("directions", "Path directions",
	'http://www.free-map.org.uk/images/querymarker.png', 
				new OpenLayers.Size(24,24));
	c.parent.addIcon ("info","Interesting place", 
		'http://www.free-map.org.uk/images/infomarker.png',
				new OpenLayers.Size(24,24));
	initModeBar();
}

function initModeBar()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).onclick = function(e)
			{ var el = getEventElement(e);
			  setMode_(parseInt(el.id.substring(4))); doBgCols_(); }
		count++;
	}
}

function setMode_(m)
{
	c.parent.setMode(m);
	doBgCols_();
}

function doBgCols_()
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

function placeSearch()
{
	c.placeSearch();
}
