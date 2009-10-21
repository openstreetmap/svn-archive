/* Copyright (c) 2009, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 */
 
var myFirstMap;
var myFirstRoute;
var MyFirstWayPoint;
var currRouteParams;
var currBounds;
var permalink;
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

function WaypointAdd() {
    /*
     * Create a new DOM element to enter waypoint info
     */
    var wypt_li = $(document.createElement("li"));
    wypt_li.attr("class","waypoint");
    var wypt_div= $(document.createElement("div"));
    MyFirstWayPoint = MyFirstRoute.waypoint(MyFirstRoute.Waypoints[length -1]);

    var button = $(document.createElement("input"));
    button.attr("type", "image");
    button.attr("name", "via");
    button.attr("src","markers/number" + MyFirstWayPoint.position + ".png");
    button.attr("alt", "Click to position waypoint " + MyFirstWayPoint.position +  " on the map");
    button.attr("title", "Click to position waypoint " + MyFirstWayPoint.position +  " on the map");
    button.attr("waypointnr", MyFirstWayPoint.position);
    button.attr("onclick", "elementClick(this);");
    button.attr("value", "Via:");
    button.css("vertical-align","middle");

    var text = $(document.createElement("input"));
    text.attr("type", "text");
    text.attr("name", "via_text");
    text.attr("waypointnr", MyFirstWayPoint.position);
    text.attr("onclick", "elementClick(this);");
    text.attr("onchange", "elementChange(this);");
    text.attr("value", "e.g. Street, City");
    text.attr("onfocus", "this.select();");

	var img = $(document.createElement("img"));
	img.attr("id", "via_image");
	img.attr("src", "images/blank.gif");
	img.attr("alt", "");
	img.attr("title", "");
	img.attr("style", "vertical-align:middle;");
	img.attr("name", "via_image");
	
                                       //<div id="to_message" style="display:inline"></div>
    wypt_div.attr("class", "via");
    wypt_div.append(button);
    wypt_div.append(' ');
    wypt_div.append(text);
    wypt_div.append(img);
    wypt_div.append('<div id="via_message" style="display:inline"></div>');
    
    wypt_li.append(wypt_div);
    wypt_li.insertBefore("#WaypointTo");
    // By inserting new elements we may have moved the map
    MyFirstMap.updateSize();
}

function init(){
    WindowLocation = String(window.location);

    OpenLayers.Feature.Vector.style['default'].strokeWidth = '2';
    OpenLayers.Feature.Vector.style['default'].fillColor = '#0000FF';
    OpenLayers.Feature.Vector.style['default'].strokeColor = '#0000FF';
	
    // Map definition based on http://wiki.openstreetmap.org/index.php/OpenLayers_Simple_Example
    MyFirstMap = new OpenLayers.Map ("map", {
        controls:[
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
	
    /* Initialize a Route object from the YourNavigation API */
    MyFirstRoute = new Yours.Route(MyFirstMap, myRouteCallback);

    // Check if a permalink is used
    if (location.search.length > 0) {
        // Add the To/From markers
        var flonlat = new OpenLayers.LonLat();
        var tlonlat = new OpenLayers.LonLat();
        var wlonlat = new OpenLayers.LonLat();
        params = location.search.substr(1).split('&');
        for (var i = 0; i < params.length; i++) {
            fields = params[i].split('=');
			
            switch (fields[0]) {
                case 'flat':
                    flonlat.lat = parseFloat(fields[1]);
                    break;
                case 'flon':
                    flonlat.lon = parseFloat(fields[1]);
                    MyFirstWayPoint = MyFirstRoute.Start;
                    MyFirstWayPoint.lonlat = flonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection);
                    MyFirstWayPoint.draw();
                    break;
                case 'wlat':
                    wlonlat.lat = parseFloat(fields[1]);
                    break;
                case 'wlon':
                    wlonlat.lon = parseFloat(fields[1]);
                    MyFirstWayPoint = MyFirstRoute.waypoint();
                    MyFirstWayPoint.lonlat = wlonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection);
                    MyFirstWayPoint.draw();
                    break;
                case 'tlat':
                    tlonlat.lat = parseFloat(fields[1]);
                    break;
                case 'tlon':
                    tlonlat.lon = parseFloat(fields[1]);
                    MyFirstWayPoint = MyFirstRoute.End;
                    MyFirstWayPoint.lonlat = tlonlat.clone().transform(MyFirstMap.displayProjection, MyFirstMap.projection);
                    MyFirstWayPoint.draw();
                    break;
                case 'v':
                    switch (fields[1]) {
                        case 'bicycle':
                            for (j = 0; j < document.forms.parameters.type.length; j++) {
                                if (document.forms.parameters.type[j].value == 'bicycle') {
                                    document.forms.parameters.type[j].checked = true;
                                }
                            }
                            break;
                        case 'foot':
                            for (j = 0; j < document.forms.parameters.type.length; j++) {
                                if (document.forms.parameters.type[j].value == 'foot') {
                                    document.forms.parameters.type[j].checked = true;
                                }
                            }
                            break;
                        default:
                            for (j = 0; j < document.forms.parameters.type.length; j++) {
                                if (document.forms.parameters.type[j].value == 'car') {
                                    document.forms.parameters.type[j].checked = true;
                                }
                            }
                            break;
                    }
                    break;
                case 'fast':
                    if (parseInt(fields[1]) == 1) {
                        for (j = 0; j < document.forms.options.method.length; j++) {
                            if (document.forms.options.method[j].value == 'fast') {
                                document.forms.options.method[j].checked = true;
                            }
                        }
                    } else {
                        for (j = 0; j < document.forms.options.method.length; j++) {
                            if (document.forms.options.method[j].value == 'short') {
                                document.forms.options.method[j].checked = true;
                            }
                        }
                    }
                    break;
                case 'layer':
                    switch (fields[1]) {
                        case 'cycle':
                            //MyFirstMap.addLayers([layerCycle, layerMapnik, layerCycleNetworks]);
                            MyFirstMap.addLayers([layerCycle, layerMapnik]);
                            break;
                            /*
                        case 'cn':
                            MyFirstMap.addLayers([layerCycleNetworks, layerMapnik, layerCycle]);
                            break;
                            */
                        default:
                            //MyFirstMap.addLayers([layerMapnik, layerCycle, layerCycleNetworks]);
                            MyFirstMap.addLayers([layerMapnik, layerCycle]);
                            break;
                    }
            }
        }
        if (null === MyFirstMap.baseLayer) {
            //Fallback for old permalinks that don't list the layer property
            //MyFirstMap.addLayers([layerMapnik, layerCycle, layerCycleNetworks]);
            MyFirstMap.addLayers([layerMapnik, layerCycle]);
        }

        MyFirstMap.zoomToExtent(MyFirstRoute.Markers.getDataExtent());
        MyFirstRoute.draw();
    } else {
        //No preference for any layer, load Mapnik layer first
        //MyFirstMap.addLayers([layerMapnik, layerCycle, layerCycleNetworks]);
        MyFirstMap.addLayers([layerMapnik, layerCycle]);
        var pos;
        if (!MyFirstMap.getCenter()) {
            pos = new OpenLayers.LonLat(5, 45);
            MyFirstMap.setCenter(pos.transform(MyFirstMap.displayProjection,MyFirstMap.projection), 3);
        }
        if (typeof(document.baseURI) != 'undefined') {
            if (document.baseURI.indexOf('-devel') > 0) {
                pos = new OpenLayers.LonLat(6, 52.2);
                MyFirstMap.setCenter(pos.transform(MyFirstMap.displayProjection,MyFirstMap.projection), 14);
            }
        }
    }
	
    PermaLink = new OpenLayers.Control.Permalink('test', 'http://www.openstreetmap.org/edit');
    MyFirstMap.addControl(PermaLink);
    PermaLink.element.textContent = 'Edit map';
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
			$('#feature_info').append("Segments:");
			seg_ul = $(document.createElement("ul"));
			seg_ul.attr("class", "segments");
			$('#feature_info').append(seg_ul);
			
			for (i = 0; i < MyFirstRoute.Segments.length; i++) {
				seg_li = $(document.createElement("li"));
				
				var seg_div= $(document.createElement("div"));
				MyFirstSegment = MyFirstRoute.Segments[i];

				text = i+") length = "+MyFirstSegment.distance.toFixed(2)+" km, nodes = "+MyFirstSegment.nodes;
				
				seg_div.attr("class", "segment");
				seg_div.append(text);
				seg_li.append(seg_div);
				seg_ul.append(seg_li);
			}
			$('#feature_info').append("Route:");
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
			seg_li.append('<a href="' + MyFirstRoute.permalink() + '">Permalink</a>');
			
			// Zoom in to the area around the route
			MyFirstMap.zoomToExtent(MyFirstRoute.Layer.getDataExtent());
			break;
		case Yours.status.starting:
			//alert('starting');
			//message_div.attr("src","images/ajax-loader.gif");
			message_div.html('Calculating route <img src="images/ajax-loader.gif">');
			break;
		case Yours.status.error:
			alert(result);
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
        wait_image.attr("src","images/blank.gif");
    } else {
        wait_image.attr("src","images/blank.gif");
        message_div.html('<font color="red">' + result + '</font>');
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
 * Function to read input in text field and try to get a location from namefinder
 */
function elementChange(element) {
    if (element.value.length > 0) {
        // Try to find element in waypoint list
        message_div = undefined;
        wait_image = undefined;
        switch (element.name) {
            case 'from_text':
                wait_image = $('#from_image');
                message_div = $('#from_message');
                MyFirstWayPoint = MyFirstRoute.Start;
                break;
            case 'to_text':
                wait_image = $('#to_image');
                message_div = $('#to_message');
                MyFirstWayPoint = MyFirstRoute.End;
                break;
            default:
            	// via
            	wait_image = $(element['#via_image']);
            	message_div = $(element['#via_message']);
                MyFirstWayPoint = MyFirstRoute.Waypoints[element.attributes.waypointnr.value];
                break;
        }

        wait_image.attr("src","images/ajax-loader.gif");
        Yours.Lookup(jQuery.trim(element.value), MyFirstWayPoint, MyFirstMap, myCallback);
    }
}

function elementClick(element) {
	var mode;
    if (element.type == "button" || element.type == "image") {
        mode = element.name;
        switch (mode) {
            case 'to':
                MyFirstWayPoint = MyFirstRoute.End;
                break;
            case 'from':
                MyFirstWayPoint = MyFirstRoute.Start;
                break;
            case 'via':
                //do nothing, let the trigger handle it..
                MyFirstWayPoint = MyFirstRoute.waypoint(element.attributes.waypointnr.value);
                break;
            case 'add waypoint':
                WaypointAdd();
                break;
            case 'clear':
                MyFirstRoute.clear();
                //clear the routelayer;
                notice("",'#feature_info');
                notice("",'#status');
                $('input[name=from_text]').val( "e.g. Street, City");
                $('input[name=to_text]').val("e.g. Street, City");
                $('.waypoint').remove();
                MyFirstMap.updateSize();
                break;
            case 'reverse':
            	$('#feature_info').empty();
            	
                MyFirstRoute.reverse();
                break;
            case 'calculate':
            	$('#feature_info').empty();
            	
                for (var i = 0; i < document.forms['parameters'].elements.length; i++) {
                    element = document.forms['parameters'].elements[i];
                    if (element.checked === true) {
                        MyFirstRoute.parameters.vehicle = element.value;
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

                MyFirstRoute.draw();
                break;
			case 'export':
				//document.open(Yours.Export(MyFirstRoute), null, null); return false;
				Yours.Export(MyFirstRoute);
				break;
        }
    } else {
        mode = element.name;
        switch (mode) {
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
