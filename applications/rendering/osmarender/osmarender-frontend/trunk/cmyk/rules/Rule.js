dojo.provide("cmyk.rules.Rule");

dojo.require("cmyk.rules.Directive");
dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.Rule
*/

dojo.declare("cmyk.rules.Rule",[cmyk.rules.Directive,cmyk.rules.ruleFileMember],{
	/** 
	      @constructs
	      @class A class that represent a Rule tag
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive,cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Rule";

		var _attributes = {
			types: [],
			keys: [],
			values: [],
			layer: null,
			notconnectedsametag: null
		};
		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				// TODO: verify if you can actually apply this rule to this key/value pair, need to create objects instead of text values
				case "e":
					_attributes.types = attribute.nodeValue.split("|");
				break;
				case "k":
					_attributes.keys = attribute.nodeValue.split("|");
				break;
				case "v":
					_attributes.values = attribute.nodeValue.split("|");
				break;
				//TODO:Should avoid this, but needed to not throw an error if this is a selectorRule instance
				case "s": break;
				case "layer":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribue.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.layer = current_value;					
				break;
				case "notConnectedSameTag":
					_attributes.notconnectedsametag = current_value;					
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});

		this.getTypes = function() {
			return dojo.clone(_attributes.types);
		};

		this.getKeys = function() {
			return dojo.clone(_attributes.keys);
		};

		this.getValues = function() {
			return dojo.clone(_attributes.values);
		}
	},
});
