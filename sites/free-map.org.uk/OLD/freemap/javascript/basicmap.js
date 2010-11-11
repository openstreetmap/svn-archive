
var map, walkroute, way, walkrouteLayer, markers;

function loadMap(x,y,convert)
{
	setupMap();

    // x, y will be in ordinary Mercator
    var mcvtr = new converter("Mercator");

    // slippy map will be in google projection
    var gcvtr = new converter("Google");

    var lonLat = (convert==false) ?
		gcvtr.normToCustom(new OpenLayers.LonLat(x,y)):
        gcvtr.normToCustom(mcvtr.customToNorm(new OpenLayers.LonLat(x,y)));
	

    var markers = new OpenLayers.Layer.Markers("Markers");
    map.setCenter(lonLat,15);
    map.addLayer ( markers );
    markers.addMarker(new OpenLayers.Marker(map.getCenter(),
                    new OpenLayers.Icon('/freemap/images/marker.png',
                                new OpenLayers.Size(24,24))));
}

function loadWalkroute(id)
{
	setupMap();
    markers = new OpenLayers.Layer.Markers("Markers");
	map.addLayer(markers);
    walkrouteLayer = new OpenLayers.Layer.Walkroute
        ("Walk Route Layer",this.cvtr);
    map.addLayer(walkrouteLayer);
	walkroute=new Walkroute(id,new converter("Google"));
	walkroute.load(wrCallback);
}

function wrCallback(xmlHTTP)
{
	walkroute.parseXML(xmlHTTP);
	map.zoomToExtent(walkroute.getBounds());
	walkroute.render(walkrouteLayer,markers);
	$('title').innerHTML = walkroute.title;
	$('description').innerHTML = walkroute.description;
	var annotations=walkroute.getAnnotations();
	var html="<ol>";
	for(var count=0; count<annotations.length; count++)
		html += "<li>"+annotations[count].description+"</li>";
	html += "</ol>";
	$('text').innerHTML=html;
}
	
function loadWay(id)
{
	setupMap();
    markers = new OpenLayers.Layer.Markers("Markers");
	map.addLayer(markers);
    walkrouteLayer = new OpenLayers.Layer.Walkroute
        ("Walk Route Layer",this.cvtr);
    map.addLayer(walkrouteLayer);
	way=new Way(new converter("Google"));
	way.setID(id);
	way.load(wayCallback);
}

function wayCallback(xmlHTTP)
{
	way.parseXML(xmlHTTP.responseXML.getElementsByTagName('way')[0]);
	map.zoomToExtent(way.getBounds());
	way.render(walkrouteLayer,markers);
}

function setupMap()
{

    map = new OpenLayers.Map('map',
            { maxExtent: new OpenLayers.Bounds(-20037508.34,
                    -20037508.34,20037508.34,20037508.34),
                    maxResolution:156543,
                    numZoomLevels:18,
                    units:'meters',
                    projection:'EPSG:41001'});

    var mapnik = new OpenLayers.Layer.TMS( "Freemap", 
            "",
            {type:'png',getURL:get_osm_url,displayOutsideMaxExtent:true},
                            {buffer:1} );
    
    map.addLayer ( mapnik );
	map.addControl(new OpenLayers.Control.LayerSwitcher());
}

