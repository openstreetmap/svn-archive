dojo.provide("cmyk.rules.pathText");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.pathText
*/

dojo.declare("cmyk.rules.pathText",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "pathText" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.pathText";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
