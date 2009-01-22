dojo.provide("cmyk.rules.areaSymbol");

dojo.require("cmyk.rules._Symbol");

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
				// TODO: Probably need to port it in the superclass
				case "transform":
					_attributes.transform= attribute.nodeValue;
				break;
				case "layer":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.layer = current_value;					
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
