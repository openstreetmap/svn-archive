// OSMNode class 
// Represents an OSM node

OpenLayers.OSMNode = OpenLayers.Class.create();

OpenLayers.OSMNode.prototype = 
	OpenLayers.Class.inherit (OpenLayers.GeometriedOSMItem, {
		
		way: 0,

		initialize: function() {
			OpenLayers.GeometriedOSMItem.prototype.initialize.apply
					(this,arguments);
			//OpenLayers.OSMItem.prototype.initialize.apply(this);
		},

		setGeometry: function(g) {
			if (g instanceof OpenLayers.Geometry.Point) {
				geometry=g;
			}
		},

		toXML : function() { 
			var cvtr=new converter("Mercator");
			var a = new OpenLayers.LonLat(this.geometry.x,
							this.geometry.y);
			var b=cvtr.customToNorm(a);
			var xml = "<node id='" + (this.osmid>0 ? this.osmid : 0)  +
						"' lat='" + b.lat + "' lon='" + 
						b.lon + "'>";

			xml += this.tagsToXML();
			xml += "</node>";	
			return xml;
		},

		// Is this node a point of interest?
		// If it contains tags other than 'created_by', it is considered so.
		isPOI:  function() {
			if(this.tags) {
				for(k in this.tags) {
					if (k != 'created_by') {
						return true;
					}
				}
			}
			return false;
		},

		CLASS_NAME : "OpenLayers.OSMNode"
});
			
