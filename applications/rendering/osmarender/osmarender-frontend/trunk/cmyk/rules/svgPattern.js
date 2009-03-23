dojo.provide("cmyk.rules.svgPattern");

dojo.require("cmyk.rules._svgFeature");

/**
	@lends cmyk.rules.svgPattern
*/

dojo.declare("cmyk.rules.svgPattern",cmyk.rules._svgFeature,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Pattern
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgFeature
	*/
	constructor: function(node) {
		this._mytag = "svg:pattern";
	},
});
