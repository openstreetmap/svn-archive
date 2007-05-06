
OpenLayers.OSMWay =  OpenLayers.Class.create();

OpenLayers.OSMWay.prototype = 
	OpenLayers.Class.inherit (OpenLayers.GeometriedOSMItem, {
		segs : null,

		initialize: function() {
			OpenLayers.GeometriedOSMItem.prototype.initialize.apply
					(this,arguments);
			//OpenLayers.OSMItem.prototype.initialize.apply(this);
		},

		setGeometry: function(g) {
			geometry=g;
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

		findSegmentPos: function(sid) {
			for(var count=0; count<this.segs.length; count++) {
				if (this.segs[count]==sid) {
					return count;
				}
			}
			return -1;
		},

		CLASS_NAME : "OpenLayers.OSMWay"
});
			
