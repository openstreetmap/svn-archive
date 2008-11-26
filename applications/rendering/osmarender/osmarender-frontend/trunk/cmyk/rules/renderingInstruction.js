dojo.provide("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.Directive");

/**
	@lends cmyk.rules.renderingInstruction
*/

dojo.declare("cmyk.rules.renderingInstruction",cmyk.rules.Directive,{
	/** 
	      @constructs
	      @class The superclass of every rendering instruction (line, circle, etc..)
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive
	*/
	constructor: function() {
	},
});
