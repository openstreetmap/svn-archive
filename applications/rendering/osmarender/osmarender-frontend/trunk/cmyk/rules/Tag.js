dojo.provide("cmyk.rules.Tag");

dojo.require("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.Tag
*/

dojo.declare("cmyk.rules.Tag",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A Tag rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Tag";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
