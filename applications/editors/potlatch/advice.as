
	// Advice window
	
	function setAdvice(severe,str,nobeep) {
		if (!preferences.data.advice && !severe) { return; }
		_root.panel.advice._visible=false;
		_root.advicepos=0;

		_root.panel.advice.createTextField("strtext",2,7,3,400,20);
		_root.panel.advice.strtext.text=str;
		if (severe) { _root.panel.advice.strtext.setTextFormat(boldWhite); }
			   else { _root.panel.advice.strtext.setTextFormat(boldText); }
		var w=_root.panel.advice.strtext.textWidth;

		_root.panel.advice.createEmptyMovieClip("bg",1);
		with (_root.panel.advice.bg) {
			lineStyle(2,0,100,true);
			if (severe) { beginFill(0xFF0000,100); }
				   else { beginFill(0xFFFF00,100); }
			moveTo(10,0);
			lineTo(w+10,0);	 curveTo(w+20,0,w+20,10);
			lineTo(w+20,15); curveTo(w+20,25,w+10,25);
			lineTo(10,25);	 curveTo(0,25,0,15);
			lineTo(0,10);	 curveTo(0,0,10,0);
			endFill();
		}

		_root.panel.advice._x=(Stage.width-w-20)/2;
		_root.panel.advice._y=0;

		_root.panel.advice._visible=true;
		_root.panel.advice._alpha=0;
		_root.panel.advice.onPress=function() { clearAdvice(); };
		clearInterval(_root.advicescroll);
		_root.advicescroll=setInterval(scrollAdvice,85);
        if (!nobeep) {
		    beep.start();
        }
	};
	function scrollAdvice() {
		_root.advicepos+=20;
		_root.panel.advice._alpha=_root.advicepos;
		_root.panel.advice._y=-_root.advicepos/2.5;
		if (_root.panel.advice._y<=-40) { clearInterval(_root.advicescroll); }
	};
	function clearAdvice() {
		removeMovieClip(_root.panel.advice.bg);
		removeMovieClip(_root.panel.advice.strtext);
	};

