dojo.provide("cmyk.rules.attributes.maximumWidth");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.maximumWidth
*/

dojo.declare("cmyk.rules.attributes.maximumWidth",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Maximum Width
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.maximumWidth";
		this.setName("maximum-width");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
