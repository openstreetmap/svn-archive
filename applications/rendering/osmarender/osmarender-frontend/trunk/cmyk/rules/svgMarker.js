dojo.provide("cmyk.rules.svgMarker");

dojo.require("cmyk.rules._svgFeature");

/**
	@lends cmyk.rules.svgMarker
*/

dojo.declare("cmyk.rules.svgMarker",cmyk.rules._svgFeature,{
	/** 
	      @constructs
	      @class This is a class that represents an SVG Marker
	      @memberOf cmyk.rules
	      @extends cmyk.rules._svgFeature
	*/
	constructor: function(node) {
		var _class="cmyk.rules.svgMarker";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
