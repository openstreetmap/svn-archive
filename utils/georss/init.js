var c;

function init()
{
	map = new OpenLayers.Map( "map", 
				{maxExtent: 
				new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,
				20037508.34), maxResolution:156543, units:'meters', 
				projection: "EPSG:41001"} );
	var osmdev = new OpenLayers.Layer.LikeGoogle
	( "OSM", "http://artem.dev.openstreetmap.org/osm_tiles/", {type:'png'} );
					        
	map.addLayer ( osmdev );
	c = new GeoRSSClient(map, 
	'http://www.free-map.org.uk/common/ajaxserver.php',
	'http://www.free-map.org.uk/common/georss.php', -0.72,51.05,14,"GOOG");
	map.addControl (new OpenLayers.Control.LayerSwitcher());

	c.setDefaultIcon
		('http://www.free-map.org.uk/images/marker.png',
		 new OpenLayers.Size(24,24) );

	c.addIcon ("hazard", 
				'http://www.free-map.org.uk/images/hazard.png',
				new OpenLayers.Size(24,24));
	c.addIcon ("view", 
	'http://www.free-map.org.uk/images/view.png', 
				new OpenLayers.Size(24,24));
	c.addIcon ("meeting", 
		'http://www.free-map.org.uk/images/meeting.png',
				new OpenLayers.Size(24,24));
	c.addIcon ("other", 
		'http://www.free-map.org.uk/images/marker.png',
				new OpenLayers.Size(24,24));
	document.getElementById('mode0').onclick = function() { c.setMode(0) };
	document.getElementById('mode1').onclick = function() { c.setMode(1) };
	document.getElementById('mode2').onclick = function() { c.setMode(2) };
}
