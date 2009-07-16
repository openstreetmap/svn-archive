
	// =====================================================================================
	// Standard UI
	// =====================================================================================

	// Radio buttons
	// UIRadio.init
	// UIRadio.addButton(x,y,text)
	// UIRadio.select(n)

	UIRadio=function() {
		this.selected=0;
		this.buttons=0;
		this.xpos=new Array();
		this.ypos=new Array();
		this.doOnChange=null;
	};
	UIRadio.prototype=new MovieClip();
	UIRadio.prototype.addButton=function(x,y,prompttext) {
		var i=++this.buttons;
		this.createEmptyMovieClip(i,i);
		this[i].attachMovie('radio_off','radio',1);
		this[i]._x=this.xpos[i]=x;
		this[i]._y=this.ypos[i]=y;
		createHitRegion(this[i],prompttext,2,3);
		this[i].onPress=function() { if (this._alpha>60) { this._parent.select(this._name); } };
	};
	UIRadio.prototype.select=function(n) {
		var i,s;
		for (i=1; i<=this.buttons; i++) {
			if (i==n) { s='radio_on'; } else { s='radio_off'; }
			this[i].attachMovie(s,'radio',1);
		}
		this.selected=n;
		if (this.doOnChange!=null) { this.doOnChange(n); }
	};
	UIRadio.prototype.enable =function(i) { this[i]._alpha=100; this[i].prompt.setTextFormat(plainSmall); };
	UIRadio.prototype.disable=function(i) { this[i]._alpha= 50; this[i].prompt.setTextFormat(plainDim);   };
	Object.registerClass("radio",UIRadio);

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
		createHitRegion(this,prompttext,0,1);
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
	//			   function to call on close,value of 'this' on close,
	//			   width)
	
	function UIMenu() {
	};
	UIMenu.prototype=new MovieClip();
	UIMenu.prototype.init=function(x,y,selected,options,tooltip,closefunction,closethis,menuwidth) {
		var i,w,h;
		this._x=x; this._y=y;
		this.selected=selected; this.original=selected;
		this.options=options;

		// create (invisible) movieclip for opened menu
		this.createEmptyMovieClip("opened",2);
		this.opened._visible=false;
		// create child for each option
		var tw=0;
		for (i=0; i<options.length; i+=1) {
			this.opened.createTextField(i,i+1,3,i*16-1,100,19);
			this.opened[i].text=options[i];
			this.opened[i].background=true;
			this.opened[i].backgroundColor=0x888888;
			this.opened[i].setTextFormat(menu_off);
			if (this.opened[i].textWidth*1.05>tw) { tw=this.opened[i].textWidth*1.05; }
		};
		// create box around menu
		this.opened.createEmptyMovieClip("box",0);
		w=tw+7; if (menuwidth>w) { w=menuwidth; } 
		this.itemwidth=w-7;
		h=options.length*16+5;
		with (this.opened.box) {
			_y=-2;
			clear();
			beginFill(0x888888,100);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		};
		// adjust all menus to have correct highlight
		for (i=0; i<options.length; i+=1) {
			this.opened[i]._width=this.itemwidth;
		}

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
		this.closed.createTextField("current",2,3,-1,this.itemwidth,19);
		this.closed.current.text=options[selected];
		this.closed.current.setTextFormat(menu_off);
		this.closed.current.background=false;
//		this.closed.current.backgroundColor=0x888888;

		this.onPress=function() { clearFloater(); this.openMenu(); };
		this.onRelease=function() { this.closeMenu(); };
		this.onReleaseOutside=function() { this.closeMenu(); };
		this.onMouseMove=function() { this.trackMenu(); };
		this.doOnClose=closefunction;
		this.closeThis=closethis;
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
//		this.opened._y=-15*this.selected;
		this.opened._y=-15*this.original;

		t=new Object(); t.x=0; t.y=this.opened._height;
		this.opened.localToGlobal(t);
		while (t.y>Stage.height) { this.opened._y-=15; t.y-=15; }
		this.trackMenu();
	};
	UIMenu.prototype.closeMenu=function() {
		if (this.selected>-1) {
			this.original=this.selected;
			this.closed.current.text=this.options[this.selected];
			this.closed.current.setTextFormat(menu_off);
			this.closed._alpha=100;
			this.opened._visible=false;
			mflash=this; flashcount=2;
			mflashid=setInterval(function() { mflash.menuFlash(); }, 40);
			this.doOnClose.call(this.closeThis,this.selected);
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
		if (mpos.x>0 && mpos.x<this.itemwidth && mpos.y>0 && mpos.y<this.options.length*15) {
			return Math.floor((mpos.y)/15);
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


	// ========================================================================
	// ModalDialogue

	// ModalDialogue.prototype.init(w,h,buttons,closefunction,leavepanel);
	// ModalDialogue.prototype.remove();
	// ModalDialogue.prototype.drawAreas();

	ModalDialogue=function() {};
	ModalDialogue.prototype=new MovieClip();
	ModalDialogue.prototype.init=function(w,h,buttons,closefunction,leavepanel) {
		clearFloater();
		this.createEmptyMovieClip("blank",1);
		this.createEmptyMovieClip("box",2);
		this.modalwidth=w;		// workaround for Stage.width suddenly changing
		this.modalheight=h;		//  |
		this.modalleave=leavepanel;
		this.drawAreas();
		
		// Create buttons
		for (var i=0; i<buttons.length; i+=1) {
			this.box.createEmptyMovieClip(i,i*2+1);
			drawButton(this.box[i],w-60*(buttons.length-i),h-30,buttons[i],"");
			this.box[i].onPress=function() {
				if (closefunction) {
					var keep = closefunction(buttons[this._name]);
					if ( !keep ) { this._parent._parent.remove(); }
				} else { 
					this._parent._parent.remove();
				}
			};
			this.box[i].useHandCursor=true;
		}
	};

	ModalDialogue.prototype.remove=function() {
		removeMovieClip(this);
	};

	ModalDialogue.prototype.drawAreas=function() {
		var w=this.modalwidth;
		var h=this.modalheight;
		var ox=(Stage.width-w)/2; var oy=(Stage.height-panelheight-h)/2;

		// Blank all other areas
		var bh=Stage.height;
		if (this.modalleave==2) { bh-=20; }
		else if (this.modalleave) { bh-=panelheight; }
		with (this.blank) {
			clear();
			beginFill(0xFFFFFF,20); moveTo(0,0); lineTo(Stage.width,0);
			lineTo(Stage.width,bh); lineTo(0,bh); lineTo(0,0); endFill();
		}
		this.blank.onPress=null;
		this.blank.useHandCursor=false;

		// Create dialogue box
		with (this.box) {
			_x=ox; _y=oy;
			clear();
			beginFill(0xBBBBBB,100);
			moveTo(0,0);
			lineTo(w,0); lineTo(w,h);
			lineTo(0,h); lineTo(0,0); endFill();
		}
	};

	Object.registerClass("modal",ModalDialogue);

	// ========================================================================
	// Support functions

	// drawButton		- draw white-on-grey button
	// (object,x,y,button text, text to right,width)

	function drawButton(buttonobject,x,y,btext,ltext,bwidth) {
        if (!bwidth) { bwidth=50; }
		buttonobject.useHandCursor=true;
		buttonobject.createTextField('btext',1,0,-1,bwidth-2,20);
		with (buttonobject.btext) {
			text=btext; setTextFormat(boldWhite);
			selectable=false; type='dynamic';
			_x=(bwidth-5-textWidth)/2;
		}
		if (ltext!="") {
			buttonobject.createTextField("explain",2,bwidth+4,-1,300,20);
			buttonobject.explain.autoSize=true;
			writeText(buttonobject.explain,ltext);
			buttonobject.explain.wordWrap=false;
		}

		// Resize if button is bigger than text
		var t=buttonobject.btext.textWidth;
		if (t+6>buttonobject.btext._width) { buttonobject.btext._width=t+4; }
		if (t>bwidth) {
			x-=(t-bwidth)/2; buttonobject.btext._x+=(t-bwidth)/2+2;
			bwidth=t+4;
		}
		with (buttonobject) {
			_x=x; _y=y;
			beginFill(0x7F7F7F,100);
			moveTo(0,0);
			lineTo(bwidth,0); lineTo(bwidth,17);
			lineTo(0,17); lineTo(0,0); endFill();
		}
	}

	// createHitRegion		- write a prompt and draw a hit region around it
	// (object,prompt text,depth for text object,depth for hit region)

	function createHitRegion(obj,prompttext,promptdepth,hitdepth) {
		obj.createTextField('prompt',promptdepth,13,-5,200,19);
		obj.prompt.text=prompttext;
		obj.prompt.setTextFormat(plainSmall);
		obj.prompt.selectable=false;
		tw=obj.prompt._width=obj.prompt.textWidth+5;

		obj.createEmptyMovieClip('hitregion',hitdepth);
		with (obj.hitregion) {
			clear(); beginFill(0,0);
			moveTo(0,0); lineTo(tw+15,0);
			lineTo(tw+15,15); lineTo(0,15);
			endFill();
 		};
	};
