dojo.provide("cmyk.rules.attributes.startOffset");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.startOffset
*/

dojo.declare("cmyk.rules.attributes.startOffset",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class startOffset
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.startOffset";
		this.setName("startOffset");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISDIMENSION);
		this.setValidator(validator);
	},
});


 
