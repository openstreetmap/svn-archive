dojo.provide("cmyk.rules.attributes.markerMid");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.startOffset
*/

dojo.declare("cmyk.rules.attributes.markerMid",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class markerMid
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.markerMid";
		this.setName("marker-mid");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDMARKER);
		this.setValidator(validator);
	},
});


 
