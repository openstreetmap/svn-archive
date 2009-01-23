dojo.provide("cmyk.rules.attributes.honorWidth");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.honorWidth
*/

dojo.declare("cmyk.rules.attributes.honorWidth",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Honor Width
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.honorWidth";
		this.setName("honor-width");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISBOOLEANYESNO);
		this.setValidator(validator);
	},
});


 
