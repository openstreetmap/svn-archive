#!/usr/bin/perl -w

	# ----------------------------------------------------------------
	# potlatch.cgi
	# Flash editor for Openstreetmap

	# editions Systeme D / Richard Fairhurst 2006-7
	# public domain

	# last update 24.4.2007 (pannable when drawing line, dimming, pointers, bugfixes)
	# next steps: 
	#	make resizable (change getgps code too)
	#	check database code now that schema has been changed for 0.4
	#	add see-through panel behind top-right hints (line 1573)

	# You may do what you like with this file, but please think very
	# carefully before adding dependencies or complicating the user
	# interface. Thank you!
	# ----------------------------------------------------------------

	use SWF qw(:ALL);
	use SWF::Constants qw(:Button);
	$fontroot ='Helmet';

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

	_root.createEmptyMovieClip("map",10);
	_root.map.setMask(_root.masksquare);

	// Get variables from browser (forcing them into numbers where appropriate)
	// London 51.5,0; Weybridge 51.4,-0.5; Worcester 52.2,-2.25; Woodstock 51.85,-1.35
	var baselat=Math.pow(lat,1);
	var baselong=Math.pow(long,1);
	var scale=Math.pow(scale,1);
	var usertoken=token;

	// Global dimensions
	var uwidth=700;
	var uheight=600;

	// Key listener - needs to be initialised before Yahoo
	keyListener=new Object();
	keyListener.onKeyDown=function() { keyPressed(); };
	Key.addListener(keyListener);

	// Mouse listener - copes with custom pointers
	mouseListener=new Object();
	mouseListener.onMouseMove=function() { trackMouse(); };

	// Initialise Yahoo
	var ylat=baselat;	var lastylat=ylat;
	var ylon=baselong;	var lastylon=ylon;
	var yzoom=8;		var lastyzoom=yzoom;

	_root.createEmptyMovieClip("yahoo",7);
	loadMovie("/potlatch/ymap.swf",_root.yahoo);
	_root.yahoo.swapDepths(_root.masksquare);
	_root.yahoo.setMask(_root.masksquare2);

	// Main initialisation
	_root.map.createEmptyMovieClip("ways"    ,10); var waydepth=1;
	_root.map.createEmptyMovieClip("feedback",5003); // feedback line

	_root.masksquare.useHandCursor=false;
	_root.masksquare.onPress   =function() { mapClick(); };
	_root.masksquare.onRollOver=function() { mapRollOver(); };
	_root.masksquare.onRollOut =function() { mapRollOut(); };
	_root.map.onMouseUp        =function() { mouseRelease(); };

	var minscale=11;				// don't zoom out past this
	var maxscale=18;				// don't zoom in past this
	var basey=lat2y(baselat);		// Y co-ordinate of map centre
	var masterscale=5825.4222222222;// master map scale - how many Flash pixels in 1 degree longitude
									// (for Landsat, 5120)
	var wayselected=0;				// way currently selected (or 0)
	var pointselected=-2;			// node currently selected (or -2)
	var waycount=0;					// number of ways currently loaded
	var waysrequested=0;			// total number of ways requested
	var waysreceived=0;				// total number of ways received
	var whichrequested=0;			// total number of 'which ways' requested
	var whichreceived=0;			// total number of 'which ways' received
	var dragmap=-1;					// map not currently being dragged
	var dragpoint=-1;				// no point currently being dragged
	var drawpoint=-1;				// no point currently being drawn
	var newwayid=-1;				// new way ID (for those not yet saved)
	var newnodeid=-2;				// new node ID (for those not yet saved)
	var currentproptype='';			// type of property currently being edited
	var pointertype='';				// current mouse pointer
	var redopropertywindow=0;		// need to redraw property window after deletion?
	var savedProperties=new Array();// clipboard for properties
	setBackground(2);				// base layer: 0 none, 1/2 Yahoo
	
	// Way styling
	// ** should be moved into presets file
	colours=new Array();
	colours["motorway"		]=0x3366CC; 
	colours["motorway_link"	]=0x3366CC;	
	colours["trunk"			]=0x007700;	// primary A road
	colours["primary"		]=0x770000;	// non-primary A road
	colours["secondary"		]=0xCC6600;	// B road
	colours["footway"		]=0xFF6644;	
	colours["cycleway"		]=0xFF6644;	
	colours["bridleway"		]=0xFF6644;	
	colours["rail"			]=0x000001;	
	colours["river"			]=0x8888FF;	
	colours["canal"			]=0x8888FF;	

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
	with (_root.i_gps) { _x=40; _y=583; };
	_root.i_gps.onPress   =function() { loadGPS(); };
	_root.i_gps.onRollOver=function() { setFloater("Show GPS tracks (G)"); };
	_root.i_gps.onRollOut =function() { clearFloater(); };

	_root.attachMovie("prefs","i_prefs",37);
	with (_root.i_prefs) { _x=65; _y=583; };
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

//	_root.attachMovie("nextattr","i_nextattr",35);
//	with (_root.i_nextattr) { _x=690; _y=545; };
//	_root.i_nextattr.onPress=function() { nextAttributes(); };

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
		text="HERE";
	};

	_root.createTextField('floater',0xFFFFFF,15,30,200,17);
	with (floater) {
		embedFonts=true;
		background=true;
		backgroundColor=0xFFEEEE;
		border=true;
		borderColor=0xAAAAAA;
		selectable = false; _visible=false;
	}
	var floaterID=null;


	// Text formats
	
	plainText =new TextFormat(); plainText.color =0x000000; plainText.size =14; plainText.font ="$fontroot";
	plainSmall=new TextFormat(); plainSmall.color=0x000000;	plainSmall.size=12; plainSmall.font="$fontroot";
	greySmall =new TextFormat(); greySmall.color =0x888888;	greySmall.size =12; greySmall.font ="$fontroot";
	boldText  =new TextFormat(); boldText.color  =0x000000; boldText.size  =14; boldText.font  ="$fontroot"; boldText.italic =true;	// italics and bold are wrong way round!
	boldSmall =new TextFormat(); boldSmall.color =0x000000; boldSmall.size =12; boldSmall.font ="$fontroot"; boldSmall.italic=true;	//  |
	boldWhite =new TextFormat(); boldWhite.color =0xFFFFFF; boldWhite.size =12; boldWhite.font ="$fontroot"; boldWhite.italic=true;	//  |
	menu_on	  =new TextFormat(); menu_on.color   =0x000000; menu_on.size   =12; menu_on.font   ="$fontroot"; menu_on.italic  =true;	//  |
	menu_off  =new TextFormat(); menu_off.color  =0xFFFFFF; menu_off.size  =12; menu_off.font  ="$fontroot"; menu_off.italic =true;	//  |

	// Text fields

	populatePropertyWindow('');	

	_root.createTextField('waysloading',22,580,5,150,20);
	with (_root.waysloading) { embedFonts=true; text="loading ways"; setTextFormat(plainSmall); type='dynamic'; _visible=false; };

	_root.createTextField('tooltip',41,580,25,150,100);
	with (_root.tooltip  ) { embedFonts=true; text=""; setTextFormat(plainSmall); selectable=false; type='dynamic'; };

	_root.createTextField('t_type',23,5,505,220,20);
	with (_root.t_type	 ) { embedFonts=true; text="Welcome to OpenStreetMap"; setTextFormat(boldText); };
	
	_root.createTextField('t_details',24,5,523,220,20);
	with (_root.t_details) { embedFonts=true; text="Potlatch v0.1"; setTextFormat(plainText); };
	
	_root.createEmptyMovieClip("properties",50);
	with (_root.properties) { _x=110; _y=525; }; // 110,505

	// TextField listener
	textfieldListener=new Object();
	textfieldListener.onChanged=function() { textChanged(); };

	// Interaction with responder script
	var loaderWaiting=false;

	remote=new NetConnection();
	remote.connect("../api/0.4/amf");
//	remote.connect("rubyamf.cgi");

	preresponder = function() { };
	preresponder.onResult = function(result) {
		_root.presets=result[0];
		_root.presetmenus=result[1];
		_root.presetnames=result[2];
		_root.presetselected='road'; setPresetIcon(presetselected);
		_root.i_preset._visible=false;
	};
	remote.call('getpresets',preresponder);

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
		if (this._name==_root.drawpoint) {
			_root.drawpoint=-2;
			// flag to signal 'stop drawing' when button released
		} else if (Key.isDown(Key.SHIFT)) {
			startNewWay(_root.map.ways[this.way].path[this._name][0],
						_root.map.ways[this.way].path[this._name][1],this.node);
		} else {
			_root.lastxmouse=_root._xmouse;
			_root.lastymouse=_root._ymouse;
			_root.dragpoint=this._name;
			_root.pointselected=this._name;
			_root.map.ways[this.way].highlight();
			setTypeText("Point",this.node);
			populatePropertyWindow('point');
			setTooltip("point selected\n(shift-click point to\nstart new line)",0);
		}
	};

	AnchorPoint.prototype.onMouseMove=function() {
		if (_root.dragpoint==this._name) {
			this._x=_root.map._xmouse;
			this._y=_root.map._ymouse;
			_root.lastxmouse=_root._xmouse;
			_root.lastymouse=_root._ymouse;
		}
		if (_root.drawpoint==this._name) {
			_root.map.feedback.clear();
			_root.map.feedback.lineStyle(3,0x000000,100,false,"none");
			_root.map.feedback.moveTo(_root.map._xmouse,_root.map._ymouse);
			_root.map.feedback.lineTo(this._x,this._y);
		}
	};

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
		if (Key.isDown(Key.SHIFT)) {
			// Merge ways
			if (this._name==0 || this._name==_root.map.ways[this.way].path.length-1) {
				_root.map.ways[wayselected].path[_root.drawpoint][3]=1;
				_root.map.ways[this.way].path[0][3]=1;
				// add from start or end of connecting way?
				if (this._name==0) {
					for (i=0; i<_root.map.ways[this.way].path.length; i+=1) {
						newpoint=new Array(_root.map.ways[this.way].path[i][0],
										   _root.map.ways[this.way].path[i][1],
										   _root.map.ways[this.way].path[i][2],
										   _root.map.ways[this.way].path[i][3],new Array(),0);
						if (_root.drawpoint==0) { _root.map.ways[wayselected].path.unshift(newpoint); }
										   else { _root.map.ways[wayselected].path.push(newpoint);    }
					}
				} else {
					p=1;
					for (i=_root.map.ways[this.way].path.length-1; i>=0; i-=1) {
						newpoint=new Array(_root.map.ways[this.way].path[i][0],
										   _root.map.ways[this.way].path[i][1],
										   _root.map.ways[this.way].path[i][2],
										   p,new Array(),0);
						p=_root.map.ways[this.way].path[i][3];
						if (_root.drawpoint==0) { _root.map.ways[wayselected].path.unshift(newpoint); }
										   else { _root.map.ways[wayselected].path.push(newpoint);    }
					}
				}
				_root.map.ways[wayselected].path[0][3]=0;	// first point always a 'move'
				// merge attributes
				z=_root.map.ways[this.way].attr;
				for (i in z) {
					if (_root.map.ways[wayselected].attr[i].substr(0,6)=='(type ') { _root.map.ways[wayselected].attr[i]=null; }
					if (z[i].substr(0,6)=='(type ') { z[i]=null; }
					
					if (_root.map.ways[wayselected].attr[i]!=null) {
						if (_root.map.ways[wayselected].attr[i]!=z[i]) { _root.map.ways[wayselected].attr[i]+='; '+z[i]; }
					} else {
						_root.map.ways[wayselected].attr[i]=z[i];
					}
				}
				_root.map.ways[this.way].remove();
				removeMovieClip(_root.map.anchorhints);
				_root.map.ways[wayselected].clean=0;
				_root.map.ways[wayselected].redraw();
				_root.map.ways[wayselected].select();
				clearTooltip();
				setTypeText("Way",wayselected);
				populatePropertyWindow('way');
				_root.map.feedback.clear();
				_root.drawpoint=-1;
			}
		} else { addEndPoint(_root.map._xmouse,_root.map._ymouse,this.node); }
	};
	Object.registerClass("anchorhint",AnchorHint);


	// =====================================================================================
	// OOP classes - OSMWay

	// ----	Initialise
	
	function OSMWay() {
		this.path=new Array();
		// path is an array of points
		// each point is an array: (x,y,node_id,0 move|1 draw,tag array,segment id)
		this.attr=new Array();
		this.clean=1;
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
			_root["map"]["ways"][result[0]].path=result[1];
			_root["map"]["ways"][result[0]].attr=result[2];
			_root["map"]["ways"][result[0]].xmin=result[3];
			_root["map"]["ways"][result[0]].xmax=result[4];
			_root["map"]["ways"][result[0]].ymin=result[5];
			_root["map"]["ways"][result[0]].ymax=result[6];
			_root["map"]["ways"][result[0]].redraw();
			_root.waysreceived+=1;
		};
		remote.call('getway',responder,this._name,wayid,baselong,basey,masterscale);
	};

	// ----	Draw line

	OSMWay.prototype.redraw=function() {
		this.createEmptyMovieClip("line",1);					// clear line
		linewidth=3; //Math.max(2/Math.pow(2,_root.scale-12),0)+1;
		if (colours[this.attr["highway"]]) {
			this.line.lineStyle(linewidth,colours[this.attr["highway" ]],100,false,"none");
		} else if (colours[this.attr["waterway"]]) {
			this.line.lineStyle(linewidth,colours[this.attr["waterway"]],100,false,"none");
		} else if (colours[this.attr["railway"]]) {
			this.line.lineStyle(linewidth,colours[this.attr["railway" ]],100,false,"none");
		} else {
			c=0xCCCCCC; z=this.attr;
			for (i in z) { if (i!='created_by' && this.attr[i]!='' && this.attr[i].substr(0,6)!='(type ') { c=0x777777; } }
			this.line.lineStyle(linewidth,c,100,false,"none");
		}
		for (i=0; i<this.path.length; i+=1) {
			if (this.path[i][3]==0) {
				this.line.moveTo(this.path[i][0],this.path[i][1]);
			} else {
				this.line.lineTo(this.path[i][0],this.path[i][1]);	// draw line
			}
		}
	};

	// ----	Remove from server
	
	OSMWay.prototype.remove=function() {
		if (this._name>=0) {
			deleteresponder = function() { };
			deleteresponder.onResult = function(result) {
				if (wayselected==result) { deselectAll(); }
				removeMovieClip(_root.map.ways[result]);
			};
			remote.call('deleteway',deleteresponder,_root.usertoken,this._name);
		} else {
			if (this._name==wayselected) { stopDrawing(); deselectAll(); }
			removeMovieClip(this);
		}
	};

	// ----	Upload to server
	
	OSMWay.prototype.upload=function() {
		putresponder=function() { };
		putresponder.onResult=function(result) {
			nw=result[1];	// new way ID
			if (result[0]!=nw) {
				_root.map.ways[result[0]]._name=nw;
				if (_root.t_details.text==result[0]) { _root.t_details.text=nw; }
				if (wayselected==result[0]) { wayselected=nw; }
			}
			_root.map.ways[nw].xmin=result[3];
			_root.map.ways[nw].xmax=result[4];
			_root.map.ways[nw].ymin=result[5];
			_root.map.ways[nw].ymax=result[6];

			// check if renumbered nodes occur in any other ways
			for (qway in _root.map.ways) {
				for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
					if (result[2][_root.map.ways[qway].path[qs][2]]) {
						_root.map.ways[qway].path[qs][2]=result[2][_root.map.ways[qway].path[qs][2]];
						if (qway!=nw) { _root.map.ways[qway].clean=0; }
					}
				}
				if (!_root.map.ways[qway].clean) {
					_root.map.ways[qway].upload();
				}
			}
		};
		this.attr['created_by']="Potlatch alpha";
		remote.call('putway',putresponder,_root.usertoken,this._name,this.path,this.attr,baselong,basey,masterscale);
		_root.map.ways[this._name].clean=1;
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
		if (Key.isDown(Key.SHIFT) && this._name==_root.wayselected) {
			// shift-click current way: insert point
			insertAnchorPoint(this._name);
			this.highlightPoints(5000,"anchor");
		} else if (_root.drawpoint>-1) {
			// click other way while drawing: insert point as junction
			insertAnchorPoint(this._name);
			this.highlightPoints(5001,"anchorhint");
			addEndPoint(_root.map._xmouse,_root.map._ymouse,newnodeid);
		} else {
			// click way: select
			this.select();
			clearTooltip();
			setTypeText("Way",this._name);
			populatePropertyWindow('way');
		}
	};
	
	OSMWay.prototype.select=function() {
		if (_root.wayselected!=this._name) { uploadSelected(); }
		_root.wayselected=this._name;
		_root.pointselected=-2;
		this.highlightPoints(5000,"anchor");
		removeMovieClip(_root.map.anchorhints);
		this.highlight();
	};
	
	OSMWay.prototype.highlight=function() {
		_root.map.createEmptyMovieClip("highlight",5);
		if (_root.pointselected>-2) {
			xd=8/Math.pow(2,Math.min(_root.scale,16)-12);
			_root.map.highlight._x=_root.map.anchors[pointselected]._x;
			_root.map.highlight._y=_root.map.anchors[pointselected]._y;
			_root.map.highlight.beginFill(0xFFFF00,80);
			_root.map.highlight.moveTo(-xd,xd);
			_root.map.highlight.lineTo(xd,xd);
			_root.map.highlight.lineTo(xd,-xd);
			_root.map.highlight.lineTo(-xd,-xd);
			_root.map.highlight.lineTo(-xd,xd);
			_root.map.highlight.endFill();
		} else {
			linewidth=11;
			_root.map.highlight.lineStyle(linewidth,0xFFFF00,80,false,"none");
			for (i=0; i<this.path.length; i+=1) {
				if (this.path[i][3]==0) {
					_root.map.highlight.moveTo(this.path[i][0],this.path[i][1]);
				} else {
					_root.map.highlight.lineTo(this.path[i][0],this.path[i][1]);	// draw line
				}
			}
		}
	};

	OSMWay.prototype.highlightPoints=function(d,atype) {
		anchorsize=120/Math.pow(2,Math.min(_root.scale,16)-12);
		group=atype+"s";
		_root.map.createEmptyMovieClip(group,d);
		for (i=0; i<this.path.length; i+=1) {
			_root.map[group].attachMovie(atype,i,i);
			_root.map[group][i]._x=this.path[i][0];
			_root.map[group][i]._y=this.path[i][1];
			_root.map[group][i]._xscale=anchorsize;
			_root.map[group][i]._yscale=anchorsize;
			_root.map[group][i].node=this.path[i][2];
			_root.map[group][i].way=this._name;
		}
	};

	OSMWay.prototype.splitWay=function() {
		if (pointselected>0 && pointselected<(this.path.length-1)) {
			_root.newwayid--;											// create new way
			_root.map.ways.attachMovie("way",newwayid,++waydepth);		//  |

			z=this.path;												// copy path array
			for (i in z) {												//  | (deep copy)
				_root.map.ways[newwayid].path[i]=new Array();			//  |
				for (j=0; j<=5; j+=1) { _root.map.ways[newwayid].path[i][j]=this.path[i][j]; }
			}															// | 

			z=this.attr; for (i in z) { _root.map.ways[newwayid].attr[i]=z[i]; }

			this.path.splice(Math.floor(pointselected)+1);				// current way
			this.redraw();												//  | (Math.floor forces
			this.clean=0;												//  |  string->number)

			_root.map.ways[newwayid].path.splice(0,pointselected);		// new way
			_root.map.ways[newwayid].path[0][3]=0;						//  | first point is 'move'
			_root.map.ways[newwayid].redraw();							//  |
			_root.map.ways[newwayid].upload();							//  |

			pointselected=-2;
			setTypeText("Way",_root.wayselected);
			populatePropertyWindow('way');
			this.select();
		};
	};


	Object.registerClass("way",OSMWay);

	// =====================================================================================
	// Support functions

	function keyDelete(doall) {
		if (_root.pointselected>-2) {
			if (doall==1) {
				// remove node from all ways
				id=_root.map.ways[_root.wayselected].path[_root.pointselected][2];
				for (qway in _root.map.ways) {
					qdirty=0;
					for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
						if (_root.map.ways[qway].path[qs][2]==id) {
							_root.map.ways[qway].path.splice(qs,1);
							if (qs<_root.map.ways[qway].path.length) { _root.map.ways[qway].path[qs][5]=0; }	// old segment IDs no longer valid
							qdirty=1;
						}
					}
					if (qdirty && _root.map.ways[qway]["path"].length<2) {
						_root.map.ways[qway].remove();
					} else if (qdirty) {
						_root.map.ways[qway].path[0][3]=0;	// make sure first point is a 'move'
						_root.map.ways[qway].redraw();
						_root.map.ways[qway].clean=0;
					}
				}
			} else {
				// remove node from this way only
				_root.map.ways[wayselected].path.splice(pointselected,1);
				if (pointselected<_root.map.ways[qway].path.length) { _root.map.ways[qway].path[pointselected][5]=0; }
				if (_root.map.ways[wayselected].path.length<2) {
					_root.map.ways[wayselected].remove();
				} else {
					_root.map.ways[wayselected].path[0][3]=0;
					_root.map.ways[wayselected].redraw();
					_root.map.ways[wayselected].clean=0;
				}
			}
			_root.pointselected=-2;
			_root.drawpoint=-1;
			_root.map.feedback.clear();
			clearTooltip();
			if (_root.wayselected) {
				_root.map.ways[_root.wayselected].select();
				setTypeText("Way",_root.wayselected);
				populatePropertyWindow('way');
			}
		}
	};

	function stopDrawing() {
		_root.drawpoint=-1;
		if (_root.map.ways[wayselected].path.length<=1) { 
			// way not long enough, so abort
			removeMovieClip(_root.map.ways[wayselected]);
			removeMovieClip(_root.map.anchors);
		}
		_root.map.feedback.clear();
		clearTooltip();
	};

	function revertWay() {
		if (_root.wayselected>0) {
			stopDrawing();
			_root.waysrequested+=1;
			_root.map.ways[wayselected].load(wayselected);
		} else if (_root.wayselected<0) {
			stopDrawing();
			removeMovieClip(_root.map.ways[wayselected]);
		}
		deselectAll();
	};

	function deselectAll() {
		_root.map.createEmptyMovieClip("anchors",5000); 
		wayselected=0;
		pointselected=-2;
		removeMovieClip(_root.map.highlight);
		clearTooltip();
		setTypeText("","");
		populatePropertyWindow('');
		_root.presetmenu._visible=false;
		_root.i_preset._visible=false;
		clearPropertyWindow();
	};
	
	function uploadSelected() {
		if (_root.wayselected!=0 && _root.map.ways[wayselected].clean==0) {
			for (qway in _root.map.ways) {
				if (_root.map.ways[qway].clean==0) {
					_root.map.ways[qway].upload();
				}
			}
		}
	};


	// =====================================================================================
	// Standard UI

	// Pop-up menu
	// UIMenu.init(x,y,selected option,array of options,tooltip,
	//			   function to call on close,width)
	
	function UIMenu() {
	};
	UIMenu.prototype=new MovieClip();
	UIMenu.prototype.init=function(x,y,selected,options,tooltip,closefunction,menuwidth) {
		this._x=x; this._y=y;
		this.selected=selected; this.original=selected;
		this.options=options;

		// create (invisible) movieclip for opened menu
		this.createEmptyMovieClip("opened",2);
		this.opened._visible=false;
		// create child for each option
		this.tw=0;
		for (i=0; i<options.length; i+=1) {
			this.opened.createTextField(i,i+1,3,i*15+1,100,15);
			this.opened[i].text=options[i];
			this.opened[i].background=true;
			this.opened[i].backgroundColor=0x888888;
			this.opened[i].embedFonts=true;
			this.opened[i].setTextFormat(menu_off);
			if (this.opened[i].textWidth*1.05>this.tw) { this.tw=this.opened[i].textWidth*1.05; }
		};
		for (i=0; i<options.length; i+=1) {
			this.opened[i]._width=this.tw;
		}
		// create box around menu
		this.opened.createEmptyMovieClip("box",0);
		w=this.tw+7;
		h=options.length*15+5;
		with (this.opened.box) {
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
		this.closed.createTextField("current",2,3,0,this.tw,17);
		this.closed.current.text=options[selected];
		this.closed.current.embedFonts=true;
		this.closed.current.setTextFormat(menu_off);
		this.closed.current.background=true;
		this.closed.current.backgroundColor=0x888888;

		this.onPress=function() { clearFloater(); this.openMenu(); };
		this.onRelease=function() { this.closeMenu(); };
		this.onReleaseOutside=function() { this.closeMenu(); };
		this.onMouseMove=function() { this.trackMenu(); };
		this.doOnClose=closefunction;
		this.opened[this.selected].backgroundColor=0xDDDDDD;
		this.opened[this.selected].embedFonts=true;
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
	


	// =====================================================================================
	// Start

	_root.attachMovie("menu","presetmenu",60);
	_root.presetmenu.init(141,505,1,presetnames['way'][presetselected],'Choose from a menu of preset attributes describing the way',setAttributesFromPreset,151);
	_root.presetmenu._visible=false;

	redrawMap(_root.map._x,_root.map._y);
	redrawYahoo();
	whichWays();
	_root.onEnterFrame=function() { everyFrame(); };



	// =====================================================================================
	// Options window
	
	function createModalDialogue(w,h) {
		clearFloater();
		_root.createEmptyMovieClip("modal",0xFFFFFE);
		ox=(uwidth-w)/2; oy=(uheight-100-h)/2;	// -100 for visual appeal

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
		// Create 'Ok' button
		_root.modal.box.createEmptyMovieClip("ok",1);
		with (_root.modal.box.ok) {
			_x=w-60; _y=h-30;
			beginFill(0x7F7F7F,100);
			moveTo(0,0);
			lineTo(50,0); lineTo(50,17);
			lineTo(0,17); lineTo(0,0); endFill();
		}
		_root.modal.box.ok.onPress=function() { clearModalDialogue(); };
		_root.modal.box.ok.useHandCursor=true;

		_root.modal.box.ok.createTextField("oktext",1,14,0,40,20);
		with (_root.modal.box.ok.oktext) {
			embedFonts=true; text="Ok"; setTextFormat(boldWhite);
			selectable=false; type='dynamic';
		}
	}
	
	function clearModalDialogue() {
		_root.createEmptyMovieClip("modal",0xFFFFFE);
		Key.addListener(keyListener);
	}

	function openOptionsWindow() {
		createModalDialogue(270,70);
		_root.modal.box.createTextField("prompt1",2,10,10,80,20);
		with (_root.modal.box.prompt1) {
			embedFonts=true; text="Background:"; setTextFormat(plainSmall);
			selectable=false; type='dynamic';
		}
		_root.modal.box.attachMovie("menu","background",3);
		_root.modal.box.background.init(90,10,_root.baselayer,
			new Array("None","Yahoo! satellite","Yahoo! satellite (dimmed)"),
			'Choose the background to display',setBackground,0);
	}

	function setBackground(n) {
		_root.baselayer=n;
		switch (n) {
			case 1: _root.yahoo._alpha=100; redrawYahoo(); break;
			case 2: _root.yahoo._alpha=50 ; redrawYahoo(); break;
		}
		redrawYahoo(); 
	}

	// =====================================================================================
	// Property window functions

	// KeyValue object

	function KeyValue() {
		this._x=_root.propx;
		this._y=_root.propy;
		_root.propy+=18; if (_root.propy>54) { _root.propy=0; _root.propx+=190; }

		this.createTextField('keyname',1,0,0,70,17);
		with (this.keyname) {
			embedFonts=true;
			backgroundColor=0xBBBBBB;
			background=true;
			text=this._name;
			setTextFormat(boldSmall);
			setNewTextFormat(boldSmall);
		};

		this.createTextField('value',2,72,0,110,17);
		this.value.onSetFocus =function() { if (this.textColor==0x888888) { this.text=''; this.textColor=0x000000; }
											this.addListener(textfieldListener); Key.removeListener(keyListener); _root.elselected=this._name;
											_root.map.ways[_root.wayselected].clean=0; };
		this.value.onKillFocus=function() { this.removeListener(textfieldListener); Key.addListener(keyListener); 
											if (this.text=='') { _root.redopropertywindow=1; } // crashes player if called directly!
											if (_root.currentproptype=='way') { _root.map.ways[wayselected].redraw(); }
											reflectPresets(); };
		with (this.value) {
			this.value.embedFonts=true;
			this.value.backgroundColor=0xDDDDDD;
			this.value.background=true;
			this.value.type='input';
			this.value.tabIndex=_root.propn;
		};
		if (_root.currentproptype=='point') {
			this.value.variable="_root.map.ways."+wayselected+".path."+pointselected+".4."+this._name;
			this.value.text=_root.map.ways[_root.wayselected].path[_root.pointselected][4][this._name];
		} else {
			this.value.variable="_root.map.ways."+wayselected+".attr."+this._name;
			this.value.text=_root.map.ways[wayselected].attr[this._name];
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
		if (proptype=='point') { proparr=_root.map.ways[wayselected].path[pointselected][4]; }
						  else { proparr=_root.map.ways[wayselected].attr; }
		_root.currentproptype=proptype;
		_root.currentproppoint=pointselected;
		_root.currentpropway=wayselected;
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
	};

	function clearPropertyWindow() {
		_root.propx=0; _root.propy=0; _root.propn=0;
		ct=0;
		for (el in _root.properties) {
			ct+=1;
			removeMovieClip(_root.properties[el]);
		}
		if (ct>0) { _root.savedpoint=_root.currentproppoint;
					_root.savedway  =_root.currentpropway;
					_root.savedtype =_root.currentproptype; }
	};

	// setTypeText - set contents of type window
	
	function setTypeText(a,b) {
		_root.t_type.text=a; _root.t_type.setTextFormat(boldText);
		_root.t_details.text=b; _root.t_details.setTextFormat(plainText);
	}

	// reflectPresets - set preset menu based on way values
	// looks in presetselected first, then in all other menus

	function reflectPresets() {
		found=findPresetInMenu(presetselected);
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
					if (currentproptype=='way') { cvalue=_root.map.ways[wayselected].attr[pkey]; }
											else { cvalue=_root.map.ways[wayselected].path[pointselected][4][pkey]; }
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
			if (currentproptype=='way') {
				if    (_root.map.ways[wayselected].attr[pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
				else { _root.map.ways[wayselected].attr[pkey]=presets[pname][pkey];	}
			} else {
				if    (_root.map.ways[wayselected].path[pointselected][4][pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
				else { _root.map.ways[wayselected].path[pointselected][4][pkey]=presets[pname][pkey]; }
			}
		}
		populatePropertyWindow(currentproptype);
		_root.map.ways[wayselected].redraw();
		_root.map.ways[wayselected].clean=0;
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
		if (_root.wayselected==0 && _root.pointselected==-2) { return; }
		if (_root.propn==12) { return; }
		if (_root.currentproptype=='point') {
			_root.map.ways[_root.wayselected].path[_root.pointselected][4].key='(type value here)';
		} else {
			_root.map.ways[wayselected].attr.key='(type value here)';
		}
		_root.properties.attachMovie("keyvalue","key",_root.propn);
		_root.properties.key['value'].textColor=0x888888;
		_root.properties.key.keyname.type="input";
		_root.properties.key.keyname.setTextFormat(boldSmall);
		_root.properties.key.keyname.setNewTextFormat(boldSmall);
		_root.properties.key.keyname.onSetFocus=function()  { this.addListener(textfieldListener); Key.removeListener(keyListener); };
		_root.properties.key.keyname.onKillFocus=function() {
			// rename field
			this.removeListener(textfieldListener);
			Key.addListener(keyListener);
			this._parent.value.variable=null;
			z=this._parent.keyname.text;
			if (_root.currentproptype=='point') {
				_root.map.ways[wayselected].path[pointselected][4][z]=_root.map.ways[wayselected].path[pointselected][4][this._parent._name];
				_root.map.ways[wayselected].path[pointselected][4][this._parent._name]='';
				// above line should be delete _root.map..., but Ming won't compile 'delete' with more than one [] in it
				this._parent._name=z;
				this._parent.value.variable="_root.map.ways."+wayselected+".path."+pointselected+".4."+z;
			} else {
				_root.map.ways[wayselected].attr[z]=_root.map.ways[wayselected].attr[this._parent._name];
				_root.map.ways[wayselected].attr[this._parent._name]='';
				this._parent._name=z;
				this._parent.value.variable="_root.map.ways."+wayselected+".attr."+z;
			}
		};
	}
	
	// repeatAttributes - paste in last set of attributes
	
	function repeatAttributes() {
		if (_root.wayselected==0 && _root.pointselected==-2) { return; }
		if (savedtype=='point') { z=_root.map.ways[savedway].path[savedpoint][4]; }
						   else { z=_root.map.ways[savedway].attr; }
		for (i in z) {
			if (Key.isDown(Key.SHIFT) && (i=='name' || i=='ref') || i=='created_by') {
				// ignore name and ref if SHIFT pressed
			} else {
				if (savedtype=='point') { j=_root.map.ways[savedway].path[savedpoint][4][i]; }
								   else { j=_root.map.ways[savedway].attr[i]; }
				if (currentproptype=='point') { _root.map.ways[wayselected].path[pointselected][4][i]=j; }
										 else { _root.map.ways[wayselected].attr[i]=j; }
			}
		}
		populatePropertyWindow(_root.currentproptype);
		_root.map.ways[wayselected].clean=0;
		_root.map.ways[wayselected].redraw();
	}
	
	// nextAttributes	- go to next screen of attributes (not yet supported)
	
	function nextAttributes() {
	}

	// textChanged		- listener marks way as dirty when any change made

	function textChanged() { 
		_root.map.ways[wayselected].clean=0;
	};
	



	// =====================================================================================
	// insertAnchorPoint		- add point into way with SHIFT-clicking
	
	function insertAnchorPoint(way) {
		nx=_root.map._xmouse;	// where we're inserting it
		ny=_root.map._ymouse;	//	|
		closest=0.05; closei=0;
		for (i=0; i<(_root.map.ways[way].path.length)-1; i+=1) {
			if (_root.map.ways[way].path[i+1][3]) {
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
		}
		_root.newnodeid--;
		newpoint=new Array(nx,ny,newnodeid,1,new Array(),0);
		_root.map.ways[way].path.splice(closei,0,newpoint);
		_root.map.ways[way].clean=0;
		_root.map.ways[way].redraw();
	}

	// =====================================================================================

	// keyPressed				- key listener

	function keyPressed() {
		k=Key.getCode();
		if (k>48 && k<58 && wayselected!=0) {
			if (presetnames[currentproptype][presetselected][k-48]!=null) {
				setAttributesFromPreset(k-48);
			}
		}
		switch (k) {
			case 46:		;													// DELETE/backspace - delete way/node
			case 8:			if (Key.isDown(Key.SHIFT)) {						//  |
								if (_root.wayselected!=0) { _root.map.ways[wayselected].remove(); }
							} else { keyDelete(1); }; break;					//  |
			case 13:		stopDrawing(); break;								// ENTER - stop drawing line
			case 27:		revertWay(); break;									// ESCAPE - revert current way
			case 112:		setBackground(0); break; 							// f1 - no base layer
			case 113:		setBackground(2-1*(Key.isDown(Key.SHIFT))); break;	// f2 - Yahoo! base layer
			case 71:		loadGPS(); break;									// G - load GPS
			case 82:		repeatAttributes(); break;							// R - repeat attributes
			case 88:		_root.map.ways[wayselected].splitWay(); break;		// X - split way
			case Key.PGUP:	zoomIn(); break;									// Page Up - zoom in
			case Key.PGDN:	zoomOut(); break;									// Page Down - zoom out
			case Key.LEFT:  moveMap( 140,0); redrawYahoo(); whichWays(); break;	// cursor keys
			case Key.RIGHT: moveMap(-140,0); redrawYahoo(); whichWays(); break;	//  |
			case Key.DOWN:  moveMap(0,-100); redrawYahoo(); whichWays(); break;	//  |
			case Key.UP:    moveMap(0, 100); redrawYahoo(); whichWays(); break;	//  |
			case 167:		cyclePresetIcon(); break;							// '�' - cycle presets
			case 187:		enterNewAttribute(); break;							// '+' - add new attribute
			case 189:		keyDelete(0); break;								// '-' - delete node from this way only
			// default:		_root.chat.text=Key.getCode()+" pressed";
		};
	}

	function startCount() { z=new Date(); _root.startTime=z.getTime(); }
	function endCount(id) { z=new Date(); zz=Math.floor((z.getTime()-_root.startTime)*100);
							if (zz>100) { _root.chat.text+=id+":"+zz+";"; } }
	


	// =====================================================================================
	// World handling

	// redrawMap(x,y)
	// update all world parameters and call in tiles
	// based on user-selected location

	function redrawMap(tx,ty) {
		_root.map._x=tx;
		_root.map._y=ty;

		// ----	Set global bounds variables
		//		x radius (lon) is 280/Math.pow(2,_root.scale)
		//		y radius (lat) is 280/Math.pow(2,_root.scale)

		bscale=Math.pow(2,_root.scale-12);

		_root.map._xscale=100*bscale;
		_root.map._yscale=100*bscale;
		
		_root.centre_lat=coord2lat((250-_root.map._y)/bscale);
		_root.edge_t	=coord2lat(-_root.map._y	 /bscale);
		_root.edge_b	=coord2lat((500-_root.map._y)/bscale);

		_root.centre_lon=coord2long((350-_root.map._x)/bscale);
		_root.edge_l	=coord2long(-_root.map._x	  /bscale);
		_root.edge_r	=coord2long((700-_root.map._x)/bscale);

//		updateGrid();

		// ----	Trace
		
//		_root.coordmonitor.text ="Centre of map: lon "+_root.centre_lon+", lat "+_root.centre_lat+" -- ";
//		_root.coordmonitor.text+="Edges: lon "+_root.edge_l+"->"+_root.edge_r+" -- ";
//		_root.coordmonitor.text+="           lat "+_root.edge_b+"->"+_root.edge_t+" -- ";

	}

	function redrawYahoo() {
		if (_root.baselayer>0) {
			_root.yahoo.visible=true;
			_root.ylat=_root.centre_lat;
			_root.ylon=_root.centre_lon;
			_root.yzoom=17-_root.scale;
			_root.yahoo._x=0;
			_root.yahoo._y=0;
		} else {
			_root.yahoo.visible=false;
			_root.yahoo._x=_root.map._x-3000/Math.pow(2,_root.scale-12);	// keep offscreen
		}
	}

	// zoomIn, zoomOut, changeScaleTo - change scale functions
	
	function zoomIn()  {
		if (_root.scale<_root.maxscale) {
			changeScaleTo(_root.scale+1);
			if (_root.waycount>500) { purgeWays(); }
			redrawMap((_root.map._x*2)-350,(_root.map._y*2)-250);
			redrawYahoo();
			for (qway in _root.map.ways) { _root.map.ways[qway].redraw(); }
			if (_root.wayselected) {
				_root.map.ways[wayselected].highlight();
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
			}
		}
	}

	function zoomOut() {
		if (_root.scale>_root.minscale) {
			changeScaleTo(_root.scale-1); 
			redrawMap((_root.map._x+350)/2,(_root.map._y+250)/2);
			redrawYahoo();
			whichWays();
			for (qway in _root.map.ways) { _root.map.ways[qway].redraw(); }
			if (_root.wayselected) {
				_root.map.ways[wayselected].highlight();
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
			}
		}
	}

	function changeScaleTo(newscale) {
		_root.scale=newscale;
		if (_root.scale==_root.minscale) { _root.i_zoomout._alpha=25;  }
									else { _root.i_zoomout._alpha=100; }
		if (_root.scale==_root.maxscale) { _root.i_zoomin._alpha =25;  }
									else { _root.i_zoomin._alpha =100; }
	}

	

	// =====================================================================
	// Map support functions

	// everyFrame() - called onEnterFrame

	function everyFrame() {
		// ----	Disable Yahoo! over-eager keyboard listener
		_root.yahoo.myMap.enableKeyboardShortcuts(false);

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

	function addEndPoint(x,y,node) {
		newpoint=new Array(x,y,node,0,new Array(),0);
		if (_root.drawpoint==0) {				// add to start or end of path?
			_root.map.ways[wayselected].path.unshift(newpoint);
			_root.map.ways[wayselected].path[1][3]=1;	// set first line to 'draw', not 'move'
		} else {
			newpoint[3]=1;
			_root.map.ways[wayselected].path.push(newpoint);
			_root.drawpoint=_root.map.ways[wayselected].path.length-1;
		}
	
		// Redraw map
		_root.map.ways[wayselected].clean=0;
		_root.map.ways[wayselected].redraw();
		_root.map.ways[wayselected].select();
		_root.map.feedback.clear();
	}


	// processDrag, moveMap - process map dragging

	function processDrag() {
		if (_root.pointertype!='hand') { setPointer('hand'); }
		moveMap(Math.floor(_xmouse-lastxmouse),Math.floor(_ymouse-lastymouse));
	}
	
	function moveMap(xdiff,ydiff) {
		_root.map._x+=xdiff;
		_root.map._y+=ydiff;
		_root.yahoo._x+=xdiff;	// could use _root.yahoo.myMap.map.map_5.MapBase2._x,
		_root.yahoo._y+=ydiff;	// but the '2' changes
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		redrawMap(_root.map._x,_root.map._y);
	}


	// mapClick - user has clicked within map area, so start drag
	
	function mapClick() {
		setPointer('pen');
		clearTooltip();
		_root.map.onMouseMove=function() { processDrag(); };
		_root.dragmap=1;
		_root.lastxmouse=_root._xmouse;
		_root.lastymouse=_root._ymouse;
		_root.firstxmouse=_root._xmouse;
		_root.firstymouse=_root._ymouse;
	}


	// mouseRelease - handle mouseUp event

	function mouseRelease() {
		_root.map.onMouseMove=function() {};
		tolerance=4/Math.pow(2,_root.scale-12);

		// ------------------------------------------
		// Double-clicked on point while drawing line

		if (_root.drawpoint==-2) {
			stopDrawing();

		// -------------------------------
		// Clicked on map without dragging

		} else if (Math.abs(_root.firstxmouse-_root._xmouse)<tolerance &&
				   Math.abs(_root.firstymouse-_root._ymouse)<tolerance) {

			_root.dragmap=-1;
			// Adding a point to the way being drawn
			if (_root.drawpoint>-1) {
				_root.newnodeid--;
				if (_root.pointselected>-2) {
					setTypeText("Way",_root.wayselected);
					populatePropertyWindow('way');
				}
				addEndPoint(_root.map._xmouse,_root.map._ymouse,newnodeid);

			// Deselecting a way
			} else if (_root.wayselected) {
				uploadSelected(); deselectAll();

			// Starting a new way
			} else {
				_root.newnodeid--; startNewWay(_root.map._xmouse,_root.map._ymouse,_root.newnodeid);
			}

		// ----------------------
		// Dragged map, so redraw

		} else if (_root.dragmap==1) {
			redrawYahoo(); whichWays();
			_root.dragmap=-1;
			if (_root.wayselected) { setPointer(''); }
							  else { setPointer('pen'); }
		}

		// -------------
		// Dragged point

		if (_root.dragpoint!=-1) {
			newx=_root.map._xmouse;
			newy=_root.map._ymouse;
			inbounds=Math.abs(newx-_root.map.ways[wayselected].path[dragpoint][0])>tolerance || 
					 Math.abs(newy-_root.map.ways[wayselected].path[dragpoint][1])>tolerance;

			if (inbounds) {

				// ====	Move existing point

				for (qway in _root.map.ways) {
					qdirty=0;
					for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
						if (_root.map.ways[qway].path[qs][2]==_root.map.ways[wayselected].path[dragpoint][2]) {
							_root.map.ways[qway].path[qs][0]=newx;
							_root.map.ways[qway].path[qs][1]=newy;
							qdirty=1;
						}
					}
					if (qdirty) {
						_root.map.ways[qway].redraw();
						_root.map.ways[qway].clean=0;
					}
				}
				_root.map.ways[wayselected].highlightPoints(5000,"anchor");
				_root.map.ways[wayselected].highlight();
			} else if ((dragpoint==0 || dragpoint==_root.map.ways[wayselected].path.length-1) && !Key.isDown(17)) {
				// ===== Clicked at start or end of line
				if (_root.drawpoint==0 || _root.drawpoint==_root.map.ways[wayselected].path.length-1) {
					// - Join looping path
					addEndPoint(_root.map.ways[wayselected].path[dragpoint][0],
								_root.map.ways[wayselected].path[dragpoint][1],
								_root.map.ways[wayselected].path[dragpoint][2]);
					stopDrawing();
				} else if (_root.drawpoint==-1) {
					// - Start elastic line for adding new point
					setTooltip("click to add point\ndouble-click/Return\nto end line",0);
					_root.drawpoint=dragpoint;
				}
			} else {
				// ===== Clicked elsewhere in line
				if (_root.drawpoint>-1) {
					addEndPoint(_root.map.ways[wayselected].path[dragpoint][0],
								_root.map.ways[wayselected].path[dragpoint][1],
								_root.map.ways[wayselected].path[dragpoint][2]);
				}
//				_root.drawpoint=-1;
			}
			_root.dragpoint=-1;
		}
	}


	// startNewWay	- create new way with first point x,y,node

	function startNewWay(x,y,node) {
		uploadSelected();
		_root.newwayid--;
		newpoint=new Array(x,y,node,0,new Array(),0);
		_root.wayselected=newwayid;
		_root.map.ways.attachMovie("way",newwayid,++waydepth);
		_root.map.ways[newwayid].path[0]=newpoint;
		_root.map.ways[newwayid].redraw();
		_root.map.ways[newwayid].select();
		_root.map.ways[newwayid].clean=0;
		_root.drawpoint=0;
		setTooltip("click to add point\ndouble-click/Return\nto end line",0);
		setTypeText("Way",_root.wayselected);
		populatePropertyWindow('way');
	}

	// =====================================================================
	// Tooltip functions

	function setTooltip(txt,delay) {
		_root.tooltip.text=txt;
		_root.tooltip.setTextFormat(plainSmall);
		_root.createEmptyMovieClip('ttbackground',40,580,25,150,100);
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


	// ================================================================
	// GPS functions
	
	// loadGPS		- load GPS backdrop from server

	function loadGPS() {
		_root.map.createEmptyMovieClip('gps',3);
		if (Key.isDown(Key.SHIFT)) { loadMovie('/potlatch/getgps.cgi?xmin='+(_root.edge_l-0.01)+'&xmax='+(_root.edge_r+0.01)+'&ymin='+(_root.edge_b-0.01)+'&ymax='+(_root.edge_t+0.01)+'&baselong='+_root.baselong+'&basey='+_root.basey+'&masterscale='+_root.masterscale+'&token='+_root.usertoken,_root.map.gps); }
							  else { loadMovie('/potlatch/getgps.cgi?xmin='+(_root.edge_l-0.01)+'&xmax='+(_root.edge_r+0.01)+'&ymin='+(_root.edge_b-0.01)+'&ymax='+(_root.edge_t+0.01)+'&baselong='+_root.baselong+'&basey='+_root.basey+'&masterscale='+_root.masterscale,_root.map.gps); }
	}


	// ================================================================
	// Way communication
	
	// whichWays	- get list of ways from remoting server

	function whichWays() {
		if (_root.waycount>500) { purgeWays(); }
		whichresponder=function() {};
		whichresponder.onResult=function(result) {
			_root.whichreceived+=1;
			for (i in result) {
				way=result[i];
				if (!_root["map"]["ways"][way]) {
					_root.map.ways.attachMovie("way",way,++waydepth);
					_root["map"]["ways"][way].load(way);
					_root.waycount+=1;
					_root.waysrequested+=1;
				}
			}
		};
		remote.call('whichways',whichresponder,_root.edge_l,_root.edge_b,_root.edge_r,_root.edge_t);
		_root.whichrequested+=1;
	}

	// purgeWays - remove any clean ways outside current view
	
	function purgeWays() {
		for (qway in _root.map.ways) {
			if (qway==_root.wayselected) {
			} else if (_root.map.ways[qway].clean==0) {
				_root.map.ways[qway].upload();
			} else if (((_root.map.ways[qway].xmin<edge_l && _root.map.ways[qway].xmax<edge_l) ||
						(_root.map.ways[qway].xmin>edge_r && _root.map.ways[qway].xmax>edge_r) ||
					    (_root.map.ways[qway].ymin<edge_b && _root.map.ways[qway].ymax<edge_b) ||
						(_root.map.ways[qway].ymin>edge_t && _root.map.ways[qway].ymax>edge_t))) {
				removeMovieClip(_root.map.ways[qway]);
				_root.waycount-=1;
			}
		}
	}


	// ================================================================
	// Pointer handling
	
	function trackMouse() {
		_root.pointer._x=_root._xmouse;
		_root.pointer._y=_root._ymouse;
		updateAfterEvent();
	}
	
	function setPointer(ptype) {
		if (ptype) {
			_root.attachMovie(ptype,"pointer",65535);
			trackMouse();
			Mouse.addListener(mouseListener);
			Mouse.hide();
		} else {
			removeMovieClip(_root.pointer);
			Mouse.removeListener(mouseListener);
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
		if ($fn=shift @ARGV) { $m->save($fn); }
						else { $m->save("potlatch.swf"); }
	}
