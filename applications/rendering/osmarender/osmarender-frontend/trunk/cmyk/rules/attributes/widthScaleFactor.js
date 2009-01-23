dojo.provide("cmyk.rules.attributes.widthScaleFactor");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.widthScaleFactor
*/

dojo.declare("cmyk.rules.attributes.widthScaleFactor",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Width Scale Factor
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.widthScaleFactor";
		this.setName("width-scale-factor");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
