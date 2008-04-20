/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */
/**
 * @class
 */
OpenLayers.Feature = Class.create();
OpenLayers.Feature.prototype= {

    /** @type OpenLayers.Events */
    events:null,

    /** @type OpenLayers.Layer */
    layer: null,

    /** @type String */
    id: null,
    
    /** @type OpenLayers.LonLat */
    lonlat:null,

    /** @type Object */
    data:null,

    /** @type OpenLayers.Marker */
    marker: null,

    /** @type OpenLayers.Popup */
    popup: null,

    /** 
     * @constructor
     * 
     * @param {OpenLayers.Layer} layer
     * @param {String} id
     * @param {OpenLayers.LonLat} lonlat
     * @param {Object} data
     */
    initialize: function(layer, lonlat, data, id) {
        this.layer = layer;
        this.lonlat = lonlat;
        this.data = (data != null) ? data : new Object();
        this.id = (id ? id : 'f' + Math.random());
    },

    /**
     * 
     */
    destroy: function() {

        //remove the popup from the map
        if ((this.layer != null) && (this.layer.map != null)) {
            if (this.popup != null) {
                this.layer.map.removePopup(this.popup);
            }
        }

        this.events = null;
        this.layer = null;
        this.id = null;
        this.lonlat = null;
        this.data = null;
        if (this.marker != null) {
            this.marker.destroy();
            this.marker = null;
        }
        if (this.popup != null) {
            this.popup.destroy();
            this.popup = null;
        }
    },
    

    /**
     * @returns A Marker Object created from the 'lonlat' and 'icon' properties
     *          set in this.data. If no 'lonlat' is set, returns null. If no
     *          'icon' is set, OpenLayers.Marker() will load the default image
     * @type OpenLayers.Marker
     */
    createMarker: function() {

        var marker = null;
        
        if (this.lonlat != null) {
            this.marker = new OpenLayers.Marker(this.lonlat, this.data.icon);
        }
        return this.marker;
    },

    /**
     * 
     */
    createPopup: function() {

        if (this.lonlat != null) {
            
            var id = this.id + "_popup";
            var anchor = (this.marker) ? this.marker.icon : null;

            this.popup = new OpenLayers.Popup.AnchoredBubble(id, 
                                                    this.lonlat,
                                                    this.data.popupSize,
                                                    this.data.popupContentHTML,
                                                    anchor); 
        }        
        return this.popup;
    },

    CLASS_NAME: "OpenLayers.Feature"
};
