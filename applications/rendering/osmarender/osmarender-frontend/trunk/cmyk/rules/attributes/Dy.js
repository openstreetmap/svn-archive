dojo.provide("cmyk.rules.attributes.Dy");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Dy
*/

dojo.declare("cmyk.rules.attributes.Dy",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Dy
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Dy";
		this.setName("dy");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
