dojo.provide("cmyk.rules._svgDirective");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules._svgDirective
*/

dojo.declare("cmyk.rules._svgDirective",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class This is a private superclass of SVG directive (path, rect).
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	      @private
	*/
	constructor: function() {
		this._mytag="_svgDirective";
	},
});
