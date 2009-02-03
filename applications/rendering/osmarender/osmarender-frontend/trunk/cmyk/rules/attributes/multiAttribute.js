dojo.provide("cmyk.rules.attributes.multiAttribute");

dojo.require("cmyk.rules.attributes._Attribute");

/**
	@lends cmyk.rules.attributes.multiAttribute
*/

dojo.declare("cmyk.rules.attributes.multiAttribute",cmyk.rules.attributes._Attribute,{
	/** 
	      @constructs
	      @class Parent class of all attributes with multi
	      @memberOf cmyk.rules.attributes
	      @extends cmyk.rules.attributes._Attribute
	*/
	constructor: function(my_name,my_value,my_splitter) {
		var _splitter = my_splitter;
		var _value = new Array();

		this.getSplitter = function() {
			return dojo.clone(_splitter);
		}

		this.setSplitter = function(splitter) {
			_splitter=splitter;
		}

		this.setValue = function(attribute_value) {
			_value = attribute_value.split(_splitter);
		}

		this.getValue = function() {
			return dojo.clone(_value);
		}
		this.setValue(my_value);
	}

});


 
