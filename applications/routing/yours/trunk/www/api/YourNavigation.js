/* Copyright (c) 2009, L. IJsselstein and others
  Yournavigation.org All rights reserved.
 

/*
    YourNavigation NameSpace with Classes and functions
    @requires OpenLayers
    @requires jQuery
 */

var gosmoreUrl = "proxy.php?u=http://yournavigation.org/api/1.0/gosmore.php?"; //leave blank when using a local gosmore service.
var namefinderUrl = "proxy.php?u=http://gazetteer.openstreetmap.org/namefinder/search.xml?"; //leave blank when using a local gosmore service.


// Create OpenLayers Control Click handler
OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },
    /*
     * Initialize is called when the Click control is activated
     * It sets the behavior of a click on the map
     */
    initialize: function() {
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

    /*
     * How OpenLayers should react to a user click on the map.
     * Get the LonLat from the user click and position
     */
    trigger: function(e) {
        var location = this.map.getLonLatFromViewPortPx(e.xy);
        if (MyFirstWayPoint != undefined) {
            MyFirstWayPoint.draw(location);
        //route.draw();
        }
    // Maybe add a new marker by default if none selected?
    }
}
);


/*
    Class: YourNavigation
    A class for routing with OpenLayers and Gosmore
    Requires:
        jQuery
        OpenLayers
*/
var YourNavigation = {

};

/*
    Function: Lookup

    Parameters:

        value - value to lookup (string)
        wp - waypoint to place when lookup is succesfull (object)
        callback - Pass a function to handle the result message

    Returns:
        on success - 'OK'
        on failure - Error message
 */
YourNavigation.Lookup = function(value,wp,map,callback) {
    var namefinderParameters = "find=" + value + "&max=1";
    $.get(namefinderUrl + namefinderParameters, {}, function(xml){
        var result = YourNavigation.NameFinder(xml,wp,map);
        if (result == 'OK') {
            //route.draw();
            callback(result);
        } else {
            callback(result);
        }
    },"xml");
}

/*
    Function: Export

    Parameters:

    Returns:

 */
YourNavigation.Export = function() {
    //TODO: fix this for the new Waypoints collection method

    /*if (routelayer.features.length > 0) {


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
    }*/
    }

/*
    Function: NameFinder

    Parameters:

        xml - file in xml format returned from a namefinder service
        wp - Waypoint that will be matched when a valid location is found in the xml


    Returns:
        on success - 'OK' and Waypoint with location, drawn on Waypoints Layer
        on failure - Error message

 */
YourNavigation.NameFinder = function(xml,wp,map) {
    if (xml.childNodes.length > 0) {
        if (xml.documentElement.nodeName == "searchresults") {
            //first test if the response contains an error notice
            error = xml.documentElement;

            for (j = 0; j < error.attributes.length; j++) {
                switch(error.attributes[j].nodeName)
                {
                    case "error":
                        // error found, set status message and return false
                        error = (error.attributes[j].nodeValue);
                        return error;
                }
            }
            // No error found, continue -->
            for (var i = 0; i < xml.documentElement.childNodes.length; i++) {
                if (xml.documentElement.childNodes[i].nodeName == "named") {
                    named = xml.documentElement.childNodes[i];
                    var responseLonLat = new OpenLayers.LonLat();
                    for (j = 0; j < named.attributes.length; j++) {
                        switch(named.attributes[j].nodeName)
                        {
                            case "lat":
                                responseLonLat.lat = parseFloat(named.attributes[j].nodeValue);
                                break;
                            case "lon":
                                responseLonLat.lon = parseFloat(named.attributes[j].nodeValue);
                                break;
                            case "name":
                                wp.name = named.attributes[j].nodeValue;
                                break;
                            case "category":
                                wp.category = named.attributes[j].nodeValue;
                                break;
                            case "rank":
                                wp.rank = named.attributes[j].nodeValue;
                                break;
                            case "info":
                                wp.info = named.attributes[j].nodeValue;
                                break;
                        }
                    }
                }
            }
            var transformedLonLat = new OpenLayers.LonLat();
            transformedLonLat = responseLonLat.transform(map.displayProjection, map.projection);
            wp.draw(transformedLonLat);
            map.setCenter(transformedLonLat);
            return 'OK';

        } else if (xml.documentElement.nodeName == "parsererror") {
            error = 'No results, please try other search';
            return error;
        }
    } else {
        // xml doc contains no searchresults
        error = 'No results, please try other search';
        return error;
    }
    error = "Something went wrong, but I don't know what";
    return error;
}

/**
 *    Class: YourNavigation.Route
 */

YourNavigation.Route = function(map) {
    /**
     * Constructor: new YourNavigation.Route(map)
     * Constructor for a new YourNavigation.Route instance.
     *
     * Parameters:
     * map - {String} OpenLayers.Map
     *
     * <http://dev.openlayers.org/releases/OpenLayers-2.8/doc/apidocs/files/OpenLayers/Map-js.html#OpenLayers.Map.OpenLayers.Map>
     *
     * Examples:
     * (code)
     * // create a map and draw a simple route
     * var MyFirstMap = new OpenLayers.Map('map');
     * var ol_wms = new OpenLayers.Layer.WMS(
     *           "OpenLayers WMS",
     *           "http://labs.metacarta.com/wms/vmap0",
     *           {layers: 'basic'}
     *       );
     * MyFirstMap.addLayers([ol_wms]);
     * MyFirstMap.addControl(new OpenLayers.Control.LayerSwitcher());
     *
     * MyFirstMap.zoomToMaxExtent();
     * MyFirstRoute = new YourNavigation.Route(MyFirstMap);
     * var flat=51.158883504779;
     * var flon=3.220214843821;
     * var tlat=51.241492039675;
     * var tlon=4.472656250021;
     * MyFirstRoute.waypoint("from").lonlat = new OpenLayers.LonLat(flon,flat);
     * MyFirstRoute.waypoint("to").lonlat = new OpenLayers.LonLat(tlon,tlat);
     * MyFirstRoute.draw(MyCallBack);
     *
     * //display the response from the route request:
     * function MyCallBack(response) {
     *   alert(response);
     * }
     * (end)
     */
    var self = this;
    this.permalink = function() {
        var flonlat = this.Start.lonlat.clone().transform(this.map.projection,this.map.displayProjection);
        var tlonlat = this.End.lonlat.clone().transform(this.map.projection,this.map.displayProjection);
        //for each via
        var via = '';
        if (this.Waypoints.length > 2) {
            for (var i = 0; i < this.Waypoints.length -1; i++) {
                if (this.Waypoints[i].type == 'via') {
                    var wlonlat = this.Waypoints[i].lonlat.clone().transform(this.map.projection,this.map.displayProjection);
                    via += '&wlat=' + wlonlat.lat + '&wlon=' + wlonlat.lon;
                }
            }
        }
        var permalink_url = 'flat=' + flonlat.lat + '&flon=' + flonlat.lon +
        via + '&tlat=' + tlonlat.lat + '&tlon=' + tlonlat.lon;
        permalink_url += '&v=' + this.parameters.vehicle +
        '&fast=' + this.parameters.fast +
        '&layer=' + this.parameters.layer;
        return location.protocol + '//' + location.host + location.pathname + "?" + permalink_url;
    }
    /*
        Function: directions
        Agregate directions for the segments
    */
    this.directions = function() {
    //for each segment in route segments[]
    /*if(this.feature == undefined) {
            alert('Feature not set, cannot get directions');
        } else {

        }*/
    }
    /**
     * Property: YourNavigation.Route.parameters
     * { Array } with parameters for calculating the route
     */
    this.parameters = {
        /**
         * Property: YourNavigation.Route.parameters.fast
         * Method for route calculation
         *
         * 0 - shortest route
         * 1 - fastest route (default)
         */
        fast: '1',
        /**
         * Property: YourNavigation.Route.parameters.type
         * Type of transportation to use for calculation
         *
         * motorcar - routing for regular cars (default)
         * hvg - Heavy goods, routing for trucks
         * psv - Public transport, routing using public transport
         * bicycle - routing using bicycle
         * foot - routing on foot
         * goods
         * horse
         * motorcycle
         * moped
         * mofa
         * motorboat
         * boat
         */
        type: 'motorcar',
        layer: 'mapnik'
    }

    /**
     * Function: YourNavigation.Route.waypoint(id)
     *
     * Parameters:
     * 
     * id - (optional) { integer } or { string } "from" or "to"
     * 
     * describing what waypoint to get from the <Waypoints> collection
     *
     * Returns:
     *
     * <YourNavigation.Waypoint>
     */
    this.waypoint = function(id) {
        switch(id) {
            case "from":
                wp = this.Waypoints[0];
                wp.type = id;
                break;
            case "to":
                wp = this.Waypoints[this.Waypoints.length - 1];
                wp.type = id;
                break;
            default:
                if (this.End.position == id || id == undefined) {
                    wp= new YourNavigation.Waypoint(this);
                    wp.position = this.Waypoints.length - 1;
                    wp.type = "via";

                    // Add the waypoint to the array of Waypoints
                    this.Waypoints.splice(wp.position, 0, wp);

                    // Update the position of the end node, since the array has grown.
                    this.Waypoints[this.Waypoints.length - 1].position = this.Waypoints.length;
                    this.End = this.Waypoints[this.Waypoints.length - 1];
                } else {
                    wp = this.Waypoints[id];
                }
        }
        
        return wp;
    }

    /*
        Constructor: reset
        Initialize a Route Object
    */
    //TODO: merge reset and clear into a single function
    this.reset = function(OpenLayersMap) {
        if (OpenLayersMap != undefined){
            this.map = OpenLayersMap;
            this.Layer = new OpenLayers.Layer.Vector("Route");
            this.Markers = new OpenLayers.Layer.Markers("Markers",
            {
                projection: this.map.displayProjection,
                'calculateInRange':	function() {
                    return true;
                }
            }
            );
            this.map.addLayers([this.Layer,this.Markers]);
            var control = new OpenLayers.Control.SelectFeature(this.Layer,
            {
                clickout: true,
                toggle: false,
                multiple: false,
                hover: false,
                toggleKey: "ctrlKey", 			// ctrl key removes from selection
                multipleKey: "shiftKey"			// shift key adds to selection
            }
            );

            this.map.addControl(control);
            var click = new OpenLayers.Control.Click();
            this.map.addControl(click);
            click.activate();
        }
        if (this.map == undefined) {
            error = "YourNavigation.Route should be initialized with a map";
            return error;
        }
        this.Layer.destroyFeatures();
        for (var i = 0; i < this.Waypoints.length; i++){
            this.Waypoints[i].destroy();
        }
        this.Waypoints[0] = new YourNavigation.Waypoint(this);
        this.Start = this.Waypoints[0];
        this.Waypoints[0].position = 0;
        this.Waypoints[0].type = "from";
        this.Waypoints[1] = new YourNavigation.Waypoint(this);
        this.Waypoints[1].position = 1;
        this.Waypoints[1].type = "to";
        this.End = this.Waypoints[1];
        return true;
    }

    /*
        Function: clear

        Reset the Route Object to an empty array with only a start and end Waypoint
    
    */
    this.clear = function() {
        this.Waypoints.length = 0;
        this.Segments.length = 0;
        this.Markers.clearMarkers();
        this.reset();

    }

    /*
        Function: draw

        Try to draw a route with the Waypoints[] collection

        Parameters:

        callback - Pass a function to handle the result message

    */
    this.draw = function(callback) {
        this.Layer.destroyFeatures();
        this.Segments.length = 0;
        //alert("length:" + this.Waypoints.length);
        if (this.Waypoints.length > 2) {
            //determine the numer of segments..
            var numsegs = this.Waypoints.length -1;
            //loop the number of segments, -1, no segment required from the to node.
            for (var i = 0; i < self.Waypoints.length - 1; i++) {
                self.Segments[i] = new YourNavigation.Segment(this);
                callback("Calculating segment " + (i + 1) + " of " + numsegs + " please wait..");
                self.Segments[i].Start = self.Waypoints[ i ];
                self.Segments[i].End = self.Waypoints[ i + 1 ];
                self.Segments[i].draw(callback);
            }
        } else {
            if ( this.Start.lonlat != undefined && this.End.lonlat != undefined) {
                /* simple route, start and finish */
                self.Layer.destroyFeatures();
                self.Segments[0] = new YourNavigation.Segment(self);
                callback("Calculating route...please wait (for a max. of 1.5 minutes)");
                self.Segments[0].Start = self.Start;
                self.Segments[0].End = self.End;
                self.Segments[0].draw(callback);
            }
        }

    }

    /*
        Function: reverse

        Reverse the Waypoints[] collection and redraw the route

        Parameters:

        callback - Pass a function to handle the result message

    */
    this.reverse = function(callback) {
        //remove all the markers from the marker layer
        this.Layer.destroyFeatures();
        this.Waypoints.reverse();
        for ( var i=0, len=this.Waypoints.length; i<len; ++i ){
            this.Waypoints[i].position = i;
            if(this.Waypoints[i].position == 0) {
                this.Waypoints[i].type = "from";
                this.Start = this.Waypoints[0];
            } else if (this.Waypoints[i].position == this.Waypoints.length - 1) {
                this.Waypoints[i].type = "to";
                this.End = this.Waypoints[i];
            } else {
                this.Waypoints[i].type = "via"
            }
            this.Waypoints[i].draw(this.Waypoints[i].lonlat);
        }
        this.draw(callback);
    }
    this.Waypoints = [];
    this.Segments = [];
    this.distance = parseFloat(0);
    this.reset(map);
}


/*
         Class: YourNavigation.Waypoint

         Parameters:
           type - Type of waypoint, can be one of from/to/via
           label - Label returned from namefinder or entered by user
           position - Position of the waypoint in the route sequence.
               0 = startpoint
               highest val = endpoint
          lonlat -OpenLayers.LonLat object holding the waypoints position
     */
YourNavigation.Waypoint = function(ParentRoute)
{
    this.route = ParentRoute;
    /*
        Function: draw

        Draw a Waypoint on the Markers Layer

        Parameters:

        OpenLayers.LonLat

    */
    this.draw = function(lonlat)
    {
        this.destroy();
        if(lonlat == undefined){
            lonlat = this.lonlat;
        } else {
            this.lonlat = lonlat;
        }
        var marker_url;
        switch (this.type) {
            case 'via':
                marker_url= 'markers/marker-yellow.png';
                break;
            case 'from':
                marker_url= 'markers/marker-green.png';
                break;
            case 'to':
                marker_url= 'markers/marker-red.png';
                break;
            default:
                marker_url= 'markers/marker-yellow.png';
                break;
        }
        /* Waypoint symbol placement */
        var size = new OpenLayers.Size(21,25);
        var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
        var icon = new OpenLayers.Icon(marker_url,
            size,offset);

        /* Create a marker and add it to the marker layer */
        this.marker = new OpenLayers.Marker(lonlat.clone(), icon);

        this.route.Markers.addMarker(this.marker);
    }

    /*
        Function: destroy

        Remove Waypoint from the Markers Layer and destroy it's location information

    */
    this.destroy = function() {
        if (this.marker != undefined)
        {
            this.route.Markers.removeMarker(this.marker);
            this.marker.destroy();
            this.marker= undefined;
            this.lonlat = undefined;
        }
    }
}

/*
    Class: Segment

    Parameters:
        start - a Waypoint Object that initializes the start of the segment
        end - a Waypoint Object that initializes the end of the segment
        distance - the total length of this segment
        parameters - get the routing parameters from the gui
        url - url to have a gosmore instance return the route
        permalink - the permalink for this segment
*/
YourNavigation.Segment = function(ParentRoute) {
    var self = this;
    this.route = ParentRoute;
    /*
        Function: create

        Create a route from the kml file returned from the gosmore service
    */
    this.create = function(xml) {
        if (xml.childNodes.length > 0) {
            // Check to make sure that kml is returned..
            if (xml.childNodes[0].nodeName == "kml") {
                var distance = xml.getElementsByTagName('distance')[0].textContent;
                if(distance == 0 || distance == undefined) {
                    return 'Segment has no length, or kml has no distance attribute';
                } else {
                    var options = {};
                    options.externalProjection = this.route.map.displayProjection;
                    options.internalProjection = this.route.map.projection;
                    var kml = new OpenLayers.Format.KML(options);
                    this.feature = kml.read(xml);
                    if (typeof(this.route.Layer) != 'undefined') {
                        this.route.Layer.addFeatures(this.feature);
                    }
                    this.distance = parseFloat(distance);
                    return 'Segment is of length:' + distance;
                }
            } else {
                return 'Response is no kml, segment cannot be constructed';
            }

        } else {
            return 'No segment found!';
        }
    }

    this.draw = function(callback){
        var flonlat= this.Start.lonlat.clone().transform(this.route.map.projection, this.route.map.displayProjection);
        var tlonlat= this.End.lonlat.clone().transform(this.route.map.projection, this.route.map.displayProjection);

        this.url = 'flat=' + flonlat.lat +
        '&flon=' + flonlat.lon +
        '&tlat=' + tlonlat.lat +
        '&tlon=' + tlonlat.lon;
        this.url += '&v=' + this.route.parameters.vehicle +
        '&fast=' + this.route.parameters.fast +
        '&layer=' + this.route.parameters.layer;
        this.permalink = location.protocol + '//' + location.host + location.pathname + "?" + this.url;
        url = gosmoreUrl + this.url;
        $.get(url, {}, function(xml){
            html = "Status: ready";
            var result = self.create(xml);
            if (result) {
                //TODO: Need a way to trigger when the last segment is drawn!
                callback("segment calculation finished");
            }
        },"xml");
    }

    /*
        Function: profile

        Generate the altitude profile, requires altitude service
    */

    this.profile =function() {
        //TODO: does not work, needs fixing.
        var geom = this.feature[0].geometry.clone();
        var length = geom.components.length;
        //TODO: clearify; why is the length required to be less then 300?
        if (length < 300) {
            // Build the profile GET request
            url = "";
            lats = "?lats=";
            lons ="&lons=";
            for (var i = 0; i < geom.components.length; i++) {
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
            html = $('#feature_info').html();
            if (url.length > 0) {
                url += lats + lons;
                html += '<p>Altitude profile:<br><img src="'+url+'" alt="altitude profile for this route">';
                html += "</p>";
            }
            else {
                html += '<p><font color="red">Note: Altitude profile only available in <a href="http://wiki.openstreetmap.org/index.php/YOURS#Altitude_profile">certain areas</a>.</font></p>';
            }
            return html;
        }
    }

    /*
        Function: directions
        Get directions for the segment
    */
    this.directions = function() {
        if(this.feature == undefined) {
            return 'Feature not set, cannot get directions';
        } else {
            len = this.feature.geometry.getLength();
            return len;
        }
    }
}

