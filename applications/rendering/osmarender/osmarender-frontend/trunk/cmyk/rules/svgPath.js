dojo.provide("cmyk.rules.svgPath");

dojo.require("cmyk.rules._svgDirective");

/**
	@lends cmyk.rules.svgPath
*/

dojo.declare("cmyk.rules.svgPath",cmyk.rules._svgDirective,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Path
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgDirective
	*/
	constructor: function(node) {
		var _class="cmyk.rules.svgPath";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
