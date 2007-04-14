/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */
// @require: OpenLayers/Layer/Grid.js
/**
* @class
*/
OpenLayers.Layer.WMS.Untiled = Class.create();
OpenLayers.Layer.WMS.Untiled.prototype = 
  Object.extend( new OpenLayers.Layer.Grid(), {

    /** @final @type hash */
    DEFAULT_PARAMS: { service: "WMS",
                      version: "1.1.1",
                      request: "GetMap",
                      styles: "",
                      exceptions: "application/vnd.ogc.se_inimage",
                      format: "image/jpeg"
                     },

    /**
    * @constructor
    *
    * @param {str} name
    * @param {str} url
    * @param {hash} params
    */
    initialize: function(name, url, params) {
        var newArguments = new Array();
        if (arguments.length > 0) {
            //uppercase params
            params = OpenLayers.Util.upperCaseObject(params);
            newArguments.push(name, url, params);
        }
        OpenLayers.Layer.Grid.prototype.initialize.apply(this, newArguments);
        
        if (arguments.length > 0) {
            OpenLayers.Util.applyDefaults(
                           this.params, 
                           OpenLayers.Util.upperCaseObject(this.DEFAULT_PARAMS)
                           );
        }
    },    

    
    /** WFS layer is never a base class. 
     * @type Boolean
     */
    isBaseLayer: function() {
        return (this.params.TRANSPARENT != true);
    },
    
    /**
    * @param {String} name
    * @param {hash} params
    *
    * @returns A clone of this OpenLayers.Layer.WMS, with the passed-in
    *          parameters merged in.
    * @type OpenLayers.Layer.WMS
    */
    clone: function (name, params) {
        var mergedParams = {};
        Object.extend(mergedParams, this.params);
        Object.extend(mergedParams, params);
        var obj = new OpenLayers.Layer.WMS(name, this.url, mergedParams);
        obj.setTileSize(this.tileSize);
        return obj;
    },

    /**
    * addTile creates a tile, initializes it (via 'draw' in this case), and 
    * adds it to the layer div. 
    *
    * @param {OpenLayers.Bounds} bounds
    *
    * @returns The added OpenLayers.Tile.Image
    * @type OpenLayers.Tile.Image
    */
    addTile:function(bounds,position) {
        url = this.getFullRequestString(
                     {BBOX:bounds.toBBOX(),
                      WIDTH:this.map.getSize().w,
                      HEIGHT:this.map.getSize().h});
        
        return new OpenLayers.Tile.Image(this, position, bounds, 
                                             url, this.map.getSize());
    },
    moveTo:function(bounds,zoomChanged, minor) {
        if (!minor) {
            this.div.innerHTML = "";
            tile = this.addTile(bounds, new OpenLayers.Pixel(-parseInt(this.map.layerContainerDiv.style.left), -parseInt(this.map.layerContainerDiv.style.top)));
            tile.draw();
        }
    },
    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.WMS.Untiled"
});
