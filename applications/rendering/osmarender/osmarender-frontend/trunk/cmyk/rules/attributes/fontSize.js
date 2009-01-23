dojo.provide("cmyk.rules.attributes.fontSize");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.fontSize
*/

dojo.declare("cmyk.rules.attributes.fontSize",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class fontSize
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.fontSize";
		this.setName("font-size");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
