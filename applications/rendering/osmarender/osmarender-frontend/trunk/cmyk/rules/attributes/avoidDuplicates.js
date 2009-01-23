dojo.provide("cmyk.rules.attributes.avoidDuplicates");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.avoidDuplicates
*/

dojo.declare("cmyk.rules.attributes.avoidDuplicates",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class avoidDuplicates
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.avoidDuplicates";
		this.setName("avoid-duplicates");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISBOOLEANTRUEFALSE);
		this.setValidator(validator);
	},
});


 
