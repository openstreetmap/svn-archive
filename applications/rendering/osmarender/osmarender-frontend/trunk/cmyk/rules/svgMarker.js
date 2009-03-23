dojo.provide("cmyk.rules.svgMarker");

dojo.require("cmyk.rules._svgFeature");

/**
	@lends cmyk.rules.svgMarker
*/

dojo.declare("cmyk.rules.svgMarker",cmyk.rules._svgFeature,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Marker
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgFeature
	*/
	constructor: function(node) {
		this._mytag = "svg:marker";
	},
});
