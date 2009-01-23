dojo.provide("cmyk.rules.attributes.textAttenuation");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.textAttenuation
*/

dojo.declare("cmyk.rules.attributes.textAttenuation",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class textAttenuation
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.textAttenuation";
		this.setName("textAttenuation");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
