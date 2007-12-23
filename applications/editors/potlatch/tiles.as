
	// =====================================================================
	// Tile management functions

	// initTiles - empty all tile layers and queues

	function initTiles() {
		_root.map.createEmptyMovieClip("tiles",7);
		for (var i=minscale; i<=maxscale; i+=1) {
			_root.map.tiles.createEmptyMovieClip(i,i);
		}
		_root.age=0;						// current age
		_root.ages=new Array();				// hash of ages
		_root.clipsloaded=0;				// number of clips loaded
		_root.clipsshown=new Array();		// what clips are currently on-screen?
		_root.tilerequested=new Array();	// what clips have been requested?
		_root.tilerequests=new Array();		// list of tile requests
	}

	function loadTile(thisurl,thistilename,thisx,thisy,mapscale) {
	}

	function serviceTileQueue() {
	}
	
	function blankTileQueue() {
	}

	function purgeTiles() {
	}

