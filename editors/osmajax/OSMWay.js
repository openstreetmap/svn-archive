
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
			segs.push(sid);
		},

		CLASS_NAME : "OpenLayers.Feature.OSMWay"
});
			
