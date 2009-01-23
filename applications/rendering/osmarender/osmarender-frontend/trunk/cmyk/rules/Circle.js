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

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
