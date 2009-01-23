dojo.provide("cmyk.rules.textWithK");

dojo.require("cmyk.rules._wayText");

/**
	@lends cmyk.rules.textWithK
*/

dojo.declare("cmyk.rules.textWithK",cmyk.rules._wayText,{
	/** 
	      @constructs
	      @class This class represents text inserted using "k"
	      @memberOf cmyk.rules
	      @extends cmyk.rules._wayText
	*/
	constructor: function(node) {
		var _class="cmyk.rules.textWithK";

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		var _attributes = [];

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue,_class));
		});
	},
});
