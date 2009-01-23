dojo.provide("cmyk.rules.attributes.smartLineCap");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.smartLineCap
*/

dojo.declare("cmyk.rules.attributes.smartLineCap",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Honor Width
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.smartLineCap";
		this.setName("smart-linecap");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISBOOLEANYESNO);
		this.setValidator(validator);
	},
});


 
