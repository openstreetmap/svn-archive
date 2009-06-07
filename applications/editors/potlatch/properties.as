 
	// =====================================================================================
	// properties.as
	// Potlatch property window functions
	// =====================================================================================

	// to do later:
	// -- relations:
	//		sort colouring/highlighting
	//		better presentation for drop-down "choose a relation" menu
	//		search for relations
	//		link icon?
	//		repeat relations
	//		preset menu
	//		autocomplete
	//			will need to be a higher stacking level than modal
	// -- scrollbar background/clicking?

	// properties is cleared/inited on:
	// - anchorpoint.select
	// - POI.reload and POI.select
	// - potlatch.as: mapClickEnd, deselectAll
	// - (relations, not a problem as yet)
	// - Way.select
	
	// =====================================================================================
	// Preset menu
	
	PresetMenu=function() {};
	PresetMenu.prototype=new MovieClip();

	PresetMenu.prototype.init=function(pw) {
		if (pw) {
			this.pw=pw;			// reference to property window
			pw.presetmenu=this;	// and back again!
			this.group=_root.lastgroup;	// what group of tags? (e.g. 'road')
			this.setIcon();
			this.reflect();
		} else {
			removeMovieClip(this.dropdown);
			removeMovieClip(this.icon);
		}
	};
	PresetMenu.prototype.initMenu=function(value) {
		this.attachMovie("menu","dropdown",1);
		this.dropdown.init(30,5,value,
						   presetnames[this.pw.proptype][this.group],
						   'Choose from a menu of preset tags describing the '+pw.proptype,
						   this.setAttributesFromPreset,this,151);
	};
	PresetMenu.prototype.reflect=function() {
		var i,t;
		var pt=this.pw.proptype;
		var found=this.pw.findInPresetMenu(this.group);
		if (found) { this.initMenu(found); return; }
		for (i=0; i<presetmenus[pt].length; i+=1) {
			t=this.pw.findInPresetMenu(presetmenus[pt][i]); if (t) { found=t; this.group=presetmenus[pt][i]; }
		}
		if (found) { this.initMenu(found);
					 this.setIcon(); }
			  else { this.initMenu(0); }
	};
	PresetMenu.prototype.setIcon=function() {
		_root.lastgroup=this.group;
		this.attachMovie("preset_"+this.group,"icon",2);
		with (this.icon) { _x=10; _y=15; }
		this.icon.onPress   =function() { this._parent.cycleIcon(); };
		this.icon.onRollOver=function() { setFloater(iText("Choose what type of presets are offered in the menu.",'tip_presettype')); };
		this.icon.onRollOut =function() { clearFloater(); };
	};
	PresetMenu.prototype.cycleIcon=function() {
		var j=0;
		var pt=this.pw.proptype;
		for (var i=0; i<presetmenus[pt].length; i+=1) {
			if (presetmenus[pt][i]==this.group) { j=i+1; }
		}
		this.group=presetmenus[pt][j%i];
		this.setIcon();
		this.initMenu(this.pw.findInPresetMenu(this.group));
	};
	PresetMenu.prototype.setAttributesFromPreset=function(pre) {
		var pname=presetnames[this.pw.proptype][this.group][pre];
		this.pw.setAttributes(presets[pname]);
	};

	Object.registerClass("presetmenu",PresetMenu);


	// =====================================================================================
	// Autocomplete

	// Still to do
	// - some way of stopping keys being added twice with same name
	//   (probably rename to highway_2?)
	// - wipe preset menu when POI reverted
	// - some way of invoking full autocomplete menu when value deleted
	//   (probably using up cursor/down cursor)

	AutoMenu=function() {};
	AutoMenu.prototype=new MovieClip();
	AutoMenu.prototype.redraw=function(tf) {

		this.tf=tf;							// textfield instance
		this.pw=tf._parent._parent._parent;	// PropertyWindow instance
		this.field=tf._name;				// keyname|value
		this.key=tf._parent.keyname.text;
		var curval=tf.text;

		if (this.field=='value' && curval=='' &&
		   (_root.lastkeypressed==8 || _root.lastkeypressed==46)) {
			// if user has just deleted value, don't show autocomplete!
			this.remove(); return;
		}

		// Find possible values for autocomplete
		if (curval=='key') { curval=''; }					// full menu on 'key'
		var possible=new Array();
		if (this.field=='keyname') {
			z=_root.autotags[this.pw.proptype];				// key
			for (i in z) {
				if (!this.pw.proparr[i]) {					// don't add if there's already a key of that name
					if (i.slice(0,curval.length)==curval) { possible.push(i); }
				} 
			}
			possible.sort();
		} else {
			z=_root.autotags[this.pw.proptype][this.key];	// value
			for (i in z) {
				if (z[i].slice(0,curval.length)==curval) { possible.push(z[i]); }
			}
		}

		// Draw autocomplete window
		var p=getGlobalCoord(tf); var wx=p[0]; var wy=p[1];
		this.autonumkeys=Math.min(possible.length,Math.floor(wy/16));
		if (this.autonumkeys==0) { this.remove(); return; }
		if (this.autonumkeys==1 && possible[0]==curval) { this.remove(); return; }

		this.createEmptyMovieClip("autolist",1);
		
		with (this.autolist) {
			_x=wx; _y=wy-16*this.autonumkeys-3;
			clear();
			beginFill(0x0000FF,100);
			moveTo(0,0); lineTo(100,0);
			lineTo(100,16*this.autonumkeys+3);
			lineTo(0,16*this.autonumkeys+3); lineTo(0,0);
			endFill();
		};

		this.useHandCursor=true;
		this.onPress=function() {
			this.select(Math.floor(this.autolist._ymouse/16));
			this.paste();
			var f=this.field;
			var k=this.key;
			if (f=='keyname') {
				renameKey(this.tf);
				_root.reinstatefocus=this.tf._parent.value;
			} else {
				this.remove();
				Selection.setFocus(null);
				_root.keytarget='';
			}
			if (this.pw.proptype!='POI') { _root.ws.redraw(); }
		};

		this.createEmptyMovieClip("triangle",2);
		with (this.triangle) {
			clear();
			beginFill(0x0000FF,100);
			moveTo(5,0);  lineTo(15,0);
			lineTo(10,5); lineTo(5,0);
			endFill();
			_x=wx; _y=wy;
		};
		
		for (i=0; i<this.autonumkeys; i++) {
			this.autolist.createTextField("o"+i,i,0,i*16,100,20);
			with (this.autolist["o"+i]) {
				text=possible[i]; setTextFormat(boldWhite); type='dynamic'; 
				background=true; backgroundColor=0x0000FF; selectable=false;
			};
		}
		this.select(0);
	};
	AutoMenu.prototype.remove=function() {
		removeMovieClip(this);
	};

	// AutoMenu.select		- set and highlight autocomplete value

	AutoMenu.prototype.select=function(n) {
		this.autolist["o"+this.selected].backgroundColor=0x0000FF;
		this.autolist["o"+this.selected].setTextFormat(auto_off);
		this.selected=n;
		this.autolist["o"+n].backgroundColor=0x00FFFF;
		this.autolist["o"+n].setTextFormat(auto_on);
	};
	
	// AutoMenu.paste		- write currently selected value into textfield

	AutoMenu.prototype.paste=function() {
		var newval=this.autolist["o"+this.selected].text;
		var tf=this.tf;
		tf.text=newval;
		if (this.field=='keyname') { renameKey(tf); }
							  else { setValueFromTextfield(tf); }
		Selection.setSelection(newval.length,newval.length);
	};
	

	// keyRespond			- respond to up/down/enter keypresses
	
	AutoMenu.prototype.keyRespond=function(k) {
		switch (k) {
			case Key.UP:	if (this.selected>0  ) { this.select(this.selected-1); }
							this.paste(); break;
			case Key.DOWN:	if (this.selected+1<this.autonumkeys) { this.select(this.selected+1); }
							this.paste(); break;
			case 13:		this.paste();
							this.remove();
							autoEnter(); break;
			case 27:		this.remove(); break;
		};
	};
	
	// autoEnter			- Enter pressed, move to next field

	function autoEnter() {
		if (Selection.getFocus().split('.').pop()=='keyname') {
			Selection.setFocus(eval(Selection.getFocus())._parent.value);
		} else {
			Selection.setFocus(null);
		}
	}

	Object.registerClass("auto",AutoMenu);


	// =====================================================================================
	// PropertyWindow object

	PropertyWindow=function() {
		this.proptype='';
	};
	PropertyWindow.prototype=new MovieClip();
	
	PropertyWindow.prototype.reinit=function() {
		var n=this.savedundo;
		this.init(this.proptype,this.xnumber,this.ynumber);
		this.savedundo=n;
	};

	PropertyWindow.prototype.init=function(proptype,w,h) {
		this.createEmptyMovieClip("attributes",1);
		this.createEmptyMovieClip("attrmask",2);
		this.createEmptyMovieClip("scrollbar",3);
//		if (proptype=='') { this.saveAttributes(); }
		this.proptype=proptype;
		this.tab=0;
		this.xnumber=w;
		this.ynumber=h;
		this.tagcount=0;
		this.savedundo=false;
		if (proptype=='') { return; }

		this.relarr = new Object();
		switch (proptype) {
			case 'point':
				this.proparr=_root.ws.path[_root.pointselected].attr;
				this.relarr=_root.noderels[_root.ws.path[pointselected].id];
				break;
			case 'POI':
				this.proparr=_root.map.pois[poiselected].attr;
				this.relarr=_root.noderels[poiselected];
				break;
			case 'way':
				this.proparr=_root.ws.attr;
				this.relarr=_root.wayrels[wayselected];
				break;
			case 'relation':
				this.proparr=_root.editingrelation.attr;
				break;
		}

		this.xpos=0; this.ypos=0;

		// Attach relations
		relarr=this.relarr;
		for (var rel in relarr) {
			if (_root.map.relations[rel]) {
				this.attributes.attachMovie("relmember",this.tagcount, this.tagcount);
				var pos = this.getXY(this.tagcount);
				this.attributes[this.tagcount]._x=pos[0];
				this.attributes[this.tagcount]._y=pos[1];
				this.attributes[this.tagcount].init(rel);
				this.attributes[this.tagcount].value.tabIndex=++this.tab;
				this.attributes[this.tagcount].value.tabEnabled=true;
				this.tagcount+=1;
			}
		}

		// Attach keys/values
		// sorted alphabetically, but with namespaced tags at the end

		proparr=this.proparr;	// annoying scope issues
		proplist=new Array();
		for (el in proparr) { proplist.push(el); }
		proplist.sort(function(a,b) {
			if      (a.indexOf(':')<b.indexOf(':')) { return -1; }
			else if (a.indexOf(':')>b.indexOf(':')) { return  1; }
			else if (a<b) { return -1; }
			else if (a>b) { return  1; }
			else		  { return  0; }
		});
		for (i=0; i<proplist.length; i++) {
			el=proplist[i];
			if (proparr[el]!='' && el!='created_by' && el!='edited_by') {
				this.attributes.attachMovie("keyvalue",this.tagcount,this.tagcount);
				var pos = this.getXY(this.tagcount);
				this.attributes[this.tagcount]._x=pos[0];
				this.attributes[this.tagcount]._y=pos[1];
				this.attributes[this.tagcount].init(el);
				this.attributes[this.tagcount].keyname.tabIndex=++this.tab;
				this.attributes[this.tagcount].value.tabIndex=++this.tab;
				this.tagcount+=1;
			}
		}


		this.scrollbar._x=0; this.scrollbar._y=this.ynumber*19;
		this.updateMask();					// Draw scrollbar
	};

	PropertyWindow.prototype.saveUndo=function() {
		// don't have more than two consecutive undos for the same way
		if (this.savedundo) { return; }
		this.savedundo=true;
		var task  =_root.undo[_root.undo.length-1][0];
		var params=_root.undo[_root.undo.length-1][1];

		switch (this.proptype) {
			case 'way':		_root.undo.append(UndoStack.prototype.undo_waytags,
											  new Array(_root.ws,deepCopy(this.proparr)),
											  iText("setting tags on a way",'action_waytags')); break;
			case 'point':	_root.undo.append(UndoStack.prototype.undo_pointtags,
											  new Array(_root.ws,_root.pointselected,deepCopy(this.proparr)),
											  iText("setting tags on a point",'action_pointtags')); break;
			case 'POI':		_root.undo.append(UndoStack.prototype.undo_poitags,
											  new Array(_root.map.pois[poiselected],deepCopy(this.proparr)),
											  iText("setting tags on a POI",'action_poitags')); break;
		};
	};

	PropertyWindow.prototype.getXY=function(i) {
		var x = Math.floor(i / this.ynumber);
		var y = i % this.ynumber;
		return [x*190, y*19];
	};

	PropertyWindow.prototype.enableTabs=function(a) {
		for (var i in this.attributes) {
			this.attributes[i].keyname.tabEnabled=a;
			this.attributes[i].value.tabEnabled  =a;
		}
	};

	// PropertyWindow.enterNewAttribute

	PropertyWindow.prototype.enterNewAttribute=function() {
		// ** check nothing already exists called "key"
		this.saveUndo();
		this.attributes.attachMovie("keyvalue",this.tagcount,this.tagcount);
		var pos = this.getXY(this.tagcount);
		this.attributes[this.tagcount]._x=pos[0];
		this.attributes[this.tagcount]._y=pos[1];
		this.attributes[this.tagcount].init('key');
		this.attributes[this.tagcount].keyname.tabIndex=++this.tab;
		this.attributes[this.tagcount].value.tabIndex=++this.tab;
		Selection.setFocus(this.attributes[this.tagcount].keyname);
		Selection.setSelection(0,3);
		this.tagcount+=1;
		this.updateMask();
		this.attributes[this.tagcount-1].scrollToField();
	};

	// PropertyWindow.updateMask
	// create mask given size of window, create scrollbar

	PropertyWindow.prototype.updateMask=function() {
		this.cols=Math.floor((this.tagcount+(this.ynumber-1))/this.ynumber);// number of columns used
		var pxwidth=this.xnumber*190-5;

		// Create and apply mask
		with (this.attrmask) {
			clear();
			beginFill(0,100);
			moveTo(0,0); lineTo(pxwidth,0);
			lineTo(pxwidth,this.ynumber*19); lineTo(0,this.ynumber*19);
			lineTo(0,0); endFill();
		}
		this.attributes.setMask(this.attrmask);

		// Draw scrollbar
		var percent=(Math.min(this.xnumber/this.cols,1));	// what percentage of scrollbar to show?
		var swidth=pxwidth*percent;							// how wide is the scrollbar?
		this.mwidth=pxwidth-swidth;							// furthest right scrollbar can go
		this.sscope=(this.cols-this.xnumber)*190;			// how many unshown pixels in attributes
		if (percent==1) { var c=0xE0E0E0; }					// colour to show scrollbar
				   else { var c=0xCCCCCC; }					//  |
		with (this.scrollbar) {
			clear();
			beginFill(c,100);
			moveTo(0,0); lineTo(swidth,0);
			lineTo(swidth,4); lineTo(0,4);
			lineTo(0,0); endFill();
		};

		// Scrollbar move events
		if (percent<1) {
			this.scrollbar.onPress=function() {
				this.onMouseMove=function() {
					this._parent.attributes._x=-(this._x/this._parent.mwidth)*this._parent.sscope;
				};
				this.startDrag(false,0,this._y,this._parent.mwidth,this._y);
			};
			this.scrollbar.onRelease=function() {
				delete this.onMouseMove;
				this.stopDrag();
			};
		}
	};
	
	PropertyWindow.prototype.repeatAttributes=function(dotags) {
		var i,proparr,relarr;
		this.saveUndo();
		switch (this.proptype) {
			case 'point':	proparr=_root.savedpointway.path[_root.saved['point']].attr; 
							relarr=_root.noderels[_root.savedpointway.path[_root.saved['point']].id];
							break;
			case 'POI':		proparr=_root.saved['POI'].attr;
							relarr=_root.noderels[_root.saved['POI']._name];
							break; // ** formerly had _root.map.pois[poiselected].attr=new Array(); in here, no obvious reason why
			case 'way':		proparr=_root.saved['way'].attr;
							relarr=_root.wayrels[_root.saved['way']._name];
							break;
		}

		// repeat tags
		if (dotags) {
			for (i in proparr) {
				if (Key.isDown(Key.SHIFT) && (i=='name' || i=='ref') || i=='created_by') {
					// ignore name and ref if SHIFT pressed
				} else {
					switch (this.proptype) {
						case 'point':	j=_root.savedpointway.path[_root.saved['point']].attr[i]; break;
						case 'POI':		j=_root.saved['POI'].attr[i]; break;
						case 'way':		j=_root.saved['way'].attr[i]; break;
					}
					setValueInObject(this.proptype,i,j);
				}
			}
		}

		// repeat relations
		for (i in relarr) {
			var r=_root.map.relations[i];	// reference to this relation
			switch (this.proptype) {
				case 'point':	r.setNodeRole(_root.ws.path[_root.pointselected].id,relarr[i]); break;
				case 'POI':		r.setNodeRole(poiselected,relarr[i]); break;
				case 'way':		r.setWayRole (wayselected,relarr[i]); break;
			}
		}
		if (this.proptype!='POI') { _root.ws.redraw(); }
		this.reflect();
		this.reinit();
	};

	PropertyWindow.prototype.setAttributes=function(pkeys) {
		this.saveUndo();
		for (var pkey in pkeys) {
			if (this.proparr[pkey].length>0 && pkeys[pkey].substr(0,6)=='(type ') {}
			else { setValueInObject(this.proptype, pkey, pkeys[pkey]); }
		}
		this.reinit();
		this.saveAttributes();
		if (this.proptype!='POI') { _root.ws.redraw(); }
	};

	PropertyWindow.prototype.nukeAttributes=function() {
		var proparr=this.proparr;
		this.saveUndo();
		for (var el in proparr) { delete this.proparr[el]; setValueInObject(this.proptype,el,''); }
		if (this.proptype!='POI') { _root.ws.redraw(); }
		this.reflect();
		this.reinit();
	};

	PropertyWindow.prototype.saveAttributes=function() {
		if (this.tagcount==0) { return; }
		switch (this.proptype) {
			case 'point':	_root.saved[this.proptype]=_root.pointselected; _root.savedpointway=_root.ws; break;
			case 'POI':		_root.saved[this.proptype]=_root.map.pois[poiselected]; break;
			case 'way':		_root.saved[this.proptype]=_root.ws; break;
		};
	};

	PropertyWindow.prototype.findInPresetMenu=function(group) {
		if (group=='address') { return 0; }	// shouldn't match addresses as they're "additional" tags
		var pname,pkeys,pre,ok,cvalue;
		var f=0;
		for (pre=presetnames[this.proptype][group].length-1; pre>-1; pre-=1) {
			pname=presetnames[this.proptype][group][pre];
			pkeys=_root.presets[pname];
			if (pkeys) {
				ok=1;
				for (pkey in pkeys) {
					cvalue=this.proparr[pkey];
					if (cvalue==null) { cvalue=''; }
					if (cvalue!=presets[pname][pkey] && presets[pname][pkey].substr(0,6)!='(type ') { ok=0; }
				}
				if (ok==1) { f=pre; }
			}
		}
		return f;
	};

	PropertyWindow.prototype.reflect=function() {
		if (this.presetmenu) { this.presetmenu.reflect(); }
	};

	PropertyWindow.prototype.setTag=function(k,v) {
		this.saveUndo();
		setValueInObject(this.proptype,k,v);
		this.reinit();
		this.saveAttributes();
	};
	

	
	// Remove any '(type...' or blank keys before uploading

	PropertyWindow.prototype.tidy=function() {
		var proparr=this.proparr;
		for (var el in proparr) {
			if (proparr[el]=='' || proparr[el].substr(0,6)=='(type ' || !proparr[el]) { delete this.proparr[el]; }
		}
	};

	Object.registerClass("propwindow",PropertyWindow);





	// =====================================================================================
	// KeyValue object

	function KeyValue() {};
	KeyValue.prototype=new MovieClip();

	KeyValue.prototype.init=function(key,value) {

		// Initialise key

		this.createTextField('keyname',1,0,-1,70,18);
		with (this.keyname) {
			backgroundColor=0xBBBBBB;
			background=true;
			text=key;
			type='input';
			setTextFormat(boldSmall);
			setNewTextFormat(boldSmall);
			restrict="^"+chr(0)+"-"+chr(31);
			maxChars=255;
		};
		this.keyname.onSetFocus =function() {
			this._parent.scrollToField();
			_root.keytarget='keyname';
		};
		this.keyname.onKillFocus=function() {
			renameKey(this);
			if (_root.lastkeypressed==-1 && _root.auto.autolist.hitTest(_root._xmouse,_root._ymouse)) { return; }
			if (this.text=='') { _root.redopropertywindow=this._parent._parent._parent; }
			_root.keytarget='';
			_root.auto.remove();
			this._parent._parent._parent.reflect();
		};
		this.keyname.onChanged=function(tf) {
			if (tf.text=='+' || tf.text=='=') {
				// if FP has picked up the "+" keypress, ignore it and set back to 'key'
				tf.text='key';
				tf.setTextFormat(boldSmall);
				tf.setNewTextFormat(boldSmall);
				Selection.setFocus(tf); Selection.setSelection(0,3);
			}
			if (!_root.auto) { _root.attachMovie("auto","auto",75); }
			_root.auto.redraw(tf);
			this._parent._parent._parent.saveAttributes();
		};

		// Initialise value

		this.createTextField('value',2,72,-1,100,18);
		this.value.onSetFocus =function() {
			this._parent.scrollToField();
			if (this.textColor==0x888888) { this.text=''; this.textColor=0; }
			if (!_root.auto) { _root.attachMovie("auto","auto",75); }
			_root.auto.redraw(this);
			_root.keytarget='value'; 
		};
		this.value.onKillFocus=function() {
			if (_root.reinstatefocus) { return; }
			if (_root.lastkeypressed==-1 && _root.auto.autolist.hitTest(_root._xmouse,_root._ymouse)) { return; }
			_root.keytarget='';
			if (this.text=='') {
				if (this._parent.lastvalue.substr(0,6)=='(type ') {
					this.text=this._parent.lastvalue; this.textColor=0x888888;
				} else {
					_root.redopropertywindow=this._parent._parent._parent; 
				}
			}
			if (this._parent._parent._parent.proptype!='POI') { _root.ws.redraw(); }
			_root.auto.remove();
			this._parent._parent._parent.reflect();
			this._parent.lastvalue=this.text;
		};
		this.value.onChanged=function(tf) {
			setValueFromTextfield(tf);
			if (!_root.auto) { _root.attachMovie("auto","auto",75); }
			_root.auto.redraw(tf);
			this._parent._parent._parent.saveAttributes();
		};
		with (this.value) {
			backgroundColor=0xDDDDDD;
			background=true;
			type='input';
			setTextFormat(plainSmall);
			setNewTextFormat(plainSmall);
			restrict="^"+chr(0)+"-"+chr(31);
			maxChars=255;
		};
		this.value.text=this.getValueFromObject(key);
		if (this.value.text.substr(0,6)=='(type ') { this.value.textColor=0x888888; }
		this.lastkey=key;
		this.lastvalue=this.value.text;
		
		// Initialise close box
		
		this.createEmptyMovieClip('grey',3);
		with (this.grey) {
			beginFill(0xDDDDDD,100);
			moveTo(172,-1); lineTo(182,-1);
			lineTo(182,17); lineTo(172,17);
			endFill();
		};

		this.attachMovie("closecross", "i_remove", 4);
		with (this.i_remove) { _x=174; _y=8; };
		this.i_remove.onPress=function() {
			this._parent.value.text='';
			setValueFromTextfield(this._parent.value);
			_root.auto.remove();
			this._parent._parent._parent.reflect();
			if (this._parent._parent._parent.proptype!='POI') { _root.ws.redraw(); }
			_root.redopropertywindow=this._parent._parent._parent;
		};
	};

	// KeyValue.getValueFromObject(key)
	// for a given key, returns the value from the way, point or POI

	KeyValue.prototype.getValueFromObject=function(k) {
		var v;
		switch (this._parent._parent.proptype) {
			case 'point':	v=_root.ws.path[_root.pointselected].attr[k]; break;
			case 'POI':		v=_root.map.pois[poiselected].attr[k]; break;
			case 'way':		v=_root.ws.attr[k]; break;
			case 'relation':v=_root.editingrelation.attr[k]; break;
		}
		if (v==undefined) { v='(type value here)'; }
		return v;
	};

	// KeyValue.scrollToField()
	// make sure this field is visible
	
	KeyValue.prototype.scrollToField=function() {
		var pw=this._parent._parent;
		if (this._x>=-(pw.attributes._x) &&
			this._x< -(pw.attributes._x)+190*(pw.xnumber-1)+5) {
			return;			// In view
							// -(pw.attributes._x) is left x of the panel
		}

		// To get in view, we need our column on the right of the panel
		var newcol=Math.floor(this._x/190);		// column we're in
		newcol=Math.min(newcol,pw.cols-pw.xnumber);
		newcol=Math.max(newcol,0);
		pw.attributes._x=-newcol*190;
		pw.scrollbar._x=newcol*190/pw.sscope*pw.mwidth;
		// (inverse of calculation in pw.scrollbar.onPress)
	};

	Object.registerClass("keyvalue",KeyValue);

	// =====================================================================================
	// KeyValue support functions

	// setValueFromTextfield(value textfield)
	// setValueInObject(property type,key,value)
	// - update the way, point or POI with the new value
	//   (opposite of getValueFromObject)
	
	function setValueFromTextfield(tf) {
		tf._parent._parent._parent.saveUndo();
		setValueInObject(tf._parent._parent._parent.proptype,
						 tf._parent.keyname.text,
						 tf.text);
	};
	
	function setValueInObject(proptype,k,v) {
		switch (proptype) {
			case 'point':	var id=_root.ws.path[_root.pointselected].id;
							nodes[id].attr[k]=v; 
							nodes[id].tagged=hasTags(nodes[id].attr);
							nodes[id].markDirty();
							_root.ws.clean=false; break;
			case 'POI':		_root.map.pois[poiselected].attr[k]=v;
							_root.map.pois[poiselected].clean=false; break;
			case 'way':		_root.ws.attr[k]=v; 
							_root.ws.clean=false; break;
			case 'relation':_root.editingrelation.attr[k]=v;
							_root.editingrelation.clean=false; break;
		}
	};

	// renameKey(key textfield)

	function renameKey(tf) {
		var k=tf.text;
		tf._parent._parent._parent.saveUndo();
		if (k!=tf._parent.lastkey) {
			// field has been renamed, so delete old one and set new one
			// (temporary references used to get around Ming delete bug)
			switch (tf._parent._parent._parent.proptype) {
				case 'point':	var noderef=_root.ws.path[_root.pointselected];
								delete noderef.attr[tf._parent.lastkey];
								_root.ws.clean=false; break;
				case 'POI':		var poiref=_root.map.pois[poiselected];
								delete poiref.attr[tf._parent.lastkey];
								_root.map.pois[poiselected].clean=false; break;
				case 'way':		delete _root.ws.attr[tf._parent.lastkey];
								_root.ws.clean=false; break;
				case 'relation':delete _root.editingrelation.attr[tf._parent.lastkey];
								_root.editingrelation.clean=false; break;
			}
			setValueFromTextfield(tf._parent.value);
			tf._parent.lastkey=k;
		}
	};

	// =====================================================================================
	// RelMember object

	function RelMember() {};
	RelMember.prototype=new MovieClip();

	RelMember.prototype.init=function(rel_id) {
		this.rel = _root.map.relations[rel_id];

		// Grey background
		this.createEmptyMovieClip('grey',1);
		with (this.grey) {
			beginFill(0x909090,100);
			moveTo(0,0); lineTo(182,0);
			lineTo(182,17); lineTo(0,17);
			endFill();
			lineStyle(1,0xDDDDDD,100);
			moveTo(115,2); lineTo(115,15);
			moveTo(166,2); lineTo(166,15);
		};

		// Initialise key
		this.createEmptyMovieClip('keynameclick', 2);
		this.keynameclick.createTextField('keyname',1,0,-1,118,18);
		var t=this.rel.getType(); var n=this.rel.getName();
		with (this.keynameclick.keyname) {
			type='dynamic'; selectable=false;
			text=n; setTextFormat(plainWhite);
			setNewTextFormat(boldWhite); replaceSel(t+" ");
		};
		this.keynameclick.onPress=function() {
			this._parent.rel.editRelation();
		};
		this.keynameclick.onRollOver=function() { this._parent.rel.setHighlight(true); };
		this.keynameclick.onRollOut=function() { this._parent.rel.setHighlight(false); };

		// Remove icon
		this.attachMovie("closecross", "i_remove", 4);
		with (this.i_remove) { _x=174; _y=8; };
		this.i_remove.onPress=function() { this._parent.removeRelation(); };

		// Role (value)
		this.createTextField('value',3,116,1,50,15);	// 3,92,-1,70,18
		with (this.value) {
			backgroundColor=0xDDDDDD;
			background=true;
			type='input';
			setTextFormat(plainTiny);
			setNewTextFormat(plainTiny);
			restrict="^"+chr(0)+"-"+chr(31);
			maxChars=255;
		};
		this.value.text=this.getRole();
		this.value.onSetFocus =function() { this._parent.scrollToField();
											_root.keytarget='value'; };
		this.value.onKillFocus=function() { _root.keytarget=''; };
		this.value.onChanged  =function(tf) { this._parent.setRole(tf); };
	};

	RelMember.prototype.getRole=function() {
		var v;
		switch (this._parent._parent.proptype) {
			case 'point':	v=this.rel.getNodeRole(_root.ws.path[_root.pointselected].id); break;
			case 'POI':		v=this.rel.getNodeRole(poiselected); break;
			case 'way':		v=this.rel.getWayRole(wayselected); break;
		}
		if (v==undefined) { v='(type value here)'; }
		return v;		
	};

	RelMember.prototype.setRole=function(tf) {
		var role = tf.text;
		switch (this._parent._parent.proptype) {
			case 'point':	v=this.rel.setNodeRole(_root.ws.path[_root.pointselected].id, role); break;
			case 'POI':		v=this.rel.setNodeRole(poiselected, role); break;
			case 'way':		v=this.rel.setWayRole(wayselected, role); break;
		}
	};

	RelMember.prototype.removeRelation=function() {
		switch (this._parent._parent.proptype) {
			case 'point': this.rel.removeNode(_root.ws.path[_root.pointselected].id); break;
			case 'POI':   this.rel.removeNode(poiselected); break;
			case 'way':   this.rel.removeWay(wayselected); break;
		}
		_root.panel.properties.reinit();
	};

	RelMember.prototype.scrollToField=KeyValue.prototype.scrollToField;

	Object.registerClass("relmember",RelMember);




	// =====================================================================================
	// General support functions

	// setTypeText - set contents of type window
	
	function setTypeText(a,b) {
		_root.panel.t_type.text=a;    _root.panel.t_type.setTextFormat(boldText);
		_root.panel.t_details.text=b; _root.panel.t_details.setTextFormat(plainText);
		if (_root.ws.locked ||
			_root.map.pois[_root.poiselected].locked) {
			_root.panel.padlock._visible=true;
			_root.panel.padlock._x=_root.panel.t_details.textWidth+15;
		} else {
			_root.panel.padlock._visible=false;
		}
		_root.panel.createEmptyMovieClip('historylink',25);
		with (_root.panel.historylink) {
			beginFill(0,0); moveTo(5,23);
			lineTo(7+_root.panel.t_details.textWidth,23);
			lineTo(7+_root.panel.t_details.textWidth,23+_root.panel.t_details.textHeight);
			lineTo(5,23+_root.panel.t_details.textHeight); lineTo(5,23);
		};
		_root.panel.historylink.onPress=getHistory;
		_root.panel.historylink.useHandCursor=true;
		_root.panel.historylink.onRollOver=function() {
			var v;
			if (_root.poiselected) { v=_root.map.pois[poiselected].version; }
			else if (_root.pointselected>-2) {
				v=_root.ws.path[_root.pointselected].version+", in ways ";
				var w=_root.ws.path[_root.pointselected].ways; for (var i in w) { v+=i+","; }
				v=v.substr(0,v.length-1);
			}
			else { v=_root.ws.version; }
			setFloater("Version "+v);
		};
		_root.panel.historylink.onRollOut =function() { clearFloater(); };
	}

	// getPanelColumns - how many columns can fit into the panel?

	function getPanelColumns() {
		return Math.max(Math.floor((Stage.width-110-15)/190),1);
	}

	// getGlobalCoord - where on the screen is the textfield?

	function getGlobalCoord(tf) {
		var pt=new Object();
		pt.x=tf._parent._x+72*(tf._name=='value');
		pt.y=tf._parent._y-2;
		tf._parent.localToGlobal(pt);
		return new Array(pt.x-tf._parent._x,pt.y-tf._parent._y);
	}

	// updateButtons - set alpha for buttons
	
	function updateButtons() {
		var pt=_root.panel.properties.proptype;
		_root.panel.i_repeatattr._alpha=100-50*(pt=='' || _root.saved[pt]=='');
		_root.panel.i_newattr._alpha   =100-50*(pt=='');
		_root.panel.i_newrel._alpha    =100-50*(pt=='');
	}

	function updateScissors(v) {
		_root.panel.i_scissors._alpha=50+50*v;
	}

	// hashLength
	
	function hashLength (a) {
		var l=0;
		for (var i in a) { l++; }
		return l;
	}

