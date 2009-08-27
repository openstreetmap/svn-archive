// Waypoint class for storing all waypoint related data (marker, route to
// next way point)
function Waypoint()
{
	this.set_prev= function(prev)
	{
		this.prev= prev;

		this.style= style_mid;
		if (this.prev == undefined)
			this.style= style_from;
		else if (this.next == undefined)
			this.style= style_to;
	}

	this.set_next= function(next)
	{
		this.next= next;

		this.style= style_mid;
		if (this.prev == undefined)
			this.style= style_from;
		else if (this.next == undefined)
			this.style= style_to;
	}

	this.set_position= function(lonlat)
	{
		this.clear_position();

		var marker_url= 'markers/marker-yellow.png';
		if (this.prev == undefined)
			marker_url= 'markers/marker-green.png';
		if (this.next == undefined)
			marker_url= 'markers/marker-red.png';
		var size = new OpenLayers.Size(21,25);
		var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);

		var icon = new OpenLayers.Icon(marker_url,
			size,offset);
		this.marker = new OpenLayers.Marker(lonlat.clone(), icon);
		markers.addMarker(this.marker);

		if (this.pos_el != undefined)
		{
			map_ll= lonlat.clone();
			map_ll.transform(map.projection,
				map.displayProjection);

			precision= 6;
			str= map_ll.lat.toFixed(precision) + ', ' +
				map_ll.lon.toFixed(precision);
			this.pos_el.innerHTML= str;
		}
	}
	this.clear_position= function()
	{
		if (this.marker != undefined)
		{
			markers.removeMarker(this.marker);
			this.marker.destroy();
			this.marker= undefined;

			this.remove_route();
			if (this.prev != undefined)
				this.prev.remove_route();
			if (this.pos_el != undefined)
				this.pos_el.innerHTML= "";
		}
	}
	this.set_route= function(routelist)
	{
		if (this.route != undefined)
		{
			alert('Waypoint.set_route: should update route');
			return;
		}
		this.route= routelist;
	}
	this.set_distance= function(distance)
	{
		this.distance= parseFloat(distance);
	}
	this.remove_route= function()
	{
		if (this.route == undefined)
			return;
		for (i= 0; i<this.route.length; i++)
		{
			routelayer.removeFeatures(this.route[i]);
		}
		this.route= undefined;
		this.distance= undefined;
	}
	this.set_button_el= function(element)
	{
		this.button_el= element;
	}
	this.set_txt_el= function(element)
	{
		this.txt_el= element;
	}
	this.set_pos_el= function(element)
	{
		this.pos_el= element;
	}
	this.set_tr_el= function(element)
	{
		this.tr_el= element;
	}
	this.destroy= function()
	{
		// Delete routes related to this waypoint, delete marker,
		// remove the waypoint from the list and clear some fields.

		prev= this.prev;
		next= this.next;

		// Remove marker (also removes routes)
		this.clear_position();

		// Unlink 
		if (prev != undefined)
			prev.set_next(next);
		if (next != undefined)
			next.set_prev(prev);
		this.prev= undefined;
		this.next= undefined;

		this.button_el= undefined;
		this.txt_el= undefined;
		this.pos_el= undefined;
		if (this.tr_el != undefined)
		{
			tr= this.tr_el;
			tr.parentNode.removeChild(tr);
			this.tr_el= undefined;
		}
	}
}

var lon = 5;
var lat = 40;
var zoom = 5;
var map, layer, markers, routelayer;

// List of waypoints. The list starts at 'fromWaypoint' and ends with
// 'toWaypoint'
var fromWaypoint;
var toWaypoint;

var position;
var currWaypoint;
var currRouteSrc;
var currRouteParams;
var currBounds;

/*
xmlhttp keeps track of an AJAX call.
xmlhttp['object'] = The AJAX JavaScript object that does all the work.
xmlhttp['what'] = Indicates what kind of request we're performing (e.g. NameFinder 'name' query or Gosmore 'route' query).
xmlhttp['url'] = The URL to query.
*/
var xmlhttp = new Array();

// Styles for drawing lines in different colors to and from the actual waypoint
// markers.
style_from = {
    strokeColor: "#00FF00",
    strokeWidth: 3,
    //strokeDashstyle: "dashdot",
    //pointRadius: 6,
    //pointerEvents: "visiblePainted"
};
style_mid = {
    strokeColor: "#FFFF00",
    strokeWidth: 3,
    //strokeDashstyle: "dashdot",
    //pointRadius: 6,
    //pointerEvents: "visiblePainted"
};
style_to = {
    strokeColor: "#FF0000",
    strokeWidth: 3,
    //strokeDashstyle: "dashdot",
    //pointRadius: 6,
    //pointerEvents: "visiblePainted"
};

function init_to_from()
{
	// Create and link initial (from and to) waypoints
	fromWaypoint= new Waypoint();
	toWaypoint= new Waypoint();
	fromWaypoint.set_prev(undefined);
	fromWaypoint.set_next(toWaypoint);
	toWaypoint.set_prev(fromWaypoint);
	toWaypoint.set_next(undefined);

	fromWaypoint.set_button_el(document.forms['route'].elements['from']);
	toWaypoint.set_button_el(document.forms['route'].elements['to']);
	fromWaypoint.set_txt_el(document.forms['route'].elements['from_text']);
	toWaypoint.set_txt_el(document.forms['route'].elements['to_text']);

	pos_td= document.getElementById('from_pos');
	fromWaypoint.set_pos_el(pos_td);

	pos_td= document.getElementById('to_pos');
	toWaypoint.set_pos_el(pos_td);
};

function init(){
	init_to_from();

	OpenLayers.Feature.Vector.style['default']['strokeWidth'] = '2';
	OpenLayers.Feature.Vector.style['default']['fillColor'] = '#0000FF';
	OpenLayers.Feature.Vector.style['default']['strokeColor'] = '#0000FF';
	
	// Map definition based on http://wiki.openstreetmap.org/index.php/OpenLayers_Simple_Example
	map = new OpenLayers.Map ("map", {
                controls:[
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.PanZoomBar(),
                    new OpenLayers.Control.LayerSwitcher(),
                    new OpenLayers.Control.Attribution()
				],
				eventListeners: {
			        //"moveend": mapEvent,
			        //"zoomend": mapEvent,
			        //"changelayer": mapLayerChanged,
			        "changebaselayer": onChangeBaseLayer
					},
                maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 20,
                units: 'm',
                projection: new OpenLayers.Projection("EPSG:900913"),
                displayProjection: new OpenLayers.Projection("EPSG:4326")
            } );
	
	//map.events.register("changebaselayer", map, onChangeBaseLayer(this));
    		
	layerMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
	layerCycle = new OpenLayers.Layer.OSM.CycleMap("Cycle Map", {
					 displayOutsideMaxExtent: true,
					 wrapDateLine: true
					 });
					 
	layerCycleNetworks = new OpenLayers.Layer.OSM.CycleMap("Cycle Networks", {
					 displayOutsideMaxExtent: true,
					 wrapDateLine: true
					 });

	layerTest = new OpenLayers.Layer.OSM.Mapnik("Test");
	
	markers = new OpenLayers.Layer.Markers("Markers",
		{
			projection: new OpenLayers.Projection("EPSG:4326"),
			'calculateInRange':	function() { return true; }
		}
	);
	
	routelayer = new OpenLayers.Layer.Vector("Route");
	
	map.addLayers([markers, routelayer]);
	
	// Check if a permalink is used
	if (location.search.length > 0) {
		// Add the To/From markers
		var flonlat = new OpenLayers.LonLat();
		var tlonlat = new OpenLayers.LonLat();
		var wlonlat = new OpenLayers.LonLat();
		params = location.search.substr(1).split('&');
		for (i = 0; i < params.length; i++) {
			fields = params[i].split('='); 
			
			switch (fields[0]) {
			case 'flat':
				flonlat.lat = parseFloat(fields[1]);
				break;
			case 'flon':
				flonlat.lon = parseFloat(fields[1]);
				fromWaypoint.set_position(flonlat.transform(
					map.displayProjection, map.projection));
				break;
			case 'wlat':
				wlonlat.lat = parseFloat(fields[1]);
				break;
			case 'wlon':
				wlonlat.lon = parseFloat(fields[1]);
				// Create new waypoint 
				addWaypoint();
				toWaypoint.prev.set_position(
					wlonlat.transform(
					map.displayProjection,
					map.projection));
			case 'tlat':
				tlonlat.lat = parseFloat(fields[1]);
				break;
			case 'tlon':
				tlonlat.lon = parseFloat(fields[1]);
				toWaypoint.set_position(tlonlat.transform(
					map.displayProjection, map.projection));
				break;
			case 'v':
				switch (fields[1]) {
				case 'bicycle':
					for (j = 0; j < document.forms['parameters'].type.length; j++) {
						if (document.forms['parameters'].type[j].value == 'bicycle') {
							document.forms['parameters'].type[j].checked = true;
						}
					}
					break;
				case 'foot':
					for (j = 0; j < document.forms['parameters'].type.length; j++) {
						if (document.forms['parameters'].type[j].value == 'foot') {
							document.forms['parameters'].type[j].checked = true;
						}
					}
					break;
				default:
					for (j = 0; j < document.forms['parameters'].type.length; j++) {
						if (document.forms['parameters'].type[j].value == 'car') {
							document.forms['parameters'].type[j].checked = true;
						}
					}
					break;
				}
				break;
			case 'fast':
				if (parseInt(fields[1]) == 1) {
					for (j = 0; j < document.forms['parameters'].method.length; j++) {
						if (document.forms['parameters'].method[j].value == 'fast') {
							document.forms['parameters'].method[j].checked = true;
						}
					}
				} else {
					for (j = 0; j < document.forms['parameters'].method.length; j++) {
						if (document.forms['parameters'].method[j].value == 'short') {
							document.forms['parameters'].method[j].checked = true;
						}
					}
				}				
				break;	
			case 'layer':
				switch (fields[1]) {
				case 'cycle':
					map.addLayers([layerCycle, layerMapnik, layerCycleNetworks, layerTest]);
					break;
				case 'cn':
					map.addLayers([layerCycleNetworks, layerMapnik, layerCycle, layerTest]);
					break;
				case 'test':
					map.addLayers([layerTest, layerMapnik, layerCycle, layerCycleNetworks]);
					break;
				default:
					map.addLayers([layerMapnik, layerCycle, layerCycleNetworks, layerTest]);
					break;
				}
			}
		}
		if (null == map.baseLayer) {
			//Fallback for old permalinks that don't list the layer property
			map.addLayers([layerMapnik, layerCycle, layerCycleNetworks, layerTest]);
		}
		

		map.zoomToExtent(markers.getDataExtent());
		
		document.forms['route'].elements['clear'].disabled = false;
		document.forms['route'].elements['calculate'].disabled = true;
		document.forms['route'].elements['to'].disabled = false;
		document.forms['route'].elements['from'].disabled = false;

		calculateRoute();
	} else {
		//No preference for any layer, load Mapnik layer first
		map.addLayers([layerMapnik, layerCycle, layerCycleNetworks, layerTest]);
		
		if (!map.getCenter()) {
			var pos;
			pos = new OpenLayers.LonLat(5, 45);
			map.setCenter(pos.transform(map.displayProjection,map.projection), 3);
		} 
		if (typeof(document.baseURI) != 'undefined') {
			if (document.baseURI.indexOf('devel') > 0) {
				// Zoom in automatically to save some time when developing
				var pos;
				pos = new OpenLayers.LonLat(6, 52.2);
				map.setCenter(pos.transform(map.displayProjection,map.projection), 14);
			}
	    }
		
		document.forms['route'].elements['clear'].disabled = true;
		document.forms['route'].elements['calculate'].disabled = true;
		document.forms['route'].elements['to'].disabled = false;
		document.forms['route'].elements['from'].disabled = false;
	}
	
    var control = new OpenLayers.Control.SelectFeature(routelayer,
	        {
	            clickout: true, toggle: false,
	            multiple: false, hover: false,
	            toggleKey: "ctrlKey", 			// ctrl key removes from selection
	            multipleKey: "shiftKey"			// shift key adds to selection
	        }
	    );
	
	map.addControl(control);
	
	var click = new OpenLayers.Control.Click();
    map.addControl(click);
	
	PermaLink = new OpenLayers.Control.Permalink('test', 'http://www.openstreetmap.org/edit');
	map.addControl(PermaLink);
	PermaLink.element.textContent = 'Edit map';
    click.activate();
} //End of init()

function get_osm_url (bounds) {
    var res = this.map.getResolution();
    var x = Math.round ((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round ((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();
    var path = z + "/" + x + "/" + y + "." + this.type;
    var url = this.url;
    if (url instanceof Array) {
        url = this.selectUrl(path, url);
    }
    return url + path;
}

// Called when the baselayer is changed
function onChangeBaseLayer(e) {
	if (undefined != map.baseLayer) {
		//alert('Baselayer changed'+map.baseLayer.name);
		switch (map.baseLayer.name) {
		case 'Cycle Networks':
			for (j = 0; j < document.forms['parameters'].type.length; j++) {
				if (document.forms['parameters'].type[j].value == 'foot' || document.forms['parameters'].type[j].value == 'motorcar') {
					document.forms['parameters'].type[j].disabled = true;
				}
				if (document.forms['parameters'].type[j].value == 'bicycle')
					document.forms['parameters'].type[j].checked = true;
			}
			break;
		default:
			for (j = 0; j < document.forms['parameters'].type.length; j++) {
				if (document.forms['parameters'].type[j].value == 'foot' 
					|| document.forms['parameters'].type[j].value == 'bicycle'
					|| document.forms['parameters'].type[j].value == 'motorcar') {
					document.forms['parameters'].type[j].disabled = false;
				}
			}
			break;
		}
	}
}

// Deternines what happens if a user clicks the map
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {                
	defaultHandlerOptions: {
		'single': true,
		'double': false,
		'pixelTolerance': 0,
		'stopSingle': false,
		'stopDouble': false
	},

		// Initialize is called when the Click control is activated
		// It sets the behavior of a click on the map
		initialize: function(options) {
			this.handlerOptions = OpenLayers.Util.extend(
				{}, this.defaultHandlerOptions
			);
			OpenLayers.Control.prototype.initialize.apply(
				this, arguments
			); 
			this.handler = new OpenLayers.Handler.Click(
				this, {
				    'click': this.trigger
				}, this.handlerOptions
			);
		}, 

		// Trigger is called when a user clicks on the map
	    trigger: function(e) {
	    	position = this.map.getLonLatFromViewPortPx(e.xy);
	    	if (currWaypoint != undefined) {
				currWaypoint.set_position(position);
				currWaypoint.button_el.disabled= false;
				currWaypoint = undefined;
				document.forms['route'].elements['clear'].disabled =
					false;
				calculateRoute();
			}
			// Maybe add a new marker by default if none selected?
	    }
	}
);

/*
function editMap() {
	lonlat = map.getCenter().transform(map.projection, map.displayProjection);
	zoom = map.getZoom();
	
	window.open('http://www.openstreetmap.org/edit?lat='+lonlat.lat+'&lon='+lonlat.lon+'&zoom='+zoom);
}
*/
function reverseRoute(element) {
	to = document.forms['route'].elements['to_text'].value;
	document.forms['route'].elements['to_text'].value = document.forms['route'].elements['from_text'].value;
	document.forms['route'].elements['from_text'].value = to;

	// Create a list of positions
	plist= []
	for (wp= toWaypoint; wp != undefined; wp= wp.prev)
	{
		if (wp.marker == undefined)
			plist.push(undefined);
		else
			plist.push(wp.marker.lonlat.clone());
	}

	var i;

	// And set markers to new positions
	for (wp= fromWaypoint, i= 0; wp != undefined; wp= wp.next, i++)
	{
		if (plist[i] == undefined)
			wp.clear_position();
		else
			wp.set_position(plist[i]);
	}

	calculateRoute();
}

// Prepare to do the magic
function calculateRoute() {
	// Make sure that we have all required locations
	for (wp= fromWaypoint; wp != undefined; wp= wp.next)
	{
		if (wp.marker == undefined)
		{
			return;
		}
	}

	document.forms['route'].elements['calculate'].disabled = true;
	for (wp= fromWaypoint; wp != undefined; wp= wp.next)
	{
		if (wp.button_el != undefined)
			wp.button_el.disabled= true;
	}
	document.forms['route'].elements['to_text'].disabled = true;
	document.forms['route'].elements['from_text'].disabled = true;
	
	loadGmlLayer();
}

function loadGmlLayer(flonlat, tlonlat) {
	// First check if the routing parameters have changed
	var routeParams='';
	for (i = 0; i < document.forms['parameters'].elements.length; i++) {
		element = document.forms['parameters'].elements[i];
		switch (element.name) {
		case 'type':
			if (element.checked == true) {
				routeParams += '&v='+element.value;
			}
			break;
		case 'method':
			if (element.value == 'fast' &&
				element.checked == true) {
				routeParams += '&fast=1';
			} else if (element.value == 'short' &&
				element.checked == true) {
				routeParams += '&fast=0';
			}
			break;
		}
	}
	
	//Layer
	for (i = 0; i < map.layers.length; i++) {
		if (map.layers[i].visibility == true) {
			switch (map.layers[i].name) {
			case 'Cycle Networks':
				routeParams += '&layer=cn';
				break;
			case 'Cycle Map':
				routeParams += '&layer=cycle';
				break;
			case 'Mapnik':
				routeParams += '&layer=mapnik';
				break;
			case 'Test':
				routeParams += '&layer=test';
				break;
			}
		}
	}

	if (currRouteParams == undefined)
		currRouteParams= routeParams;
	else if (currRouteParams != routeParams)
	{
		// Clear existing routes
		for (wp = fromWaypoint; wp != undefined; wp= wp.next)
			wp.remove_route();

		currRouteParams= routeParams;
	}
		
	for (wp= fromWaypoint; wp.next != undefined;
		wp= wp.next)
	{
		if (wp.route == undefined)
			break;
	}
	if (wp.next == undefined)
	{
		// We are done
		for (wp= fromWaypoint; wp != undefined; wp= wp.next)
		{
			if (wp.button_el != undefined)
				wp.button_el.disabled= false;
		}
		document.forms['route'].elements['from_text'].disabled =
			false;
		document.forms['route'].elements['to_text'].disabled = false;
		document.forms['route'].elements['calculate'].disabled =
			false;

		currBounds= undefined;

		return;
	}

	currRouteSrc= wp;

	m_from= currRouteSrc.marker;
	m_to= currRouteSrc.next.marker;

	flonlat= m_from.lonlat.clone().transform(map.projection, map.displayProjection);
	tlonlat= m_to.lonlat.clone().transform(map.projection, map.displayProjection);

	html = "Calculating route...please wait (for a max. of 1.5 minutes)";
	OpenLayers.Util.getElement('status').innerHTML = html;
	
	routeURL = '?flat='+roundNumber(flonlat.lat, 6)+'&flon='+roundNumber(flonlat.lon, 6)+'&tlat='+roundNumber(tlonlat.lat, 6)+'&tlon='+roundNumber(tlonlat.lon, 6);
	routeURL += routeParams;
	
	xmlhttp['url'] = 'api/1.0/gosmore.php'+routeURL;
	xmlhttp['what'] = 'route';
	loadxmldoc(xmlhttp);
}

function addRouteLayer(vector, distance) {
	if (typeof(routelayer) != 'undefined') {
		routelayer.onFeatureInsert = function(feature) {
			feature_info(feature, distance);
		}

		sf= vector[0].geometry.getVertices(true);
		f= sf[0];
		t= sf[1];

		flonlat= currRouteSrc.marker.lonlat;
		fstyle= currRouteSrc.style;
		tlonlat= currRouteSrc.next.marker.lonlat;
		tstyle= currRouteSrc.next.style;

		fm= new OpenLayers.Geometry.Point(flonlat.lon, flonlat.lat);
		tm= new OpenLayers.Geometry.Point(tlonlat.lon, tlonlat.lat);
		fgeom= new OpenLayers.Geometry.LineString([fm,f])
		tgeom= new OpenLayers.Geometry.LineString([t,tm])

		fv= new OpenLayers.Feature.Vector(fgeom,null,fstyle);
		tv= new OpenLayers.Feature.Vector(tgeom,null,tstyle);

		currRouteSrc.set_route([vector, fv, tv]);
		currRouteSrc.set_distance(distance);

		routelayer.addFeatures(fv);
		routelayer.addFeatures(tv);
		routelayer.addFeatures(vector);

		currRouteSrc= undefined;
	}
}

//var distance;
var nodes;
function processRouteXML(request) {
	
	if (request.responseText == "" && request.responseXML == null) {
		alert('No route found!');
		return;
	} 
	
	var doc = request.responseXML;

	if (!doc || !doc.documentElement) {
		alert('text');
		doc = request.responseText;
	}
	var format = new OpenLayers.Format.XML();
	nodes = format.getElementsByTagNameNS(doc, 'http://earth.google.com/kml/2.0', 'distance');
	var distance = format.getChildValue(nodes[0]);
	//var distance = format.getChildValue(nodes[0], 'unknown'); //due to changed behavior of OpenLayers this line is rewritten
    //alert("1"+distance);
	
    var options = {};
	options.externalProjection = map.displayProjection;
	options.internalProjection = map.projection;
        
	var kml = new OpenLayers.Format.KML(options);
	var vect = kml.read(doc);
	
	addRouteLayer(vect, distance);
	addAltitudeProfile(vect);

	// And continue routing
	calculateRoute();

}

function addAltitudeProfile(vect) {
	var geom = vect[0].geometry.clone();
	var length = geom.components.length;
	if (length < 300) {
		// Build the profile GET request
		url = "";
		lats = "?lats=";
		lons ="&lons=";
		for (i = 0; i < geom.components.length; i++) {
			if (i > 0) {
				lats+=",";
				lons+=",";
			}
			point = geom.components[i].transform(map.projection, map.displayProjection);
			lons += roundNumber(point.x, 5);
			lats += roundNumber(point.y, 5);
		}
		
		// Determine which profile server to query
		// The server configurations shoul come from a config file someday
		start = geom.components[0];
		stop  = geom.components[length-1];
		
		if (start.x > 0 && start.x < 10 && start.y > 49 && start.y < 54 && stop.x > 0 && stop.x < 10 && stop.y > 49 && stop.y < 54) {
			// Benelux + western Germany
			url = "http://altitude.openstreetmap.nl/profile/gchart";
		}
		else if (start.x > 20 && start.x < 31 && start.y > 43 && start.y < 49 && stop.x > 20 && stop.x < 31 && stop.y > 43 && stop.y < 49) {
			// Romania + Moldova
			url = "http://profile.fedoramd.org/profile/gchart";
		}
		else if (start.x > 10 && start.x < 16 && start.y > 49 && start.y < 55 && stop.x > 10 && stop.x < 16 && stop.y > 49 && stop.y < 55) {
			// Eastern Germany
			url = "http://profile.fedoramd.org/profile/gchart";
		}
		else if (start.x > -179 && start.x < -44 && start.y > -56 && start.y < 60 && stop.x > -179 && stop.x < -44 && stop.y > -56 && stop.y < 60) {
			// North and South America
			url = "http://labs.metacarta.com/altitude/profile/gchart";
		}
		else if (start.x > 23 && start.x < 33 && start.y > 50 && start.y < 57 && stop.x > 23 && stop.x < 33 && stop.y > 50 && stop.y < 57) {
			// Belarus
			url = "http://altitude.komzpa.net/profile/gchart";
		}
		/*
		else {
			// Everything else
			url = "http://labs.metacarta.com/altitude/profile/gchart";
		}
		*/
		
		// Load the profile image in the browser
		html = OpenLayers.Util.getElement('feature_info').innerHTML;
		if (url.length > 0) {
			url += lats + lons;
			//alert(url);
			html += '<p>Altitude profile:<br><img src="'+url+'" alt="altitude profile for this route">';
			html += "</p>";
		}
		else {
			html += '<p><font color="red">Note: Altitude profile only available in <a href="http://wiki.openstreetmap.org/index.php/YOURS#Altitude_profile">certain areas</a>.</font></p>';
		}
		OpenLayers.Util.getElement('feature_info').innerHTML = html;
	}
}

function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}

//function addMarker(lonlat, type) {
//	//var size = new OpenLayers.Size(10,17);
//	//var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
//	//var icon = new OpenLayers.Icon('http://boston.openguides.org/markers/AQUA.png',size,offset);
//	var marker;
//	switch (type) {
//	case 'to':
//		var size = new OpenLayers.Size(21,25);
//		var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
//		var icon = new OpenLayers.Icon('http://openlayers.org/api/img/marker.png',size,offset);
//		marker = new OpenLayers.Marker(lonlat, icon);
//		//alert('add to');
//		markers.addMarker(marker);
//		break;
//	case 'from':
//		var size = new OpenLayers.Size(21,25);
//		var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
//		var icon = new OpenLayers.Icon('http://openlayers.org/api/img/marker-green.png',size,offset);
//		marker = new OpenLayers.Marker(lonlat, icon);
//		//alert('add to');
//		markers.addMarker(marker);
//		break;	
//	default:
//		marker = new OpenLayers.Marker(lonlat);
//		//alert('add else');
//		markers.addMarker(marker);
//		break;
//	}
//	return marker;
//}
/*
function getRouteAs() {
	if (routelayer.features.length > 0) {
		for (i = 0; i < document.forms['export'].elements.length; i++) {
			element = document.forms['export'].elements[i];
			if (element.name == 'type') {
				if (element.checked == true) {
					type = element.value;
				}
			}
		}
		if (type == 'wpt') {
			alert('this format is not supported yet');
		}
		url = 'api/1.0/saveas.php?type='+type+'&data=';
	

		first= true;
		for (wp= fromWaypoint; wp; wp= wp.next)
		{
			if (first)
				first= false;
			else
				url += ',';
			lonlat= wp.marker.lonlat.clone().
				transform(map.projection,
				map.displayProjection);
			url += roundNumber(lonlat.lon, 6) + ' ' +
				roundNumber(lonlat.lat, 6);

			if (!wp.next)
				break;

			vertices= wp.route[0][0].geometry.getVertices();
		 	for (i = 0; i < vertices.length; i++) {
		 		point = vertices[i];

		 		lonlat = new OpenLayers.LonLat(point.x,
					point.y).transform(map.projection,
		 			map.displayProjection);
		  		url += ',' + roundNumber(lonlat.lon, 6) + ' ' +
		 			roundNumber(lonlat.lat, 6);
		 	}
		}
		return url;
	} else {
		alert('There is no route to export');
	} 
}
*/
function getRouteAs() {
	if (routelayer.features.length > 0) {
		
		
		for (i = 0; i < document.forms['export'].elements.length; i++) {
			element = document.forms['export'].elements[i];
			if (element.name == 'type') {
				if (element.checked == true) {
					type = element.value;
				}
			}
		}
		if (type == 'wpt') {
			alert('this format is not supported yet');
			return;
		}
		var newWindow = window.open("api/1.0/saveas.php", "Download");
		if (!newWindow) return false;
		var html = "";
		var data = "";
		html += "<html><head></head><body><form id='formid' method='post' action='" + ' api/1.0/saveas.php'  + "'>";
		html += "<input type='hidden' name='type' value='" + type + "'/>";

		first= true;
		for (wp= fromWaypoint; wp; wp= wp.next)
		{
			if (first)
				first= false;
			else
				data += ',';
			lonlat= wp.marker.lonlat.clone().
				transform(map.projection,
				map.displayProjection);
			data += roundNumber(lonlat.lon, 6) + ' ' +
				roundNumber(lonlat.lat, 6);

			if (!wp.next)
				break;

			vertices= wp.route[0][0].geometry.getVertices();
		 	for (i = 0; i < vertices.length; i++) {
		 		point = vertices[i];

		 		lonlat = new OpenLayers.LonLat(point.x,
					point.y).transform(map.projection,
		 			map.displayProjection);
		  		data += ',' + roundNumber(lonlat.lon, 6) + ' ' +
		 			roundNumber(lonlat.lat, 6);
		 	}
		}
		html += "<input type='hidden' name='data' value='" + data + "'/>";
		html += "</form><script type='text/javascript'>document.getElementById(\"formid\").submit()</script></body></html>";

		newWindow.document.write(html);
		aler('document written' )
	} else {
		alert('There is no route to export');
	} 
}


function elementChange(element) {
	if (element.value.length > 0) {
		// Try to find element in waypoint list
		for (wp= fromWaypoint; wp != undefined; wp= wp.next)
		{
			if (wp.txt_el == element)
				break;
		}
		if (wp == undefined)
		{
			alert("elementChange: element not found for waypoint");
			return;
		}
		currTextWaypoint = wp;
		xmlhttp['what'] = 'name';
		document.forms['route'].elements['to_text'].disabled = true;
		document.forms['route'].elements['clear'].disabled = false;
		document.forms['route'].elements['calculate'].disabled = true;
		
		url = "http://gazetteer.openstreetmap.org/namefinder/search.xml&find=" + Url.encode(trim(element.value)) + "&max=1";
		
		//xmlhttp['url'] = "cgi-bin/proxy.cgi/?url=" + url;	// does not work because OpenLayers proxy script does not proxy the url parameters
		xmlhttp['url'] = "transport.php?url=" + url;
		loadxmldoc(xmlhttp);
	}
}

function trim(value) {
  value = value.replace(/^\s+/,'');
  value = value.replace(/\s+$/,'');
  return value;
}

function addWaypoint() {
	// Find "Add Waypoint" button in the DOM
	// <tr>
	// <td><img src="marker{color}.pn"></td>
	// <td><input type="button" name="waypoint"
	//	onclick="elementClick(this);" value="Waypoint:"
	//	tabindex=5></td>
	// <td><input type="text" name="waypoint" onclick="elementClick(this);"
	//	onchange="elementChange(this);" value=""
	//	tabindex=1 onfocus="this.select()"></td>
	// <td>{position}</td>
	//</tr>
	trel= document.createElement("tr");

	tdel= document.createElement("td");
	trel.appendChild(tdel);
	imgel= document.createElement("img");
	imgel.setAttribute("src", "markers/marker-yellow.png");
	tdel.appendChild(imgel);

	tdel= document.createElement("td");
	trel.appendChild(tdel);
	button= document.createElement("input");
	button.setAttribute("type", "button");
	button.setAttribute("name", "waypoint");
	button.setAttribute("onclick", "elementClick(this);");
	button.setAttribute("value", "Waypoint:");
	button.setAttribute("tabindex", "5");
	tdel.appendChild(button);

	tdel= document.createElement("td");
	trel.appendChild(tdel);
	text= document.createElement("input");
	text.setAttribute("type", "text");
	text.setAttribute("name", "waypoint");
	text.setAttribute("onclick", "elementClick(this);");
	text.setAttribute("onchange", "elementChange(this);");
	text.setAttribute("value", "");
	text.setAttribute("tabindex", "1");
	text.setAttribute("onfocus", "this.select();");
	tdel.appendChild(text);

	pos_td= document.createElement("td");
	trel.appendChild(pos_td);
	
	tabrow= document.getElementById('add waypoint');
	tabrow.parentNode.insertBefore(trel, tabrow);

	wp= new Waypoint();
	wp.set_tr_el(trel);
	wp.set_button_el(button);
	wp.set_txt_el(text);
	wp.set_pos_el(pos_td);
	prev= toWaypoint.prev;
	prev.set_next(wp);
	wp.set_prev(prev);
	wp.set_next(toWaypoint);
	toWaypoint.set_prev(wp);
	prev.remove_route();

	// By inserting new elements we may have moved the map
	map.updateSize();
}

function elementClick(element) {
	var mode;
	if (element.type == "button") {
	    mode = element.name;
	    switch (mode)
	    {
	    case 'from':
	    case 'to':
	    case 'waypoint':
		// Try to find element in waypoint list
		for (wp= fromWaypoint; wp != undefined; wp= wp.next)
		{
			if (wp.button_el == element)
				break;
		}
		if (wp == undefined)
		{
			alert("elementClick: element not found for waypoint");
			return;
		}
		if (currWaypoint != undefined)
			currWaypoint.button_el.disabled = false;
		currWaypoint = wp;
		document.body.style.cursor='crosshair';
		element.disabled = true;
		break;

	    case 'add waypoint':
		addWaypoint();
		break;
	    case 'clear':
		while (fromWaypoint.next != undefined)
			fromWaypoint.next.destroy();
		fromWaypoint.destroy();
		init_to_from();
		currWaypoint = undefined;

	    	OpenLayers.Util.getElement('feature_info').innerHTML = "";
	    	document.forms['route'].elements['from_text'].value = "";
	    	document.forms['route'].elements['to_text'].value = "";
	    	document.forms['route'].elements['calculate'].disabled = true;
	    	document.forms['route'].elements['clear'].disabled = true;
	    	document.forms['route'].elements['to'].disabled = false;
	    	document.forms['route'].elements['from'].disabled = false;
	    	document.forms['route'].elements['from_text'].disabled = false;
	    	document.forms['route'].elements['to_text'].disabled = false;

		// By deleting elements we may have moved the map
		map.updateSize();

	    	break;
	    case 'calculate':
		// So we have two markers, now we can request a route
		calculateRoute();
	    	break;
	    }
    } else {
    	mode = element.name;
		switch (mode)
		{
		case 'to_text':
			if (element.value == "Street, City") {
				element.value = "";
			}
	    	break;
	    case 'from_text':
	    	if (element.value == "Street, City") {
				element.value = "";
			}
	    	break;
	    }
	}
    //toggleControls(element);
}

var doc;
function processNamefinderXML(response) {
	var xml = new OpenLayers.Format.XML();
	var xml_lonlat = new OpenLayers.LonLat();
	var lonlat = new OpenLayers.LonLat();
	
	doc = xml.read(response);
	
	var bError = false;
	if (doc.childNodes.length > 0) {
		if (doc.documentElement.nodeName == "searchresults") {
			error = doc.documentElement;
			for (j = 0; j < error.attributes.length; j++) {
				switch(error.attributes[j].nodeName)
				{
				case "error":
					html = '<font color="red">Status: Namefinder reports: '+(error.attributes[j].nodeValue)+'</font>';
					bError = true;
					break;
				}
			}
			OpenLayers.Util.getElement('status').innerHTML = html;
		}
		else if (doc.documentElement.nodeName == "parsererror") {
			//html = '<font color="red">Status: Namefinder reports: '+(doc.documentElement.lastChild.textContent)+'</font>';
			html = '<font color="red">Status: Namefinder could not find any results, please try other search words</font>';
			bError = true;
			OpenLayers.Util.getElement('status').innerHTML = html;
		}
	}
	else {
		html = '<font color="red">Status: Namefinder could not find any results, please try other search words</font>';
		bError = true;
		OpenLayers.Util.getElement('status').innerHTML = html;
	}
	if (bError == false) {
		for (i = 0; i < doc.documentElement.childNodes.length; i++) {
			if (doc.documentElement.childNodes[i].nodeName == "named") {
				named = doc.documentElement.childNodes[i];
				for (j = 0; j < named.attributes.length; j++) {
					switch(named.attributes[j].nodeName)
					{
					case "lat":
						lat = parseFloat(named.attributes[j].nodeValue);
						break;
					case "lon":
						lon = parseFloat(named.attributes[j].nodeValue);
						break;
					}
				}
				break;
			}
		}
	
		xml_lonlat.lat = lat;
		xml_lonlat.lon = lon;
	
		lonlat = xml_lonlat.transform(map.displayProjection, map.projection);
	
		currTextWaypoint.set_position(lonlat);
		document.forms['route'].elements['to_text'].disabled = false;
		document.forms['route'].elements['calculate'].disabled = false;
		map.setCenter(lonlat);
	}
}

function feature_info(feature, distance) {
	bounds= feature.geometry.getBounds();
	if (currBounds == undefined)
	{
		currBounds= OpenLayers.Bounds.fromArray(bounds.toArray());
	}
	else
		currBounds.extend(bounds);
	map.zoomToExtent(currBounds);
	
	len = feature.geometry.getLength();
	var wpCount= 0;
	var tot_dist= 0;
	for (wp= fromWaypoint; wp; wp= wp.next)
	{
		wpCount++;
		if (wp.next != undefined)
			tot_dist += wp.distance;
	}

	html = "<p>Routes: <br>" + feature.layer.features.length +
		" (should be " + (wpCount-1)*3 + ")</p>";

	if (wpCount > 2)
	{
		html += "<table>"
		for (i= 1, wp= fromWaypoint; wp && wp.next; i++, wp= wp.next)
		{
			html += "<tr><td>Leg " + i + ":</td>";
			html += "<td>" + Math.round(wp.distance*10)/10 +
				" km</td></tr>";
		}
		html += "</table>"
	}
	html += "<p>This route:<br>";
	html += "Points: " + feature.geometry.components.length + "<br>";
	html += "Length: " + Math.round(tot_dist*10)/10 + " km<br>";

	routeURL = '';
	first= 1;
	for (wp= fromWaypoint; wp; wp= wp.next)
	{
		if (wp.prev == undefined)
			key= 'f';
		else if (wp.next == undefined)
			key= 't';
		else
			key= 'w';
		lonlat= wp.marker.lonlat.clone().transform(map.projection,
			map.displayProjection);
		routeURL += (first ? '?' : '&') +
			key + 'lat='+roundNumber(lonlat.lat, 6) +
			'&' + key + 'lon='+roundNumber(lonlat.lon, 6);
		first= 0;
	}
	routeURL += currRouteParams;
	
	permalink = location.protocol+'//'+location.host+location.pathname+
		routeURL;
	html += "<a href=" + permalink + ">Permalink</a><br>";
	
    OpenLayers.Util.getElement('feature_info').innerHTML = html;
    
    html = "Completed successfully";
	OpenLayers.Util.getElement('status').innerHTML = html;
}

function onXmlHttpReceived() {
	xmlcheckreadystate(xmlhttp['object']);
	if (xmlhttp['object'].readyState == 4) {
		response = xmlhttp['object'];
		switch (xmlhttp['what']) {
		case 'name':
			html = "Status: ready";
			processNamefinderXML(response.responseText);
			
			calculateRoute();
			break;
		case 'route':
			document.forms['route'].elements['calculate'].disabled = false;
			if (xmlhttp['object'].responseText.substring(0,5) == '<'+'?xml') { 
				processRouteXML(response);
				html = "Status: ready";
			} else {
				html = "<font color=red>Status: "+xmlhttp['object'].responseText+"</font>";
			}
			break;
		default:
			alert('Received AJAX response for unknown request origin: '+xmlhttp['url']);
		}
	} else {
		switch (xmlhttp['what']) {
		case 'name':
			html = "Status: waiting for the Namefinder service response " + addWaitImage();
			break;
		case 'route':
			html = "Status: waiting for the route calculation result"  + addWaitImage();
			break;
		default:
			alert('Received AJAX response for unknown request origin: '+xmlhttp['url']);
		}
	}
	OpenLayers.Util.getElement('status').innerHTML = html;
}

function addWaitImage() {
	return '<img src="wait_small.gif"/>';
}

function loadxmldoc(xmlhttp) {
  xmlhttp['object'] = null;
  if (window.XMLHttpRequest) { xmlhttp['object']=new XMLHttpRequest(); } // Mozilla etc
  else if (window.ActiveXObject) { xmlhttp['object']=new ActiveXObject("Microsoft.XMLHTTP"); } // IE
  if (xmlhttp['object'] != null) {
    xmlhttp['object'].onreadystatechange = onXmlHttpReceived;
    xmlhttp['object'].open("GET", xmlhttp['url'], true);
    xmlhttp['object'].setRequestHeader("X-Yours-Client", "www.yournavigation.org");
    xmlhttp['object'].send(null);
  } else {
    alert("You are not using a suitable browser");
  }
}

function xmlcheckreadystate(obj) {
  if(obj.readyState == 4) {
    var internalerror = false;
    if(obj.status == 200) {
      return false;
    }
    alert("URL: "+xmlhttp.url+"\n\nReturn value: "+obj.status+"\nDescription: "+obj.statusText); 
  }
  return false;
}

/**
*
*  URL encode / decode
*  http://www.webtoolkit.info/
*
**/

var Url = {

    // public method for url encoding
    encode : function (string) {
        return escape(this._utf8_encode(string));
    },

    // public method for url decoding
    decode : function (string) {
        return this._utf8_decode(unescape(string));
    },

    // private method for UTF-8 encoding
    _utf8_encode : function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    },

    // private method for UTF-8 decoding
    _utf8_decode : function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {

            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }

        }

        return string;
    }

}
