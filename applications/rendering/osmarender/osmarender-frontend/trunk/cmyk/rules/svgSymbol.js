dojo.provide("cmyk.rules.svgSymbol");

dojo.require("cmyk.rules._svgFeature");

/**
	@lends cmyk.rules.svgSymbol
*/

dojo.declare("cmyk.rules.svgSymbol",cmyk.rules._svgFeature,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Symbol
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgFeature
	*/
	constructor: function(node) {
		var _class="cmyk.rules.svgSymbol";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
