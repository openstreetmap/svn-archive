/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */
// @require: OpenLayers/Layer/Grid.js
// @require: OpenLayers/Layer/Markers.js
/**
* @class
*/
OpenLayers.Layer.WFS = Class.create();
OpenLayers.Layer.WFS.prototype = 
  Object.extend(new OpenLayers.Layer.Grid(),
    Object.extend(new OpenLayers.Layer.Markers(), {

    /** @type Object */
    featureClass: OpenLayers.Feature.WFS,

    /** @final @type hash */
    DEFAULT_PARAMS: { service: "WFS",
                      version: "1.0.0",
                      request: "GetFeature",
                      typename: "docpoint"
                    },

    /**
    * @constructor
    *
    * @param {str} name
    * @param {str} url
    * @param {hash} params
    * @param {Object} featureClass
    */
    initialize: function(name, url, params, featureClass) {
        if (featureClass != null) this.featureClass = featureClass;
        
        var newArguments = new Array();
        if (arguments.length > 0) {
            //uppercase params
            params = OpenLayers.Util.upperCaseObject(params);
            newArguments.push(name, url, params);
        }
        OpenLayers.Layer.Grid.prototype.initialize.apply(this, newArguments);
        OpenLayers.Layer.Markers.prototype.initialize.apply(this, newArguments);
    
        if (arguments.length > 0) {
            OpenLayers.Util.applyDefaults(
                           this.params, 
                           OpenLayers.Util.upperCaseObject(this.DEFAULT_PARAMS)
                           );
        }
    },    
    

    /**
     * 
     */
    destroy: function() {
        OpenLayers.Layer.Grid.prototype.destroy.apply(this, arguments);
        OpenLayers.Layer.Markers.prototype.destroy.apply(this, arguments);
    },
    
    /** 
    * @param {OpenLayers.Bounds} bounds
    * @param {Boolean} zoomChanged
    */
    moveTo: function(bounds, zoomChanged) {
        OpenLayers.Layer.Grid.prototype.moveTo.apply(this, arguments);
        OpenLayers.Layer.Markers.prototype.moveTo.apply(this, arguments);
    },
    
    /** WFS layer is never a base class. 
     * @type Boolean
     */
    isBaseLayer: function() {
        return false;
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
        var mergedParams = {}
        Object.extend(mergedParams, this.params);
        Object.extend(mergedParams, params);
        var obj = new OpenLayers.Layer.WFS(name, this.url, mergedParams);
        obj.setTileSize(this.tileSize);
        return obj;
    },

    /**
    * addTile creates a tile, initializes it (via 'draw' in this case), and 
    * adds it to the layer div. 
    *
    * @param {OpenLayers.Bounds} bounds
    *
    * @returns The added OpenLayers.Tile.WFS
    * @type OpenLayers.Tile.WFS
    */
    addTile:function(bounds, position) {
        url = this.getFullRequestString(
                     { BBOX:bounds.toBBOX() });

        return new OpenLayers.Tile.WFS(this, position, bounds, 
                                           url, this.tileSize);
    },


    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.WFS"
}
)
);
