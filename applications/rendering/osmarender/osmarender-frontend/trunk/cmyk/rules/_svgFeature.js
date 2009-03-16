dojo.provide("cmyk.rules._svgFeature");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules._svgFeature
*/

dojo.declare("cmyk.rules._svgFeature",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class This is a private superclass of SVG features (patterns, markers, symbols). Needed to keep compatibility with old osm-features
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	      @private
	*/
	constructor: function() {
	},
});
