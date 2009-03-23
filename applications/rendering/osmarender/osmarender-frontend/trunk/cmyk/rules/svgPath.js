dojo.provide("cmyk.rules.svgPath");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgPath
*/

dojo.declare("cmyk.rules.svgPath",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Path
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		this._mytag = "svg:path";
	},
});
