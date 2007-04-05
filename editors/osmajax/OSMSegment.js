
OpenLayers.Feature.OSMSegment =  OpenLayers.Class.create();

OpenLayers.Feature.OSMSegment.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Feature.OSM, {
		parent : 0,
		initialize : function(geometry,data,style) {
			OpenLayers.Feature.OSM.prototype.initialize.apply
				(this,arguments);
		},

		setParent: function(id) { 
			this.parent = id;
		},

		setNodes: function(node1,node2) {
			this.geometry.components[0] = node1;	
			this.geometry.components[1] = node2;	
		},

		CLASS_NAME : "OpenLayers.Feature.OSMSegment"
});
			
