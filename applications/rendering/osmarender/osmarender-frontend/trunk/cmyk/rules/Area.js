dojo.provide("cmyk.rules.Area");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Area
*/

dojo.declare("cmyk.rules.Area",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class An "area" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Area";

		var _attributes = {
			classes: [],
			smartlinecap: null,
			layer: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "class":
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				case "smart-linecap":
					var current_value;
					if (attribute.nodeValue=="yes") current_value==true;
					else if (attribute.nodeValue=="no") current_value==false;
					else throw new Error('attribute '+attribute.nodeName+' must be "yes" or "no". Value '+attribute.nodeValue+' encountered instead for class '+_class);
					_attributes.smartlinecap = new Boolean(current_value);
				break;
				case "layer":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.layer = current_value;					
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});

		this.getClasses = function() {
			return dojo.clone(_attributes.classes);
		};
	},
});
