dojo.provide("cmyk.rules.includeSection");

dojo.require("cmyk.rules.ruleFileMember");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.includeSection
*/

dojo.declare("cmyk.rules.includeSection",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A include
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		this._mytag = "include";
	},
});
