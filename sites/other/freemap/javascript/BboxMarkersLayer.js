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
OpenLayers.Layer.BboxMarkersLayer = OpenLayers.Class.create();
OpenLayers.Layer.BboxMarkersLayer.prototype = 
//  OpenLayers.Util.extend( new OpenLayers.Layer.Markers(), {
	  OpenLayers.Class.inherit( OpenLayers.Layer.Markers, {

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

		this.iconMap = new Array();
    },

	/**
	 * specify a converter function to convert lat/lon of markers into another 
	 * coord system
	 */
	setConverterFunction: function(f) {
		this.converterFunction = f;
	},
	
	setPopupStyle: function(size,colour) {
		this.popupSize = size;
		this.popupColour = colour;
	},

	addMarker: function(marker)
	{
		this.markers.push(marker);
		marker.map = this.map;
		this.drawMarker(marker);
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

	getIcon: function(type) {
		return (this.iconMap[type]) ? this.iconMap[type] : this.defaultIcon;
	},

	load: function(bounds) {
		var bboxURL = (bounds) ? this.location + 
			"?action=get&bbox="+bounds.toBBOX() : this.location; 
		OpenLayers.loadURL(bboxURL,null,this,this.parseData);
	},

    /**
     * @param {?} ajaxRequest
     */
    parseData: function(ajaxRequest) {
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
				// This breaks in 2.2
				//popup.setSize(this.layer.popupSize);
				popup.setBackgroundColor(this.layer.popupColour);
            	OpenLayers.Event.observe(popup.div, "click",
            	function() { 
              	for(var i=0; i < this.layer.map.popups.length; i++) { 
                	this.layer.map.removePopup(this.layer.map.popups[i]); 
              	  }   
            	}.bindAsEventListener(this));
            	this.layer.map.addPopup(popup); 
        	}
        	OpenLayers.Event.stop(evt);
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
   
   	getDefaultOLIcon: function()
	{
		return new OpenLayers.Icon 
			(this.defaultIcon.filename, this.defaultIcon.size);
	},

    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.BboxMarkersLayer"
});
     
    
