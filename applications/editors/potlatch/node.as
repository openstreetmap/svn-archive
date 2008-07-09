
	// Originally 0=x, 1=y, 2=id, 4=tags;
	// now .x, .y, .id, .attr

	function Node(id,x,y,attr) {
		this.id=id;
		this.x=x;
		this.y=y;
		this.attr=attr;
		this.tagged=hasTags(attr);
	};

	// Node.ways?
	// Node.removeFromWay?
	// Node.addToWay?

	Node.prototype.removeFromAllWays=function() {
		var qway,qs,x,y,attr;
		var waylist=new Array(); var poslist=new Array();
		for (qway in _root.map.ways) {
			var qdirty=false;
			for (qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs]==this.id) {
					waylist.push(qway); poslist.push(qs);
					_root.map.ways[qway].path.splice(qs,1);
					qdirty=true;
				}
			}
			if (qdirty) { _root.map.ways[qway].removeDuplicates(); }
			if (qdirty && _root.map.ways[qway].path.length<2) {
				_root.map.ways[qway].remove();
			} else if (qdirty) {
				_root.map.ways[qway].redraw();
				_root.map.ways[qway].clean=false;
			}
		}
		if (_root.wayselected) { _root.ws.select(); }
		_root.undo.append(UndoStack.prototype.undo_deletepoint,
						  new Array(deepCopy(this),waylist,poslist),
						  "deleting a point");
	};

	Node.prototype.moveTo=function(newx,newy,ignoreway) {
		// ** if this.ways is created, this should loop through it
		this.x=newx; this.y=newy;
		var qchanged;
		for (var qway in _root.map.ways) {
			var qdirty=0;
			for (var qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs]==this.id) { qdirty=1; }
			} 
			if (qdirty && qway!=ignoreway) { _root.map.ways[qway].redraw(); qchanged=qway; }
		}
		return qchanged;	// return ID of last changed way
	};

	Node.prototype.renumberTo=function(id) {
		var old=this.id;
		nodes[id]=new Node(id,this.x,this.y,this.attr);
		delete this;
		for (var qway in _root.map.ways) {
			for (qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs]==old) {
					_root.map.ways[qway].path[qs]=id;
				}
			}
		}
	};


	// ------------------------------------------------------------------------
	// Support functions
	
	// hasTags - does a tag hash contain any significant tags?

	function hasTags(a) {
		var c=false;
		for (var j in a) {
			if (j!='created_by' && a[j]!='' && j!='source' && j.indexOf('tiger:')!=0) { c=true; }
		}
		return c;
	}
