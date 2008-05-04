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
		memberDeleted('node', this._name);
		if (this._name>=0 && !_root.sandbox) {
			poidelresponder = function() { };
			poidelresponder.onResult = function(result) {
				var code=result.shift(); if (code) { handleError(code,result); return; }
				if (poiselected==result[0]) { deselectAll(); }
				removeMovieClip(_root.map.pois[result[0]]);
			};
			remote.call('putpoi',poidelresponder,_root.usertoken,Math.floor(this._name),this._x,this._y,this.attr,0,baselong,basey,masterscale);
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
			_root.panel.properties.init('POI',getPanelColumns(),4);
			_root.panel.presets.init(_root.panel.properties);
			updateButtons();
			updateScissors(false);
			redrawRelationsForMember('node', result[0]);
		};
		remote.call('getpoi',poirelresponder,Math.floor(this._name),baselong,basey,masterscale);
	};
	POI.prototype.upload=function() {
		poiresponder=function() { };
		poiresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var ni=result[1];	// new way ID
			if (result[0]!=ni) {
				_root.map.pois[result[0]]._name=ni;
				renumberMemberOfRelation('node', result[0], ni);
				if (poiselected==result[0]) {
					poiselected=ni;
					if (_root.panel.t_details.text==result[0]) { _root.panel.t_details.text=ni; _root.panel.t_details.setTextFormat(plainText); }
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
		this.originalx=this._x;		// saved for undo
		this.originaly=this._y;		//  |
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
			markClean(false);
			_root.undo.append(UndoStack.prototype.undo_movepoi,
							  new Array(this,this.originalx,this.originaly),
							  "moving a POI");
		}
	};
	POI.prototype.select=function() {
		_root.panel.properties.saveAttributes();
		_root.poiselected=this._name;
		setTypeText("Point",this._name);
		_root.panel.properties.init('POI',getPanelColumns(),4);
		_root.panel.presets.init(_root.panel.properties);
		updateButtons();
		updateScissors(false);
		highlightSquare(this._x,this._y,8/Math.pow(2,Math.min(_root.scale,16)-13));
		redrawRelationsForMember('node', this._name);
	};
	// POI.prototype.recolour=function() { };
	// ** above will recolour as red/green depending on whether locked
	
	Object.registerClass("poi",POI);

	function resizePOIs() {
		var n=Math.max(100/Math.pow(2,_root.scale-13),6.25);
		for (var qpoi in _root.map.pois) {
			_root.map.pois[qpoi]._xscale=_root.map.pois[qpoi]._yscale=n;
		}
		if (_root.poiselected) {
			highlightSquare(_root.map.pois[poiselected]._x,_root.map.pois[poiselected]._y,8/Math.pow(2,Math.min(_root.scale,16)-13));
		}
	}


	// purgePOIs - remove any clean POIs outside current view

	function purgePOIs() {
		var coord_l=long2coord(edge_l); var coord_t=lat2coord(edge_t);
		var coord_r=long2coord(edge_r); var coord_b=lat2coord(edge_b);
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
