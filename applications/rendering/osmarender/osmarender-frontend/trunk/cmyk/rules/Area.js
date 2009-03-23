dojo.provide("cmyk.rules.Area");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Area
*/

dojo.declare("cmyk.rules.Area",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class An "area" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "area";
	},
});
