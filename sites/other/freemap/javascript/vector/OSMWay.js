
OpenLayers.OSMWay =  OpenLayers.Class.create();

OpenLayers.OSMWay.prototype = 
	OpenLayers.Class.inherit (OpenLayers.GeometriedOSMItem, {
		type : null,
		segs : null,

		initialize: function() {
			OpenLayers.GeometriedOSMItem.prototype.initialize.apply
					(this,arguments);
			//OpenLayers.OSMItem.prototype.initialize.apply(this);
		},

		setGeometry: function(g) {
			geometry=g;
		},

		setType: function(t) { 
			this.type = t; 
		},

		addSeg: function(sid) {
			this.segs.push(sid);
		},
	
		toXML : function() { 
			var xml = "<way id='" + (this.osmid>0 ? this.osmid : 0)  +
						"'>";
			for(seg in this.segs) {
				xml += "<seg id='" + this.segs[seg] + "' />";
			}

			xml += this.tagsToXML();
			xml += "</way>";	
			return xml;
		},

		CLASS_NAME : "OpenLayers.OSMWay"
});
			
