dojo.provide("cmyk.rules.attributes.strokeOpacity");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.strokeOpacity
*/

dojo.declare("cmyk.rules.attributes.strokeOpacity",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class strokeOpacity
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.strokeOpacity";
		this.setName("stroke-opacity");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISNUMBER);
		this.setValidator(validator);
	},
});


 
