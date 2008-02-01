
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
			markClean(false);
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
					if (_root.map.ways[qway].path[qs][2]==_root.ws.path[this._name][2]) {
						_root.map.ways[qway].path[qs][0]=newx;
						_root.map.ways[qway].path[qs][1]=newy;
						qdirty=1;
					}
				}
				if (qdirty) { _root.map.ways[qway].redraw(); }
			}
			_root.ws.highlightPoints(5000,"anchor");
			_root.ws.highlight();
			_root.ws.clean=false;
			markClean(false);

		} else {
			this._x=_root.ws.path[this._name][0];	// Return point to original position
			this._y=_root.ws.path[this._name][1];	//  | (in case dragged slightly)
			if ((this._name==0 || this._name==_root.ws.path.length-1) && !Key.isDown(17)) {
				// ===== Clicked at start or end of line
				if (_root.drawpoint==0 || _root.drawpoint==_root.ws.path.length-1) {
					// - Join looping path
					addEndPoint(_root.ws.path[this._name][0],
								_root.ws.path[this._name][1],
								_root.ws.path[this._name][2]);
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
					addEndPoint(_root.ws.path[this._name][0],
								_root.ws.path[this._name][1],
								_root.ws.path[this._name][2]);
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
				_root.ws.mergeWay(_root.drawpoint,_root.map.ways[this.way],this._name);
				_root.drawpoint=-1;
				_root.ws.redraw();
//				_root.ws.upload();
//				_root.map.ways[this.way].remove(wayselected);
				clearTooltip();
				_root.map.elastic.clear();
				_root.ws.select();	// removes anchorhints, so must be last
			}
		} else { 
			// Join ways (i.e. junction)
			addEndPoint(this._x,this._y,this.node);
			_root.junction=true;						// flag to prevent elastic band stopping on _this_ mouseUp
			restartElastic();
		}
	};
	Object.registerClass("anchorhint",AnchorHint);


