dojo.provide("cmyk.rules.svgG");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgG
*/

dojo.declare("cmyk.rules.svgG",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG G
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		this._mytag = "svg:g";
	},
});
