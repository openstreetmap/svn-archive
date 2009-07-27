
	// =====================================================================================
	// Icon panel

	function drawIconPanel() {
		if (!_root.iconlist) { return; }
		var w=Math.min((getPanelColumns()*190-30)/7,105);
		_root.panel.createEmptyMovieClip("iconpanel",51);
		_root.panel.iconpanel._visible=true;
		_root.panel.iconpanel.createEmptyMovieClip("icons",1);
		_root.panel.iconpanel.createEmptyMovieClip("legends",2);
		_root.panel.iconpanel.createTextField("prompt",3,5,-20,300,20);
		_root.panel.iconpanel.prompt.text=iText("Drag and drop points of interest","drag_pois");
		_root.panel.iconpanel.prompt.setTextFormat(plainText);
		with (_root.panel.iconpanel) { _x=110; _y=25; _visible=true; };
		var x=0; var y=0; var n=0;
		for (var i in iconlist) {
			var n=iconlist[i];
			_root.panel.iconpanel.icons.attachMovie("icon_"+n,n,i);
			_root.panel.iconpanel.icons[n]._x=x*w+15;
			_root.panel.iconpanel.icons[n]._y=y*18+15;
			_root.panel.iconpanel.icons[n].onPress=function() { startIconDrag(this); };

			_root.panel.iconpanel.legends.createTextField(n,i,x*w+10+15,y*18+15-10,w-20,18);
			_root.panel.iconpanel.legends[n].text=iconnames[n];
			_root.panel.iconpanel.legends[n].setTextFormat(plainSmall);
			x++; if (x==7) { x=0; y++; }
		}
	}

	function removeIconPanel() {
		_root.panel.iconpanel._visible=false;
		_root.panel.createEmptyMovieClip("iconpanel",51);
	}

	function startIconDrag(o) {
		var mpos=new Object(); mpos.x=o._x; mpos.y=o._y;
		_root.panel.iconpanel.localToGlobal(mpos);
		_root.attachMovie("icon_"+o._name,"dragicon",25);
		_root.dragicon._x=mpos.x;
		_root.dragicon._y=mpos.y;
		_root.dragicon.onMouseUp=function() { endIconDrag(o); };
		_root.dragicon.startDrag();
	}

	function endIconDrag(o) {
		_root.dragicon.stopDrag();
		if (!_root.masksquare.hitTest(_root.dragicon)) {
			removeMovieClip(_root.dragicon);
			return;
		}
		var mpos=new Object();
		mpos.x=_root.dragicon._x;
		mpos.y=_root.dragicon._y;
		_root.map.globalToLocal(mpos);

		_root.map.pois.attachMovie("poi_"+o._name,--newnodeid,++poidepth);
		_root.map.pois[newnodeid]._x=mpos.x;
		_root.map.pois[newnodeid]._y=mpos.y;
		_root.map.pois[newnodeid].icon="poi_"+o._name;
		_root.map.pois[newnodeid].attr=deepCopy(icontags[o._name]);
		_root.map.pois[newnodeid]._xscale=_root.map.pois[newnodeid]._yscale=_root.iconscale;
		_root.map.pois[newnodeid].attr['name']="(type name here)";
		_root.map.pois[newnodeid].select();
		_root.map.pois[newnodeid].clean=false;
		_root.poicount++;
		markClean(false);
		removeMovieClip(_root.dragicon);
		_root.undo.append(UndoStack.prototype.undo_createpoi,
						  [_root.map.pois[newnodeid]],iText("creating a POI",'action_createpoi'));
	}
