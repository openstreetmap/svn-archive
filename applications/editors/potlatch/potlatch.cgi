#!/usr/bin/perl -w

	# ----------------------------------------------------------------
	# potlatch.cgi
	# Flash editor for Openstreetmap

	# editions Systeme D / Richard Fairhurst 2006-7
	# public domain

	# last update 10.11.2007 (revert/undo)

	# You may do what you like with this file, but please think very
	# carefully before adding dependencies or complicating the user
	# interface. Thank you!
	# ----------------------------------------------------------------

	use SWF qw(:ALL);
	use SWF::Constants qw(:Button);

	# -----	Initialise

	SWF::setScale(20.0);
	SWF::useSWFVersion(8);

	$m = new SWF::Movie();
	$m->setDimension(700, 600);
	$m->setRate(50);
	$m->setBackground(0xFF,0xFF,0xFF);

	require "potlatch_assets.pl";

	# -----	ActionScript

	$actionscript=<<EOF;

	// =====================================================================================
	// Initialise

	// Site-specific URLs
//	var apiurl='rubyamf.cgi';
//	var gpsurl='/potlatch/getgps.cgi';
//	var gpxurl='http://127.0.0.1/~richard/gpx/';
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
	var minscale=11;				// don't zoom out past this
	var maxscale=18;				// don't zoom in past this
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
	var tolerance=4/Math.pow(2,_root.scale-12);
	var bigedge_l=999999; var bigedge_r=-999999; // area of largest whichways
	var bigedge_b=999999; var bigedge_t=-999999; //  |
	var sandbox=false;				// we're doing proper editing
	var signature="Potlatch 0.5d";	// current version
	if (preferences.data.baselayer    ==undefined) { preferences.data.baselayer    =2; }	// show Yahoo?
	if (preferences.data.custompointer==undefined) { preferences.data.custompointer=true; }	// use custom pointers?
	
	// Way styling
	// ** should be moved into presets file
	// ** area colours need to be value-specific (not just key-specific)
	colours=new Array();
	colours["motorway"		]=0x809BC0; colours["motorway_link"	]=0x809BC0;	// 0x3366CC
	colours["trunk"			]=0x7FC97F;	colours["trunk_link"	]=0x7FC97F; // 0x007700
	colours["primary"		]=0xE46D71;	colours["primary_link"	]=0xE46D71; // 0x770000
	colours["secondary"		]=0xFDBF6F; // 0xCC6600
	colours["tertiary"		]=0xFEFECB; // blank
	colours["unclassified"	]=0xE8E8E8; colours["residential"	]=0xE8E8E8; // blank
	colours["footway"		]=0xFF6644;	
	colours["cycleway"		]=0xFF6644;	
	colours["bridleway"		]=0xFF6644;	
	colours["rail"			]=0x000001;	
	colours["river"			]=0x8888FF;	
	colours["canal"			]=0x8888FF;	
	colours["stream"		]=0x8888FF;	
	
	casing=new Array();
	casing['motorway'   ]=1; casing['motorway_link']=1;
	casing['trunk'		]=1; casing['trunk_link'   ]=1;
	casing['primary'	]=1; casing['primary_link' ]=1;
	casing['secondary'	]=1; casing['tertiary'     ]=1;
	casing['residential']=1; casing['unclassified' ]=1;

	areas=new Array();
	areas['leisure' ]=0x8CD6B5;
	areas['amenity' ]=0xADCEB5;
	areas['shop'    ]=0xADCEB5;
	areas['tourism' ]=0xF7CECE;
	areas['historic']=0xF7F7DE;
	areas['ruins'   ]=0xF7F7DE;
	areas['landuse' ]=0x444444;
	areas['military']=0xD6D6D6;
	areas['natural' ]=0xADD6A5;
	areas['sport'   ]=0x8CD6B5;

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
	_root.i_newattr.onPress   =function() { enterNewAttribute(); };
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
		if (_root.map.ways[wayselected].path.length>200) {
			setTooltip("too long to unlock:\nplease split into\nshorter ways");
		} else {
			_root.map.ways[wayselected].locked=false;
			_root.map.ways[wayselected].clean=false;
			_root.map.ways[wayselected].redraw();
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
	boldText  =new TextFormat(); boldText.color  =0x000000; boldText.size  =14; boldText.font  ="_sans"; boldText.bold =true;	// italics and bold are wrong way round!
	boldSmall =new TextFormat(); boldSmall.color =0x000000; boldSmall.size =12; boldSmall.font ="_sans"; boldSmall.bold=true;	//  |
	boldWhite =new TextFormat(); boldWhite.color =0xFFFFFF; boldWhite.size =12; boldWhite.font ="_sans"; boldWhite.bold=true;	//  |
	menu_on	  =new TextFormat(); menu_on.color   =0x000000; menu_on.size   =12; menu_on.font   ="_sans"; menu_on.bold  =true;	//  |
	menu_off  =new TextFormat(); menu_off.color  =0xFFFFFF; menu_off.size  =12; menu_off.font  ="_sans"; menu_off.bold =true;	//  |

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
		this._xscale=this._yscale=Math.max(100/Math.pow(2,_root.scale-12),6.25);
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
			this.beginDrag();
		}
	};
	POI.prototype.beginDrag=function() {
		this.onMouseMove=function() { this.trackDrag(); };
		this.onMouseUp  =function() { this.endDrag();   };
		_root.firstxmouse=_root._xmouse;
		_root.firstymouse=_root._ymouse;
	};
	POI.prototype.trackDrag=function() {
		this._x=_root.map._xmouse; // _root.lastxmouse=_root._xmouse;
		this._y=_root.map._ymouse; // _root.lastymouse=_root._ymouse;
	};
	POI.prototype.endDrag=function() {
		this.onMouseMove=function() {};
		this.onMouseUp  =function() {};
		var t=new Date();
		var xdist=Math.abs(_root._xmouse-_root.firstxmouse);
		var ydist=Math.abs(_root._xmouse-_root.firstymouse);
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
		highlightSquare(this._x,this._y,8/Math.pow(2,Math.min(_root.scale,16)-12));
	};
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
			if (length(result[1])==0) { removeMovieClip(_root.map.ways[result[0]]); return; }
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
		var linewidth=3; //Math.max(2/Math.pow(2,_root.scale-12),0)+1;
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

		if (f>-1 || casing[this.attr['highway']]) {
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
			highlightSquare(_root.map.anchors[pointselected]._x,_root.map.anchors[pointselected]._y,8/Math.pow(2,Math.min(_root.scale,17)-12));
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
		anchorsize=120/Math.pow(2,_root.scale-12);
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

	// =====================================================================================
	// Standard UI

	// Checkboxes
	// UICheckbox.init(x,y,text,state,changefunction)

	UICheckbox=function() {
	};
	UICheckbox.prototype=new MovieClip();
	UICheckbox.prototype.init=function(x,y,prompttext,state,changefunction) {
		this._x=x;
		this._y=y;
		this.state=state;
		this.doOnChange=changefunction;
		this.createTextField('prompt',0,13,-5,200,19);
		this.prompt.text=prompttext;
		this.prompt.setTextFormat(plainSmall);
		tw=this.prompt._width=this.prompt.textWidth+5;

		this.createEmptyMovieClip('hitregion',1);
		with (this.hitregion) {
			clear(); beginFill(0,0);
			moveTo(0,0); lineTo(tw+15,0);
			lineTo(tw+15,15); lineTo(0,15);
			endFill();
 		};
		this.hitregion.onPress=function() {
			this._parent.state=!this._parent.state;
			this._parent.draw();
			this._parent.doOnChange(this._parent.state);
		};

		this.createEmptyMovieClip('box',2);
		this.draw();
	};
	UICheckbox.prototype.draw=function() {
		with (this.box) {
			clear();
			lineStyle(2,0,100);
			moveTo(1,0);
			lineTo(9,0); lineTo(9,9);
			lineTo(0,9); lineTo(0,0);
			if (this.state==true) {
				lineStyle(2,0,100);
				moveTo(1,1); lineTo(8,8);
				moveTo(8,1); lineTo(1,8);
			}
		}
	};
	Object.registerClass("checkbox",UICheckbox);

	// Pop-up menu
	// UIMenu.init(x,y,selected option,array of options,tooltip,
	//			   function to call on close,width)
	
	function UIMenu() {
	};
	UIMenu.prototype=new MovieClip();
	UIMenu.prototype.init=function(x,y,selected,options,tooltip,closefunction,menuwidth) {
		var i,w,h;
		this._x=x; this._y=y;
		this.selected=selected; this.original=selected;
		this.options=options;

		// create (invisible) movieclip for opened menu
		this.createEmptyMovieClip("opened",2);
		this.opened._visible=false;
		// create child for each option
		this.tw=0;
		for (i=0; i<options.length; i+=1) {
			this.opened.createTextField(i,i+1,3,i*16-1,100,19);
			this.opened[i].text=options[i];
			this.opened[i].background=true;
			this.opened[i].backgroundColor=0x888888;
			this.opened[i].setTextFormat(menu_off);
			if (this.opened[i].textWidth*1.05>this.tw) { this.tw=this.opened[i].textWidth*1.05; }
		};
		for (i=0; i<options.length; i+=1) {
			this.opened[i]._width=this.tw;
		}
		// create box around menu
		this.opened.createEmptyMovieClip("box",0);
		w=this.tw+7;
		h=options.length*16+5;
		with (this.opened.box) {
			_y=-2;
			clear();
			beginFill(0x888888,100);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		};

		// create (visible) movieclip for closed menu
		if (menuwidth>0) { w=menuwidth; } else { w+=11; }
		this.createEmptyMovieClip("closed",1);
		this.closed.createEmptyMovieClip("box",0);
		with (this.closed.box) {
			clear();
			beginFill(0x888888,100);
			lineTo(w,0 ); lineTo(w,17);
			lineTo(0,17); lineTo(0,0 ); endFill();
			beginFill(0xFFFFFF,100);
			moveTo(w-11,7); lineTo(w-3,7);
			lineTo(w-7,13); lineTo(w-11,7); endFill();
		};
		this.closed.createTextField("current",2,3,-1,this.tw,19);
		this.closed.current.text=options[selected];
		this.closed.current.setTextFormat(menu_off);
		this.closed.current.background=false;
//		this.closed.current.backgroundColor=0x888888;

		this.onPress=function() { clearFloater(); this.openMenu(); };
		this.onRelease=function() { this.closeMenu(); };
		this.onReleaseOutside=function() { this.closeMenu(); };
		this.onMouseMove=function() { this.trackMenu(); };
		this.doOnClose=closefunction;
		this.opened[this.selected].backgroundColor=0xDDDDDD;
		this.opened[this.selected].setTextFormat(menu_on);

		if (tooltip!='') {
			this.onRollOver=function() { setFloater(tooltip); };
			this.onRollOut =function() { clearFloater(); };
		}
	};
	UIMenu.prototype.trackMenu=function() {
		if (this.opened._visible) {
			this.opened[this.selected].backgroundColor=0x888888;
			this.opened[this.selected].setTextFormat(menu_off);
			this.selected=this.whichSelection();
			this.opened[this.selected].backgroundColor=0xDDDDDD;
			this.opened[this.selected].setTextFormat(menu_on);
		}
	};
	UIMenu.prototype.openMenu=function() {
		this.closed._alpha=50;
		this.opened._visible=true;
		this.opened._y=-15*this.selected;

		t=new Object(); t.x=0; t.y=this.opened._height;
		this.opened.localToGlobal(t);
		while (t.y>uheight) { this.opened._y-=15; t.y-=15; }
		this.trackMenu();
	};
	UIMenu.prototype.closeMenu=function() {
		if (this.selected>-1) {
			this.original=this.selected;
			this.closed.current.text=this.options[this.selected];
			this.closed._alpha=100;
			this.opened._visible=false;
			mflash=this; flashcount=2;
			mflashid=setInterval(function() { mflash.menuFlash(); }, 40);
			this.doOnClose(this.selected);
		} else {
			this.closed.current.text=this.options[this.original];
			this.closed.current.setTextFormat(menu_off);
			this.closed._alpha=100;
			this.opened._visible=false;
		}
	};
	UIMenu.prototype.setValue=function(n) {
		this.opened[this.selected].backgroundColor=0x888888;
		this.opened[this.selected].setTextFormat(menu_off);
		this.selected=n; this.original=n;
		this.closed.current.text=this.options[this.selected];
		this.closed.current.setTextFormat(menu_off);
	};
	UIMenu.prototype.whichSelection=function() {
		mpos=new Object();
		mpos.x=_root._xmouse;
		mpos.y=_root._ymouse;
		this.opened.globalToLocal(mpos);
		if (mpos.x>0 && mpos.x<this.tw && mpos.y>3 && mpos.y<this.options.length*15+3) {
			return Math.floor((mpos.y-1)/15);
		}
		return -1;
	};
	UIMenu.prototype.menuFlash=function() {
		// ** flashcount and mflashid are globals, and really shouldn't be
		flashcount-=1; if (flashcount==0) { clearInterval(mflashid); };
		if (flashcount/2!=Math.floor(flashcount/2)) {
			this.closed.current.backgroundColor=0xDDDDDD;
			this.closed.current.setTextFormat(menu_on);
		} else {
			this.closed.current.backgroundColor=0x888888;
			this.closed.current.setTextFormat(menu_off);
		}
		updateAfterEvent();
	};

	Object.registerClass("menu",UIMenu);
	
	// modalDialogue

	function createModalDialogue(w,h,buttons,closefunction) {
		clearFloater();
		_root.createEmptyMovieClip("modal",0xFFFFFE);
		var ox=(uwidth-w)/2; var oy=(uheight-100-h)/2;	// -100 for visual appeal

		// Blank all other areas
		_root.modal.createEmptyMovieClip("blank",1);
		with (_root.modal.blank) {
			beginFill(0xFFFFFF,0); moveTo(0,0); lineTo(uwidth,0);
			lineTo(uwidth,uheight); lineTo(0,uheight); lineTo(0,0); endFill();
		}
		_root.modal.blank.onPress=function() {};
		_root.modal.blank.useHandCursor=false;
		Key.removeListener(keyListener);

		// Create dialogue box
		_root.modal.createEmptyMovieClip("box",2);
		with (_root.modal.box) {
			_x=ox; _y=oy;
			beginFill(0xBBBBBB,100);
			moveTo(0,0);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		}

		// Create buttons
		for (var i=0; i<buttons.length; i+=1) {
			_root.modal.box.createEmptyMovieClip(i,i*2+1);
			drawButton(_root.modal.box[i],w-60*(buttons.length-i),h-30,buttons[i],"");
			_root.modal.box[i].onPress=function() {
				if (closefunction) { closefunction(buttons[this._name]); }
				clearModalDialogue();
			};
			_root.modal.box[i].useHandCursor=true;
		}
	}

	function clearModalDialogue() {
		Key.addListener(keyListener);
		_root.createEmptyMovieClip("modal",0xFFFFFE);
	}


	// drawButton		- draw white-on-grey button
	// (object,x,y,button text, text to right)

	function drawButton(buttonobject,x,y,btext,ltext) {
		with (buttonobject) {
			_x=x; _y=y;
			beginFill(0x7F7F7F,100);
			moveTo(0,0);
			lineTo(50,0); lineTo(50,17);
			lineTo(0,17); lineTo(0,0); endFill();
		}
		buttonobject.useHandCursor=true;
		buttonobject.createTextField('btext',1,0,-1,48,20);
		with (buttonobject.btext) {
			text=btext; setTextFormat(boldWhite);
			selectable=false; type='dynamic';
			_x=(45-textWidth)/2;
		}
		if (ltext!="") {
			buttonobject.createTextField("explain",2,54,-1,300,20);
			writeText(buttonobject.explain,ltext);
		}
	}



	// =====================================================================================
	// Start

	_root.attachMovie("menu","presetmenu",60);
	_root.presetmenu.init(141,505,1,presetnames['way'][presetselected],'Choose from a menu of preset attributes describing the way',setAttributesFromPreset,151);
	_root.presetmenu._visible=false;

	redrawMap(350-350*Math.pow(2,_root.scale-12),
			  250-250*Math.pow(2,_root.scale-12));
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
		createModalDialogue(275,90,new Array('Ok'),null);
		_root.modal.box.createTextField("prompt1",2,7,9,80,20);
		writeText(_root.modal.box.prompt1,"Background:");

		_root.modal.box.attachMovie("menu","background",6);
		_root.modal.box.background.init(87,10,preferences.data.baselayer,
			new Array("None","Yahoo! satellite","Yahoo! satellite (dimmed)"),
			'Choose the background to display',setBackground,0);

		_root.modal.box.attachMovie("checkbox","pointer",4);
		_root.modal.box.pointer.init(10,40,"Use pen and hand pointers",preferences.data.custompointer,function(n) { preferences.data.custompointer=n; });

	}
	
	function setBackground(n) {
		preferences.data.baselayer=n;
		preferences.flush();
		switch (n) {
			case 1: _root.yahoo._alpha=100; break;
			case 2: _root.yahoo._alpha=50 ; break;
		}
		redrawYahoo(); 
	}

	// =====================================================================================
	// Property window functions

	// KeyValue object

	function KeyValue() {
		this._x=_root.propx;
		this._y=_root.propy;
		_root.propy+=19; if (_root.propy>57) { _root.propy=0; _root.propx+=190; }

		this.createTextField('keyname',1,0,-1,70,18);
		with (this.keyname) {
			backgroundColor=0xBBBBBB;
			background=true;
			text=this._name;
			setTextFormat(boldSmall);
			setNewTextFormat(boldSmall);
		};

		this.createTextField('value',2,72,-1,110,18);
		this.value.onSetFocus =function() { if (this.textColor==0x888888) { this.text=''; this.textColor=0x000000; }
											this.addListener(textfieldListener); Key.removeListener(keyListener); _root.elselected=this._name;
											if (_root.currentproptype=='POI') { _root.map.pois[_root.poiselected].clean=false; }
																			  { _root.map.ways[_root.wayselected].clean=false; }
											};
		this.value.onKillFocus=function() { this.removeListener(textfieldListener); Key.addListener(keyListener); 
											if (this.text=='') { _root.redopropertywindow=1; } // crashes player if called directly!
											if (_root.currentproptype=='way') { _root.map.ways[wayselected].redraw(); }
											reflectPresets(); };
		with (this.value) {
			this.value.backgroundColor=0xDDDDDD;
			this.value.background=true;
			this.value.type='input';
			// this.value.tabIndex=_root.propn;
		};
		switch (_root.currentproptype) {
			case 'point':	this.value.variable="_root.map.ways."+wayselected+".path."+pointselected+".4."+this._name;
							this.value.text=_root.map.ways[_root.wayselected].path[_root.pointselected][4][this._name];
							break;
			case 'POI':		this.value.variable="_root.map.pois."+poiselected+".attr."+this._name;
							this.value.text=_root.map.pois[poiselected].attr[this._name];
							break;
			case 'way':		this.value.variable="_root.map.ways."+wayselected+".attr."+this._name;
							this.value.text=_root.map.ways[wayselected].attr[this._name];
							break;
		}
		this.value.setTextFormat(plainSmall);
		this.value.setNewTextFormat(plainSmall);
		_root.propn+=1;
	};
	KeyValue.prototype=new MovieClip();
	Object.registerClass("keyvalue",KeyValue);

	// populatePropertyWindow	- set contents of property window
	// clearPropertyWindow		- clear window

	function populatePropertyWindow(proptype) {
		clearPropertyWindow();
		_root.i_repeatattr._alpha=
		_root.i_newattr._alpha =100-50*(proptype=='');
		_root.i_scissors._alpha=100-50*(proptype!='point');
		if (proptype=='') { return; }
		
		if (proptype!=currentproptype) { presetmenu.init(141,505,0,presetnames[proptype][presetselected],'Choose from a menu of preset attributes describing the '+proptype,setAttributesFromPreset,151); }
		switch (proptype) {
			case 'point':	proparr=_root.map.ways[wayselected].path[pointselected][4]; break;
			case 'POI':		proparr=_root.map.pois[poiselected].attr; break;
			case 'way':		proparr=_root.map.ways[wayselected].attr; break;
		}
		_root.currentproptype=proptype;
		_root.currentproppoint=pointselected;
		_root.currentpropway=wayselected;
		_root.currentproppoi=poiselected;
		for (el in proparr) {
			if (proparr[el]!='' && el!='created_by' && el!='edited_by') {
				_root.properties.attachMovie("keyvalue",el,_root.propn);
			}
			if (proparr[el].substr(0,6)=='(type ') {
				_root.properties[el]['value'].textColor=0x888888;
			}
		}

		_root.presetmenu._visible=true;
		_root.i_preset._visible=true;
		reflectPresets();
		setTabOrder();
	};

	function clearPropertyWindow() {
		removeMovieClip(_root.welcome); 
		_root.propx=0; _root.propy=0; _root.propn=0;
		ct=0;
		for (el in _root.properties) {
			ct+=1;
			removeMovieClip(_root.properties[el]);
		}
		if (ct>0) { _root.savedpoint=_root.currentproppoint;
					_root.savedpoi  =_root.currentproppoi;
					_root.savedway  =_root.currentpropway;
					_root.savedtype =_root.currentproptype; }
	};

	// setTypeText - set contents of type window
	
	function setTypeText(a,b) {
		_root.t_type.text=a; _root.t_type.setTextFormat(boldText);
		_root.t_details.text=b; _root.t_details.setTextFormat(plainText);
		if (_root.map.ways[_root.wayselected].locked) {
			_root.padlock._visible=true;
			_root.padlock._x=_root.t_details.textWidth+15;
		} else {
			_root.padlock._visible=false;
		}
	}

	// reflectPresets - set preset menu based on way values
	// looks in presetselected first, then in all other menus

	function reflectPresets() {
		var i,t;
		var found=findPresetInMenu(presetselected);
		if (found) { presetmenu.setValue(found); return; }
		for (i=0; i<presetmenus[currentproptype].length; i+=1) {
			t=findPresetInMenu(presetmenus[currentproptype][i]); if (t) { found=t; presetselected=presetmenus[currentproptype][i]; }
		}
		if (found) { presetmenu.init(141,505,found,presetnames[currentproptype][presetselected],'Choose from a menu of preset attributes describing the '+currentproptype,setAttributesFromPreset,151);
					 setPresetIcon(presetselected); }
			  else { presetmenu.setValue(0); }
	}

	// look in a particular menu
	
	function findPresetInMenu(menuname) {
		f=0;
		for (pre=presetnames[currentproptype][menuname].length-1; pre>-1; pre-=1) {
			pname=presetnames[currentproptype][menuname][pre];
			pkeys=_root.presets[pname];
			if (pkeys) {
				ok=1;
				for (pkey in pkeys) {
					switch (currentproptype) {
						case 'way':		cvalue=_root.map.ways[wayselected].attr[pkey]; break;
						case 'POI':		cvalue=_root.map.pois[poiselected].attr[pkey]; break;
						case 'point':	cvalue=_root.map.ways[wayselected].path[pointselected][4][pkey]; break;
					}
					if (cvalue==null) { cvalue=''; }
					if (cvalue!=presets[pname][pkey] && presets[pname][pkey].substr(0,6)!='(type ') { ok=0; }
				}
				if (ok==1) { f=pre; }
			}
		}
		return f;
	}

	// setAttributesFromPreset - update way values based on pop-up choice
	
	function setAttributesFromPreset(pre) {
		pname=presetnames[currentproptype][presetselected][pre];
		pkeys=presets[pname];
		for (pkey in pkeys) {
			switch (currentproptype) {
				case 'way':		if   (_root.map.ways[wayselected].attr[pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
								else { _root.map.ways[wayselected].attr[pkey]=presets[pname][pkey];	}
								_root.map.ways[wayselected].redraw();
								_root.map.ways[wayselected].clean=false;
								break;
				case 'POI':		if   (_root.map.pois[poiselected].attr[pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
								else { _root.map.pois[poiselected].attr[pkey]=presets[pname][pkey];	}
								_root.map.pois[poiselected].clean=false;
								break;
				case 'point':	if   (_root.map.ways[wayselected].path[pointselected][4][pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
								else { _root.map.ways[wayselected].path[pointselected][4][pkey]=presets[pname][pkey]; }
								_root.map.ways[wayselected].clean=false;
								break;
			}
		}
		populatePropertyWindow(currentproptype);
	}

	// setPresetIcon and cyclePresetIcon

	function setPresetIcon(category) {
		 _root.attachMovie("preset_"+category,"i_preset",38);
		 with (_root.i_preset) { _x=120; _y=515; };
		_root.i_preset.onPress=function() { cyclePresetIcon(); };
		_root.i_preset.onRollOver=function() { setFloater("Choose what type of presets are offered in the menu"); };
		_root.i_preset.onRollOut =function() { clearFloater(); };
	}
	function cyclePresetIcon() {
		var i,j;
		if (_root.i_preset._visible) {
			j=0;
			for (i=0; i<presetmenus[currentproptype].length; i+=1) {
				if (presetmenus[currentproptype][i]==presetselected) { j=i+1; }
			}
			presetselected=presetmenus[currentproptype][j%i];
			setPresetIcon(presetselected);
			presetmenu.init(141,505,findPresetInMenu(presetselected),presetnames[currentproptype][presetselected],'Choose from a menu of preset attributes describing the '+currentproptype,setAttributesFromPreset,151);
		}
	}

	
	// enterNewAttribute - create new attribute

	function enterNewAttribute() {
		if (_root.wayselected==0 && _root.pointselected==-2 && _root.poiselected==0) { return; }
		if (_root.propn==12) { return; }
		switch (_root.currentproptype) {
			case 'point':	_root.map.ways[_root.wayselected].path[_root.pointselected][4].key='(type value here)'; break;
			case 'POI':		_root.map.pois[poiselected].attr.key='(type value here)'; break;
			case 'way':		_root.map.ways[wayselected].attr.key='(type value here)'; break;
		}
		_root.properties.attachMovie("keyvalue","key",_root.propn);
		_root.properties.key['value'].textColor=0x888888;
		_root.properties.key.keyname.type="input";
		_root.properties.key.keyname.setTextFormat(boldSmall);
		_root.properties.key.keyname.setNewTextFormat(boldSmall);
		_root.properties.key.keyname.onSetFocus=function()  { 
			this.addListener(textfieldListener);
			Key.removeListener(keyListener);
		};
		_root.properties.key.keyname.onKillFocus=function() {
			// rename field
			this.removeListener(textfieldListener);
			Key.addListener(keyListener);
			this._parent.value.variable=null;
			z=this._parent.keyname.text;
			switch (_root.currentproptype) {
				case 'point':	_root.map.ways[wayselected].path[pointselected][4][z]=_root.map.ways[wayselected].path[pointselected][4][this._parent._name];
								_root.map.ways[wayselected].path[pointselected][4][this._parent._name]='';
								// above line should be delete _root.map..., but Ming won't compile 'delete' with more than one [] in it
								this._parent._name=z;
								this._parent.value.variable="_root.map.ways."+wayselected+".path."+pointselected+".4."+z;
								break;
				case 'POI':		_root.map.pois[poiselected].attr[z]=_root.map.pois[poiselected].attr[this._parent._name];
								_root.map.pois[poiselected].attr[this._parent._name]='';
								this._parent._name=z;
								this._parent.value.variable="_root.map.pois."+poiselected+".attr."+z;
								break;
				case 'way':		_root.map.ways[wayselected].attr[z]=_root.map.ways[wayselected].attr[this._parent._name];
								_root.map.ways[wayselected].attr[this._parent._name]='';
								this._parent._name=z;
								this._parent.value.variable="_root.map.ways."+wayselected+".attr."+z;
								break;
			}
		};
		setTabOrder();
		// Selection.setFocus(_root.properties.key.keyname); Selection.setSelection(0,3); // should work, but doesn't
	}
	
	// repeatAttributes - paste in last set of attributes
	
	function repeatAttributes() {
		var i,z;
		if (_root.wayselected==0 && _root.pointselected==-2 && _root.poiselected==0) { return; }
		switch (savedtype) {
			case 'point':	z=_root.map.ways[savedway].path[savedpoint][4]; break;
			case 'POI':		z=_root.map.pois[savedpoi].attr; _root.map.pois[poiselected].attr=new Array(); break;
			case 'way':		z=_root.map.ways[savedway].attr; break;
		}
		for (i in z) {
			if (Key.isDown(Key.SHIFT) && (i=='name' || i=='ref') || i=='created_by') {
				// ignore name and ref if SHIFT pressed
			} else {
				switch (savedtype) {
					case 'point':	j=_root.map.ways[savedway].path[savedpoint][4][i]; break;
					case 'POI':		j=_root.map.pois[savedpoi].attr[i]; break;
					case 'way':		j=_root.map.ways[savedway].attr[i]; break;
				}
				switch (currentproptype) {
					case 'point':	_root.map.ways[wayselected].path[pointselected][4][i]=j;
									_root.map.ways[wayselected].clean=false;
									break;
					case 'POI':		_root.map.pois[poiselected].attr[i]=j; 
									_root.map.pois[poiselected].clean=false;
									break;
					case 'way':		_root.map.ways[wayselected].attr[i]=j; 
									_root.map.ways[wayselected].clean=false;
									_root.map.ways[wayselected].redraw();
									break;
				}
			}
		}
		populatePropertyWindow(_root.currentproptype);
	}
	
	// textChanged		- listener marks way as dirty when any change made

	function textChanged() { 
		if (_root.poiselected!=0) { _root.map.pois[poiselected].clean=false; }
							 else { _root.map.ways[wayselected].clean=false; }
	};
	
	// setTabOrder		- fix order for tabbing between fields

	function setTabOrder() {
		for (el in _root.properties) {
			o=(_root.properties[el]._x/190*4+_root.properties[el]._y/19)*2;
			_root.properties[el].keyname.tabIndex=o;
			_root.properties[el].value.tabIndex=o+1;
		}
	}


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
			case 113:		setBackground(2-1*(Key.isDown(Key.SHIFT))); break;	// f2 - Yahoo! base layer
			case 71:		loadGPS(); break;									// G - load GPS
			case 72:		if (_root.wayselected>0) { wayHistory(); }; break;	// H - way history
			case 82:		repeatAttributes(); break;							// R - repeat attributes
			case 85:		getDeleted(); break;								// U - undelete
			case 88:		_root.map.ways[wayselected].splitWay(); break;		// X - split way
			case Key.PGUP:	zoomIn(); break;									// Page Up - zoom in
			case Key.PGDN:	zoomOut(); break;									// Page Down - zoom out
			case Key.LEFT:  moveMap( 140,0); redrawYahoo(); whichWays(); break;	// cursor keys
			case Key.RIGHT: moveMap(-140,0); redrawYahoo(); whichWays(); break;	//  |
			case Key.DOWN:  moveMap(0,-100); redrawYahoo(); whichWays(); break;	//  |
			case Key.UP:    moveMap(0, 100); redrawYahoo(); whichWays(); break;	//  |
			case 167:		cyclePresetIcon(); break;							// '¤' - cycle presets
			case 187:		enterNewAttribute(); break;							// '+' - add new attribute
			case 189:		keyDelete(0); break;								// '-' - delete node from this way only
			case 76:		showPosition(); break;								// L - show latitude/longitude
			// default:		_root.chat.text=Key.getCode()+" pressed";
		};
	}

	function showPosition() { setTooltip("lat "+Math.floor(coord2lat (_root.map._ymouse)*10000)/10000
									  +"\nlon "+Math.floor(coord2long(_root.map._xmouse)*10000)/10000,0); }
	function startCount()	{ z=new Date(); _root.startTime=z.getTime(); }
	function endCount(id)	{ z=new Date(); zz=Math.floor((z.getTime()-_root.startTime)*100);
							if (zz>100) { _root.chat.text+=id+":"+zz+";"; } }
	


	// =====================================================================================
	// World handling

	// redrawMap(x,y)
	// update all world parameters and call in tiles
	// based on user-selected location
	// ** could be substantially tidied

	function redrawMap(tx,ty) {
		_root.map._x=tx;
		_root.map._y=ty;
		var bscale=Math.pow(2,_root.scale-12);
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

	function redrawYahoo() {
		if (preferences.data.baselayer>0) {
			_root.yahoo._visible=true;
			_root.yahoo._x=0;
			_root.yahoo._y=0;
			_root.ylat=centrelat(_root.bgyoffset);
			_root.ylon=centrelong(_root.bgxoffset);
			_root.yzoom=17-_root.scale;
		} else {
			_root.yahoo._visible=false;
		}
	}

	// zoomIn, zoomOut, changeScaleTo - change scale functions
	
	function zoomIn()  {
		if (_root.scale<_root.maxscale) {
			changeScaleTo(_root.scale+1);
			if (_root.waycount>500) { purgeWays(); }
			if (_root.poicount>500) { purgePOIs(); }
			redrawMap((_root.map._x*2)-350,(_root.map._y*2)-250);
			redrawYahoo();
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
			changeScaleTo(_root.scale-1); 
			redrawMap((_root.map._x+350)/2,(_root.map._y+250)/2);
			redrawYahoo();
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
		_root.scale=newscale;
		if (_root.scale==_root.minscale) { _root.i_zoomout._alpha=25;  }
									else { _root.i_zoomout._alpha=100; }
		if (_root.scale==_root.maxscale) { _root.i_zoomin._alpha =25;  }
									else { _root.i_zoomin._alpha =100; }
		_root.tolerance=4/Math.pow(2,_root.scale-12);
	}

	function resizePOIs() {
		var n=Math.max(100/Math.pow(2,_root.scale-12),6.25);
		for (var qpoi in _root.map.pois) {
			_root.map.pois[qpoi]._xscale=_root.map.pois[qpoi]._yscale=n;
		}
		if (_root.poiselected) {
			highlightSquare(_root.map.pois[poiselected]._x,_root.map.pois[poiselected]._y,8/Math.pow(2,Math.min(_root.scale,16)-12));
		}
	}

	// =====================================================================
	// Map support functions

	// everyFrame() - called onEnterFrame

	function everyFrame() {
		// ----	Fix Yahoo! peculiarities
		_root.yahoo.myMap.enableKeyboardShortcuts(false);
		_root.yahoo._visible=(preferences.data.baselayer>0);

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
					redrawYahoo();									// 0.5s elapsed, so
					_root.yahootime=new Date();						// request new tiles
				}
			}
			moveMap(Math.floor(_xmouse-lastxmouse),Math.floor(_ymouse-lastymouse));
		}
	}

	function endMapDrag() {
		_root.map.onMouseMove=function() {};
		_root.map.onMouseUp  =function() {};
		redrawYahoo();
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

	function centrelat(o)  { return  coord2lat((250-_root.map._y-o)/Math.pow(2,_root.scale-12)); }
	function centrelong(o) { return coord2long((350-_root.map._x-o)/Math.pow(2,_root.scale-12)); }



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
		_root.tracks=new Array();
		var lastTime=0;
		_root.curtrack=0; _root.tracks[curtrack]=new Array();
		var gpxdoc=new XML();
		gpxdoc.load(gpxurl+gpxname);
		gpxdoc.onLoad=function() {
			_root.map.gpx.createEmptyMovieClip("line",1);
			_root.map.gpx.line.lineStyle(1,0x00FFFF,100,false,"none");
	
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
						}
					}
				}
			}
		};
	}

	function parsePoint(xmlobj) {
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
		for (var i=0; i<_root.tracks.length; i+=1) {
			_root.tracks[i]=simplifyPath(_root.tracks[i]);

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

	function simplifyPath(track) {
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
				if (d>furthdist && d>0.2) { furthest=i; furthdist=d; }
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
						_root.map.pois[point]._xscale=_root.map.pois[point]._yscale=Math.max(100/Math.pow(2,_root.scale-12),6.25);
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

	
EOF

	$m->add(new SWF::Action($actionscript));

	# -----	Output file

	$m->nextFrame();

	if (exists($ENV{'DOCUMENT_ROOT'})) {
		# We're running under a web server, so output to browser
		print "Content-type: application/x-shockwave-flash\n\n";
		$m->output(9);
	} else {
		# Running from command line, so output to file
		if ($fn=shift @ARGV) { print "Saving to $fn\n"; $m->save($fn); }
						else { print "Saving to this directory\n"; $m->save("potlatch.swf"); }
	}
