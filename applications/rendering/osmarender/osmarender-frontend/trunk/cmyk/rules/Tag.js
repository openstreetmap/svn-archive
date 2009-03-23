dojo.provide("cmyk.rules.Tag");

dojo.require("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.Tag
*/

dojo.declare("cmyk.rules.Tag",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A Tag rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "tag";
	},
});
