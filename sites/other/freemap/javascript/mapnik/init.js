var c;
var mode=0;

function init()
{
	map = new OpenLayers.Map('map',
			{ maxExtent: new OpenLayers.Bounds(-700000,6500000,200000,8100000),
				resolutions: [10],
				tileSize: new OpenLayers.Size(500,500),
				  	  units: 'meters' }	
					  	);

	var mapnik = new OpenLayers.Layer.WMS( "Freemap/Mapnik", 
			"http://www.free-map.org.uk/cgi-bin/render",
							{buffer:1} );
	
							
												        
	map.addLayer ( mapnik );

	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/common/ajaxserver.php',
   'http://www.free-map.org.uk/freemap/common/georss.php',easting,northing,null,
				"Mercator",
				"distUnits","distTenths","units","resetDist",
				'http://www.free-map.org.uk/freemap/common/ggproxy.php');

	map.addControl (new OpenLayers.Control.LayerSwitcher());

	c.setupEvents();

	c.parent.setDefaultIcon
		('http://www.free-map.org.uk/images/marker.png',
		 new OpenLayers.Size(24,24) );

	c.parent.addIcon ("hazard", 
				'http://www.free-map.org.uk/images/hazard.png',
				new OpenLayers.Size(24,24));
	c.parent.addIcon ("view", 
	'http://www.free-map.org.uk/images/view.png', 
				new OpenLayers.Size(24,24));
	c.parent.addIcon ("meeting", 
		'http://www.free-map.org.uk/images/meeting.png',
				new OpenLayers.Size(24,24));
	c.parent.addIcon ("other", 
		'http://www.free-map.org.uk/images/marker.png',
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
	alert('placeSearch');
	c.placeSearch();
}
