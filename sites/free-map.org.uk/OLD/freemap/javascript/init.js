var c;
var mode=0;
var md;
var map;


function init()
{
    map = new OpenLayers.Map('map',
            { maxExtent: new OpenLayers.Bounds(-20037508.34,
                    -20037508.34,20037508.34,20037508.34),
                    maxResolution:156543,
					numZoomLevels:18,
                    units:'meters',
                    projection:'EPSG:41001'});

    var mapnik = new OpenLayers.Layer.TMS( "Testing", 
            "",
            {type:'png',getURL:get_osm_url,displayOutsideMaxExtent:true},
                            {buffer:1} );
    
                            
                                                        
    map.addLayer ( mapnik );
	map.setCenter(new OpenLayers.LonLat(easting,northing),zoom);


	c = new FreemapClient(map, 
	'http://www.free-map.org.uk/freemap/api/markers.php',
   'http://www.free-map.org.uk/freemap/api/markers.php',
				easting,northing,zoom,loggedIn,
				"Google",
				"distUnits","distTenths","units","resetDist");
	
	c.addGeographLayer('http://www.free-map.org.uk/freemap/ggproxy.php');


	c.resetInfoPanel();
	c.activateOSMLookup();
	
	map.addControl (new OpenLayers.Control.LayerSwitcher());

	md = new OpenLayers.Control.MouseDefaults();
	md.setMap(map);


	c.setupEvents();

	c.setDefaultIcon
		('http://www.free-map.org.uk/freemap/images/marker.png',
		 new OpenLayers.Size(16,16) );

	c.addIcon ("hazard","Caution or path blockage", 
				'http://www.free-map.org.uk/freemap/images/hazardmarker.png',
				new OpenLayers.Size(16,16));
	c.addIcon ("directions", "Path directions",
	'http://www.free-map.org.uk/freemap/images/querymarker.png', 
				new OpenLayers.Size(16,16));
	c.addIcon ("info","Interesting place", 
		'http://www.free-map.org.uk/freemap/images/infomarker.png',
				new OpenLayers.Size(16,16));
	c.addIcon ("photo","Photo", 
		'http://www.free-map.org.uk/freemap/images/infomarker.png',
				new OpenLayers.Size(16,16));
	
	document.getElementById('mapkey').onclick = c.showKey.bind(c);
	document.getElementById('nearby').onclick = c.getNearby.bind(c);
	initModeBar();
}

function initModeBar()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).onclick = function(e)
			{ var el = getEventElement(e ? e:window.event);
			  setMode(parseInt(el.id.substring(4))); doBgCols(); }
		count++;
	}
}

function setMode(m)
{
	c.setMode(m);
	doBgCols();
}

function doBgCols()
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
