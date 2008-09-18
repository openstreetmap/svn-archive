
	// =====================================================================
	// Tile management functions

	// tileListener - MovieClipLoader class
	//				  on complete or error, remove from queue
	
	tileListener.onLoadError=function(tile,errorCode) {
		_root.tilesloaded-=1;
		delete tilerequested[tile._parent._name+','+tile._name];
		delete    _root.ages[tile._parent._name+','+tile._name];
		removeFromTileRequests(tile);
		tile.removeMovieClip();
	};
	tileListener.onLoadComplete=function(tile) {
		delete tilerequested[tile._parent._name+','+tile._name];
		removeFromTileRequests(tile);
	};
	
	function removeFromTileRequests(tile) {
		var sc=tile._parent._name;
		var x =tile._name.split(',')[0];
		var y =tile._name.split(',')[1];
		for (var i=0; i<tilerequests.length; i++) {
			if (tilerequests[i][0]==x &&
				tilerequests[i][1]==y &&
				tilerequests[i][2]==sc) {
				tilerequests.splice(i,1); i--;
			}
		}
	}
		


	// initTiles - empty all tile layers and queues

	function initTiles() {
		_root.map.createEmptyMovieClip("tiles",1);
		for (var i=minscale; i<=maxscale; i+=1) {
			_root.map.tiles.createEmptyMovieClip(i,i);
		}
		_root.age=0;						// current age
		_root.ages=new Array();				// hash of ages
		_root.tilesloaded=0;				// number of clips loaded
		_root.tilerequested=new Array();	// what clips have been requested?
		_root.tilerequests=new Array();		// list of tile requests
		_root.tiledepth=0;					// depth of tile movieclip
	}

	// updateTiles - load all tiles and select correct zoom level
	
	function updateTiles() {
		for (var i=minscale; i<=maxscale; i+=1) {
			_root.map.tiles[i]._visible=(_root.scale==i);
		}
		for (var x=_root.tile_l; x<=_root.tile_r; x+=1) {
			for (var y=_root.tile_t; y<=_root.tile_b; y+=1) {
				if (!_root.tilerequested[_root.scale+','+x+','+y] &&
					!_root.map.tiles[_root.scale][x+','+y]) { requestTile(x,y); }
			}
		}
		if (_root.tilesloaded>200) { purgeTiles(); }
	}

	// loadTile - add tile to end of request queue

	function requestTile(tx,ty) {
		var a=new Array(tx,ty,_root.scale);
		tilerequests.push(a);
		tilerequested[_root.scale+','+tx+','+ty]=true;
	}

	// serviceTileQueue - fire load requests for four most recent requested
	//					- called onEnterFrame
	//					- splice will cause one of the four to be skipped, but
	//					  this will be remedied on the next run through

	function serviceTileQueue() {
		for (var n=0; n<Math.min(4,tilerequests.length); n++) {
			var r=tilerequests[n]; var x=r[0]; var y=r[1]; var sc=r[2];
			if (_root.map.tiles[sc][x+','+y]) {
				// tile exists
			} else if (x>=tile_l && x<=tile_r && y>=tile_t && y<=tile_b) {
				// tile doesn't exist but is on-screen, so load
				_root.map.tiles[sc].createEmptyMovieClip(x+","+y,++_root.tiledepth);
				_root.tileLoader.loadClip(tileURL(x,y),_root.map.tiles[sc][x+","+y]);
				with (_root.map.tiles[sc][x+','+y]) {
					_x=long2coord(tile2long(x));
					_y=lat2coord(tile2lat(y));
					// ** check that this shouldn't be y-1, etc.
					_xscale=_yscale=100/Math.pow(2,sc-13);
				}
				_root.tilesloaded++;
				_root.ages[sc+','+x+','+y]=_root.age;
				tilerequests[n][3]=getTimer();
			} else {
				// tile doesn't exist and is now off-screen, so delete
				delete tilerequested[sc+','+x+','+y];
				tilerequests.splice(n,1);
			}
		}
	}
	
	function purgeTiles() {
		for (var i=minscale; i<=maxscale; i+=1) {
			for (j in _root.map.tiles[i]) {
				var x=j.substr(0,j.indexOf(','));
				var y=j.substr(j.indexOf(',')+1);
				if (x<tile_l || x>tile_r || y<tile_t || y>tile_b) {
					delete tilerequested[i+','+j];
					removeMovieClip(_root.map.tiles[i][j]);
					_root.tilesloaded--;
				}
			}
		}
	}


	// tile co-ordinates

	function long2tile(lon) { return (Math.floor((lon+180)/360*Math.pow(2,_root.scale))); }
	function lat2tile(lat)  { return (Math.floor((1-Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2 *Math.pow(2,_root.scale))); }
	function tile2long(t)	{ return (t/Math.pow(2,_root.scale)*360-180); }
	function tile2lat(t)	{ var n=Math.PI-2*Math.PI*t/Math.pow(2,_root.scale);
							  return (180/Math.PI*Math.atan(0.5*(Math.exp(n)-Math.exp(-n)))); }

	// tile URLs
	
	function tileURL(x,y) { 
		switch (preferences.data.baselayer) {
			case 1:	return (tileprefix+"http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/"+_root.scale+"/"+x+"/"+y+".jpg");
			case 3: return (tileprefix+"http://tile.openstreetmap.org/"+_root.scale+"/"+x+"/"+y+".png");
			case 4: return (tileprefix+"http://tah.openstreetmap.org/Tiles/tile/"+_root.scale+"/"+x+"/"+y+".png");
			case 5: return (tileprefix+"http://tah.openstreetmap.org/Tiles/maplint/"+_root.scale+"/"+x+"/"+y+".png");
			case 6: return (tileprefix+"http://andy.sandbox.cloudmade.com/tiles/cycle/"+_root.scale+"/"+x+"/"+y+".png");
			case 7: return (tileprefix+"http://richard.dev.openstreetmap.org/npe/"+_root.scale+"/"+x+"/"+y+".jpg");
			case 8:	return (tileprefix+"http://tile.openaerialmap.org/tiles/1.0.0/opentopomap-900913/"+_root.scale+"/"+x+"/"+y+".jpg");
//			case 7: return (tileprefix+"http://127.0.0.1/~richard/npe/"+_root.scale+"/"+x+"/"+y+".jpg");
		}
	}
