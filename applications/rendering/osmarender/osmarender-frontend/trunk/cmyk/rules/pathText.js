dojo.provide("cmyk.rules.pathText");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.pathText
*/

dojo.declare("cmyk.rules.pathText",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "pathText" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "pathText";
	},
});
