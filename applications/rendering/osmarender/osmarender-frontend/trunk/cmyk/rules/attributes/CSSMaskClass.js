dojo.provide("cmyk.rules.attributes.CSSMaskClass");

dojo.require("cmyk.rules.attributes._multiAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.CSSMaskClass
*/

dojo.declare("cmyk.rules.attributes.CSSMaskClass",cmyk.rules.attributes._multiAttribute,{
	/** 
	      @constructs
	      @class CSS Mask Class
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._multiAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.CSSMaskClass";
		this.setName("mask-class");
		this.setSplitter(" ");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDCLASS);
		this.setValidator(validator);
	},
});


 
