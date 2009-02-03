dojo.provide("cmyk.rules.Caption");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.Caption
*/

dojo.declare("cmyk.rules.Caption",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "Caption" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Caption";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
