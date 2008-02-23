
	// =====================================================================================
	// properties.as
	// Potlatch property window functions
	// =====================================================================================

	// Saturday 23rd:
	// **** if you add two keys in succession, sometimes doesn't notice the first (?)
	// ** autocomplete disabled
	// ** preset menu disabled
	// ** doesn't redraw way according to tag change

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
	AutoMenu.prototype.redraw=function() {

		// Find possible values for autocomplete
		var a=Selection.getFocus().split('.');	// 3 is key, 4 is keyname|value
		this.key=a[3];
		this.field=a[4];
		var curval=_root.panel.properties[this.key][this.field].text;
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
				if (i.slice(0,curval.length)==curval && !_root.panel.properties[i]) { possible.push(i); }
			}
			possible.sort();
		} else {
			z=_root.autotags[_root.currentproptype][this.key];	// value
			for (i in z) {
				if (z[i].slice(0,curval.length)==curval) { possible.push(z[i]); }
			}
		}

		// Draw autocomplete window
		var wx=_root.panel.properties[this.key]._x+110+72*(this.field!='keyname');
		var wy=_root.panel.properties[this.key]._y+16;
		this.autonumkeys=Math.min(possible.length,Math.floor((wy+Stage.height-100)/16)); // limit keys if will go off top of screen
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
				var n=_root.panel.properties[k][f].text;
				_root.panel.properties[k].renameKey();
				this.ignorekill=true;
				Selection.setFocus(_root.panel.properties[n].value);
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
		_root.panel.properties[this.key][this.field].text=newval;
		_root.panel.properties[this.key][this.field].setTextFormat(plainSmall);
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
		if (Selection.getFocus().split('.')[4]=='keyname') {
			Selection.setFocus(eval(Selection.getFocus())._parent.value);
		} else {
			Selection.setFocus(null);
		}
	}

	Object.registerClass("auto",AutoMenu);


	// =====================================================================================
	// PropertyWindow object
	// temporarily removed:
	// ** support for more than 12 tags
	// ** autocomplete
	// ** repeat last tags

	PropertyWindow=function() {};
	PropertyWindow.prototype=new MovieClip();
	
	PropertyWindow.prototype.init=function(proptype) {
		removeMovieClip(_root.panel.welcome); 
		this.createEmptyMovieClip("attributes",1);
		this.proptype=proptype;
		if (proptype=='') { return; }

		switch (proptype) {
			case 'point':	proparr=_root.ws.path[_root.pointselected][4]; break;
			case 'POI':		proparr=_root.map.pois[poiselected].attr; break;
			case 'way':		proparr=_root.ws.attr; break;
		}

		// Attach keys/values		
		this.tagcount=0;
		this.xpos=0; this.ypos=0;
		for (el in proparr) {
			if (proparr[el]!='' && el!='created_by' && el!='edited_by') {
				this.attributes.attachMovie("keyvalue",this.tagcount,this.tagcount);
				this.attributes[this.tagcount]._x=this.xpos;
				this.attributes[this.tagcount]._y=this.ypos;
				this.ypos+=19; if (this.ypos>57) { this.ypos=0; this.xpos+=190; }
				this.attributes[this.tagcount].init(el);
				this.tagcount+=1;
			}
		}

		// ** Attach relations in same way as above
		// ** Set i_repeatattr, i_newattr, i_scissors?
		// ** Do presetmenu (reflectPresets) and i_preset?

		this.setTabOrder();
	};

	PropertyWindow.prototype.setTabOrder=function() {
		// ** This could perhaps be incorporated into init...
		for (el in this.attributes) {
			o=(this.attributes[el]._x/190*4+this.attributes[el]._y/19)*2;
			this.attributes[el].keyname.tabIndex=o;
			this.attributes[el].value.tabIndex=o+1;
		}
	};

	// PropertyWindow.enterNewAttribute

	PropertyWindow.prototype.enterNewAttribute=function() {
		// ** check nothing already exists called "key"
		this.attributes.attachMovie("keyvalue",this.tagcount,this.tagcount);
		this.attributes[this.tagcount]._x=this.xpos;
		this.attributes[this.tagcount]._y=this.ypos;
		this.ypos+=19; if (this.ypos>57) { this.ypos=0; this.xpos+=190; }
		this.attributes[this.tagcount].init('key');
		this.setTabOrder();
		Selection.setFocus(this.attributes[this.tagcount].keyname);
		Selection.setSelection(0,3);
		this.tagcount+=1;
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
//			selectable=false;
		};
		this.keyname.onSetFocus=function() { _root.keytarget='key'; };
		this.keyname.onKillFocus=function() { _root.keytarget=''; };
		this.keyname.onChanged=function(tf) { renameKey(tf); };

		// Initialise value

		this.createTextField('value',2,72,-1,110,18);
		this.value.onSetFocus =function() {
			if (this.textColor==0x888888) {
				this.text=''; this.textColor=0;
				// ** autocomplete support
			}
			_root.keytarget='value'; _root.elselected=this._name;
			markAttrUnclean(false);
		};
		this.value.onKillFocus=function() {
			_root.keytarget='';
			if (this.text=='') { _root.redopropertywindow=1; } // crashes player if called directly!
			// ** redraw way; autocomplete support; reflect presets
		};
		with (this.value) {
			backgroundColor=0xDDDDDD;
			background=true;
			type='input';
			setTextFormat(plainSmall);
			setNewTextFormat(plainSmall);
		};
		this.value.text=this.getValueFromObject(key);
		this.value.onChanged=function(tf) { setValueInObject(tf); };
		this.lastkey=key;
	};

	// KeyValue.getValueFromObject(key)
	// for a given key, returns the value from the way, point or POI

	KeyValue.prototype.getValueFromObject=function(k) {
		var v;
		switch (this._parent._parent.proptype) {
			case 'point':	v=_root.ws.path[_root.pointselected][4][k]; break;
			case 'POI':		v=_root.map.pois[poiselected].attr[k]; break;
			case 'way':		v=_root.ws.attr[k]; break;
		}
		if (v==undefined) { v='(type value here)'; }
		return v;
	};

	Object.registerClass("keyvalue",KeyValue);

	// =====================================================================================
	// KeyValue support functions

	// setValueInObject(value textfield)
	// - update the way, point or POI with the new value
	//   (opposite of getValueFromObject)
	
	function setValueInObject(tf) {
		var k=tf._parent.keyname.text;
		var v=tf.text;
		switch (tf._parent._parent._parent.proptype) {
			case 'point':	_root.ws.path[_root.pointselected][4][k]=v; 
							_root.ws.clean=false; break;
			case 'POI':		_root.map.pois[poiselected].attr[k]=v;
							_root.map.pois[poiselected]=false; break;
			case 'way':		_root.ws.attr[k]=v; 
							_root.ws.clean=false; break;
		}
	};

	// renameKey(key textfield)

	function renameKey(tf) {
		var k=tf.text;
		if (k=='+') {
			// if FP has picked up the "+" keypress, ignore it and set back to 'key'
			tf.text='key';
			tf.setTextFormat(boldSmall);
			tf.setNewTextFormat(boldSmall);
			Selection.setFocus(tf); Selection.setSelection(0,3);
		} else if (k!=tf._parent.lastkey) {
			// field has been renamed, so delete old one and set new one
			// (we have to set it to '' sometimes because of Ming delete bug)
			switch (tf._parent._parent._parent.proptype) {
				case 'point':	_root.ws.path[_root.pointselected][4][tf._parent.lastkey]=''; break;
				case 'POI':		_root.map.pois[poiselected].attr[tf._parent.lastkey]=''; break;
				case 'way':		delete _root.ws.attr[tf._parent.lastkey]; break;
			}
			setValueInObject(tf._parent.value);
			tf._parent.lastkey=k;
		}
	};


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
	}
