dojo.provide("cmyk.rules.wayMarker");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.wayMarker
*/

dojo.declare("cmyk.rules.wayMarker",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "Way Marker" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.wayMarker";

		var _attributes = {
			key: "",
			classes: [],
			fill: null,
			stroke: null,
			strokewidth: null,
			strokeopacity: null,
			markermid: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "k":
					_attributes.key = attribute.nodeValue;
				break;
				case "fill":
					_attributes.fill = attribute.nodeValue;
				break;
				case "stroke":
					_attributes.stroke = attribute.nodeValue;
				break;
				case "stroke-width":
					_attributes.strokewidth = attribute.nodeValue;
				break;
				case "stroke-opacity":
					_attributes.strokeopacity = attribute.nodeValue;
				break;
				case "marker-mid":
					_attributes.markermid = attribute.nodeValue;
				break;
				case "class":
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});
	},
});
