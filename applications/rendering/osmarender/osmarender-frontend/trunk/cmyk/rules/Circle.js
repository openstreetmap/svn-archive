dojo.provide("cmyk.rules.Circle");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Circle
*/

dojo.declare("cmyk.rules.Circle",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "circle" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Circle";

		var _attributes = {
			radius: 0,
			classes: [],
			transform: null,
			layer: null,
			stroke: null,
			strokewidth: null,
			fill: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "class":
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				case "r":
					//TODO: Handle px
/*					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.radius = current_value;					*/
					_attributes.radius = attribute.nodeValue;
				break;
				// TODO: Probably need to port it in the superclass
				case "transform":
					_attributes.transform = attribute.nodeValue;
				break;
				// TODO: Probably need to port it in the superclass
				case "stroke":
					_attributes.stroke = attribute.nodeValue;
				break;
				// TODO: Probably need to port it in the superclass
				case "stroke-width":
					_attributes.strokewidth = attribute.nodeValue;
				break;
				// TODO: Probably need to port it in the superclass
				case "fill":
					_attributes.fill = attribute.nodeValue;
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
	},
});
