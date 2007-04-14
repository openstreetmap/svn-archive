var PI = 3.14159265358979323846;

function tiles_to_refresh(lat,lon) {
	this.minzoom = 0;
	this.maxzoom = 20;
	for (var i = this.minzoom; i <= this.maxzoom; i++) {
		document.write("<img src=\"http://tile.openstreetmap.org/cgi-bin/steve/mapserv?map=/usr/lib/cgi-bin/steve/wms.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE&LAYERS=landsat,streets"+tile_to_refresh(lat,lon,i)+"\">");
	}
}

function tile_to_refresh(lon,lat,z) {

	this.scale = 1000000;
	this.lon_min_clamp = -180 * this.scale;
	this.lon_max_clamp = 180 * this.scale;
	this.lat_min_clamp = -180 * this.scale; //t
	this.lat_max_clamp = 180 * this.scale; //t
	this.lon_start_tile = 180 * this.scale;
	this.lat_start_tile = 90 * this.scale; //t
	this.zoom_power = 2;
	this.lon_quant = this.lon_start_tile;
	this.lat_quant = this.lat_start_tile;
	this.lon = lon;
	this.lat = lat;

	// operational lat - = lat due to quirks in our engine and quirks in lon/lat design
	lat = -lat;

	// divide tile size until reach requested zoom
	// trying to guarantee consistency so as to not thrash the server side tile cache
	while(z > 0) {
		this.lon_quant = this.lon_quant / this.zoom_power;
		this.lat_quant = this.lat_quant / this.zoom_power;
		z--;
	}
	this.lon_quant = Math.round( this.lon_quant );
	this.lat_quant = Math.round( this.lat_quant );
	
	// get user requested exact lon/lat
	this.lon_scaled = Math.round( lon * this.scale );
	this.lat_scaled = Math.round( lat * this.scale );
	
	// convert requested exact lon/lat to quantized lon lat (rounding down or up as best suits)
	this.lon_round = Math.round( this.lon_scaled / this.lon_quant ) * this.lon_quant;
	this.lat_round = Math.round( this.lat_scaled / this.lat_quant ) * this.lat_quant;
	
	// calculate world extents [ this is the span of all tiles in lon/lat ]
	this.lon_min = this.lon_round - this.lon_quant;
	this.lat_min = this.lat_round - this.lat_quant;
	
	// set tiled region details [ this is the span of all tiles in pixels ]
	this.tilewidth = 256;
	this.tileheight = 128;
	
	///
	/// draw the spanning lon/lat range
	/// drag is simply the mouse delta in pixels
	///

	var x = this.lon_min;
	var y = this.lat_min;

	// convert to WMS compliant coordinate system
	var lt = x / this.scale;
	var rt = lt + this.lon_quant / this.scale;
	var tp = y / this.scale;
	var bt = tp + this.lat_quant / this.scale;
	var temp = bt;
	var bt = -tp;
	var tp = -temp;

	// modify for mercator-projected tiles: 
	tp = 180 / PI * (2 * Math.atan(Math.exp(tp * PI / 180)) - PI / 2);
	bt = 180 / PI * (2 * Math.atan(Math.exp(bt * PI / 180)) - PI / 2);
				
	// make a key
	var key = "&WIDTH="+(this.tilewidth)+"&HEIGHT="+(this.tileheight)+"&BBOX="+lt+","+tp+","+rt+","+bt;

	return key;

}

