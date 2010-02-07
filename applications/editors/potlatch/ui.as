
	// =====================================================================================
	// Standard UI
	// =====================================================================================

	// Vertical scrollbar
	
	VerticalScrollBar=function() { };
	VerticalScrollBar.prototype=new MovieClip();
	VerticalScrollBar.prototype.init=function(h,max,barheight,dragfunc) {
		this.max=max;
		this.height=h;
		this.barheight=barheight;
		this.dragsize=(h-20-barheight);
		this.doOnDrag=dragfunc;

		this.attachMovie("scroll_up"  ,"up"  ,1); this.up._y=0;
		this.up.onPress=startScrollBarUp;
		this.up.onRelease=stopScrollBarMove;

		this.attachMovie("scroll_down","down",2); this.down._y=h-10;
		this.down.onPress=startScrollBarDown;
		this.down.onRelease=stopScrollBarMove;
	
		this.lineStyle(1,0xCCCCCC,100);
		this.moveTo(0,10); this.lineTo(0 ,this.height-10);
		this.lineTo(10,this.height-10); this.lineTo(10,10);
		this.lineTo(0,10);

		this.createEmptyMovieClip("bar",3);
		this.bar.onPress=pressScrollBar;
		this.bar.onRelease=releaseScrollBar;
		this.bar._y=10;
		with (this.bar) {
			clear();
			beginFill(0xBBBBBB,100);
			moveTo(0,0); lineTo(10,0);
			lineTo(10,this.barheight); lineTo(0,this.barheight);
			lineTo(0,0); endFill();
		};
	};

	VerticalScrollBar.prototype.moveto=function(pos) {
		this.bar._x=0;
		this.bar._y=pos/this.max*this.dragsize+10;
	};

	function startScrollBarUp()   { clearInterval(this._parent.mover); this._parent.mover=setInterval(doScrollBarMove,10,this._parent,-2); }
	function startScrollBarDown() { clearInterval(this._parent.mover); this._parent.mover=setInterval(doScrollBarMove,10,this._parent, 2); }
	function stopScrollBarMove()  { clearInterval(this._parent.mover); }
	function doScrollBarMove(sb,inc) {
		sb.bar._y=Math.min(Math.max(sb.bar._y+inc,10),sb.dragsize+10);
		sb.doOnDrag.call(sb, ((sb.bar._y-10)/sb.dragsize)*sb.max);
	}

	function pressScrollBar() {
		this.startDrag(false,0,10,0,this._parent.dragsize+10);
		this.onMouseMove=function() {
			this._parent.doOnDrag.call(this._parent, ((this._y-10)/this._parent.dragsize)*this._parent.max);
		};
	};

	function releaseScrollBar() {
		delete this.onMouseMove;
		this.stopDrag();
	};
	Object.registerClass("vertical",VerticalScrollBar);

	// Floating palette

	var palettecss=new TextField.StyleSheet();
	palettecss.load("/potlatch/photos.css?d=1");
	
	UIPalette=function() {};
	UIPalette.prototype=new MovieClip();

	UIPalette.prototype.init=function(x,y,w,h,n) {
		this.w=w;
		this.h=h;
		this.setPosition(x,y);
		this.createControls();
		this.redraw(n);
	};

	UIPalette.prototype.initHTML=function(x,y,n,tx) {
		this.createTextField('desc',1,5,5,190,290); 
		with (this.desc) {
			multiline=true; wordWrap=true; selectable=true; type='dynamic';
			autoSize='left';
			styleSheet=_root.palettecss;
			html=true;
			htmlText=tx;
			htmlText=htmlText.split('TARGET=""').join('');
			htmlText=htmlText.split('HREF="').join('href="');
			htmlText=htmlText.split('href="').join('target="_blank" href="');
		}
		this.w=this.desc._width+10;
		this.h=Math.max(150,this.desc._height+10);
		this.setPosition(x,y);
		this.createControls(n);
		this.redraw();
	};
	
	UIPalette.prototype.setPosition=function(x,y) {
		if (x>Stage.width-this.w) { x=x-this.w-20; }
		this._x=x; 
		this._y=Math.max(20,y);
	};
	
	UIPalette.prototype.createControls=function(n) {

		this.createEmptyMovieClip("drag",2);
		
		this.createEmptyMovieClip("resizeHandle",3);
		with (this.resizeHandle) {
			beginFill(0xFFFFFF,50); moveTo(0,0); lineTo(-10,0);
			lineTo(-10,-10); lineTo(0,-10); lineTo(0,0); endFill();
			lineStyle(1,0xFFFFFF);
			moveTo(-9,-2); lineTo(-2,-9);
			moveTo(-6,-2); lineTo(-2,-6);
			moveTo(-3,-2); lineTo(-2,-3);
		}

		this.createTextField('titleText',4,20,-18,w-20,19);
		this.titleText.text=n;
		this.titleText.setTextFormat(plainWhite);
		this.titleText.selectable=false;

		this.drag.onPress=function() { this._parent.startDrag(); };
		this.drag.onRelease=function() { this._parent.stopDrag(); };

		this.attachMovie("closecross","closex",5);
		this.closex._x=10;
		this.closex._y=-9;
		this.closex.onPress=function() { removeMovieClip(this._parent); };

		this.resizeHandle.onPress=function() { this.onMouseMove=function() { this._parent.resize(); }; };
		this.resizeHandle.onMouseUp=function() { this.onMouseMove=null; };
	};

	UIPalette.prototype.resize=function() {
		var w=_root._xmouse-this._x;
		var h=_root._ymouse-this._y;
		if (w<50 || h<50) { return; }
		this.w=w; this.h=h; this.redraw();
	};

	UIPalette.prototype.redraw=function() {
		with (this) {
			clear();
			beginFill(0,80); moveTo(0,0); lineTo(this.w,0);
			lineTo(this.w,this.h); lineTo(0,h); lineTo(0,0); endFill();
		}
		with (this.drag) {
			clear();
			beginFill(0,100); moveTo(0,0); lineTo(this.w,0);
			lineTo(this.w,-17); lineTo(0,-17); lineTo(0,0); endFill();
		}
		with (this.resizeHandle) { _x=this.w; _y=this.h; }
		with (this.desc) { _width=this.w-10; _height=this.h-10; }
		this.titleText._width=this.w-20;
	};

	Object.registerClass("palette",UIPalette);


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
	// UICheckbox.init(x,y,text,state,changefunction,enabled?)

	UICheckbox=function() {
	};
	UICheckbox.prototype=new MovieClip();
	UICheckbox.prototype.init=function(x,y,prompttext,state,changefunction,enabled) {
		if (enabled==undefined) { enabled=true; }
		this._x=x;
		this._y=y;
		this.enabled=enabled;
		this.state=state;
		this.doOnChange=changefunction;
		createHitRegion(this,prompttext,0,1,enabled);
		if (enabled) {
			this.hitregion.onPress=function() {
				this._parent.state=!this._parent.state;
				this._parent.draw();
				this._parent.doOnChange(this._parent.state);
			};
		}
		this.createEmptyMovieClip('box',2);
		this.draw();
	};
	UICheckbox.prototype.draw=function() {
		with (this.box) {
			clear();
			if (this.enabled) { lineStyle(2,0,100); }
						 else { lineStyle(2,0,30); }
			moveTo(1,0);
			lineTo(9,0); lineTo(9,9);
			lineTo(0,9); lineTo(0,0);
			if (this.state) {
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
	UIMenu.prototype.init=function(x,y,selected,options,tooltip,closefunction,closethis,menuwidth,closedtitle) {
		var i,w,h;
		this._x=x; this._y=y;
		this.selected=selected; this.original=selected;
		this.options=options;
		this.closedtitle=closedtitle ? closedtitle : '';
		this.enable=[];

		// create (invisible) movieclip for opened menu
		this.createEmptyMovieClip("opened",2);
		this.opened._visible=false;
		// create child for each option
		var tw=0;
		for (i=0; i<options.length; i+=1) {
			if (options[i]!='--') {
				this.opened.createTextField(i,i+1,3,i*16-1,100,19);
				this.opened[i].background=true;
				this.opened[i].backgroundColor=0x888888;
				this.opened[i].text=options[i];
				this.opened[i].setTextFormat(menu_off);
				this.enable[i]=true;
				if (this.opened[i].textWidth*1.05>tw) { tw=this.opened[i].textWidth*1.05; }
			}
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
		// adjust all menus to have correct highlight, and draw dividers
		this.opened.box.lineStyle(1,0xFFFFFF,50);
		for (i=0; i<options.length; i+=1) {
			if (options[i]=='--') {
				this.opened.box.moveTo(5,i*16+10);
				this.opened.box.lineTo(this.itemwidth,i*16+10);
			} else {
				this.opened[i]._width=this.itemwidth;
			}
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
		this.closed.current.text=(this.closedtitle=='') ? options[selected] : this.closedtitle;
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
			if (this.selected>-1) {
				this.opened[this.selected].backgroundColor=0xDDDDDD;
				this.opened[this.selected].setTextFormat(menu_on);
			}
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
			this.closed.current.text=(this.closedtitle=='') ? this.options[this.selected] : this.closedtitle;
			this.closed.current.setTextFormat(menu_off);
			this.closed._alpha=100;
			this.opened._visible=false;
			mflash=this; flashcount=2;
			mflashid=setInterval(function() { mflash.menuFlash(); }, 40);
			this.doOnClose.call(this.closeThis,this.selected);
		} else {
			this.closed.current.text=(this.closedtitle=='') ? this.options[this.original] : this.closedtitle;
			this.closed.current.setTextFormat(menu_off);
			this.closed._alpha=100;
			this.opened._visible=false;
		}
	};
	UIMenu.prototype.setValue=function(n) {
		this.opened[this.selected].backgroundColor=0x888888;
		this.opened[this.selected].setTextFormat(menu_off);
		this.selected=n; this.original=n;
		this.closed.current.text=(this.closedtitle=='') ? this.options[this.selected] : this.closedtitle;
		this.closed.current.setTextFormat(menu_off);
	};
	UIMenu.prototype.whichSelection=function() {
		mpos=new Object();
		mpos.x=_root._xmouse;
		mpos.y=_root._ymouse;
		this.opened.globalToLocal(mpos);
		if (mpos.x>0 && mpos.x<this.itemwidth && mpos.y>0 && mpos.y<this.options.length*16) {
			var i=Math.floor((mpos.y)/16);
			if (this.opened[i] && this.enable[i]) { return i; }
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
	UIMenu.prototype.enableOption=function(i) {
		this.enable[i]=true;
		this.opened[i].setTextFormat(menu_off);
	};
	UIMenu.prototype.disableOption=function(i) {
		this.enable[i]=false;
		this.opened[i].setTextFormat(menu_dis);
	};
	UIMenu.prototype.renameOption=function(i,t) {
		this.opened[i].text=t;
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
		this.ypos=7;
		this.depthpos=5;
		
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

	ModalDialogue.prototype.addHeadline=function(objname,t) { this.addTextItem(boldText  ,20,objname,t); };
	ModalDialogue.prototype.addText    =function(objname,t) { this.addTextItem(plainSmall,18,objname,t); };
	ModalDialogue.prototype.addTextItem=function(textformat,textheight,objname,t) {
		this.box.createTextField(objname,this.depthpos++,7,this.ypos,this.modalwidth-14,textheight);
		this.box[objname].text = t;
		with (this.box[objname]) { wordWrap=true; setTextFormat(textformat); selectable=false; type='dynamic'; }
		adjustTextField(this.box[objname]);
		this.ypos+=textheight;
	};

	ModalDialogue.prototype.addTextEntry=function(objname,t,fieldheight) {
		this.box.createTextField(objname,this.depthpos++,10,this.ypos+5,this.modalwidth-20,fieldheight);
		with (this.box[objname]) {
			setNewTextFormat(plainSmall);
			type='input';
			backgroundColor=0xDDDDDD;
			background=true;
			border=true;
			borderColor=0xFFFFFF;
			wordWrap=true;
			text=t;
		}
		Selection.setFocus(this.box[objname]);
		this.ypos+=textheight+10;
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
	// (object,prompt text,depth for text object,depth for hit region,enabled?)

	function createHitRegion(obj,prompttext,promptdepth,hitdepth,enabled) {
		if (enabled==undefined) { enabled=true; }
		obj.createTextField('prompt',promptdepth,13,-5,200,19);
		if (enabled) { obj.prompt.setNewTextFormat(plainSmall); }
				else { obj.prompt.setNewTextFormat(greySmall);  }
		obj.prompt.text=prompttext;
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
