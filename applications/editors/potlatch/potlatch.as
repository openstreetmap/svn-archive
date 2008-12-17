
	System.security.loadPolicyFile("http://www.openstreetmap.org/api/crossdomain.xml");

	// =====================================================================================
	// Initialise

	// Resizable window, disable right-click
	Stage.showMenu = false;
	Stage.align="TL";
	Stage.scaleMode="noScale";
	resizeListener=new Object();
	resizeListener.onResize=function() { resizeWindow(); };
	Stage.addListener(resizeListener);
	var panelheight=110;
	
	// Master movieclip for map
	_root.createEmptyMovieClip("map",10);
	_root.map.setMask(_root.masksquare);

	// Master movieclip for panel
	_root.createEmptyMovieClip("panel",15);
	_root.panel._x=0; _root.panel._y=500;
	_root.panel.beginFill(0xF3F3F3,100);
	_root.panel.moveTo(0,0); _root.panel.lineTo(3000,0);
	_root.panel.lineTo(3000,panelheight); _root.panel.lineTo(0,panelheight);
	_root.panel.lineTo(0,0);
	_root.panel.endFill();
	_root.panel.createEmptyMovieClip("advice",0xFFFFFC);

	_root.createEmptyMovieClip("windows",0xFFFFFD);
	var windowdepth=1;
	var windowsopen=0;

	// Sound
	beep=new Sound();
	beep.loadSound("/potlatch/beep.mp3",false);

	// Co-ordinates
	// London 51.5,0; Weybridge 51.4,-0.5; Worcester 52.2,-2.25; Woodstock 51.85,-1.35
	var minscale=13;				// don't zoom out past this
	var maxscale=19;				// don't zoom in past this
	if (scale) {} else {scale=14;}	// default scale if not provided (e.g. GPX)
	var scale=Math.max(Math.min(Math.floor(scale),maxscale),minscale);
	var masterscale=5825.4222222222;// master map scale - how many Flash pixels in 1 degree longitude
									// (for Landsat, 5120)
	updateCoords();					// get radius, scale
	
	// Preselected way/node (from query string)
	var preway=Number(way);
	var prenode=Number(node);

	// Preferences
	preferences=SharedObject.getLocal("preferences");
	var usertoken=token;
	if (winie=='true' || winie==true) { winie=true; } else { winie=false; }

	// Key listener - needs to be initialised before Yahoo
	keyListener=new Object();
	keyListener.onKeyDown=function() { keyPressed(); };
	Key.addListener(keyListener);

	// Initialise Yahoo
	var ywidth=0;		var yheight=0;			// current Yahoo state
	var yzoom=8;		var lastyzoom=yzoom;	//  |
	var bgxoffset=0;	var bgyoffset=0;		// manually correct Yahoo imagery
	var yahooloaded=false;						// is Yahoo component loaded?
	var yahooinited=false;						// is Yahoo component inited?
	var yahoorightsize=true;					// do we need to resize Yahoo?
	_root.createEmptyMovieClip("yahoo",7);

//	var layernames=new Array("None","Aerial - OpenAerialMap","Aerial - Yahoo!","OSM - Mapnik","OSM - Osmarender","OSM - Maplint (errors)","OSM - cycle map");
//	var layernums=new Array();
//	var j=layernames.length; for (i in layernames) { j--; layernums[layernames[i]]=j; }

	// Main initialisation
	_root.map.createEmptyMovieClip("areas"    ,8);  var areadepth=1;
	_root.map.createEmptyMovieClip("gpx"      ,9);
	_root.map.createEmptyMovieClip("relations",10); var reldepth=1;
	_root.map.createEmptyMovieClip("ways"     ,11); var waydepth=1;
	_root.map.createEmptyMovieClip("pois"	  ,12); var poidepth=1;
	_root.map.createEmptyMovieClip("elastic"  ,5003); // elastic line
	initTiles();					// create tile clips on layer 7

	_root.masksquare.useHandCursor=false;
	_root.masksquare.onPress   =function() { mapClick(); };
	_root.masksquare.onRollOver=function() { mapRollOver(); };
	_root.masksquare.onRollOut =function() { mapRollOut(); };
	_root.masksquare.onRelease =function() { mapClickEnd(); };
	_root.onMouseDown=function() { _root.lastkeypressed=-1; };

	selectWay(0);					// way selected?    0 no, otherwise way id
	var poiselected=0;				// POI selected?    0 no, otherwise way id
	var pointselected=-2;			// point selected? -2 no, otherwise point order
	var waycount=0;					// number of ways currently loaded
	var waysrequested=0;			// total number of ways requested
	var waysreceived=0;				// total number of ways received
	var writesrequested=0;			// number of outstanding write operations
	var relcount=0;					// number of relations currently loaded
	var relsrequested=0;			// total number of relations requested
	var relsreceived=0;				// total number of relations received
	var poicount=0;					// number of POIs currently loaded
	var whichrequested=0;			// total number of whichways requested
	var whichreceived=0;			// total number of whichways received
	var redrawlist=new Array();		// list of ways to redraw (after zooming)
	var redrawskip=true;			// skip redrawing ways, just do POIs?
	var lastwhichways=new Date();	// last time whichways was requested
	var lastresize=new Date();		// last time window was resized
	var mapdragged=false;			// map being dragged?
	var drawpoint=-1;				// point being drawn? -1 no, 0+ yes (point order)
	var lastpoint=0;				// last value of drawpoint (triple-click detection)
	var lastpointtime=null;			// time of last double-click (triple-click detection)
	var newrelid=-1;				// new relation ID  (for those not yet saved)
	var newwayid=-1;				// new way ID		(for those not yet saved)
	var newnodeid=-2;				// new node ID		(for those not yet saved)
	var newpoiid=-1;				// new POI ID		(for those not yet saved)
	var nodes=new Object();			// hash of nodes
	var currentproptype='';			// type of property currently being edited
	var pointertype='';				// current mouse pointer
	var redopropertywindow=null;	// need to redraw property window after deletion?
	var lastkeypressed=null;		// code of last key pressed
	var keytarget='';				// send keys where? ('','key','value')
	var tilesetloaded=-1;			// which tileset is loaded?
	var tolerance=4/Math.pow(2,_root.scale-13);
	var bigedge_l=999999; var bigedge_r=-999999; // area of largest whichways
	var bigedge_b=999999; var bigedge_t=-999999; //  |
	var saved=new Array();			// no saved presets yet
	var sandbox=false;				// we're doing proper editing
	var lang=System.capabilities.language; // language (e.g. 'en', 'fr')
	var signature="Potlatch 0.10g";	// current version
	var maximised=false;			// minimised/maximised?
	var sourcetags=new Array("","","Yahoo","","","","","NPE","OpenTopoMap");

//	if (layernums[preferences.data.baselayer]==undefined) { preferences.data.baselayer="Aerial - Yahoo!"; }
	if (preferences.data.baselayer    ==undefined) { preferences.data.baselayer    =2; }	// background layer
	if (preferences.data.dimbackground==undefined) { preferences.data.dimbackground=true; }	// dim background?
	if (preferences.data.custompointer==undefined) { preferences.data.custompointer=true; }	// use custom pointers?
	if (preferences.data.thinlines    ==undefined) { preferences.data.thinlines    =false;}	// always use thin lines?
	if (preferences.data.advice       ==undefined) { preferences.data.advice       =true; }	// show floating advice?
	if (preferences.data.nosplash     ==undefined) { preferences.data.nosplash     =false; }// hide splash screen?
	if (preferences.data.noname       ==undefined) { preferences.data.nosplash     =false; }// highlight unnamed ways?

	// =====================================================================================
	// Icons

	_root.attachMovie("zoomin","i_zoomin",30);
	with (_root.i_zoomin) { _x=5; _y=5; };
	_root.i_zoomin.onPress=function() { zoomIn(); };

	_root.attachMovie("zoomout","i_zoomout",31);
	with (_root.i_zoomout) { _x=5; _y=27; };
	_root.i_zoomout.onPress=function() { zoomOut(); };
	changeScaleTo(_root.scale);

	// Way-specific

	_root.panel.attachMovie("scissors","i_scissors",32);
	with (_root.panel.i_scissors) { _x=15; _y=63; };
	_root.panel.i_scissors.onPress   =function() { _root.ws.splitWay(_root.pointselected); };
	_root.panel.i_scissors.onRollOver=function() { setFloater(iText("Split way at selected point (X)",'tip_splitway')); };
	_root.panel.i_scissors.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("rotation","i_direction",39);
	with (_root.panel.i_direction) { _x=40; _y=63; _rotation=-45; _visible=true; _alpha=50; };
	_root.panel.i_direction.onPress=function() { _root.ws.reverseWay(); };
	_root.panel.i_direction.onRollOver=function() { setFloater(iText("Direction of way - click to reverse",'tip_direction')); };
	_root.panel.i_direction.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("clockwise","i_clockwise",40);
	with (_root.panel.i_clockwise) { _x=40; _y=63; _visible=false; };
	_root.panel.i_clockwise.onPress=function() { _root.ws.reverseWay(); };
	_root.panel.i_clockwise.onRollOver=function() { setFloater(iText("Clockwise circular way - click to reverse",'tip_clockwise')); };
	_root.panel.i_clockwise.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("anticlockwise","i_anticlockwise",42);
	with (_root.panel.i_anticlockwise) { _x=40; _y=63; _visible=false; };
	_root.panel.i_anticlockwise.onPress=function() { _root.ws.reverseWay(); };
	_root.panel.i_anticlockwise.onRollOver=function() { setFloater(iText("Anti-clockwise circular way - click to reverse",'tip_anticlockwise')); };
	_root.panel.i_anticlockwise.onRollOut =function() { clearFloater(); };

	// General tools

	_root.panel.lineStyle(1,0xCCCCCC,100);
	_root.panel.moveTo(5,78); _root.panel.lineTo(75,78);

	_root.panel.attachMovie("undo","i_undo",38);
	with (_root.panel.i_undo) { _x=15; _y=93; _alpha=50; };
	_root.panel.i_undo.onPress   =function() { _root.undo.rollback(); };
	_root.panel.i_undo.onRollOver=function() { setFloater(iText("Nothing to undo",'tip_noundo')); };
	_root.panel.i_undo.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("gps","i_gps",36);
	with (_root.panel.i_gps) { _x=40; _y=93; };
	_root.panel.i_gps.onPress   =function() { loadGPS(); };
	_root.panel.i_gps.onRollOver=function() { setFloater(iText("Show GPS tracks (G)",'tip_gps')); };
	_root.panel.i_gps.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("prefs","i_prefs",37);
	with (_root.panel.i_prefs) { _x=65; _y=93; };
	_root.panel.i_prefs.onPress   =function() { openOptionsWindow(); };
	_root.panel.i_prefs.onRollOver=function() { setFloater(iText("Set options (choose the map background)",'tip_options')); };
	_root.panel.i_prefs.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("newattr","i_newattr",33);
	with (_root.panel.i_newattr) { _x=690; _y=95; };
	_root.panel.i_newattr.onRelease =function() { _root.panel.properties.enterNewAttribute(); };
	_root.panel.i_newattr.onRollOver=function() { setFloater(iText("Add a new tag",'tip_addtag')); };
	_root.panel.i_newattr.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("newrel","i_newrel",44);
	with (_root.panel.i_newrel) { _x=690; _y=75; backgroundColor=0xDDBBBB; background=true;};
	_root.panel.i_newrel.onRelease =function() { if (Key.isDown(Key.SHIFT)) { _root.panel.properties.repeatAttributes(false);
												} else { addToRelation(); } };
	_root.panel.i_newrel.onRollOver=function() { setFloater(iText("Add to a relation",'tip_addrelation')); };
	_root.panel.i_newrel.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("repeatattr","i_repeatattr",34);
	with (_root.panel.i_repeatattr) { _x=690; _y=55; };
	_root.panel.i_repeatattr.onPress   =function() { _root.panel.properties.repeatAttributes(true); };
	_root.panel.i_repeatattr.onRollOver=function() { setFloater(iText("Repeat tags from the previously selected way (R)",'tip_repeattag')); };
	_root.panel.i_repeatattr.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("exclamation","i_warning",35);
	with (_root.panel.i_warning) { _x=58; _y=50; _visible=false; };
	_root.panel.i_warning.onPress=function() { handleWarning(); };
	_root.panel.i_warning.onRollOver=function() { setFloater(iText("An error occurred - click for details",'tip_alert')); };
	_root.panel.i_warning.onRollOut =function() { clearFloater(); };
	wflashid=setInterval(function() { _root.panel.i_warning._alpha=150-_root.panel.i_warning._alpha; }, 750);

	_root.panel.attachMovie("padlock","padlock",41);
	with (_root.panel.padlock) { _y=32; _visible=false; };
	_root.panel.padlock.onPress=function() { keyLock(); };

	_root.createEmptyMovieClip("pointer",0xFFFFFF);

	// =====================================================================================
	// Initialise text areas

	// Debug text fields

	_root.createTextField('chat',20,130,315,400,80); // 515
	with (_root.chat) {
		multiline=true; wordWrap=true;  border=true; selectable = true; type = 'input';
		_visible=false;//#debug
	};

	_root.createTextField('coordmonitor',21,130,400,400,60); // 515
	with (_root.coordmonitor) {
		multiline=true; wordWrap=true; border=true; selectable = true; type = 'input';
		_visible=false;//#debug
	};

	_root.createTextField('floater',0xFFFFFE,15,30,200,18);
	with (floater) {
		background=true; backgroundColor=0xFFEEEE;
		border=true; borderColor=0xAAAAAA;
		selectable=false; _visible=false;
	}
	var floaterID=null;

	// Centre cross-hair
	
	if (_root.chat._visible) {
		_root.createEmptyMovieClip("crosshair",0xFFFFF0);
		with (_root.crosshair) {
			_x=Stage.width/2;
			_y=(Stage.height-panelheight)/2;
			lineStyle(1,0xFFFFFF,100);
			moveTo(-2,0); lineTo(-10,0);
			moveTo( 2,0); lineTo( 10,0);
			moveTo(0,-2); lineTo(0,-10);
			moveTo(0, 2); lineTo(0, 10);
		}
	}

// for (i in layernames) { _root.chat.text+=i+"="+layernames[i]+","; }
// for (i in layernums ) { _root.chat.text+=i+"="+layernums[i]+","; }

	// Text formats
	
	plainText =new TextFormat(); plainText.color =0x000000; plainText.size =14; plainText.font ="_sans";
	plainSmall=new TextFormat(); plainSmall.color=0x000000;	plainSmall.size=12; plainSmall.font="_sans";
	plainDim  =new TextFormat(); plainDim.color  =0x777777;	plainDim.size  =12; plainDim.font  ="_sans";
	plainRight=new TextFormat(); plainRight.color=0x000000;	plainRight.size=12; plainRight.font="_sans"; plainRight.align="right";
	plainTiny =new TextFormat(); plainTiny.color =0x000000;	plainTiny.size =11; plainTiny.font ="_sans";
	plainWhite=new TextFormat(); plainWhite.color=0xFFFFFF; plainWhite.size=12; plainWhite.font="_sans";
	greySmall =new TextFormat(); greySmall.color =0x888888;	greySmall.size =12; greySmall.font ="_sans";
	boldText  =new TextFormat(); boldText.color  =0x000000; boldText.size  =14; boldText.font  ="_sans"; boldText.bold =true;
	boldSmall =new TextFormat(); boldSmall.color =0x000000; boldSmall.size =12; boldSmall.font ="_sans"; boldSmall.bold=true;
	boldWhite =new TextFormat(); boldWhite.color =0xFFFFFF; boldWhite.size =12; boldWhite.font ="_sans"; boldWhite.bold=true;
	menu_on	  =new TextFormat(); menu_on.color   =0x000000; menu_on.size   =12; menu_on.font   ="_sans"; menu_on.bold  =true;
	menu_off  =new TextFormat(); menu_off.color  =0xFFFFFF; menu_off.size  =12; menu_off.font  ="_sans"; menu_off.bold =true;
	auto_on	  =new TextFormat(); auto_on.color   =0x0000FF; auto_on.size   =12; auto_on.font   ="_sans"; auto_on.bold  =true;
	auto_off  =new TextFormat(); auto_off.color  =0xFFFFFF; auto_off.size  =12; auto_off.font  ="_sans"; auto_off.bold =true;

	// Colour transform
	
	to_black  =new Object(); to_black.ra=to_black.ga=to_black.ba=-100;
	to_red	  =new Object(); to_red.rb=255; to_red.gb=0;
	to_normal =new Object(); to_normal.ra=to_normal.ga=to_normal.ba=100;
							 to_normal.rb=to_normal.gb=to_normal.bb=0;

	// Text fields

	_root.createEmptyMovieClip('waysloading',22);
	_root.waysloading._visible=false;

	_root.createTextField('tooltip',46,480,30,215,100);
	with (_root.tooltip  ) { text=""; setTextFormat(plainRight); selectable=false; type='dynamic'; };

	_root.panel.createTextField('t_type',23,5,5,220,20);
	with (_root.panel.t_type	 ) { text=signature; setTextFormat(boldText); selectable=false; };
	
	_root.panel.createTextField('t_details',24,5,23,220,20);
	with (_root.panel.t_details) { text=""; setTextFormat(plainText); selectable=false; };
	
	// MovieClip loader
	var tileLoader=new MovieClipLoader();
	tileListener=new Object();
	tileLoader.addListener(tileListener);

	// Interaction with responder script
	var loaderWaiting=false;

	remote_read=new NetConnection();
	remote_read.connect(apiurl+'/read');
	remote_read.onStatus=function(info) { _root.panel.i_warning._visible=true; };

	remote_write=new NetConnection();
	remote_write.connect(apiurl+'/write');
	remote_write.onStatus=function(info) { _root.panel.i_warning._visible=true; };

	#include 'node.as'
	#include 'anchorpoint.as'
	#include 'poi.as'
	#include 'relation.as'
	#include 'way.as'
	#include 'history.as'
	#include 'ui.as'
	#include 'properties.as'
	#include 'world.as'
	#include 'tiles.as'
	#include 'gps.as'
	#include 'undo.as'
	#include 'advice.as'
	#include 'changeset.as'
	#include 'start.as'

	// =====================================================================================
	// Start

	var undo=new UndoStack();

	_root.panel.attachMovie("propwindow","properties",50);
	with (_root.panel.properties) { _x=110; _y=25; _visible=false; };

	_root.panel.attachMovie("presetmenu","presets",60);
	with (_root.panel.presets) { _x=110; _y=1; _visible=false; };

	updateButtons();
	updateScissors();
	resizeWindow();
	startChangeset('');
	loadPresets();






	// =====================================================================
	// Map support functions
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
	
	// processMapDrag, moveMap - process map dragging

	function processMapDrag() {
		if (mapDragging() || Key.isDown(Key.SPACE)) {
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
			} else if (preferences.data.baselayer) {
				redrawBackground();
			}
			moveMap(Math.floor(_xmouse-lastxmouse),Math.floor(_ymouse-lastymouse));
		}
	}

	function endMapDrag() {
		delete _root.onMouseMove;
		delete _root.onMouseUp;
		if (mapDragging()) {
			redrawBackground();
			updateLinks();
			whichWays();
		}
		restartElastic();
		if (_root.wayselected) { setPointer(''); }
						  else { setPointer('pen'); }
	}
	
	function moveMap(xdiff,ydiff) {
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		if (Key.isDown(Key.SPACE)) {
			_root.bgxoffset+=xdiff; _root.map.tiles._x+=xdiff;
			_root.bgyoffset+=ydiff; _root.map.tiles._y+=ydiff;
			updateCoords(_root.map._x,_root.map._y);
		} else {
			_root.map._x+=xdiff;
			_root.map._y+=ydiff;
			updateCoords(_root.map._x,_root.map._y);
		}
	}

	// mapClick - user has clicked within map area, so start drag
	
	function mapClick() {
		setPointer('pen');
		clearTooltip();
		_root.onMouseMove=function() { processMapDrag(); };
		_root.onMouseUp  =function() { endMapDrag(); };
		_root.mapdragged=false;
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		_root.firstxmouse=_root._xmouse;
		_root.firstymouse=_root._ymouse;
		_root.clicktime=new Date();
		_root.yahootime=new Date();
	}

	// mapDragging - test whether map was dragged or just clicked
	
	function mapDragging() {
		var t=new Date();
		var tol=Math.max(_root.tolerance,2);
		var longclick=(t.getTime()-_root.clicktime)>300;
		var xdist=Math.abs(_root.firstxmouse-_root._xmouse);
		var ydist=Math.abs(_root.firstymouse-_root._ymouse);
		if ((xdist<tol*4  && ydist<tol*4 ) ||
		   ((xdist<tol*10 && ydist<tol*10) && !longclick)) {
		} else {
			_root.mapdragged=true;
		}
		return _root.mapdragged;
	}

	// mapClickEnd - end of click within map area

	function mapClickEnd() {
		removeWelcome(true);
		var t=new Date();
		if (!mapdragged) {
		// Clicked on map without dragging
			// Adding a point to the way being drawn
			if (_root.drawpoint>-1) {
				_root.newnodeid--;
				_root.nodes[newnodeid]=new Node(newnodeid,_root.map._xmouse,_root.map._ymouse,new Array(),0);
				if (_root.pointselected>-2) {
					setTypeText(iText("Way",'way'),_root.wayselected);
					_root.panel.properties.tidy();
					_root.panel.properties.init('way',getPanelColumns(),4);
					updateButtons();
					updateScissors(false);
				}
				addEndPoint(_root.nodes[newnodeid]);
				restartElastic();

			// Deselecting a way
			} else if (_root.wayselected) {
				uploadSelected(); deselectAll();
				_root.lastpoint=-1;				// Trap double-click to deselect
				_root.lastpointtime=new Date();	//  |

			// Deselecting a POI
			} else if (_root.poiselected) {
				uploadSelected(); deselectAll();

			// Starting a new way
			// ** double-click trap should probably also check distance moved
			} else if (_root.lastpoint!=-1 || (t.getTime()-_root.lastpointtime)>300) {
				_root.newnodeid--; 
				_root.nodes[newnodeid]=new Node(newnodeid,_root.map._xmouse,_root.map._ymouse,new Array(),0);
				startNewWay(newnodeid);
			}
		}
		_root.mapdragged=false;
	}



	// =====================================================================
	// Tooltip and pointer functions

	function setTooltip(txt,delay) {
		_root.tooltip.text=txt;
		_root.tooltip.setTextFormat(plainRight);
		_root.createEmptyMovieClip('ttbackground',45);
		with (_root.ttbackground) {
			_x=Stage.width-5; _y=30;
			beginFill(0xFFFFFF,75);
			moveTo(0,0); lineTo(-_root.tooltip.textWidth-10,0);
			lineTo(-_root.tooltip.textWidth-10,_root.tooltip.textHeight+5);
			lineTo(0,_root.tooltip.textHeight+5); lineTo(0,0);
			endFill();
		}
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

			if (_root._ymouse+25<Stage.height      ) { _y=_root._ymouse+4;			 }
												else { _y=_root._ymouse-16; 	     }
			if (_root._xmouse+textWidth<Stage.width) { _x=_root._xmouse+4;			 }
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

	function setPointer(ptype) {
		if (_root.pointertype==ptype) { return; }
		if ((ptype) && preferences.data.custompointer) {
			_root.attachMovie(ptype,"pointer",0xFFFFFF);
			_root.pointer.cacheAsBitmap=true;
			_root.pointer._x=_root._xmouse;
			_root.pointer._y=_root._ymouse;
			_root.pointer.startDrag(true);
			Mouse.hide();
		} else {
			_root.pointer._visible=false;
			_root.pointer.stopDrag();
			Mouse.show();
			removeMovieClip(_root.pointer);
		}
		_root.pointertype=ptype;
	}

	



	// =====================================================================================
	// Keypress functions

	// keyPressed				- key listener

	function keyPressed() {
		clearAdvice();
		var k=Key.getCode();
		var c=Key.getAscii(); if (c>=97 && c<=122) { c=c-32; }
		var s=String.fromCharCode(c);
		_root.lastkeypressed=k;

		if (keytarget=='keyname' || keytarget=='value') {
			if (_root.auto!=undefined) { _root.auto.keyRespond(k); }
					   else if (k==13) { autoEnter(); }
			return;
		} else if (_root.windowsopen) {
			if (k==187 && _root.windows.relation.box.properties!=undefined) {
				_root.windows.relation.box.properties.enterNewAttribute();
			};
			return;
		} else if (keytarget!='') { return; }

		if (c>48 && c<58 && (wayselected!=0 || poiselected!=0)) {
			if (presetnames[_root.panel.properties.proptype][_root.panel.presets.group][c-48]!=null) {
				_root.panel.presets.setAttributes(c-48);
				_root.panel.presets.reflect();
				if (_root.panel.properties.proptype=='way') { _root.ws.redraw(); }
			}
			return;
		} else if (k>=112 && k<=120) {
			preferences.data.dimbackground=Key.isDown(Key.SHIFT); 
			setBackground(k-112);
			return;
		}

		switch (k) {
			case 46:		;													// DELETE/backspace - delete way/node
			case 8:			if (Key.isDown(Key.SHIFT)) {						//  |
								if (_root.wayselected!=0) { _root.ws.removeWithConfirm(); }
							} else { keyDelete(1); }; break;					//  |
			case 13:		_root.junction=false; stopDrawing(); break;			// ENTER - stop drawing line
			case 27:		keyRevert(); break;									// ESCAPE - revert current way
			case Key.PGUP:	zoomIn(); break;									// Page Up - zoom in
			case Key.PGDN:	zoomOut(); break;									// Page Down - zoom out
			case Key.LEFT:  moveMap( 140,0); updateLinks(); redrawBackground(); whichWays(); break;	// cursor keys
			case Key.RIGHT: moveMap(-140,0); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case Key.DOWN:  moveMap(0,-100); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case Key.UP:    moveMap(0, 100); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case Key.CAPSLOCK: dimMap(); break;									// CAPS LOCK - dim map
			case 167:		_root.panel.presets.cycleIcon(); break;				// cycle presets
		}
		
		switch (s) {
			case 'C':		closeChangeset(); break;							// C - close current changeset
			case 'G':		loadGPS(); break;									// G - load GPS
			case 'H':		getHistory(); break;								// H - history
			case 'L':		showPosition(); break;								// L - show latitude/longitude
			case 'P':		askOffset(); break;									// O - parallel path
			case 'R':		_root.panel.properties.repeatAttributes(true);break;// R - repeat attributes
			case 'U':		getDeleted(); break;								// U - undelete
			case 'X':		_root.ws.splitWay(_root.pointselected); break;		// X - split way
			case 'Z':		_root.undo.rollback(); break;						// Z - undo
			case '`':		_root.panel.presets.cycleIcon(); break;				// '`' - cycle presets
			case '+':		_root.panel.properties.enterNewAttribute(); break;	// '+' - add new attribute (107/187)
			case '-':		keyDelete(0); break;								// '-' - delete node from this way only (189)
			case '/':		cycleStacked(); break;								// '/' - cycle between stacked ways (191)
			case 'M':		maximiseSWF(); break;								// 'M' - maximise/minimise Potlatch
			case 'K':		keyLock(); break;									// 'K' - lock item
			case 'S':		_root.panel.properties.setTag("source",sourcetags[preferences.data.baselayer]); break;	// 'S' - set source tag
//			default:		_root.chat.text=Key.getCode()+" pressed";
		};
	}

	function showPosition() { setTooltip("lat "+Math.floor(coord2lat (_root.map._ymouse)*10000)/10000
									  +"\nlon "+Math.floor(coord2long(_root.map._xmouse)*10000)/10000,0); }
	function startCount()	{ z=new Date(); _root.startTime=z.getTime(); }
	function endCount(id)	{ z=new Date(); zz=Math.floor((z.getTime()-_root.startTime)*100);
							if (zz>100) { _root.chat.text+=id+":"+zz+";"; } }

	function showObjects(z,indnt) {
		for (var i in z) {
			_root.chat.text+=indnt+i+" ("+typeof(z[i])+")\n";
			if (typeof(z[i])=='movieclip' || typeof(z[i])=='object') {
				showObjects(z[i],indnt+" ");
			}
		}
	}
	
	function showWaysForNode() {
		var z=_root.ws.path[pointselected].ways;
		_root.coordmonitor.text="";
		for (i in z) { _root.coordmonitor.text+=i+","; }
	}

	function keyLock() {
		_root.panel.padlock._x=_root.panel.t_details.textWidth+15;
		if (_root.wayselected && _root.ws.locked && _root.ws.path.length>200 && !_root.ws.historic) {
			setAdvice(true,iText("Too long to unlock - please split into shorter ways",'advice_toolong'));
		} else if (_root.wayselected) {
			_root.ws.locked=!_root.ws.locked;
			_root.ws.redraw();
			_root.panel.padlock._visible=_root.ws.locked;
			if (!_root.ws.locked) { markWayRelationsDirty(_root.wayselected); }
		} else if (_root.poiselected) {
			_root.map.pois[poiselected].locked=!_root.map.pois[poiselected].locked;
			_root.map.pois[poiselected].recolour();
			_root.panel.padlock._visible=_root.map.pois[poiselected].locked;
			if (!_root.map.pois[poiselected].locked) { markNodeRelationsDirty(poiselected); }
		}
	}

	function keyDelete(doall) {
		if (_root.poiselected) {
			// delete POI
			_root.map.pois[poiselected].saveUndo(iText("deleting",'deleting'));
			_root.map.pois[poiselected].remove();
		} else if (_root.drawpoint>-1) {
			// 'backspace' most recently drawn point
			_root.undo.append(UndoStack.prototype.undo_deletepoint,
							  new Array(deepCopy(_root.ws.path[drawpoint]),
										[wayselected],[drawpoint]),
							  iText("deleting a point",'action_deletepoint'));
			_root.ws.path[drawpoint].removeWay(_root.wayselected);
			if (_root.drawpoint==0) { _root.ws.path.shift(); }
							   else { _root.ws.path.pop(); _root.drawpoint-=1; }
			if (_root.ws.path.length) {
				_root.ws.clean=false;
				markClean(false);
				_root.ws.redraw();
				_root.ws.highlightPoints(5000,"anchor");
				_root.ws.highlight();
				restartElastic();
			} else {
				_root.map.anchors[_root.drawpoint].endElastic();
				_root.ws.remove();
				_root.drawpoint=-1;
			}
		} else if (_root.pointselected>-2) {
			// delete selected point
			if (doall==1) { _root.ws.path[_root.pointselected].removeFromAllWays(); }
					 else { _root.ws.removeAnchorPoint(pointselected); }
			_root.pointselected=-2;
			_root.drawpoint=-1;
			_root.map.elastic.clear();
			clearTooltip();
			markClean(false);
			if (_root.wayselected) { _root.ws.select(); }
		}
	};

	function keyRevert() {
		if		(_root.wayselected<0) { _root.ws.saveUndo(iText("deleting",'deleting'));
										stopDrawing();
										removeMovieClip(_root.map.areas[wayselected]);
										removeMovieClip(_root.ws); }
		else if	(_root.wayselected>0) {	_root.ws.saveUndo(iText("cancelling changes to",'action_cancelchanges'));
										stopDrawing();
										_root.ws.reload(); }
		else if (_root.poiselected>0) { _root.map.pois[poiselected].saveUndo(iText("cancelling changes to",'action_cancelchanges'));
										_root.map.pois[poiselected].reload(); }
		else if (_root.poiselected<0) { _root.map.pois[poiselected].saveUndo(iText("deleting",'deleting'));
										removeMovieClip(_root.map.pois[poiselected]); }
		revertDirtyRelations();
		deselectAll();
	};



	// =====================================================================
	// Potlatch-specific UI functions
	
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
				 else { error=result[0]+iText("\n\nPlease e-mail richard\@systemeD.net with a bug report, saying what you were doing at the time.",'emailauthor'); h+=50; }
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(275,h,new Array('Ok'),null);
		_root.windows.error.box.createTextField("prompt",2,7,9,250,h-30);
		writeText(_root.windows.error.box.prompt,error);
	}

	function handleWarning() {
		_root.windows.attachMovie("modal","error",++windowdepth);
		_root.windows.error.init(275,130,new Array('Retry','Cancel'),handleWarningAction);
		_root.windows.error.box.createTextField("prompt",2,7,9,250,100);
		writeText(_root.windows.error.box.prompt,iText("Sorry - the connection to the OpenStreetMap server failed. Any recent changes have not been saved.\n\nWould you like to try again?",'error_connectionfailed'));
	};

	function handleWarningAction(choice) {
		if (choice=='Retry') {
			// loop through all ways which are uploading, and reupload
			_root.panel.i_warning._visible=false;
			for (var qway in _root.map.ways) {
				if (_root.map.ways[qway].uploading) {
					_root.map.ways[qway].uploading=false;
					_root.map.ways[qway].upload();
				}
			}
			for (var qrel in _root.map.relations) {
				if (_root.map.relations[qrel].uploading) {
					_root.map.relations[qrel].uploading=false;
					_root.map.relations[qrel].upload();
				}
			}
		}
	};


	// =====================================================================
	// Internationalisation functions
	
	function iText(en,id,key1,key2) {
		var t=en;
		if (l!='en' && _root.i18n[id][_root.lang]) {
			t=_root.i18n[id][_root.lang];
		}
		t=replaceStr(t,'$1',key1);
		t=replaceStr(t,'$2',key2);
		t=replaceStr(t,'\\n',"\n");
		return t;
	}

	function replaceStr(s,a,b) {
		while (s.indexOf(a)>-1) {
			var n=s.indexOf(a);
			s=s.substr(0,n)+b+s.substr(n+a.length);
		}
		return s;
	}


	// =====================================================================
	// General support functions

	// everyFrame() - called onEnterFrame

	function everyFrame() {

		// ----	Fix Yahoo! peculiarities
		_root.yahoo.myMap.enableKeyboardShortcuts(false);

		if (preferences.data.baselayer==2) {
			var t=0;
			for (i in _root.yahoo.myMap.map["map_"+(18-_root.scale)].mc) {
				t+=_root.yahoo.myMap.map["map_"+(18-_root.scale)].mc[i][i].getBytesTotal();
			}
			_root.yahoo._visible=(t>124000);	// 122850=all blank
		}

		if (_root.yahoo.myMap.config.isLoaded) {
			if (!_root.yahooinited) {
				_root.yahooinited=true;
				_root.yahooresizer=setInterval(resizeWindow,1000);
				setYahooSize();
				enableYahooZoom();
				_root.yahoo.myMap.map.updateCopyright();
			} else if (!_root.yahoorightsize) {
				_root.yahoorightsize=true;
				_root.yahooresizer=setInterval(resizeWindow,1000);
			}
		}

		// ----	Do we need to redraw the property window?
		if (_root.redopropertywindow) {
			_root.redopropertywindow.tidy();
			_root.redopropertywindow.reinit();
			_root.redopropertywindow=null;
		}

		// ---- If resizing has stopped, issue new whichways
		//		and resize panel
		var t=new Date();
		if ((t.getTime()-lastresize.getTime())>500) {
			if (lastresize.getTime()>lastwhichways.getTime()) {
				whichWays();
				redrawBackground();
			}
		}

		// ----	Control "loading ways" display
		var loading=(_root.waysrequested!=_root.waysreceived) || (_root.whichrequested!=_root.whichreceived);
		if (loading && _root.writesrequested) { setIOStatus(3); }
			  else if (_root.writesrequested) { setIOStatus(2); }
			  				else if (loading) { setIOStatus(1); }
			  							 else { _root.io=0; _root.waysloading._visible=false; }

		// ---- Redraw lines if necessary (3 per frame)
		if (_root.redrawlist.length) {
			for (var i=0; i<3; i++) {
				var w=_root.redrawlist.pop();
				_root.map.ways[w].redraw(_root.redrawskip);
			}
		} else { _root.redrawskip=true; }

		// ---- Service tile queue
		if (preferences.data.baselayer!=0 &&
			preferences.data.baselayer!=2 ) { serviceTileQueue(); }

		if (_root.map.ways._alpha<40) {
			if (!Key.isToggled(Key.CAPSLOCK)) { dimMap(); }
		}
			
		// ----	Reinstate focus if lost after click event
		if (_root.reinstatefocus) {
			Selection.setFocus(_root.reinstatefocus); 
			_root.reinstatefocus=null;
		}
	}
	
	// Dim map on CAPS LOCK

	function dimMap() {
		_root.map.areas._alpha=
		_root.map.highlight._alpha=
		_root.map.relations._alpha=
		_root.map.ways._alpha=
		_root.map.pois._alpha=Key.isToggled(Key.CAPSLOCK)?30:100;
	}

	// Options window
	
	function openOptionsWindow() {
		_root.windows.attachMovie("modal","options",++windowdepth);
		_root.windows.options.init(290,170,new Array('Ok'),function() { preferences.flush(); } );
		_root.windows.options.box.createTextField("prompt1",2,7,9,80,20);
		writeText(_root.windows.options.box.prompt1,iText("Background:",'option_background'));

		_root.windows.options.box.attachMovie("menu","background",30);
		_root.windows.options.box.background.init(87,10,preferences.data.baselayer,
			new Array("None","-----------------------------------------","Aerial - Yahoo!","OSM - Mapnik","OSM - Osmarender","OSM - Maplint (errors)","OSM - cycle map","Other - out-of-copyright map","Other - OpenTopoMap"),
			'Choose the background to display',setBackground,null,0);

		_root.windows.options.box.attachMovie("checkbox","fadepref",5);
		_root.windows.options.box.fadepref.init(10,40,iText("Fade background",'option_fadebackground'),preferences.data.dimbackground,function(n) { preferences.data.dimbackground=n; redrawBackground(); });

		_root.windows.options.box.attachMovie("checkbox","linepref",8);
		_root.windows.options.box.linepref.init(10,60,iText("Use thin lines at all scales",'option_thinlines'),preferences.data.thinlines,function(n) { preferences.data.thinlines=n; changeScaleTo(_root.scale); redrawWays(); });

		_root.windows.options.box.attachMovie("checkbox","noname",10);
		_root.windows.options.box.noname.init(10,80,iText("Highlight unnamed roads",'option_noname'),preferences.data.noname,function(n) { preferences.data.noname=n; redrawWays(); });

		_root.windows.options.box.attachMovie("checkbox","pointer",4);
		_root.windows.options.box.pointer.init(10,100,iText("Use pen and hand pointers",'option_custompointers'),preferences.data.custompointer,function(n) { preferences.data.custompointer=n; });

		_root.windows.options.box.attachMovie("checkbox","warnings",3);
		_root.windows.options.box.warnings.init(10,120,iText("Show floating warnings",'option_warnings'),preferences.data.advice,function(n) { preferences.data.advice=n; });

	}
	
	// markClean - set JavaScript variable for alert when leaving page

	function markClean(a) {
		if (!_root.sandbox) {
			if (winie) { // _root.chat._visible=true; fscommand("changesaved",a);
					   }
				  else { getURL("javascript:var changesaved="+a); }
		}
	}
	
	// deselectAll

	function deselectAll() {
		_root.map.createEmptyMovieClip("anchors",5000); 
		removeMovieClip(_root.map.highlight);
		_root.panel.i_clockwise._visible=false;
		_root.panel.i_anticlockwise._visible=false;
		_root.panel.i_direction._visible=true;
		_root.panel.i_direction._alpha=50;
		clearTooltip();
		setTypeText("","");
		_root.panel.padlock._visible=false;

		_root.panel.properties.saveAttributes();
		_root.panel.properties.tidy();
		_root.panel.properties.init('');
		_root.panel.presets.init();
		updateButtons();
		updateScissors(false);
		poiselected=0;
		pointselected=-2;
		lastpoint=0;
		selectWay(0);
		markClean(true);
	};
	
	// uploadSelected
	
	function uploadSelected() {
		_root.panel.properties.tidy();
		if (_root.wayselected!=0 && !_root.ws.clean) {
			uploadDirtyWays(true);
		}
		if (_root.poiselected!=0 && !_root.map.pois[poiselected].clean) {
			_root.map.pois[poiselected].upload();
		}
		uploadDirtyRelations();
	};
	
	function uploadDirtyWays(allow_ws) {
		var z=_root.map.ways;
		for (i in z) {
			if (!_root.map.ways[i].clean && (i!=wayselected || allow_ws) && !_root.map.ways[i].hasDependentNodes()) { 
				_root.map.ways[i].upload();
			}
		}
	};

	// highlightSquare
	
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

	// setIOStatus - update top-right message

	function setIOStatus(msg) {
		if (_root.io==msg) { return; }
		_root.io=msg; var t;
		switch (msg) {
			case 3:	t=iText("loading/saving data",'hint_saving_loading'); break;
			case 2:	t=iText("saving data",'hint_saving'); break;
			case 1: t=iText("loading data",'hint_loading'); break;
		}
		_root.waysloading.createTextField('prompt',2,0,0,195,20);
		with (_root.waysloading.prompt) { text=t; setTextFormat(plainRight); selectable=false; }
		_root.waysloading.createEmptyMovieClip('bg',1);
		with (_root.waysloading.bg) {
			beginFill(0xFFFFFF,75);
			moveTo(215,0); lineTo(210-_root.waysloading.prompt.textWidth-22,0);
			lineTo(210-_root.waysloading.prompt.textWidth-22,_root.waysloading.prompt.textHeight+5);
			lineTo(215,_root.waysloading.prompt.textHeight+5); lineTo(215,0);
			endFill();
		}
		_root.waysloading._visible=true;
	}

	// deepCopy (recursive) and shallowCopy (non-recursive)

	function deepCopy(z) {
		var a;
		if (z.length==null) { a=new Object(); }
					   else { a=new Array(); }
		for (var i in z) {
			if (typeof(z[i])=='object') { a[i]=deepCopy(z[i]); }
								   else { a[i]=z[i]; }
		}
		return a;
	};

	function shallowCopy(z) {
		var a;
		if (z.length==null) { a=new Object(); }
					   else { a=new Array(); }
		for (var i in z) { a[i]=z[i]; }
		return a;
	}
