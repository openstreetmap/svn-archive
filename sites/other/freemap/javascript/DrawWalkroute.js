OpenLayers.Control.DrawWalkroute = OpenLayers.Class.create();

OpenLayers.Control.DrawWalkroute.prototype =
	OpenLayers.Class.inherit(OpenLayers.Control.DrawFeature, {

	callback: null,

	initialize: function(layer,handler,options) {
		OpenLayers.Control.DrawFeature.prototype.initialize.apply
				(this,arguments);
	},

	setCallback: function(cb)
	{
		this.callback=cb;
	},

	drawFeature : function(geometry) {

		if(geometry instanceof OpenLayers.Geometry.LineString)
		{
			// This gives us an array of points (x,y)
			var realComponents = geometry.components;
			this.callback(realComponents);
		}
	},

	CLASS_NAME: "OpenLayers.Control.DrawWalkroute"
});
