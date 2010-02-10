
	var tagstyles=new TextField.StyleSheet();
	tagstyles.load("/potlatch/tags.css?d=3");

	// Open the tagfinder dialogue

	function openTagFinder() {
		_root.panel.properties.enableTabs(false);
		_root.windows.attachMovie("modal","tf",++windowdepth);
		_root.windows.tf.init(300, 110, [iText('cancel'), iText('ok')], createTagWindow);

		_root.windows.tf.addHeadline("z1",iText('tags_findatag'));
		_root.windows.tf.addText("z2",iText('tags_typesearchterm'));
		_root.windows.tf.addTextEntry("comment","",20);
		
		_root.tfswallowed=false;
		_root.windows.tf.box.comment.onChanged=function() {
			fixUTF8();
			if (_root.windows.tf.box.comment.text.toUpperCase()=='F' && !_root.tfswallowed ) { _root.windows.tf.box.comment.text=''; }
			_root.tfswallowed=true;
		};
	}
	
	// Create the tag window

	function createTagWindow(button) {
		if (button!=iText('ok') || !_root.windows.tf.box.comment.text) { return false; }
		_root.tagsearch=_root.windows.tf.box.comment.text;

		_root.tagtable=[];
		createHelpWindow(500,350);
		_root.help.createTextField("headline",5,10,10,480,30);
		with (_root.help.headline) { setNewTextFormat(yellowHead); wordWrap=true; selectable=false; type='dynamic'; }
	
		if (tagsearch.indexOf('=')>0) {
			var c=tagsearch.split('=');
			beginMoreDetails(c[0],c[1]);
		} else {
			beginTagTable();
			var tagdoc=new XML();
			tagdoc.load("http://tagstat.hypercube.telascience.org/xmlsearch.php?action=fulltext_fast&key="+tagsearch);
			tagdoc.onLoad=function() { createTagTable(this); writeTagTable(); };	
		}
	}

	// Parse XML and create the table of tags

	function createTagTable(doc) {
		var level1=doc.childNodes;
		for (i=0; i<level1.length; i+=1) {
			if (level1[i].nodeName=='search') {
				var level2=level1[i].childNodes;
				for (j=0; j<level2.length; j+=1) {
					if (level2[j].nodeName=='results') {

						var level3=level2[j].childNodes;
						for (k=0; k<level3.length; k+=1) {
							if (level3[k].nodeName=='result') {

								// Read values into hash
								var o=[];
								var level4=level3[k].childNodes;
								for (l=0; l<level4.length; l+=1) {
									if (level4[l].nodeName) { o[level4[l].nodeName]=level4[l].firstChild.nodeValue; }
								}

								// Add to tag table
								tagtable.push([o['tag'],o['value'],Number(o['total']),o['onway'],o['onnode'],o['onrelation']]);
							}
						}
					}
				}
			}
		}
		tagtable.sortOn(2,18);
	}
	
	// Write the table of tags
	
	function beginTagTable() {
		_root.help.createTextField("tag",10,10,60,180,280); _root.help.tag.setNewTextFormat(plainWhite); _root.help.tag.selectable=false; _root.help.tag.text=iText('loading');
		_root.help.headline.text=iText('tags_matching',_root.tagsearch);
	}

	function writeTagTable() {
		removeMovieClip(_root.help.returnlink);
		_root.help.createTextField("legend1",20,200,36,180,280); _root.help.legend1.setNewTextFormat(plainLight); _root.help.legend1.selectable=false; _root.help.legend1.text="Total";
		_root.help.createTextField("legend2",21,260,36,180,280); _root.help.legend2.setNewTextFormat(plainLight); _root.help.legend2.selectable=false; _root.help.legend2.text="Ways";
		_root.help.createTextField("legend3",22,320,36,180,280); _root.help.legend3.setNewTextFormat(plainLight); _root.help.legend3.selectable=false; _root.help.legend3.text="Nodes";
		_root.help.createTextField("legend4",23,380,36,180,280); _root.help.legend4.setNewTextFormat(plainLight); _root.help.legend4.selectable=false; _root.help.legend4.text="Relations";

		var y=60;
		_root.help.createTextField("tag"   ,10,10 ,y,180,280); _root.help.tag.setNewTextFormat(plainWhite); _root.help.tag.selectable=false;
		_root.help.createTextField("total1",11,200,y,59 ,280) ; _root.help.total1.setNewTextFormat(plainWhite); _root.help.total1.selectable=false;
		_root.help.createTextField("total2",12,260,y,59 ,280) ; _root.help.total2.setNewTextFormat(plainWhite); _root.help.total2.selectable=false;
		_root.help.createTextField("total3",13,320,y,59 ,280) ; _root.help.total3.setNewTextFormat(plainWhite); _root.help.total3.selectable=false;
		_root.help.createTextField("total4",14,380,y,59 ,280) ; _root.help.total4.setNewTextFormat(plainWhite); _root.help.total4.selectable=false;
		_root.help.createTextField("more"  ,15,440,y,40 ,280) ; _root.help.more.setNewTextFormat(moreText); _root.help.more.selectable=false;

		for (i=0; i<tagtable.length; i++) {
			_root.help.tag.text   +=tagtable[i][0]+" = "+tagtable[i][1]+"\n";
			_root.help.total1.text+=tagtable[i][2]+"\n";
			_root.help.total2.text+=tagtable[i][3]+"\n";
			_root.help.total3.text+=tagtable[i][4]+"\n";
			_root.help.total4.text+=tagtable[i][5]+"\n";
 			_root.help.more.text  +=iText('more')+"\n";
			if (i==0) { _root.rowheight=_root.help.tag.textHeight; } // how tall is each row?
		}

		if (_root.help.tag.scroll<_root.help.tag.maxscroll) {
			_root.help.attachMovie("vertical","scrollbar",6);
			_root.help.scrollbar._x=480; _root.help.scrollbar._y=y;
			_root.help.scrollbar.init(280,_root.help.tag.maxscroll,30,
				function(n) { _root.help.tag.scroll=
							  _root.help.total1.scroll=
							  _root.help.total2.scroll=
							  _root.help.total3.scroll=
							  _root.help.total4.scroll=
							  _root.help.more.scroll=Math.ceil(n); }
			);
		}

		_root.help.bg.onPress=function() {
			var chosen=Math.floor((_root.help.tag._ymouse-4)/_root.rowheight)+_root.help.tag.scroll-1;
			if (_root.help.tag._xmouse>0 && _root.help.tag._xmouse<180) {
				_root.panel.properties.setTag(tagtable[chosen][0], tagtable[chosen][1]);
				_root.createEmptyMovieClip("help",0xFFFFFD); _root.createEmptyMovieClip("blank",0xFFFFFC); 
			} else if (_root.help.more._xmouse>0 && _root.help.more._xmouse<50) {
				beginMoreDetails(tagtable[chosen][0], tagtable[chosen][1]);
			}
		};
	}

	// Request 'more' from the server
	
	function beginMoreDetails(key,value) {
		removeMovieClip(_root.help.total1);	removeMovieClip(_root.help.legend1);
		removeMovieClip(_root.help.total2);	removeMovieClip(_root.help.legend2);
		removeMovieClip(_root.help.total3);	removeMovieClip(_root.help.legend3);
		removeMovieClip(_root.help.total4);	removeMovieClip(_root.help.legend4);
		removeMovieClip(_root.help.total5); removeMovieClip(_root.help.more);
		removeMovieClip(_root.help.scrollbar); removeMovieClip(_root.help.tag);

		_root.help.headline.text=iText('tags_descriptions',key+"="+value);
		_root.help.createTextField("tag",10,10,40,465,270);
		_root.help.tag.setNewTextFormat(plainWhite); _root.help.tag.selectable=false;
		_root.help.tag.text=iText('loading');

		if (tagsearch.indexOf('=')<1) {
			_root.help.createTextField("returnlink",11,10,320,465,20);
			_root.help.returnlink.setNewTextFormat(moreText); _root.help.returnlink.selectable=false;
			_root.help.returnlink.text=iText('tags_backtolist');
			_root.help.bg.onPress=function() {
				if (_root.help.bg._ymouse>=320 ) { beginTagTable(); writeTagTable(); }
			};
		}

		var lv=new LoadVars();
		lv.load("http://richard.dev.openstreetmap.org/cgi-bin/description.cgi?key="+key+"&value="+value);
		lv.onData=writeMoreDetails;
	}

	function writeMoreDetails(data) {
		with (_root.help.tag) {
			styleSheet=tagstyles;
			html=true;
			multiline=true;
			wordWrap=true;
			htmlText=data;
		}

		if (_root.help.tag.scroll<_root.help.tag.maxscroll) {
			_root.help.attachMovie("vertical","scrollbar",6);
			_root.help.scrollbar._x=480; _root.help.scrollbar._y=40;
			_root.help.scrollbar.init(270,_root.help.tag.maxscroll,30,
				function(n) { _root.help.tag.scroll=Math.ceil(n); }
			);
		}
	}
