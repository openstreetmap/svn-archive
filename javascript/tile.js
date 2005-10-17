// June 20th 2005 http://civicactions.net anselm@hook.org public domain version 0.5

//
// miscellaneous
//

var netscape = ( document.getElementById && !document.all ) || document.layers;
var moz = (typeof document.implementation != 'undefined') && (typeof document.implementation.createDocument != 'undefined');
var ie = (typeof window.ActiveXObject != 'undefined');
var urlpath = "/";
var urlbase = "maps.civicactions.net";
var visiting = "home";
var parameters_array = null;
var xmlDoc;
var xmlHttp;
var defaultEngine = null; // xxx for firefox keyboard events.

//
// Utility - get div position - may not be accurate
//

function getCSSPositionX(parent) {
	var offset = parent.x ? parseInt(parent.x) : 0;
	offset += parent.style.left ? parseInt(parent.style.left) : 0;
	for(var node = parent; node ; node = node.offsetParent ) { offset += node.offsetLeft; }
	return offset;
}

function getCSSPositionY(parent) {
	var offset = parent.y ? parseInt(parent.y) : 0;
	offset += parent.style.top ? parseInt(parent.style.top) : 0;
	for(var node = parent; node ; node = node.offsetParent ) { offset += node.offsetTop; }
	return offset;
}

///
/// PICK APART URL
///
function Get_URLParts() {

	// convert to string
	var a = "" + location.href;
	// remove the http part
	if( a.indexOf("http://") == 0 ) {
		a = a.substring(7,a.length);
	}

	// remove the parameters and  get them into an index
	var i = a.indexOf('?');
	if( i >= 0 ) {
		b = a.substring(0,i);
		parameters_array = a.substring(i+1,a.length).split('&');
	} else {
		b = a;
	}

	// strip off the domain and hold onto the portion *after* the domain
	urlpath = b;

	i = urlpath.indexOf('/');
	if( i >= 0 ) {
		urlpath = b.substring( i, b.length );
	} else {
		urlpath = b;
	}

	// get the domain and the first path (the user being visited)
	var c = b.split("/");
	if( c.length > 0 ) {
		urlbase = c[0];
	}
	if( c.length > 1 ) {
		visiting = c[1];
	}
}

function Get_Parameter(name) {
	var i = 0;
	if( parameters_array != null ) {
		while( i < parameters_array.length ) {
			var sp2 = parameters_array[i].indexOf('=');
			if( sp2 == -1 ) {
				if( name == parameters_array[i] ) {
					return "true";
				}
			}
			else if( name == parameters_array[i].substring(0,sp2) ) {
				return parameters_array[i].substring(sp2+1,parameters_array[i].length);
			}
			i = i + 1;
		}
	}
	return null;
}

function showhide(id, show) {
	var i = document.getElementById(id);
	if (! i) { return; }
	if (show) {
		i.style.visibility = "visible";
		i.style.display = "block";
	} else {
		i.style.visibility = "hidden";
		i.style.display = "none";
	}
}

function loadedXML() {
	return xmlDoc;
}

//
// load some xml
//
function importXML(file) {
	if (document.implementation && document.implementation.createDocument)
	{
		xmlDoc = document.implementation.createDocument('','doc', null);
		xmlHttp = new XMLHttpRequest();
		xmlHttp.open('GET',file, false);
		xmlHttp.send(null);
		xmlDoc = xmlHttp.responseXML;
		return xmlDoc;
	}
	else if (window.ActiveXObject) {
		xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async = false;
		xmlDoc.load(file);
		while (xmlDoc.readyState != 4) { }
		return xmlDoc;
 	}
	else {
		alert('Your browser can\'t handle the truth');
		return null;
	}
}

///
/// initialize a new tile engine object
/// usage: var engine = new tile_engine_new(parentdiv,stylehints,wmssource,lon,lat,zoom,optional width, optional height)
///
function tile_engine_new(parentname,hints,feedurl,url,lon,lat,zoom,w,h) {

	// get parent div or fail
	this.parent = document.getElementById(parentname);
	if( this.parent == null ) {
		alert('The tile map engine cannot find a parent container named ['+parentname+']');
		return;
	}

	//
	// store for later
	//

	this.parentname = parentname;
	this.hints = hints;
	this.feedurl = feedurl;
	this.url = url;
	this.lon = lon;
	this.lat = lat;
	this.zoom = zoom;
	this.optionalw = w;
	this.optionalh = h;
	this.dragcontainer = 1;
	this.debug = 0;
	this.parent.tile_engine = this;

	// for firefox keyboard
	defaultEngine = this;
	document.engine = this;


	this.seteditvis = function() {
		if (this.zoom >= 13) {
			showhide("editlinkdiv", true);
		} else {
			showhide("editlinkdiv", false);
		}
	}
	this.seteditvis();

	//
	// decide on display width and height
	//
	if( !w || !h ) {
		w = parseInt(this.parent.style.width);
		h = parseInt(this.parent.style.height);
		if(!w || !h) {
			w = 512;
			h = 256;
			this.parent.style.width = w + 'px';
			this.parent.style.height = h + 'px';
		}
	} else {
		this.parent.style.width = parseInt(w) + 'px';
		this.parent.style.height = parseInt(h) + 'px';
	}
	this.displaywidth = w;
	this.displayheight = h;


	//
	// enforce parent div style?
	// position absolute is really only required for firefox
	// http://www.quirksmode.org/js/findpos.html
	//

	this.parent_x = getCSSPositionX(this.parent);
	this.parent_y = getCSSPositionY(this.parent);

	//	this.parent.style.left = this.parent_x;
	//	this.parent.style.top = this.parent_y;
	//	this.parent.style.position = 'absolute';

	this.parent.style.position = 'relative';
	this.parent.style.overflow = 'hidden';
	this.parent.style.cursor = 'move';
	this.parent.style.backgroundColor = '#000036';

	//
	// it is possible that this collection is already in use - clean it
	//

	while( this.parent.hasChildNodes() ) {
		this.parent.removeChild( this.parent.firstChild );
	}

	//
	// build inner tile container for theoretical speed improvement?
	// center in parent for simplicity of math
	// size of inner container is irrelevant since overflow is enabled
	//

	if( this.dragcontainer ) {
		this.tiles = document.createElement('div');
		this.tiles.style.position = 'absolute';
		this.tiles.style.left = this.displaywidth/2 + 'px';
		this.tiles.style.top = this.displayheight/2 + 'px';
		this.tiles.style.width = '16px';
		this.tiles.style.height = '16px';
		if( this.debug ) {
			this.tiles.style.border = 'dashed green 1px';
		}
		this.tiles.tile_engine = this;
		this.parent.appendChild(this.tiles);
	} else {
		this.tiles = this.parent;
	}

	///
	/// focus over specified lon/lat and zoom
	/// user should call this.drag(0,0) after this to force an initial refresh
	///

	this.performzoom = function(lon,lat,z) {

		// setup for zoom
		// this engine operates at * scale to try avoid tile errors thrashing server cache
	
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
		this.lon_max = this.lon_round + this.lon_quant;
		this.lat_min = this.lat_round - this.lat_quant;
		this.lat_max = this.lat_round + this.lat_quant;
	
		// set tiled region details [ this is the span of all tiles in pixels ]
		this.centerx = 0;
		this.centery = 0;
		this.tilewidth = 256;
		this.tileheight = 128;
		this.left = -this.tilewidth;
		this.right = this.tilewidth;
		this.top = -this.tileheight;
		this.bot = this.tileheight;
	
		// adjust the current center position slightly to reflect exact lat/lon not rounded
		this.centerx -= (this.lon_scaled-this.lon_round)/(this.lon_max-this.lon_min)*(this.right-this.left);
		this.centery -= (this.lat_scaled-this.lat_round)/(this.lat_max-this.lat_min)*(this.bot-this.top);
	}

	///
	/// update permalinks using DOM rather than using a JS link
	/// (so they can be copied and pasted)
	///

	this.update_perma_link = function() {
		//var editlink = document.getElementById('editlink');
		//var permalink = document.getElementById('permalink');
		var debuginfo = document.getElementById('debuginfo');
		var editlinkscale = (360.0/Math.pow(2.0,this.zoom))/512.0;
		var linklat = 180 / 3.141592 * (2 * Math.atan(Math.exp(this.lat * 3.141592 / 180)) - 3.141592 / 2); // because we're using mercator
		//if (permalink) {
		//	permalink.href = "http://" + urlbase + urlpath + "?zoom=" + this.zoom + "&lon=" + this.lon + "&lat=" + linklat;
		//}
		//if (editlink) {
		//	editlink.href = "http://www.openstreetmap.org/edit/edit-map.html?lat=" + linklat + "&lon=" + this.lon + "&scale=" + editlinkscale;
		//}
		if (debuginfo) {
			debuginfo.innerHTML = "lat/lon: " + linklat + " " + this.lon;
		}

    var myhtml = "<a href=\"http://www.openstreetmap.org/edit/view-map2.html?lat=" 
           								+ linklat + "&lon=" + this.lon + "&scale=" + editlinkscale + "\" target=\"_new\">link to this map</a>";

    if( this.zoom >=14 )
    {
      myhtml +=  ", <a href=\"http://www.openstreetmap.org/edit/edit-map.html?lat=" 
           								+ linklat + "&lon=" + this.lon + "&scale=" + editlinkscale + "\" target=\"_new\">edit this map</a>";

    }
    else
    {
      myhtml += " (zoom in to edit map)";
    
    }
    var linksdiv = document.getElementById('linksdiv');
    linksdiv.innerHTML = myhtml;
    
		//alert("Latitude: " + this.lat + " Longitude: " + this.lon + " Zoom: " + this.zoom + " Scale: " + editlinkscale);
	}

	///
	/// draw the spanning lon/lat range
	/// drag is simply the mouse delta in pixels
	///

	this.drag = function(dragx,dragy) {

		// move the drag offset
		this.centerx += dragx;
		this.centery += dragy;

		// update where we think the user is actually focused
		this.lon = ( this.lon_round - ( this.lon_max - this.lon_min ) / ( this.right - this.left ) * this.centerx ) / this.scale;
		this.lat = - ( this.lat_round - ( this.lat_max - this.lat_min ) / ( this.bot - this.top ) * this.centery ) / this.scale;

		this.update_perma_link();

		// show it
		var helper = this.navhelp; //document.getElementById( this.parentname + "_helper");
//		if( helper ) {
//			helper.innerHTML = "[ You are at longitude, latitude = " + this.lon + ", " + this.lat + " ]";
//		}

 		// extend exposed sections
		var dirty = false;
		while( this.left + this.centerx > -this.displaywidth/2 && this.lon_min > this.lon_min_clamp ) {
			this.left -= this.tilewidth;
			this.lon_min -= this.lon_quant;
			dirty = true;
		}
		while( this.right + this.centerx < this.displaywidth/2 && this.lon_max < this.lon_max_clamp ) {
			this.right += this.tilewidth;
			this.lon_max += this.lon_quant;
			dirty = true;
		}
		while( this.top + this.centery > -this.displayheight/2 && this.lat_min > this.lat_min_clamp ) {
			this.top -= this.tileheight;
			this.lat_min -= this.lat_quant;
			dirty = true;
		}
		while( this.bot + this.centery < this.displayheight/2 && this.lat_max < this.lat_max_clamp ) {
			this.bot += this.tileheight;
			this.lat_max += this.lon_quant;
			dirty = true;
		}

		// prepare to walk the container and assure that all nodes are correct
		var containerx;
		var containery;

		// in drag container mode we do not have to move the children all the time
		if( this.dragcontainer ) {
			this.tiles.style.left = this.displaywidth / 2 + this.centerx + 'px';
			this.tiles.style.top = this.displayheight / 2 + this.centery + 'px';
			if( !dirty && this.tiles.hasChildNodes() ) {
				return;
			}
			containerx = this.left;
			containery = this.top;
		} else {
			containerx = this.left + this.centerx;
			containery = this.top + this.centery;
		}

		// walk all tiles and repair as needed
		// xxx one bug is that it walks the _entire_ width and height... not just visible.
		// xxx this makes cleanup harder and perhaps a bitmap is better

		var removehidden = 1;
		var removecolumn = 0;
		var removerow = 0;
		var containeryreset = containery;

		for( var x = this.lon_min; x < this.lon_max ; x+= this.lon_quant ) {

			// will this row be visible in the next round?
			if( removehidden ) {
				var rx = containerx + this.centerx;
				if( rx > this.displaywidth / 2 ) {
					removerow = 1;
					// ideally i would truncate max width here
				} else if( rx + this.tilewidth < - this.displaywidth / 2 ) {
					removerow = 1;
				} else {
					removerow = 0;
				}
			}

			for( var y = this.lat_min; y < this.lat_max ; y+= this.lat_quant ) {

				// is this column visible?
				if( removehidden ) {
					var ry = containery + this.centery;
					if( ry > this.displayheight / 2 ) {
						removecolumn = 1;
					} else if( ry + this.tileheight < - this.displayheight / 2 ) {
						removecolumn = 1;
					} else {
						removecolumn = 0;
					}
				}

				// convert to WMS compliant coordinate system
				var lt = x / this.scale;
				var rt = lt + this.lon_quant / this.scale;
				var tp = y / this.scale;
				var bt = tp + this.lat_quant / this.scale;
				var temp = bt;
				var bt = -tp;
				var tp = -temp;

				// modify for mercator-projected tiles: 
				tp = 180 / 3.141592 * (2 * Math.atan(Math.exp(tp * 3.141592 / 180)) - 3.141592 / 2);
				bt = 180 / 3.141592 * (2 * Math.atan(Math.exp(bt * 3.141592 / 180)) - 3.141592 / 2);
				
				// make a key
				var key = this.url + "&WIDTH="+(this.tilewidth)+"&HEIGHT="+(this.tileheight)+"&BBOX="+lt+","+tp+","+rt+","+bt;

				// see if our tile is already present
				var node = document.getElementById(key);

				/*
				// remove if marked for remove
				// xxx this fails because the paint algorithm only visits this loop rarely
				// xxx a bitmap may be better
				if( false && removerow || removecolumn ) {
					if( node ) {
						this.tiles.removeChild( node );
					}
				} else
				*/
				// create if not present
				if(!node) {
					if( this.debug > 0) {
						node = document.createElement('div');
					} else {
						node = document.createElement('img');
					}
					node.id = key;
					node.className = 'tile';
					node.style.position = 'absolute';
					//node.style.backgroundColor = 'darkblue';
					node.style.width = this.tilewidth + 'px';
					node.style.height = this.tileheight + 'px';
					node.style.left = containerx + 'px';
					node.style.top = containery + 'px';
					node.style.zIndex = 10; // to appear under the rss elements
					node.tile_engine = this;
					if( this.debug > 0) {
						node.style.border = "1px solid yellow";
						node.innerHTML = key;
						if( this.debug > 1 ) {
							var img = document.createElement('img');
							img.src = key;
							node.appendChild(img);
						}
					}
					node.src = key;
					this.tiles.appendChild(node);
				}
				// adjust if using active style
				else if( !this.dragcontainer ) {
					node.style.left = containerx + 'px';
					node.style.top = containery + 'px';
				}

				containery += this.tileheight;
			}
			containery = containeryreset;
			containerx += this.tilewidth;
		}

	}

	function getfield(content,field) {
		for(var subnode = content.firstChild; subnode != null; subnode = subnode.nextSibling ) {
			if( field == subnode.nodeName ) {
				if( subnode.nodeType == 1 ) {
					if( subnode.firstChild ) {
						return subnode.firstChild.data;
					}
				}
				break;
			}
		}
		return "";
	}

	///
	/// draw a feed
	/// could use http://www.w3schools.com/xsl/xsl_client.asp
	///

	this.drawfeed = function(data) {

		if( data == null || data.firstChild == null) {
			return;
		}

		if( data.firstChild.nodeName == 'rdf:RDF') {
			data = data.firstChild;
		} else if( data.firstChild.nextSibling.nodeName == 'rdf:RDF' ) {
			data = data.firstChild.nextSibling;
		}

		for(var content = data.firstChild; content != null; content = content.nextSibling ) {

				if( content.nodeName != 'item' ) {
					continue;
				}

				var icon = getfield(content,"dc:thumb");
				var thumb = getfield(content,"dc:thumb");
				var lon = parseFloat(getfield(content,"geo:long"));
				var lat = parseFloat(getfield(content,"geo:lat"));
				var title = getfield(content,"title");
				var link = getfield(content,"link");
				var description = getfield(content,"description");

				// internal quirk
				lat = -lat;

//				var thumbtack_a = document.createElement('a');
//				var thumbtack_div = document.createElement('div');
//				thumbtack_a.href = link;
//				http://thingster.org/admin/.admin.jpg

				// draw where the update where we think the user is actually focused
				var x = ( lon * this.scale - this.lon_round ) * (this.right-this.left) / (this.lon_max - this.lon_min );
				var y = ( lat * this.scale - this.lat_round ) * (this.right-this.left) / (this.lon_max - this.lon_min );

				var img = document.createElement('img');
				img.style.position = "absolute";
				img.style.left = x+'px';
				img.style.top = y+'px';
				img.style.width = '30px';
				img.style.height = '20px';
				img.style.zIndex = 15; // to appear on top of the image elements
				img.border = 0;
				if( img.runtimeStyle ) {
					// http://redvip.homelinux.net/varios/explorer-png-en.html
					img.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='tile.png',sizingMethod='scale')";
					img.src = "transparentpixel.gif";
				} else {
					img.src = "tile.png";
				}
				var a = document.createElement('a');
				a.href = link;
				a.style.border = 0;
				a.appendChild(img);
				this.tiles.appendChild(a);
			}
	}

	///
	/// Setup the engine zoomed at a position to show a feed
	///

	this.zoomtofeed = function(data) {

		// any content?
		if( data == null || data.firstChild == null) {
			this.performzoom(0,0,0);
			return;
		}

		if( data.firstChild.nodeName == 'rdf:RDF') {
			data = data.firstChild;
		} else if( data.firstChild.nextSibling.nodeName == 'rdf:RDF' ) {
			data = data.firstChild.nextSibling;
		}

		// get extents

		var success=0;
		var maxlon=0,maxlat=0,minlon=0,minlat=0;

		for(var content = data.firstChild; content != null; content = content.nextSibling ) {

			if( content.nodeName != 'item' ) {
				continue;
			}

			var lon = parseFloat(getfield(content,"geo:long"));
			var lat = parseFloat(getfield(content,"geo:lat"));

			if( maxlon < lon || success == 0) maxlon = lon;
			if( minlon > lon || success == 0) minlon = lon;
			if( maxlat < lat || success == 0) maxlat = lat;
			if( minlat > lat || success == 0) minlat = lat;
			success = 1;
		}

		// calculate zoom required to reach extent

		var lon = (maxlon-minlon)/1.9;
		var lat = (maxlat-minlat)/1.9;
		var zoomaxis = Math.sqrt( lon*lon + lat*lat );
		lon += minlon;
		lat += minlat;
		var zoom = 0;
		var zoomquant = Math.sqrt( 180 * 180 + 90 * 90 );
		while( zoomaxis < zoomquant && zoom < 13) {
			zoomquant = zoomquant / 2;
			zoom++;
		}

		// xxx think more about this; had to back out a bit - use y axis as base? w*w+h*h?
		if( zoom > 0 ) zoom--;

		// set current zoom

		this.zoom = zoom;

		// perform zoom

		if( success ) {
			this.performzoom(lon,lat,zoom);
		} else {
			this.performzoom(0,0,0);
		}

		this.zoom = zoom;

	}

	///
	/// immediately draw and or fit to feed
	///
	if( feedurl ) {
		var data = importXML( feedurl );
		if( data && hints && hints.indexOf('FIT') >= 0 ) {
			this.zoomtofeed(data);
			this.drag(0,0);
			this.drawfeed(data);
		} else {
			this.performzoom(lon,lat,zoom);
			this.drag(0,0);
			this.drawfeed(data);
		}
	} else {
		this.performzoom(lon,lat,zoom);
		this.drag(0,0);
	}

	///
	/// intercept context events to minimize out-of-browser interruptions
	///
	
	this.event_context = function(e) {
		return false;
	}

	///
	/// keys
	///

	this.event_key = function(e) {
		var key = 0;		

		var hostengine = defaultEngine;

		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine == null ) {
			hostengine = defaultEngine;
			if( hostengine == null ) {
				return;
			}
		}

		if( e == null && document.all ) {
			e = window.event;
		}

		if( e ) {
			if( e.keyCode ) {
				key = e.keyCode;
			}
			else if( e.which ) {
				key = e.which;
			}

			switch(key) {
			case 97: // a = left
				hostengine.drag(16,0);
				break;
			case 100: // d = right
				hostengine.drag(-16,0);
				break;
			case 119: // w = up
				hostengine.drag(0,16);
				break;
			case 120: // x = dn
				hostengine.drag(0,-16);
				break;
			case 115: // s = center
				new tile_engine_new(hostengine.parentname,
							"FULL",
							hostengine.feedurl, // xxx hrm, cache this?
							hostengine.url,
							hostengine.lon,
							hostengine.lat,
							hostengine.zoom,
							0,0
							);
				break;
			case 122: // z = zoom
				new tile_engine_new(hostengine.parentname,
							"FULL",
							hostengine.feedurl, // xxx hrm, cache this?
							hostengine.url,
							hostengine.lon,
							hostengine.lat,
							hostengine.zoom + 2,
							0,0
							);
				break;
			case  99: // c = unzoom
				new tile_engine_new(hostengine.parentname,
							"FULL",
							hostengine.feedurl, // xxx hrm, cache this?
							hostengine.url,
							hostengine.lon,
							hostengine.lat,
							hostengine.zoom - 2,
							0,0
							);
				break;
			}
		}	
	}

	///
	/// catch mouse move events
	/// this routine _must_ return false or else the operating system outside-of-browser-scope drag and drop handler will interfere
	///

	this.event_mouse_move = function(e) {

		var hostengine = null;
		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine && hostengine.drag ) {
			if( hostengine.mousedown ) {
				if( netscape ) {
					hostengine.mousex = parseInt(e.pageX) + 0.0;
					hostengine.mousey = parseInt(e.pageY) + 0.0;
				} else {
					hostengine.mousex = parseInt(window.event.clientX) + 0.0;
					hostengine.mousey = parseInt(window.event.clientY) + 0.0;
				}
				hostengine.drag(hostengine.mousex-hostengine.lastmousex,hostengine.mousey-hostengine.lastmousey);
			}
			hostengine.lastmousex = hostengine.mousex;
			hostengine.lastmousey = hostengine.mousey;
		}

		// must return false to prevent operating system drag and drop from handling events
		return false;
	}

	///
	/// catch mouse down
	///

	this.event_mouse_down = function(e) {

		var hostengine = null;
		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			if( netscape ) {
				hostengine.mousex = parseInt(e.pageX) + 0.0;
				hostengine.mousey = parseInt(e.pageY) + 0.0;
			} else {
				hostengine.mousex = parseInt(window.event.clientX) + 0.0;
				hostengine.mousey = parseInt(window.event.clientY) + 0.0;
			}
			hostengine.lastmousex = hostengine.mousex;
			hostengine.lastmousey = hostengine.mousey;
			hostengine.mousedown = 1;
		}

		// must return false to prevent operating system drag and drop from handling events
		return false;
	}

	///
	/// catch double click (use to center map)
	///

	this.event_double_click = function(e) {

		var hostengine = null;
		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			if( netscape ) {
				hostengine.mousex = parseInt(e.pageX) + 0.0;
				hostengine.mousey = parseInt(e.pageY) + 0.0;
			} else {
				hostengine.mousex = parseInt(window.event.clientX) + 0.0;
				hostengine.mousey = parseInt(window.event.clientY) + 0.0;
			}
			var dx = hostengine.mousex-(hostengine.displaywidth/2)-hostengine.parent_x;
			var dy = hostengine.mousey-(hostengine.displayheight/2)-hostengine.parent_y;
			hostengine.drag(-dx,-dy); // TODO smooth
		}

		// must return false to prevent operating system drag and drop from handling events
		return false;

	}

	///
	/// catch mouse up
	///

	this.event_mouse_up = function(e) {

		var hostengine = null;
		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			if( netscape ) {
				hostengine.mousex = parseInt(e.pageX) + 0.0;
				hostengine.mousey = parseInt(e.pageY) + 0.0;
			} else {
				hostengine.mousex = parseInt(window.event.clientX) + 0.0;
				hostengine.mousey = parseInt(window.event.clientY) + 0.0;
			}
			hostengine.mousedown = 0;
			hostengine.update_perma_link();
		}

		// must return false to prevent operating system drag and drop from handling events
		return false;
	}

	///
	/// catch mouse out
	///

	this.event_mouse_out = function(e) {

		var hostengine = null;
		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			if( netscape ) {
				hostengine.mousex = parseInt(e.pageX) + 0.0;
				hostengine.mousey = parseInt(e.pageY) + 0.0;
			} else {
				hostengine.mousex = parseInt(window.event.clientX) + 0.0;
				hostengine.mousey = parseInt(window.event.clientY) + 0.0;
			}
			hostengine.mousedown = 0;
			hostengine.update_perma_link();
		}

		// must return false to prevent operating system drag and drop from handling events
		return false;
	}

	///
	/// zoom a tile group
	///
	this.tile_engine_zoomout = function(e) {
	 	var amount = -2;
		var hostengine = this.tile_engine;

		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			new tile_engine_new(hostengine.parentname,
							"FULL",
							hostengine.feedurl, // xxx hrm, cache this?
							hostengine.url,
							hostengine.lon,
							hostengine.lat,
							hostengine.zoom + amount,
							0,0
							);
		}

	}

	///
	/// zoom a tile group
	///
	this.tile_engine_zoomin = function(e) {
		var amount = 2;
		var hostengine = this.tile_engine;

		if( window && window.event && window.event.srcElement ) {
			hostengine = window.event.srcElement.tile_engine;
		} else if( e.target ) {
			hostengine = e.target.tile_engine;
		} else if( e.srcElement ) {
			hostengine = e.srcElement.tile_engine;
		}

		if( hostengine ) {
			new tile_engine_new(hostengine.parentname,
							"FULL",
							hostengine.feedurl, // xxx hrm, cache this?
							hostengine.url,
							hostengine.lon,
							hostengine.lat,
							hostengine.zoom + amount,
							0,0
							);
		}

	}

	///
	/// register new handlers to catch desired events
	///

	this.event_catch = function(component) {

		if( netscape ) {
			window.captureEvents(Event.MOUSEMOVE);
			window.captureEvents(Event.KEYPRESS);
		}

		if( component ) {
			component.onmousemove = this.event_mouse_move;
			component.onmousedown = this.event_mouse_down;
			component.onmouseup = this.event_mouse_up;
			//component.onmouseout = this.event_mouse_out;
			component.onkeypress = this.event_key;
			window.ondblclick = this.event_double_click;
		}

		if( window ) {
			window.onmousemove = this.event_mouse_move;
			window.onmousedown = this.event_mouse_down;
			window.onmouseup = this.event_mouse_up;
			//window.onmouseout = this.event_mouse_out;
			window.onkeypress = this.event_key;
			window.ondblclick = this.event_double_click;
		}

		//document.onkeypress = this.event_key;
		//document.addEventListener('keypress', this.key_handler, true);
	}

	this.link = function() {
		document.location = "http://" + urlbase + urlpath + "?zoom=" + this.zoom + "&lon=" + this.lon + "&lat=" + this.lat;
	}
	this.editlink = function() {
		document.location = "http://openstreetmap.org/?zoom=" + this.zoom + "&lon=" + this.lon + "&lat=" + this.lat;
	}

	// attach event capture parent div
	this.event_catch(this.parent);

	// draw navigation buttons into the parent div
	this.navform = document.createElement('form');
	this.navform.name = parentname + "_form";
	this.navform.style.position = 'absolute';
	this.navform.style.zIndex = 15;
	this.navform.tile_engine = this;

	this.navout = document.createElement('input');
	this.navout.name = parentname + "_out";
	this.navout.type = "image";
	this.navout.src = "/images/map_zoomout.png";
	this.navout.value = "out";
	this.navout.style.zIndex = 99;
	this.navout.style.cursor = this.zoom <= 0 ? 'arrow' : 'hand';
	//this.navout.onclick = this.zoom == 0 ? 0 : this.tile_engine_zoomout;

  this.navout.onclick = this.tile_engine_zoomout;

  this.navout.tile_engine = this;
	this.navform.appendChild(this.navout);

	this.navin = document.createElement('input');
	this.navin.name = parentname + "_in";
	this.navin.type = "image";
	this.navin.src = "/images/map_zoomin.png";
	this.navin.value = "in";
	this.navin.style.zIndex = 99;
	this.navin.style.cursor = this.zoom >= 20 ? 'arrow' : 'hand';


  // this.navin.onclick = this.zoom >= 20 ? 0 : this.tile_engine_zoomin;

  this.navin.onclick = this.tile_engine_zoomin;
  
  this.navin.tile_engine = this;
	this.navform.appendChild(this.navin);

//	this.navhelp = document.createElement('div');
//	this.navhelp.name = parentname + "_helper";
//	this.navhelp.style.display = 'inline';
//	this.navhelp.style.color = 'white';
//	this.navhelp.innerHTML = '[ Please select and drag to move map ]'
//	this.navform.appendChild(this.navhelp);

	this.parent.appendChild(this.navform);

}


