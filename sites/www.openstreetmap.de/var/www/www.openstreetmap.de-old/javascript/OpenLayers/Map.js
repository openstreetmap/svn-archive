/* Copyright (c) 2006 MetaCarta, Inc., published under a modified BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/repository-license.txt 
 * for the full text of the license. */


/**
 * @class
 * 
 * @requires OpenLayers/Util.js
 * @requires OpenLayers/Events.js
 */
OpenLayers.Map = OpenLayers.Class.create();
OpenLayers.Map.TILE_WIDTH = 256;
OpenLayers.Map.TILE_HEIGHT = 256;
OpenLayers.Map.prototype = {
    
    /** base z-indexes for different classes of thing 
     * 
     * @type Object
     */
    Z_INDEX_BASE: { BaseLayer: 100, Overlay: 325, Popup: 750, Control: 1000 },

    /** supported application event types
     * 
     * @type Array */
    EVENT_TYPES: [ 
        "addlayer", "removelayer", "changelayer", "movestart", "move", 
        "moveend", "zoomend", "popupopen", "popupclose",
        "addmarker", "removemarker", "clearmarkers", "mouseover",
        "mouseout", "mousemove", "dragstart", "drag", "dragend",
        "changebaselayer"],

    /** @type OpenLayers.Events */
    events: null,

    /** the div that our map lives in
     * 
     * @type DOMElement */
    div: null,

    /** Size of the main div (this.div)
     * 
     * @type OpenLayers.Size */
    size: null,
    
    /** @type HTMLDivElement  */
    viewPortDiv: null,

    /** The lonlat at which the later container was re-initialized (on-zoom)
     * @type OpenLayers.LonLat */
    layerContainerOrigin: null,

    /** @type HTMLDivElement */
    layerContainerDiv: null,

    /** ordered list of layers in the map
     * 
     * @type Array(OpenLayers.Layer)
     */
    layers: null,

    /** @type Array(OpenLayers.Control) */
    controls: null,

    /** @type Array(OpenLayers.Popup) */
    popups: null,

    /** The currently selected base layer - this determines min/max zoom level, 
     *  projection, etc.
     * 
     * @type OpenLayers.Layer */
    baseLayer: null,
    
    /** @type OpenLayers.LonLat */
    center: null,

    /** @type int */
    zoom: 0,    

  // Options

    /** @type OpenLayers.Size */
    tileSize: null,

    /** @type String */
    projection: "EPSG:4326",    
        
    /** @type String */
    units: 'degrees',

    /** default max is 360 deg / 256 px, which corresponds to
     *    zoom level 0 on gmaps
     * 
     * @type float */
    maxResolution: 1.40625,

    /** @type float */
    minResolution: null,

    /** @type float */
    maxScale: null,

    /** @type float */
    minScale: null,

    /** @type OpenLayers.Bounds */
    maxExtent: null,
    
    /** @type OpenLayers.Bounds */
    minExtent: null,
    
    /** @type int */
    numZoomLevels: 16,

    /** @type string */
    theme: null,

    /**
     * @constructor
     * 
     * @param {DOMElement} div
     * @param {Object} options Hashtable of extra options to tag onto the map
     */    
    initialize: function (div, options) {

        this.div = div = $(div);

        // the viewPortDiv is the outermost div we modify
        var id = div.id + "_OpenLayers_ViewPort";
        this.viewPortDiv = OpenLayers.Util.createDiv(id, null, null, null,
                                                     "relative", null,
                                                     "hidden");
        this.viewPortDiv.style.width = "100%";
        this.viewPortDiv.style.height = "100%";
        this.viewPortDiv.className = "olMapViewport";
        this.div.appendChild(this.viewPortDiv);

        // the layerContainerDiv is the one that holds all the layers
        id = div.id + "_OpenLayers_Container";
        this.layerContainerDiv = OpenLayers.Util.createDiv(id);
        this.layerContainerDiv.style.zIndex=this.Z_INDEX_BASE['Popup']-1;
        
        this.viewPortDiv.appendChild(this.layerContainerDiv);

        this.events = new OpenLayers.Events(this, div, this.EVENT_TYPES);

        this.updateSize();
 
    // Because Mozilla does not support the "resize" event for elements other
    //  than "window", we need to put a hack here. 
    // 
        if (navigator.appName.contains("Microsoft")) {
            // If IE, register the resize on the div
            this.events.register("resize", this, this.updateSize);
        } else {
            // Else updateSize on catching the window's resize
            //  Note that this is ok, as updateSize() does nothing if the 
            //  map's size has not actually changed.
            OpenLayers.Event.observe(window, 'resize', 
                          this.updateSize.bindAsEventListener(this));
        }
        
        //set the default options
        this.setOptions(options);
        
        var cssNode = document.createElement('link');
        cssNode.setAttribute('rel', 'stylesheet');
        cssNode.setAttribute('type', 'text/css');
        cssNode.setAttribute('href', this.theme);
        document.getElementsByTagName('head')[0].appendChild(cssNode); 

        this.layers = [];
        
        if (this.controls == null) {
            if (OpenLayers.Control != null) { // running full or lite?
                this.controls = [ new OpenLayers.Control.MouseDefaults(),
                                  new OpenLayers.Control.PanZoom(),
                                  new OpenLayers.Control.ArgParser()
                                ];
            } else {
                this.controls = [];
            }
        }

        for(var i=0; i < this.controls.length; i++) {
            this.addControlToMap(this.controls[i]);
        }

        this.popups = new Array();

        // always call map.destroy()
        OpenLayers.Event.observe(window, 
                      'unload', 
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
     * @private
     * 
     * @param {Object} options Hashtable of options to tag to the map
     */
    setOptions: function(options) {

        // Simple-type defaults are set in class definition. 
        //  Now set complex-type defaults 
        this.tileSize = new OpenLayers.Size(OpenLayers.Map.TILE_WIDTH,
                                            OpenLayers.Map.TILE_HEIGHT);
        
        this.maxExtent = new OpenLayers.Bounds(-180, -90, 180, 90);

        this.theme = OpenLayers._getScriptLocation() + 
                             'theme/default/style.css'; 

        // now add the options declared by the user
        //  (these will override defaults)
        OpenLayers.Util.extend(this, options);
    },

    /**
     * @type OpenLayers.Size
     */
     getTileSize: function() {
         return this.tileSize;
     },

  /********************************************************/
  /*                                                      */
  /*           Layers, Controls, Popup Functions          */
  /*                                                      */
  /*     The following functions deal with adding and     */
  /*        removing Layers, Controls, and Popups         */
  /*                to and from the Map                   */
  /*                                                      */
  /********************************************************/         

    /**
     * @param {String} name
     * 
     * @returns The Layer with the corresponding id from the map's 
     *           layer collection, or null if not found.
     * @type OpenLayers.Layer
     */
    getLayer: function(id) {
        var foundLayer = null;
        for (var i = 0; i < this.layers.length; i++) {
            var layer = this.layers[i];
            if (layer.id == id) {
                foundLayer = layer;
            }
        }
        return foundLayer;
    },

    /**
    * @param {OpenLayers.Layer} layer
    * @param {int} zIdx
    * @private
    */    
    setLayerZIndex: function (layer, zIdx) {
        layer.setZIndex(
            this.Z_INDEX_BASE[layer.isBaseLayer ? 'BaseLayer' : 'Overlay']
            + zIdx * 5 );
    },

    /**
    * @param {OpenLayers.Layer} layer
    */    
    addLayer: function (layer) {
        layer.div.style.overflow = "";
        this.setLayerZIndex(layer, this.layers.length);

        if (layer.isFixed) {
            this.viewPortDiv.appendChild(layer.div);
        } else {
            this.layerContainerDiv.appendChild(layer.div);
        }
        this.layers.push(layer);
        layer.setMap(this);

        if (layer.isBaseLayer)  {
            if (this.baseLayer == null) {
                // set the first baselaye we add as the baselayer
                this.setBaseLayer(layer);
            } else {
                layer.setVisibility(false);
            }
        } else {
            if (this.getCenter() != null) {
                layer.moveTo(this.getExtent(), true);   
            }
        }

        this.events.triggerEvent("addlayer");
    },

    /**
    * @param {Array(OpenLayers.Layer)} layers
    */    
    addLayers: function (layers) {
        for (var i = 0; i <  layers.length; i++) {
            this.addLayer(layers[i]);
        }
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
        if (layer.isFixed) {
            this.viewPortDiv.removeChild(layer.div);
        } else {
            this.layerContainerDiv.removeChild(layer.div);
        }
        layer.map = null;
        OpenLayers.Util.removeItem(this.layers, layer);

        // if we removed the base layer, need to set a new one
        if (this.baseLayer == layer) {
            this.baseLayer = null;
            for(i=0; i < this.layers.length; i++) {
                var iLayer = this.layers[i];
                if (iLayer.isBaseLayer) {
                    this.setBaseLayer(iLayer);
                    break;
                }
            }
        }
        this.events.triggerEvent("removelayer");
    },

    /**
    * @returns The number of layers attached to the map.
    * @type int
    */
    getNumLayers: function () {
        return this.layers.length;
    },

    /** 
    * @returns The current (zero-based) index of the given layer in the map's
    *     layer stack. Returns -1 if the layer isn't on the map.
    *
    * @param {OpenLayers.Layer} layer
    * @type int
    */
    getLayerIndex: function (layer) {
        return OpenLayers.Util.indexOf(this.layers, layer);
    },
    
    /** Move the given layer to the specified (zero-based) index in the layer
    *     list, changing its z-index in the map display. Use
    *     map.getLayerIndex() to find out the current index of a layer. Note
    *     that this cannot (or at least should not) be effectively used to
    *     raise base layers above overlays.
    *
    * @param {OpenLayers.Layer} layer
    * @param {int} idx
    */
    setLayerIndex: function (layer, idx) {
        var base = this.getLayerIndex(layer);
        if (idx < 0) 
            idx = 0;
        else if (idx > this.layers.length)
            idx = this.layers.length;
        if (base != idx) {
            this.layers.splice(base, 1);
            this.layers.splice(idx, 0, layer);
            for (var i = 0; i < this.layers.length; i++)
                this.setLayerZIndex(this.layers[i], i);
            this.events.triggerEvent("changelayer");
        }
    },

    /** Change the index of the given layer by delta. If delta is positive, 
    *     the layer is moved up the map's layer stack; if delta is negative,
    *     the layer is moved down.  Again, note that this cannot (or at least
    *     should not) be effectively used to raise base layers above overlays.
    *
    * @param {OpenLayers.Layer} layer
    * @param {int} idx
    */
    raiseLayer: function (layer, delta) {
        var idx = this.getLayerIndex(layer) + delta;
        this.setLayerIndex(layer, idx);
    },
    
    /** Allows user to specify one of the currently-loaded layers as the Map's
     *   new base layer.
     * 
     * @param {OpenLayers.Layer} newBaseLayer
     * @param {Boolean} noEvent
     */
    setBaseLayer: function(newBaseLayer, noEvent) {
        var oldExtent = null;
        if(this.baseLayer) {
            oldExtent = this.baseLayer.getExtent();
        }

        if (newBaseLayer != this.baseLayer) {
          
            // is newBaseLayer an already loaded layer?
            if (OpenLayers.Util.indexOf(this.layers, newBaseLayer) != -1) {

                // make the old base layer invisible 
                if (this.baseLayer != null) {
                    this.baseLayer.setVisibility(false, noEvent);
                }

                // set new baselayer and make it visible
                this.baseLayer = newBaseLayer;
                this.baseLayer.setVisibility(true, noEvent);

                //redraw all layers
                var center = this.getCenter();
                if (center != null) {
                    if (oldExtent == null) {
                        this.setCenter(center);            
                    } else {
                        this.zoomToExtent(oldExtent);
                    }
                }

                if ((noEvent == null) || (noEvent == false)) {
                    this.events.triggerEvent("changebaselayer");
                }
            }        
        }
    },

    /**
    * @param {OpenLayers.Control} control
    * @param {OpenLayers.Pixel} px
    */    
    addControl: function (control, px) {
        this.controls.push(control);
        this.addControlToMap(control, px);
    },

    /**
     * @private
     * 
     * @param {OpenLayers.Control} control
     * @param {OpenLayers.Pixel} px
     */    
    addControlToMap: function (control, px) {
        // If a control doesn't have a div at this point, it belongs in the
        // viewport.
        control.outsideViewport = (control.div != null);
        control.setMap(this);
        var div = control.draw(px);
        if (div) {
            if(!control.outsideViewport) {
                div.style.zIndex = this.Z_INDEX_BASE['Control'] +
                                    this.controls.length;
                this.viewPortDiv.appendChild( div );
            }
        }
    },
    
    /** 
    * @param {OpenLayers.Popup} popup
    * @param {Boolean} exclusive If true, closes all other popups first
    */
    addPopup: function(popup, exclusive) {

        if (exclusive) {
            //remove all other popups from screen
            for(var i=0; i < this.popups.length; i++) {
                this.removePopup(this.popups[i]);
            }
        }

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
        OpenLayers.Util.removeItem(this.popups, popup);
        if (popup.div) {
            try { this.layerContainerDiv.removeChild(popup.div); }
            catch (e) { } // Popups sometimes apparently get disconnected
                      // from the layerContainerDiv, and cause complaints.
        }
        popup.map = null;
    },

  /********************************************************/
  /*                                                      */
  /*              Container Div Functions                 */
  /*                                                      */
  /*   The following functions deal with the access to    */
  /*    and maintenance of the size of the container div  */
  /*                                                      */
  /********************************************************/     

    /**
    * @returns An OpenLayers.Size object that represents the size, in pixels, 
    *          of the div into which OpenLayers has been loaded. 
    * 
    *          Note: A clone() of this locally cached variable is returned, so 
    *                as not to allow users to modify it.
    * 
    * @type OpenLayers.Size
    */
    getSize: function () {
        var size = null;
        if (this.size != null) {
            size = this.size.clone();
        }
        return size;
    },

    /**
    * This function should be called by any external code which dynamically
    * changes the size of the map div (because mozilla wont let us catch the
    * "onresize" for an element)
    */
    updateSize: function() {
        var newSize = this.getCurrentSize();
        var oldSize = this.getSize();
        if (oldSize == null)
            this.size = oldSize = newSize;
        if (!newSize.equals(oldSize)) {
            
            //notify layers of mapresize
            for(var i=0; i < this.layers.length; i++) {
                this.layers[i].onMapResize();                
            }
            
            // store the new size
            this.size = newSize;
            // the div might have moved on the page, also
            this.events.element.offsets = null;

            if (this.baseLayer != null) {
                var center = new OpenLayers.Pixel(newSize.w /2, newSize.h / 2);
                var centerLL = this.getLonLatFromViewPortPx(center);
                var zoom = this.getZoom();
                this.zoom = null;
                this.setCenter(this.getCenter(), zoom);
            }

        }
    },
    
    /**
     * @private 
     * 
     * @returns A new OpenLayers.Size object with the dimensions of the map div
     * @type OpenLayers.Size
     */
    getCurrentSize: function() {

        var size = new OpenLayers.Size(this.div.clientWidth, 
                                       this.div.clientHeight);

        // Workaround for the fact that hidden elements return 0 for size.
        if (size.w == 0 && size.h == 0) {
            var dim = OpenLayers.Element.getDimensions(this.div);
            size.w = dim.width;
            size.h = dim.height;
        }
        if (size.w == 0 && size.h == 0) {
            size.w = parseInt(this.div.style.width);
            size.h = parseInt(this.div.style.height);
        }
        return size;
    },

    /** 
     * @param {OpenLayers.LonLat} center Default is this.getCenter()
     * @param {float} resolution Default is this.getResolution() 
     * 
     * @returns A Bounds based on resolution, center, and current mapsize.
     * @type OpenLayers.Bounds
     */
    calculateBounds: function(center, resolution) {

        var extent = null;
        
        if (center == null) {
            center = this.getCenter();
        }                
        if (resolution == null) {
            resolution = this.getResolution();
        }
    
        if ((center != null) && (resolution != null)) {

            var size = this.getSize();
            var w_deg = size.w * resolution;
            var h_deg = size.h * resolution;
        
            extent = new OpenLayers.Bounds(center.lon - w_deg / 2,
                                           center.lat - h_deg / 2,
                                           center.lon + w_deg / 2,
                                           center.lat + h_deg / 2);
        
        }

        return extent;
    },


  /********************************************************/
  /*                                                      */
  /*            Zoom, Center, Pan Functions               */
  /*                                                      */
  /*    The following functions handle the validation,    */
  /*   getting and setting of the Zoom Level and Center   */
  /*       as well as the panning of the Map              */
  /*                                                      */
  /********************************************************/
    /**
    * @return {OpenLayers.LonLat}
    */
    getCenter: function () {
        return this.center;
    },


    /**
    * @return {int}
    */
    getZoom: function () {
        return this.zoom;
    },
    
    /** Allows user to pan by a value of screen pixels
     * 
     * @param {int} dx
     * @param {int} dy
     */
    pan: function(dx, dy) {

        // getCenter
        var centerPx = this.getViewPortPxFromLonLat(this.getCenter());

        // adjust
        var newCenterPx = centerPx.add(dx, dy);
        
        // only call setCenter if there has been a change
        if (!newCenterPx.equals(centerPx)) {
            var newCenterLonLat = this.getLonLatFromViewPortPx(newCenterPx);
            this.setCenter(newCenterLonLat);
        }

   },

    /**
    * @param {OpenLayers.LonLat} lonlat
    * @param {int} zoom
    * @param {Boolean} dragging Specifies whether or not to 
    *                           trigger movestart/end events
    */
    setCenter: function (lonlat, zoom, dragging) {
        
        if (!this.center && !this.isValidLonLat(lonlat)) {
            lonlat = this.maxExtent.getCenterLonLat();
        }
        
        var zoomChanged = (this.isValidZoomLevel(zoom)) && 
                          (zoom != this.getZoom());

        var centerChanged = (this.isValidLonLat(lonlat)) && 
                            (!lonlat.equals(this.center));


        // if neither center nor zoom will change, no need to do anything
        if (zoomChanged || centerChanged || !dragging) {

            if (!dragging) { this.events.triggerEvent("movestart"); }

            if (centerChanged) {
                if ((!zoomChanged) && (this.center)) { 
                    // if zoom hasnt changed, just slide layerContainer
                    //  (must be done before setting this.center to new value)
                    this.centerLayerContainer(lonlat);
                }
                this.center = lonlat.clone();
            }

            // (re)set the layerContainerDiv's location
            if ((zoomChanged) || (this.layerContainerOrigin == null)) {
                this.layerContainerOrigin = this.center.clone();
                this.layerContainerDiv.style.left = "0px";
                this.layerContainerDiv.style.top  = "0px";
            }

            if (zoomChanged) {
                this.zoom = zoom;
                    
                //redraw popups
                for (var i = 0; i < this.popups.length; i++) {
                    this.popups[i].updatePosition();
                }
            }    
            
            var bounds = this.getExtent();
            
            //send the move call to the baselayer and all the overlays    
            this.baseLayer.moveTo(bounds, zoomChanged, dragging);
            for (var i = 0; i < this.layers.length; i++) {
                var layer = this.layers[i];
                if (!layer.isBaseLayer) {
                    
                    var moveLayer;
                    var inRange = layer.calculateInRange();
                    if (layer.inRange != inRange) {
                        // Layer property has changed. We are going 
                        // to call moveLayer so that the layer can be turned
                        // off or on.   
                        layer.inRange = inRange;
                        moveLayer = true;
                        this.events.triggerEvent("changelayer");
                    } else {
                        // If nothing has changed, then we only move the layer
                        // if it is visible and inrange.
                        moveLayer = (layer.visibility && layer.inRange);
                    }

                    if (moveLayer) {
                        layer.moveTo(bounds, zoomChanged, dragging);
                    }
                }                
            }
            
            this.events.triggerEvent("move");
    
            if (zoomChanged) { this.events.triggerEvent("zoomend"); }
        }

        // even if nothing was done, we want to notify of this
        if (!dragging) { this.events.triggerEvent("moveend"); }
    },

    /** This function takes care to recenter the layerContainerDiv 
     * 
     * @private 
     * 
     * @param {OpenLayers.LonLat} lonlat
     */
    centerLayerContainer: function (lonlat) {

        var originPx = this.getViewPortPxFromLonLat(this.layerContainerOrigin);
        var newPx = this.getViewPortPxFromLonLat(lonlat);

        if ((originPx != null) && (newPx != null)) {
            this.layerContainerDiv.style.left = (originPx.x - newPx.x) + "px";
            this.layerContainerDiv.style.top  = (originPx.y - newPx.y) + "px";
        }
    },

    /**
     * @private 
     * 
     * @param {int} zoomLevel
     * 
     * @returns Whether or not the zoom level passed in is non-null and 
     *           within the min/max range of zoom levels.
     * @type Boolean
     */
    isValidZoomLevel: function(zoomLevel) {
       return ( (zoomLevel != null) &&
                (zoomLevel >= 0) && 
                (zoomLevel < this.getNumZoomLevels()) );
    },
    
    /**
     * @private 
     * 
     * @param {OpenLayers.LonLat} lonlat
     * 
     * @returns Whether or not the lonlat passed in is non-null and within
     *             the maxExtent bounds
     * 
     * @type Boolean
     */
    isValidLonLat: function(lonlat) {
        var valid = false;
        if (lonlat != null) {
            var maxExtent = this.getMaxExtent();
            valid = maxExtent.containsLonLat(lonlat);        
        }
        return valid;
    },

  /********************************************************/
  /*                                                      */
  /*                 Layer Options                        */
  /*                                                      */
  /*    Accessor functions to Layer Options parameters    */
  /*                                                      */
  /********************************************************/
    
    /**
     * @returns The Projection of the base layer
     * @type String
     */
    getProjection: function() {
        var projection = null;
        if (this.baseLayer != null) {
            projection = this.baseLayer.projection;
        }
        return projection;
    },
    
    /**
     * @returns The Map's Maximum Resolution
     * @type String
     */
    getMaxResolution: function() {
        var maxResolution = null;
        if (this.baseLayer != null) {
            maxResolution = this.baseLayer.maxResolution;
        }
        return maxResolution;
    },
        
    /**
    * @type OpenLayers.Bounds
    */
    getMaxExtent: function () {
        var maxExtent = null;
        if (this.baseLayer != null) {
            maxExtent = this.baseLayer.maxExtent;
        }        
        return maxExtent;
    },
    
    /**
     * @returns The total number of zoom levels that can be displayed by the 
     *           current baseLayer.
     * @type int
     */
    getNumZoomLevels: function() {
        var numZoomLevels = null;
        if (this.baseLayer != null) {
            numZoomLevels = this.baseLayer.numZoomLevels;
        }
        return numZoomLevels;
    },

  /********************************************************/
  /*                                                      */
  /*                 Baselayer Functions                  */
  /*                                                      */
  /*    The following functions, all publicly exposed     */
  /*       in the API?, are all merely wrappers to the    */
  /*       the same calls on whatever layer is set as     */
  /*                the current base layer                */
  /*                                                      */
  /********************************************************/

    /**
     * @returns A Bounds object which represents the lon/lat bounds of the 
     *          current viewPort. 
     *          If no baselayer is set, returns null.
     * @type OpenLayers.Bounds
     */
    getExtent: function () {
        var extent = null;
        if (this.baseLayer != null) {
            extent = this.baseLayer.getExtent();
        }
        return extent;
    },

    /**
     * @returns The current resolution of the map. 
     *          If no baselayer is set, returns null.
     * @type float
     */
    getResolution: function () {
        var resolution = null;
        if (this.baseLayer != null) {
            resolution = this.baseLayer.getResolution();
        }
        return resolution;
    },

     /**
      * @returns The current scale denominator of the map. 
      *          If no baselayer is set, returns null.
      * @type float
      */
    getScale: function () {
        var scale = null;
        if (this.baseLayer != null) {
            var res = this.getResolution();
            var units = this.baseLayer.units;
            scale = OpenLayers.Util.getScaleFromResolution(res, units);
        }
        return scale;
    },


    /**
     * @param {OpenLayers.Bounds} bounds
     *
     * @returns A suitable zoom level for the specified bounds.
     *          If no baselayer is set, returns null.
     * @type int
     */
    getZoomForExtent: function (bounds) {
        var zoom = null;
        if (this.baseLayer != null) {
            zoom = this.baseLayer.getZoomForExtent(bounds);
        }
        return zoom;
    },

    /**
     * @param {float} resolution
     *
     * @returns A suitable zoom level for the specified resolution.
     *          If no baselayer is set, returns null.
     * @type int
     */
    getZoomForResolution: function(resolution) {
        var zoom = null;
        if (this.baseLayer != null) {
            zoom = this.baseLayer.getZoomForResolution(resolution);
        }
        return zoom;
    },

  /********************************************************/
  /*                                                      */
  /*                  Zooming Functions                   */
  /*                                                      */
  /*    The following functions, all publicly exposed     */
  /*       in the API, are all merely wrappers to the     */
  /*               the setCenter() function               */
  /*                                                      */
  /********************************************************/
  
    /** Zoom to a specific zoom level
     * 
     * @param {int} zoom
     */
    zoomTo: function(zoom) {
        if (this.isValidZoomLevel(zoom)) {
            this.setCenter(null, zoom);
        }
    },
    
    /**
     * @param {int} zoom
     */
    zoomIn: function() {
        this.zoomTo(this.getZoom() + 1);
    },
    
    /**
     * @param {int} zoom
     */
    zoomOut: function() {
        this.zoomTo(this.getZoom() - 1);
    },

    /** Zoom to the passed in bounds, recenter
     * 
     * @param {OpenLayers.Bounds} bounds
     */
    zoomToExtent: function(bounds) {
        this.setCenter(bounds.getCenterLonLat(), 
                       this.getZoomForExtent(bounds));
    },

    /** Zoom to the full extent and recenter.
     */
    zoomToMaxExtent: function() {
        this.zoomToExtent(this.getMaxExtent());
    },

    /** zoom to a specified scale 
     * 
     * @param {float} scale
     */
    zoomToScale: function(scale) {
        var res = OpenLayers.Util.getResolutionFromScale(scale, 
                                                         this.baseLayer.units);
        var size = this.getSize();
        var w_deg = size.w * res;
        var h_deg = size.h * res;
        var center = this.getCenter();

        var extent = new OpenLayers.Bounds(center.lon - w_deg / 2,
                                           center.lat - h_deg / 2,
                                           center.lon + w_deg / 2,
                                           center.lat + h_deg / 2);
        this.zoomToExtent(extent);
    },
    
  /********************************************************/
  /*                                                      */
  /*             Translation Functions                    */
  /*                                                      */
  /*      The following functions translate between       */
  /*           LonLat, LayerPx, and ViewPortPx            */
  /*                                                      */
  /********************************************************/
      
  //
  // TRANSLATION: LonLat <-> ViewPortPx
  //

    /**
    * @param {OpenLayers.Pixel} viewPortPx
    *
    * @returns An OpenLayers.LonLat which is the passed-in view port
    *          OpenLayers.Pixel, translated into lon/lat by the 
    *          current base layer
    * @type OpenLayers.LonLat
    * @private
    */
    getLonLatFromViewPortPx: function (viewPortPx) {
        var lonlat = null; 
        if (this.baseLayer != null) {
            lonlat = this.baseLayer.getLonLatFromViewPortPx(viewPortPx);
        }
        return lonlat;
    },

    /**
    * @param {OpenLayers.LonLat} lonlat
    *
    * @returns An OpenLayers.Pixel which is the passed-in OpenLayers.LonLat, 
    *          translated into view port pixels by the 
    *          current base layer
    * @type OpenLayers.Pixel
    * @private
    */
    getViewPortPxFromLonLat: function (lonlat) {
        var px = null; 
        if (this.baseLayer != null) {
            px = this.baseLayer.getViewPortPxFromLonLat(lonlat);
        }
        return px;
    },

    
  //
  // CONVENIENCE TRANSLATION FUNCTIONS FOR API
  //

    /**
     * @param {OpenLayers.Pixel} pixel
     *
     * @returns An OpenLayers.LonLat corresponding to the given
     *          OpenLayers.Pixel, translated into lon/lat by the 
     *          current base layer
     * @type OpenLayers.LonLat
     */
    getLonLatFromPixel: function (px) {
        return this.getLonLatFromViewPortPx(px);
    },

    /**
     * @param {OpenLayers.LonLat} lonlat
     *
     * @returns An OpenLayers.Pixel corresponding to the OpenLayers.LonLat
     *          translated into view port pixels by the 
     *          current base layer
     * @type OpenLayers.Pixel
     */
    getPixelFromLonLat: function (lonlat) {
        return this.getViewPortPxFromLonLat(lonlat);
    },



  //
  // TRANSLATION: ViewPortPx <-> LayerPx
  //

    /**
     * @private
     * 
     * @param {OpenLayers.Pixel} layerPx
     * 
     * @returns Layer Pixel translated into ViewPort Pixel coordinates
     * @type OpenLayers.Pixel
     */
    getViewPortPxFromLayerPx:function(layerPx) {
        var viewPortPx = null;
        if (layerPx != null) {
            var dX = parseInt(this.layerContainerDiv.style.left);
            var dY = parseInt(this.layerContainerDiv.style.top);
            viewPortPx = layerPx.add(dX, dY);            
        }
        return viewPortPx;
    },
    
    /**
     * @private
     * 
     * @param {OpenLayers.Pixel} viewPortPx
     * 
     * @returns ViewPort Pixel translated into Layer Pixel coordinates
     * @type OpenLayers.Pixel
     */
    getLayerPxFromViewPortPx:function(viewPortPx) {
        var layerPx = null;
        if (viewPortPx != null) {
            var dX = -parseInt(this.layerContainerDiv.style.left);
            var dY = -parseInt(this.layerContainerDiv.style.top);
            layerPx = viewPortPx.add(dX, dY);
            if (isNaN(layerPx.x) || isNaN(layerPx.y)) {
                layerPx = null;
            }
        }
        return layerPx;
    },
    
  //
  // TRANSLATION: LonLat <-> LayerPx
  //

    /**
    * @param {OpenLayers.Pixel} px
    *
    * @type OpenLayers.LonLat
    */
    getLonLatFromLayerPx: function (px) {
       //adjust for displacement of layerContainerDiv
       px = this.getViewPortPxFromLayerPx(px);
       return this.getLonLatFromViewPortPx(px);         
    },
    
    /**
    * @param {OpenLayers.LonLat} lonlat
    *
    * @returns An OpenLayers.Pixel which is the passed-in OpenLayers.LonLat, 
    *          translated into layer pixels by the current base layer
    * @type OpenLayers.Pixel
    */
    getLayerPxFromLonLat: function (lonlat) {
       //adjust for displacement of layerContainerDiv
       var px = this.getViewPortPxFromLonLat(lonlat);
       return this.getLayerPxFromViewPortPx(px);         
    },


    CLASS_NAME: "OpenLayers.Map"
};
