
	// =====================================================================================
	// properties.as
	// Potlatch property window functions
	// =====================================================================================

	// =====================================================================================
	// Autocomplete

	// Still to do
	// - some way of stopping keys being added twice with same name
	//   (probably rename to highway_2?)
	// - scroll list when lots available 
	// - wipe preset menu when POI reverted
	// - some way of invoking full autocomplete menu when value deleted
	//   (probably using up cursor/down cursor)

	AutoMenu=function() {};
	AutoMenu.prototype=new MovieClip();
	AutoMenu.prototype.redraw=function() {

		// Find possible values for autocomplete
		var a=Selection.getFocus().split('.');	// 2 is key, 3 is keyname|value
		this.key=a[2];
		this.field=a[3];
		var curval=_root.properties[this.key][this.field].text;
		if (this.field=='value' && curval=='' &&
		   (_root.lastkeypressed==8 || _root.lastkeypressed==46)) {
			// if user has just deleted value, don't show autocomplete!
			this.remove(); return;
		}

		if (curval=='key') { curval=''; }					// full menu on 'key'
		var possible=new Array();
		if (this.field=='keyname') {
			z=_root.autotags[_root.currentproptype];		// key
			for (i in z) {
				if (i.slice(0,curval.length)==curval && !_root.properties[i]) { possible.push(i); }
			}
			possible.sort();
		} else {
			z=_root.autotags[_root.currentproptype][this.key];	// value
			for (i in z) {
				if (z[i].slice(0,curval.length)==curval) { possible.push(z[i]); }
			}
		}

		// Draw autocomplete window
		var wx=_root.properties[this.key]._x+110+72*(this.field!='keyname');
		var wy=_root.properties[this.key]._y+516;
		this.autonumkeys=Math.min(possible.length,Math.floor(wy/16)); // limit keys if will go off top of screen
		if (this.autonumkeys==0) { this.remove(); return; }

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
				var n=_root.properties[k][f].text;
				_root.properties[k].renameKey();
				this.ignorekill=true;
				Selection.setFocus(_root.properties[n].value);
			} else {
				this.remove();
				Selection.setFocus(null);
				_root.keytarget='';
			}
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
		_root.properties[this.key][this.field].text=newval;
		_root.properties[this.key][this.field].setTextFormat(plainSmall);
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
		if (Selection.getFocus().split('.')[3]=='keyname') {
			Selection.setFocus(eval(Selection.getFocus())._parent.value);
		} else {
			Selection.setFocus(null);
		}
	}

	Object.registerClass("auto",AutoMenu);

	// =====================================================================================
	// KeyValue object

	function KeyValue() {
		this._x=_root.propx;
		this._y=_root.propy;
		_root.propy+=19; if (_root.propy>57) { _root.propy=0; _root.propx+=190; }

		this.createTextField('keyname',1,0,-1,70,18);
		with (this.keyname) {
			backgroundColor=0xBBBBBB;
			background=true;
			text=this._name;
			setTextFormat(boldSmall);
			setNewTextFormat(boldSmall);
		};

		this.createTextField('value',2,72,-1,110,18);
		this.value.onSetFocus =function() {	if (this.textColor==0x888888) {
												this.text=''; this.textColor=0;
												if (!_root.auto) { _root.attachMovie("auto","auto",75); }
												_root.auto.redraw();
											}
											this.addListener(textfieldListener); _root.keytarget=this._name; _root.elselected=this._name;
											markAttrUnclean(false); };
		this.value.onKillFocus=function() { if (_root.auto.hitTest(_root._xmouse,_root._ymouse)) { return; }
											if (_root.auto.ignorekill) { _root.auto.ignorekill=false; return; }
											this.removeListener(textfieldListener); _root.keytarget='';
											if (this.text=='') { _root.redopropertywindow=1; } // crashes player if called directly!
											if (_root.currentproptype=='way') { _root.map.ways[wayselected].redraw(); }
											_root.auto.remove();
											reflectPresets(); };
		with (this.value) {
			this.value.backgroundColor=0xDDDDDD;
			this.value.background=true;
			this.value.type='input';
			// this.value.tabIndex=_root.propn;
		};
		this.value.variable=getAttrArrayName()+'.'+this._name;
		this.value.text=getAttrArray()[this._name];
		this.value.setTextFormat(plainSmall);
		this.value.setNewTextFormat(plainSmall);
		_root.propn+=1;
	};
	KeyValue.prototype=new MovieClip();

	KeyValue.prototype.renameKey=function() {
		z=this.keyname.text;
		if (z!=this._name) {
			// field has been renamed
			this.value.variable=null;
			getAttrArray()[z]=getAttrArray()[this._name];
			delete getAttrArray()[this._name];
			this._name=z; 
			this.value.variable=getAttrArrayName()+'.'+z;
		}
	};

	Object.registerClass("keyvalue",KeyValue);

	// populatePropertyWindow	- set contents of property window
	// clearPropertyWindow		- clear window

	function populatePropertyWindow(proptype,startat) {
		if (!startat) { startat=0; }
		if (_root.currentproptype ==proptype &&
			_root.currentpropway  ==wayselected &&
			_root.currentproppoi  ==poiselected &&
			_root.currentproppoint==pointselected &&
			_root.currentstartat  ==startat &&
			_root.redopropertywindow==0) { return; }
		clearPropertyWindow();
		_root.i_repeatattr._alpha=
		_root.i_newattr._alpha =100-50*(proptype=='');
		_root.i_scissors._alpha=100-50*(proptype!='point');
		if (proptype=='') { _root.currentproptype=''; _root.i_nextattr._alpha=50; return; }
		
		if (proptype!=currentproptype) { presetmenu.init(141,505,0,presetnames[proptype][presetselected],'Choose from a menu of preset tags describing the '+proptype,setAttributesFromPreset,151); }
		_root.currentproptype=proptype;
		_root.currentstartat=startat;
		_root.currentproppoint=pointselected;
		_root.currentpropway=wayselected;
		_root.currentproppoi=poiselected;
		var proparr=getAttrArray();
		for (el in proparr) {
			if (proparr[el]!='' && el!='created_by' && el!='edited_by') {
				if (tagcount>=startat && tagcount<startat+12) {
					_root.properties.attachMovie("keyvalue",el,_root.propn);
					if (proparr[el].substr(0,6)=='(type ') {
						_root.properties[el]['value'].textColor=0x888888;
					}
				}
				tagcount+=1;
			}
		}

		_root.i_nextattr._alpha=50+50*(tagcount>12);
		_root.presetmenu._visible=true;
		_root.i_preset._visible=true;
		reflectPresets();
		setTabOrder();
	};

	function clearPropertyWindow() {
		removeMovieClip(_root.welcome); 
		_root.propx=0; _root.propy=0; _root.propn=0; _root.tagcount=0;
		ct=0;
		for (el in _root.properties) {
			ct+=1;
			removeMovieClip(_root.properties[el]);
		}
		if (ct>0) { _root.savedpoint=_root.currentproppoint;
					_root.savedpoi  =_root.currentproppoi;
					_root.savedway  =_root.currentpropway;
					_root.savedtype =_root.currentproptype; }
	};

	function advancePropertyWindow() {
		if (_root.i_nextattr._alpha==50) { return; }
		if (_root.currentstartat+12>_root.tagcount) {
			populatePropertyWindow(_root.currentproptype,0);
		} else {
			populatePropertyWindow(_root.currentproptype,_root.currentstartat+12);
		}
	};

	// setTypeText - set contents of type window
	
	function setTypeText(a,b) {
		_root.t_type.text=a; _root.t_type.setTextFormat(boldText);
		_root.t_details.text=b; _root.t_details.setTextFormat(plainText);
		if (_root.map.ways[_root.wayselected].locked ||
			_root.map.pois[_root.poiselected].locked) {
			_root.padlock._visible=true;
			_root.padlock._x=_root.t_details.textWidth+15;
		} else {
			_root.padlock._visible=false;
		}
	}

	// reflectPresets - set preset menu based on way values
	// looks in presetselected first, then in all other menus

	function reflectPresets() {
		var i,t;
		var found=findPresetInMenu(presetselected);
		if (found) { presetmenu.setValue(found); return; }
		for (i=0; i<presetmenus[currentproptype].length; i+=1) {
			t=findPresetInMenu(presetmenus[currentproptype][i]); if (t) { found=t; presetselected=presetmenus[currentproptype][i]; }
		}
		if (found) { presetmenu.init(141,505,found,presetnames[currentproptype][presetselected],'Choose from a menu of preset tags describing the '+currentproptype,setAttributesFromPreset,151);
					 setPresetIcon(presetselected); }
			  else { presetmenu.setValue(0); }
	}

	// look in a particular menu
	
	function findPresetInMenu(menuname) {
		var f=0;
		for (pre=presetnames[currentproptype][menuname].length-1; pre>-1; pre-=1) {
			pname=presetnames[currentproptype][menuname][pre];
			pkeys=_root.presets[pname];
			if (pkeys) {
				ok=1;
				for (pkey in pkeys) {
					cvalue=getAttrArray()[pkey];
					if (cvalue==null) { cvalue=''; }
					if (cvalue!=presets[pname][pkey] && presets[pname][pkey].substr(0,6)!='(type ') { ok=0; }
				}
				if (ok==1) { f=pre; }
			}
		}
		return f;
	}

	// setAttributesFromPreset - update way values based on pop-up choice
	
	function setAttributesFromPreset(pre) {
		pname=presetnames[currentproptype][presetselected][pre];
		pkeys=presets[pname];
		for (pkey in pkeys) {
			if (getAttrArray()[pkey].length>0 && presets[pname][pkey].substr(0,6)=='(type ') {}
			else { getAttrArray()[pkey]=presets[pname][pkey]; }
			markAttrUnclean(true);
		}
		populatePropertyWindow(currentproptype);
	}

	// setPresetIcon and cyclePresetIcon

	function setPresetIcon(category) {
		 _root.attachMovie("preset_"+category,"i_preset",38);
		 with (_root.i_preset) { _x=120; _y=515; };
		_root.i_preset.onPress=function() { cyclePresetIcon(); };
		_root.i_preset.onRollOver=function() { setFloater("Choose what type of presets are offered in the menu"); };
		_root.i_preset.onRollOut =function() { clearFloater(); };
	}
	function cyclePresetIcon() {
		var i,j;
		if (_root.i_preset._visible) {
			j=0;
			for (i=0; i<presetmenus[currentproptype].length; i+=1) {
				if (presetmenus[currentproptype][i]==presetselected) { j=i+1; }
			}
			presetselected=presetmenus[currentproptype][j%i];
			setPresetIcon(presetselected);
			presetmenu.init(141,505,findPresetInMenu(presetselected),presetnames[currentproptype][presetselected],'Choose from a menu of preset tags describing the '+currentproptype,setAttributesFromPreset,151);
		}
	}

	// enterNewAttribute - create new attribute

	function enterNewAttribute() {
		if (_root.wayselected==0 && _root.pointselected==-2 && _root.poiselected==0) { return; }
		if (_root.tagcount>=_root.currentstartat+12) {
			populatePropertyWindow(_root.currentproptype,Math.floor((_root.tagcount+1)/12)*12);
		}
//		if (_root.propn==12) { return; }
		getAttrArray().key='(type value here)';
		_root.properties.attachMovie("keyvalue","key",_root.propn);
		_root.tagcount+=1; _root.i_nextattr._alpha=50+50*(tagcount>12);
		_root.properties.key['value'].textColor=0x888888;
		_root.properties.key.keyname.type="input";
		_root.properties.key.keyname.setTextFormat(boldSmall);
		_root.properties.key.keyname.setNewTextFormat(boldSmall);
		_root.properties.key.keyname.onSetFocus=function()  { 
			this.addListener(textfieldListener);
			_root.keytarget=this._name;
		};
		_root.properties.key.keyname.onKillFocus=function() {
			if (_root.auto.hitTest(_root._xmouse,_root._ymouse)) { return; }
			_root.keytarget='';
			this.removeListener(textfieldListener);
			_root.auto.remove();
			if (this._parent.keyname.text=='' || this._parent.keyname.text==undefined) { 
				delete getAttrArray()[this._parent._name];
				removeMovieClip(this._parent);
				_root.tagcount-=1;
			} else {
				this._parent.renameKey();
			}
		};
		setTabOrder();
		Selection.setFocus(_root.properties.key.keyname); Selection.setSelection(0,3);
		if (!_root.auto) { _root.attachMovie("auto","auto",75); }
		_root.auto.redraw();
	}
	
	// getAttrArray		- return a reference to the current attribute array
	// getAttrArrayName	- return the variable name for the current attribute array
	// markAttrUnclean	- attributes have been changed, so mark as unclean
	
	function getAttrArray() {
		switch (_root.currentproptype) {
			case 'point':	return _root.map.ways[_root.wayselected].path[_root.pointselected][4]; break;
			case 'POI':		return _root.map.pois[poiselected].attr; break;
			case 'way':		return _root.map.ways[wayselected].attr; break;
		}
	}

	function getAttrArrayName() {
		switch (_root.currentproptype) {
			case 'point':	return "_root.map.ways."+wayselected+".path."+pointselected+".4"; break;
			case 'POI':		return "_root.map.pois."+poiselected+".attr"; break;
			case 'way':		return "_root.map.ways."+wayselected+".attr"; break;
		}
	}

	function markAttrUnclean(redrawflag) {
		markClean(false);
		switch (currentproptype) {
			case 'point':	_root.map.ways[wayselected].clean=false; break;
			case 'POI':		_root.map.pois[poiselected].clean=false; break;
			case 'way':		if (redrawflag) { _root.map.ways[wayselected].redraw(); }
							_root.map.ways[wayselected].clean=false; break;
		}
	}
	
	// repeatAttributes - paste in last set of attributes
	
	function repeatAttributes() {
		var i,z;
		if (_root.wayselected==0 && _root.pointselected==-2 && _root.poiselected==0) { return; }
		switch (savedtype) {
			case 'point':	z=_root.map.ways[savedway].path[savedpoint][4]; break;
			case 'POI':		z=_root.map.pois[savedpoi].attr; _root.map.pois[poiselected].attr=new Array(); break;
			case 'way':		z=_root.map.ways[savedway].attr; break;
		}
		for (i in z) {
			if (Key.isDown(Key.SHIFT) && (i=='name' || i=='ref') || i=='created_by') {
				// ignore name and ref if SHIFT pressed
			} else {
				switch (savedtype) {
					case 'point':	j=_root.map.ways[savedway].path[savedpoint][4][i]; break;
					case 'POI':		j=_root.map.pois[savedpoi].attr[i]; break;
					case 'way':		j=_root.map.ways[savedway].attr[i]; break;
				}
				getAttrArray()[i]=j;
				markAttrUnclean(true);
			}
		}
		populatePropertyWindow(_root.currentproptype);
	}
	
	// textChanged			- listener marks way as dirty when any change made

	function textChanged() { 
		if (Selection.getFocus()=='_level0.properties.key.keyname' && 
		   (_root.properties.key.keyname.text=='+' ||
		    _root.properties.key.keyname.text=='=')) {
			// annoying workaround when user has pressed '+'= and FP
			// automatically uses it as the name of the new key...
			_root.properties.key.keyname.text='key';
			_root.properties.key.keyname.setTextFormat(boldSmall);
			_root.properties.key.keyname.setNewTextFormat(boldSmall);
			Selection.setFocus(_root.properties.key.keyname); Selection.setSelection(0,3);
		}
		markAttrUnclean(false);
		if (!_root.auto) { _root.attachMovie("auto","auto",75); }
		_root.auto.redraw();
	}
	
	// setTabOrder		- fix order for tabbing between fields

	function setTabOrder() {
		for (el in _root.properties) {
			o=(_root.properties[el]._x/190*4+_root.properties[el]._y/19)*2;
			_root.properties[el].keyname.tabIndex=o;
			_root.properties[el].value.tabIndex=o+1;
		}
	}

