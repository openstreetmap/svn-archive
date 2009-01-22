dojo.provide("cmyk.rules.Symbol");

dojo.require("cmyk.rules._Symbol");

/**
	@lends cmyk.rules.Symbol
*/

dojo.declare("cmyk.rules.Symbol",cmyk.rules._Symbol,{
	/** 
	      @constructs
	      @class This is a class representing a Symbol rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules._Symbol
	      @private
	*/
	constructor: function(node) {
//TODO: connect the real svg inside the object
		var _class="cmyk.rules.Symbol";

		var _attributes = {
			ref: "",
			position: "center",
			transform: null,
			layer: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "ref":
					_attributes.ref = attribute.nodeValue;
				break;
				case "position":
					_attributes.position= attribute.nodeValue;
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});

		this.getRef = function() {
			return dojo.clone(_attributes.ref);
		};

		this.getPosition = function() {
			return dojo.clone(_attributes.position);
		};
	},
});
