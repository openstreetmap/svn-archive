
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
	
		/*
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
		*/

		CLASS_NAME : "OpenLayers.Feature.OSMWay"
});
			
