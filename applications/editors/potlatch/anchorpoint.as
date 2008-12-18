
	// =====================================================================================
	// OOP classes - AnchorPoint
	// click behaviour:
	// - click and drag: move point
	// - click (not drag) start/end point: go into draw mode extend line
	
	function AnchorPoint() {
		this.way=null;
		this.node=0;
	};
	AnchorPoint.prototype=new MovieClip();
	AnchorPoint.prototype.onPress=function() {
		removeWelcome(true);
		var t=new Date();
		if (this._name==0 && this.way.path.length==1) {
			// solo double-click - create new POI
			stopDrawing();
			_root.map.pois.attachMovie("poi",--newpoiid,++poidepth);
			_root.map.pois[newpoiid]._x=_root.map._xmouse;
			_root.map.pois[newpoiid]._y=_root.map._ymouse;
			_root.map.pois[newpoiid].select();
			_root.map.pois[newpoiid].clean=false;
			markClean(false);
			_root.undo.append(UndoStack.prototype.undo_createpoi,
							  [_root.map.pois[newpoiid]],iText("creating a POI",'action_createpoi'));

		} else if (Key.isDown(Key.SHIFT) && !this.way.historic) {
			_root.junction=true;				// flag to prevent elastic band stopping on _this_ mouseUp
			startNewWay(this.node);
		} else if (this._name==_root.drawpoint ||
				  (this._name==_root.lastpoint && (t.getTime()-_root.lastpointtime)<700)) {
			// double-click at end of route
			_root.lastpoint=_root.drawpoint;	// trap triple-click
			_root.lastpointtime=new Date();		//  |
			stopDrawing();
		} else {
//			_root.lastxmouse=_root._xmouse;
//			_root.lastymouse=_root._ymouse;
			_root.clicktime=new Date();
			this.beginDrag();
			this.select();
		}
	};

	AnchorPoint.prototype.select=function() {
		_root.panel.properties.tidy();
		_root.panel.properties.saveAttributes();
		_root.pointselected=this._name;
		this.way.highlight();
		setTypeText(iText("Point",'point'),this.node);
_root.chat.text="Node "+this.node+" version "+_root.nodes[this.node].version;
		_root.panel.properties.init('point',getPanelColumns(),4);
		_root.panel.presets.init(_root.panel.properties);
		updateButtons();
		updateScissors(true);
		setTooltip(iText("point selected\n(shift-click point to\nstart new line)",'hint_pointselected'),0);
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
		delete this.onMouseMove;
		delete this.onMouseUp;
		var newx=_root.map._xmouse;
		var newy=_root.map._ymouse;
		var t=new Date();
		var xdist=Math.abs(newx-_root.firstxmouse);
		var ydist=Math.abs(newy-_root.firstymouse);
		var longclick=(t.getTime()-_root.clicktime)>300;

		if ((xdist>=tolerance   || ydist>=tolerance  ) ||
		   ((xdist>=tolerance/2 || ydist>=tolerance/2) && longclick)) {
			// ====	Move existing point
			_root.undo.append(UndoStack.prototype.undo_movenode,
							  new Array(deepCopy(_root.ws.path[this._name])),
							  iText("moving a point",'action_movepoint'));
			_root.ws.path[this._name].moveTo(newx,newy,undefined);
			_root.ws.highlightPoints(5000,"anchor");
			_root.ws.highlight();
			_root.ws.redraw();
			_root.ws.clean=false;
			markClean(false);

		} else {
			this._x=_root.ws.path[this._name].x;	// Return point to original position
			this._y=_root.ws.path[this._name].y;	//  | (in case dragged slightly)
			if ((this._name==0 || this._name==_root.ws.path.length-1) && !Key.isDown(17)) {
				// ===== Clicked at start or end of line
				if (_root.drawpoint==0 || _root.drawpoint==_root.ws.path.length-1) {
					// - Join looping path
					addEndPoint(_root.ws.path[this._name]);
					_root.lastpoint=_root.drawpoint;	// trap triple-click
					_root.lastpointtime=new Date();		//  |
					stopDrawing();
				} else if (_root.drawpoint==-1) {
					// - Start elastic line for adding new point
					setTooltip(iText("click to add point\ndouble-click/Return\nto end line",'hint_drawmode'),0);
					_root.drawpoint=this._name;
					this.startElastic();
				}
	
			} else {
				// ===== Clicked elsewhere in line
				if (_root.drawpoint>-1) {
					addEndPoint(_root.ws.path[this._name]);
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
		_root.map.elastic.lineStyle(Math.min(linewidth,6),0x000000,100,false,"none");
		_root.map.elastic.moveTo(_root.map._xmouse,_root.map._ymouse);
		_root.map.elastic.lineTo(this._x,this._y);
	};
	
	AnchorPoint.prototype.endElastic=function() {
		if (_root.junction) { _root.junction=false; }
					   else { delete this.onMouseMove; 
							  delete this.onMouseUp; }
	};

	function restartElastic() {
		if (_root.drawpoint!=-1) {
			_root.map.anchors[_root.drawpoint].startElastic();
			_root.map.anchors[_root.drawpoint].trackElastic();
		}
	}

	AnchorPoint.prototype.onRollOver=function() {
		if (_root.drawpoint>-1) {
			if (this._name==0 || this._name==this.way.path.length-1) {
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
		this.way=null;
		this.node=0;
	};
	AnchorHint.prototype=new MovieClip();
	AnchorHint.prototype.onRollOver=function() {
		if (this._name==0 || this._name==this.way.path.length-1) {
			setTooltip(iText("over endpoint\nclick to join\nshift-click to merge",'hint_overendpoint'));
			setPointer('peno');
		} else {
			setTooltip(iText("over point\nclick to join",'hint_overpoint'));
			setPointer('penx');
		}
	};
	AnchorHint.prototype.onRollOut=function() {
		clearTooltip();
	};

	AnchorHint.prototype.onPress=function() {
		if (this.way.historic) {
			_root.junction=true;
			restartElastic(); return;	// can't merge/join to historic ways
		}
		var i,z;
		if (Key.isDown(Key.SHIFT)) {
			// Merge ways
			if (this._name==0 || this._name==this.way.path.length-1) {
				_root.ws.mergeWay(_root.drawpoint,this.way,this._name);
				_root.drawpoint=-1;
				_root.ws.redraw();
//				_root.ws.upload();
//				this.way.remove(wayselected);
				clearTooltip();
				_root.map.elastic.clear();
				_root.ws.select();	// removes anchorhints, so must be last
			}
		} else { 
			// Join ways (i.e. junction)
			addEndPoint(_root.nodes[this.node]);
			_root.junction=true;						// flag to prevent elastic band stopping on _this_ mouseUp
			restartElastic();
		}
	};
	Object.registerClass("anchorhint",AnchorHint);

