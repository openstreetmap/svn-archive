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

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
