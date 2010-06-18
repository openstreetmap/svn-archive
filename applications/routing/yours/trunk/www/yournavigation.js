/* Copyright (c) 2009, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 */

var myFirstMap;
var myFirstRoute;
var currRouteParams;
var currBounds;
var WindowLocation;

var map_layer = [];
map_layer.target = 'world';

// Styles for drawing lines in different colors to and from the actual waypoint
// markers.
style_from = {
	strokeColor: "#00FF00",
	strokeWidth: 3
};
style_mid = {
	strokeColor: "#FFFF00",
	strokeWidth: 3
};
style_to = {
	strokeColor: "#FF0000",
	strokeWidth: 3
};

function waypointAdd() {
	/*
	 * Create a new DOM element to enter waypoint info
	 */
	var wp = MyFirstRoute.waypoint();
	MyFirstRoute.selectWaypoint(wp.position);

	// Update the number of the end
	$("li.waypoint[waypointnr='" + wp.position + "']").attr("waypointnr", wp.position + 1);

	// Add the DOM LI
	var wypt_li = waypointCreateDOM(wp);
	$("#route_via > li.waypoint:last-child").before(wypt_li);

	// By inserting new elements we may have moved the map
	MyFirstMap.updateSize();
}

function waypointCreateDOM(waypoint) {
	/*
	 * Create a new DOM element to enter waypoint info
	 */
	var waypointName;
	if (waypoint.type == "from") {
		waypointName = "start";
	} else if (waypoint.type == "to") {
		waypointName = "finish";
	} else {
		waypointName = "waypoint " + waypoint.position;
	}

	var wypt_li = $(document.createElement("li"));
	wypt_li.attr("waypointnr", waypoint.position);
	wypt_li.addClass("waypoint");

	var marker_image = $(document.createElement("img"));
	marker_image.attr("src", waypoint.markerUrl());
	marker_image.attr("alt", "Via:");
	marker_image.attr("title", "Click to position " + waypointName +  " on the map");
	marker_image.bind("click", function() {
		if (MyFirstRoute.Selected !== undefined && MyFirstRoute.Selected.position == this.parentNode.attributes.waypointnr.value) {
			// Already selected, deselect
			MyFirstRoute.selectWaypoint();
		} else {
			// Select
			MyFirstRoute.selectWaypoint(this.parentNode.attributes.waypointnr.value);
		}
	});
	marker_image.addClass("marker");

	var text = $(document.createElement("input"));
	text.attr("type", "text");
	text.attr("name", "via_text");
	text.attr("value", "e.g. Street, City");
	text.bind("change", function() { elementChange(this); });
	text.bind("focus", function() { this.select(); });

	var del_button = $(document.createElement("input"));
	del_button.attr("type", "image");
	del_button.attr("name", "via_del_image");
	del_button.attr("src", "images/del.png");
	del_button.attr("alt", "Remove " + waypointName + " from the map");
	del_button.attr("title", "Remove " + waypointName + " from the map");
	del_button.bind("click", function() { elementClick(this); });
	del_button.attr("value", "");
	del_button.addClass("via_del_image");

	var via_image = $(document.createElement("img"));
	via_image.attr("src", "images/ajax-loader.gif");
	via_image.css("visibility", "hidden");
	via_image.attr("alt", "");
	via_image.addClass("via_image");

	var via_message = $(document.createElement("span"));
	via_message.addClass("via_message");

	wypt_li.addClass("via");
	wypt_li.append(marker_image);
	wypt_li.append(' ');
	wypt_li.append(text);
	wypt_li.append(' ');
	wypt_li.append(del_button);
	wypt_li.append(' ');
	wypt_li.append(via_image);
	wypt_li.append(via_message);

	var disable_delete = $("#route_via li").length < 2;
	$("#route_via input[name='via_del_image']").attr("disabled", disable_delete ? "disabled" : "");
	
	return wypt_li;
}

function waypointRemove(waypointnr) {
	/*
	 * Remove a waypoint from the UI and the route object
	 */

	// Deselect waypoint
	if (MyFirstRoute.Selected !== undefined && MyFirstRoute.Selected.position == waypointnr) {
		MyFirstRoute.selectWaypoint();
	}

	// Delete waypoint
	MyFirstRoute.removeWaypoint(parseInt(waypointnr));

	// Remove from UI
	$("li.waypoint[waypointnr='" + waypointnr + "']").remove();

	// Renumber in the UI
	waypointRenumberUI();

	// Ensure there are always at least two waypoints (start and end)
	var disable_delete = $("#route_via li").length <= 2;
	$("#route_via input[name='via_del_image']").attr("disabled", disable_delete ? "disabled" : "");

	// Redraw map
	MyFirstMap.updateSize();
}

function waypointReorderCallback(event, ui) {
	var oldPosition = parseInt($(ui.item.context).attr("waypointnr"));
	var newPosition = parseInt($(ui.item.context).prev().attr("waypointnr")) + 1;
	if (isNaN(newPosition)) {
		// The waypoint was moved to the first position
		newPosition = 0;
	}
	// Add the new waypoint
	var wptOld = MyFirstRoute.Waypoints[oldPosition];
	MyFirstRoute.addWaypoint(newPosition);
	MyFirstRoute.updateWaypoint(newPosition, wptOld.lonlat, wptOld.name)
	// Remove the old waypoint
	if (oldPosition > newPosition) {
		// After adding the new waypoint, the old position has increased
		oldPosition++;
	}
	MyFirstRoute.removeWaypoint(oldPosition);

	MyFirstRoute.selectWaypoint();
	MyFirstMap.updateSize();

	waypointRenumberUI();
}

function waypointRenumberUI() {
	$("#route_via").children().each(function(index, wypt_li) {
		var waypointName;
		if (index == 0) {
			waypointName = "start";
		} else if (index == MyFirstRoute.Waypoints.length - 1) {
			waypointName = "finish";
		} else {
			waypointName = "waypoint " + index;
		}

		// Update HTML list
		$(wypt_li).attr("waypointnr", index);

		var marker_image = $("img.marker", wypt_li);
		marker_image.attr("src", MyFirstRoute.Waypoints[index].markerUrl());
		marker_image.attr("title", "Click to position " + waypointName +  " on the map");

		var del_button = $("input[name='via_del_image']", wypt_li);
		del_button.attr("alt", "Remove " + waypointName + " from the map");
		del_button.attr("title", "Remove " + waypointName + " from the map");
	});
}

function initWaypoints() {
	// Add begin waypoint
	$("#route_via").append(waypointCreateDOM(MyFirstRoute.Start));

	// Add end waypoint
	$("#route_via").append(waypointCreateDOM(MyFirstRoute.End));

	// Let the user choose begin waypoint first
	MyFirstRoute.selectWaypoint(MyFirstRoute.Start.position);
}

function init() {
	WindowLocation = String(window.location);

	OpenLayers.Feature.Vector.style['default'].strokeWidth = '2';
	OpenLayers.Feature.Vector.style['default'].fillColor = '#0000FF';
	OpenLayers.Feature.Vector.style['default'].strokeColor = '#0000FF';

	// Map definition based on http://wiki.openstreetmap.org/index.php/OpenLayers_Simple_Example
	MyFirstMap = new OpenLayers.Map ("map", {
		controls: [
			new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.PanZoomBar(),
			new OpenLayers.Control.LayerSwitcher(),
			new OpenLayers.Control.Attribution()
		],
		/*eventListeners: {
			//"moveend": mapEvent,
			//"zoomend": mapEvent,
			//"changelayer": mapLayerChanged,
			"changebaselayer": onChangeBaseLayer
		},*/
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
	/*
	layerCycleNetworks = new OpenLayers.Layer.OSM.CycleMap("Cycle Networks", {
		displayOutsideMaxExtent: true,
		wrapDateLine: true
	});
	*/

	MyFirstMap.addLayers([layerMapnik, layerCycle]);
	
	/* Initialize a Route object from the YourNavigation API */
	MyFirstRoute = new Yours.Route(MyFirstMap, myRouteCallback, updateWaypointCallback);

	initWaypoints();
	
	// Check if a permalink is used
	if (location.search.length > 0) {
		// Add the To/From markers
		var flonlat = new OpenLayers.LonLat();
		var tlonlat = new OpenLayers.LonLat();
		var wlonlat = new OpenLayers.LonLat();
		var zlonlat = new OpenLayers.LonLat();
		var zlevel = -1;
		params = location.search.substr(1).split('&');
		for (var i = 0; i < params.length; i++) {
			fields = params[i].split('=');
			
			switch (fields[0]) {
				case 'flat':
					flonlat.lat = parseFloat(fields[1]);
					break;
				case 'flon':
					value = parseFloat(fields[1]);
					if (value !== 0) {
						flonlat.lon = value;
						MyFirstRoute.selectWaypoint(MyFirstRoute.Start.position);
						MyFirstRoute.updateWaypoint("selected", flonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection));
					}
					break;
				case 'wlat':
					wlonlat.lat = parseFloat(fields[1]);
					break;
				case 'wlon':
					value = parseFloat(fields[1]);
					if (value !== 0) {
						wlonlat.lon = value;
						waypointAdd();
						MyFirstRoute.updateWaypoint("selected", flonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection));
					}
					break;
				case 'tlat':
					tlonlat.lat = parseFloat(fields[1]);
					break;
				case 'tlon':
					value = parseFloat(fields[1]);
					if (value !== 0) {
						tlonlat.lon = value;
						MyFirstRoute.selectWaypoint(MyFirstRoute.End.position);
						MyFirstRoute.updateWaypoint("selected", tlonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection));
					}
					break;
				case 'v':
					$("input[name='type'][value='" + fields[1] + "']").attr("checked", true);
					break;
				case 'fast':
					if (parseInt(fields[1]) == 1) {
						$("input[name='method'][value='fast']").attr("checked", true);
					} else {
						$("input[name='method'][value='short']").attr("checked", true);
					}
					break;
				case 'layer':
					switch (fields[1]) {
						case 'cycle':
							MyFirstMap.setBaseLayer(layerCycle);
							break;
							/*
						case 'cn':
							MyFirstMap.setBaseLayer(layerCycleNetworks);
							break;
							*/
						default:
							MyFirstMap.setBaseLayer(layerMapnik);
							break;
					}
				case 'zlat':
					zlonlat.lat = parseFloat(fields[1]);
					break;
				case 'zlon':
					value = parseFloat(fields[1]);
					if (value !== 0) {
						zlonlat.lon = value;
						zlonlat.transform(MyFirstMap.displayProjection, MyFirstMap.projection);
					}
					break;
				case 'zlevel':
					zlevel = parseInt(fields[1]);
					break;
			}
		}

		// Determine where to zoom the map and at what level
		if (zlevel == -1) {
			var extent = MyFirstRoute.Markers.getDataExtent();
			if (extent != null) {
				MyFirstMap.zoomToExtent(extent);
			}
		}
		
		prepareDrawRoute();
		MyFirstRoute.draw();
		if (zlevel != -1) {
			var extent = MyFirstRoute.Markers.getDataExtent();
			if (extent != null) {
				MyFirstMap.zoomToExtent(extent);
			}
		} else {
			zlevel = MyFirstMap.getZoom();
		}
			
		if (zlonlat.lon !== 0) {
			MyFirstMap.setCenter(zlonlat, zlevel);
		}
	} else {
		// No permalink used -> start with a clean map
		
		var pos;
		if (!MyFirstMap.getCenter()) {
			pos = new OpenLayers.LonLat(5, 45);
			MyFirstMap.setCenter(pos.transform(MyFirstMap.displayProjection,MyFirstMap.projection), 3);
			if (navigator.geolocation) {  
				// Our geolocation is available, zoom to it if the user allows us to retreive it
				navigator.geolocation.getCurrentPosition(function(position) {
					var pos = new OpenLayers.LonLat(position.coords.longitude, position.coords.latitude);
					MyFirstMap.setCenter(pos.transform(MyFirstMap.displayProjection, MyFirstMap.projection), 14);
				});
			}
		}
		if (typeof(document.baseURI) != 'undefined') {
			if (document.baseURI.indexOf('-devel') > 0 || document.baseURI.indexOf('test') > 0) {
				pos = new OpenLayers.LonLat(6, 52.2);
				MyFirstMap.setCenter(pos.transform(MyFirstMap.displayProjection,MyFirstMap.projection), 14);
			}
		}
	}
	
	var permalink = new OpenLayers.Control.Permalink('test', 'http://www.openstreetmap.org/edit');
	MyFirstMap.addControl(permalink);
	permalink.element.textContent = 'Edit map';
} //End of init()

// Called when the baselayer is changed
/*
function onChangeBaseLayer(e) {
	if (undefined !== MyFirstMap.baseLayer) {
		switch (MyFirstMap.baseLayer.name) {
			case 'Cycle Networks':
				for (j = 0; j < document.forms.parameters.type.length; j++) {
					if (document.forms.parameters.type[j].value == 'foot' || document.forms.parameters.type[j].value == 'motorcar') {
						document.forms.parameters.type[j].disabled = true;
					}
					if (document.forms.parameters.type[j].value == 'bicycle') {
						document.forms.parameters.type[j].checked = true;
					}
				}
				break;
			default:
				for (j = 0; j < document.forms.parameters.type.length; j++) {
					if (document.forms.parameters.type[j].value == 'foot'
						|| document.forms.parameters.type[j].value == 'bicycle'
						|| document.forms.parameters.type[j].value == 'motorcar') {
						document.forms.parameters.type[j].disabled = false;
					}
				}
				break;
		}
	}
}
*/

function myRouteCallback(code, result) {
	message_div = $('#status');
	switch (code) {
		case Yours.status.routeFinished:
			$('#directions').html("Not implemented yet");
			notice("",'#status');
			
			var seg_ul;
			var seg_li;
			
			// Add segment info (nodes, distance), totals and permalink
			$('#feature_info').empty();
			$('#feature_info').append("Segments:");
			seg_ul = $(document.createElement("ul"));
			seg_ul.attr("class", "segments");
			$('#feature_info').append(seg_ul);
			
			for (i = 0; i < MyFirstRoute.Segments.length; i++) {
				seg_li = $(document.createElement("li"));
				
				var seg_div = $(document.createElement("div"));
				seg_div.addClass("segment");
				MyFirstSegment = MyFirstRoute.Segments[i];

				seg_div.append(i + ") ");
				if (MyFirstSegment.distance === undefined) {
					seg_div.append($(document.createElement("i")).append("Unavailable"));
				} else {
					seg_div.append("length = "+MyFirstSegment.distance.toFixed(2)+" km, nodes = "+MyFirstSegment.nodes);
				}
				seg_li.append(seg_div);
				seg_ul.append(seg_li);
			}
			if (MyFirstRoute.completeRoute) {
				$('#feature_info').append("Route:");
			} else {
				$('#feature_info').append($(document.createElement("font")).attr("color", "red").append("Route (partial):"));
			}
			seg_ul = $(document.createElement("ul"));
			seg_ul.attr("class", "segments");
			$('#feature_info').append(seg_ul);
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			seg_li.append("Distance = "+MyFirstRoute.distance.toFixed(2)+" km");
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			seg_li.append("OSM nodes = "+MyFirstRoute.nodes);
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			if (MyFirstRoute.completeRoute) {
				// Create a permalink
				// FIXME: User can add waypoints while calculating route, in which case permalink() will fail
				seg_li.append($(document.createElement("a")).attr("href", MyFirstRoute.permalink()).append("Permalink"));
			}
			
			// Zoom in to the area around the route
			if (!result.quiet) {
				MyFirstMap.zoomToExtent(MyFirstRoute.Layer.getDataExtent());
			}
			break;
		case Yours.status.starting:
			//alert('starting');
			//message_div.attr("src","images/ajax-loader.gif");
			message_div.html('Calculating route <img src="images/ajax-loader.gif">');
			break;
		case Yours.status.error:
			//alert(result);
			message_div.html('<font color="red">' + result + '</font>');
		default:
			//$('#status').html(result);
			break;
	}
	//alert(code);
}

/*
 * Called when a namefinder result is returned
 */
function myCallback(result) {
	if (result == "OK") {
		if (undefined !== wait_image) {
			wait_image.css("visibility", "hidden");
		}
		if (undefined !== message_div) {
			message_div.html('');
		}
	} else {
		wait_image.css("visibility", "hidden");
		message_div.html('<font color="red">' + result + '</font>');
	}
}

/*
 * Called when a reverse geocoder result is returned
 */
function updateWaypointCallback(waypoint) {
	if (waypoint !== undefined) {
		$("#route_via li[waypointnr='" + waypoint.position + "'] input").val(waypoint.name);
	}
}

function typeChange(element) {
	if (element.value == "cycleroute") {
		// Disable the shortest route option
		$('input[name=method]')[0].checked = true;
		$('input[name=method]')[1].disabled = true;
	} else {
		// Enable the shortest route option
		$('input[name=method]')[1].disabled = false;
	}
}

/*
 * Function to read input in text field and try to get a location from geocoder
 */
function elementChange(element) {
	if (element.value.length > 0) {
		// Try to find element in waypoint list
		viaNr = element.parentNode.attributes['waypointnr'].value;
		wait_image = $(".via_image", element.parentNode);
		message_div = $(".via_message", element.parentNode);
		MyFirstRoute.selectWaypoint(viaNr);

		wait_image.css("visibility", "visible");
		// Choose between Namefinder or Nominatim
		//Yours.NamefinderLookup( Yours.lookupMethod.nameToCoord, jQuery.trim(element.value), MyFirstWayPoint, MyFirstMap, myCallback);
		Yours.NominatimLookup( Yours.lookupMethod.nameToCoord, jQuery.trim(element.value), MyFirstRoute.Selected, MyFirstMap, myCallback);
	}
}

function elementClick(element) {
	var mode;
	if (element.type == "button" || element.type == "image") {
		mode = element.name;
		switch (mode) {
			case 'via_del_image':
				waypointRemove(element.parentNode.attributes.waypointnr.value);
				break;
			case 'add waypoint':
				waypointAdd();
				break;
			case 'clear':
				MyFirstRoute.clear();
				//clear the routelayer;
				notice("",'#feature_info');
				notice("",'#status');
				$('.waypoint').remove();
				initWaypoints();
				MyFirstMap.updateSize();
				break;
			case 'reverse':
				$('#feature_info').empty();
				
				MyFirstRoute.reverse();
				break;
			case 'calculate':
				prepareDrawRoute();

				MyFirstRoute.draw();
				break;
			case 'export':
				//document.open(Yours.Export(MyFirstRoute), null, null); return false;
				Yours.Export(MyFirstRoute);
				break;
		}
	}
}

function prepareDrawRoute() {
	$('#feature_info').empty();
				
	for (var i = 0; i < document.forms['parameters'].elements.length; i++) {
		element = document.forms['parameters'].elements[i];
		if (element.checked === true) {
			MyFirstRoute.parameters.type = element.value;
			break;
		}
	}
	for (var j = 0; j < document.forms['options'].elements.length; j++) {
		element = document.forms['options'].elements[j];
		if (element.value == 'fast' &&
			element.checked === true) {
			MyFirstRoute.parameters.fast = '1';
		} else if (element.value == 'short' &&
			element.checked === true) {
			MyFirstRoute.parameters.fast = '0';
		}
	}
	
	//Layer
	for (var k = 0; k < MyFirstRoute.map.layers.length; k++) {
		if (MyFirstRoute.map.layers[k].visibility === true) {
			switch (MyFirstRoute.map.layers[k].name) {
				case 'Cycle Networks':
					MyFirstRoute.parameters.layer = 'cn';
					break;
				case 'Cycle Map':
					MyFirstRoute.parameters.layer = 'cycle';
					break;
				case 'Mapnik':
					MyFirstRoute.parameters.layer = 'mapnik';
					break;
			}
		}
	}
}

/*
 * Show an image indicating progress
 */
function addWaitImage() {
	return '<img src="wait_small.gif"/>';
}


/*
 * Simple function that uses jQuery to display a message in a given documentElement
 */
function notice(message, div, type) {
	switch (type) {
		case 'warning':
			message = '<font color="orange">' + message + '</font>';
			break;
		case 'error':
			message = '<font color="red">' + message + '</font>';
			break;
		default:
			message = message;
	}
	$(div).html(message);
}
