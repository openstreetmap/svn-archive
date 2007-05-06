
OpenLayers.Feature.OSM =  OpenLayers.Class.create();

OpenLayers.Feature.OSM.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Feature.Vector, {

		osmitem: null,

		initialize : function(osmi,data,style) {
			var newArguments = new Array();
			newArguments.push(osmi.geometry);
			newArguments.push(data);
			newArguments.push(style);
			OpenLayers.Feature.Vector.prototype.initialize.apply
				(this,newArguments);
			this.osmitem = osmi;
		},

		toXML: function() {
			return this.osmitem.toXML();
		},

		CLASS_NAME : "OpenLayers.Feature.OSM"
});
			
