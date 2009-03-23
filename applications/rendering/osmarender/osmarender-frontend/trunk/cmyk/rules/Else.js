dojo.provide("cmyk.rules.Else");

dojo.require("cmyk.rules.Directive");

/**
	@lends cmyk.rules.Else
*/

dojo.declare("cmyk.rules.Else",cmyk.rules.Directive,{
	/** 
	      @constructs
	      @class A class that represent an Else tag
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive
	*/
	constructor: function(node) {
		this._mytag = "else";
	},
});
