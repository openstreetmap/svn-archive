/* Copyright (c) 2006 MetaCarta, Inc., published under a modified BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/repository-license.txt 
 * for the full text of the license. */


/**
 * @class
 * 
 * @requires OpenLayers/Layer/HTTPRequest.js
 */
OpenLayers.Layer.Grid = OpenLayers.Class.create();
OpenLayers.Layer.Grid.prototype = 
  OpenLayers.Class.inherit( OpenLayers.Layer.HTTPRequest, {
    
    /** @type OpenLayers.Size */
    tileSize: null,
    
    /** this is an array of rows, each row is an array of tiles
     * 
     * @type Array(Array) */
    grid: null,

    /** @type Integer */
    buffer: 2,

    /**
     * @constructor
     * 
     * @param {String} name
     * @param {String} url
     * @param {Object} params
     * @param {Object} options Hashtable of extra options to tag onto the layer
    */
    initialize: function(name, url, params, options) {
        OpenLayers.Layer.HTTPRequest.prototype.initialize.apply(this, 
                                                                arguments);
        this.grid = new Array();
    },

    /** on destroy, clear the grid.
     *
     */
    destroy: function() {
        this.clearGrid();
        this.grid = null;
        this.tileSize = null;
        OpenLayers.Layer.HTTPRequest.prototype.destroy.apply(this, arguments); 
    },

    /**
     * @param {Object} obj
     * 
     * @returns An exact clone of this OpenLayers.Layer.Grid
     * @type OpenLayers.Layer.Grid
     */
    clone: function (obj) {
        
        if (obj == null) {
            obj = new OpenLayers.Layer.Grid(this.name,
                                            this.url,
                                            this.params,
                                            this.options);
        }

        //get all additions from superclasses
        obj = OpenLayers.Layer.HTTPRequest.prototype.clone.apply(this, [obj]);

        // copy/set any non-init, non-simple values here
        if (this.tileSize != null) {
            obj.tileSize = this.tileSize.clone();
        }
        
        // we do not want to copy reference to grid, so we make a new array
        obj.grid = new Array();

        return obj;
    },    

    /** When the layer is added to a map, then we can ask the map for
     *   its default tile size
     * 
     * @param {OpenLayers.Map} map
     */
    setMap: function(map) {
        OpenLayers.Layer.HTTPRequest.prototype.setMap.apply(this, arguments);
        if (this.tileSize == null) {
            this.tileSize = this.map.getTileSize();
        }
    },

    /** This function is called whenever the map is moved. All the moving
     * of actual 'tiles' is done by the map, but moveTo's role is to accept
     * a bounds and make sure the data that that bounds requires is pre-loaded.
     * 
     * @param {OpenLayers.Bounds} bounds
     * @param {Boolean} zoomChanged
     * @param {Boolean} dragging
     */
    moveTo:function(bounds, zoomChanged, dragging) {
        OpenLayers.Layer.HTTPRequest.prototype.moveTo.apply(this, arguments);
        
        if (bounds == null) {
            bounds = this.map.getExtent();
        }
        if (bounds != null) {
            if (!this.grid.length || zoomChanged 
                || !this.getGridBounds().containsBounds(bounds, true)) { 
                this._initTiles();
            } else {
                var buffer = (this.buffer) ? this.buffer*1.5 : 1;
                while (true) {
                    var tlLayer = this.grid[0][0].position;
                    var tlViewPort = 
                        this.map.getViewPortPxFromLayerPx(tlLayer);
                    if (tlViewPort.x > -this.tileSize.w * (buffer - 1)) {
                        this.shiftColumn(true);
                    } else if (tlViewPort.x < -this.tileSize.w * buffer) {
                        this.shiftColumn(false);
                    } else if (tlViewPort.y > -this.tileSize.h * (buffer - 1)) {
                        this.shiftRow(true);
                    } else if (tlViewPort.y < -this.tileSize.h * buffer) {
                        this.shiftRow(false);
                    } else {
                        break;
                    }
                };
                if (this.buffer == 0) {
                    for (var r=0, rl=this.grid.length; r<rl; r++) {
                        var row = this.grid[r];
                        for (var c=0, cl=row.length; c<cl; c++) {
                            var tile = row[c];
                            if (!tile.drawn && tile.bounds.intersectsBounds(bounds, false)) {
                                tile.draw();
                            }
                        }
                    }
                }
            }
        }
    },
    
    /**
     * @private
     * 
     * @returns A Bounds object representing the bounds of all the currently 
     *           loaded tiles (including those partially or not at all seen 
     *           onscreen)
     * @type OpenLayers.Bounds
     */
    getGridBounds:function() {
        
        var bottom = this.grid.length - 1;
        var bottomLeftTile = this.grid[bottom][0];

        var right = this.grid[0].length - 1; 
        var topRightTile = this.grid[0][right];

        return new OpenLayers.Bounds(bottomLeftTile.bounds.left, 
                                     bottomLeftTile.bounds.bottom,
                                     topRightTile.bounds.right, 
                                     topRightTile.bounds.top);
    },

    /**
     * @private
     */
    _initTiles:function() {
        
        // work out mininum number of rows and columns; this is the number of
        // tiles required to cover the viewport plus one for panning
        var viewSize = this.map.getSize();
        var minRows = Math.ceil(viewSize.h/this.tileSize.h) + 1;
        var minCols = Math.ceil(viewSize.w/this.tileSize.w) + 1;
        
        var bounds = this.map.getExtent();
        var extent = this.map.getMaxExtent();
        var resolution = this.map.getResolution();
        var tilelon = resolution * this.tileSize.w;
        var tilelat = resolution * this.tileSize.h;
        
        var offsetlon = bounds.left - extent.left;
        var tilecol = Math.floor(offsetlon/tilelon) - this.buffer;
        var tilecolremain = offsetlon/tilelon - tilecol;
        var tileoffsetx = -tilecolremain * this.tileSize.w;
        var tileoffsetlon = extent.left + tilecol * tilelon;
        
        var offsetlat = bounds.top - (extent.bottom + tilelat);  
        var tilerow = Math.ceil(offsetlat/tilelat) + this.buffer;
        var tilerowremain = tilerow - offsetlat/tilelat;
        var tileoffsety = -tilerowremain * this.tileSize.h;
        var tileoffsetlat = extent.bottom + tilerow * tilelat;
        
        tileoffsetx = Math.round(tileoffsetx); // heaven help us
        tileoffsety = Math.round(tileoffsety);

        this.origin = new OpenLayers.Pixel(tileoffsetx, tileoffsety);

        var startX = tileoffsetx; 
        var startLon = tileoffsetlon;

        var rowidx = 0;
    
        do {
            var row = this.grid[rowidx++];
            if (!row) {
                row = new Array();
                this.grid.push(row);
            }

            tileoffsetlon = startLon;
            tileoffsetx = startX;
            var colidx = 0;
 
            do {
                var tileBounds = new OpenLayers.Bounds(tileoffsetlon, 
                                                      tileoffsetlat, 
                                                      tileoffsetlon + tilelon,
                                                      tileoffsetlat + tilelat);

                var x = tileoffsetx;
                x -= parseInt(this.map.layerContainerDiv.style.left);

                var y = tileoffsety;
                y -= parseInt(this.map.layerContainerDiv.style.top);

                var px = new OpenLayers.Pixel(x, y);
                var tile = row[colidx++];
                if (!tile) {
                    tile = this.addTile(tileBounds, px);
                    row.push(tile);
                } else {
                    tile.moveTo(tileBounds, px, false);
                }
     
                tileoffsetlon += tilelon;       
                tileoffsetx += this.tileSize.w;
            } while ((tileoffsetlon <= bounds.right + tilelon * this.buffer)
                     || colidx < minCols)  
             
            tileoffsetlat -= tilelat;
            tileoffsety += this.tileSize.h;
        } while((tileoffsetlat >= bounds.bottom - tilelat * this.buffer)
                || rowidx < minRows)
        
        // remove extra rows
        while (this.grid.length > rowidx) {
            var row = this.grid.pop();
            for (var i=0, l=row.length; i<l; i++) {
                row[i].destroy();
            }
        }
        
        // remove extra columns
        while (this.grid[0].length > colidx) {
            for (var i=0, l=this.grid.length; i<l; i++) {
                var row = this.grid[i];
                var tile = row.pop();
                tile.destroy();
            }
        }
        
        //now actually draw the tiles
        this.spiralTileLoad();
    },
    
    /** 
     * @private 
     * 
     *   Starts at the top right corner of the grid and proceeds in a spiral 
     *    towards the center, adding tiles one at a time to the beginning of a 
     *    queue. 
     * 
     *   Once all the grid's tiles have been added to the queue, we go back 
     *    and iterate through the queue (thus reversing the spiral order from 
     *    outside-in to inside-out), calling draw() on each tile. 
     */
    spiralTileLoad: function() {
        var tileQueue = new Array();
 
        var directions = ["right", "down", "left", "up"];

        var iRow = 0;
        var iCell = -1;
        var direction = OpenLayers.Util.indexOf(directions, "right");
        var directionsTried = 0;
        
        while( directionsTried < directions.length) {

            var testRow = iRow;
            var testCell = iCell;

            switch (directions[direction]) {
                case "right":
                    testCell++;
                    break;
                case "down":
                    testRow++;
                    break;
                case "left":
                    testCell--;
                    break;
                case "up":
                    testRow--;
                    break;
            } 
    
            // if the test grid coordinates are within the bounds of the 
            //  grid, get a reference to the tile.
            var tile = null;
            if ((testRow < this.grid.length) && (testRow >= 0) &&
                (testCell < this.grid[0].length) && (testCell >= 0)) {
                tile = this.grid[testRow][testCell];
            }
            
            if ((tile != null) && (!tile.queued)) {
                //add tile to beginning of queue, mark it as queued.
                tileQueue.unshift(tile);
                tile.queued = true;
                
                //restart the directions counter and take on the new coords
                directionsTried = 0;
                iRow = testRow;
                iCell = testCell;
            } else {
                //need to try to load a tile in a different direction
                direction = (direction + 1) % 4;
                directionsTried++;
            }
        } 
        
        // now we go through and draw the tiles in forward order
        for(var i=0; i < tileQueue.length; i++) {
            var tile = tileQueue[i]
            tile.draw();
            //mark tile as unqueued for the next time (since tiles are reused)
            tile.queued = false;       
        }
    },

    /**
     * addTile gives subclasses of Grid the opportunity to create an 
     * OpenLayer.Tile of their choosing. The implementer should initialize 
     * the new tile and take whatever steps necessary to display it.
     *
     * @param {OpenLayers.Bounds} bounds
     *
     * @returns The added OpenLayers.Tile
     * @type OpenLayers.Tile
     */
    addTile:function(bounds, position) {
        // Should be implemented by subclasses
    },

    
    /** go through and remove all tiles from the grid, calling
     *    destroy() on each of them to kill circular references
     * 
     * @private
     */
    clearGrid:function() {
        if (this.grid) {
            for(var iRow=0; iRow < this.grid.length; iRow++) {
                var row = this.grid[iRow];
                for(var iCol=0; iCol < row.length; iCol++) {
                    row[iCol].destroy();
                }
            this.grid = [];
            }
        }
    },

    /**
     * @private 
     * 
     * @param {Boolean} prepend if true, prepend to beginning.
     *                          if false, then append to end
     */
    shiftRow:function(prepend) {
        var modelRowIndex = (prepend) ? 0 : (this.grid.length - 1);
        var modelRow = this.grid[modelRowIndex];

        var resolution = this.map.getResolution();
        var deltaY = (prepend) ? -this.tileSize.h : this.tileSize.h;
        var deltaLat = resolution * -deltaY;

        var row = (prepend) ? this.grid.pop() : this.grid.shift();

        for (var i=0; i < modelRow.length; i++) {
            var modelTile = modelRow[i];
            var bounds = modelTile.bounds.clone();
            var position = modelTile.position.clone();
            bounds.bottom = bounds.bottom + deltaLat;
            bounds.top = bounds.top + deltaLat;
            position.y = position.y + deltaY;
            row[i].moveTo(bounds, position);
        }

        if (prepend) {
            this.grid.unshift(row);
        } else {
            this.grid.push(row);
        }
    },

    /**
     * @private
     * 
     * @param {Boolean} prepend if true, prepend to beginning.
     *                          if false, then append to end
     */
    shiftColumn: function(prepend) {
        var deltaX = (prepend) ? -this.tileSize.w : this.tileSize.w;
        var resolution = this.map.getResolution();
        var deltaLon = resolution * deltaX;

        for (var i=0; i<this.grid.length; i++) {
            var row = this.grid[i];
            var modelTileIndex = (prepend) ? 0 : (row.length - 1);
            var modelTile = row[modelTileIndex];
            
            var bounds = modelTile.bounds.clone();
            var position = modelTile.position.clone();
            bounds.left = bounds.left + deltaLon;
            bounds.right = bounds.right + deltaLon;
            position.x = position.x + deltaX;

            var tile = prepend ? this.grid[i].pop() : this.grid[i].shift()
            tile.moveTo(bounds, position);
            if (prepend) {
                this.grid[i].unshift(tile);
            } else {
                this.grid[i].push(tile);
            }
        }
    },
    
    /** @final @type String */
    CLASS_NAME: "OpenLayers.Layer.Grid"
});
