dojo.provide("cmyk.rules.Directive");

dojo.require("cmyk.rules.ruleFileMember");
/**
	@lends cmyk.rules.Directive
*/

dojo.declare("cmyk.rules.Directive",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A class that represent a generic directive inside a rule
	      @memberOf cmyk.rules
	*/
	constructor: function(node) {
                this._mytag="directive";
	}
});
