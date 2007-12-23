
	// =====================================================================================
	// Standard UI
	// =====================================================================================

	// Checkboxes
	// UICheckbox.init(x,y,text,state,changefunction)

	UICheckbox=function() {
	};
	UICheckbox.prototype=new MovieClip();
	UICheckbox.prototype.init=function(x,y,prompttext,state,changefunction) {
		this._x=x;
		this._y=y;
		this.state=state;
		this.doOnChange=changefunction;
		this.createTextField('prompt',0,13,-5,200,19);
		this.prompt.text=prompttext;
		this.prompt.setTextFormat(plainSmall);
		tw=this.prompt._width=this.prompt.textWidth+5;

		this.createEmptyMovieClip('hitregion',1);
		with (this.hitregion) {
			clear(); beginFill(0,0);
			moveTo(0,0); lineTo(tw+15,0);
			lineTo(tw+15,15); lineTo(0,15);
			endFill();
 		};
		this.hitregion.onPress=function() {
			this._parent.state=!this._parent.state;
			this._parent.draw();
			this._parent.doOnChange(this._parent.state);
		};

		this.createEmptyMovieClip('box',2);
		this.draw();
	};
	UICheckbox.prototype.draw=function() {
		with (this.box) {
			clear();
			lineStyle(2,0,100);
			moveTo(1,0);
			lineTo(9,0); lineTo(9,9);
			lineTo(0,9); lineTo(0,0);
			if (this.state==true) {
				lineStyle(2,0,100);
				moveTo(1,1); lineTo(8,8);
				moveTo(8,1); lineTo(1,8);
			}
		}
	};
	Object.registerClass("checkbox",UICheckbox);

	// Pop-up menu
	// UIMenu.init(x,y,selected option,array of options,tooltip,
	//			   function to call on close,width)
	
	function UIMenu() {
	};
	UIMenu.prototype=new MovieClip();
	UIMenu.prototype.init=function(x,y,selected,options,tooltip,closefunction,menuwidth) {
		var i,w,h;
		this._x=x; this._y=y;
		this.selected=selected; this.original=selected;
		this.options=options;

		// create (invisible) movieclip for opened menu
		this.createEmptyMovieClip("opened",2);
		this.opened._visible=false;
		// create child for each option
		this.tw=0;
		for (i=0; i<options.length; i+=1) {
			this.opened.createTextField(i,i+1,3,i*16-1,100,19);
			this.opened[i].text=options[i];
			this.opened[i].background=true;
			this.opened[i].backgroundColor=0x888888;
			this.opened[i].setTextFormat(menu_off);
			if (this.opened[i].textWidth*1.05>this.tw) { this.tw=this.opened[i].textWidth*1.05; }
		};
		for (i=0; i<options.length; i+=1) {
			this.opened[i]._width=this.tw;
		}
		// create box around menu
		this.opened.createEmptyMovieClip("box",0);
		w=this.tw+7;
		h=options.length*16+5;
		with (this.opened.box) {
			_y=-2;
			clear();
			beginFill(0x888888,100);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		};

		// create (visible) movieclip for closed menu
		if (menuwidth>0) { w=menuwidth; } else { w+=11; }
		this.createEmptyMovieClip("closed",1);
		this.closed.createEmptyMovieClip("box",0);
		with (this.closed.box) {
			clear();
			beginFill(0x888888,100);
			lineTo(w,0 ); lineTo(w,17);
			lineTo(0,17); lineTo(0,0 ); endFill();
			beginFill(0xFFFFFF,100);
			moveTo(w-11,7); lineTo(w-3,7);
			lineTo(w-7,13); lineTo(w-11,7); endFill();
		};
		this.closed.createTextField("current",2,3,-1,this.tw,19);
		this.closed.current.text=options[selected];
		this.closed.current.setTextFormat(menu_off);
		this.closed.current.background=false;
//		this.closed.current.backgroundColor=0x888888;

		this.onPress=function() { clearFloater(); this.openMenu(); };
		this.onRelease=function() { this.closeMenu(); };
		this.onReleaseOutside=function() { this.closeMenu(); };
		this.onMouseMove=function() { this.trackMenu(); };
		this.doOnClose=closefunction;
		this.opened[this.selected].backgroundColor=0xDDDDDD;
		this.opened[this.selected].setTextFormat(menu_on);

		if (tooltip!='') {
			this.onRollOver=function() { setFloater(tooltip); };
			this.onRollOut =function() { clearFloater(); };
		}
	};
	UIMenu.prototype.trackMenu=function() {
		if (this.opened._visible) {
			this.opened[this.selected].backgroundColor=0x888888;
			this.opened[this.selected].setTextFormat(menu_off);
			this.selected=this.whichSelection();
			this.opened[this.selected].backgroundColor=0xDDDDDD;
			this.opened[this.selected].setTextFormat(menu_on);
		}
	};
	UIMenu.prototype.openMenu=function() {
		this.closed._alpha=50;
		this.opened._visible=true;
		this.opened._y=-15*this.selected;

		t=new Object(); t.x=0; t.y=this.opened._height;
		this.opened.localToGlobal(t);
		while (t.y>uheight) { this.opened._y-=15; t.y-=15; }
		this.trackMenu();
	};
	UIMenu.prototype.closeMenu=function() {
		if (this.selected>-1) {
			this.original=this.selected;
			this.closed.current.text=this.options[this.selected];
			this.closed._alpha=100;
			this.opened._visible=false;
			mflash=this; flashcount=2;
			mflashid=setInterval(function() { mflash.menuFlash(); }, 40);
			this.doOnClose(this.selected);
		} else {
			this.closed.current.text=this.options[this.original];
			this.closed.current.setTextFormat(menu_off);
			this.closed._alpha=100;
			this.opened._visible=false;
		}
	};
	UIMenu.prototype.setValue=function(n) {
		this.opened[this.selected].backgroundColor=0x888888;
		this.opened[this.selected].setTextFormat(menu_off);
		this.selected=n; this.original=n;
		this.closed.current.text=this.options[this.selected];
		this.closed.current.setTextFormat(menu_off);
	};
	UIMenu.prototype.whichSelection=function() {
		mpos=new Object();
		mpos.x=_root._xmouse;
		mpos.y=_root._ymouse;
		this.opened.globalToLocal(mpos);
		if (mpos.x>0 && mpos.x<this.tw && mpos.y>3 && mpos.y<this.options.length*15+3) {
			return Math.floor((mpos.y-1)/15);
		}
		return -1;
	};
	UIMenu.prototype.menuFlash=function() {
		// ** flashcount and mflashid are globals, and really shouldn't be
		flashcount-=1; if (flashcount==0) { clearInterval(mflashid); };
		if (flashcount/2!=Math.floor(flashcount/2)) {
			this.closed.current.backgroundColor=0xDDDDDD;
			this.closed.current.setTextFormat(menu_on);
		} else {
			this.closed.current.backgroundColor=0x888888;
			this.closed.current.setTextFormat(menu_off);
		}
		updateAfterEvent();
	};

	Object.registerClass("menu",UIMenu);
	
	// modalDialogue

	function createModalDialogue(w,h,buttons,closefunction) {
		clearFloater();
		_root.createEmptyMovieClip("modal",0xFFFFFE);
		var ox=(uwidth-w)/2; var oy=(uheight-100-h)/2;	// -100 for visual appeal

		// Blank all other areas
		_root.modal.createEmptyMovieClip("blank",1);
		with (_root.modal.blank) {
			beginFill(0xFFFFFF,0); moveTo(0,0); lineTo(uwidth,0);
			lineTo(uwidth,uheight); lineTo(0,uheight); lineTo(0,0); endFill();
		}
		_root.modal.blank.onPress=function() {};
		_root.modal.blank.useHandCursor=false;
		_root.keytarget='dialogue';

		// Create dialogue box
		_root.modal.createEmptyMovieClip("box",2);
		with (_root.modal.box) {
			_x=ox; _y=oy;
			beginFill(0xBBBBBB,100);
			moveTo(0,0);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		}

		// Create buttons
		for (var i=0; i<buttons.length; i+=1) {
			_root.modal.box.createEmptyMovieClip(i,i*2+1);
			drawButton(_root.modal.box[i],w-60*(buttons.length-i),h-30,buttons[i],"");
			_root.modal.box[i].onPress=function() {
				if (closefunction) { closefunction(buttons[this._name]); }
				clearModalDialogue();
			};
			_root.modal.box[i].useHandCursor=true;
		}
	}

	function clearModalDialogue() {
		_root.keytarget='';
		_root.createEmptyMovieClip("modal",0xFFFFFE);
	}


	// drawButton		- draw white-on-grey button
	// (object,x,y,button text, text to right)

	function drawButton(buttonobject,x,y,btext,ltext) {
		with (buttonobject) {
			_x=x; _y=y;
			beginFill(0x7F7F7F,100);
			moveTo(0,0);
			lineTo(50,0); lineTo(50,17);
			lineTo(0,17); lineTo(0,0); endFill();
		}
		buttonobject.useHandCursor=true;
		buttonobject.createTextField('btext',1,0,-1,48,20);
		with (buttonobject.btext) {
			text=btext; setTextFormat(boldWhite);
			selectable=false; type='dynamic';
			_x=(45-textWidth)/2;
		}
		if (ltext!="") {
			buttonobject.createTextField("explain",2,54,-1,300,20);
			writeText(buttonobject.explain,ltext);
		}
	}
