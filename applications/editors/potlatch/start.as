
	// Potlatch introduction/welcome functions
	
	// -----------------------------------------------------------------------
	// Load presets
	
	function loadPresets() {
        pleaseWait("Please wait - loading presets.");
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
			_root.iconlist=result[10];
			_root.iconnames=result[11];
			_root.icontags=result[12];
			_root.i18n=result[13];
			_root.panel.i_preset._visible=false;

			var pages=result[14].split('<page/>');
			_root.helppages=new Array();
			for (var i in pages) {
                helppages[i]=pages[i].split('<column/>');
		    }

			_root.waysloading._x=Stage.width-220;
			_root.waysloading._y=5;
			_root.waysloading.attachMovie("whirl","whirl",3);
			_root.waysloading.whirl._x=205;
			_root.waysloading.whirl._y=10;
			_root.waysloading.whirl._xscale=
			_root.waysloading.whirl._yscale=75;
			_root.io=0;

			for (var icon in iconlist) {
				Object.registerClass("poi_"+iconlist[icon],POI);
			}

			if (lat!=undefined) { startPotlatch(); }	// Parse GPX if supplied
			if (gpx) { parseGPX(gpx); }					//  |
				else { _root.windows.pleasewait.remove();
					   if (lat==undefined) { lat=0; long=0; startPotlatch(); }
			}
			splashScreen();
			welcomeButtons();
		};
		remote_read.call('getpresets',preresponder,_root.usertoken,_root.lang);
	}

    function pleaseWait(a,cancelFunction) {
		_root.windows.attachMovie("modal","pleasewait",++windowdepth);
		if (cancelFunction) {
			_root.windows.pleasewait.init(295,65,new Array(iText('cancel')),cancelFunction);
		} else {
			_root.windows.pleasewait.init(295,35,new Array(),null);
		}
		_root.windows.pleasewait.box.createTextField("prompt",2,37,8,280,20);
		_root.windows.pleasewait.box.attachMovie("whirl","whirl",3);
		with (_root.windows.pleasewait.box.whirl) { _x=20; _y=18; }
		writeText(_root.windows.pleasewait.box.prompt,a);
	}
	
	// -----------------------------------------------------------------------
	// Create splash screen	
	
	function splashScreen() {
        var h=267;
        _root.dogpx=false;
        if (gpx) { h+=20; }
        if (preferences.data.launcher) { h+=30; }
        
		_root.windows.attachMovie("modal","splashscreen",++windowdepth);
		_root.windows.splashscreen.init(350,h,[],null,2);
        _root.windows.splashscreen.box._alpha=85;
        var box=_root.windows.splashscreen.box;

		box.createTextField("title",1,0,15,350,40);
		with (box.title) {
			type='dynamic';
			text=iText('prompt_welcome'); setTextFormat(boldLarge);
            _x=(350-textWidth)/2;
		}
		adjustTextField(box.title);
		
		box.createTextField("help",4,0,40,350,20);
		with (box.help) {
			type='dynamic';
			text=iText('prompt_helpavailable'); setTextFormat(plainSmall);
            _x=(350-textWidth)/2;
		}
		adjustTextField(box.help);

        box.attachMovie("editwithsave","start_save",50);
        with (box.start_save) {
            _x=90; _y=125;
            _xscale=500; _yscale=500;
        }
		box.start_save.onPress=function() { removeSplash(false); };

        box.attachMovie("editlive","start_live",51);
        with (box.start_live) {
            _x=240; _y=107;
            _xscale=375; _yscale=375;
        }
		box.start_live.onPress=function() { removeSplash(true); };

		box.createEmptyMovieClip("button_save",2);
		drawButton(box.button_save,50,225,iText('prompt_editsave'),'',100);
		box.button_save.onPress=function() { removeSplash(false); };

		box.createEmptyMovieClip("button_live",3);
		drawButton(box.button_live,200,225,iText('prompt_editlive'),'',100);
		box.button_live.onPress=function() { removeSplash(true); };

        // Optional buttons

        var h=260;
        if (gpx) { 
    		box.attachMovie("checkbox","convert",5);
    		box.convert.init(0,h,iText('prompt_track'),false,function(n) { _root.dogpx=n; });
            box.convert._x=(350-13-box.convert.prompt.textWidth)/2;
            h+=20;
        }
        if (preferences.data.launcher) {
    		box.createEmptyMovieClip("button_launch",6);
    		drawButton(box.button_launch,100,h+5,iText('prompt_launch'),'',150);
			box.button_launch.onPress=function() { 
				var a=preferences.data.launcher.split('!');
				getUrl(a[0]+_root.scale+a[1]+centrelong(0)+a[2]+centrelat(0)+a[3],"_blank");
			};
        }
	}

	// -----------------------------------------------------------------------
	// Create welcome buttons
	
	function removeSplash(live) {
		_root.panel.properties._visible=true;
		_root.panel.presets._visible=true;
		_root.windows.splashscreen.remove();
		if (_root.panel.properties.proptype=='') { drawIconPanel(); }
		if (_root.dogpx) { gpxToWays(); }
		if (live && !preferences.data.livewarned) {
			_root.windows.attachMovie("modal","confirm",++windowdepth);
			_root.windows.confirm.init(275,140,new Array(iText('no'),iText('yes')),
				function(choice) {
					preferences.data.livewarned=true;
					preferences.flush();
					if (choice==iText('yes')) { beginEditing(true); }
										 else { beginEditing(false); }
				});
			_root.windows.confirm.box.createTextField("title",12,7,7,400-14,20);
			with (_root.windows.confirm.box.title) {
				type='dynamic'; text=iText("warning"); setTextFormat(boldText);
			}
			_root.windows.confirm.box.createTextField("prompt",13,7,29,250,140);
			writeText(_root.windows.confirm.box.prompt,iText('prompt_live'));
		} else {
			beginEditing(live);
		}
	}

	function beginEditing(live) {
		if (live) {
			setEditingStatus(iText('editinglive'),0xFF0000);
			pleaseWait(iText('openchangeset'));
			_root.changecomment=''; startChangeset(true);
		} else {
			_root.sandbox=true;
			setEditingStatus(iText('editingoffline'),0x008800);
			_root.panel.advanced.disableOption(5);
		}
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

		var x=7;
		_root.panel.createEmptyMovieClip("help",80);
		drawButton(_root.panel.help,x,114,iText('help'),"",undefined,true);
		_root.panel.help.onPress=function() { openHelp(); };
		x+=_root.panel.help._width+2;

		_root.panel.createEmptyMovieClip("findtag",81);
		drawButton(_root.panel.findtag,x,114,iText('tags_findtag'),"",undefined,true);
		_root.panel.findtag.onPress=function() { openTagFinder(); _root.reinstatefocus=_root.windows.tf.box.comment; _root.tfswallowed=true; };
		x+=_root.panel.findtag._width+7;

		_root.panel.attachMovie("menu","advanced",82);
		_root.panel.advanced.init(x,114,-1,
			new Array(iText("advanced_parallel"),
					  iText("advanced_history"),
					  "--",
					  iText("advanced_inspector"),
					  iText("advanced_undelete"),
					  iText("advanced_close"),
					  iText("advanced_maximise")),
			iText('advanced_tooltip'),advancedAction,null,85,iText("advanced"));
		selectWay(0);	// just to update the menu items

		_root.onEnterFrame=function() { everyFrame(); };
	}

	function setEditingStatus(str,col) {
		_root.createEmptyMovieClip("status",62);

		_root.status.createTextField("btext",1,0,-1,90,20);
		with (_root.status.btext) {
			text=str;
			setTextFormat(boldWhite);
			selectable=false; type='dynamic';
		};
		adjustTextField(_root.status.btext);
		
		var tw=_root.status.btext.textWidth+5;
		with (_root.status) {
			// if (preferences.data.baselayer==2) { _y-=32; }
			beginFill(col,100);
			moveTo(0,0); lineTo(tw,0); lineTo(tw,17);
			lineTo(0,17); lineTo(0,0); endFill();
		};
		
		if (_root.sandbox) {
			_root.status.createEmptyMovieClip("save",3);
			drawButton(_root.status.save,tw+10,0,iText('save'),"");
			_root.status.save.onPress=function() { prepareUpload(); };
			_root.status.save._x=tw+10;	// we don't want it centred, even if internationalised!
		}

		setStatusPosition();
	};

	function setStatusPosition() {
		_root.status._x=Stage.width-_root.status.btext.textWidth-10;
		if (_root.sandbox) { _root.status._x-=_root.status.save._width+10; }
		_root.status._y=Stage.height-21; // panelheight-22;
		// if (preferences.data.baselayer==2) { _root.status._y-=32; }
	};

	function establishConnections() {
		_root.remote_read=new NetConnection();
		_root.remote_read.connect(apiurl+'/read');
		_root.remote_read.onStatus=function(info) {
	        readError=true; 
		    _root.panel.i_warning._visible=true;
		};

		_root.remote_write=new NetConnection();
		_root.remote_write.connect(apiurl+'/write');
		_root.remote_write.onStatus=function(info) {
	        writeError=true;
	 	    _root.panel.i_warning._visible=true;
	        if (_root.uploading) { handleWarning(); }
		};
	};
