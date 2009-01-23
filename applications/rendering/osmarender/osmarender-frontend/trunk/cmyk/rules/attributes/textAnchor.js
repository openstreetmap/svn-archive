dojo.provide("cmyk.rules.attributes.textAnchor");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.textAnchor
*/

dojo.declare("cmyk.rules.attributes.textAnchor",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class textAnchor
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.textAnchor";
		this.setName("text-anchor");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._NONE);
		this.setValidator(validator);
	},
});


 
