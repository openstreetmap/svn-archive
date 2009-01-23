dojo.provide("cmyk.rules.Line");

dojo.require("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.attributes.attributeFactory");

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

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
