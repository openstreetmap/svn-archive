/* Copyright (c) 2009, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 */

var myFirstMap;
var myFirstRoute;

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
	var wp = myFirstRoute.waypoint();
	myFirstRoute.selectWaypoint(wp.position);

	// Update the number of the end
	$("li.waypoint[waypointnr='" + wp.position + "']").attr("waypointnr", wp.position + 1);

	// Add the DOM LI
	var wypt_li = waypointCreateDOM(wp);
	$("#route_via > li.waypoint:last-child").before(wypt_li);

	// Enable delete buttons once we have more than two waypoints
	updateWaypointDeleteButtons();

	// By inserting new elements we may have moved the map
	myFirstMap.updateSize();
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
		if (myFirstRoute.Selected !== undefined && myFirstRoute.Selected.position == this.parentNode.attributes.waypointnr.value) {
			// Already selected, deselect
			myFirstRoute.selectWaypoint();
		} else {
			// Select
			myFirstRoute.selectWaypoint(this.parentNode.attributes.waypointnr.value);
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
	del_button.attr("disabled", "disabled");
	del_button.css("visibility", "hidden");
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

	return wypt_li;
}

function waypointRemove(waypointnr) {
	/*
	 * Remove a waypoint from the UI and the route object
	 */

	// Deselect waypoint
	if (myFirstRoute.Selected !== undefined && myFirstRoute.Selected.position == waypointnr) {
		myFirstRoute.selectWaypoint();
	}

	// Delete waypoint
	myFirstRoute.removeWaypoint(parseInt(waypointnr));

	// Remove from UI
	$("li.waypoint[waypointnr='" + waypointnr + "']").remove();

	// Renumber in the UI
	waypointRenumberUI();

	// Ensure there are always at least two waypoints (start and end)
	updateWaypointDeleteButtons();

	// Redraw map
	myFirstMap.updateSize();
}

function updateWaypointDeleteButtons() {
	// Enable the remove buttons based on the number of waypoints
	var disable_delete = $("#route_via li").length <= 2;
        if (disable_delete) {
                $("#route_via input[name='via_del_image']").attr("disabled", "disabled").css("visibility", "hidden");
        } else {
                $("#route_via input[name='via_del_image']").removeAttr("disabled").css("visibility", "visible");
        }
}

function waypointReorderCallback(event, ui) {
	var oldPosition = parseInt($(ui.item.context).attr("waypointnr"));
	var newPosition = parseInt($(ui.item.context).prev().attr("waypointnr")) + 1;
	if (isNaN(newPosition)) {
		// The waypoint was moved to the first position
		newPosition = 0;
	}
	// Add the new waypoint
	var wptOld = myFirstRoute.Waypoints[oldPosition];
	myFirstRoute.addWaypoint(newPosition);
	myFirstRoute.updateWaypoint(newPosition, wptOld.lonlat, wptOld.name)
	// Remove the old waypoint
	if (oldPosition > newPosition) {
		// After adding the new waypoint, the old position has increased
		oldPosition++;
	}
	myFirstRoute.removeWaypoint(oldPosition);

	myFirstRoute.selectWaypoint();
	myFirstMap.updateSize();

	waypointRenumberUI();
}

function waypointRenumberUI() {
	$("#route_via").children().each(function(index, wypt_li) {
		var waypointName;
		if (index == 0) {
			waypointName = "start";
		} else if (index == myFirstRoute.Waypoints.length - 1) {
			waypointName = "finish";
		} else {
			waypointName = "waypoint " + index;
		}

		// Update HTML list
		$(wypt_li).attr("waypointnr", index);

		var marker_image = $("img.marker", wypt_li);
		marker_image.attr("src", myFirstRoute.Waypoints[index].markerUrl());
		marker_image.attr("title", "Click to position " + waypointName +  " on the map");

		var del_button = $("input[name='via_del_image']", wypt_li);
		del_button.attr("alt", "Remove " + waypointName + " from the map");
		del_button.attr("title", "Remove " + waypointName + " from the map");
	});
}

function initWaypoints() {
	// Add begin waypoint
	$("#route_via").append(waypointCreateDOM(myFirstRoute.Start));

	// Add end waypoint
	$("#route_via").append(waypointCreateDOM(myFirstRoute.End));

	// Let the user choose begin waypoint first
	myFirstRoute.selectWaypoint(myFirstRoute.Start.position);
}

function init() {
	OpenLayers.Feature.Vector.style['default'].strokeWidth = '2';
	OpenLayers.Feature.Vector.style['default'].fillColor = '#0000FF';
	OpenLayers.Feature.Vector.style['default'].strokeColor = '#0000FF';

	// Map definition based on http://wiki.openstreetmap.org/index.php/OpenLayers_Simple_Example
	myFirstMap = new OpenLayers.Map ("map", {
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
			
	var layerMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
	var layerCycle = new OpenLayers.Layer.OSM.CycleMap("Cycle Map", {
		displayOutsideMaxExtent: true,
		wrapDateLine: true
	});

	myFirstMap.addLayers([layerMapnik, layerCycle]);
	
	/* Initialize a Route object from the YourNavigation API */
	myFirstRoute = new Yours.Route(myFirstMap, myRouteCallback, updateWaypointCallback);

	initWaypoints();
	
	// Check if a permalink is used
	if (location.search.length > 0) {
		// Add the To/From markers
		var flonlat = new OpenLayers.LonLat();
		var tlonlat = new OpenLayers.LonLat();
		var wlonlat = new OpenLayers.LonLat();
		var zlonlat = new OpenLayers.LonLat();
		var zlevel = -1;
		var params = location.search.substr(1).split('&');
		for (var i = 0; i < params.length; i++) {
			var fields = params[i].split('=');
			
			switch (fields[0]) {
				case 'flat':
					flonlat.lat = parseFloat(fields[1]);
					break;
				case 'flon':
					var value = parseFloat(fields[1]);
					if (value !== 0) {
						flonlat.lon = value;
						myFirstRoute.selectWaypoint(myFirstRoute.Start.position);
						myFirstRoute.updateWaypoint("selected", flonlat.clone().transform(myFirstMap.displayProjection, myFirstMap.projection));
					}
					break;
				case 'wlat':
					wlonlat.lat = parseFloat(fields[1]);
					break;
				case 'wlon':
					var value = parseFloat(fields[1]);
					if (value !== 0) {
						wlonlat.lon = value;
						waypointAdd();
						myFirstRoute.updateWaypoint("selected", wlonlat.clone().transform(myFirstMap.displayProjection, myFirstMap.projection));
					}
					break;
				case 'tlat':
					tlonlat.lat = parseFloat(fields[1]);
					break;
				case 'tlon':
					var value = parseFloat(fields[1]);
					if (value !== 0) {
						tlonlat.lon = value;
						myFirstRoute.selectWaypoint(myFirstRoute.End.position);
						myFirstRoute.updateWaypoint("selected", tlonlat.clone().transform(myFirstMap.displayProjection, myFirstMap.projection));
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
							myFirstMap.setBaseLayer(layerCycle);
							break;
							/*
						case 'cn':
							myFirstMap.setBaseLayer(layerCycleNetworks);
							break;
							*/
						default:
							myFirstMap.setBaseLayer(layerMapnik);
							break;
					}
				case 'zlat':
					zlonlat.lat = parseFloat(fields[1]);
					break;
				case 'zlon':
					var value = parseFloat(fields[1]);
					if (value !== 0) {
						zlonlat.lon = value;
						zlonlat.transform(myFirstMap.displayProjection, myFirstMap.projection);
					}
					break;
				case 'zlevel':
					zlevel = parseInt(fields[1]);
					break;
			}
		}

		// Determine where to zoom the map and at what level
		if (zlevel == -1) {
			var extent = myFirstRoute.Markers.getDataExtent();
			if (extent != null) {
				myFirstMap.zoomToExtent(extent);
			}
		}
		
		prepareDrawRoute();
		myFirstRoute.draw();
		if (zlevel != -1) {
			var extent = myFirstRoute.Markers.getDataExtent();
			if (extent != null) {
				myFirstMap.zoomToExtent(extent);
			}
		} else {
			zlevel = myFirstMap.getZoom();
		}
			
		if (zlonlat.lon !== 0) {
			myFirstMap.setCenter(zlonlat, zlevel);
		}
	} else {
		// No permalink used -> start with a clean map
		
		var pos;
		if (!myFirstMap.getCenter()) {
			pos = new OpenLayers.LonLat(5, 45);
			myFirstMap.setCenter(pos.transform(myFirstMap.displayProjection,myFirstMap.projection), 3);
			if (navigator.geolocation) {  
				// Our geolocation is available, zoom to it if the user allows us to retreive it
				navigator.geolocation.getCurrentPosition(function(position) {
					var pos = new OpenLayers.LonLat(position.coords.longitude, position.coords.latitude);
					myFirstMap.setCenter(pos.transform(myFirstMap.displayProjection, myFirstMap.projection), 14);
				});
			}
		}
		if (typeof(document.baseURI) != 'undefined') {
			if (document.baseURI.indexOf('-devel') > 0 || document.baseURI.indexOf('test') > 0) {
				pos = new OpenLayers.LonLat(6, 52.2);
				myFirstMap.setCenter(pos.transform(myFirstMap.displayProjection,myFirstMap.projection), 14);
			}
		}
	}
	
	var permalink = new OpenLayers.Control.Permalink('test', 'http://www.openstreetmap.org/edit');
	myFirstMap.addControl(permalink);
	permalink.element.textContent = 'Edit map';
	
	
	// Setup file drag-n-drop listeners.
	if (window.FileReader) {
		var dropZone = document.getElementById('content');
		dropZone.addEventListener('dragover', handleDragOver, false);
		dropZone.addEventListener('drop', handleFileSelect, false);
	}
} //End of init()

// Called when the baselayer is changed
/*
function onChangeBaseLayer(e) {
	if (undefined !== myFirstMap.baseLayer) {
		switch (myFirstMap.baseLayer.name) {
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
	var message_div = $('#status');
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
			
			for (i = 0; i < myFirstRoute.Segments.length; i++) {
				seg_li = $(document.createElement("li"));
				
				var seg_div = $(document.createElement("div"));
				seg_div.addClass("segment");
				MyFirstSegment = myFirstRoute.Segments[i];

				seg_div.append(i + ") ");
				if (MyFirstSegment.distance === undefined) {
					seg_div.append($(document.createElement("i")).append("Unavailable"));
				} else {
					seg_div.append("length = "+MyFirstSegment.distance.toFixed(2)+" km, nodes = "+MyFirstSegment.nodes);
				}
				seg_li.append(seg_div);
				seg_ul.append(seg_li);
			}
			if (myFirstRoute.completeRoute) {
				$('#feature_info').append("Route:");
			} else {
				$('#feature_info').append($(document.createElement("font")).attr("color", "red").append("Route (partial):"));
			}
			seg_ul = $(document.createElement("ul"));
			seg_ul.attr("class", "segments");
			$('#feature_info').append(seg_ul);
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			seg_li.append("Distance = "+myFirstRoute.distance.toFixed(2)+" km");
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			seg_li.append("OSM nodes = "+myFirstRoute.nodes);
			
			seg_li = $(document.createElement("li"));
			seg_ul.append(seg_li);
			if (myFirstRoute.completeRoute) {
				// Create a permalink
				// FIXME: User can add waypoints while calculating route, in which case permalink() will fail
				seg_li.append($(document.createElement("a")).attr("href", myFirstRoute.permalink()).append("Permalink"));
			}
			
			// Zoom in to the area around the route
			if (!result.quiet && myFirstRoute.Layer.getDataExtent() != null) {
				myFirstMap.zoomToExtent(myFirstRoute.Layer.getDataExtent());
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
function namefinderCallback(message_div, wait_image, result) {
	if (result == "OK") {
		wait_image.css("visibility", "hidden");
		message_div.html('');
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
		var viaNr = element.parentNode.attributes['waypointnr'].value;
		var wait_image = $(".via_image", element.parentNode);
		var message_div = $(".via_message", element.parentNode);
		myFirstRoute.selectWaypoint(viaNr);

		wait_image.css("visibility", "visible");

		var myCallback = function(result) { namefinderCallback(message_div, wait_image, result); }
		// Choose between Namefinder or Nominatim
		//Yours.NamefinderLookup( Yours.lookupMethod.nameToCoord, jQuery.trim(element.value), MyFirstWayPoint, myFirstMap, myCallback);
		Yours.NominatimLookup( Yours.lookupMethod.nameToCoord, jQuery.trim(element.value), myFirstRoute.Selected, myFirstMap, myCallback);
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
				myFirstRoute.clear();
				//clear the routelayer;
				notice("",'#feature_info');
				notice("",'#status');
				$('.waypoint').remove();
				initWaypoints();
				myFirstMap.updateSize();
				break;
			case 'reverse':
				$('#feature_info').empty();
				
				myFirstRoute.reverse();
				break;
			case 'calculate':
				prepareDrawRoute();

				myFirstRoute.draw();
				break;
			case 'export':
				//document.open(Yours.Export(myFirstRoute), null, null); return false;
				Yours.Export(myFirstRoute);
				break;
		}
	}
}

function prepareDrawRoute() {
	$('#feature_info').empty();
	
	for (var i = 0; i < document.forms['parameters'].elements.length; i++) {
		var element = document.forms['parameters'].elements[i];
		if (element.checked === true) {
			myFirstRoute.parameters.type = element.value;
			break;
		}
	}
	for (var j = 0; j < document.forms['options'].elements.length; j++) {
		var element = document.forms['options'].elements[j];
		if (element.value == 'fast' &&
			element.checked === true) {
			myFirstRoute.parameters.fast = '1';
		} else if (element.value == 'short' &&
			element.checked === true) {
			myFirstRoute.parameters.fast = '0';
		}
	}
	
	//Layer
	for (var k = 0; k < myFirstRoute.map.layers.length; k++) {
		if (myFirstRoute.map.layers[k].visibility === true) {
			switch (myFirstRoute.map.layers[k].name) {
				case 'Cycle Networks':
					myFirstRoute.parameters.layer = 'cn';
					break;
				case 'Cycle Map':
					myFirstRoute.parameters.layer = 'cycle';
					break;
				case 'Mapnik':
					myFirstRoute.parameters.layer = 'mapnik';
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

/*
 * Handle dropping a file on the drop zone
 */
function handleFileSelect(evt) {
	evt.stopPropagation();
	evt.preventDefault();

	var files = evt.dataTransfer.files; // FileList object.

	// files is a FileList of File objects. List some properties.
	var output = [];
	for (var i = 0, f; f = files[i]; i++) {
		var url = window.URL.createObjectURL(f);
		addGpxOvelay(f.name, url);
	}
}
 
/*
 * Handle dragging a file over the drop zone
 */
function handleDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
}

/*
 * Add a GPX overlay to the map
 */
function addGpxOvelay(name, url) {
	myFirstMap.addLayer(new OpenLayers.Layer.GML(name, url, {
		format: OpenLayers.Format.GPX,
		style: {strokeColor: "red", strokeWidth: 3, strokeOpacity: 0.5},
		projection: new OpenLayers.Projection("EPSG:4326")
	}));
}
