dojo.provide("cmyk.rules.renderingInstruction");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.renderingInstruction
*/

dojo.declare("cmyk.rules.renderingInstruction",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class The superclass of every rendering instruction (line, circle, etc..)
	      @memberOf cmyk.rules
	      @extends cmyk.rules.Directive
	*/
	constructor: function(node) {
		// this contains the name of the tag of the rendering instruction
		this._mytag = "generic_rendering_instruction";
		var _classes = new Array();
		var _attributes = new Array();
		var _list_attributes_multiple_space = ["class","mask-class"];
		var _list_attributes_multiple_or = ["e","k","v"];
		var _xmlNodeRead = node;
		var _xmlNodeWrite = null;

//		var _attributeFactory = new cmyk.rules.attributes.attributeFactory();

		dojo.forEach(node.attributes, function(attribute,index,array) {
			if (dojo.indexOf(_list_attributes_multiple_space,attribute.nodeName)!=-1) {
				_attributes.push({"name":attribute.nodeName,"value":attribute.nodeValue.split(" ")});
			}
			else if (dojo.indexOf(_list_attributes_multiple_or,attribute.nodeName)!=-1) {
				_attributes.push({"name":attribute.nodeName,"value":attribute.nodeValue.split("|")});
			}
			else {
				_attributes.push({"name":attribute.nodeName,"value":attribute.nodeValue});
			}
			//_attributes.push(_attributeFactory.factory(attribute.nodeName,attribute.nodeValue));
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
			var my_node = document.createElementNS("",this._mytag);
			for (var attribute_index in _attributes) {
				var value_to_write="";
				if (dojo.indexOf(_list_attributes_multiple_space,_attributes[attribute_index].name)!=-1) {
					value_to_write=_attributes[attribute_index].value.join(" ");
				}
				else if (dojo.indexOf(_list_attributes_multiple_or,_attributes[attribute_index].name)!=-1) {
					value_to_write=_attributes[attribute_index].value.join("|");
				}
				else {
					value_to_write=_attributes[attribute_index].value;
				}

				my_node.setAttribute(_attributes[attribute_index].name,value_to_write);
			}
			_xmlNodeWrite = my_node;
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	},
});
