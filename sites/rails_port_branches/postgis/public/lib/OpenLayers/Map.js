/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */
// @require: OpenLayers/Util.js
/**
* @class
*
*
*/

OpenLayers.Map = Class.create();
OpenLayers.Map.prototype = {
    // Hash: base z-indexes for different classes of thing 
    Z_INDEX_BASE: { Layer: 100, Popup: 200, Control: 1000 },

    // Array: supported application event types
    EVENT_TYPES: [ 
        "addlayer", "removelayer", "movestart", "move", "moveend",
        "zoomend", "layerchanged", "popupopen", "popupclose",
        "addmarker", "removemarker", "clearmarkers", "mouseover",
        "mouseout", "mousemove", "dragstart", "drag", "dragend" ],

    // int: zoom levels, used to draw zoom dragging control and limit zooming
    maxZoomLevel: 16,

    // OpenLayers.Bounds
    maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),

    /* projection */
    projection: "EPSG:4326",

    /** @type OpenLayers.Size */
    size: null,

    // float
    maxResolution: 1.40625, // degrees per pixel 
                            // Default is whole world in 256 pixels, from GMaps

    // DOMElement: the div that our map lives in
    div: null,

    // HTMLDivElement: the map's view port             
    viewPortDiv: null,

    // HTMLDivElement: the map's layer container
    layerContainerDiv: null,

    // Array(OpenLayers.Layer): ordered list of layers in the map
    layers: null,

    // Array(OpenLayers.Control)
    controls: null,

    // Array(OpenLayers.Popup)
    popups: null,

    // OpenLayers.LonLat
    center: null,

    // int
    zoom: null,

    // OpenLayers.Events
    events: null,

    // OpenLayers.Pixel
    mouseDragStart: null,

    /** @type OpenLayers.Layer */
    baseLayer: null,

    /**
    * @param {DOMElement} div
    */    
    initialize: function (div, options) {
        Object.extend(this, options);

        this.div = div = $(div);

        // the viewPortDiv is the outermost div we modify
        var id = div.id + "_OpenLayers_ViewPort";
        this.viewPortDiv = OpenLayers.Util.createDiv(id, null, null, null,
                                                     "relative", null,
                                                     "hidden");
        this.viewPortDiv.style.width = "100%";
        this.viewPortDiv.style.height = "100%";
        this.div.appendChild(this.viewPortDiv);

        // the layerContainerDiv is the one that holds all the layers
        id = div.id + "_OpenLayers_Container";
        this.layerContainerDiv = OpenLayers.Util.createDiv(id);
        this.viewPortDiv.appendChild(this.layerContainerDiv);

        this.events = new OpenLayers.Events(this, div, this.EVENT_TYPES);

        this.updateSize();
        // make the entire maxExtent fix in zoom level 0 by default
        if (this.maxResolution == null || this.maxResolution == "auto") {
            this.maxResolution = Math.max(
                this.maxExtent.getWidth()  / this.size.w,
                this.maxExtent.getHeight() / this.size.h );
        }
        // update the internal size register whenever the div is resized
        this.events.register("resize", this, this.updateSize);

        this.layers = [];
        
        if (!this.controls) {
            this.controls = [];
            this.addControl(new OpenLayers.Control.MouseDefaults());
            this.addControl(new OpenLayers.Control.PanZoom());
        }

        this.popups = new Array();

        // always call map.destroy()
        Event.observe(window, 'unload', 
            this.destroy.bindAsEventListener(this));
    },

    /**
    * @private
    */
    destroy:function() {
        if (this.layers != null) {
            for(var i=0; i< this.layers.length; i++) {
                this.layers[i].destroy();
            } 
            this.layers = null;
        }
        if (this.controls != null) {
            for(var i=0; i< this.controls.length; i++) {
                this.controls[i].destroy();
            } 
            this.controls = null;
        }
    },

    /**
    * @param {OpenLayers.Layer} layer
    */    
    addLayer: function (layer) {
        layer.setMap(this);
        layer.div.style.overflow = "";
        layer.div.style.zIndex = this.Z_INDEX_BASE['Layer'] + this.layers.length;

        if (layer.viewPortLayer) {
            this.viewPortDiv.appendChild(layer.div);
        } else {
            this.layerContainerDiv.appendChild(layer.div);
        }
        this.layers.push(layer);

        // hack hack hack - until we add a more robust layer switcher,
        //   which is able to determine which layers are base layers and 
        //   which are not (and put baselayers in a radiobutton group and 
        //   other layers in checkboxes) this seems to be the most straight-
        //   forward way of dealing with this. 
        //
        if (layer.isBaseLayer()) {
            this.baseLayer = layer;
        }
        this.events.triggerEvent("addlayer");
    },

    /** Removes a layer from the map by removing its visual element (the 
     *   layer.div property), then removing it from the map's internal list 
     *   of layers, setting the layer's map property to null. 
     * 
     *   a "removelayer" event is triggered.
     * 
     *   very worthy of mention is that simply removing a layer from a map
     *   will not cause the removal of any popups which may have been created
     *   by the layer. this is due to the fact that it was decided at some
     *   point that popups would not belong to layers. thus there is no way 
     *   for us to know here to which layer the popup belongs.
     *    
     *     A simple solution to this is simply to call destroy() on the layer.
     *     the default OpenLayers.Layer class's destroy() function
     *     automatically takes care to remove itself from whatever map it has
     *     been attached to. 
     * 
     *     The correct solution is for the layer itself to register an 
     *     event-handler on "removelayer" and when it is called, if it 
     *     recognizes itself as the layer being removed, then it cycles through
     *     its own personal list of popups, removing them from the map.
     * 
     * @param {OpenLayers.Layer} layer
     */
    removeLayer: function(layer) {
        this.layerContainerDiv.removeChild(layer.div);
        this.layers.remove(layer);
        layer.map = null;
        this.events.triggerEvent("removelayer");
    },

    /**
    * @param {Array(OpenLayers.Layer)} layers
    */    
    addLayers: function (layers) {
        for (var i = 0; i <  layers.length; i++) {
            this.addLayer(layers[i]);
        }
    },

    /**
    * @param {OpenLayers.Control} control
    * @param {OpenLayers.Pixel} px
    */    
    addControl: function (control, px) {
        control.map = this;
        this.controls.push(control);
        var div = control.draw(px);
        if (div) {
            div.style.zIndex = this.Z_INDEX_BASE['Control'] +
                                this.controls.length;
            this.viewPortDiv.appendChild( div );
        }
    },

    /** 
    * @param {OpenLayers.Popup} popup
    */
    addPopup: function(popup) {
        popup.map = this;
        this.popups.push(popup);
        var popupDiv = popup.draw();
        if (popupDiv) {
            popupDiv.style.zIndex = this.Z_INDEX_BASE['Popup'] +
                                    this.popups.length;
            this.layerContainerDiv.appendChild(popupDiv);
        }
    },
    
    /** 
    * @param {OpenLayers.Popup} popup
    */
    removePopup: function(popup) {
        this.popups.remove(popup);
        if (popup.div) {
            this.layerContainerDiv.removeChild(popup.div);
        }
        popup.map = null;
    },
        
    /**
    * @return {float}
    */
    getResolution: function () {
        // return degrees per pixel
        return this.maxResolution / Math.pow(2, this.zoom);
    },

    /**
    * @return {int}
    */
    getZoom: function () {
        return this.zoom;
    },

    /**
    * @returns {OpenLayers.Size}
    */
    getSize: function () {
        return this.size;
    },

    /**
    * @private
    */
    updateSize: function() {
        this.size = new OpenLayers.Size(
                    this.div.clientWidth, this.div.clientHeight);
        this.events.div.offsets = null;
        // Workaround for the fact that hidden elements return 0 for size.
        if (this.size.w == 0 && this.size.h == 0) {
            var dim = Element.getDimensions(this.div);
            this.size.w = dim.width;
            this.size.h = dim.height;
        }
        if (this.size.w == 0 && this.size.h == 0) {
            this.size.w = parseInt(this.div.style.width);
            this.size.h = parseInt(this.div.style.height);
    	}
    },

    /**
    * @return {OpenLayers.LonLat}
    */
    getCenter: function () {
        return this.center;
    },

    /**
    * @return {OpenLayers.Bounds}
    */
    getExtent: function () {
        if (this.center) {
            var res = this.getResolution();
            var size = this.getSize();
            var w_deg = size.w * res;
            var h_deg = size.h * res;
            return new OpenLayers.Bounds(
                this.center.lon - w_deg / 2, 
                this.center.lat - h_deg / 2,
                this.center.lon + w_deg / 2,
                this.center.lat + h_deg / 2);
        } else {
            return null;
        }
    },

    /**
    * @return {OpenLayers.Bounds}
    */
    getFullExtent: function () {
        return this.maxExtent;
    },
    
    getZoomLevels: function() {
        return this.maxZoomLevel;
    },

    /**
    * @param {OpenLayers.Bounds} bounds
    *
    * @return {int}
    */
    getZoomForExtent: function (bounds) {
        var size = this.getSize();
        var width = bounds.getWidth();
        var height = bounds.getHeight();
        var deg_per_pixel = (width > height ? width / size.w : height / size.h);
        var zoom = Math.log(this.maxResolution / deg_per_pixel) / Math.log(2);
        return Math.floor(Math.min(Math.max(zoom, 0), this.getZoomLevels())); 
    },
    
    /**
     * @param {OpenLayers.Pixel} layerPx
     * 
     * @returns px translated into view port pixel coordinates
     * @type OpenLayers.Pixel
     * @private
     */
    getViewPortPxFromLayerPx:function(layerPx) {
        var viewPortPx = layerPx.copyOf();

        viewPortPx.x += parseInt(this.layerContainerDiv.style.left);
        viewPortPx.y += parseInt(this.layerContainerDiv.style.top);

        return viewPortPx;
    },
    
    /**
     * @param {OpenLayers.Pixel} viewPortPx
     * 
     * @returns px translated into view port pixel coordinates
     * @type OpenLayers.Pixel
     * @private
     */
    getLayerPxFromViewPortPx:function(viewPortPx) {
        var layerPx = viewPortPx.copyOf();

        layerPx.x -= parseInt(this.layerContainerDiv.style.left);
        layerPx.y -= parseInt(this.layerContainerDiv.style.top);

        return layerPx;
    },


    /**
    * @param {OpenLayers.Pixel} px
    *
    * @return {OpenLayers.LonLat} 
    */
    getLonLatFromLayerPx: function (px) {
       //adjust for displacement of layerContainerDiv
       px = this.getViewPortPxFromLayerPx(px);
       return this.getLonLatFromViewPortPx(px);         
    },
    
    /**
    * @param {OpenLayers.Pixel} viewPortPx
    *
    * @returns An OpenLayers.LonLat which is the passed-in view port
    *          OpenLayers.Pixel, translated into lon/lat given the 
    *          current extent and resolution
    * @type OpenLayers.LonLat
    * @private
    */
    getLonLatFromViewPortPx: function (viewPortPx) {
        var center = this.getCenter();        //map center lon/lat
        var res  = this.getResolution();
        var size = this.getSize();
    
        var delta_x = viewPortPx.x - (size.w / 2);
        var delta_y = viewPortPx.y - (size.h / 2);
        
        return new OpenLayers.LonLat(center.lon + delta_x * res ,
                                     center.lat - delta_y * res); 
    },

    // getLonLatFromPixel is a convenience function for the API
    /**
    * @param {OpenLayers.Pixel} pixel
    *
    * @returns An OpenLayers.LonLat corresponding to the given
    *          OpenLayers.Pixel, translated into lon/lat using the 
    *          current extent and resolution
    * @type OpenLayers.LonLat
    */
    getLonLatFromPixel: function (px) {
	return this.getLonLatFromViewPortPx(px);
    },

    /**
    * @param {OpenLayers.LonLat} lonlat
    *
    * @returns An OpenLayers.Pixel which is the passed-in OpenLayers.LonLat, 
    *          translated into layer pixels given the current extent 
    *          and resolution
    * @type OpenLayers.Pixel
    */
    getLayerPxFromLonLat: function (lonlat) {
       //adjust for displacement of layerContainerDiv
       var px = this.getViewPortPxFromLonLat(lonlat);
       return this.getLayerPxFromViewPortPx(px);         
    },

    /**
    * @param {OpenLayers.LonLat} lonlat
    *
    * @returns An OpenLayers.Pixel which is the passed-in OpenLayers.LonLat, 
    *          translated into view port pixels given the current extent 
    *          and resolution
    * @type OpenLayers.Pixel
    * @private
    */
    getViewPortPxFromLonLat: function (lonlat) {
        var resolution = this.getResolution();
        var extent = this.getExtent();
        return new OpenLayers.Pixel(
                       Math.round(1/resolution * (lonlat.lon - extent.left)),
                       Math.round(1/resolution * (extent.top - lonlat.lat))
                       );    
    },

    // getLonLatFromPixel is a convenience function for the API
    /**
    * @param {OpenLayers.LonLat} lonlat
    *
    * @returns An OpenLayers.Pixel corresponding to the OpenLayers.LonLat
    *          translated into view port pixels using the current extent 
    *          and resolution
    * @type OpenLayers.Pixel
    */
    getPixelFromLonLat: function (lonlat) {
	return this.getViewPortPxFromLonLat(lonlat);
    },

    /**
    * @param {OpenLayers.LonLat} lonlat
    * @param {int} zoom
    */
    setCenter: function (lonlat, zoom, minor) {
        if (this.center) { // otherwise there's nothing to move yet
            this.moveLayerContainer(lonlat);
        }
        this.center = lonlat.copyOf();
        var zoomChanged = null;
        if (zoom != null && zoom != this.zoom 
            && zoom >= 0 && zoom <= this.getZoomLevels()) {
            zoomChanged = (this.zoom == null ? 0 : this.zoom);
            this.zoom = zoom;
        }

        if (!minor) this.events.triggerEvent("movestart");
        this.moveToNewExtent(zoomChanged, minor);
        if (!minor) this.events.triggerEvent("moveend");
    },
    
    /**
     * ZOOM TO BOUNDS FUNCTION
     * @private
     */
    moveToNewExtent: function (zoomChanged, minor) {
        if (zoomChanged != null) { // reset the layerContainerDiv's location
            this.layerContainerDiv.style.left = "0px";
            this.layerContainerDiv.style.top  = "0px";

            //redraw popups
            for (var i = 0; i < this.popups.length; i++) {
                this.popups[i].updatePosition();
            }

        }
        var bounds = this.getExtent();
        for (var i = 0; i < this.layers.length; i++) {
            this.layers[i].moveTo(bounds, (zoomChanged != null), minor);
        }
        this.events.triggerEvent("move");
        if (zoomChanged != null)
            this.events.triggerEvent("zoomend", 
                {oldZoom: zoomChanged, newZoom: this.zoom});
    },

    /**
     * zoomIn
     * Increase zoom level by one.
     * @param {int} zoom
     */
    zoomIn: function() {
        if (this.zoom != null && this.zoom <= this.getZoomLevels()) {
            this.zoomTo( this.zoom += 1 );
        }
    },
    
    /**
     * zoomTo
     * Set Zoom To int
     * @param {int} zoom
     */
    zoomTo: function(zoom) {
       if (zoom >= 0 && zoom <= this.getZoomLevels()) {
            var oldZoom = this.zoom;
            this.zoom = zoom;
            this.moveToNewExtent(oldZoom);
       }
    },

    /**
     * zoomOut
     * Decrease zoom level by one.
     * @param {int} zoom
     */
    zoomOut: function() {
        if (this.zoom != null && this.zoom > 0) {
            this.zoomTo( this.zoom - 1 );
        }
    },
    
    /**
     * zoomToFullExtent
     * Zoom to the full extent and recenter.
     */
    zoomToFullExtent: function() {
        var fullExtent = this.getFullExtent();
        this.setCenter(
          new OpenLayers.LonLat((fullExtent.left+fullExtent.right)/2,
                                (fullExtent.bottom+fullExtent.top)/2),
          this.getZoomForExtent(fullExtent)
        );
    },

    /**
    * @param {OpenLayers.LonLat} lonlat
    * @private
    */
    moveLayerContainer: function (lonlat) {
        var container = this.layerContainerDiv;
        var resolution = this.getResolution();

        var deltaX = Math.round((this.center.lon - lonlat.lon) / resolution);
        var deltaY = Math.round((this.center.lat - lonlat.lat) / resolution);
     
        var offsetLeft = parseInt(container.style.left);
        var offsetTop  = parseInt(container.style.top);

        container.style.left = (offsetLeft + deltaX) + "px";
        container.style.top  = (offsetTop  - deltaY) + "px";
    },

    CLASS_NAME: "OpenLayers.Map"
};
