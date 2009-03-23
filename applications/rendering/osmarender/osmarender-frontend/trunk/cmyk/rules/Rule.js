dojo.provide("cmyk.rules.Rule");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Rule
*/

dojo.declare("cmyk.rules.Rule",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A class that represent a Rule tag
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive,cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		this._mytag = "rule";
	}
});
