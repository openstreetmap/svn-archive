
	function Photo() {
	};

	Photo.prototype.init=function(lat,lon,thumb,desc,name) {
		this._x=long2coord(lon);
		this._y=lat2coord(lat); 
		this._xscale=this._yscale=Math.max(100/Math.pow(2,_root.scale-_root.flashscale),2);
		this.thumb=thumb; this.desc=desc; this.name=name;
	};
	
	Photo.prototype.onPress=function() {
		var ppos=new Object(); ppos.x=this._x; ppos.y=this._y;
		_root.map.localToGlobal(ppos);
		palettedepth++; var pn="p"+palettedepth;
		_root.palettes.attachMovie("palette",pn,palettedepth);
		_root.palettes[pn].initHTML(ppos.x, ppos.y, this.name, this.desc);
	};
	
	Object.registerClass("photo",Photo);



	// ================================================================
	// Support functions
	

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
