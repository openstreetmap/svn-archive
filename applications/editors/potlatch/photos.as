
	var photocss=new TextField.StyleSheet();
	photocss.load("/potlatch/photos.css?d=1");

	function Photo() {
	};

	Photo.prototype.init=function(lat,lon,thumb,desc,name) {
		this._x=long2coord(lon);
		this._y=lat2coord(lat); 
		this._xscale=this._yscale=Math.max(100/Math.pow(2,_root.scale-13),6.25);
		this.thumb=thumb; this.desc=desc; this.name=name;
	};
	
	Photo.prototype.onPress=function() {
		_root.createEmptyMovieClip("popup",19);
		pos=new Object(); pos.x=this._x; pos.y=this._y;
		_root.map.localToGlobal(pos);
		_root.popup._x=pos.x+10; if (pos.x>Stage.width-200) { _root.popup._x=pos.x-200; }
		_root.popup._y=Math.max(20,pos.y);

		_root.popup.createTextField('desc',1,5,5,190,290); 
		var p=this;
		with (_root.popup.desc) {
			multiline=true; wordWrap=true; selectable=true; type='dynamic';
			autoSize='left';
			styleSheet=_root.photocss;
			html=true;
			htmlText=p.desc;
			htmlText=htmlText.split('TARGET=""').join('');
			htmlText=htmlText.split('HREF="').join('href="');
			htmlText=htmlText.split('href="').join('target="_blank" href="');
		}
		var w=_root.popup.desc._width+10;
		var h=Math.max(150,_root.popup.desc._height+10);

		_root.popup.createEmptyMovieClip("drag",2);
		_root.popup.createEmptyMovieClip("resize",3);
		with (_root.popup.resize) {
			beginFill(0xFFFFFF,50); moveTo(0,0); lineTo(-10,0);
			lineTo(-10,-10); lineTo(0,-10); lineTo(0,0); endFill();
			lineStyle(1,0xFFFFFF);
			moveTo(-9,-2); lineTo(-2,-9);
			moveTo(-6,-2); lineTo(-2,-6);
			moveTo(-3,-2); lineTo(-2,-3);
		}

		_root.popup.createTextField('name',4,20,-18,w-20,19);
		_root.popup.name.text=this.name;
		_root.popup.name.setTextFormat(plainWhite);
		_root.popup.name.selectable=false;

		redrawPopup(w,h);
		_root.popup.drag.onPress=function() { _root.popup.startDrag(); };
		_root.popup.drag.onRelease=function() { _root.popup.stopDrag(); };

		_root.popup.attachMovie("closecross","closex",5);
		_root.popup.closex._x=10;
		_root.popup.closex._y=-9;
		_root.popup.closex.onPress=function() {
			removeMovieClip(_root.popup); 
		};
		
		_root.popup.resize.onPress=function() { _root.popup.resize.onMouseMove=resizePopup; };
		_root.popup.resize.onMouseUp=function() { _root.popup.resize.onMouseMove=null; };
	};
	
	Object.registerClass("photo",Photo);



	// ================================================================
	// Support functions
	
	// popup redraw and resize
	
	function resizePopup() {
		var w=_root._xmouse-_root.popup._x;
		var h=_root._ymouse-_root.popup._y;
		redrawPopup(w,h);
	}
	
	function redrawPopup(w,h) {
		if (w<50 || h<50) { return; }
		with (_root.popup) {
			clear();
			beginFill(0,80); moveTo(0,0); lineTo(w,0);
			lineTo(w,h); lineTo(0,h); lineTo(0,0); endFill();
		}
		with (_root.popup.drag) {
			clear();
			beginFill(0,100); moveTo(0,0); lineTo(w,0);
			lineTo(w,-17); lineTo(0,-17); lineTo(0,0); endFill();
		}
		with (_root.popup.resize) { _x=w; _y=h; }
		with (_root.popup.desc) { _width=w-10; _height=h-10; }
		_root.popup.name._width=w-20;
	}

	// loadPhotos

	function loadPhotos() {
		var kmldoc=new XML();
		kmldoc.load(preferences.data.photokml+"?bbox="+_root.edge_l+","+_root.edge_b+","+_root.edge_r+","+_root.edge_t);
		kmldoc.onLoad=function() {
			// find level2 - <kml>
			var level1=this.childNodes;
			for (i=0; i<level1.length; i+=1) {
				if (level1[i].nodeName=='kml') {

					// find level3 - <Document>
					var level2=level1[i].childNodes;
					for (j=0; j<level2.length; j+=1) {
						if (level2[j].nodeName=='Document') {

							// find level4 - <Placemark>
							var level3=level2[j].childNodes;
							for (k=0; k<level3.length; k+=1) {
								if (level3[k].nodeName=='Placemark') {

									// individual elements of Placemark
									var p_name,p_icon,p_desc,p_lat,p_lon;
									var level4=level3[k].childNodes;
									for (l=0; l<level4.length; l+=1) {
										switch (level4[l].nodeName) {
											case 'description':	p_desc=level4[l].childNodes[1].nodeValue; break;		// <description><![CDATA [...html...]]></description>
											case 'name':		p_name=level4[l].firstChild.nodeValue; break;		// <name>icon5.jpg</name>
											case 'Icon':		p_icon=getElement(level4[l],'href'); break;		// <Icon><href>http://a.com/b.jpg</href></Icon>
											case 'Point':		var p =getElement(level4[l],'coordinates');		// <Point><coordinates>1.5,51.2</coordinates></Point>
																var c =p.split(','); p_lon=c[0]; p_lat=c[1]; break;
										}
									}

									// place photo
									if (p_icon && !_root.map.photos[innocent(p_icon)]) {
										var n=innocent(p_icon);
										_root.map.photos.attachMovie("photo",n,++photodepth);
										_root.map.photos[n].init(p_lat,p_lon,p_icon,p_desc,p_name);
									}
								}
							}
						}
					}
				}
			}
		};
	}

	function getElement(xmlobj,el) {
		var a=xmlobj.childNodes; var r=null;
		for (var i=0; i<a.length; i++) {
			if (a[i].nodeName==el) { r=a[i].firstChild.nodeValue; }
		}
		return r;
	}

	// remove non-alphanumerics
	function innocent(a) {
		var b='';
		for (var i=0; i<a.length; i++) {
			var c=a.substr(i,1); if ((c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9') || c=='_') { b+=c; }
		}
		return b;
	}
