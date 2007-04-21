// This is heavily based on the GeoRSS.js from OpenLayers 2.1, copyright
// notice follows...

/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */

function SizedIcon(filename,size) {
	this.filename = filename;
	this.size = size;
}

/**
 * @class
 * 
 * @requires OpenLayers/Layer/Markers.js
 */
OpenLayers.Layer.GeoRSS2 = Class.create();
OpenLayers.Layer.GeoRSS2.prototype = 
  Object.extend( new OpenLayers.Layer.Markers(), {

    /** store url of text file
    * @type str */
    location:null,

    /** @type Array(OpenLayers.Feature) */
    features: null,

    /** @type OpenLayers.Feature */
    selectedFeature: null,


	/** icon map */
	iconMap : null,
	defaultIcon: null,

	converterFunction: null,
	deleteTestFunction: null,
	deleteFunction: null,
	
	popupSize : new OpenLayers.Size(320,200),
	popupColour : "white", 

	nGuidlessMarkers: 0 ,
    /**
    * @constructor
    *
    * @param {String} name
    * @param {String} location
    */
    initialize: function(name, location) {
        OpenLayers.Layer.Markers.prototype.initialize.apply(this, [name]);
        this.location = location;
        this.features = new Array();
        // do not load on creation
		// OpenLayers.loadURL(location, null, this, this.parseData);

		this.iconMap = new Array();
    },

	/**
	 * specify a converter function to convert lat/lon of markers into another 
	 * coord system
	 */
	setConverterFunction: function(f) {
		this.converterFunction = f;
	},
	
	setDeleteTestFunction: function(f) {
		this.deleteTestFunction = f;
	},

	setDeleteFunction: function(f) {
		this.deleteFunction = f;
	},

	setPopupStyle: function(size,colour) {
		this.popupSize = size;
		this.popupColour = colour;
	},

    /**
     * 
     */
	// UNCHANGED
    destroy: function() {
        this.clearFeatures();
        this.features = null;
        OpenLayers.Layer.Markers.prototype.destroy.apply(this, arguments);

		this.iconMap = null; 
    },

	addIcon: function(featureTypeTag, icon, size)  {
		this.iconMap[featureTypeTag] = new SizedIcon (icon,size);
	},

	setDefaultIcon : function (defaultIcon, defaultIconSize) {
		this.defaultIcon = new SizedIcon (defaultIcon, defaultIconSize);
	},

	load: function(bounds) {
		var bboxURL = (bounds) ? this.location + "?bbox="+bounds.toBBOX() :
						this.location;
		OpenLayers.loadURL(bboxURL,null,this,this.parseData);
	},

    /**
     * @param {?} ajaxRequest
     */
    parseData: function(ajaxRequest) {
        var doc = ajaxRequest.responseXML;
        if (!doc || ajaxRequest.fileType!="XML") {
            doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
        }
        this.name = doc.getElementsByTagName("title")[0].firstChild.nodeValue;    
        var itemlist = doc.getElementsByTagName('item');
        for (var i = 0; i < itemlist.length; i++) {
            var data = {};
            var point = OpenLayers.Util.getNodes(itemlist[i], 'georss:point');
            var lat = OpenLayers.Util.getNodes(itemlist[i], 'geo:lat');
            var lon = OpenLayers.Util.getNodes(itemlist[i], 'geo:long');
            if (point.length > 0) {
                var location = point[0].firstChild.nodeValue.split(" ");
                
                if (location.length !=2) {
                    var location = point[0].firstChild.nodeValue.split(",");
                }
            } else if (lat.length > 0 && lon.length > 0) {
                var location = [parseFloat(lat[0].firstChild.nodeValue), 
								parseFloat(lon[0].firstChild.nodeValue)];
            } else {
                continue;
            }
			/*
            location = new OpenLayers.LonLat(parseFloat(location[1]), 
					parseFloat(location[0]));
			*/

			
		
			// Convert the lat/lon to another coordinate scheme if specified
			location = (this.converterFunction ) ?
				this.converterFunction(
					new OpenLayers.LonLat(parseFloat(location[1]), 
									 parseFloat(location[0]))
									 	 ) :
					new OpenLayers.LonLat(parseFloat(location[1]),
										parseFloat(location[0]));
            
            /* Provide defaults for title and description */
            var title = "";
            try {
              title = OpenLayers.Util.getNodes(itemlist[i], 
                        "title")[0].firstChild.nodeValue;
            }
            catch (e) { }
            
            var description = "No description";
            try {
              description = OpenLayers.Util.getNodes(itemlist[i], 
                              "description")[0].firstChild.nodeValue;
            }
            catch (e) {  }

            try { 
				var link = OpenLayers.Util.getNodes(itemlist[i], 
						"link")[0].firstChild.nodeValue; 
			} catch (e) { } 

			var guid; 
			try { 
				guid = OpenLayers.Util.getNodes(itemlist[i],
								"guid")[0].firstChild.nodeValue;
			} catch(e) {  guid = "fmap"+(nGuidlessMarkers++);}

			var featureTypeTag = "other";
			try {
				featureTypeTag = OpenLayers.Util.getNodes(itemlist[i],
						"georss:featuretypetag")[0].firstChild.nodeValue;
			} catch(e) { }


			var pubDate = "";
			try {
				pubDate = OpenLayers.Util.getNodes(itemlist[i],
							"pubDate")[0].firstChild.nodeValue;
			} catch(e) { }

			// Do something with user-specified icons
			data.icon = (this.iconMap[featureTypeTag] ) ?
				new OpenLayers.Icon
					(this.iconMap[featureTypeTag].filename,
				 	this.iconMap[featureTypeTag].size) :
				new OpenLayers.Icon
					(this.defaultIcon.filename, this.defaultIcon.size);

            data.popupSize = new OpenLayers.Size(320, 200);
			contentHTML = "";
            if ((title != null) && (description != null)) {
				contentHTML += "<h1><a href='"+link+"'>" + title + "</a></h1>" 
						+ "<p>"+description+"</p>";
				if(pubDate!="") {
					contentHTML += "<p><em>" + pubDate + 
						"</em><p>";
				}
                data['popupContentHTML'] = contentHTML;
                
            }

			// Only add if it does not already exist
			if (! this.features[guid] ) {
            	var feature = new OpenLayers.Feature(this, location, data);
				feature.id = guid;
            	//this.features.push(feature);

				// Use hash/associative array using the guid as the key
				this.features[guid] = feature;
            	var marker = feature.createMarker();
            	marker.events.register('click', feature, this.markerClick);
            	this.addMarker(marker);
			}
        }
    },
    
    /**
     * @param {Event} evt
     */
	

    markerClick: function(evt) {
		if(this.layer.deleteTestFunction && this.layer.deleteFunction && 
				this.layer.deleteTestFunction()) { 
			this.layer.deleteFunction(this.id);
			this.layer.removeMarker(this.marker);
			this.destroy();
		} else {
        	sameMarkerClicked = (this == this.layer.selectedFeature);
        	this.layer.selectedFeature = (!sameMarkerClicked) ? this : null;
        	for(var i=0; i < this.layer.map.popups.length; i++) {
            	this.layer.map.removePopup(this.layer.map.popups[i]);
        	}
        	if (!sameMarkerClicked) {
            	var popup = this.createPopup();
				popup.setSize(this.layer.popupSize);
				popup.setBackgroundColor(this.layer.popupColour);
            	Event.observe(popup.div, "click",
            	function() { 
              	for(var i=0; i < this.layer.map.popups.length; i++) { 
                	this.layer.map.removePopup(this.layer.map.popups[i]); 
              	  }   
            	}.bindAsEventListener(this));
            	this.layer.map.addPopup(popup); 
        	}
        	Event.stop(evt);
		}
    },

    /**
     * 
     */
	// UNCHANGED
    clearFeatures: function() {
        if (this.features != null) {
            while(this.features.length > 0) {
                var feature = this.features[0];
                this.features.remove(feature);
                feature.destroy();
            }
        }        
    },
    
    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.GeoRSS2"
});
     
    
