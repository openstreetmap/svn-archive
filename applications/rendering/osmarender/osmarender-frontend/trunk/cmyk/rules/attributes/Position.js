dojo.provide("cmyk.rules.attributes.Position");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.Ref
*/

dojo.declare("cmyk.rules.attributes.Position",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Position
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.Position";
		this.setName("position");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDPOSITION);
		this.setValidator(validator);
	},
});


 
