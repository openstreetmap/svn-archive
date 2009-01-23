dojo.provide("cmyk.rules.attributes.suppressMarkersTag");

dojo.require("cmyk.rules.attributes._singleAttribute");

dojo.require("cmyk.utils.Validators");

/**
	@lends cmyk.rules.attributes.SuppressMarkersTag
*/

dojo.declare("cmyk.rules.attributes.suppressMarkersTag",cmyk.rules.attributes._singleAttribute,{
	/** 
	      @constructs
	      @class Suppress Markers Tag
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._singleAttribute
	*/
	constructor: function(attribute_value) {
		var _class = "cmyk.rules.attributes.suppressMarkersTag";
		this.setName("suppress-markers-tag");
		this.setValue(attribute_value);

		var validator = new cmyk.utils.Validators(this.getName(),attribute_value,_class);
		validator.setValidator(validator._ISVALIDTAG);
		this.setValidator(validator);
	},
});


 
