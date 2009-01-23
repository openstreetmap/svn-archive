dojo.provide("cmyk.rules.attributes.Transform");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Transform
*/

dojo.declare("cmyk.rules.attributes.Transform",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Transform
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Transform";
		this.setName("transform");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDTRANSFORM);
		this.setValidator(validator);
	},
});


 
