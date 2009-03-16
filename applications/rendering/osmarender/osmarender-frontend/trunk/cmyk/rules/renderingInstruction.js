dojo.provide("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.Directive");

/**
	@lends cmyk.rules.renderingInstruction
*/

dojo.declare("cmyk.rules.renderingInstruction",cmyk.rules.Directive,{
	/** 
	      @constructs
	      @class The superclass of every rendering instruction (line, circle, etc..)
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive
	*/
	constructor: function(node) {
		var _classes = new Array();
		var _attributes = new Array();
		// this contains the name of the tag of the rendering instruction
		var _mytag = "null";
		var _xmlNodeRead = node;
		var _xmlNodeWrite = null;

		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		dojo.forEach(node.attributes, function(attribute,index,array) {
			_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue));
		});


		this.addClass = function(name,object) {
			_classes[name]=object;
		};

		this.getClass = function(name) {
			if (_classes[name]!=undefined) {
				return _classes[name];
			}
			throw new Error ("unknown class "+name);
		};

		this.getAttributes = function() {
			return _attributes;
		};

		this.write = function() {
//console.debug("creating renderinginstruction");
			var my_node = document.createElementNS("",_mytag);
//console.debug("creating attributes");
			for (var attribute_index in _attributes) {
//console.debug("creating attribute "+_attributes[attribute_index].getName()+" with value: "+_attributes[attribute_index].getValue());
				my_node.setAttribute(_attributes[attribute_index].getName(),_attributes[attribute_index].getValue());
			}
			_xmlNodeWrite = my_node;
//console.debug("fine renderinginstruction");
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	},
});
