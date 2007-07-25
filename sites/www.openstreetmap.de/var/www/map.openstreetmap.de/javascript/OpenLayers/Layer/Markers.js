/* Copyright (c) 2006 MetaCarta, Inc., published under a modified BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/repository-license.txt 
 * for the full text of the license. */


/**
 * @class
 * 
 * @requires OpenLayers/Layer.js
 */
OpenLayers.Layer.Markers = OpenLayers.Class.create();
OpenLayers.Layer.Markers.prototype = 
  OpenLayers.Class.inherit( OpenLayers.Layer, {
    
    /** Markers layer is never a base layer. 
     * 
     * @type Boolean
     */
    isBaseLayer: false,
    
    /** internal marker list
    * @type Array(OpenLayers.Marker) */
    markers: null,
    
    /**
    * @constructor
    *
    * @param {String} name
    * @param {Object} options Hashtable of extra options to tag onto the layer
    */
    initialize: function(name, options) {
        OpenLayers.Layer.prototype.initialize.apply(this, arguments);
        this.markers = new Array();
    },
    
    /**
     * 
     */
    destroy: function() {
        this.clearMarkers();
        markers = null;
        OpenLayers.Layer.prototype.destroy.apply(this, arguments);
    },

    
    /** 
     * @param {OpenLayers.Bounds} bounds
     * @param {Boolean} zoomChanged
     * @param {Boolean} dragging
     */
    moveTo:function(bounds, zoomChanged, dragging) {
        OpenLayers.Layer.prototype.moveTo.apply(this, arguments);

        if (zoomChanged) {
            this.redraw();
        }
    },

    /**
     * @param {OpenLayers.Marker} marker
     */
    addMarker: function(marker) {
        this.markers.push(marker);
        if (this.map && this.map.getExtent()) {
            marker.map = this.map;
            this.drawMarker(marker);
        }
    },

    /**
     * @param {OpenLayers.Marker} marker
     */
    removeMarker: function(marker) {
        OpenLayers.Util.removeItem(this.markers, marker);
        if ((marker.icon != null) && (marker.icon.imageDiv != null) &&
            (marker.icon.imageDiv.parentNode == this.div) ) {
            this.div.removeChild(marker.icon.imageDiv);    
        }
    },

    /**
     * 
     */
    clearMarkers: function() {
        if (this.markers != null) {
            while(this.markers.length > 0) {
                this.removeMarker(this.markers[0]);
            }
        }
    },

    /** clear all the marker div's from the layer and then redraw all of them.
    *    Use the map to recalculate new placement of markers.
    */
    redraw: function() {
        for(i=0; i < this.markers.length; i++) {
            this.drawMarker(this.markers[i]);
        }
    },

    /** Calculate the pixel location for the marker, create it, and 
    *    add it to the layer's div
    * 
    * @private 
    * 
    * @param {OpenLayers.Marker} marker
    */
    drawMarker: function(marker) {
        var px = this.map.getLayerPxFromLonLat(marker.lonlat);
        if (px == null) {
            marker.display(false);
        } else {
            var markerImg = marker.draw(px);
            if (!marker.drawn) {
                this.div.appendChild(markerImg);
                marker.drawn = true;
            }
        }
    },
    
    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.Markers"
});
