dojo.provide("cmyk.rules.attributes.Ref");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Ref
*/

dojo.declare("cmyk.rules.attributes.Ref",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Ref
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Ref";
		this.setName("ref");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDSYMBOL);
		this.setValidator(validator);
	},
});


 
