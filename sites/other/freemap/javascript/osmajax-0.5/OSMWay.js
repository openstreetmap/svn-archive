
OpenLayers.OSMWay =  OpenLayers.Class.create();

OpenLayers.OSMWay.prototype = 
	OpenLayers.Class.inherit (OpenLayers.GeometriedOSMItem, {
		nds : null,

		initialize: function() {
			OpenLayers.GeometriedOSMItem.prototype.initialize.apply
					(this,arguments);
			//OpenLayers.OSMItem.prototype.initialize.apply(this);
		},

		setGeometry: function(g) {
			geometry=g;
		},

		addNd: function(ndid) {
			this.nds.push(ndid);
		},
			
		toXML : function() { 
			var xml = "<way id='" + (this.osmid>0 ? this.osmid : 0)  +
						"'>";
			for(nd in this.nds) {
				xml += "<nd ref='" + this.nds[nd] + "' />";
			}

			xml += this.tagsToXML();
			xml += "</way>";	
			return xml;
		},

		findNdPos: function(ndid) {
			for(var count=0; count<this.nds.length; count++) {
				if (this.nds[count]=ndid) {
					return count;
				}
			}
			return -1;
		},

		CLASS_NAME : "OpenLayers.OSMWay"
});
			
