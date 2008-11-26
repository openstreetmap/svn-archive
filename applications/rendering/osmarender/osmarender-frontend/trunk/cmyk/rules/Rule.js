dojo.provide("cmyk.rules.Rule");

dojo.require("cmyk.rules.Directive");
dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.Rule
*/

dojo.declare("cmyk.rules.Rule",[cmyk.rules.Directive,cmyk.rules.ruleFileMember],{
	/** 
	      @constructs
	      @class A class that represent a Rule tag
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive,cmyk.rules.ruleFileMember
	*/
	constructor: function() {
	},
});
