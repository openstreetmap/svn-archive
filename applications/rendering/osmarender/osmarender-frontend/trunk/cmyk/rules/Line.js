dojo.provide("cmyk.rules.Line");

dojo.require("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.attributes.attributeFactory");

/**
	@lends cmyk.rules.Line
*/

dojo.declare("cmyk.rules.Line",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "line" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		var _class="cmyk.rules.Line";

		var _evaluator = new cmyk.utils.Evaluators();
		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

/*		var _newattributes = {
			classes: {
				attributeName: "class",
				value: [],
				evaluator: _evaluator._ISVALIDCLASS,
				splitter: " "
			},
			maskclasses: {
				attributeName: "mask-class",
				value: [],
				evaluator: _evaluator._ISVALIDCLASS,
				splitter: " "
			},
			widthscalefactor: {
				attributeName: "width-scale-factor".
				value: null,
				evaluator: _evaluator._ISNUMBER
			},
			honorwidth: {
				attributeName: "honor-width",
				value: null,
				evaluator: _evaluator._ISNUMBER
			},
			minimumwidth: {
				attributeName: "minimum-width",
				value: null,
				evaluator: _evaluator._ISNUMBER
			},
			maximumwidth: {
				attributeName: "maximum-width",
				value: null,
				evaluator: _evaluator._ISNUMBER
			},
			smartlinecap: {
				attributeName: "smart-linecap",
				value: null,
				evaluator: evaluator._ISBOOLEAN
			},
			suppressmarkerstag: {
				attributeName: "suppress-markers-tag",
				value: null,
				evaluator: evaluator._ISVALIDTAG
			},
			layer: {
				attributeName: "layer",
				value: null,
				evaluator: evaluator._ISNUMBER
			}
		};*/

		var _attributes = {
			classes: [],
			maskclasses: [],
			widthscalefactor: null,
			honorwidth: null,
			minimumwidth: null,
			maximumwidth: null,
			smartlinecap: null,
			suppressmarkerstag: null,
			layer: null
		};

		dojo.forEach(node.attributes, function(attribute,index,array) {
			switch (attribute.nodeName) {
				case "class":
//					console.debug("AAA creo classe");
					var pippo = _attributeFactory.factory("class",attribute.nodeValue);
//					console.debug("AAA il mio valore è "+pippo.getValue());
//					console.debug("AAA il mio splitter è "+pippo.getSplitter());
					_attributes.classes = attribute.nodeValue.split(" ");
				break;
				case "mask-class":
					_attributes.maskclasses = attribute.nodeValue.split(" ");
				break;
				case "suppress-markers-tag":
					//TODO: is valid tag?
					var current_value = new String(attribute.nodeValue);
					_attributes.suppressmarkerstag = current_value;
				break;
				case "width-scale-factor":
					if (_evaluator.isNumber(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.widthscalefactor = attribute.nodeValue;
					}
				break;
				case "minimum-width":
					if (_evaluator.isNumber(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.minimumwidth = attribute.nodeValue;
					}
				break;
				case "maximum-width":
					if (_evaluator.isNumber(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.maximumwidth = attribute.nodeValue;
					}
				break;
				case "honor-width":
					if (_evaluator.isBoolean(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.honorwidth = _evaluator.convertToBoolean(attribute.nodeName,attribute.nodeValue,_class,_evaluator._YESNO);
					}
				break;
				case "smart-linecap":
					if (_evaluator.isBoolean(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.smartlinecap = _evaluator.convertToBoolean(attribute.nodeName,attribute.nodeValue,_class,_evaluator._YESNO);
					}
				break;
				case "layer":
					if (_evaluator.isNumber(attribute.nodeName,attribute.nodeValue,_class)) {
						_attributes.layer = attribute.nodeValue;
					}
				break;
				default:
					throw new Error('unknown attribute: '+attribute.nodeName+' with value '+attribute.nodeValue+' for class '+_class);
			}
		});

		this.getClasses = function() {
			return dojo.clone(_attributes.classes);
		};
	},
});
