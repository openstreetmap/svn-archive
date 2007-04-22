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
OpenLayers.Layer.GGKML = OpenLayers.Class.create();
OpenLayers.Layer.GGKML.prototype = 
//  OpenLayers.Util.extend( new OpenLayers.Layer.Markers(), {
	  OpenLayers.Class.inherit( OpenLayers.Layer.BboxMarkersLayer, {

    /**
    * @constructor
    *
    * @param {String} name
    * @param {String} location
    */
    initialize: function(name, location) {
        OpenLayers.Layer.BboxMarkersLayer.prototype.initialize.apply
			(this, [name,location]);
    },

    /**
     * @param {?} ajaxRequest
     */
    parseData: function(ajaxRequest) {
        var doc = ajaxRequest.responseXML;
        if (!doc || ajaxRequest.fileType!="XML") {
            doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
        }
		//alert(ajaxRequest.responseText);
        var itemlist = doc.getElementsByTagName('Document')[0].getElementsByTagName('Placemark');
        for (var i = 0; i < itemlist.length; i++) {
			//alert(i);
            var data = {};
			var name0 = OpenLayers.Util.getNodes(itemlist[i],"name")[0];
			var name = name0.firstChild.nodeValue;
			//alert(name);
            var description = 
				OpenLayers.Util.getNodes(itemlist[i], 'description')[0].firstChild.nodeValue;
            var c  = OpenLayers.Util.getNodes
				(itemlist[i].getElementsByTagName('Point')[0],'coordinates')[0].firstChild.nodeValue;
			//alert(c);
			var c1 = c.split(",");
			var lon = c1[0];
			var lat = c1[1];
		
			// Convert the lat/lon to another coordinate scheme if specified
			/*
			if(this.converterFunction)
				alert('there is a converted fucntom');
				*/
			var loca= (this.converterFunction ) ?
				this.converterFunction(
					new OpenLayers.LonLat(parseFloat(lon), 
									 parseFloat(lat) )
									 	 ) :
					new OpenLayers.LonLat(parseFloat(lon),
										parseFloat(lat));
            

			//alert(loca.lon+' '+loca.lat);
			// Do something with user-specified icons
			data.icon = 
				new OpenLayers.Icon
					(this.defaultIcon.filename, this.defaultIcon.size);

			//alert('ccc');
            data.popupSize = new OpenLayers.Size(320, 200);
			contentHTML = "";
			//contentHTML = "<div style='overflow:auto'>";

            if (description != null) {
				contentHTML += description; 
            }
			//contentHTML += "</div>";
			data['popupContentHTML'] = contentHTML;

			//alert('ddd');
			// Only add if it does not already exist
			if (! (this.features[name])) {
            	var feature = new OpenLayers.Feature(this, loca, data);
				//feature.id = guid;
            	//this.features.push(feature);

				// Use hash/associative array using the guid as the key
				this.features[name] = feature;
            	var marker = feature.createMarker();
            	marker.events.register('click', feature, this.markerClick);
            	this.addMarker(marker);
			}
        }
    },
    
    /**
     * @param {Event} evt
     */
	

    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.GGKML"
});
     
    
