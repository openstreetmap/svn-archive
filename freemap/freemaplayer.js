// This is largely taken from the OpenLayers WFS layer and hence is under the
// same BSD licence.

// @require: OpenLayers/Layer/Grid.js
     // @require: OpenLayers/Layer/Markers.js
     /**
     * @class
     */
     OpenLayers.Layer.FreemapAnnotations = Class.create();
     OpenLayers.Layer.FreemapAnnotations.prototype =
       Object.extend(new OpenLayers.Layer.Grid(),
         Object.extend(new OpenLayers.Layer.Markers(), {
    
        /** @type Object */
    
        /** @final @type hash */
        DEFAULT_PARAMS: { },
		
		mode: 0,

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
            OpenLayers.Layer.Grid.prototype.initialize.apply(this,newArguments);
            OpenLayers.Layer.Markers.prototype.initialize.apply
                (this, newArguments);
       
            if (arguments.length > 0) {
                OpenLayers.Util.applyDefaults(
                               this.params,
                               OpenLayers.Util.upperCaseObject
                                       (this.DEFAULT_PARAMS)
                               );
            }
        },   
      
	  	setMap: function(map) {
			OpenLayers.Layer.prototype.setMap.apply(this,arguments);
			if(this.tileSize==null) 
			{
				this.tileSize = this.map.getTileSize();
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
            var obj = new OpenLayers.Layer.FreemapAnnotations
				(name,this.url,mergedParams);
            obj.setTileSize(this.tileSize);
            return obj;
        },
    
        /**
        * addTile creates a tile, initializes it (via 'draw' in this case), and
        * adds it to the layer div.
        *
        * @param {OpenLayers.Bounds} bounds
        *
        * @returns The added OpenLayers.Tile.FreemapAnnotations
        * @type OpenLayers.Tile.FreemapAnnotations
        */
        addTile:function(bounds, position) {
            url = this.getFullRequestString(
                         { BBOX:bounds.toBBOX() });
    
            return new OpenLayers.Tile.FreemapAnnotations
			(this, position, bounds, url, this.tileSize);
        },
     
     
         /** @final @type String */
         CLASS_NAME: "OpenLayers.Layer.FreemapAnnotations"
     }
     )
     );
