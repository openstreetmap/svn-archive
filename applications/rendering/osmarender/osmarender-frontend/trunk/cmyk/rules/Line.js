dojo.provide("cmyk.rules.Line");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Line
*/

dojo.declare("cmyk.rules.Line",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "line" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Line";

		var _attributes = {
			classes: [],
			maskclasses: [],
			widthscalefactor: null,
			honorwidth: null,
			minimumwidth: null,
			maximumwidth: null,
			smartlinecap: null,
			suppressmarkerstag: null,
			layer: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "class":
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				case "mask-class":
					_attributes.maskclasses = attribute.nodeValue.split(" ");
				break;
				case "suppress-markers-tag":
					//TODO: is valid tag?
					var current_value = new String(attribute.nodeValue);
					_attributes.suppressmarkerstag = current_value;
				break;
				case "width-scale-factor":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.widthscalefactor = current_value;
				break;
				case "minimum-width":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.minimumwidth = current_value;
				break;
				case "maximum-width":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.maximumwidth = current_value;
				break;
				case "honor-width":
					var current_value;
					if (attribute.nodeValue=="yes") current_value==true;
					else if (attribute.nodeValue=="no") current_value==false;
					else throw new Error('attribute '+attribute.nodeName+' must be "yes" or "no". Value '+attribute.nodeValue+' encountered instead for class '+_class);
					_attributes.honorwidth = new Boolean(current_value);
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
