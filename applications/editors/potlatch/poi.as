
	// =====================================================================================
	// OOP classes - POI
	
	// ----	Initialise
	
	function POI() {
		this.attr=new Object();
		this.clean=true;
		this.uploading=false;
		this.locked=false;
		this.icon='poi';
		this.version=0;
		this._xscale=this._yscale=Math.max(100/Math.pow(2,_root.scale-13),6.25);
	};
	POI.prototype=new MovieClip();
	POI.prototype.remove=function() {
		memberDeleted('Node', this._name);
		uploadDirtyRelations();
		if (this._name>=0 && !this.locked) {
			if (_root.sandbox) {
				_root.poistodelete[this._name]=[this.version,coord2long(this._x),coord2lat(this._y),deepCopy(this.attr)];
				markClean(false);
			} else {
				renewChangeset();
				poidelresponder = function() { };
				poidelresponder.onResult = function(result) { deletepoiRespond(result); };
				_root.writesrequested++;
				remote_write.call('putpoi',poidelresponder,_root.usertoken,_root.changeset,Number(this.version),Number(this._name),coord2long(this._x),coord2lat(this._y),this.attr,0);
			}
		}
		if (this._name<0 || _root.sandbox) {
			if (this._name==poiselected) { deselectAll(); }
			removeMovieClip(this);
		}
	};
	function deletepoiRespond(result) {
		_root.writesrequested--;
		var code=result.shift(); var msg=result.shift(); if (code) { handleError(code,msg,result); return; }
		if (poiselected==result[0]) { deselectAll(); }
		delete _root.nodes[result[0]];
		removeMovieClip(_root.map.pois[result[0]]);
		operationDone(result[0]);
	}
	POI.prototype.reload=function(timestamp) {
		poirelresponder=function() {};
		poirelresponder.onResult=function(result) {
			var code=result.shift(); var msg=result.shift(); if (code) { handleError(code,msg,result); return; }
			_root.map.pois[result[0]]._x  =long2coord(result[1]);
			_root.map.pois[result[0]]._y  =lat2coord (result[2]);
			_root.map.pois[result[0]].attr=result[3];
			_root.map.pois[result[0]].version=result[4];
			_root.map.pois[result[0]].select();
		};
		if (!timestamp) { timestamp=''; }
		remote_read.call('getpoi',poirelresponder,Math.floor(this._name),timestamp);
	};
	POI.prototype.upload=function() {
		poiresponder=function() { };
		poiresponder.onResult=function(result) {
			_root.writesrequested--;
			var code=result.shift(); var msg=result.shift(); if (code) { handleError(code,msg,result); return; }
			var ni=result[1];	// new POI ID
			if (result[0]!=ni) {
				_root.map.pois[result[0]]._name=ni;
				noderels[ni]=noderels[result[0]]; delete noderels[result[0]];
				renumberMemberOfRelation('Node', result[0], ni);
				if (poiselected==result[0]) {
					poiselected=ni;
					if (_root.panel.t_details.text==result[0]) { _root.panel.t_details.text=ni; _root.panel.t_details.setTextFormat(plainText); }
				}
			}
			_root.map.pois[ni].clean=true;
			_root.map.pois[ni].uploading=false;
			_root.map.pois[ni].version=result[2];
			operationDone(result[0]);
		};
		if (!this.uploading && !this.locked && (!_root.sandbox || _root.uploading)) {
			renewChangeset();
			_root.writesrequested++;
			this.uploading=true;
			remote_write.call('putpoi',poiresponder,_root.usertoken,_root.changeset,Number(this.version),Number(this._name),coord2long(this._x),coord2lat(this._y),this.attr,1);
		}
	};
	POI.prototype.onRollOver=function() {
		setPointer('');
		var a=getName(this.attr,nodenames); if (a) { setFloater(a); }
	};
	POI.prototype.onRollOut=function() { clearFloater(); };
	POI.prototype.onPress=function() {
		removeWelcome(true);
		if (_root.drawpoint>-1) {
			// add POI to way
			_root.nodes[this._name]=new Node(this._name,this._x,this._y,this.attr,this.version);
			addEndPoint(_root.nodes[this._name]);
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
		clearFloater();
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
		delete this.onMouseMove;
		delete this.onMouseUp;
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
							  iText("moving a POI",'action_movepoi'));
		}
	};
	POI.prototype.select=function() {
		_root.pointselected=-2;
		_root.wayselected=0;
		_root.panel.properties.tidy();
		_root.panel.properties.saveAttributes();
		_root.poiselected=this._name;
		setTypeText(iText("Point",'point'),this._name);
		removeIconPanel();
		_root.panel.properties.init('POI',getPanelColumns(),4);
		_root.panel.presets.init(_root.panel.properties);
		updateButtons();
		updateScissors(false);
		this.highlight();
		var z=_root.noderels[this._name];
		for (var rel in z) { _root.map.relations[rel].redraw(); }
	};
	POI.prototype.recolour=function() { 
		this.redden=new Color(this);
		if (this.locked) { this.redden.setTransform(to_red); }
					else { this.redden.setTransform(to_normal); }
	};
	
	POI.prototype.saveUndo=function(str) {
		_root.undo.append(UndoStack.prototype.undo_deletepoi,
						  new Array(this._name,this._x,this._y,
									deepCopy(this.attr)),iText("$1 a POI",'a_poi',str));
	};

	POI.prototype.highlight=function() {
		var s=8/Math.pow(2,Math.min(_root.scale,16)-13);
		if (this.icon!="poi") { s*=1.3; }
		highlightSquare(this._x,this._y,s);
	};
	
	POI.prototype.redraw=function() {
		var a=getPOIIcon(this.attr);
		if (this.icon==a) { return; }
		replaceIcon(this,a);
	};

	Object.registerClass("poi",POI);



	// ==============================================================
	// Support functions

	function getName(attr,namelist) {
		var a='';
		if (attr['name'] && attr['name'].substr(0,6)!='(type ') {
			a=attr['name'];
		} else {
			for (var i in namelist) {
				if (attr[namelist[i]]) { a=attr[namelist[i]]; }
			}
		}
		if (attr['ref']) { a=attr['ref']+" "+a; }
		if (attr['tiger:reviewed']=='aerial') { a+="*"; }
		return a;
	}

	function resizePOIs() {
		var n=Math.max(100/Math.pow(2,_root.scale-13),6.25);
		for (var qpoi in _root.map.pois) {
			_root.map.pois[qpoi]._xscale=_root.map.pois[qpoi]._yscale=n;
		}
		if (_root.poiselected) {
			_root.map.pois[poiselected].highlight();
		}
		for (var qp in _root.map.photos) {
			_root.map.photos[qp]._xscale=_root.map.photos[qp]._yscale=n;
		}
	}
	
	function getPOIIcon(attr) {
		var a='poi';
		for (var i in icontags) {
			var ok=true;
			var t=icontags[i]; for (var k in t) {
				if (!attr[k] || attr[k]!=icontags[i][k]) { ok=false; }
			}
			if (ok) { a='poi_'+i; }
		}
		return a;
	}

	function replaceIcon(poi,newicon) {
		var x=poi._x; var y=poi._y;
		var s=poi._xscale; var v=poi.version; var a=deepCopy(poi.attr);
		var d=poi.getDepth(); var n=poi._name; var c=poi.clean;
		_root.map.pois.attachMovie(newicon,n,d);
		poi=_root.map.pois[n];
		poi._x=x; poi._y=y;
		poi._xscale=poi._yscale=s;
		poi.version=v; poi.attr=a; poi.icon=newicon; poi.clean=c;
		if (n==_root.poiselected) { poi.highlight(); }
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
