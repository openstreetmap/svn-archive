
	// =====================================================================================
	// OOP classes - OSMWay

	// ----	Initialise
	
	function OSMWay() {
		this.path=new Array();
		// path is an array of points
		// each point is an array: (x,y,node_id,0 move|1 draw,tag array,segment id)
		this.attr=new Array();
		this.clean=true;				// altered since last upload?
		this.uploading=false;			// currently uploading?
		this.locked=false;				// locked against upload?
		this.oldversion=0;				// is this an undeleted, not-uploaded way?
		this.mergedways=new Array();	// list of ways merged into this
		this.xmin=0;
		this.xmax=0;
		this.ymin=0;
		this.ymax=0;
	};
	OSMWay.prototype=new MovieClip();

	// ----	Load from remote server

	OSMWay.prototype.load=function() {
		responder = function() { };
		responder.onResult = function(result) {
			_root.waysreceived+=1;
			var w=result[0];
			if (length(result[1])==0) { removeMovieClip(_root.map.ways[w]); 
										removeMovieClip(_root.map.areas[w]); return; }
			var i,id;
			_root.map.ways[w].clean=true;
			_root.map.ways[w].locked=false;
			_root.map.ways[w].oldversion=0;
			_root.map.ways[w].path=result[1];
			_root.map.ways[w].attr=result[2];
			_root.map.ways[w].xmin=coord2long(result[3]);
			_root.map.ways[w].xmax=coord2long(result[4]);
			_root.map.ways[w].ymin=coord2lat(result[5]);
			_root.map.ways[w].ymax=coord2lat(result[6]);
			_root.map.ways[w].redraw();
			_root.map.ways[w].clearPOIs();
		};
		remote.call('getway',responder,Math.floor(this._name),baselong,basey,masterscale);
	};

	OSMWay.prototype.loadFromDeleted=function(version) {
		delresponder=function() { };
		delresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var i,z;
			var w=result[0];
			_root.map.ways[w].clean=false;
			_root.map.ways[w].oldversion=result[7];
			z=result[1]; // assign negative IDs to anything moved
			for (i in z) {
				if (result[1][i][2]<0) { _root.newnodeid--; result[1][i][2]=newnodeid; }
			}
			_root.map.ways[w].path=result[1];
			_root.map.ways[w].attr=result[2];
			_root.map.ways[w].xmin=result[3];
			_root.map.ways[w].xmax=result[4];
			_root.map.ways[w].ymin=result[5];
			_root.map.ways[w].ymax=result[6];
			if (w==wayselected) { _root.map.ways[w].select(); markClean(false); }
						   else { _root.map.ways[w].locked=true; }
			_root.map.ways[w].redraw();
			_root.map.ways[w].clearPOIs();
		};
		remote.call('getway_old',delresponder,Math.floor(this._name),Math.floor(version),baselong,basey,masterscale);
	};

	OSMWay.prototype.clearPOIs=function() {
		// check if any way nodes are POIs, delete the POIs if so
		var i,z;
		z=this.path;
		for (i in z) {
			if (_root.map.pois[this.path[i][2]]) { removeMovieClip(_root.map.pois[this.path[i][2]]); }
		}
	};

	// ----	Draw line

	OSMWay.prototype.redraw=function() {
		this.createEmptyMovieClip("line",1);					// clear line
		var linewidth=3; //Math.max(2/Math.pow(2,_root.scale-13),0)+1;
		var linealpha=100; // -50*(this.locked==true);
		
		// Set stroke

		if		(this.locked)					 { this.line.lineStyle(linewidth,0xFF0000,linealpha,false,"none"); }
		else if (colours[this.attr["highway"]])  { this.line.lineStyle(linewidth,colours[this.attr["highway" ]],linealpha,false,"none"); }
		else if (colours[this.attr["waterway"]]) { this.line.lineStyle(linewidth,colours[this.attr["waterway"]],linealpha,false,"none"); }
		else if (colours[this.attr["railway"]])  { this.line.lineStyle(linewidth,colours[this.attr["railway" ]],linealpha,false,"none"); }
		else {
			var c=0xAAAAAA; var z=this.attr;
			for (var i in z) { if (i!='created_by' && this.attr[i]!='' && this.attr[i].substr(0,6)!='(type ') { c=0x707070; } }
			this.line.lineStyle(linewidth,c,linealpha,false,"none");
		}
		
		// Draw fill/casing

		var f=this.getFill();
		if ((f>-1 || casing[this.attr['highway']]) && !this.locked) {
			if (!_root.map.areas[this._name]) { _root.map.areas.createEmptyMovieClip(this._name,++areadepth); }
			with (_root.map.areas[this._name]) {
				clear();
				enabled=false;
				moveTo(this.path[0][0],this.path[0][1]); 
				if (f>-1) { beginFill(f,20); }
					 else { lineStyle(linewidth*1.5,0,100,false,"none"); }
				for (var i=1; i<this.path.length; i+=1) {
					lineTo(this.path[i][0],this.path[i][1]);
				}
				if (f>-1) { endFill(); }
			};
		} else if (_root.map.areas[this._name]) {
			removeMovieClip(_root.map.areas[this._name]);
		}

		// Draw line

		this.line.moveTo(this.path[0][0],this.path[0][1]); 
		for (var i=1; i<this.path.length; i+=1) {
			this.line.lineTo(this.path[i][0],this.path[i][1]);
		}

		redrawRelationsForMember('way', this._name);
	};

	OSMWay.prototype.getFill=function() {
		var f=-1; 
		if (this.path[this.path.length-1][0]==this.path[0][0] &&
			this.path[this.path.length-1][1]==this.path[0][1] &&
			this.path.length>2) {
			if (this.attr['area']) { f='0x777777'; }
			var z=this.attr;
			for (var i in z) { if (areas[i] && this.attr[i]!='' && this.attr[i]!='coastline') { f=areas[i]; } }
		}
		return f;
	};

	// ----	Show direction

	OSMWay.prototype.direction=function() {
		if (this.path.length<2) {
			_root.panel.i_circular._visible=false;
			_root.panel.i_direction._visible=true;
			_root.panel.i_direction._alpha=50;
		} else {
			dx=this.path[this.path.length-1][0]-this.path[0][0];
			dy=this.path[this.path.length-1][1]-this.path[0][1];
			if (dx==0 && dy==0) {
				_root.panel.i_circular._visible=true;
				_root.panel.i_direction._visible=false;
			} else {
				_root.panel.i_direction._rotation=180-Math.atan2(dx,dy)*(180/Math.PI)-45;
				_root.panel.i_direction._alpha=100;
				_root.panel.i_direction._visible=true;
				_root.panel.i_circular._visible=false;
			}
		}
	};

	// ----	Remove from server
	
	OSMWay.prototype.remove=function() {
		this.deleteMergedWays();
		memberDeleted('way', this._name);
		if (this._name>=0 && !_root.sandbox && this.oldversion==0) {
			deleteresponder = function() { };
			deleteresponder.onResult = function(result) {
				var code=result.shift(); if (code) { handleError(code,result); return; }
				if (wayselected==result[0]) { deselectAll(); }
				removeMovieClip(_root.map.ways[result[0]]);
				removeMovieClip(_root.map.areas[result[0]]);
			};
			remote.call('deleteway',deleteresponder,_root.usertoken,Math.floor(this._name));
		} else {
			if (this._name==wayselected) { stopDrawing(); deselectAll(); }
			removeMovieClip(_root.map.areas[this._name]);
			removeMovieClip(this);
		}
	};

	// ----	Upload to server
	
	OSMWay.prototype.upload=function() {
		putresponder=function() { };
		putresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var i,r,z,nw,qway,qs;
			nw=result[1];	// new way ID
			if (result[0]!=nw) {
				_root.map.ways[result[0]]._name=nw;
				renumberMemberOfRelation('way', result[0], nw);
				if (_root.map.areas[result[0]]) { _root.map.areas[result[0]]._name=nw; }
				if (_root.panel.t_details.text==result[0]) { _root.panel.t_details.text=nw; _root.panel.t_details.setTextFormat(plainText); }
				if (wayselected==result[0]) { selectWay(nw); }
			}
			_root.map.ways[nw].xmin=result[3];
			_root.map.ways[nw].xmax=result[4];
			_root.map.ways[nw].ymin=result[5];
			_root.map.ways[nw].ymax=result[6];
			_root.map.ways[nw].uploading=false;
			_root.map.ways[nw].oldversion=0;

			// check if renumbered nodes occur in any other ways
			for (qway in _root.map.ways) {
				for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
					if (result[2][_root.map.ways[qway].path[qs][2]]) {
						_root.map.ways[qway].path[qs][2]=result[2][_root.map.ways[qway].path[qs][2]];
					}
				}
			}
			// check if renumbered nodes are part of relations
			for ( var oid in result[2] ) {
				var nid = result[2][oid];
				renumberMemberOfRelation('node', oid, nid);
			}
			_root.map.ways[nw].clearPOIs();
			_root.map.ways[nw].deleteMergedWays();
		};
		if (!this.uploading && !this.locked && !_root.sandbox && this.path.length>1) {
			this.attr['created_by']=_root.signature;
			this.uploading=true;
			remote.call('putway',putresponder,_root.usertoken,Math.floor(this._name),this.path,this.attr,this.oldversion,baselong,basey,masterscale);
			this.clean=true;
		}
	};

	// ---- Delete any ways merged into this one

	OSMWay.prototype.deleteMergedWays=function() {
		while (this.mergedways.length>0) {
			var i=this.mergedways.shift();
			_root.map.ways.attachMovie("way",i,++waydepth);	// can't remove unless the movieclip exists!
			_root.map.ways[i].remove();
		}
	};

	// ----	Revert to copy in database
	
	OSMWay.prototype.reload=function() {
		_root.waysrequested+=1;
		while (this.mergedways.length>0) {
			var i=this.mergedways.shift();
			_root.waysrequested+=1;
			_root.map.ways.attachMovie("way",i,++waydepth);
			_root.map.ways[i].load();
		}
		this.load();
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
		if (Key.isDown(Key.SHIFT) && this._name==_root.wayselected && _root.drawpoint==-1) {
			// shift-click current way: insert point
			_root.pointselected=insertAnchorPoint(this._name);
			this.highlightPoints(5000,"anchor");
			_root.map.anchors[pointselected].beginDrag();
		} else if (Key.isDown(Key.SHIFT) && _root.wayselected && this.name!=_root.wayselected && _root.drawpoint==-1) {
			// shift-click other way: merge two ways
			var selstart =_root.ws.path[0][2];
			var sellen   =_root.ws.path.length-1;
			var selend   =_root.ws.path[sellen][2];
			var thisstart=_root.map.ways[this._name ].path[0][2];
			var thislen  =_root.map.ways[this._name ].path.length-1;
			var thisend  =_root.map.ways[this._name ].path[thislen][2];
			if      (selstart==thisstart) { _root.ws.mergeWay(0,_root.map.ways[this._name],0); }
			else if (selstart==thisend  ) { _root.ws.mergeWay(0,_root.map.ways[this._name],thislen); }
			else if (selend  ==thisstart) { _root.ws.mergeWay(sellen,_root.map.ways[this._name],0); }
			else if (selend  ==thisend  ) { _root.ws.mergeWay(sellen,_root.map.ways[this._name],thislen); }
			else						  { return; }
			_root.ws.redraw();
//			_root.ws.upload();
//			_root.map.ways[this._name ].remove(wayselected);
			_root.ws.select();
		} else if (_root.drawpoint>-1) {
			// click other way while drawing: insert point as junction
			if (this.oldversion==0) {
				if (this._name==_root.wayselected && _root.drawpoint>0) {
					_root.drawpoint+=1;	// inserting node earlier into the way currently being drawn
				}
				insertAnchorPoint(this._name);
				this.highlightPoints(5001,"anchorhint");
				addEndPoint(_root.map._xmouse,_root.map._ymouse,newnodeid);
			}
			_root.junction=true;
			restartElastic();
		} else {
			// click way: select
			this.select();
			clearTooltip();
		}
	};
	
	// ----	Select/highlight
	
	OSMWay.prototype.select=function() {
		if (_root.wayselected!=this._name || _root.poiselected!=0) { uploadSelected(); }
		_root.panel.properties.saveAttributes();
		selectWay(this._name);
		_root.pointselected=-2;
		_root.poiselected=0;
		this.highlightPoints(5000,"anchor");
		removeMovieClip(_root.map.anchorhints);
		this.highlight();
		setTypeText("Way",this._name);
		_root.panel.properties.init('way',getPanelColumns(),4);
		_root.panel.presets.init(_root.panel.properties);
		updateButtons();
		updateScissors(false);
	};
	
	OSMWay.prototype.highlight=function() {
		_root.map.createEmptyMovieClip("highlight",5);
		if (_root.pointselected>-2) {
			highlightSquare(_root.map.anchors[pointselected]._x,_root.map.anchors[pointselected]._y,8/Math.pow(2,Math.min(_root.scale,17)-13));
		} else {
			var linewidth=11;
			var linecolour=0xFFFF00; if (this.locked) { var linecolour=0x00FFFF; }
			_root.map.highlight.lineStyle(linewidth,linecolour,80,false,"none");
			_root.map.highlight.moveTo(this.path[0][0],this.path[0][1]);
			for (var i=1; i<this.path.length; i+=1) {
				_root.map.highlight.lineTo(this.path[i][0],this.path[i][1]);
			}
		}
		this.direction();
	};

	OSMWay.prototype.highlightPoints=function(d,atype) {
		anchorsize=120/Math.pow(2,_root.scale-13);
		group=atype+"s";
		_root.map.createEmptyMovieClip(group,d);
		for (var i=0; i<this.path.length; i+=1) {
			_root.map[group].attachMovie(atype,i,i);
			_root.map[group][i]._x=this.path[i][0];
			_root.map[group][i]._y=this.path[i][1];
			_root.map[group][i]._xscale=anchorsize;
			_root.map[group][i]._yscale=anchorsize;
			_root.map[group][i].node=this.path[i][2];
			_root.map[group][i].way=this._name;
		}
	};

	// ----	Split, merge, reverse

	OSMWay.prototype.splitWay=function() {
		var i,z;
		if (pointselected>0 && pointselected<(this.path.length-1) && this.oldversion==0) {
			_root.newwayid--;											// create new way
			_root.map.ways.attachMovie("way",newwayid,++waydepth);		//  |

			z=this.path;												// copy path array
			for (i in z) {												//  | (deep copy)
				_root.map.ways[newwayid].path[i]=new Array();			//  |
				for (j=0; j<=5; j+=1) { _root.map.ways[newwayid].path[i][j]=this.path[i][j]; }
			}															// | 
			z=this.attr; for (i in z) { _root.map.ways[newwayid].attr[i]=z[i]; }

			z=getRelationsForWay(this._name);							// copy relations
			for (i in z) {												//  | 
				_root.map.relations[z[i]].setWayRole(newwayid,_root.map.relations[z[i]].getWayRole(this._name));
			}															//  |

			this.path.splice(Math.floor(pointselected)+1);				// current way
			this.redraw();												//  |

			_root.map.ways[newwayid].path.splice(0,pointselected);		// new way
			_root.map.ways[newwayid].locked=this.locked;				//  |
			_root.map.ways[newwayid].redraw();							//  |
			_root.map.ways[newwayid].upload();							//  |

			this.upload();												// upload current way
			pointselected=-2;											//  |
			this.select();												//  |
			uploadDirtyRelations();
		};
	};

	//		Merge (start/end of this way,other way object,start/end of other way)
	// ** needs to not add duplicate points

	OSMWay.prototype.mergeWay=function(topos,otherway,frompos) {
		var i,z;
		if (this.oldversion>0 || otherway.oldversion>0) { return; }

		if (frompos==0) { for (i=0; i<otherway.path.length;    i+=1) { this.addPointFrom(topos,otherway,i); } }
				   else { for (i=otherway.path.length-1; i>=0; i-=1) { this.addPointFrom(topos,otherway,i); } }

		z=otherway.attr;
		for (i in z) {
			if (otherway.attr[i].substr(0,6)=='(type ') { otherway.attr[i]=null; }
			if (this.attr[i].substr(0,6)=='(type ') { this.attr[i]=null; }
			if (this.attr[i]!=null) {
				if (this.attr[i]!=otherway.attr[i] && otherway.attr[i]!=null) { this.attr[i]+='; '+otherway.attr[i]; }
			} else {
				this.attr[i]=otherway.attr[i];
			}
			if (!this.attr[i]) { delete this.attr[i]; }
		}

		z=getRelationsForWay(otherway._name);						// copy relations
		for (i in z) {												//  | 
			if (!_root.map.relations[z[i]].hasWay(this._name)) {	//  |
				_root.map.relations[z[i]].setWayRole(this._name,_root.map.relations[z[i]].getWayRole(otherway._name));
			}														//  |
		}															//  |

		this.mergedways.push(otherway._name);
		this.mergedways.concat(otherway.mergedways);
		this.clean=false;
		markClean(false);
		if (otherway.locked) { this.locked=true; }
		removeMovieClip(_root.map.areas[otherway._name]);
		removeMovieClip(otherway);
		if (this._name==_root.wayselected) { 
			_root.panel.properties.reinit();
		}
	};
	OSMWay.prototype.addPointFrom=function(topos,otherway,srcpt) {
		if (topos==0) { if (this.path[0					][2]==otherway.path[srcpt][2]) { return; } }	// don't add duplicate points
				 else { if (this.path[this.path.length-1][2]==otherway.path[srcpt][2]) { return; } }	//  |
		var newpoint=new Array(otherway.path[srcpt][0],
							   otherway.path[srcpt][1],
							   otherway.path[srcpt][2],null,
							   otherway.path[srcpt][4]);
		if (topos==0) { this.path.unshift(newpoint); }
			     else { this.path.push(newpoint); }
	};

	// ---- Reverse order
	
	OSMWay.prototype.reverseWay=function() {
		if (this.path.length<2) { return; }
		if (_root.drawpoint>-1) { _root.drawpoint=(this.path.length-1)-_root.drawpoint; }
		this.path.reverse();
		this.redraw();
		this.direction();
		this.select();
		this.clean=false;
		markClean(false);
	};

	// ----	Check for duplicates (e.g. when C is removed from ABCB)
	
	OSMWay.prototype.removeDuplicates=function() {
		var z=this.path; var ch=false;
		for (var i in z) {
			if (i>0) {
				if (this.path[i][2]==this.path[i-1][2]) { this.path.splice(i,1); ch=true; }
			}
		}
		return ch;
	};

	Object.registerClass("way",OSMWay);


	// =====================================================================================
	// Drawing support functions

	// insertAnchorPoint		- add point into way with SHIFT-clicking
	
	function insertAnchorPoint(way) {
		var nx,ny,closest,closei,i,x1,y1,x2,y2,direct,via,newpoint;
		nx=_root.map._xmouse;	// where we're inserting it
		ny=_root.map._ymouse;	//	|
		closest=0.05; closei=0;
		for (i=0; i<(_root.map.ways[way].path.length)-1; i+=1) {
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
		_root.newnodeid--;
		newpoint=new Array(nx,ny,newnodeid,1,new Array(),0);
		_root.map.ways[way].path.splice(closei,0,newpoint);
		_root.map.ways[way].clean=false;
		_root.map.ways[way].redraw();
		markClean(false);
		return closei;
	}

	// startNewWay	- create new way with first point x,y,node

	function startNewWay(x,y,node) {
		uploadSelected();
		_root.newwayid--;
		newpoint=new Array(x,y,node,0,new Array(),0);
		selectWay(newwayid);
		_root.poiselected=0;
		_root.map.ways.attachMovie("way",newwayid,++waydepth);
		_root.map.ways[newwayid].path[0]=newpoint;
		_root.map.ways[newwayid].redraw();
		_root.map.ways[newwayid].select();
		_root.map.ways[newwayid].clean=false;
		_root.map.anchors[0].startElastic();
		_root.drawpoint=0;
		markClean(false);
		setTooltip("click to add point\ndouble-click/Return\nto end line",0);
	}

	// addEndPoint - add point to start/end of line

	function addEndPoint(x,y,node,tags) {
		if (tags) {} else { tags=new Array(); }
		newpoint=new Array(x,y,node,0,tags,0);
		x1=_root.ws.path[_root.drawpoint][0];
		y1=_root.ws.path[_root.drawpoint][1];
		if (_root.drawpoint==_root.ws.path.length-1) {
			_root.ws.path.push(newpoint);
			_root.drawpoint=_root.ws.path.length-1;
		} else {
			_root.ws.path.unshift(newpoint);	// drawpoint=0, add to start
		}
	
		// Redraw line (if possible, just extend it to save time)
		if (_root.ws.getFill()>-1 || 
			_root.ws.path.length<3 ||
			_root.pointselected>-2) {
			_root.ws.redraw();
			_root.ws.select();
		} else {
			_root.ws.line.moveTo(x1,y1);
			_root.ws.line.lineTo(x,y);
			if (casing[_root.ws.attr['highway']]) {
				_root.map.areas[wayselected].moveTo(x1,y1);
				_root.map.areas[wayselected].lineTo(x,y);
			}
			_root.map.highlight.moveTo(x1,y1);
			_root.map.highlight.lineTo(x,y);
			_root.ws.direction();
			_root.ws.highlightPoints(5000,"anchor");
			removeMovieClip(_root.map.anchorhints);
		}
		redrawRelationsForMember('way',_root.wayselected);
		// Mark as unclean
		_root.ws.clean=false;
		markClean(false);
		_root.map.elastic.clear();
	}

	function stopDrawing() {
		_root.map.anchors[_root.drawpoint].endElastic();
		_root.drawpoint=-1;
		if (_root.ws.path.length<=1) { 
			// way not long enough, so abort
			removeMovieClip(_root.map.areas[wayselected]);
			removeMovieClip(_root.ws);
			removeMovieClip(_root.map.anchors);
		}
		_root.map.elastic.clear();
		clearTooltip();
	};

	// cycleStacked	- cycle through ways sharing same point

	function cycleStacked() {
		if (_root.pointselected>-2) {
			var id=_root.ws.path[_root.pointselected][2];
			var firstfound=0; var nextfound=0;
			for (qway in _root.map.ways) {
				if (qway!=_root.wayselected) {
					for (qs=0; qs<_root.map.ways[qway]["path"].length; qs+=1) {
						if (_root.map.ways[qway].path[qs][2]==id) {
							var qw=Math.floor(qway);
							if (firstfound==0 || qw<firstfound) { firstfound=qw; }
							if ((nextfound==0 || qw<nextfound) && qw>wayselected) { nextfound=qw; }
						}
					}
				}
			}
			if (firstfound) {
				if (nextfound==0) { var nextfound=firstfound; }
				_root.map.ways[nextfound].swapDepths(_root.ws);
				_root.map.ways[nextfound].select();
			}
		}
	};

	// ================================================================
	// Way communication
	
	// whichWays	- get list of ways from remoting server

	function whichWays() {
		_root.lastwhichways=new Date();
		if (_root.waycount>500) { purgeWays(); }
		if (_root.poicount>500) { purgePOIs(); }
		if (_root.edge_l>_root.bigedge_l &&
			_root.edge_r<_root.bigedge_r &&
			_root.edge_b>_root.bigedge_b &&
			_root.edge_t<_root.bigedge_t) {
			// we have already loaded this area, so ignore
		} else {
			whichresponder=function() {};
			whichresponder.onResult=function(result) {
				_root.whichreceived+=1;
				waylist  =result[0];
				pointlist=result[1];
				relationlist=result[2];

				for (i in waylist) {										// ways
					way=waylist[i];											//  |
					if (!_root.map.ways[way]) {								//  |
						_root.map.ways.attachMovie("way",way,++waydepth);	//  |
						_root.map.ways[way].load();							//  |
						_root.waycount+=1;									//  |
						_root.waysrequested+=1;								//  |
					}
				}
				
				for (i in pointlist) {										// POIs
					point=pointlist[i][0];									//  |
					if (!_root["map"]["pois"][point]) {						//  |
						_root.map.pois.attachMovie("poi",point,++poidepth);	//  |
						_root.map.pois[point]._x=pointlist[i][1];			//  |
						_root.map.pois[point]._y=pointlist[i][2];			//  |
						_root.map.pois[point]._xscale=
						_root.map.pois[point]._yscale=Math.max(100/Math.pow(2,_root.scale-13),6.25);
						_root.map.pois[point].attr=pointlist[i][3];			//  |
						_root.poicount+=1;									//  |
					}
				}

				for (i in relationlist) {
					rel = relationlist[i];
                    if (!_root.map.relations[rel]) {
						_root.map.relations.attachMovie("relation",rel,++reldepth);
						_root.map.relations[rel].load();
						_root.relcount+=1;
						_root.relsrequested+=1;
					}
                }
			};
			remote.call('whichways',whichresponder,_root.edge_l,_root.edge_b,_root.edge_r,_root.edge_t,baselong,basey,masterscale);
			_root.bigedge_l=_root.edge_l; _root.bigedge_r=_root.edge_r;
			_root.bigedge_b=_root.edge_b; _root.bigedge_t=_root.edge_t;
			_root.whichrequested+=1;
		}
	}

	// purgeWays - remove any clean ways outside current view
	
	function purgeWays() {
		for (qway in _root.map.ways) {
			if (qway==_root.wayselected) {
			} else if (!_root.map.ways[qway].clean) {
				_root.map.ways[qway].upload();
				uploadDirtyRelations();
			} else if (((_root.map.ways[qway].xmin<edge_l && _root.map.ways[qway].xmax<edge_l) ||
						(_root.map.ways[qway].xmin>edge_r && _root.map.ways[qway].xmax>edge_r) ||
					    (_root.map.ways[qway].ymin<edge_b && _root.map.ways[qway].ymax<edge_b) ||
						(_root.map.ways[qway].ymin>edge_t && _root.map.ways[qway].ymax>edge_t))) {
				removeMovieClip(_root.map.ways[qway]);
				removeMovieClip(_root.map.areas[qway]);
				_root.waycount-=1;
			}
		}
		_root.bigedge_l=_root.edge_l; _root.bigedge_r=_root.edge_r;
		_root.bigedge_b=_root.edge_b; _root.bigedge_t=_root.edge_t;
	}

	function selectWay(id) {
		_root.wayselected=Math.floor(id);
		_root.ws=_root.map.ways[id];
	}
