
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
		var undopoint=false;
		var z=this.ways; for (qway in z) {	// was in _root.map.ways
			for (qs=0; qs<_root.map.ways[qway].path.length; qs+=1) {
				if (_root.map.ways[qway].path[qs]==this) {
					waylist.push(qway); poslist.push(qs);
					_root.map.ways[qway].path.splice(qs,1);
					if (this.id>0) { _root.map.ways[qway].deletednodes[this.id]=this.version; }
					// needs to be in every way's .deletednodes - if it's just one, the API will refuse
					// to delete it, because it's still in the other (not yet rewritten) way
				}
			}
			_root.map.ways[qway].clean=false;
			_root.map.ways[qway].removeDuplicates();
			if (_root.map.ways[qway].path.length<2) {
				_root.map.ways[qway].saveDeleteUndo(iText('deleting'));
				_root.map.ways[qway].remove();
			} else {
				_root.map.ways[qway].redraw();
				undopoint=true;
			}
		}
		if (_root.wayselected) { _root.ws.select(); }
		if (undopoint) { _root.undo.append(UndoStack.prototype.undo_deletepoint,
										   new Array(deepCopy(this),waylist,poslist),
										   iText('action_deletepoint')); }
	};

	Node.prototype.moveTo=function(newx,newy,ignoreway,ignore_oneway) {
		this.x=newx; this.y=newy; this.markDirty();
		var qchanged;
		var z=this.ways; for (var qway in z) {
			if (qway!=ignoreway) { _root.map.ways[qway].redraw(false,ignore_oneway); qchanged=qway; }
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

	Node.prototype.inspect=function() {
		var str = iText('inspector_latlon', Math.floor(coord2lat (this.y)*10000)/10000, Math.floor(coord2long(this.x)*10000)/10000);

		// Status
		if (!this.clean) { str+=iText('inspector_unsaved'); }
		if (this.uploading) { str+=' ' + iText('inspector_uploading'); }
		if (!this.clean) { str+="\n"; }

		// Which ways is this in?
		if (this.numberOfWays()==0) { 
			str+=iText('inspector_not_in_any_ways') + "\n";
		} else {
			str+=iText('inspector_in_ways') + ' ';
			var w=this.ways; for (var i in w) {
				var n=getName(_root.map.ways[i].attr,waynames);
                if (n) {
                    str += iText('inspector_way_name', i, n);
                } else {
                    str += iText('inspector_way', i);
                }
				var c=0;
				for (j=0; j<_root.map.ways[i].path.length; j++) {
					if (_root.map.ways[i].path[j]==this) { c++; }
				}
				if (c>1) { str += ' <b>'+iText('inspector_node_count', c)+'</b>'; }
				str += ", ";
			}
			str=str.substr(0,str.length-2);
		}

		return "<p>"+str+"</p>";
	};

	// ------------------------------------------------------------------------
	// Node->way mapping
	
	Node.prototype.addWay=function(id) { this.ways[id]=true; };
	Node.prototype.removeWay=function(id) { delete this.ways[id]; };
	Node.prototype.numberOfWays=function() { var z=this.ways; var c=0; for (var i in z) { c++; } return c; };
	Node.prototype.redrawWays=function() { var z=this.ways; for (var i in z) { _root.map.ways[i].redraw(); } };

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
