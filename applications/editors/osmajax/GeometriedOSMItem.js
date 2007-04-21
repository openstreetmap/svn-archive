// GeometriedOSMItem class 

OpenLayers.GeometriedOSMItem = OpenLayers.Class.create();

OpenLayers.GeometriedOSMItem.prototype = 
	OpenLayers.Class.inherit (OpenLayers.OSMItem, {

		geometry:null,

		initialize: function() {
			OpenLayers.OSMItem.prototype.initialize.apply(this,arguments);
		},

		CLASS_NAME : "OpenLayers.GeometriedOSMItem"
});
			
