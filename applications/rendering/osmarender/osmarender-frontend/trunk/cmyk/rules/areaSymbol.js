dojo.provide("cmyk.rules.areaSymbol");

dojo.require("cmyk.rules._Symbol");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.areaSymbol
*/

dojo.declare("cmyk.rules.areaSymbol",cmyk.rules._Symbol,{
	/** 
	      @constructs
	      @class This is a class representing an AreaSymbol rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules._Symbol
	      @private
	*/
	constructor: function(node) {
//TODO: connect the real svg inside the object
		var _class="cmyk.rules.areaSymbol";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
