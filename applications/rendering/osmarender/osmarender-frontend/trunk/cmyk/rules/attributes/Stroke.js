dojo.provide("cmyk.rules.attributes.Stroke");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Stroke
*/

dojo.declare("cmyk.rules.attributes.Stroke",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Stroke
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Stroke";
		this.setName("stroke");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._NONE);
		this.setValidator(validator);
	},
});


 
