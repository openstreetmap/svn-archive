
	// Potlatch introduction/welcome functions
	
	// -----------------------------------------------------------------------
	// Load presets
	
	function loadPresets() {
		_root.windows.attachMovie("modal","pleasewait",++windowdepth);
		_root.windows.pleasewait.init(295,35,new Array(),null);
		_root.windows.pleasewait.box.createTextField("prompt",2,37,8,280,20);
		_root.windows.pleasewait.box.attachMovie("whirl","whirl",3);
		with (_root.windows.pleasewait.box.whirl) { _x=20; _y=18; }
		writeText(_root.windows.pleasewait.box.prompt,"Please wait - loading presets.");
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
			_root.i18n=result[10];
			_root.panel.i_preset._visible=false;

			_root.waysloading._x=Stage.width-220;
			_root.waysloading._y=5;
			_root.waysloading.attachMovie("whirl","whirl",3);
			_root.waysloading.whirl._x=205;
			_root.waysloading.whirl._y=10;
			_root.waysloading.whirl._xscale=
			_root.waysloading.whirl._yscale=75;
			_root.io=0;

			if (lat!=undefined) { startPotlatch(); }	// Parse GPX if supplied
			if (gpx) { parseGPX(gpx); }					//  |
				else { _root.windows.pleasewait.remove(); }
			if (lat==undefined) { lat=0; long=0; startPotlatch(); }
			if (!preferences.data.nosplash) { splashScreen(); }
			welcomeButtons();
		};
		remote_read.call('getpresets',preresponder);
	}
	
	// -----------------------------------------------------------------------
	// Create splash screen	
	
	function splashScreen() {
		_root.windows.attachMovie("modal","splashscreen",++windowdepth);
		_root.windows.splashscreen.init(500,225,[],null,true);

		_root.windows.splashscreen.box.createTextField("title",1,7,7,500-14,20);
		with (_root.windows.splashscreen.box.title) {
			type='dynamic';
			text=iText("Welcome to OpenStreetMap!",'prompt_welcome'); setTextFormat(boldText);
		}

		// Light grey background
		_root.windows.splashscreen.box.createEmptyMovieClip('lightgrey',2);
		with (_root.windows.splashscreen.box.lightgrey) {
			beginFill(0xF3F3F3,100);
			moveTo(10,30); lineTo(492,30);
			lineTo(492,195); lineTo(10,195);
			lineTo(10,30); endFill();
		};
		
		_root.windows.splashscreen.box.createTextField("prompt",3,15,35,500-30,180);
		writeText(_root.windows.splashscreen.box.prompt,iText(
			"Choose a button below to get editing. If you click 'Start', "+
			"you'll be editing the main map as you work - most changes show "+
			"up after an hour or two. If you click 'Play', your changes won't be "+
			"saved, so you can practise editing.\n\n"+
			"Remember the golden rules of OpenStreetMap:\n\n",'prompt_introduction')+
			chr(0x278A)+"  "+iText("Don't copy from other maps"							,'prompt_dontcopy')+"\n"+
			chr(0x278B)+"  "+iText("Accuracy is important - only map places you've been",'prompt_accuracy')+"\n"+
			chr(0x278C)+"  "+iText("And have fun!"										,'prompt_enjoy'   )+"\n");

		_root.windows.splashscreen.box.attachMovie("checkbox","nosplash",5);
		_root.windows.splashscreen.box.nosplash.init(12,205,iText("Don't show this message again",'dontshowagain'),preferences.data.nosplash,function(n) { preferences.data.nosplash=n; });
	}

	// -----------------------------------------------------------------------
	// Create welcome buttons
	
	function welcomeButtons() {
		_root.panel.properties._visible=false;
		_root.panel.createEmptyMovieClip("welcome",61);
	
		_root.panel.welcome.createEmptyMovieClip("start",1);
		drawButton(_root.panel.welcome.start,150,7,iText("Start",'start'),iText("Start mapping with OpenStreetMap.",'prompt_start'));
		_root.panel.welcome.start.onPress=function() { removeWelcome(true); };

		_root.panel.welcome.createEmptyMovieClip("play",2);
		drawButton(_root.panel.welcome.play,150,29,iText("Play",'play'),iText("Practise mapping - your changes won't be saved.",'prompt_practise'));
		_root.panel.welcome.play.onPress=function() { removeWelcome(false); };
	
		_root.panel.welcome.createEmptyMovieClip("help",3);
		drawButton(_root.panel.welcome.help,150,51,iText("Help",'help'),iText("Find out how to use Potlatch, this map editor.",'prompt_help'));
		_root.panel.welcome.help.onPress=function() { getUrl("http://wiki.openstreetmap.org/index.php/Potlatch","_blank"); };
	
		if (gpx) {
			_root.panel.welcome.createEmptyMovieClip("convert",4);
			drawButton(_root.panel.welcome.convert,150,73,iText("Track",'track'),iText("Convert your GPS track to (locked) ways for editing.",'prompt_track'));
			_root.panel.welcome.convert.onPress=function() { gpxToWays(); removeWelcome(true); };
		} else if (preferences.data.launcher) {
			_root.panel.welcome.createEmptyMovieClip("launcher",4);
			drawButton(_root.panel.welcome.launcher,150,73,iText("Launch",'launch'),iText("Launch an external URL at this location.",'prompt_launch'));
			_root.panel.welcome.launcher.onPress=function() { 
				var a=preferences.data.launcher.split('!');
				getUrl(a[0]+_root.scale+a[1]+centrelong(0)+a[2]+centrelat(0)+a[3],"_blank");
				removeWelcome(true);
			};
		}
	}

	function removeWelcome(live) {
		if (!_root.panel.welcome) { return; }
		removeMovieClip(_root.panel.welcome);
		_root.panel.properties._visible=true;
		_root.panel.presets._visible=true;
		_root.windows.splashscreen.remove();
		if (live) { setEditingStatus(iText("Editing map",'editingmap')); }
			 else { setEditingStatus(iText("Practice mode",'practicemode'));
					_root.sandbox=true; }
	}

	// -----------------------------------------------------------------------
	// Main start function

	function startPotlatch() {
		_root.urllat  =Number(lat);						// LL from query string
		_root.urllong =Number(long);					//  |
		_root.baselong=urllong-xradius/masterscale/bscale;
		_root.basey   =lat2y(urllat)+yradius/masterscale/bscale;
		_root.baselat =y2lat(basey);
		_root.ylat=baselat;	 _root.lastylat=ylat;		// current Yahoo state
		_root.ylon=baselong; _root.lastylon=ylon;		//  |
		updateCoords(0,0);
		updateLinks();
		initBackground();
		whichWays();
		_root.onEnterFrame=function() { everyFrame(); };
	}
	
	// -----------------------------------------------------------------------
	// Main start function

	function setEditingStatus(str) {
		_root.createEmptyMovieClip("status",62);
		_root.status.createTextField("btext",1,0,0,90,20);
		with (_root.status.btext) {
			text=str;
			setTextFormat(boldWhite);
			selectable=false; type='dynamic';
		};
		setStatusPosition();
		var tw=_root.status.btext.textWidth+5;
		with (_root.status) {
			if (preferences.data.baselayer==2) { _y-=32; }
			beginFill(0xFF0000,100);
			moveTo(0,0); lineTo(tw,0); lineTo(tw,17);
			lineTo(0,17); lineTo(0,0); endFill();
		};
	};

	function setStatusPosition() {
		_root.status._x=Stage.width-_root.status.btext.textWidth-9;
		_root.status._y=Stage.height-panelheight-22;
		if (preferences.data.baselayer==2) { _root.status._y-=32; }
	};
