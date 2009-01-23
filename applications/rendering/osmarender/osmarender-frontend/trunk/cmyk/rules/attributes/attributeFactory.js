dojo.provide("cmyk.rules.attributes.attributeFactory");

dojo.require("cmyk.rules.attributes.CSSClass");

/**
	@lends cmyk.rules.attributes.attributeFactory
*/

dojo.declare("cmyk.rules.attributes.attributeFactory",null,{
	/** 
	      @constructs
	      @class Factory of all valid attributes
	      @memberOf cmyk.rules.attributes
	*/
	constructor: function() {
		this.factory = function(attribute_name,attribute_value) {
			switch (attribute_name) {
				case "class":
					return new cmyk.rules.attributes.CSSClass(attribute_value);
				break;
			}
		}
	}

});


 
