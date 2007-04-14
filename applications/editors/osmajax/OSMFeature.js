
OpenLayers.Feature.OSM =  OpenLayers.Class.create();

OpenLayers.Feature.OSM.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Feature.Vector, {
		tags : null,

		initialize : function(geometry,data,style) {
			OpenLayers.Feature.Vector.prototype.initialize.apply
				(this,arguments);
			this.tags = new Array();
		},

		addTag: function (k,v) {
			this.tags[k] = v;
		},

		tagsToXML: function() {
			var str = "";
			for ( k in this.tags) {
				if (this.tags[k]!==null) {
					str += "<tag k='" + k + "' v='" + this.tags[k] + "' />";
				}
			}
			return str;
		},

		CLASS_NAME : "OpenLayers.Feature.OSM"
});
			
