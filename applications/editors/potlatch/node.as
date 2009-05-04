
	// Originally 0=x, 1=y, 2=id, 4=tags;
	// now .x, .y, .id, .attr

	function Node(id,x,y,attr,version) {
		this.id=id;
		this.x=x;
		this.y=y;
		this.attr=attr;
		this.tagged=hasTags(attr);
		this.ways=new Object();
		this.version=version;
		this.clean=false;		// set to true if just loaded from server
		this.uploading=false;
	};

	Node.prototype.markDirty=function() {
		// doesn't really need to be a discrete function,
		// but keeping it in for now
		this.clean=false;
	};

	Node.prototype.removeFromAllWays=function() {
		var qway,qs,x,y,attr;
		var waylist=new Array(); var poslist=new Array();
		var z=this.ways; for (qway in z) {	// was in _root.map.ways
			for (qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs]==this) {
					waylist.push(qway); poslist.push(qs);
					_root.map.ways[qway].path.splice(qs,1);
					_root.map.ways[qway].deletednodes[this.id]=this.version;
					// needs to be in every way's .deletednodes - if it's just one, the API will refuse
					// to delete it, because it's still in the other (not yet rewritten) way
				}
			}
			_root.map.ways[qway].clean=false;
			_root.map.ways[qway].removeDuplicates();
			if (_root.map.ways[qway].path.length<2) { _root.map.ways[qway].remove(); }
											   else { _root.map.ways[qway].redraw(); }
		}
		if (_root.wayselected) { _root.ws.select(); }
		_root.undo.append(UndoStack.prototype.undo_deletepoint,
						  new Array(deepCopy(this),waylist,poslist),
						  "deleting a point");
	};

	Node.prototype.moveTo=function(newx,newy,ignoreway) {
		this.x=newx; this.y=newy; this.markDirty();
		var qchanged;
		var z=this.ways; for (var qway in z) {
			if (qway!=ignoreway) { _root.map.ways[qway].redraw(); qchanged=qway; }
		}
		return qchanged;	// return ID of last changed way
	};

	Node.prototype.renumberTo=function(id) {
		var old=this.id;
		noderels[id]=noderels[old]; delete noderels[old];
		nodes[id]=new Node(id,this.x,this.y,this.attr,this.version);
		nodes[id].clean=this.clean;
		var z=this.ways; for (var qway in z) {
			nodes[id].addWay(qway);
			for (var qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs].id==old) {
					_root.map.ways[qway].path[qs]=nodes[id];
				}
			}
		}
		var z=_root.map.anchors; for (var a in z) {
			if (_root.map.anchors[a].node==old) { _root.map.anchors[a].node=id; }
		}
		var z=_root.map.anchorhints; for (var a in z) {
			if (_root.map.anchorhints[a].node==old) { _root.map.anchorhints[a].node=id; }
		}
	};

	// ------------------------------------------------------------------------
	// Node->way mapping
	
	Node.prototype.addWay=function(id) { this.ways[id]=true; };
	Node.prototype.removeWay=function(id) { delete this.ways[id]; };
	Node.prototype.numberOfWays=function() { var z=this.ways; var c=0; for (var i in z) { c++; } return c; };

	// ------------------------------------------------------------------------
	// Support functions
	
	// hasTags - does a tag hash contain any significant tags?

	function hasTags(a) {
		var c=false;
		for (var j in a) {
			if (a[j] != '' && j != 'attribution' &&	
				j != 'created_by' && j!='source' &&	
				j.indexOf('tiger:')!=0) { c=true; }
		}
		return c;
	}
