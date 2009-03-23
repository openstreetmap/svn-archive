dojo.provide("cmyk.rules.selectorRule");

dojo.require("cmyk.rules.Rule");

/**
	@lends cmyk.rules.selectorRule
*/

dojo.declare("cmyk.rules.selectorRule",cmyk.rules.Rule,{
	/** 
	      @constructs
	      @class A class that represent a selector Rule
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Rule
	*/
	constructor: function(node) {
		this._mytag = "rule";
/*		var selector=node.getAttribute("s");

		this.getSelector = function() {
			return dojo.clone(selector);
		};*/

	},
});
