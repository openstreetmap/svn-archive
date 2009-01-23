dojo.provide("cmyk.rules.attributes.strokeWidth");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.strokeWidth
*/

dojo.declare("cmyk.rules.attributes.strokeWidth",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class strokeWidth
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.strokeWidth";
		this.setName("stroke-width");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
