dojo.provide("cmyk.rules.rulesSection");

dojo.require("cmyk.rules.ruleFileMember");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.RulesSection
*/

dojo.declare("cmyk.rules.rulesSection",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A RUles
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		this._mytag = "rules";
	},
});
