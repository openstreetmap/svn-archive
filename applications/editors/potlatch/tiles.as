
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


	function showTileDebug() {
//		_root.chat._visible=true;
		lat=centrelat(0);
		lon=centrelong(0);
		z=_root.scale;
		xtile=Math.floor((lon+180)/360*Math.pow(2,z));
		ytile=Math.floor((1-Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2 *Math.pow(2,z));

		_root.chat.text="http://tile.openstreetmap.org/"+z+"/"+xtile+"/"+ytile+".png";
// basey is y co-ordinate of map centre
// lat2y(baselat)

//		_root.chat.text+="\nx: "+(baselong+_root.x/_root.masterscale);
//		_root.chat.text+="\ny: "+basey   +","+(-_root.y);
		_root.chat.text+="\ny via Landsat projection: "+lat2y(centrelat(0));
//		_root.chat.text+="y via spherical Mercator: "+((Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2)*360;
//		_root.chat.text+="y via OpenLayers code: "+Math.log(Math.tan((90+lat)*Math.PI/360))/(Math.PI/180);
	}



	// tile filenames

	function getTileNumber(lat,lon,z) {
		xtile=Math.floor((lon+180)/360*Math.pow(2,z)) ;
		ytile=Math.floor((1-Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2 *Math.pow(2,z));
	}
