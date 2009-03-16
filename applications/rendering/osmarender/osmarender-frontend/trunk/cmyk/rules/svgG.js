dojo.provide("cmyk.rules.svgG");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgG
*/

dojo.declare("cmyk.rules.svgG",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG G
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		var _class="cmyk.rules.svgG";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
