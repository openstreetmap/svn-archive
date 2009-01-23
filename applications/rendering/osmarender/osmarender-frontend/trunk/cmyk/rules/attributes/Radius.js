dojo.provide("cmyk.rules.attributes.Radius");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Radius
*/

dojo.declare("cmyk.rules.attributes.Radius",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Radius
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Radius";
		this.setName("radius");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
