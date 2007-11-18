// This is heavily based on the GeoRSS.js from OpenLayers 2.1, copyright
// notice follows...

/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */

/**
 * @class
 * 
 * @requires OpenLayers/Layer/Markers.js
 */
OpenLayers.Layer.WalkRouteMarkers = OpenLayers.Class.create();
OpenLayers.Layer.WalkRouteMarkers.prototype = 
      OpenLayers.Class.inherit( OpenLayers.Layer.GeoRSS2, {

    /**
    * @constructor
    *
    * @param {String} name
    * @param {String} location
    */

    manager: null,
    annotations: null,

    initialize: function(name, location) {
        OpenLayers.Layer.GeoRSS2.prototype.initialize.apply
                (this, [name,location]);
        this.annotations = new Array();
    },

    setManager: function(manager)
    {
        this.manager=manager;
    },

    /**
     * @param {?} ajaxRequest
     */
    parseAnnotations: function(ajaxRequest) 
    {
        var doc = ajaxRequest.responseXML;
        if (!doc || ajaxRequest.fileType!="XML") 
        {
            doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
        }
        var itemlist = doc.getElementsByTagName('annotation');
        for (var i = 0; i < itemlist.length; i++) 
        {
            var data = {};
            var lat = itemlist[i].getAttribute('lat');
            var lon = itemlist[i].getAttribute('lon');
            var annotation = itemlist[i].firstChild.nodeValue;

            // Convert the lat/lon to another coordinate scheme if specified
            var location = (this.converterFunction ) ?
                this.converterFunction(
                    new OpenLayers.LonLat(parseFloat(lon), 
                                     parseFloat(lat))
                                          ) :
                    new OpenLayers.LonLat(parseFloat(lon), parseFloat(lat));
           

            // Do something with user-specified icons
            data.icon = this.getAnnotationIcon(i+1);

            data.popupSize = new OpenLayers.Size(240, 150);
            contentHTML = "";
        
            if (annotation != null)  
            {
                contentHTML += "<p>" + annotation + "</p>";
                data['popupContentHTML'] = contentHTML;
            }

            var feature = new OpenLayers.Feature(this, location, data);
            feature.id=0;
            var marker = feature.createMarker();
            marker.events.register('click', feature, 
                OpenLayers.Layer.BboxMarkersLayer.prototype.markerClick);
            this.addMarker(marker);
            this.annotations.push(feature);
        }
    },
   
    markerClick: function(evt) 
    {
        var f2 = this.layer.selectedFeature;


        // Load walk route for f1.id
        this.layer.manager.getRouteById(this.id);
    },

    clearAnnotations: function() {
        if (this.annotations != null) {
            while(this.annotations.length > 0) {
                var annotation = this.annotations[0];
                OpenLayers.Util.removeItem(this.annotations,annotation);
                annotation.destroy();
            }
        }        
    },

    /**
     * @param {Event} evt
     */
   
   	getAnnotationIcon: function (id)
	{
		return new OpenLayers.Icon 
			('http://www.free-map.org.uk/freemap/common/flag.php?n='+id, 
			new OpenLayers.Size(32,32));
	},

    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.WalkRouteMarkers"
});
     
    
