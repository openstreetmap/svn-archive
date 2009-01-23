dojo.provide("cmyk.rules.attributes.Layer");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Layer
*/

dojo.declare("cmyk.rules.attributes.Layer",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Layer
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Layer";
		this.setName("layer");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
