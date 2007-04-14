
OpenLayers.Feature.OSMWay =  OpenLayers.Class.create();

OpenLayers.Feature.OSMWay.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Feature.OSM, {
		type : null,
		segs : null,
		initialize : function(geometry,data,style) {
			OpenLayers.Feature.OSM.prototype.initialize.apply
				(this,arguments);
			segs = new Array();
		},

		setType: function(t) { 
			this.type = t; 
		},

		addSeg: function(sid) {
			this.segs.push(sid);
		},
	
		toXML : function() { 
			var xml = "<way id='" + (this.fid>0 ? this.fid : 0)  +
						"'>";
			for(seg in this.segs) {
				xml += "<seg id='" + this.segs[seg] + "' />";
			}

			xml += this.tagsToXML();
			xml += "</way>";	
			return xml;
		},

		upload: function(url,receiver,callback) {

				//loadURL won't handle PUT
				//OpenLayers.loadURL(url,null,this,this.uploaded);
				var data = 
					"method=PUT&data=<osm version='0.3'>"+this.toXML()+"</osm>";
				var realCB = (receiver) ? callback.bind(receiver) : callback;
				alert('uploading the following XML: to ' + url + ' ' + data);
				ajaxrequest(url,'POST',data,realCB, this);
		},


		CLASS_NAME : "OpenLayers.Feature.OSMWay"
});
			
