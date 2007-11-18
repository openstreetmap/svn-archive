var c;
var mode=0;
var md;
var map;

function init()
{
	if(navigator.appName=='Microsoft Internet Explorer')
	{
		alert('Unfortunately Freemap does not yet work fully on Internet ' +
			 'Explorer. For the moment, I recommend you use the Firefox ' +
			 '(www.mozilla.com) or Opera (www.opera.com) web browsers.');
	}

	map = new OpenLayers.Map('map',
			{ maxExtent: new OpenLayers.Bounds(-700000,6000000,200000,8100000),
				resolutions: [5,10],
				tileSize: new OpenLayers.Size(500,500),
				  	  units: 'meters' }	
					  	);

	var mapnik = new OpenLayers.Layer.WMS( "Freemap/Mapnik", 
			"http://www.free-map.org.uk/cgi-bin/render",
							{buffer:1} );
	
							
												        
	map.addLayer ( mapnik );

	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/common/marker.php',
   'http://www.free-map.org.uk/freemap/common/marker.php',
   				easting,northing,null,
				"Mercator",
				"distUnits","distTenths","units","resetDist",
				'http://www.free-map.org.uk/freemap/common/ggproxy.php');

	map.addControl (new OpenLayers.Control.LayerSwitcher());

	md = new OpenLayers.Control.MouseDefaults();
	md.setMap(map);

	c.setupEvents();

	c.setDefaultIcon
		('http://www.free-map.org.uk/images/marker.png',
		 new OpenLayers.Size(24,24) );

	c.addIcon ("hazard","Caution or path blockage", 
				'http://www.free-map.org.uk/images/hazardmarker.png',
				new OpenLayers.Size(24,24));
	c.addIcon ("directions", "Path directions",
	'http://www.free-map.org.uk/images/querymarker.png', 
				new OpenLayers.Size(24,24));
	c.addIcon ("info","Interesting place", 
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
	c.setMode(m);
	doBgCols_();
}

function doBgCols_()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).style.backgroundColor = 
			(count==c.mode) ? '#8080ff': '#000080';
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

function toNPE()
{
	alert('toNPE()');
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

	var easting=489600, northing=128500;

	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/common/ajaxserver.php',
   'http://www.free-map.org.uk/freemap/common/georss.php',easting,northing,null,
				"OSGB",
				"distUnits","distTenths","units","resetDist",
				'http://www.free-map.org.uk/freemap/common/ggproxy.php');
}
