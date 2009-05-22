
	// Potlatch help functions

	var topicselected=0;
	var topics=new Array(iText('Introduction','heading_introduction'),
	                     iText('Surveying','heading_surveying'),
	                     iText('Drawing','heading_drawing'),
	                     iText('Tagging','heading_tagging'),
	                     iText('Troubleshooting','heading_troubleshooting'),
	                     iText('Quick reference','heading_quickref'));

	var styles=new TextField.StyleSheet();
	styles.load("/potlatch/help.css?d=5");
	
	function openHelp() {
		var tw,aw,i;

		// Blank rest of page
		var bh=Stage.height;
		_root.createEmptyMovieClip("blank",0xFFFFFC);
		with (_root.blank) {
			clear();
			beginFill(0xFFFFFF,20); moveTo(0,0); lineTo(Stage.width,0);
			lineTo(Stage.width,bh); lineTo(0,bh); lineTo(0,0); endFill();
		}
		_root.blank.onPress=function() { _root.createEmptyMovieClip("help",0xFFFFFD); _root.createEmptyMovieClip("blank",0xFFFFFC); };
		_root.blank.useHandCursor=false;

		// Background
		_root.help.createEmptyMovieClip("bg",2);
		var w=700; var h=450;
		_root.help._x=(Stage.width-w)/2;
		_root.help._y=(Stage.height-panelheight-h)/2;
		with (_root.help.bg) {
			beginFill(0,90);
			moveTo(0,0); lineTo(w,0);
			lineTo(w,h); lineTo(0,h);
			lineTo(0,0); endFill();
		}
        _root.help.bg.onPress=null;
		_root.help.bg.useHandCursor=false;

		// Topics
		_root.help.createEmptyMovieClip("topics",3);
		aw=0;
		for (i=0; i<topics.length; i++) {
			_root.help.topics.createEmptyMovieClip(i,i);
			_root.help.topics[i].createTextField("t",2,0,0,200,20);
			_root.help.topics[i].t.text=topics[i];
			_root.help.topics[i].t.selectable=true;
			if (i==topicselected) { _root.help.topics[i].t.setTextFormat(boldYellow); }
							 else { _root.help.topics[i].t.setTextFormat(boldWhite); }
			tw=_root.help.topics[i].t.textWidth;
			_root.help.topics[i].createEmptyMovieClip("b",3);
			with (_root.help.topics[i].b) {
				beginFill(0,0);
				moveTo(-10,-5); lineTo(tw+15,-5);
				lineTo(tw+15,25); lineTo(-10,25);
				lineTo(-10,-5); endFill();
			}
			_root.help.topics[i].b.onPress=function() { doHelp(this._parent._name); };
			aw+=tw;
		}
		var origin=10;
		var surplus=(w-aw-15-origin)/(topics.length-1);
		aw=0;
		for (i=0; i<topics.length; i++) {
			_root.help.topics[i]._x=origin;
			_root.help.topics[i]._y=10;
			origin+=_root.help.topics[i].t.textWidth+surplus;
		}
		
		_root.help.createEmptyMovieClip("body",4);
		for (i=0; i<=2; i++) {
			_root.help.body.createTextField(i,i,i*230+10,50,210,h-70);
			with (_root.help.body[i]) { styleSheet=styles; html=true; multiline=true; wordWrap=true; }
		}

		doHelp(topicselected);
	}

	function doHelp(page) {
		_root.help.topics[topicselected].t.setTextFormat(boldWhite); topicselected=page;
		_root.help.topics[topicselected].t.setTextFormat(boldYellow);
        
        for (i=0; i<=2; i++) {
            if (_root.helppages[topicselected][i]) {
                _root.help.body[i].htmlText=_root.helppages[topicselected][i];
            } else {
                _root.help.body[i].htmlText="";
            }
        }
	};
