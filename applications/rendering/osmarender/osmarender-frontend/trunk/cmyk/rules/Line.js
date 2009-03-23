dojo.provide("cmyk.rules.Line");

dojo.require("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.Line
*/

dojo.declare("cmyk.rules.Line",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "line" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "line";
	},
});
