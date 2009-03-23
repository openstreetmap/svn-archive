dojo.provide("cmyk.rules.svgRect");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgRect
*/

dojo.declare("cmyk.rules.svgRect",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Rect
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		this._mytag = "svg:rect";
	},
});
