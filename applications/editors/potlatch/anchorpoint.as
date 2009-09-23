
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
			_root.map.pois.attachMovie("poi",--newnodeid,++poidepth);
			_root.map.pois[newnodeid]._x=_root.map._xmouse;
			_root.map.pois[newnodeid]._y=_root.map._ymouse;
			_root.map.pois[newnodeid].select();
			_root.map.pois[newnodeid].clean=false;
			markClean(false);
			_root.undo.append(UndoStack.prototype.undo_createpoi,
							  [_root.map.pois[newnodeid]],iText('action_createpoi'));

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
		setTypeText(iText('point'),this.node);
		removeIconPanel();
		_root.panel.properties.init('point',getPanelColumns(),4);
		_root.panel.presets.init(_root.panel.properties);
		updateButtons();
		updateScissors(true);
		updateInspector();
		setTooltip(iText('hint_pointselected'),0);
	};

	AnchorPoint.prototype.beginDrag=function() {
		this.onMouseMove=function() { this.trackDrag(); };
		this.onMouseUp  =function() { this.endDrag();   };
		_root.firstxmouse=_root.map._xmouse;
		_root.firstymouse=_root.map._ymouse;
		_root.oldx=_root.ws.path[this._name].x;
		_root.oldy=_root.ws.path[this._name].y;
		this.oldclean=_root.ws.path[this._name].clean;
		clearFloater();
	};

	AnchorPoint.prototype.trackDrag=function() {
		this._x=_root.map._xmouse;
		this._y=_root.map._ymouse;
		_root.ws.path[this._name].moveTo(this._x,this._y,undefined,true);
		_root.ws.highlight();
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
			_root.ws.path[this._name].x=_root.oldx;	// just so it's saved for undo
			_root.ws.path[this._name].y=_root.oldy;	//  |
			_root.undo.append(UndoStack.prototype.undo_movenode,
							  new Array(deepCopy(_root.ws.path[this._name])),
							  iText('action_movepoint'));
			_root.ws.path[this._name].moveTo(newx,newy,undefined);
			_root.ws.highlightPoints(5000,"anchor");
			_root.ws.highlight();
			_root.ws.redraw();
			_root.ws.clean=false;
			markClean(false);

		} else {
			_root.ws.path[this._name].moveTo(_root.oldx,_root.oldy,undefined);	// Return point to original position
			_root.ws.path[this._name].clean=this.oldclean;						//  | (don't make dirty)
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
					setTooltip(iText('hint_drawmode'),0);
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
		var a=getName(_root.nodes[this.node].attr,nodenames); if (a) { setFloater(a); }
	};
	
	AnchorPoint.prototype.joinNodes=function() {
		var t=new Object(); t.x=this._x; t.y=this._y;
		_root.map.localToGlobal(t);

		var waylist=new Array(); var poslist=new Array();
		for (qway in _root.map.ways) {
			if (_root.map.ways[qway].hitTest(t.x,t.y,true) && qway!=this.way._name && !_root.nodes[this.node].ways[qway]) {
				poslist.push(_root.map.ways[qway].insertAnchorPoint(_root.nodes[this.node]));
				waylist.push(_root.map.ways[qway]);
			}
		}
		if (poslist.length==0) { return; }
		_root.undo.append(UndoStack.prototype.undo_addpoint,
						  new Array(waylist,poslist), iText('action_insertnode'));
		_root.ws.highlightPoints(5000,"anchor");
		updateInspector();
	};
	
	AnchorPoint.prototype.onRollOut=function() { clearFloater(); };

	Object.registerClass("anchor",AnchorPoint);
	Object.registerClass("anchor_junction",AnchorPoint);



	// =====================================================================================
	// OOP classes - AnchorHint

	function AnchorHint() {
		this.way=null;
		this.node=0;
	};
	AnchorHint.prototype=new MovieClip();
	AnchorHint.prototype.onRollOver=function() {
		if (this._name==0 || this._name==this.way.path.length-1) {
			setTooltip(iText('hint_overendpoint',this.node));
			setPointer('peno');
		} else {
			setTooltip(iText('hint_overpoint',this.node));
			setPointer('penx');
		}
		var a=getName(_root.nodes[this.node].attr,nodenames); if (a) { setFloater(a); }
	};
	AnchorHint.prototype.onRollOut=function() {
		clearTooltip();
		clearFloater();
	};

	AnchorHint.prototype.onPress=function() {
		if (this.way.historic) {
			_root.junction=true;
			restartElastic(); return;	// can't merge/join to historic ways
		}
		var i,z;
		if (Key.isDown(Key.SHIFT) && (this._name==0 || this._name==this.way.path.length-1)) {
			// Merge ways
			var w=this.way;
			addEndPoint(_root.nodes[this.node]);
			_root.drawpoint=-1; clearTooltip();
			_root.map.elastic.clear();
			mergeWayKeepingID(w,_root.ws);
		} else { 
			// Join ways (i.e. junction)
			addEndPoint(_root.nodes[this.node]);
			_root.junction=true;						// flag to prevent elastic band stopping on _this_ mouseUp
			restartElastic();
		}
	};
	Object.registerClass("anchorhint",AnchorHint);
	Object.registerClass("anchorhint_junction",AnchorHint);

