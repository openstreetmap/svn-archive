
OpenLayers.Feature.OSMMarker =  OpenLayers.Class.create();

OpenLayers.Feature.OSMMarker.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Feature, {

		osmitem: null,

		initialize : function(osmi,layer,data) {
			var newArguments = new Array();
			var lonlat = new OpenLayers.LonLat(osmi.geometry.x,
												osmi.geometry.y);
			newArguments.push(layer);
			newArguments.push(lonlat);
			newArguments.push(data);
			OpenLayers.Feature.prototype.initialize.apply
				(this,newArguments);
			this.osmitem = osmi;
		},

		toXML: function() {
			return this.osmitem.toXML();
		},

		CLASS_NAME : "OpenLayers.Feature.OSMMarker"
});
			
