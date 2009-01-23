dojo.provide("cmyk.rules.attributes.Dx");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Dx
*/

dojo.declare("cmyk.rules.attributes.Dx",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Dx
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Dx";
		this.setName("dx");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
