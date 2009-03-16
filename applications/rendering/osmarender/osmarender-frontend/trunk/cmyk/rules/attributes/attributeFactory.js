dojo.provide("cmyk.rules.attributes.attributeFactory");

dojo.require("cmyk.rules.attributes.singleAttribute");
dojo.require("cmyk.rules.attributes.multiAttribute");

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
			//TODO: support checking of valid attribute for this calling class
			switch (attribute_name) {
				case "class":
					return new cmyk.rules.attributes.multiAttribute(attribute_name,attribute_value," ");
				break;
				case "mask-class":
					return new cmyk.rules.attributes.multiAttribute(attribute_name,attribute_value," ");
				break;
				case "e":
					return new cmyk.rules.attributes.multiAttribute(attribute_name,attribute_value,"|");
				break;
				case "k":
					return new cmyk.rules.attributes.multiAttribute(attribute_name,attribute_value,"|");
				break;
				case "v":
					return new cmyk.rules.attributes.multiAttribute(attribute_name,attribute_value,"|");
				break;
				default:
					return new cmyk.rules.attributes.singleAttribute(attribute_name,attribute_value);
			}
		}
	}

});


 
