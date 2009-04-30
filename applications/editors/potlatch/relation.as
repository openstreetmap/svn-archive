
	// =====================================================================================
	// relations.as
	// Potlatch relation-handling code
	// =====================================================================================

	// ** highlight isn't yellow if it's part of a relation (should be)
	// ** default tags: type, name
	// ** verboseText isn't very good
	// ** some circumstances (while saving?) in which if you pan and a new version
	//    is loaded, they're added to the relation all over again

	// ** need to move relations hash out of way/nodes (so we can maintain it for unloaded ways)

	// =====================================================================================
	// Classes - OSMRelation

	function OSMRelation() {
		this.members = new Array();
		this.attr=new Array();
		this.isHighlighting = false;
		this.clean=true;					// altered since last upload?
		this.uploading=false;				// currently uploading?
		this.locked=false;					// locked against upload?
		this.version=0;
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
			_root.map.relations[w].attr=result[1];
			_root.map.relations[w].members=result[2];
			_root.map.relations[w].version=result[3];
			_root.map.relations[w].redraw();
			var mems=result[2];
			for (m in mems) {
				findLinkedHash(mems[m][0],mems[m][1])[w]=mems[m][2];
			}
		};
		remote_read.call('getrelation',responder,Math.floor(this._name));
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
				_root.map.relations[result[0]]._name=nw;				// rename relation object
				var mems=_root.map.relations[nw].members;				// make sure wayrels/noderels entries are up-to-date
				for (var i in mems) {									//  |
					var r=findLinkedHash(mems[i][0],mems[i][1]);		//  |
					r[nw]=mems[i][2];									//  |
					delete r[result[0]];								//  |
				}
			}
			_root.map.relations[nw].uploading=false;
			_root.map.relations[nw].version=result[2];
			_root.writesrequested--;
		};

		// ways/nodes for negative IDs should have been previously put
		// so the server should know about them
		if (!this.uploading && !this.locked && !_root.sandbox ) {
			this.uploading=true;
			_root.writesrequested++;
			remote_write.call('putrelation', putresponder,
				_root.usertoken, _root.changeset, this.version,
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
		var linewidth=Math.max(_root.linewidth*3,10);
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
			if ( ms[m][0] == 'Way' && _root.map.ways[ms[m][1]] ) {
				var way = _root.map.ways[ms[m][1]];
		
				this.line.moveTo(way.path[0].x,way.path[0].y);
				for (var i=1; i<way.path.length; i+=1) {
					this.line.lineTo(way.path[i].x,way.path[i].y);
				}
			} else if ( ms[m][0] == 'Node' && _root.map.pois[ms[m][1]] ) {
				var poi = _root.map.pois[ms[m][1]];
				this.drawPoint(poi._x, poi._y);
			} else if ( ms[m][0] == 'Node' ) {
				this.drawPoint(nodes[ms[m][1]].x,nodes[ms[m][1]].y);
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
		return this.getRole('Way', way_id);
	};

	OSMRelation.prototype.getRole=function(type, id) {
		return findLinkedHash(type,id)[this._name];
	};

	OSMRelation.prototype.renumberMember=function(type, id, new_id) {
		var mems = this.members;
		var set = false;
		var r;
		for ( var m = 0; m < mems.length && !set; m++ ) {
			if ( mems[m][0] == type && mems[m][1] == id ) {
				mems[m][1] = new_id;
				set = true;
				r=findLinkedHash(type,id); delete r[this._name];
				findLinkedHash(type,new_id)[this._name]=mems[m][2];
				this.clean = false;
			}
		}
	};

	OSMRelation.prototype.setRole=function(type, id, role) {
		var mems = this.members;
		var set = false;
		var diff = true;
		for ( var m = 0; m < mems.length && !set; m++ ) {
			if ( mems[m][0] == type && mems[m][1] == id ) {
				diff = (mems[m][2] != role);
				mems[m][2] = role;
				set = true;
			}
		}
		findLinkedHash(type,id)[this._name]=role;
		if ( !set )
			this.members.push([type, id, role]);
		if ( diff ) {
			this.clean = false;
			this.redraw();
		}
	};

	OSMRelation.prototype.setWayRole=function(way_id, role) {
		this.setRole('Way', way_id, role);
	};

	OSMRelation.prototype.hasWay=function(way_id) {
		var role = this.getWayRole(way_id);
		return role == undefined ? false : true;
	};

	OSMRelation.prototype.getNodeRole=function(node_id) {
		return this.getRole('Node', node_id);
	};

	OSMRelation.prototype.setNodeRole=function(node_id, role) {
		this.setRole('Node', node_id, role);
	};

	OSMRelation.prototype.hasNode=function(node_id) {
		var role = this.getNodeRole(node_id);
		return role == undefined ? false : true;
	};

	OSMRelation.prototype.removeMember=function(type, id) {
		this.removeMemberDirty(type, id, true);
	};

	OSMRelation.prototype.removeMemberDirty=function(type, id, markDirty) {
		var mems = this.members;
		for (var m in mems) {
			if ( mems[m][0] == type && mems[m][1] == id ) {
				mems.splice(m, 1);
				if ( markDirty )
					this.clean = false;
				this.redraw();
			}
		}
		var r=findLinkedHash(type,id);
		delete r[this._name];
	};

	OSMRelation.prototype.removeWay=function(way_id) {
		this.removeMember('Way', way_id);
	};

	OSMRelation.prototype.removeNode=function(node_id) {
		this.removeMember('Node', node_id);
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
		box.newattr.onRollOver=function() { setFloater(iText("Add a new tag",'tip_addtag')); };
		box.newattr.onRollOut =function() { clearFloater(); };
	};

	Object.registerClass("relation",OSMRelation);


	// ===============================
	// Support functions for relations

	function findLinkedHash(type,id) {
		// returns hash of relations in way/POI/node object
		var r;
		if (type=='Way') {
			if (!_root.wayrels[id]) { _root.wayrels[id]=new Object(); }
			r=_root.wayrels[id]; 
		} else {
			if (!_root.noderels[id]) { _root.noderels[id]=new Object(); }
			r=_root.noderels[id];
		}
		return r;
	}

	function getRelations(type, id) {
		// this is very expensive and shouldn't be called unless really necessary
		var rels = new Object();
		var z = _root.map.relations;
		for (var i in z) {
			var mems=z[i].members;
			for (var m in mems) {
				if (mems[m][0]==type && mems[m][1]==id) { rels[i]=mems[m][2]; }
			}
		}
		return rels;
	}

	function memberDeleted(type, id) {
		var rels = _root.map.relations;
		for ( r in rels )
			rels[r].removeMemberDirty(type, id, true);
	}

	function renumberMemberOfRelation(type, id, new_id) {
		var rels = _root.map.relations;
		for ( r in rels )
			rels[r].renumberMember(type, id, new_id);
	}

	function markWayRelationsDirty(way) {
		var z=_root.wayrels[way];
		for (var i in z) { _root.map.relations[i].clean=false; }

		var p=_root.map.ways[way].path;
		for (var i in p) { markNodeRelationsDirty(_root.map.ways[way].path[i].id); }
	}
	
	function markNodeRelationsDirty(node) {
		var z=_root.noderels[node];
		for (var i in z) { _root.map.relations[i].clean=false; }
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
			case 'way':		type='Way' ; id=wayselected; break;
			case 'point':	type='Node'; id=_root.ws.path[_root.pointselected].id; break;
			case 'POI':		type='Node'; id=poiselected; break;
		}
		if ( type == undefined || id == undefined ) return;

		var completeAdd = function(button) {

			if ( button != iText("Ok",'ok') ) return false;

			var box=_root.windows.relation.box;
			var radio=box.reloption.selected;
			var keepDialog = false;

			switch (radio) {
				case 1:	// Add to an existing relation
						var selected=box.addroute_menu.selected;
						var rs=_root.map.relations;
						var i=0;
						for (var r in rs) {
							if (selected==i) { 
							rs[r].setRole(type, id, ''); }
							i++;
						}
						break;
				case 2:	// Create a new relation
						var nid = newrelid--;
						_root.map.relations.attachMovie("relation",nid,++reldepth);
						_root.map.relations[nid].setRole(type, id, '');
						_root.map.relations[nid].attr['type'] = undefined;
						_root.windows.relation.remove(); keepDialog = true;
						_root.map.relations[nid].editRelation();
						break;
				case 3:	// Find a relation
						keepDialog=true;
						if (box.search.text!='') {
							findresponder=function() {};
							findresponder.onResult=function(rellist) {
								for (r in rellist) {
									var w=rellist[r][0];
									if (!_root.map.relations[w]) {
										_root.map.relations.attachMovie("relation",w,++reldepth);
										_root.map.relations[w].attr=rellist[r][1];
										_root.map.relations[w].members=rellist[r][2];
										_root.map.relations[w].version=rellist[r][3];
										_root.relcount++;
										var mems=rellist[r][2];
										for (m in mems) {
											findLinkedHash(mems[m][0],mems[m][1])[this._name]=mems[m][2];
										}
									}
								}
								createRelationMenu(_root.windows.relation.box,20);
								_root.windows.relation.box.search.text='';
							};
							remote_read.call('findrelations',findresponder,box.search.text);
						}
						break;
			}
			_root.panel.properties.reinit();
			if (keepDialog) { _root.panel.properties.enableTabs(false); }
			return keepDialog;
		};

		// Create dialogue

		_root.panel.properties.enableTabs(false);
		_root.windows.attachMovie("modal","relation",++windowdepth);
		_root.windows.relation.init(300, 150, [iText("Cancel",'cancel'), iText("Ok",'ok')], completeAdd);
		var z = 5;
		var box = _root.windows.relation.box;
		
		box.createTextField("title",z++,7,7,300-14,20);
		box.title.text = iText("Add $1 to a relation",'prompt_addtorelation',proptype);
		with (box.title) {
			wordWrap=true;
			setTextFormat(boldText);
			selectable=false; type='dynamic';
		}
		
		box.createTextField("instr",z++,7,30,300-14,40);

		// Create radio buttons and menu

		box.attachMovie("radio","reloption",z++);
		box.reloption.addButton(10,35,iText("Add to an existing relation",'existingrelation'));
		box.reloption.addButton(10,75,iText("Create a new relation",'createrelation'));
		box.reloption.addButton(10,95,iText("Find a relation containing",'findrelation'));

		createRelationMenu(box,20);

		var w=box.reloption[3].prompt._width+25;
		box.createTextField("search",z++,w,90,290-w,17);
		box.search.setNewTextFormat(plainSmall);
		box.search.type='input';
		box.search.backgroundColor=0xDDDDDD;
		box.search.background=true;
		box.search.border=true;
		box.search.borderColor=0xFFFFFF;
		box.search.onSetFocus=function() { this._parent.reloption.select(3); };
	}

	function createRelationMenu(box,z) {
		var relations=new Array();
		var rs = _root.map.relations;
		for ( var r in rs ) {
			relations.push(rs[r].verboseText());
		}

		if (relations.length==0) {
			relations.push(iText("No relations in current area",'norelations'));
			box.reloption.disable(1);
			box.reloption.select(2);
		} else {
			box.reloption.enable(1);
			box.reloption.select(1);
		}
		box.attachMovie("menu", "addroute_menu", z);
		box.addroute_menu.init(25, 50, 0, relations,
					iText('Add to the chosen route','tip_selectrelation'),null, null, 268);
	}
