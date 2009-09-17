
	// =====================================================================================
	// undo.as
	// Potlatch undo stack code
	// =====================================================================================

	function UndoStack() {
		this.sp=0;
	};

	UndoStack.prototype=new Array();

	UndoStack.prototype.append=function(task,params,tooltip) {
		// add to stack
		if (_root.undoing) { return; }
		this[this.sp]=new Array(task,params,tooltip); this.sp++;
		this.setTooltip(tooltip);
	};

	UndoStack.prototype.rollback=function() {
		if (_root.undoing) { return; }		// Stop any routines from adding to the undo stack
		if (this.sp==0) { return; }
		_root.undoing=true;
		var popped=this[this.sp-1];			// Same as this.pop, but works :)
		this.sp--;							//	|
		popped[0].call(this,popped[1]);
		if (this.sp)	 { this.setTooltip(this[this.sp-1][2]); }
					else { this.setTooltip(""); }
		_root.undoing=false;				// Permit adding to undo stack
	};

	UndoStack.prototype.setTooltip=function(str) {
		if (str=='') {
			_root.panel.i_undo.onRollOver=function() { setFloater(iText("Nothing to undo",'tip_noundo')); };
			_root.panel.i_undo._alpha=50;
		} else {
			_root.panel.i_undo.onRollOver=function() { setFloater(iText("Undo $1 (Z)",'tip_undo',str)); };
			_root.panel.i_undo._alpha=100;
		}
	};
	
	UndoStack.prototype.clear=function() { this.sp=0; };


	// ----	Individual undo methods
	
	//		Added point into way

	UndoStack.prototype.undo_addpoint=function(params) {
		var waylist=params[0]; var poslist=params[1]; var w;
		for (var i in waylist) {
			w=waylist[i];
			w.removeAnchorPoint(poslist[i]);
			w.clean=false;
		}
		w.select();
		stopDrawing();
	};

	//		Moved node
	
	UndoStack.prototype.undo_movenode=function(params) {
		var nodecopy=params[0];
		var w=_root.nodes[nodecopy.id].moveTo(nodecopy.x,nodecopy.y,null);
		if (w) { 
			_root.map.ways[w].clean=false;
			_root.map.ways[w].select();
		}
	};
	
	//		Merged ways

	UndoStack.prototype.undo_mergeways=function(params) {
		var way=params[0];
		if (way) {
			way.attr=params[1];
			way.splitWay(params[3],params[2]);
		} else {
			handleError(-1,new Array(iText("Way $1 cannot be found (perhaps you've panned away?) so I can't undo.",'error_noway',way.id)));
			this.clear();
		}
	};

	//		Split way

	UndoStack.prototype.undo_splitway=function(params) {
		var way1=params[0]; var way2=params[1];
		if (way1.mergeAtCommonPoint(way2)) {
			way2.redraw(); way2.select();
		} else {
			handleError(-1,new Array(iText("Ways $1 and $2 don't share a common point any more, so I can't undo the split.",'error_nosharedpoint',way1.id,way2.id)));
			this.clear();
		}
	};

	//		Changed tags
	//		** to do - relations support
	
	UndoStack.prototype.undo_waytags=function(params) {
		var way=params[0]; if (!way) {
			handleError(-1,new Array(iText("Way $1 cannot be found (perhaps you've panned away?) so I can't undo.",'error_noway',way.id)));
			this.clear(); return;
		} else {
			way.attr=params[1];
			way.clean=false;
			way.redraw();
			way.select();
		}
	};
	UndoStack.prototype.undo_pointtags=function(params) {
		var way=params[0]; var point=params[1]; if (!way) {
			handleError(-1,new Array(iText("Way $1 cannot be found (perhaps you've panned away?) so I can't undo.",'error_noway',way.id)));
			this.clear(); return;
		} else {
			way.path[point].attr=params[2];
			way.path[point].markDirty();
			way.clean=false;
			way.select(); _root.map.anchors[point].select();
		}
	};
	UndoStack.prototype.undo_poitags=function(params) {
		var poi=params[0]; if (!poi) {
			handleError(-1,new Array(iText("The POI cannot be found (perhaps you've panned away?) so I can't undo.",'error_nopoi')));
			this.clear(); return;
		} else {
			poi.attr=params[1];
			poi.clean=false;
			poi.select();
		}
	};
	
	//		Removed point from way(s) (at x,y,tags if it no longer exists)

	UndoStack.prototype.undo_deletepoint=function(params) {
		var last;
		var nodecopy=params[0]; var id=nodecopy.id;
		var waylist=params[1];
		var poslist=params[2];

		// create node if no longer in existence
		if (!_root.nodes[id]) {
			newnodeid--; id=newnodeid;
			_root.nodes[id]=new Node(id,nodecopy.x,nodecopy.y,nodecopy.attr,nodecopy.version);
		}

		// reinstate at each place
		for (qway in waylist) {
			last=waylist[qway];	// select last one
			_root.nodes[id].addWay(last);
			_root.map.ways[last].path.splice(poslist[qway],0,_root.nodes[id]);
			_root.map.ways[last].clean=false;
			_root.map.ways[last].redraw();
			var z=_root.map.ways[last].deletednodes; delete z[id];
		}
		stopDrawing();
		_root.map.ways[last].select();
	};

	//		Moved POI

	UndoStack.prototype.undo_movepoi=function(params) {
		var poi=params[0]; if (!poi) {
			handleError(-1,new Array(iText("The POI cannot be found (perhaps you've panned away?) so I can't undo.",'error_nopoi')));
			this.clear(); return;
		} else {
			poi._x=params[1]; poi._y=params[2];
			poi.clean=false; poi.select();
		}
	};

	//		Moved way (and any nodes used in other ways)

	UndoStack.prototype.undo_movenodes=function(params) {
		var way=params[0]; if (!way) {
			handleError(-1,new Array(iText("Way $1 cannot be found (perhaps you've panned away?) so I can't undo.",'error_noway',way.id)));
			this.clear(); return;
		} else {
			way.moveNodes(-params[1],-params[2]);
			way.redraw();
			way.select();
		}
	};

	//		Reversed way

	UndoStack.prototype.undo_reverse=function(params) {
		var way=params[0]; if (!way) {
			handleError(-1,new Array(iText("Way $1 cannot be found (perhaps you've panned away?) so I can't undo.",'error_noway',way.id)));
			this.clear(); return;
		} else {
			way.reverseWay();
		}
	};

	//		Deleted POI

	UndoStack.prototype.undo_deletepoi=function(params) {
		var poi=params[0]; newnodeid--;
		stopDrawing();
		if (_root.map.pois[newnodeid]) {} else {
			_root.map.pois.attachMovie("poi",newnodeid,++poidepth);
		}
		_root.map.pois[newnodeid]._x=params[1];
		_root.map.pois[newnodeid]._y=params[2];
		_root.map.pois[newnodeid].attr=params[3];
		_root.map.pois[newnodeid].select();
		_root.map.pois[newnodeid].clean=false;
		markClean(false);
	};
	
	//		Deleted way

	UndoStack.prototype.undo_deleteway=function(params) {
		var w;
		if (_root.sandbox) { w=params[0]; }
					  else { w=newwayid--; }
		stopDrawing();
		_root.map.ways.attachMovie("way",w,++waydepth);
		_root.map.ways[w]._x=params[1];
		_root.map.ways[w]._y=params[2];
		_root.map.ways[w].attr=params[3];
		if (_root.sandbox) { _root.map.ways[w].path=restorepath(params[4],w); delete _root.waystodelete[w]; }
					  else { _root.map.ways[w].path=renumberDeleted(params[4],params[0],w); }
		_root.map.ways[w].version=params[5];
		_root.map.ways[w].redraw();
		_root.map.ways[w].select();
		_root.map.ways[w].clean=false;
		markClean(false);
	};

	//		Changed path (for tidying)

	UndoStack.prototype.undo_changeway=function(params) {
		var way=params[0]; var w=_root.map.ways[way];
		w.path=restorepath(params[1],way);
		w.deletednodes=params[2];
		w.attr=params[3];
		stopDrawing();
		w.redraw();
		w.select();
		w.clean=false;
		markClean(false);
	};

	function restorepath(path,way) {
		// we need to be careful not to end up with duplicate versions of
		// the same node
		var returnpath=new Array();
		var done=new Object();
		for (var i=0; i<path.length; i++) {
			var id=path[i].id;
			if (!done[id]) {
				if (_root.nodes[id]) {
					_root.nodes[id].x=path[i].x;
					_root.nodes[id].y=path[i].y;
					_root.nodes[id].attr=path[i].attr;
					_root.nodes[id].tagged=hasTags(path[i].attr);
					_root.nodes[id].version=path[i].version;
					_root.nodes[id].clean=false;
					_root.nodes[id].addWay(way);
				} else {
					_root.nodes[id]=deepCopy(path[i]);
				}
				if (_root.nodes[id].numberOfWays()>1) {
					_root.nodes[id].redrawWays();
				}
			}
			returnpath.push(_root.nodes[id]);
			done[id]=true;
		}
		return returnpath;
	}


	//		Created POI

	UndoStack.prototype.undo_createpoi=function(params) {
		poi=params[0]; poi.remove();
		markClean(false);
	};
	
	//		Created ways (e.g. parallel)
	
	UndoStack.prototype.undo_makeways=function(params) {
		for (i in params) { _root.map.ways[params[i]].remove(); }
	};


	// ----	renumberDeleted - give new ids to any nodes with only one way (i.e. deleted)

	function renumberDeleted(path,oldway,newway) {
		for (var i in path) {
			if (path[i].numberOfWays()==1 && path[i].ways[oldway]) {
				newnodeid--;
				path[i].id=newnodeid;
				path[i].version=0; path[i].clean=false;
				path[i].ways=new Object(); path[i].ways[newway]=true;
				_root.nodes[newnodeid]=path[i]; // path[i].renumberTo(newnodeid);
			} else {
				var z=path[i].ways; delete z[oldway]; z[newway]=true;
				_root.nodes[path[i].id]=path[i];
			}
		}
		return path;
	}
