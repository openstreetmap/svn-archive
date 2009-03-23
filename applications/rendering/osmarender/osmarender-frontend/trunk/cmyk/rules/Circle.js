dojo.provide("cmyk.rules.Circle");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Circle
*/

dojo.declare("cmyk.rules.Circle",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "circle" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "circle";
	},
});
