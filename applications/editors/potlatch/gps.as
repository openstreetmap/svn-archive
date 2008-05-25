
	// ================================================================
	// GPS functions
	
	// loadGPS		- load GPS backdrop from server

	function loadGPS() {
		_root.map.createEmptyMovieClip('gps',3);
		if (Key.isDown(Key.SHIFT)) { loadMovie(gpsurl+'?xmin='+(_root.edge_l-0.01)+'&xmax='+(_root.edge_r+0.01)+'&ymin='+(_root.edge_b-0.01)+'&ymax='+(_root.edge_t+0.01)+'&baselong='+_root.baselong+'&basey='+_root.basey+'&masterscale='+_root.masterscale+'&token='+_root.usertoken,_root.map.gps); }
							  else { loadMovie(gpsurl+'?xmin='+(_root.edge_l-0.01)+'&xmax='+(_root.edge_r+0.01)+'&ymin='+(_root.edge_b-0.01)+'&ymax='+(_root.edge_t+0.01)+'&baselong='+_root.baselong+'&basey='+_root.basey+'&masterscale='+_root.masterscale,_root.map.gps); }
	}

	// parseGPX		- parse GPX file
	// parsePoint
	
	function parseGPX(gpxname) {
		_root.windows.attachMovie("modal","pleasewait",++windowdepth);
		_root.windows.pleasewait.init(295,35,new Array(),null);
		_root.windows.pleasewait.box.createTextField("prompt",2,7,9,280,20);
		writeText(_root.windows.pleasewait.box.prompt,"Please wait while the GPX track is processed.");

		_root.tracks=new Array();
		_root.curtrack=0; _root.tracks[curtrack]=new Array();
		_root.map.gpx.createEmptyMovieClip("line",1);
		_root.map.gpx.line.lineStyle(1,0x00FFFF,100,false,"none");

		var gpxs=gpxname.split(',');
		for (var g in gpxs) {
			var gpxdoc=new XML();
			gpxdoc.load(gpxurl+gpxs[g]+gpxsuffix);
			gpxdoc.onLoad=function() {
		
				_root.lastTime=0;
				var waypoints=new Array();
				var level1=this.childNodes;
				for (i=0; i<level1.length; i+=1) {
					if (level1[i].nodeName=='gpx') {
						var level2=level1[i].childNodes;
						for (j=0; j<level2.length; j+=1) {
							if (level2[j].nodeName=='trk') {
								var level3=level2[j].childNodes;
								for (k=0; k<level3.length; k+=1) {
									if (level3[k].nodeName=='trkseg') {
										var level4=level3[k].childNodes;
										for (l=0; l<level4.length; l+=1) {
											if (level4[l].nodeName=='trkpt') {
												parsePoint(level4[l]);
											}
										}
									}
								}
							} else if (level2[j].nodeName=='wpt') {
								var wpattr=new Array();
								var level3=level2[j].childNodes;
								for (k=0; k<level3.length; k+=1) {
									if (level3[k].nodeName=='name') { wpattr['name'			  ]=level3[k].firstChild.nodeValue; }
									if (level3[k].nodeName=='ele' ) { wpattr['ele'			  ]=level3[k].firstChild.nodeValue; }
									if (level3[k].nodeName=='sym' ) { wpattr['wpt_symbol'     ]=level3[k].firstChild.nodeValue; }
									if (level3[k].nodeName=='desc') { wpattr['wpt_description']=level3[k].firstChild.nodeValue; }
								}
								waypoints.push(new Array(level2[j].attributes['lon'],level2[j].attributes['lat'],wpattr));
							}
						}
					}
				}
				// Do waypoints last, because we might not have the base lat/lon at the start
				for (i in waypoints) {
					_root.map.pois.attachMovie("poi",--newpoiid,++poidepth);
					_root.map.pois[newpoiid]._x=long2coord(waypoints[i][0]);
					_root.map.pois[newpoiid]._y=lat2coord (waypoints[i][1]);
					_root.map.pois[newpoiid].attr=waypoints[i][2];
					_root.map.pois[newpoiid].locked=true;
					_root.map.pois[newpoiid].clean=false;
					// _root.map.pois[newpoiid].recolour();
					_root.poicount+=1;
				}
				_root.windows.pleasewait.remove();
			};
		}
	}

	function parsePoint(xmlobj) {
		if (lat) {} else { lat =xmlobj.attributes['lat'];	// Get root co-ords
						   long=xmlobj.attributes['lon'];	//  from GPX?
						   startPotlatch(); }				//   |
		var y= lat2coord(xmlobj.attributes['lat']);
		var x=long2coord(xmlobj.attributes['lon']);
		var tme=new Date();
		tme.setTime(0);
		var xcn=xmlobj.childNodes;
		for (a in xcn) {
			if (xcn[a].nodeName=='time') {
				str=xcn[a].firstChild.nodeValue;
				if (str.substr( 4,1)=='-' &&
					str.substr(10,1)=='T' &&
					str.substr(19,1)=='Z') {
					tme.setFullYear(str.substr(0,4),str.substr(5,2),str.substr(8,2));
					tme.setHours(str.substr(11,2));
					tme.setMinutes(str.substr(14,2));
					tme.setSeconds(str.substr(17,2));
				}
			}
		}

		if (tme==null || tme.getTime()-_root.lastTime<180000) {
			_root.map.gpx.line.lineTo(x,y);
			_root.tracks[curtrack].push(new Array(x,y));
		} else {
			_root.map.gpx.line.moveTo(x,y);
			_root.curtrack+=1;
			_root.tracks[curtrack]=new Array();
			_root.tracks[curtrack].push(new Array(x,y));
		}
		lastTime=tme.getTime();
	}
	
	// gpxToWays	- convert all GPS tracks to ways
	
	function gpxToWays() {
		var tol=0.2; if (Key.isDown(Key.SHIFT)) { tol=0.1; }
		for (var i=0; i<_root.tracks.length; i+=1) {
			_root.tracks[i]=simplifyPath(_root.tracks[i],tol);

			_root.newwayid--;
			_root.map.ways.attachMovie("way",newwayid,++waydepth);
			_root.map.ways[newwayid].xmin= 999999;
			_root.map.ways[newwayid].xmax=-999999;
			_root.map.ways[newwayid].ymin= 999999;
			_root.map.ways[newwayid].ymax=-999999;
			for (var j=0; j<_root.tracks[i].length; j+=1) {
				_root.map.ways[newwayid].path.push(new Array(_root.tracks[i][j][0],_root.tracks[i][j][1],--newnodeid,Math.min(j,1),new Array(),0));
				_root.map.ways[newwayid].xmin=Math.min(_root.tracks[i][j][0],_root.map.ways[newwayid].xmin);
				_root.map.ways[newwayid].xmax=Math.max(_root.tracks[i][j][0],_root.map.ways[newwayid].xmax);
				_root.map.ways[newwayid].ymin=Math.min(_root.tracks[i][j][1],_root.map.ways[newwayid].ymin);
				_root.map.ways[newwayid].ymax=Math.max(_root.tracks[i][j][1],_root.map.ways[newwayid].ymax);
			}
			_root.map.ways[newwayid].clean=false;
			_root.map.ways[newwayid].locked=true;
			_root.map.ways[newwayid].redraw();
		}
	}

	// ================================================================
	// Douglas-Peucker code

	function distance(ax,ay,bx,by,l,cx,cy) {
		// l=length of line
		// r=proportion along AB line (0-1) of nearest point
		var r=((cx-ax)*(bx-ax)+(cy-ay)*(by-ay))/(l*l);
		// now find the length from cx,cy to ax+r*(bx-ax),ay+r*(by-ay)
		var px=(ax+r*(bx-ax)-cx);
		var py=(ay+r*(by-ay)-cy);
		return Math.sqrt(px*px+py*py);
	}

	function simplifyPath(track,tolerance) {
		if (track.length<=2) { return track; }
		
		result=new Array();
		stack=new Array();
		stack.push(track.length-1);
		anchor=0;
		
		while (stack.length) {
			float=stack[stack.length-1];
			var xa=track[anchor][0]; var xb=track[float][0];
			var ya=track[anchor][1]; var yb=track[float][1];
			var l=Math.sqrt((xb-xa)*(xb-xa)+(yb-ya)*(yb-ya));
			var furthest=0; var furthdist=0;
	
			// find furthest-out point
			for (var i=anchor+1; i<float; i+=1) {
				var d=distance(xa,ya,xb,yb,l,track[i][0],track[i][1]);
				if (d>furthdist && d>tolerance) { furthest=i; furthdist=d; }
			}
			
			if (furthest==0) {
				anchor=stack.pop();
				result.push(new Array(track[float][0],track[float][1]));
			} else {
				stack.push(furthest);
			}
		}

		return result;
	}

