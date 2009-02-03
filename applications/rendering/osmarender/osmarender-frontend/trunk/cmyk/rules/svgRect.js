dojo.provide("cmyk.rules.svgRect");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgRect
*/

dojo.declare("cmyk.rules.svgRect",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Rect
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		var _class="cmyk.rules.svgRect";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
