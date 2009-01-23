dojo.provide("cmyk.rules.attributes.CSSClass");

dojo.require("cmyk.rules.attributes._multiAttribute");

dojo.require("cmyk.utils.Dimensions");
//dojo.require("cmyk.utils.Validators");

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
//console.debug("before");
//var dimension = new cmyk.utils.Dimensions("10px");
//TODO: this not works... but the above yes... why????
		//var validator = new cmyk.utils.Validators("class",attribute_value,"cmyk.rules.attributes.CSSClass");
//console.debug("after");
//		var validator = new cmyk.utils.Validators();
		//validator.setValidator(validator._ISVALIDCLASS);
		this.setName("class");
		this.setSplitter(" ");
		this.setValue(attribute_value);
		//this.setValidator(validator);
	},
});


 
