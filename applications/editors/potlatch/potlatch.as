
	// =====================================================================================
	// Initialise

	// Site-specific URLs
//	var apiurl='rubyamf.cgi';
//	var gpsurl='/potlatch/getgps.cgi';
//	var gpxurl='http://localhost:3000/trace/';
//	var yahoourl='/~richard/potlatch/ymap.swf';
	var apiurl='../api/0.5/amf';
	var gpsurl='../api/0.5/swf/trackpoints';
	var gpxurl='http://www.openstreetmap.org/trace/';
	var yahoourl='/potlatch/ymap.swf';

	// Master movieclip for map
	_root.createEmptyMovieClip("map",10);
	_root.map.setMask(_root.masksquare);

	// Get variables from browser (forcing them into numbers where appropriate)
	// London 51.5,0; Weybridge 51.4,-0.5; Worcester 52.2,-2.25; Woodstock 51.85,-1.35
	var minscale=12;				// don't zoom out past this
	var maxscale=19;				// don't zoom in past this
	var baselat=Math.pow(lat,1);
	var baselong=Math.pow(long,1);
	var scale=Math.max(Math.min(Math.pow(scale,1),maxscale),minscale);
	var usertoken=token;
	
	// Preferences
	preferences=SharedObject.getLocal("preferences");

	// Global dimensions
	var uwidth=700;
	var uheight=600;

	// Key listener - needs to be initialised before Yahoo
	keyListener=new Object();
	keyListener.onKeyDown=function() { keyPressed(); };
	Key.addListener(keyListener);

	// Mouse listener - copes with custom pointers
//	mouseListener=new Object();
//	mouseListener.onMouseMove=function() { trackMouse(); };

	// Initialise Yahoo
	var ylat=baselat;	var lastylat=ylat;		// monitored by ymap.swf
	var ylon=baselong;	var lastylon=ylon;		//  |
	var yzoom=8;		var lastyzoom=yzoom;	//  |
	var bgxoffset=0;	var bgyoffset=0;		// manually correct Yahoo imagery

	_root.createEmptyMovieClip("yahoo",7);
	loadMovie(yahoourl,_root.yahoo);
	_root.yahoo.swapDepths(_root.masksquare);
	_root.yahoo.setMask(_root.masksquare2);

	// Main initialisation
	_root.map.createEmptyMovieClip("areas"   ,8);  var areadepth=1;
	_root.map.createEmptyMovieClip("gpx"     ,9);
	_root.map.createEmptyMovieClip("ways"    ,10); var waydepth=1;
	_root.map.createEmptyMovieClip("pois"	 ,11); var poidepth=1;
	_root.map.createEmptyMovieClip("elastic",5003); // elastic line
	initTiles();					// create tile clips on layer 7

	_root.masksquare.useHandCursor=false;
	_root.masksquare.onPress   =function() { mapClick(); };
	_root.masksquare.onRollOver=function() { mapRollOver(); };
	_root.masksquare.onRollOut =function() { mapRollOut(); };
	_root.masksquare.onRelease =function() { mapClickEnd(); };

	var basey=lat2y(baselat);		// Y co-ordinate of map centre
	var masterscale=5825.4222222222;// master map scale - how many Flash pixels in 1 degree longitude
									// (for Landsat, 5120)
	var wayselected=0;				// way selected?    0 no, otherwise way id
	var poiselected=0;				// POI selected?    0 no, otherwise way id
	var pointselected=-2;			// point selected? -2 no, otherwise point order
	var waycount=0;					// number of ways currently loaded
	var waysrequested=0;			// total number of ways requested
	var waysreceived=0;				// total number of ways received
	var poicount=0;					// number of POIs currently loaded
	var whichrequested=0;			// total number of 'which ways' requested
	var whichreceived=0;			// total number of 'which ways' received
	var dragmap=false;				// map being dragged?
	var drawpoint=-1;				// point being drawn? -1 no, 0+ yes (point order)
	var newwayid=-1;				// new way ID  (for those not yet saved)
	var newnodeid=-2;				// new node ID (for those not yet saved)
	var newpoiid=-1;				// new POI ID  (for those not yet saved)
	var currentproptype='';			// type of property currently being edited
	var pointertype='';				// current mouse pointer
	var redopropertywindow=0;		// need to redraw property window after deletion?
	var lastkeypressed=null;		// code of last key pressed
	var keytarget='';				// send keys where? ('','dialogue','key','value')
	var tilesetloaded=-1;			// which tileset is loaded?
	var tolerance=4/Math.pow(2,_root.scale-13);
	var bigedge_l=999999; var bigedge_r=-999999; // area of largest whichways
	var bigedge_b=999999; var bigedge_t=-999999; //  |
	var sandbox=false;				// we're doing proper editing
	var signature="Potlatch 0.6a";	// current version
	if (preferences.data.baselayer    ==undefined) { preferences.data.baselayer    =2; }	// show Yahoo?
	if (preferences.data.dimbackground==undefined) { preferences.data.baselayer    =true; }	// dim background?
	if (preferences.data.baselayer    ==1        ) { preferences.data.baselayer    =2; }	// temporary migration
	if (preferences.data.custompointer==undefined) { preferences.data.custompointer=true; }	// use custom pointers?

	// =====================================================================================
	// Icons

	_root.attachMovie("zoomin","i_zoomin",30);
	with (_root.i_zoomin) { _x=5; _y=5; };
	_root.i_zoomin.onPress=function() { zoomIn(); };

	_root.attachMovie("zoomout","i_zoomout",31);
	with (_root.i_zoomout) { _x=5; _y=27; };
	_root.i_zoomout.onPress=function() { zoomOut(); };
	changeScaleTo(_root.scale);

	_root.attachMovie("scissors","i_scissors",32);
	with (_root.i_scissors) { _x=15; _y=583; };
	_root.i_scissors.onPress   =function() { _root.map.ways[wayselected].splitWay(); };
	_root.i_scissors.onRollOver=function() { setFloater("Split way at selected point (X)"); };
	_root.i_scissors.onRollOut =function() { clearFloater(); };

	_root.attachMovie("gps","i_gps",36);
	with (_root.i_gps) { _x=65; _y=583; };
	_root.i_gps.onPress   =function() { loadGPS(); };
	_root.i_gps.onRollOver=function() { setFloater("Show GPS tracks (G)"); };
	_root.i_gps.onRollOut =function() { clearFloater(); };

	_root.attachMovie("prefs","i_prefs",37);
	with (_root.i_prefs) { _x=90; _y=583; };
	_root.i_prefs.onPress   =function() { openOptionsWindow(); };
	_root.i_prefs.onRollOver=function() { setFloater("Set options (choose the map background)"); };
	_root.i_prefs.onRollOut =function() { clearFloater(); };

	_root.attachMovie("newattr","i_newattr",33);
	with (_root.i_newattr) { _x=690; _y=585; };
	_root.i_newattr.onRelease =function() { enterNewAttribute(); };
	_root.i_newattr.onRollOver=function() { setFloater("Add a new attribute"); };
	_root.i_newattr.onRollOut =function() { clearFloater(); };

	_root.attachMovie("repeatattr","i_repeatattr",34);
	with (_root.i_repeatattr) { _x=690; _y=565; };
	_root.i_repeatattr.onPress=function() { repeatAttributes(); };
	_root.i_repeatattr.onRollOver=function() { setFloater("Repeat attributes from the previously selected way (R)"); };
	_root.i_repeatattr.onRollOut =function() { clearFloater(); };

	_root.attachMovie("exclamation","i_warning",35);
	with (_root.i_warning) { _x=10; _y=545; _visible=false; };
	_root.i_warning.onPress=function() { handleWarning(); };
	_root.i_warning.onRollOver=function() { setFloater("An error occurred - click for details"); };
	_root.i_warning.onRollOut =function() { clearFloater(); };
	wflashid=setInterval(function() { _root.i_warning._alpha=150-_root.i_warning._alpha; }, 750);

	_root.attachMovie("rotation","i_direction",39);
	with (_root.i_direction) { _x=40; _y=583; _rotation=-45; _visible=true; _alpha=50; };
	_root.i_direction.onPress=function() { _root.map.ways[wayselected].reverseWay(); };
	_root.i_direction.onRollOver=function() { setFloater("Direction of way - click to reverse"); };
	_root.i_direction.onRollOut =function() { clearFloater(); };

	_root.attachMovie("roundabout","i_circular",40);
	with (_root.i_circular) { _x=40; _y=583; _rotation=-45; _visible=false; };
	_root.i_circular.onRollOver=function() { setFloater("Circular way"); };
	_root.i_circular.onRollOut =function() { clearFloater(); };

	_root.attachMovie("padlock","padlock",41);
	with (_root.padlock) { _y=532; _visible=false; };
	_root.padlock.onPress=function() {
		if (_root.wayselected) {
			if (_root.map.ways[wayselected].path.length>200) {
				setTooltip("too long to unlock:\nplease split into\nshorter ways");
			} else {
				_root.map.ways[wayselected].locked=false;
				_root.map.ways[wayselected].clean=false;
				_root.map.ways[wayselected].redraw();
				_root.padlock._visible=false;
			}
		} else if (_root.poiselected) {
			_root.map.pois[poiselected].locked=false;
			_root.map.pois[poiselected].clean=false;
//			_root.map.pois[poiselected].recolour();
			_root.padlock._visible=false;
		}
	};

	_root.createEmptyMovieClip("pointers",90000);
	_root.pointers.attachMovie("penso"  ,"penso"  ,1); _root.pointers.penso._visible=false;
	_root.pointers.attachMovie("penx"   ,"penx"   ,2); _root.pointers.penx._visible=false;
	_root.pointers.attachMovie("peno"   ,"peno"   ,3); _root.pointers.peno._visible=false;
	_root.pointers.attachMovie("penplus","penplus",5); _root.pointers.penplus._visible=false;
	_root.pointers.attachMovie("pen"    ,"pen"    ,6); _root.pointers.pen._visible=false;
	_root.pointers.attachMovie("hand"   ,"hand"   ,7); _root.pointers.hand._visible=false;


	// =====================================================================================
	// Initialise text areas

	// Debug text fields

	_root.createTextField('chat',20,130,315,400,80); // 515
	with (_root.chat) {
		multiline=true;
		wordWrap=true;
		border=true;
		selectable = true; type = 'input'; _visible = false;
	};

	_root.createTextField('coordmonitor',21,130,400,400,60); // 515
	with (_root.coordmonitor) {
		multiline=true;
		wordWrap=true;
		border=true;
		selectable = true; type = 'input'; _visible = false;
	};

	_root.createTextField('floater',0xFFFFFF,15,30,200,18);
	with (floater) {
		background=true;
		backgroundColor=0xFFEEEE;
		border=true;
		borderColor=0xAAAAAA;
		selectable = false; _visible=false;
	}
	var floaterID=null;


	// Text formats
	
	plainText =new TextFormat(); plainText.color =0x000000; plainText.size =14; plainText.font ="_sans";
	plainSmall=new TextFormat(); plainSmall.color=0x000000;	plainSmall.size=12; plainSmall.font="_sans";
	greySmall =new TextFormat(); greySmall.color =0x888888;	greySmall.size =12; greySmall.font ="_sans";
	boldText  =new TextFormat(); boldText.color  =0x000000; boldText.size  =14; boldText.font  ="_sans"; boldText.bold =true;
	boldSmall =new TextFormat(); boldSmall.color =0x000000; boldSmall.size =12; boldSmall.font ="_sans"; boldSmall.bold=true;
	boldWhite =new TextFormat(); boldWhite.color =0xFFFFFF; boldWhite.size =12; boldWhite.font ="_sans"; boldWhite.bold=true;
	menu_on	  =new TextFormat(); menu_on.color   =0x000000; menu_on.size   =12; menu_on.font   ="_sans"; menu_on.bold  =true;
	menu_off  =new TextFormat(); menu_off.color  =0xFFFFFF; menu_off.size  =12; menu_off.font  ="_sans"; menu_off.bold =true;
	auto_on	  =new TextFormat(); auto_on.color   =0x0000FF; auto_on.size   =12; auto_on.font   ="_sans"; auto_on.bold  =true;
	auto_off  =new TextFormat(); auto_off.color  =0xFFFFFF; auto_off.size  =12; auto_off.font  ="_sans"; auto_off.bold =true;

	// Text fields

	populatePropertyWindow('');	

	_root.createTextField('waysloading',22,580,5,150,20);
	with (_root.waysloading) { text="loading ways"; setTextFormat(plainSmall); type='dynamic'; _visible=false; };

	_root.createTextField('tooltip',46,580,25,150,100);
	with (_root.tooltip  ) { text=""; setTextFormat(plainSmall); selectable=false; type='dynamic'; };

	_root.createTextField('t_type',23,5,505,220,20);
	with (_root.t_type	 ) { text="Welcome to OpenStreetMap"; setTextFormat(boldText); };
	
	_root.createTextField('t_details',24,5,523,220,20);
	with (_root.t_details) { text=signature; setTextFormat(plainText); };
	
	_root.createEmptyMovieClip("properties",50);
	with (_root.properties) { _x=110; _y=525; }; // 110,505

	// TextField listener
	textfieldListener=new Object();
	textfieldListener.onChanged=function() { textChanged(); };

	// Interaction with responder script
	var loaderWaiting=false;

	remote=new NetConnection();
	remote.connect(apiurl);
	remote.onStatus=function(info) { 
		_root.i_warning._visible=true;
	};

	preresponder = function() { };
	preresponder.onResult = function(result) {
		_root.presets=result[0];
		_root.presetmenus=result[1];
		_root.presetnames=result[2];
		_root.colours=result[3];
		_root.casing=result[4];
		_root.areas=result[5];
		_root.autotags=result[6];
		_root.presetselected='road'; setPresetIcon(presetselected);
		_root.i_preset._visible=false;
	};
	remote.call('getpresets',preresponder);

	if (gpx) { parseGPX(gpx); }			// Parse GPX if supplied

	// =====================================================================================
	// OOP classes - AnchorPoint
	// click behaviour:
	// - click and drag: move point
	// - click (not drag) start/end point: go into draw mode extend line
	
	function AnchorPoint() {
		this.way=0;
		this.node=0;
	};
	AnchorPoint.prototype=new MovieClip();
	AnchorPoint.prototype.onPress=function() {
		if (this._name==0 && _root.map.ways[this.way].path.length==1) {
			// solo double-click - create new POI
			stopDrawing();
			_root.map.pois.attachMovie("poi",--newpoiid,++poidepth);
			_root.map.pois[newpoiid]._x=_root.map._xmouse;
			_root.map.pois[newpoiid]._y=_root.map._ymouse;
			_root.map.pois[newpoiid].select();
			_root.map.pois[newpoiid].clean=false;
		} else if (this._name==_root.drawpoint) {
			// double-click at end of route
			stopDrawing();
		} else if (Key.isDown(Key.SHIFT) && _root.map.ways[this.way].oldversion==0) {
			_root.junction=true;						// flag to prevent elastic band stopping on _this_ mouseUp
			startNewWay(_root.map.ways[this.way].path[this._name][0],
						_root.map.ways[this.way].path[this._name][1],this.node);
		} else {
//			_root.lastxmouse=_root._xmouse;
//			_root.lastymouse=_root._ymouse;
			_root.clicktime=new Date();
			this.beginDrag();
			_root.pointselected=this._name;
			_root.map.ways[this.way].highlight();
			setTypeText("Point",this.node);
			populatePropertyWindow('point');
			setTooltip("point selected\n(shift-click point to\nstart new line)",0);
		}
	};

	AnchorPoint.prototype.beginDrag=function() {
		this.onMouseMove=function() { this.trackDrag(); };
		this.onMouseUp  =function() { this.endDrag();   };
		_root.firstxmouse=_root.map._xmouse;
		_root.firstymouse=_root.map._ymouse;
	};

	AnchorPoint.prototype.trackDrag=function() {
		this._x=_root.map._xmouse;
		this._y=_root.map._ymouse;
	};
	
	AnchorPoint.prototype.endDrag=function() {
		this.onMouseMove=function() {};
		this.onMouseUp  =function() {};
		var newx=_root.map._xmouse;
		var newy=_root.map._ymouse;
		var t=new Date();
		var xdist=Math.abs(newx-_root.firstxmouse);
		var ydist=Math.abs(newy-_root.firstymouse);
		var longclick=(t.getTime()-_root.clicktime)>300;

		if ((xdist>=tolerance   || ydist>=tolerance  ) ||
		   ((xdist>=tolerance/2 || ydist>=tolerance/2) && longclick)) {
			// ====	Move existing point
			for (qway in _root.map.ways) {
				qdirty=0;
				for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
					if (_root.map.ways[qway].path[qs][2]==_root.map.ways[wayselected].path[this._name][2]) {
						_root.map.ways[qway].path[qs][0]=newx;
						_root.map.ways[qway].path[qs][1]=newy;
						qdirty=1;
					}
				}
				if (qdirty) { _root.map.ways[qway].redraw(); }
			}
			_root.map.ways[wayselected].highlightPoints(5000,"anchor");
			_root.map.ways[wayselected].highlight();
			_root.map.ways[wayselected].clean=false;

		} else {
			this._x=_root.map.ways[wayselected].path[this._name][0];	// Return point to original position
			this._y=_root.map.ways[wayselected].path[this._name][1];	//  | (in case dragged slightly)
			if ((this._name==0 || this._name==_root.map.ways[wayselected].path.length-1) && !Key.isDown(17)) {
				// ===== Clicked at start or end of line
				if (_root.drawpoint==0 || _root.drawpoint==_root.map.ways[wayselected].path.length-1) {
					// - Join looping path
					addEndPoint(_root.map.ways[wayselected].path[this._name][0],
								_root.map.ways[wayselected].path[this._name][1],
								_root.map.ways[wayselected].path[this._name][2]);
					stopDrawing();
				} else if (_root.drawpoint==-1) {
					// - Start elastic line for adding new point
					setTooltip("click to add point\ndouble-click/Return\nto end line",0);
					_root.drawpoint=this._name;
					this.startElastic();
				}
	
			} else {
				// ===== Clicked elsewhere in line
				if (_root.drawpoint>-1) {
					addEndPoint(_root.map.ways[wayselected].path[this._name][0],
								_root.map.ways[wayselected].path[this._name][1],
								_root.map.ways[wayselected].path[this._name][2]);
					_root.junction=true; restartElastic();
				}
			}
		}
	};

	AnchorPoint.prototype.startElastic=function() {
		this.onMouseMove=function() { this.trackElastic(); };
		this.onMouseUp  =function() { this.endElastic();   };
	};
	
	AnchorPoint.prototype.trackElastic=function() {
		_root.map.elastic.clear();
		_root.map.elastic.lineStyle(3,0x000000,100,false,"none");
		_root.map.elastic.moveTo(_root.map._xmouse,_root.map._ymouse);
		_root.map.elastic.lineTo(this._x,this._y);
	};
	
	AnchorPoint.prototype.endElastic=function() {
		if (_root.junction) { _root.junction=false; }
					   else { this.onMouseMove=function() {};
							  this.onMouseUp  =function() {}; }
	};

	function restartElastic() {
		if (_root.drawpoint!=-1) {
			_root.map.anchors[_root.drawpoint].startElastic();
			_root.map.anchors[_root.drawpoint].trackElastic();
		}
	}

	AnchorPoint.prototype.onRollOver=function() {
		if (_root.drawpoint>-1) {
			if (this._name==0 || this._name==_root.map.ways[this.way].path.length-1) {
				setPointer('penso');
			} else {
				setPointer('penx');
			}
		} else {
			setPointer('');
		}
	};

	Object.registerClass("anchor",AnchorPoint);



	// =====================================================================================
	// OOP classes - AnchorHint

	function AnchorHint() {
		this.way=0;
		this.node=0;
	};
	AnchorHint.prototype=new MovieClip();
	AnchorHint.prototype.onRollOver=function() {
		if (this._name==0 || this._name==_root.map.ways[this.way].path.length-1) {
			setTooltip("over endpoint\nclick to join\nshift-click to merge");
			setPointer('peno');
		} else {
			setTooltip("over point\nclick to join");
			setPointer('penx');
		}
	};
	AnchorHint.prototype.onRollOut=function() {
		clearTooltip();
	};

	AnchorHint.prototype.onPress=function() {
		if (_root.map.ways[this.way].oldversion>0) {
			_root.junction=true;
			restartElastic(); return;	// can't merge/join to historic ways
		}
		var i,z;
		if (Key.isDown(Key.SHIFT)) {
			// Merge ways
			if (this._name==0 || this._name==_root.map.ways[this.way].path.length-1) {
				_root.map.ways[wayselected].mergeWay(_root.drawpoint,_root.map.ways[this.way],this._name);
				_root.drawpoint=-1;
				_root.map.ways[wayselected].redraw();
//				_root.map.ways[wayselected].upload();
//				_root.map.ways[this.way].remove(wayselected);
				clearTooltip();
				_root.map.elastic.clear();
				_root.map.ways[wayselected].select();	// removes anchorhints, so must be last
			}
		} else { 
			// Join ways (i.e. junction)
			addEndPoint(this._x,this._y,this.node);
			_root.junction=true;						// flag to prevent elastic band stopping on _this_ mouseUp
			restartElastic();
		}
	};
	Object.registerClass("anchorhint",AnchorHint);


	// =====================================================================================
	// OOP classes - POI
	
	// ----	Initialise
	
	function POI() {
		this.attr=new Array();
		this.clean=true;
		this.uploading=false;
		this.locked=false;
		this._xscale=this._yscale=Math.max(100/Math.pow(2,_root.scale-13),6.25);
	};
	POI.prototype=new MovieClip();
	POI.prototype.remove=function() {
		if (this._name>=0 && !_root.sandbox) {
			poidelresponder = function() { };
			poidelresponder.onResult = function(result) {
				var code=result.shift(); if (code) { handleError(code,result); return; }
				if (poiselected==result[0]) { deselectAll(); }
				removeMovieClip(_root.map.pois[result[0]]);
			};
			remote.call('putpoi',poidelresponder,_root.usertoken,this._name,this._x,this._y,this.attr,0,baselong,basey,masterscale);
		} else {
			if (this._name==poiselected) { deselectAll(); }
			removeMovieClip(this);
		}
	};
	POI.prototype.reload=function() {
		poirelresponder=function() {};
		poirelresponder.onResult=function(result) {
			_root.map.pois[result[0]]._x  =result[1];
			_root.map.pois[result[0]]._y  =result[2];
			_root.map.pois[result[0]].attr=result[3];
			populatePropertyWindow('POI');
		};
		remote.call('getpoi',poirelresponder,this._name,baselong,basey,masterscale);
	};
	POI.prototype.upload=function() {
		poiresponder=function() { };
		poiresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var ni=result[1];	// new way ID
			if (result[0]!=ni) {
				_root.map.pois[result[0]]._name=ni;
				if (poiselected==result[0]) {
					poiselected=ni;
					if (_root.t_details.text==result[0]) { _root.t_details.text=ni; _root.t_details.setTextFormat(plainText); }
				}
			}
			_root.map.pois[ni].uploading=false;
		};
		if (!this.uploading && !this.locked && !_root.sandbox) {
			this.attr['created_by']=_root.signature;
			this.uploading=true;
			remote.call('putpoi',poiresponder,_root.usertoken,this._name,this._x,this._y,this.attr,1,baselong,basey,masterscale);
			this.clean=true;
		}
	};
	POI.prototype.onRollOver=function() {
		setPointer('');
	};
	POI.prototype.onPress=function() {
		if (_root.drawpoint>-1) {
			// add POI to way
			addEndPoint(this._x,this._y,this._name,this.attr);
			_root.junction=true; restartElastic();
			removeMovieClip(this);
		} else {
			// click POI
			if (_root.wayselected || _root.poiselected!=this._name) {
				stopDrawing(); uploadSelected(); deselectAll(); 
			}
			this.select();
			_root.clicktime=new Date();
			this.beginDrag();
		}
	};
	POI.prototype.beginDrag=function() {
		this.onMouseMove=function() { this.trackDrag(); };
		this.onMouseUp  =function() { this.endDrag();   };
		_root.firstxmouse=_root.map._xmouse;
		_root.firstymouse=_root.map._ymouse;
	};
	POI.prototype.trackDrag=function() {
		this._x=_root.map._xmouse;
		this._y=_root.map._ymouse;
	};
	POI.prototype.endDrag=function() {
		this.onMouseMove=function() {};
		this.onMouseUp  =function() {};
		var t=new Date();
		var xdist=Math.abs(_root.map._xmouse-_root.firstxmouse);
		var ydist=Math.abs(_root.map._ymouse-_root.firstymouse);
		var longclick=(t.getTime()-_root.clicktime)>300;

		if ((xdist>=tolerance   || ydist>=tolerance  ) ||
		   ((xdist>=tolerance/2 || ydist>=tolerance/2) && longclick)) {
			this.clean=false;
			this.select();
		}
	};
	POI.prototype.select=function() {
		_root.poiselected=this._name;
		setTypeText("Point",this._name);
		populatePropertyWindow('POI');
		highlightSquare(this._x,this._y,8/Math.pow(2,Math.min(_root.scale,16)-13));
	};
	// POI.prototype.recolour=function() { };
	// ** above will recolour as red/green depending on whether locked
	
	Object.registerClass("poi",POI);

	// =====================================================================================
	// OOP classes - OSMWay

	// ----	Initialise
	
	function OSMWay() {
		this.path=new Array();
		// path is an array of points
		// each point is an array: (x,y,node_id,0 move|1 draw,tag array,segment id)
		this.attr=new Array();
		this.clean=true;				// altered since last upload?
		this.uploading=false;			// currently uploading?
		this.locked=false;				// locked against upload?
		this.oldversion=0;				// is this an undeleted, not-uploaded way?
		this.mergedways=new Array();	// list of ways merged into this
		this.xmin=0;
		this.xmax=0;
		this.ymin=0;
		this.ymax=0;
	};
	OSMWay.prototype=new MovieClip();

	// ----	Load from remote server

	OSMWay.prototype.load=function(wayid) {
		responder = function() { };
		responder.onResult = function(result) {
			_root.waysreceived+=1;
			if (length(result[1])==0) { removeMovieClip(_root.map.ways[result[0]]); 
										removeMovieClip(_root.map.areas[result[0]]); return; }
			var i,id;
			_root.map.ways[result[0]].clean=true;
			_root.map.ways[result[0]].locked=false;
			_root.map.ways[result[0]].oldversion=0;
			_root.map.ways[result[0]].path=result[1];
			_root.map.ways[result[0]].attr=result[2];
			_root.map.ways[result[0]].xmin=result[3];
			_root.map.ways[result[0]].xmax=result[4];
			_root.map.ways[result[0]].ymin=result[5];
			_root.map.ways[result[0]].ymax=result[6];
			_root.map.ways[result[0]].redraw();
			_root.map.ways[result[0]].clearPOIs();
		};
		remote.call('getway',responder,this._name,wayid,baselong,basey,masterscale);
	};

	OSMWay.prototype.loadFromDeleted=function(wayid,version) {
		delresponder=function() { };
		delresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var i,z;
			_root.map.ways[result[0]].clean=false;
			_root.map.ways[result[0]].oldversion=result[7];
			z=result[1]; // assign negative IDs to anything moved
			for (i in z) {
				if (result[1][i][2]<0) { _root.newnodeid--; result[1][i][2]=newnodeid; }
			}
			_root.map.ways[result[0]].path=result[1];
			_root.map.ways[result[0]].attr=result[2];
			_root.map.ways[result[0]].xmin=result[3];
			_root.map.ways[result[0]].xmax=result[4];
			_root.map.ways[result[0]].ymin=result[5];
			_root.map.ways[result[0]].ymax=result[6];
			if (result[0]==wayselected) { _root.map.ways[result[0]].select(); }
								   else { _root.map.ways[result[0]].locked=true; }
			_root.map.ways[result[0]].redraw();
			_root.map.ways[result[0]].clearPOIs();
		};
		remote.call('getway_old',delresponder,this._name,wayid,version,baselong,basey,masterscale);
	};

	OSMWay.prototype.clearPOIs=function() {
		// check if any way nodes are POIs, delete the POIs if so
		var i,z;
		z=this.path;
		for (i in z) {
			if (_root.map.pois[this.path[i][2]]) { removeMovieClip(_root.map.pois[this.path[i][2]]); }
		}
	};

	// ----	Draw line

	OSMWay.prototype.redraw=function() {
		this.createEmptyMovieClip("line",1);					// clear line
		var linewidth=3; //Math.max(2/Math.pow(2,_root.scale-13),0)+1;
		var linealpha=100; // -50*(this.locked==true);
		
		// Set stroke

		if		(this.locked)					 { this.line.lineStyle(linewidth,0xFF0000,linealpha,false,"none"); }
		else if (colours[this.attr["highway"]])  { this.line.lineStyle(linewidth,colours[this.attr["highway" ]],linealpha,false,"none"); }
		else if (colours[this.attr["waterway"]]) { this.line.lineStyle(linewidth,colours[this.attr["waterway"]],linealpha,false,"none"); }
		else if (colours[this.attr["railway"]])  { this.line.lineStyle(linewidth,colours[this.attr["railway" ]],linealpha,false,"none"); }
		else {
			var c=0xAAAAAA; var z=this.attr;
			for (var i in z) { if (i!='created_by' && this.attr[i]!='' && this.attr[i].substr(0,6)!='(type ') { c=0x707070; } }
			this.line.lineStyle(linewidth,c,linealpha,false,"none");
		}
		
		// Draw fill/casing

		var f=-1; 
		if (this.path[this.path.length-1][0]==this.path[0][0] &&
			this.path[this.path.length-1][1]==this.path[0][1] &&
			this.path.length>2) {
			if (this.attr['area']) { f='0x777777'; }
			var z=this.attr;
			for (var i in z) { if (areas[i] && this.attr[i]!='' && this.attr[i]!='coastline') { f=areas[i]; } }
		}

		if ((f>-1 || casing[this.attr['highway']]) && !this.locked) {
			if (!_root.map.areas[this._name]) { _root.map.areas.createEmptyMovieClip(this._name,++areadepth); }
			with (_root.map.areas[this._name]) {
				clear();
				enabled=false;
				moveTo(this.path[0][0],this.path[0][1]); 
				if (f>-1) { beginFill(f,20); }
					 else { lineStyle(linewidth*1.5,0,100,false,"none"); }
				for (var i=1; i<this.path.length; i+=1) {
					lineTo(this.path[i][0],this.path[i][1]);
				}
				if (f>-1) { endFill(); }
			};
		} else if (_root.map.areas[this._name]) {
			removeMovieClip(_root.map.areas[this._name]);
		}

		// Draw line

		this.line.moveTo(this.path[0][0],this.path[0][1]); 
		for (var i=1; i<this.path.length; i+=1) {
			this.line.lineTo(this.path[i][0],this.path[i][1]);
		}
	};

	// ----	Show direction

	OSMWay.prototype.direction=function() {
		if (this.path.length<2) {
			_root.i_circular._visible=false;
			_root.i_direction._visible=true;
			_root.i_direction._alpha=50;
		} else {
			dx=this.path[this.path.length-1][0]-this.path[0][0];
			dy=this.path[this.path.length-1][1]-this.path[0][1];
			if (dx==0 && dy==0) {
				_root.i_circular._visible=true;
				_root.i_direction._visible=false;
			} else {
				_root.i_direction._rotation=180-Math.atan2(dx,dy)*(180/Math.PI)-45;
				_root.i_direction._alpha=100;
				_root.i_direction._visible=true;
				_root.i_circular._visible=false;
			}
		}
	};

	// ----	Remove from server
	
	OSMWay.prototype.remove=function() {
		this.deleteMergedWays();
		if (this._name>=0 && !_root.sandbox && this.oldversion==0) {
			deleteresponder = function() { };
			deleteresponder.onResult = function(result) {
				var code=result.shift(); if (code) { handleError(code,result); return; }
				if (wayselected==result[0]) { deselectAll(); }
				removeMovieClip(_root.map.ways[result[0]]);
				removeMovieClip(_root.map.areas[result[0]]);
			};
			remote.call('deleteway',deleteresponder,_root.usertoken,this._name);
		} else {
			if (this._name==wayselected) { stopDrawing(); deselectAll(); }
			removeMovieClip(_root.map.areas[this._name]);
			removeMovieClip(this);
		}
	};

	// ----	Upload to server
	
	OSMWay.prototype.upload=function() {
		putresponder=function() { };
		putresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var i,r,z,nw,qway,qs;
			nw=result[1];	// new way ID
			if (result[0]!=nw) {
				_root.map.ways[result[0]]._name=nw;
				if (_root.map.areas[result[0]]) { _root.map.areas[result[0]]._name=nw; }
				if (_root.t_details.text==result[0]) { _root.t_details.text=nw; _root.t_details.setTextFormat(plainText); }
				if (wayselected==result[0]) { wayselected=nw; }
			}
			_root.map.ways[nw].xmin=result[3];
			_root.map.ways[nw].xmax=result[4];
			_root.map.ways[nw].ymin=result[5];
			_root.map.ways[nw].ymax=result[6];
			_root.map.ways[nw].uploading=false;
			_root.map.ways[nw].oldversion=0;

			// check if renumbered nodes occur in any other ways
			for (qway in _root.map.ways) {
				for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
					if (result[2][_root.map.ways[qway].path[qs][2]]) {
						_root.map.ways[qway].path[qs][2]=result[2][_root.map.ways[qway].path[qs][2]];
					}
				}
			}
			_root.map.ways[nw].clearPOIs();
			_root.map.ways[nw].deleteMergedWays();
		};
		if (!this.uploading && !this.locked && !_root.sandbox && this.path.length>1) {
			this.attr['created_by']=_root.signature;
			this.uploading=true;
			remote.call('putway',putresponder,_root.usertoken,this._name,this.path,this.attr,this.oldversion,baselong,basey,masterscale);
			this.clean=true;
		}
	};

	// ---- Delete any ways merged into this one

	OSMWay.prototype.deleteMergedWays=function() {
		while (this.mergedways.length>0) {
			var i=this.mergedways.shift();
			_root.map.ways.attachMovie("way",i,++waydepth);	// can't remove unless the movieclip exists!
			_root.map.ways[i].remove();
		}
	};

	// ----	Revert to copy in database
	
	OSMWay.prototype.reload=function() {
		_root.waysrequested+=1;
		while (this.mergedways.length>0) {
			var i=this.mergedways.shift();
			_root.waysrequested+=1;
			_root.map.ways.attachMovie("way",i,++waydepth);
			_root.map.ways[i].load(i);
		}
		this.load(this._name);
	};

	// ----	Click handling	

	OSMWay.prototype.onRollOver=function() {
		if (this._name!=_root.wayselected && _root.drawpoint>-1) {
			this.highlightPoints(5001,"anchorhint");
			setPointer('penplus');
		} else if (_root.drawpoint>-1) { setPointer('penplus'); }
								  else { setPointer(''); }
	};
	
	OSMWay.prototype.onRollOut=function() {
		if (_root.wayselected) { setPointer(''   ); }
						  else { setPointer('pen'); }
		_root.map.anchorhints.removeMovieClip();
	};
	
	OSMWay.prototype.onPress=function() {
		if (Key.isDown(Key.SHIFT) && this._name==_root.wayselected && _root.drawpoint==-1) {
			// shift-click current way: insert point
			_root.pointselected=insertAnchorPoint(this._name);
			this.highlightPoints(5000,"anchor");
			_root.map.anchors[pointselected].beginDrag();
		} else if (Key.isDown(Key.SHIFT) && _root.wayselected && this.name!=_root.wayselected && _root.drawpoint==-1) {
			// shift-click other way: merge two ways
			var selstart =_root.map.ways[wayselected].path[0][2];
			var sellen   =_root.map.ways[wayselected].path.length-1;
			var selend   =_root.map.ways[wayselected].path[sellen][2];
			var thisstart=_root.map.ways[this._name ].path[0][2];
			var thislen  =_root.map.ways[this._name ].path.length-1;
			var thisend  =_root.map.ways[this._name ].path[thislen][2];
			if      (selstart==thisstart) { _root.map.ways[wayselected].mergeWay(0,_root.map.ways[this._name],0); }
			else if (selstart==thisend  ) { _root.map.ways[wayselected].mergeWay(0,_root.map.ways[this._name],thislen); }
			else if (selend  ==thisstart) { _root.map.ways[wayselected].mergeWay(sellen,_root.map.ways[this._name],0); }
			else if (selend  ==thisend  ) { _root.map.ways[wayselected].mergeWay(sellen,_root.map.ways[this._name],thislen); }
			else						  { return; }
			_root.map.ways[wayselected].redraw();
//			_root.map.ways[wayselected].upload();
//			_root.map.ways[this._name ].remove(wayselected);
			_root.map.ways[wayselected].select();
		} else if (_root.drawpoint>-1) {
			// click other way while drawing: insert point as junction
			if (this.oldversion==0) {
				if (this._name==_root.wayselected && _root.drawpoint>0) {
					_root.drawpoint+=1;	// inserting node earlier into the way currently being drawn
				}
				insertAnchorPoint(this._name);
				this.highlightPoints(5001,"anchorhint");
				addEndPoint(_root.map._xmouse,_root.map._ymouse,newnodeid);
			}
			_root.junction=true;
			restartElastic();
		} else {
			// click way: select
			this.select();
			clearTooltip();
		}
	};
	
	// ----	Select/highlight
	
	OSMWay.prototype.select=function() {
		if (_root.wayselected!=this._name || _root.poiselected!=0) { uploadSelected(); }
		_root.wayselected=this._name;
		_root.pointselected=-2;
		_root.poiselected=0;
		this.highlightPoints(5000,"anchor");
		removeMovieClip(_root.map.anchorhints);
		this.highlight();
		setTypeText("Way",this._name);
		populatePropertyWindow('way');
	};
	
	OSMWay.prototype.highlight=function() {
		_root.map.createEmptyMovieClip("highlight",5);
		if (_root.pointselected>-2) {
			highlightSquare(_root.map.anchors[pointselected]._x,_root.map.anchors[pointselected]._y,8/Math.pow(2,Math.min(_root.scale,17)-13));
		} else {
			var linewidth=11;
			var linecolour=0xFFFF00; if (this.locked) { var linecolour=0x00FFFF; }
			_root.map.highlight.lineStyle(linewidth,linecolour,80,false,"none");
			_root.map.highlight.moveTo(this.path[0][0],this.path[0][1]);
			for (var i=1; i<this.path.length; i+=1) {
				_root.map.highlight.lineTo(this.path[i][0],this.path[i][1]);
			}
		}
		this.direction();
	};

	OSMWay.prototype.highlightPoints=function(d,atype) {
		anchorsize=120/Math.pow(2,_root.scale-13);
		group=atype+"s";
		_root.map.createEmptyMovieClip(group,d);
		for (var i=0; i<this.path.length; i+=1) {
			_root.map[group].attachMovie(atype,i,i);
			_root.map[group][i]._x=this.path[i][0];
			_root.map[group][i]._y=this.path[i][1];
			_root.map[group][i]._xscale=anchorsize;
			_root.map[group][i]._yscale=anchorsize;
			_root.map[group][i].node=this.path[i][2];
			_root.map[group][i].way=this._name;
		}
	};

	// ----	Split, merge, reverse

	OSMWay.prototype.splitWay=function() {
		if (pointselected>0 && pointselected<(this.path.length-1) && this.oldversion==0) {
			_root.newwayid--;											// create new way
			_root.map.ways.attachMovie("way",newwayid,++waydepth);		//  |

			z=this.path;												// copy path array
			for (i in z) {												//  | (deep copy)
				_root.map.ways[newwayid].path[i]=new Array();			//  |
				for (j=0; j<=5; j+=1) { _root.map.ways[newwayid].path[i][j]=this.path[i][j]; }
			}															// | 

			z=this.attr; for (i in z) { _root.map.ways[newwayid].attr[i]=z[i]; }

			this.path.splice(Math.floor(pointselected)+1);				// current way
			this.redraw();												//  |

			_root.map.ways[newwayid].path.splice(0,pointselected);		// new way
			_root.map.ways[newwayid].redraw();							//  |
			_root.map.ways[newwayid].locked=this.locked;				//  |
			_root.map.ways[newwayid].upload();							//  |

			pointselected=-2;
			this.select();
			this.clean=false;
		};
	};

	//		Merge (start/end of this way,other way object,start/end of other way)
	// ** needs to not add duplicate points

	OSMWay.prototype.mergeWay=function(topos,otherway,frompos) {
		var i,z;
		if (this.oldversion>0 || otherway.oldversion>0) { return; }

		if (frompos==0) { for (i=0; i<otherway.path.length;    i+=1) { this.addPointFrom(topos,otherway,i); } }
				   else { for (i=otherway.path.length-1; i>=0; i-=1) { this.addPointFrom(topos,otherway,i); } }

		z=otherway.attr;
		for (i in z) {
			if (otherway.attr[i].substr(0,6)=='(type ') { otherway.attr[i]=null; }
			if (this.attr[i].substr(0,6)=='(type ') { this.attr[i]=null; }
			if (this.attr[i]!=null) {
				if (this.attr[i]!=otherway.attr[i] && otherway.attr[i]!=null) { this.attr[i]+='; '+otherway.attr[i]; }
			} else {
				this.attr[i]=otherway.attr[i];
			}
			if (!this.attr[i]) { delete this.attr[i]; }
		}

		this.mergedways.push(otherway._name);
		this.mergedways.concat(otherway.mergedways);
		this.clean=false;
		if (otherway.locked) { this.locked=true; }
		removeMovieClip(_root.map.areas[otherway._name]);
		removeMovieClip(otherway);
	};
	OSMWay.prototype.addPointFrom=function(topos,otherway,srcpt) {
		if (topos==0) { if (this.path[0					][2]==otherway.path[srcpt][2]) { return; } }	// don't add duplicate points
				 else { if (this.path[this.path.length-1][2]==otherway.path[srcpt][2]) { return; } }	//  |
		var newpoint=new Array(otherway.path[srcpt][0],
							   otherway.path[srcpt][1],
							   otherway.path[srcpt][2],null,
							   otherway.path[srcpt][4]);
		if (topos==0) { this.path.unshift(newpoint); }
			     else { this.path.push(newpoint); }
	};

	// ---- Reverse order
	
	OSMWay.prototype.reverseWay=function() {
		if (this.path.length<2) { return; }
		if (_root.drawpoint>-1) { _root.drawpoint=(this.path.length-1)-_root.drawpoint; }
		this.path.reverse();
		this.redraw();
		this.direction();
		this.select();
		this.clean=false;
	};



	Object.registerClass("way",OSMWay);

	// =====================================================================================
	// Support functions

	function writeText(obj,t) {
		with (obj) {
			text=t; wordWrap=true;
			setTextFormat(plainSmall);
			selectable=false; type='dynamic';
		}
	}

	function handleError(code,result) {
		var h=100;
		if (code==-1) { error=result[0]; }
				 else { error=result[0]+"\n\nPlease e-mail richard\@systemeD.net with a bug report, saying what you were doing at the time."; h+=50; }
		createModalDialogue(275,h,new Array('Ok'),null);
		_root.modal.box.createTextField("prompt",2,7,9,250,h-30);
		writeText(_root.modal.box.prompt,error);
	}

	function handleWarning() {
		createModalDialogue(275,130,new Array('Retry','Cancel'),handleWarningAction);
		_root.modal.box.createTextField("prompt",2,7,9,250,100);
		writeText(_root.modal.box.prompt,"Sorry - the connection to the OpenStreetMap server failed. Any recent changes have not been saved.\n\nWould you like to try again?");
	};

	function handleWarningAction(choice) {
		if (choice=='Retry') {
			// loop through all ways which are uploading, and reupload
			_root.i_warning._visible=false;
			for (qway in _root.map.ways) {
				if (_root.map.ways[qway].uploading) {
					_root.map.ways[qway].uploading=false;
					_root.map.ways[qway].upload();
				}
			}
		}
	};

	function keyDelete(doall) {
		if (_root.poiselected) {
			// delete POI
			_root.map.pois[poiselected].remove();
		} else if (_root.drawpoint>-1) {
			// delete most recently drawn point
			if (_root.drawpoint==0) { _root.map.ways[wayselected].path.shift(); }
							   else { _root.map.ways[wayselected].path.pop(); _root.drawpoint-=1; }
			if (_root.map.ways[wayselected].path.length) {
				_root.map.ways[wayselected].clean=false;
				_root.map.ways[wayselected].redraw();
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
				_root.map.ways[wayselected].highlight();
				restartElastic();
			} else {
				_root.map.anchors[_root.drawpoint].endElastic();
				_root.map.ways[wayselected].remove();
				_root.drawpoint=-1;
			}
		} else if (_root.pointselected>-2) {
			// delete selected point
			if (doall==1) {
				// remove node from all ways
				id=_root.map.ways[_root.wayselected].path[_root.pointselected][2];
				for (qway in _root.map.ways) {
					qdirty=0;
					for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
						if (_root.map.ways[qway].path[qs][2]==id) {
							_root.map.ways[qway].path.splice(qs,1);
							qdirty=1;
						}
					}
					if (qdirty && _root.map.ways[qway]["path"].length<2) {
						_root.map.ways[qway].remove();
					} else if (qdirty) {
						_root.map.ways[qway].redraw();
						_root.map.ways[qway].clean=false;
					}
				}
			} else {
				// remove node from this way only
				_root.map.ways[wayselected].path.splice(pointselected,1);
				if (_root.map.ways[wayselected].path.length<2) {
					_root.map.ways[wayselected].remove();
				} else {
					_root.map.ways[wayselected].redraw();
					_root.map.ways[wayselected].clean=false;
				}
			}
			_root.pointselected=-2;
			_root.drawpoint=-1;
			_root.map.elastic.clear();
			clearTooltip();
			if (_root.wayselected) {
				_root.map.ways[_root.wayselected].select();
			}
		}
	};

	function stopDrawing() {
		_root.map.anchors[_root.drawpoint].endElastic();
		_root.drawpoint=-1;
		if (_root.map.ways[wayselected].path.length<=1) { 
			// way not long enough, so abort
			removeMovieClip(_root.map.areas[wayselected]);
			removeMovieClip(_root.map.ways[wayselected]);
			removeMovieClip(_root.map.anchors);
		}
		_root.map.elastic.clear();
		clearTooltip();
	};

	function keyRevert() {
		if		(_root.wayselected<0) { stopDrawing();
										removeMovieClip(_root.map.areas[wayselected]);
										removeMovieClip(_root.map.ways[wayselected]); }
		else if	(_root.wayselected>0) {	stopDrawing();
										_root.map.ways[wayselected].reload(); }
		else if (_root.poiselected>0) { _root.map.pois[poiselected].reload(); }
		else if (_root.poiselected<0) { removeMovieClip(_root.map.pois[poiselected]); }
		deselectAll();
	};

	function deselectAll() {
		_root.map.createEmptyMovieClip("anchors",5000); 
		wayselected=0;
		poiselected=0;
		pointselected=-2;
		removeMovieClip(_root.map.highlight);
		_root.i_circular._visible=false;
		_root.i_direction._visible=true;
		_root.i_direction._alpha=50;
		clearTooltip();
		setTypeText("","");
		populatePropertyWindow('');
		_root.presetmenu._visible=false;
		_root.i_preset._visible=false;
		clearPropertyWindow();
	};
	
	function uploadSelected() {
		if (_root.wayselected!=0 && !_root.map.ways[wayselected].clean) {
			for (qway in _root.map.ways) {
				if (!_root.map.ways[qway].clean) {
					_root.map.ways[qway].upload();
				}
			}
		}
		if (_root.poiselected!=0 && !_root.map.pois[poiselected].clean) {
			_root.map.pois[poiselected].upload();
		}
	};

	function highlightSquare(sx,sy,ss) {
		_root.map.createEmptyMovieClip("highlight",5);
		_root.map.highlight._x=sx;
		_root.map.highlight._y=sy;
		_root.map.highlight.beginFill(0xFFFF00,80);
		_root.map.highlight.moveTo(-ss, ss);
		_root.map.highlight.lineTo( ss, ss);
		_root.map.highlight.lineTo( ss,-ss);
		_root.map.highlight.lineTo(-ss,-ss);
		_root.map.highlight.lineTo(-ss, ss);
		_root.map.highlight.endFill();
	};

	#include 'ui.as'




	// =====================================================================================
	// Start

	_root.attachMovie("menu","presetmenu",60);
	_root.presetmenu.init(141,505,1,presetnames['way'][presetselected],'Choose from a menu of preset attributes describing the way',setAttributesFromPreset,151);
	_root.presetmenu._visible=false;

	redrawMap(350-350*Math.pow(2,_root.scale-13),
			  250-250*Math.pow(2,_root.scale-13));
	setBackground(preferences.data.baselayer);
	whichWays();
	_root.onEnterFrame=function() { everyFrame(); };

	// Welcome buttons

	_root.createEmptyMovieClip("welcome",61);

	_root.welcome.createEmptyMovieClip("start",1);
	drawButton(_root.welcome.start,250,507,"Start","Start mapping with OpenStreetMap.");
	_root.welcome.start.onPress=function() { removeMovieClip( _root.welcome); };

	_root.welcome.createEmptyMovieClip("play",2);
	drawButton(_root.welcome.play,250,529,"Play","Practice mapping - your changes won't be saved.");
	_root.welcome.play.onPress=function() {
		_root.sandbox=true; removeMovieClip(_root.welcome);
		_root.createEmptyMovieClip("practice",62);
		with (_root.practice) {
			_x=603; _y=478; beginFill(0xFF0000,100);
			moveTo(0,0); lineTo(90,0); lineTo(90,17);
			lineTo(0,17); lineTo(0,0); endFill();
		};
		_root.practice.createTextField("btext",1,0,0,90,20);
		with (_root.practice.btext) {
			text="Practice mode";
			setTextFormat(boldWhite);
			selectable=false; type='dynamic';
		};
	};

	_root.welcome.createEmptyMovieClip("help",3);
	drawButton(_root.welcome.help,250,551,"Help","Find out how to use Potlatch, this map editor.");
	_root.welcome.help.onPress=function() { getUrl("http://wiki.openstreetmap.org/index.php/Potlatch","_blank"); };

	if (gpx) {
		_root.welcome.createEmptyMovieClip("convert",4);
		drawButton(_root.welcome.convert,250,573,"Track","Convert your GPS track to (locked) ways for editing.");
		_root.welcome.convert.onPress=function() { removeMovieClip(_root.welcome); gpxToWays(); };
	}


	// =====================================================================================
	// Options window
	
	function openOptionsWindow() {
		createModalDialogue(210,110,new Array('Ok'),function() { preferences.flush(); } );
		_root.modal.box.createTextField("prompt1",2,7,9,80,20);
		writeText(_root.modal.box.prompt1,"Background:");

		_root.modal.box.attachMovie("menu","background",6);
		_root.modal.box.background.init(87,10,preferences.data.baselayer,
			new Array("None","------------","Yahoo! satellite"),
			'Choose the background to display',setBackground,0);
// "OpenAerialMap","Yahoo! satellite","Osmarender","Mapnik"

		_root.modal.box.attachMovie("checkbox","pointer",5);
		_root.modal.box.pointer.init(10,40,"Fade background",preferences.data.dimbackground,function(n) { preferences.data.dimbackground=n; redrawBackground(); });

		_root.modal.box.attachMovie("checkbox","pointer",4);
		_root.modal.box.pointer.init(10,60,"Use pen and hand pointers",preferences.data.custompointer,function(n) { preferences.data.custompointer=n; });

	}
	
	function setBackground(n) {
		preferences.data.baselayer=n;
		preferences.flush();
		redrawBackground(); 
	}

	#include 'properties.as'

	// =====================================================================================
	// insertAnchorPoint		- add point into way with SHIFT-clicking
	
	function insertAnchorPoint(way) {
		var nx,ny,closest,closei,i,x1,y1,x2,y2,direct,via,newpoint;
		nx=_root.map._xmouse;	// where we're inserting it
		ny=_root.map._ymouse;	//	|
		closest=0.05; closei=0;
		for (i=0; i<(_root.map.ways[way].path.length)-1; i+=1) {
			x1=_root.map.ways[way].path[i][0];
			y1=_root.map.ways[way].path[i][1];
			x2=_root.map.ways[way].path[i+1][0];
			y2=_root.map.ways[way].path[i+1][1];
			direct=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			via   =Math.sqrt((nx-x1)*(nx-x1)+(ny-y1)*(ny-y1));
			via  +=Math.sqrt((nx-x2)*(nx-x2)+(ny-y2)*(ny-y2));
			if (Math.abs(via/direct-1)<closest) {
				closei=i+1;
				closest=Math.abs(via/direct-1);
			}
		}
		_root.newnodeid--;
		newpoint=new Array(nx,ny,newnodeid,1,new Array(),0);
		_root.map.ways[way].path.splice(closei,0,newpoint);
		_root.map.ways[way].clean=false;
		_root.map.ways[way].redraw();
		return closei;
	}

	// =====================================================================================

	// keyPressed				- key listener

	function keyPressed() {
		var k=Key.getCode();
		_root.lastkeypressed=k;

		switch (keytarget) {
			case 'keyname':	;
			case 'value':	if (_root.auto) { _root.auto.keyRespond(k); }
							else if (k==13) { autoEnter(); }
							return; break;
			case '':		break;
			default:		return; break;
		}

		if (k>48 && k<58 && (wayselected!=0 || poiselected!=0)) {
			if (presetnames[currentproptype][presetselected][k-48]!=null) {
				setAttributesFromPreset(k-48);
			}
		}
		switch (k) {
			case 46:		;													// DELETE/backspace - delete way -- ode
			case 8:			if (Key.isDown(Key.SHIFT)) {						//  |
								if (_root.wayselected!=0) { _root.map.ways[wayselected].remove(); }
							} else { keyDelete(1); }; break;					//  |
			case 13:		stopDrawing(); break;								// ENTER - stop drawing line
			case 27:		keyRevert(); break;									// ESCAPE - revert current way
			case 112:		setBackground(0); break; 							// f1 - no base layer
			case 113:		preferences.data.dimbackground=Key.isDown(Key.SHIFT); setBackground(2); break;	// f2 - Yahoo! base layer
			case 71:		loadGPS(); break;									// G - load GPS
			case 72:		if (_root.wayselected>0) { wayHistory(); }; break;	// H - way history
			case 82:		repeatAttributes(); break;							// R - repeat attributes
			case 85:		getDeleted(); break;								// U - undelete
			case 88:		_root.map.ways[wayselected].splitWay(); break;		// X - split way
			case Key.PGUP:	zoomIn(); break;									// Page Up - zoom in
			case Key.PGDN:	zoomOut(); break;									// Page Down - zoom out
			case Key.LEFT:  moveMap( 140,0); redrawBackground(); whichWays(); break;	// cursor keys
			case Key.RIGHT: moveMap(-140,0); redrawBackground(); whichWays(); break;	//  |
			case Key.DOWN:  moveMap(0,-100); redrawBackground(); whichWays(); break;	//  |
			case Key.UP:    moveMap(0, 100); redrawBackground(); whichWays(); break;	//  |
			case 167:		cyclePresetIcon(); break;							// '¤' - cycle presets
			case 187:		enterNewAttribute(); break;							// '+' - add new attribute
			case 189:		keyDelete(0); break;								// '-' - delete node from this way only
			case 76:		showPosition(); break;								// L - show latitude/longitude
			case 84:		showTileDebug(); break;								// T - show tile debug information
			// default:		_root.chat.text=Key.getCode()+" pressed";
		};
	}

	function showPosition() { setTooltip("lat "+Math.floor(coord2lat (_root.map._ymouse)*10000)/10000
									  +"\nlon "+Math.floor(coord2long(_root.map._xmouse)*10000)/10000,0); }
	function startCount()	{ z=new Date(); _root.startTime=z.getTime(); }
	function endCount(id)	{ z=new Date(); zz=Math.floor((z.getTime()-_root.startTime)*100);
							if (zz>100) { _root.chat.text+=id+":"+zz+";"; } }
	
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

		_root.chat.text+="\nx: "+(baselong+_root.map._x/_root.masterscale);
		_root.chat.text+="\ny: "+basey   +","+(250-_root.map._y);
		_root.chat.text+="\ny via Landsat projection: "+lat2y(centrelat(0));
//		_root.chat.text+="y via spherical Mercator: "+((Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2)*360;
//		_root.chat.text+="y via OpenLayers code: "+Math.log(Math.tan((90+lat)*Math.PI/360))/(Math.PI/180);
	}


	// =====================================================================================
	// World handling

	// redrawMap(x,y)
	// update all world parameters and call in tiles
	// based on user-selected location
	// ** could be substantially tidied

	function redrawMap(tx,ty) {
		_root.map._x=tx;
		_root.map._y=ty;
		var bscale=Math.pow(2,_root.scale-13);
		_root.map._xscale=100*bscale;
		_root.map._yscale=100*bscale;
		_root.coord_t=    -_root.map._y /bscale; _root.edge_t=coord2lat(_root.coord_t);
		_root.coord_b=(500-_root.map._y)/bscale; _root.edge_b=coord2lat(_root.coord_b);
		_root.coord_l=    -_root.map._x	/bscale; _root.edge_l=coord2long(_root.coord_l);
		_root.coord_r=(700-_root.map._x)/bscale; _root.edge_r=coord2long(_root.coord_r);
		getURL("javascript:updatelinks("+centrelong(0)+","+centrelat(0)+","+_root.scale+")");

		// ----	Trace
		//		x radius (lon) is 280/Math.pow(2,_root.scale)
		//		y radius (lat) is 280/Math.pow(2,_root.scale)
		
//		_root.coordmonitor.text ="Centre of map: lon "+centrelong()+", lat "+centrelat()+" -- ";
//		_root.coordmonitor.text+="Edges: lon "+_root.edge_l+"->"+_root.edge_r+" -- ";
//		_root.coordmonitor.text+="           lat "+_root.edge_b+"->"+_root.edge_t+" -- ";

	}

	function redrawBackground() {
		var alpha=100-50*preferences.data.dimbackground;
		switch (preferences.data.baselayer) {
			case 0: _root.yahoo._visible=false;	// none
					_root.map.tiles._visible=false;
					break;
			case 2: _root.yahoo._visible=true;	// Yahoo
					_root.yahoo._alpha=alpha;	
					_root.yahoo._x=0;
					_root.yahoo._y=0;
					_root.ylat=centrelat(_root.bgyoffset);
					_root.ylon=centrelong(_root.bgxoffset);
					_root.yzoom=18-_root.scale;
					_root.map.tiles._visible=false;
					break;
			case 1: ; // OpenAerialMap
			case 3: ; // Mapnik
			case 4: ; // Osmarender
					if (_root.tilesetloaded!=preferences.data.baselayer) {
						_root.tilesetloaded=preferences.data.baselayer;
						initTiles();
					}
					_root.map.tiles._visible=true;
					_root.yahoo._visible=false;
					break;
		}
	}

	// zoomIn, zoomOut, changeScaleTo - change scale functions
	
	function zoomIn()  {
		if (_root.scale<_root.maxscale) {
			blankTileQueue();
			changeScaleTo(_root.scale+1);
			if (_root.waycount>500) { purgeWays(); }
			if (_root.poicount>500) { purgePOIs(); }
			redrawMap((_root.map._x*2)-350,(_root.map._y*2)-250);
			redrawBackground();
			resizePOIs();
			for (qway in _root.map.ways) { _root.map.ways[qway].redraw(); }
			if (_root.wayselected) {
				_root.map.ways[wayselected].highlight();
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
			}
			restartElastic();
		}
	}

	function zoomOut() {
		if (_root.scale>_root.minscale) {
			blankTileQueue();
			changeScaleTo(_root.scale-1); 
			redrawMap((_root.map._x+350)/2,(_root.map._y+250)/2);
			redrawBackground();
			resizePOIs();
			whichWays();
			for (qway in _root.map.ways) { _root.map.ways[qway].redraw(); }
			if (_root.wayselected) {
				_root.map.ways[wayselected].highlight();
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
			}
			restartElastic();
		}
	}

	function changeScaleTo(newscale) {
		_root.map.tiles[_root.scale]._visible=false; _root.scale=newscale;
		_root.map.tiles[_root.scale]._visible=true;
		if (_root.scale==_root.minscale) { _root.i_zoomout._alpha=25;  }
									else { _root.i_zoomout._alpha=100; }
		if (_root.scale==_root.maxscale) { _root.i_zoomin._alpha =25;  }
									else { _root.i_zoomin._alpha =100; }
		_root.tolerance=4/Math.pow(2,_root.scale-13);
	}

	function resizePOIs() {
		var n=Math.max(100/Math.pow(2,_root.scale-13),6.25);
		for (var qpoi in _root.map.pois) {
			_root.map.pois[qpoi]._xscale=_root.map.pois[qpoi]._yscale=n;
		}
		if (_root.poiselected) {
			highlightSquare(_root.map.pois[poiselected]._x,_root.map.pois[poiselected]._y,8/Math.pow(2,Math.min(_root.scale,16)-13));
		}
	}

	#include 'tiles.as'

	// =====================================================================
	// Map support functions

	// everyFrame() - called onEnterFrame

	function everyFrame() {

		// ----	Fix Yahoo! peculiarities
		_root.yahoo.myMap.enableKeyboardShortcuts(false);
		_root.yahoo._visible=(preferences.data.baselayer==2);

		// ----	Do we need to redraw the property window? (workaround)
		if (_root.redopropertywindow) {
			_root.redopropertywindow=0;
			populatePropertyWindow(_root.currentproptype);
		}

		// ----	Control "loading ways" display
		_root.waysloading._visible=(_root.waysrequested!=_root.waysreceived) || (_root.whichrequested!=_root.whichreceived);
	}
	
	// mapRollOver/Out - user has rolled over/out the main map area
	
	function mapRollOver() {
		removeMovieClip(_root.map.anchorhints);
		if (_root.tooltip.text.substring(0,4)=='over') { clearTooltip(); }
		if (_root.drawpoint>-1)     { setPointer('pen'); }
		else if (_root.wayselected) { setPointer(''   ); }
						       else { setPointer('pen'); }
	}

	function mapRollOut() {
		if (_root.pointertype!='')  { setPointer(''   ); }
	}
	

	// addEndPoint - add point to start/end of line

	function addEndPoint(x,y,node,tags) {
		if (tags) {} else { tags=new Array(); }
		newpoint=new Array(x,y,node,0,tags,0);
		if (_root.drawpoint==_root.map.ways[wayselected].path.length-1) {
			_root.map.ways[wayselected].path.push(newpoint);
			_root.drawpoint=_root.map.ways[wayselected].path.length-1;
		} else {
			_root.map.ways[wayselected].path.unshift(newpoint);	// drawpoint=0, add to start
		}
	
		// Redraw map
		_root.map.ways[wayselected].clean=false;
		_root.map.ways[wayselected].redraw();
		_root.map.ways[wayselected].select();
		_root.map.elastic.clear();
	}


	// processMapDrag, moveMap - process map dragging

	function processMapDrag() {
		if (Math.abs(_root.firstxmouse-_root._xmouse)>(tolerance*4) ||
			Math.abs(_root.firstymouse-_root._ymouse)>(tolerance*4)) {
			if (_root.pointertype!='hand') { setPointer('hand'); }

			if (_root.yahoo._visible) {
				var t=new Date();
				if ((t.getTime()-yahootime.getTime())<500) {
					_root.yahoo._x+=Math.floor(_xmouse-lastxmouse); // less than 0.5s, so
					_root.yahoo._y+=Math.floor(_ymouse-lastymouse); // just move offset
				} else {
					redrawBackground();								// 0.5s elapsed, so
					_root.yahootime=new Date();						// request new tiles
				}
			}
			moveMap(Math.floor(_xmouse-lastxmouse),Math.floor(_ymouse-lastymouse));
		}
	}

	function endMapDrag() {
		_root.map.onMouseMove=function() {};
		_root.map.onMouseUp  =function() {};
		redrawBackground();
		restartElastic();
		if (Math.abs(_root.firstxmouse-_root._xmouse)>tolerance*4 &&
			Math.abs(_root.firstymouse-_root._ymouse)>tolerance*4) {
			whichWays();
		}
		_root.dragmap=false;
		if (_root.wayselected) { setPointer(''); }
						  else { setPointer('pen'); }
	}
	
	function moveMap(xdiff,ydiff) {
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		if (Key.isDown(Key.SPACE)) {
			_root.bgxoffset+=xdiff;
			_root.bgyoffset+=ydiff;
		} else {
			_root.map._x+=xdiff;
			_root.map._y+=ydiff;
			redrawMap(_root.map._x,_root.map._y);
		}
	}


	// mapClick - user has clicked within map area, so start drag
	
	function mapClick() {
		setPointer('pen');
		clearTooltip();
		_root.map.onMouseMove=function() { processMapDrag(); };
		_root.map.onMouseUp  =function() { endMapDrag(); };
		_root.dragmap=true;
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		_root.firstxmouse=_root._xmouse;
		_root.firstymouse=_root._ymouse;
		_root.clicktime=new Date();
		_root.yahootime=new Date();
	}

	// mapClickEnd - end of click within map area

	function mapClickEnd() {

		// Clicked on map without dragging

		if (Math.abs(_root.firstxmouse-_root._xmouse)<(tolerance*4) &&
			Math.abs(_root.firstymouse-_root._ymouse)<(tolerance*4)) {
			_root.dragmap=false;
			// Adding a point to the way being drawn
			if (_root.drawpoint>-1) {
				_root.newnodeid--;
				if (_root.pointselected>-2) {
					setTypeText("Way",_root.wayselected);
					populatePropertyWindow('way');
				}
				addEndPoint(_root.map._xmouse,_root.map._ymouse,newnodeid);
				restartElastic();

			// Deselecting a way
			} else if (_root.wayselected) {
				uploadSelected(); deselectAll();

			// Deselecting a POI
			} else if (_root.poiselected) {
				uploadSelected(); deselectAll();

			// Starting a new way
			} else {
				_root.newnodeid--; startNewWay(_root.map._xmouse,_root.map._ymouse,_root.newnodeid);
			}
		}
	}




	// startNewWay	- create new way with first point x,y,node

	function startNewWay(x,y,node) {
		uploadSelected();
		_root.newwayid--;
		newpoint=new Array(x,y,node,0,new Array(),0);
		_root.wayselected=newwayid;
		_root.poiselected=0;
		_root.map.ways.attachMovie("way",newwayid,++waydepth);
		_root.map.ways[newwayid].path[0]=newpoint;
		_root.map.ways[newwayid].redraw();
		_root.map.ways[newwayid].select();
		_root.map.ways[newwayid].clean=false;
		_root.map.anchors[0].startElastic();
		_root.drawpoint=0;
		setTooltip("click to add point\ndouble-click/Return\nto end line",0);
	}

	// =====================================================================
	// Tooltip functions

	function setTooltip(txt,delay) {
		_root.tooltip.text=txt;
		_root.tooltip.setTextFormat(plainSmall);
		_root.createEmptyMovieClip('ttbackground',45,580,25,150,100);
// draw a white box at the relevant size, _alpha=50
// _root.ttbackground.color=0xFFFFFF;
// _root.ttbackground._alpha=50;
	}

	function clearTooltip() {
		_root.tooltip.text='';
		removeMovieClip(_root.ttbackground);
	}
	
	function setFloater(txt) {
		with (_root.floater) {
			text=txt;
			setTextFormat(plainSmall); 
			_width=textWidth+4;

			if (_root._ymouse<575          ) { _y=_root._ymouse+4;			 }
									    else { _y=_root._ymouse-16; 	     }
			if (_root._xmouse+textWidth<700) { _x=_root._xmouse+4;			 }
									    else { _x=_root._xmouse-textWidth-8; }
			_visible=false;
		}
		clearInterval(floaterID);
		floaterID=setInterval(unveilFloater,1000);
	}

	function unveilFloater(txt) {
		_root.floater._visible=true;
		clearInterval(floaterID);
	}
	function clearFloater(txt) {
		clearInterval(floaterID);
		_root.floater._visible=false;
	}

	// =====================================================================
	// Co-ordinate conversion
	
	// lat/long <-> coord conversion

	function lat2coord(a)	{ return -(lat2y(a)-basey)*_root.masterscale+250; }
	function coord2lat(a)	{ return y2lat((a-250)/-_root.masterscale+basey); }
	function long2coord(a)	{ return (a-baselong)*_root.masterscale+350; }
	function coord2long(a)	{ return (a-350)/_root.masterscale+baselong; }

	// y2lat			- converts Y co-ordinate (Mercator) to latitude
	// lat2y			- converts latitude to Y co-ordinate (Mercator)

	function y2lat(a) { return 180/Math.PI * (2 * Math.atan(Math.exp(a*Math.PI/180)) - Math.PI/2); }
	function lat2y(a) { return 180/Math.PI * Math.log(Math.tan(Math.PI/4+a*(Math.PI/180)/2)); }

	// get centre points

	function centrelat(o)  { return  coord2lat((250-_root.map._y-o)/Math.pow(2,_root.scale-13)); }
	function centrelong(o) { return coord2long((350-_root.map._x-o)/Math.pow(2,_root.scale-13)); }

	// tile filenames

	function getTileNumber(lat,lon,z) {
		xtile=Math.floor((lon+180)/360*Math.pow(2,z)) ;
		ytile=Math.floor((1-Math.log(Math.tan(lat*Math.PI/180) + 1/Math.cos(lat*Math.PI/180))/Math.PI)/2 *Math.pow(2,z));
	}



	#include 'gps.as'

	// =====================================================================================
	// History functions
	// wayHistory - show dialogue for previous versions of the way
	//				(calling handleRevert to actually initiate the revert)
	// getDeleted - load all deleted ways (like whichways), but locked
	
	function wayHistory() {
		historyresponder = function() { };
		historyresponder.onResult = function(result) {
			createModalDialogue(275,90,new Array('Revert','Cancel'),handleRevert);
			_root.modal.box.createTextField("prompt",2,7,9,250,100);
			writeText(_root.modal.box.prompt,"Revert to an earlier saved version:");

			var versionlist=new Array();
			_root.versionnums=new Array();
			for (i=0; i<result[0].length; i+=1) {
				versionlist.push(result[0][i][1]+' ('+result[0][i][3]+')');
				versionnums[i]=result[0][i][0];
			}
			_root.modal.box.attachMovie("menu","version",6);
			_root.modal.box.version.init(9,32,0,versionlist,
				'Choose the version to revert to',
				function(n) { _root.revertversion=versionnums[n]; },0);
			_root.revertversion=versionnums[0];
		};
		remote.call('getway_history',historyresponder,_root.wayselected);
	};
	function handleRevert(choice) {
		if (choice=='Cancel') { return; }
		_root.map.ways[_root.wayselected].loadFromDeleted(_root.wayselected,_root.revertversion);
	};
	function getDeleted() {
		whichdelresponder=function() {};
		whichdelresponder.onResult=function(result) {
			waylist=result[0];
			for (i in waylist) {										// ways
				way=waylist[i];											//  |
				if (!_root.map.ways[way]) {								//  |
					_root.map.ways.attachMovie("way",way,++waydepth);	//  |
					_root.map.ways[way].loadFromDeleted(way,-1);		//  |
					_root.waycount+=1;									//  |
				}
			}
		};
		remote.call('whichways_deleted',whichdelresponder,_root.edge_l,_root.edge_b,_root.edge_r,_root.edge_t,baselong,basey,masterscale);
	};


	// ================================================================
	// Way communication
	
	// whichWays	- get list of ways from remoting server

	function whichWays() {
		if (_root.waycount>500) { purgeWays(); }
		if (_root.poicount>500) { purgePOIs(); }
		if (_root.edge_l>_root.bigedge_l &&
			_root.edge_r<_root.bigedge_r &&
			_root.edge_b>_root.bigedge_b &&
			_root.edge_t<_root.bigedge_t) {
			// we have already loaded this area, so ignore
		} else {
			whichresponder=function() {};
			whichresponder.onResult=function(result) {
				_root.whichreceived+=1;
				waylist  =result[0];
				pointlist=result[1];

				for (i in waylist) {										// ways
					way=waylist[i];											//  |
					if (!_root.map.ways[way]) {								//  |
						_root.map.ways.attachMovie("way",way,++waydepth);	//  |
						_root.map.ways[way].load(way);						//  |
						_root.waycount+=1;									//  |
						_root.waysrequested+=1;								//  |
					}
				}
				
				for (i in pointlist) {										// POIs
					point=pointlist[i][0];									//  |
					if (!_root["map"]["pois"][point]) {						//  |
						_root.map.pois.attachMovie("poi",point,++poidepth);	//  |
						_root.map.pois[point]._x=pointlist[i][1];			//  |
						_root.map.pois[point]._y=pointlist[i][2];			//  |
						_root.map.pois[point]._xscale=_root.map.pois[point]._yscale=Math.max(100/Math.pow(2,_root.scale-13),6.25);
						_root.map.pois[point].attr=pointlist[i][3];			//  |
						_root.poicount+=1;									//  |
					}
				}
			};
			remote.call('whichways',whichresponder,_root.edge_l,_root.edge_b,_root.edge_r,_root.edge_t,baselong,basey,masterscale);
			_root.bigedge_l=_root.edge_l; _root.bigedge_r=_root.edge_r;
			_root.bigedge_b=_root.edge_b; _root.bigedge_t=_root.edge_t;
			_root.whichrequested+=1;
		}
	}

	// purgeWays/purgePOIs - remove any clean ways/POIs outside current view
	
	function purgeWays() {
		for (qway in _root.map.ways) {
			if (qway==_root.wayselected) {
			} else if (!_root.map.ways[qway].clean) {
				_root.map.ways[qway].upload();
			} else if (((_root.map.ways[qway].xmin<edge_l && _root.map.ways[qway].xmax<edge_l) ||
						(_root.map.ways[qway].xmin>edge_r && _root.map.ways[qway].xmax>edge_r) ||
					    (_root.map.ways[qway].ymin<edge_b && _root.map.ways[qway].ymax<edge_b) ||
						(_root.map.ways[qway].ymin>edge_t && _root.map.ways[qway].ymax>edge_t))) {
				removeMovieClip(_root.map.ways[qway]);
				removeMovieClip(_root.map.areas[qway]);
				_root.waycount-=1;
			}
		}
		_root.bigedge_l=_root.edge_l; _root.bigedge_r=_root.edge_r;
		_root.bigedge_b=_root.edge_b; _root.bigedge_t=_root.edge_t;
	}

	function purgePOIs() {
		for (qpoi in _root.map.pois) {
			if (qpoi==_root.poiselected) {
			} else if (!_root.map.pois[qpoi].clean) {
				_root.map.pois[qpoi].upload();
			} else if ((_root.map.pois[qpoi]._x<coord_l || _root.map.pois[qpoi]._x>coord_r) &&
					   (_root.map.pois[qpoi]._y<coord_b || _root.map.pois[qpoi]._y>coord_t)) {
				removeMovieClip(_root.map.pois[qpoi]);
				_root.poicount-=1;
			}
		}
	}
	
	// ================================================================
	// Pointer handling
	
	function setPointer(ptype) {
		if (_root.pointertype==ptype) { return; }
		_root.pointers[_root.pointertype]._visible=false;
		if ((ptype) && preferences.data.custompointer) {
			_root.pointers[ptype]._x=_root._xmouse;
			_root.pointers[ptype]._y=_root._ymouse;
			_root.pointers[ptype].startDrag(true);
			_root.pointers[ptype]._visible=true;
			Mouse.hide();
		} else {
			_root.pointers[_root.pointertype].stopDrag();
			Mouse.show();
		}
		_root.pointertype=ptype;
	}
