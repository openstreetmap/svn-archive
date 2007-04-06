// OSMNode class 
// Represents an OSM node
// 
// It's a bit hacky putting IDs and tags in a "Geometry", but it's to deal
// with the fact that we don't want to create Features out of nodes which are 
// just points within ways, rather than proper features in their own right.

OpenLayers.Geometry.OSMNode =  OpenLayers.Class.create();

OpenLayers.Geometry.OSMNode.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Geometry.Point, {
		id: null,
		tags: null,

		initialize : function(x,y) {
			OpenLayers.Geometry.OSM.prototype.initialize.apply
				(this,arguments);
			this.tags=new Array();
		},

		// TODO - does the JavaScript prototype library do multiple 
		// inheritance? Good to put all the OSM stuff in a class of its own.

		toXML : function() { 
			var xml = "<node id='" + (this.id>0 ? this.id : 0)  +
						"'>";

			for ( k in this.tags) {
				xml += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
			}
			xml += "</node>";	
			return xml;
		},

		addTag: function(k,v) {
			this.tags[k] = v;
		},

		setID : function(id) {
			this.id=id;
		},

		// Is this node a point of interest?
		// If it contains tags other than 'created_by', it is considered so.
		isPOI: function() {
			for(k in this.tags) {
				if (k != 'created_by') {
					return true;
				}
			}
			return false;
		},

		CLASS_NAME : "OpenLayers.Geometry.OSMNode"
});
			
