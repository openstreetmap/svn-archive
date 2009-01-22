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

		var _attributes = {
			key: "",
			startoffset: "center",
			classes: [],
			dx: null,
			dy: null,
			layer: null,
			textanchor: null,
			avoidduplicates: null,
			textattenuation: null,
			fontsize:null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "k":
					_attributes.key = attribute.nodeValue;
				break;
				//TODO: need to check px
				case "font-size":
					_attributes.fontsize = attribute.nodeValue;
				break;
				case "text-anchor":
					_attributes.textanchor = attribute.nodeValue;
				break;
				//TODO: need to check %
				case "startOffset":
					_attributes.startoffset = attribute.nodeValue;
				break;
				case "avoid-duplicates":
					var current_value;
					if (attribute.nodeValue=="true") current_value==true;
					else if (attribute.nodeValue=="false") current_value==false;
					else throw new Error('attribute '+attribute.nodeName+' must be "true" or "false". Value '+attribute.nodeValue+' encountered instead');
					_attributes.avoidduplicates = new Boolean(current_value);
				break;
				//TODO: need to check px
				case "dy":
					_attributes.dy = attribute.nodeValue;
				break;
				//TODO: need to check px
				case "dx":
					_attributes.dx = attribute.nodeValue;
				break;
				case "class":
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				case "layer":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.layer = current_value;					
				break;
				case "textAttenuation":
					var current_value = new Number(attribute.nodeValue);
					if (isNaN(current_value)) {
						throw new Error('attribute '+attribute.nodeName+' must be a Number. Value '+attribute.nodeValue+' encountered instead for class '+_class);
					}
					_attributes.textattenuation = current_value;					
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});
	},
});
