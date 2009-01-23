dojo.provide("cmyk.rules.attributes.Fill");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Fill
*/

dojo.declare("cmyk.rules.attributes.Fill",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Fill
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Fill";
		this.setName("fill");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._NONE);
		this.setValidator(validator);
	},
});


 
