dojo.provide("cmyk.rules.attributes.CSSClass");

dojo.require("cmyk.rules.attributes._multiAttribute");

dojo.require("cmyk.utils.Evaluators");

/**
	@lends cmyk.rules.attributes.CSSClass
*/

dojo.declare("cmyk.rules.attributes.CSSClass",cmyk.rules.attributes._multiAttribute,{
	/** 
	      @constructs
	      @class CSS Class
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._Attribute
	*/
	constructor: function(attribute_value) {
		var evaluator = new cmyk.utils.Evaluators();
		this.setName("class");
		this.setSplitter(" ");
		this.setValue(attribute_value);
		this.setEvaluator(evaluator._ISVALIDCLASS);
	},
});


 
