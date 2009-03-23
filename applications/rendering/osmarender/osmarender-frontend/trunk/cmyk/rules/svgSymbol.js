dojo.provide("cmyk.rules.svgSymbol");

dojo.require("cmyk.rules._svgFeature");

/**
	@lends cmyk.rules.svgSymbol
*/

dojo.declare("cmyk.rules.svgSymbol",cmyk.rules._svgFeature,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Symbol
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgFeature
	*/
	constructor: function(node) {
		this._mytag = "svg:symbol";
	},
});
