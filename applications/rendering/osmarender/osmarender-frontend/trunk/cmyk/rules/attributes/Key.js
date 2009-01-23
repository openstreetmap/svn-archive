dojo.provide("cmyk.rules.attributes.Key");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Key
*/

dojo.declare("cmyk.rules.attributes.Key",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Key
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Key";
		this.setName("k");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDKEY);
		this.setValidator(validator);
	},
});


 
