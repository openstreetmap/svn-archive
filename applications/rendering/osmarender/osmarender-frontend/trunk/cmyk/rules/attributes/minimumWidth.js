dojo.provide("cmyk.rules.attributes.minimumWidth");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.minimumWidth
*/

dojo.declare("cmyk.rules.attributes.minimumWidth",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Minimum Width
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.minimumWidth";
		this.setName("minimum-width");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
