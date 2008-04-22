
	// =====================================================================================
	// Initialise

	// Site-specific URLs
//	var apiurl='rubyamf.cgi';
//	var gpsurl='/potlatch/getgps.cgi';
//	var gpxurl='http://localhost:3000/trace/';
//	var yahoourl='/~richard/potlatch/ymap.swf';
//	var tileprefix='http://127.0.0.1/~richard/cgi-bin/proxy.cgi?url=';
	var apiurl='../api/0.5/amf';
	var gpsurl='../api/0.5/swf/trackpoints';
	var gpxurl='http://www.openstreetmap.org/trace/';
	var yahoourl='/potlatch/ymap2.swf';
	var tileprefix='';
	var gpxsuffix='/data.xml';

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

	// Co-ordinates
	// London 51.5,0; Weybridge 51.4,-0.5; Worcester 52.2,-2.25; Woodstock 51.85,-1.35
	var minscale=12;				// don't zoom out past this
	var maxscale=19;				// don't zoom in past this
	if (scale) {} else {scale=14;}	// default scale if not provided (e.g. GPX)
	var scale=Math.max(Math.min(Math.floor(scale),maxscale),minscale);
	var masterscale=5825.4222222222;// master map scale - how many Flash pixels in 1 degree longitude
									// (for Landsat, 5120)
	updateCoords();					// get radius, scale
	
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
	var relcount=0;					// number of relations currently loaded
	var relsrequested=0;			// total number of relations requested
	var relsreceived=0;				// total number of relations received
	var poicount=0;					// number of POIs currently loaded
	var whichrequested=0;			// total number of whichways requested
	var whichreceived=0;			// total number of whichways received
	var lastwhichways=new Date();	// last time whichways was requested
	var lastresize=new Date();		// last time window was resized
	var dragmap=false;				// map being dragged?
	var drawpoint=-1;				// point being drawn? -1 no, 0+ yes (point order)
	var newrelid=-1;				// new relation ID  (for those not yet saved)
	var newwayid=-1;				// new way ID		(for those not yet saved)
	var newnodeid=-2;				// new node ID		(for those not yet saved)
	var newpoiid=-1;				// new POI ID		(for those not yet saved)
	var currentproptype='';			// type of property currently being edited
	var pointertype='';				// current mouse pointer
	var redopropertywindow=null;	// need to redraw property window after deletion?
	var lastkeypressed=null;		// code of last key pressed
	var keytarget='';				// send keys where? ('','dialogue','key','value')
	var basekeytarget='';			// reset keytarget to what after editing key?
	var tilesetloaded=-1;			// which tileset is loaded?
	var tolerance=4/Math.pow(2,_root.scale-13);
	var bigedge_l=999999; var bigedge_r=-999999; // area of largest whichways
	var bigedge_b=999999; var bigedge_t=-999999; //  |
	var saved=new Array();			// no saved presets yet
	var sandbox=false;				// we're doing proper editing
	var signature="Potlatch 0.8b";	// current version

//	if (layernums[preferences.data.baselayer]==undefined) { preferences.data.baselayer="Aerial - Yahoo!"; }
	if (preferences.data.baselayer    ==undefined) { preferences.data.baselayer    =2; }	// show Yahoo?
	if (preferences.data.dimbackground==undefined) { preferences.data.dimbackground=true; }	// dim background?
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

	_root.panel.attachMovie("scissors","i_scissors",32);
	with (_root.panel.i_scissors) { _x=15; _y=93; };
	_root.panel.i_scissors.onPress   =function() { _root.ws.splitWay(); };
	_root.panel.i_scissors.onRollOver=function() { setFloater("Split way at selected point (X)"); };
	_root.panel.i_scissors.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("gps","i_gps",36);
	with (_root.panel.i_gps) { _x=65; _y=93; };
	_root.panel.i_gps.onPress   =function() { loadGPS(); };
	_root.panel.i_gps.onRollOver=function() { setFloater("Show GPS tracks (G)"); };
	_root.panel.i_gps.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("prefs","i_prefs",37);
	with (_root.panel.i_prefs) { _x=90; _y=93; };
	_root.panel.i_prefs.onPress   =function() { openOptionsWindow(); };
	_root.panel.i_prefs.onRollOver=function() { setFloater("Set options (choose the map background)"); };
	_root.panel.i_prefs.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("newattr","i_newattr",33);
	with (_root.panel.i_newattr) { _x=690; _y=95; };
	_root.panel.i_newattr.onRelease =function() { _root.panel.properties.enterNewAttribute(); };
	_root.panel.i_newattr.onRollOver=function() { setFloater("Add a new tag"); };
	_root.panel.i_newattr.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("newrel","i_newrel",44);
	with (_root.panel.i_newrel) { _x=690; _y=75; backgroundColor=0xDDBBBB; background=true;};
	_root.panel.i_newrel.onRelease =function() { addToRelation(); };
	_root.panel.i_newrel.onRollOver=function() { setFloater("Add to a relation"); };
	_root.panel.i_newrel.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("repeatattr","i_repeatattr",34);
	with (_root.panel.i_repeatattr) { _x=690; _y=55; };
	_root.panel.i_repeatattr.onPress=function() { _root.panel.properties.repeatAttributes(); };
	_root.panel.i_repeatattr.onRollOver=function() { setFloater("Repeat tags from the previously selected way (R)"); };
	_root.panel.i_repeatattr.onRollOut =function() { clearFloater(); };

//	_root.panel.attachMovie("nextattr","i_nextattr",42);
//	with (_root.panel.i_nextattr) { _x=690; _y=25; };
//	_root.panel.i_nextattr.onRelease =function() { advancePropertyWindow(); };
//	_root.panel.i_nextattr.onRollOver=function() { setFloater("Next page of tags"); };
//	_root.panel.i_nextattr.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("exclamation","i_warning",35);
	with (_root.panel.i_warning) { _x=10; _y=45; _visible=false; };
	_root.panel.i_warning.onPress=function() { handleWarning(); };
	_root.panel.i_warning.onRollOver=function() { setFloater("An error occurred - click for details"); };
	_root.panel.i_warning.onRollOut =function() { clearFloater(); };
	wflashid=setInterval(function() { _root.panel.i_warning._alpha=150-_root.panel.i_warning._alpha; }, 750);

	_root.panel.attachMovie("rotation","i_direction",39);
	with (_root.panel.i_direction) { _x=40; _y=93; _rotation=-45; _visible=true; _alpha=50; };
	_root.panel.i_direction.onPress=function() { _root.ws.reverseWay(); };
	_root.panel.i_direction.onRollOver=function() { setFloater("Direction of way - click to reverse"); };
	_root.panel.i_direction.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("roundabout","i_circular",40);
	with (_root.panel.i_circular) { _x=40; _y=93; _rotation=-45; _visible=false; };
	_root.panel.i_circular.onRollOver=function() { setFloater("Circular way"); };
	_root.panel.i_circular.onRollOut =function() { clearFloater(); };

	_root.panel.attachMovie("padlock","padlock",41);
	with (_root.panel.padlock) { _y=32; _visible=false; };
	_root.panel.padlock.onPress=function() {
		if (_root.wayselected) {
			if (_root.ws.path.length>200 && _root.ws.oldversion==0) {
				setTooltip("too long to unlock:\nplease split into\nshorter ways");
			} else {
				_root.ws.locked=false;
				_root.ws.clean=false;
				_root.ws.redraw();
				_root.panel.padlock._visible=false;
				markClean(false);
			}
		} else if (_root.poiselected) {
			_root.map.pois[poiselected].locked=false;
			_root.map.pois[poiselected].clean=false;
//			_root.map.pois[poiselected].recolour();
			_root.panel.padlock._visible=false;
			markClean(false);
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
		multiline=true; wordWrap=true;  border=true; selectable = true; type = 'input';
		_visible=false;//#debug
	};

	_root.createTextField('coordmonitor',21,130,400,400,60); // 515
	with (_root.coordmonitor) {
		multiline=true; wordWrap=true; border=true; selectable = true; type = 'input';
		_visible=false;//#debug
	};

	_root.createTextField('floater',0xFFFFFF,15,30,200,18);
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

	// Text fields

//	populatePropertyWindow('');	

	_root.createTextField('waysloading',22,580,5,150,20);
	with (_root.waysloading) { text="loading ways"; setTextFormat(plainSmall); type='dynamic'; _visible=false; };

	_root.createTextField('tooltip',46,580,25,150,100);
	with (_root.tooltip  ) { text=""; setTextFormat(plainSmall); selectable=false; type='dynamic'; };

	_root.panel.createTextField('t_type',23,5,5,220,20);
	with (_root.panel.t_type	 ) { text="Welcome to OpenStreetMap"; setTextFormat(boldText); };
	
	_root.panel.createTextField('t_details',24,5,23,220,20);
	with (_root.panel.t_details) { text=signature; setTextFormat(plainText); };
	
//	// TextField listener
//	textfieldListener=new Object();
//	textfieldListener.onChanged=function() { textChanged(); };

	// MovieClip loader
	var tileLoader=new MovieClipLoader();
	tileListener=new Object();
	tileLoader.addListener(tileListener);

	// Interaction with responder script
	var loaderWaiting=false;

	remote=new NetConnection();
	remote.connect(apiurl);
	remote.onStatus=function(info) { 
		_root.panel.i_warning._visible=true;
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
		_root.relcolours=result[7];
		_root.relalphas=result[8];
		_root.relwidths=result[9];
//		_root.presetselected='road'; setPresetIcon(presetselected);
		_root.panel.i_preset._visible=false;
	};
	remote.call('getpresets',preresponder);

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


	// =====================================================================================
	// Start

	_root.panel.attachMovie("propwindow","properties",50);
	with (_root.panel.properties) { _x=110; _y=25; };

	_root.panel.attachMovie("presetmenu","presets",60);
	with (_root.panel.presets) { _x=110; _y=1; };

	updateButtons();
	updateScissors();
	resizeWindow();

	if (lat) { startPotlatch(); }			// Parse GPX if supplied
	if (gpx) { parseGPX(gpx); }				//  |
	if (lat) {} else { lat=0; long=51.5; startPotlatch(); } // London if none!

	// Welcome buttons

	_root.panel.createEmptyMovieClip("welcome",61);

	_root.panel.welcome.createEmptyMovieClip("start",1);
	drawButton(_root.panel.welcome.start,250,7,"Start","Start mapping with OpenStreetMap.");
	_root.panel.welcome.start.onPress=function() { removeMovieClip(_root.panel.welcome); };

	_root.panel.welcome.createEmptyMovieClip("play",2);
	drawButton(_root.panel.welcome.play,250,29,"Play","Practice mapping - your changes won't be saved.");
	_root.panel.welcome.play.onPress=function() {
		_root.sandbox=true; removeMovieClip(_root.panel.welcome);
		_root.createEmptyMovieClip("practice",62);
		with (_root.practice) {
			_x=Stage.width-97; _y=Stage.height-panelheight-22; beginFill(0xFF0000,100);
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

	_root.panel.welcome.createEmptyMovieClip("help",3);
	drawButton(_root.panel.welcome.help,250,51,"Help","Find out how to use Potlatch, this map editor.");
	_root.panel.welcome.help.onPress=function() { getUrl("http://wiki.openstreetmap.org/index.php/Potlatch","_blank"); };

	if (gpx) {
		_root.panel.welcome.createEmptyMovieClip("convert",4);
		drawButton(_root.panel.welcome.convert,250,73,"Track","Convert your GPS track to (locked) ways for editing.");
		_root.panel.welcome.convert.onPress=function() { removeMovieClip(_root.panel.welcome); gpxToWays(); };
	}

	// =====================================================================
	// Main start function

	function startPotlatch() {
		_root.urllat  =Number(lat);		// LL from query string
		_root.urllong =Number(long);	//  |
		_root.baselong=urllong-xradius/masterscale/bscale;
		_root.basey   =lat2y(urllat)+yradius/masterscale/bscale;
		_root.baselat =y2lat(basey);
		_root.ylat=baselat;	 _root.lastylat=ylat;		// current Yahoo state
		_root.ylon=baselong; _root.lastylon=ylon;		//  |
		updateCoords(0,0);
		updateLinks();
		setBackground(preferences.data.baselayer);
		whichWays();
		_root.onEnterFrame=function() { everyFrame(); };
	}

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
		if (Math.abs(_root.firstxmouse-_root._xmouse)>(tolerance*4) ||
			Math.abs(_root.firstymouse-_root._ymouse)>(tolerance*4) ||
			Key.isDown(Key.SPACE)) {
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
//		_root.map.onMouseMove=function() {};
//		_root.map.onMouseUp  =function() {};
		_root.onMouseMove=function() {};
		_root.onMouseUp  =function() {};
		if (Math.abs(_root.firstxmouse-_root._xmouse)>tolerance*4 ||
			Math.abs(_root.firstymouse-_root._ymouse)>tolerance*4) {
			redrawBackground();
			updateLinks();
			whichWays();
		}
		restartElastic();
		_root.dragmap=false;
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
//		_root.map.onMouseMove=function() { processMapDrag(); };
//		_root.map.onMouseUp  =function() { endMapDrag(); };
		_root.onMouseMove=function() { processMapDrag(); };
		_root.onMouseUp  =function() { endMapDrag(); };
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
					_root.panel.properties.init('way',getPanelColumns(),4);
					updateButtons();
					updateScissors(false);
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



	// =====================================================================
	// Tooltip and pointer functions

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

	



	// =====================================================================================
	// Keypress functions

	// keyPressed				- key listener

	function keyPressed() {
		var k=Key.getCode();
		_root.lastkeypressed=k;

		switch (keytarget) {
			case 'keyname':	 ;
			case 'value':	 if (_root.auto!=undefined) { _root.auto.keyRespond(k); }
							 else if (k==13) { autoEnter(); }
							 return; break;
			case 'dialogue': if (k==187 && _root.modal.box.properties!=undefined) {
								_root.modal.box.properties.enterNewAttribute();
							 };
							 return; break;
			case '':		 break;
			default:		 return; break;
		}

		if (k>48 && k<58 && (wayselected!=0 || poiselected!=0)) {
			if (presetnames[_root.panel.properties.proptype][_root.panel.presets.group][k-48]!=null) {
				_root.panel.presets.setAttributes(k-48);
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
			case 46:		;													// DELETE/backspace - delete way -- ode
			case 8:			if (Key.isDown(Key.SHIFT)) {						//  |
								if (_root.wayselected!=0) { _root.ws.removeWithConfirm(); }
							} else { keyDelete(1); }; break;					//  |
			case 13:		stopDrawing(); break;								// ENTER - stop drawing line
			case 27:		keyRevert(); break;									// ESCAPE - revert current way
			case 71:		loadGPS(); break;									// G - load GPS
			case 72:		if (_root.wayselected>0) { wayHistory(); }; break;	// H - way history
			case 82:		_root.panel.properties.repeatAttributes(); break;							// R - repeat attributes
			case 85:		getDeleted(); break;								// U - undelete
			case 88:		_root.ws.splitWay(); break;							// X - split way
			case Key.PGUP:	zoomIn(); break;									// Page Up - zoom in
			case Key.PGDN:	zoomOut(); break;									// Page Down - zoom out
			case Key.LEFT:  moveMap( 140,0); updateLinks(); redrawBackground(); whichWays(); break;	// cursor keys
			case Key.RIGHT: moveMap(-140,0); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case Key.DOWN:  moveMap(0,-100); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case Key.UP:    moveMap(0, 100); updateLinks(); redrawBackground(); whichWays(); break;	//  |
			case 192:		;													// '`' - cycle presets
			case 167:		_root.panel.presets.cycleIcon(); break;				// '¤' -  |
			case 107:		;													// '+' - add new attribute
			case 187:		_root.panel.properties.enterNewAttribute(); break;	//  |
			case 189:		keyDelete(0); break;								// '-' - delete node from this way only
			case 191:		cycleStacked(); break;								// '/' - cycle between stacked ways
			case 76:		showPosition(); break;								// L - show latitude/longitude
//			default:		_root.chat.text=Key.getCode()+" pressed";
		};
	}

	function showPosition() { setTooltip("lat "+Math.floor(coord2lat (_root.map._ymouse)*10000)/10000
									  +"\nlon "+Math.floor(coord2long(_root.map._xmouse)*10000)/10000,0); }
	function startCount()	{ z=new Date(); _root.startTime=z.getTime(); }
	function endCount(id)	{ z=new Date(); zz=Math.floor((z.getTime()-_root.startTime)*100);
							if (zz>100) { _root.chat.text+=id+":"+zz+";"; } }

	function keyDelete(doall) {
		if (_root.poiselected) {
			// delete POI
			_root.map.pois[poiselected].remove();
		} else if (_root.drawpoint>-1) {
			// delete most recently drawn point
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
			// ** should be moved into way class
			if (doall==1) {
				// remove node from all ways
				id=_root.ws.path[_root.pointselected][2];
				for (qway in _root.map.ways) {
					var qdirty=false;
					for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
						if (_root.map.ways[qway].path[qs][2]==id) {
							_root.map.ways[qway].path.splice(qs,1);
							qdirty=true;
						}
					}
					if (qdirty) { _root.map.ways[qway].removeDuplicates(); }
					if (qdirty && _root.map.ways[qway]["path"].length<2) {
						_root.map.ways[qway].remove();
					} else if (qdirty) {
						_root.map.ways[qway].redraw();
						_root.map.ways[qway].clean=false;
					}
				}
			} else {
				// remove node from this way only
				_root.ws.path.splice(pointselected,1);
				_root.ws.removeDuplicates();
				if (_root.ws.path.length<2) {
					_root.ws.remove();
				} else {
					_root.ws.redraw();
					_root.ws.clean=false;
				}
			}
			_root.pointselected=-2;
			_root.drawpoint=-1;
			_root.map.elastic.clear();
			clearTooltip();
			markClean(false);
			if (_root.wayselected) {
				_root.ws.select();
			}
		}
	};

	function keyRevert() {
		if		(_root.wayselected<0) { stopDrawing();
										removeMovieClip(_root.map.areas[wayselected]);
										removeMovieClip(_root.ws); }
		else if	(_root.wayselected>0) {	stopDrawing();
										_root.ws.reload(); }
		else if (_root.poiselected>0) { _root.map.pois[poiselected].reload(); }
		else if (_root.poiselected<0) { removeMovieClip(_root.map.pois[poiselected]); }
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
			_root.panel.i_warning._visible=false;
			for (qway in _root.map.ways) {
				if (_root.map.ways[qway].uploading) {
					_root.map.ways[qway].uploading=false;
					_root.map.ways[qway].upload();
				}
			}
		}
	};



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
			} else if (!_root.yahoorightsize) {
				_root.yahoorightsize=true;
				_root.yahooresizer=setInterval(resizeWindow,1000);
			}
		}

		// ----	Do we need to redraw the property window?
		if (_root.redopropertywindow) {
			_root.redopropertywindow.reinit();
			_root.redopropertywindow=null;
		}

		// ---- If resizing has stopped, issue new whichways
		//		and resize panel
		var t=new Date();
		if ((t.getTime()-lastresize.getTime())>500) {
			if (lastresize.getTime()>lastwhichways.getTime()) {
				whichWays();
			}
		}

		// ----	Control "loading ways" display
		_root.waysloading._visible=(_root.waysrequested!=_root.waysreceived) || (_root.whichrequested!=_root.whichreceived);

		// ---- Service tile queue
		if (preferences.data.baselayer!=0 &&
			preferences.data.baselayer!=2 ) { serviceTileQueue(); }
			
		// ----	Reinstate focus if lost after click event
		if (_root.reinstatefocus) {
_root.coordmonitor.text+="!";
			Selection.setFocus(_root.reinstatefocus); 
			_root.reinstatefocus=null;
		}
	}
	

	// Options window
	
	function openOptionsWindow() {
		createModalDialogue(290,110,new Array('Ok'),function() { preferences.flush(); } );
		_root.modal.box.createTextField("prompt1",2,7,9,80,20);
		writeText(_root.modal.box.prompt1,"Background:");

		_root.modal.box.attachMovie("menu","background",6);
		_root.modal.box.background.init(87,10,preferences.data.baselayer,
			new Array("None","Aerial - OpenAerialMap","Aerial - Yahoo!","OSM - Mapnik","OSM - Osmarender","OSM - Maplint (errors)","OSM - cycle map","Other - out-of-copyright map","Other - OpenTopoMap"),
			'Choose the background to display',setBackground,null,0);

		_root.modal.box.attachMovie("checkbox","pointer",5);
		_root.modal.box.pointer.init(10,40,"Fade background",preferences.data.dimbackground,function(n) { preferences.data.dimbackground=n; redrawBackground(); });

		_root.modal.box.attachMovie("checkbox","pointer",4);
		_root.modal.box.pointer.init(10,60,"Use pen and hand pointers",preferences.data.custompointer,function(n) { preferences.data.custompointer=n; });
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
		_root.panel.i_circular._visible=false;
		_root.panel.i_direction._visible=true;
		_root.panel.i_direction._alpha=50;
		clearTooltip();
		setTypeText("","");
		_root.panel.padlock._visible=false;

		_root.panel.properties.init('');
		_root.panel.presets.hide();
		updateButtons();
		updateScissors(false);
		poiselected=0;
		pointselected=-2;
		selectWay(0);
		markClean(true);
	};
	
	// uploadSelected
	
	function uploadSelected() {
		if (_root.wayselected!=0 && !_root.ws.clean) {
			for (qway in _root.map.ways) {
				if (!_root.map.ways[qway].clean) {
					_root.map.ways[qway].upload();
				}
			}
		}
		if (_root.poiselected!=0 && !_root.map.pois[poiselected].clean) {
			_root.map.pois[poiselected].upload();
		}
		uploadDirtyRelations();
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
