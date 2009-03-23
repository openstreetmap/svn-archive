dojo.provide("cmyk.rules.Caption");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Caption
*/

dojo.declare("cmyk.rules.Caption",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "Caption" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "caption";
	},
});
