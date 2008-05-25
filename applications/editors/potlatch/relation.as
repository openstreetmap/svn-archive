
	// =====================================================================================
	// relations.as
	// Potlatch relation-handling code
	// =====================================================================================

	// ** highlight isn't yellow if it's part of a relation (should be)
	// ** default tags: type, name
	// ** verboseText isn't very good

	// =====================================================================================
	// Classes - OSMRelation

	function OSMRelation() {
		this.members = new Array();
		this.attr=new Array();
		this.isHighlighting = false;
		this.clean=true;					// altered since last upload?
		this.uploading=false;				// currently uploading?
		this.locked=false;					// locked against upload?
	};
	OSMRelation.prototype=new MovieClip();


	// OSMRelation.verboseText - create long description of relation

	OSMRelation.prototype.verboseText = function() {
		var text = this._name+": ";
		var type = undefined;
		if ( this.attr['type'] ) {
			type = this.attr['type'];
			text += type + " ";
		}

		if ( type == 'route' ) {
			if ( this.attr['route'] )	text += this.attr['route'] + " ";
			if ( this.attr['network'] )	text += this.attr['network'] + " ";
			if ( this.attr['ref'] )		text += this.attr['ref']+" ";
			if ( this.attr['name'] )	text += this.attr['name']+" ";
			if ( this.attr['state'] )	text += "("+this.attr['state']+") ";
		} else if ( this.attr['name'] )	text += this.attr['name'];

		return text;
	};

	// OSMRelation.getType/getName - summary info used in property window

	OSMRelation.prototype.getType=function() {
		if (!this.attr['type']) { return "relation"; }
		if (this.attr['type']=='route') {
			if (this.attr['network']) { return this.attr['network']; }
			if (this.attr['route']) { return this.attr['route']; }
		}
		return this.attr['type'];
	};
	
	OSMRelation.prototype.getName=function() {
		if (this.attr['ref' ]) { return this.attr['ref' ]; }
		if (this.attr['name']) { return this.attr['name']; }
		return '';
	};

	// OSMRelation.load - load from remote server

	OSMRelation.prototype.load=function() {
		responder = function() { };
		responder.onResult = function(result) {
			_root.relsreceived+=1;
			var w=result[0];
			var i,id;
			_root.map.relations[w].clean=true;
			_root.map.relations[w].locked=false;
			_root.map.relations[w].oldversion=0;
			_root.map.relations[w].attr=result[1];
			_root.map.relations[w].members=result[2];
			_root.map.relations[w].redraw();
		};
		remote.call('getrelation',responder,Math.floor(this._name));
	};

	OSMRelation.prototype.reload=function() {
		if ( this._name < 0 )
			this.removeMovieClip();
		else {
			_root.relsrequested++;
			this.load();
		}
	};

	// OSMRelation.upload - save to remote server

	OSMRelation.prototype.upload=function() {
		putresponder=function() { };
		putresponder.onResult=function(result) {
			var code=result.shift(); if (code) { handleError(code,result); return; }
			var nw=result[1];	// new relation ID
			if (result[0]!=nw) {
				_root.map.relations[result[0]]._name=nw;
			}
			_root.map.relations[nw].uploading=false;
		};

		// ways/nodes for negative IDs should have been previously put
		// so the server should know about them
		if (!this.uploading && !this.locked && !_root.sandbox ) {
			this.attr['created_by']=_root.signature;
			this.uploading=true;
			remote.call('putrelation', putresponder, _root.usertoken,
				Math.floor(this._name),
				this.attr, this.members, 1);
			this.clean=true;
		}
	};


	// OSMRelation.redraw 		- show on map
	// OSMRelation.drawPoint
	// OSMRelation.setHighlight

	OSMRelation.prototype.redraw=function() {
		this.createEmptyMovieClip("line",1);					// clear line
		var linewidth=10;
		var linealpha= this.isHighlighting ? 75 : 50;
		var c = this.isHighlighting ? 0xff8800 : 0x8888ff;

		var type = this.getType();
		if ( !this.isHighlighting ) {
			if ( relcolours[type] != undefined ) {
				c = relcolours[type]; linewidth = relwidths[type]; linealpha = relalphas[type];
			}
		}
		this.line.lineStyle(linewidth,c,linealpha,false,"none");

		var ms = this.members;
		for ( var m = 0; m < ms.length; m++ ) {
			if ( ms[m][0] == 'way' && _root.map.ways[ms[m][1]] ) {
				var way = _root.map.ways[ms[m][1]];
		
				this.line.moveTo(way.path[0][0],way.path[0][1]);
				for (var i=1; i<way.path.length; i+=1) {
					this.line.lineTo(way.path[i][0],way.path[i][1]);
				}
			} else if ( ms[m][0] == 'node' && _root.map.pois[ms[m][1]] ) {
				var poi = _root.map.pois[ms[m][1]];
				this.drawPoint(poi._x, poi._y);
			} else if ( ms[m][0] == 'node' ) {
				var pid = ms[m][1];
				var done = false;
				var qway = wayselected;// {
					for (qs=0; qs<_root.map.ways[qway].path.length && !done; qs+=1) {
						var poi = _root.map.ways[qway].path[qs];
						if ( poi[2] == pid ) {
							this.drawPoint(poi[0], poi[1]);
							done = true;
						}
					}
				//}
			}
		}
	};

	OSMRelation.prototype.drawPoint = function(x, y) {
		var z = Math.max(100/Math.pow(2,_root.scale-13),6.25)/30;
		this.line.moveTo(x-z,y-z);
		this.line.lineTo(x-z,y+z);
		this.line.lineTo(x+z,y+z);
		this.line.lineTo(x+z,y-z);
		this.line.lineTo(x-z,y-z);
	};

	OSMRelation.prototype.setHighlight = function(highlight) {
		if ( this.isHighlighting == highlight )
			return;

		this.isHighlighting = highlight;
		this.redraw();
	};

	// ---- Editing and information functions

	OSMRelation.prototype.getWayRole=function(way_id) {
		return this.getRole('way', way_id);
	};

	OSMRelation.prototype.getRole=function(type, id) {
		var ws = this.members;
		var role;
		for ( var m = 0; m < ws.length && role == undefined; m++ ) {
			if ( ws[m][0] == type && ws[m][1] == id ) {
				role = ws[m][2];
			}
		}
		return role;
	};

	OSMRelation.prototype.renumberMember=function(type, id, new_id) {
		var ws = this.members;
		var set = false;
		for ( var m = 0; m < ws.length && !set; m++ ) {
			if ( ws[m][0] == type && ws[m][1] == id ) {
				ws[m][1] = new_id;
				set = true;
			}
		}
	};

	OSMRelation.prototype.setRole=function(type, id, role) {
		var ws = this.members;
		var set = false;
		var diff = true;
		for ( var m = 0; m < ws.length && !set; m++ ) {
			if ( ws[m][0] == type && ws[m][1] == id ) {
				diff = (ws[m][2] != role);
				ws[m][2] = role;
				set = true;
			}
		}
		if ( !set )
			this.members.push([type, id, role]);
		if ( diff ) {
			this.clean = false;
			this.redraw();
		}
	};

	OSMRelation.prototype.setWayRole=function(way_id, role) {
		this.setRole('way', way_id, role);
	};

	OSMRelation.prototype.hasWay=function(way_id) {
		var role = this.getWayRole(way_id);
		return role == undefined ? false : true;
	};

	OSMRelation.prototype.getNodeRole=function(node_id) {
		return this.getRole('node', node_id);
	};

	OSMRelation.prototype.setNodeRole=function(node_id, role) {
		this.setRole('node', node_id, role);
	};

	OSMRelation.prototype.hasNode=function(node_id) {
		var role = this.getNodeRole(node_id);
		return role == undefined ? false : true;
	};

	OSMRelation.prototype.removeMember=function(type, id) {
		this.removeMemberDirty(type, id, true);
	};

	OSMRelation.prototype.removeMemberDirty=function(type, id, markDirty) {
		var ws = this.members;
		for (var m in ws) {
			if ( ws[m][0] == type && ws[m][1] == id ) {
				ws.splice(m, 1);
				if ( markDirty )
					this.clean = false;
				this.redraw();
			}
		}
	};

	OSMRelation.prototype.removeWay=function(way_id) {
		this.removeMember('way', way_id);
	};

	OSMRelation.prototype.removeNode=function(node_id) {
		this.removeMember('node', node_id);
	};

	// ----- UI

	OSMRelation.prototype.editRelation = function() {
		var rel = this;
		var completeEdit = function() {
			rel.setHighlight(false);
			_root.panel.properties.reinit();
		};

		rel.setHighlight(true);
		_root.panel.properties.enableTabs(false);
		
		_root.windows.attachMovie("modal","relation",++windowdepth);
		_root.windows.relation.init(402, 255, ["OK"], completeEdit);
		var z=5;
		var box=_root.windows.relation.box;
		
		box.createTextField("title",z++,7,7,400-14,20);
		with (box.title) {
			type='dynamic';
			text=this._name; setTextFormat(plainText);
			setNewTextFormat(boldText); replaceSel("Relation ");
		}

		// Light grey background
		box.createEmptyMovieClip('lightgrey',z++);
		with (box.lightgrey) {
			beginFill(0xF3F3F3,100);
			moveTo(10,30); lineTo(392,30);
			lineTo(392,213); lineTo(10,213);
			lineTo(10,30); endFill();
		};

		box.attachMovie("propwindow","properties",z++);
		with (box.properties) { _x=14; _y=34; };
		_root.editingrelation = this;
		box.properties.ynumber = 9;
		box.properties.init("relation",2,9);

		box.attachMovie("newattr", "newattr", z++);
		with ( box.newattr ) {
			_x = 400-16; _y = 18;
		}
		box.newattr.onRelease =function() { box.properties.enterNewAttribute(); };
		box.newattr.onRollOver=function() { setFloater("Add a new tag"); };
		box.newattr.onRollOut =function() { clearFloater(); };
	};

	Object.registerClass("relation",OSMRelation);


	// ===============================
	// Support functions for relations

	function redrawRelationsForMember(type, id) {
		var rels = type == 'way' ? getRelationsForWay(id) : getRelationsForNode(id);
		for ( r in rels )
			_root.map.relations[rels[r]].redraw();
	}

	// the server-side handles removing a deleted object from a relation
	// but we need to keep the client side in sync
	function memberDeleted(type, id) {
		var rels = _root.map.relations;
		for ( r in rels )
			rels[r].removeMemberDirty(type, id, false);
	}

	function renumberMemberOfRelation(type, id, new_id) {
		var rels = _root.map.relations;
		for ( r in rels )
			rels[r].renumberMember(type, id, new_id);
	}

	function getRelationsForWay(way) {
		var rels = [];
		var z = _root.map.relations;
		for ( var i in z ) {
			if ( z[i].hasWay(way) )
				rels.push(i);
		}
		return rels;
	}

	function getRelationsForNode(node) {
		var rels = [];
		var z = _root.map.relations;
		for ( var i in z ) {
			if ( z[i].hasNode(node) )
				rels.push(i);
		}
		return rels;
	}

	function uploadDirtyRelations() {
		var rs = _root.map.relations;
		for ( var i in rs ) {
			if ( !rs[i].clean ) {
				rs[i].upload();
			}
		}
	}

	function revertDirtyRelations() {
		var rs = _root.map.relations;
		for ( var i in rs ) {
			if ( !rs[i].clean ) {
				rs[i].reload();
			}
		}
	}

	// addToRelation - add a way/point to a relation
	//				   called when user clicks '+' relation button

	function addToRelation() {
		var proptype = _root.panel.properties.proptype;
		var type, id;
		switch (proptype) {
			case 'way':		type='way' ; id=wayselected; break;
			case 'point':	type='node'; id=_root.pointselected; break;
			case 'POI':		type='node'; id=poiselected; break;
		}
		if ( type == undefined || id == undefined ) return;

		var completeAdd = function(button) {
			if ( button != 'Add' ) return false;

			var box = _root.windows.relation.box;

			var selected = box.addroute_menu.selected;
			var keepDialog = false;
			if ( selected > 0 ) {
				var rs = _root.map.relations;
				for ( var r in rs ) {
					selected -= 1;
					if ( selected == 0 )
						rs[r].setRole(type, id, '');
				}
			} else if ( selected == 0 ) {
				var nid = newrelid--;
				_root.map.relations.attachMovie("relation",nid,++reldepth);
				_root.map.relations[nid].setRole(type, id, '');
				_root.map.relations[nid].attr['type'] = undefined;
				_root.windows.relation.remove(); keepDialog = true;
				_root.map.relations[nid].editRelation();
			}
			_root.panel.properties.reinit();
			if (keepDialog) { _root.panel.properties.enableTabs(false); }
			return keepDialog;
		};

		_root.windows.attachMovie("modal","relation",++windowdepth);
		_root.windows.relation.init(300, 140, ["Cancel", "Add"], completeAdd);
		var z = 5;
		var box = _root.windows.relation.box;
		
		box.createTextField("title",z++,7,7,300-14,20);
		box.title.text = "Add "+proptype+" to a relation";
		with (box.title) {
			wordWrap=true;
			setTextFormat(boldText);
			selectable=false; type='dynamic';
		}
		
		box.createTextField("instr",z++,7,30,300-14,40);
		writeText(box.instr, "Select an existing relation to add to, or create a new relation.");

		var relations = new Array("Create a new relation");
		var rs = _root.map.relations;
		for ( var r in rs ) {
			relations.push(rs[r].verboseText());
		}
		// a normal, scrollable list may be better than a menu
		box.attachMovie("menu", "addroute_menu", z++);
		box.addroute_menu.init(7, 75, 0, relations,
					'Add to the chosen route', null, null, 300-14);
	}
