dojo.provide("cmyk.rules.Area");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Area
*/

dojo.declare("cmyk.rules.Area",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class An "area" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Area";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
