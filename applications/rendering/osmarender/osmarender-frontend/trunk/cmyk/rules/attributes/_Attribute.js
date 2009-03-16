dojo.provide("cmyk.rules.attributes._Attribute");

/**
	@lends cmyk.rules.attributes._Attribute
*/

dojo.declare("cmyk.rules.attributes._Attribute",null,{
	/** 
	      @constructs
	      @class Parent class of all attributes
	      @memberOf cmyk.rules.attributes
	*/
	constructor: function(my_name,my_value) {
		var _name = my_name;
		var _value = "";
		var _validator = null;

		this.getName = function() {
			return _name;
		}

		this.setName = function(name) {
			_name = name;
		}

		this.setValue = function(value) {
			_value = value;
		}

		this.getValue = function() {
			return _value;
		}

		this.getValidator = function() {
			return _validator;
		}

		this.setValidator = function(validator) {
			_validator = validator;
		}

		this.applyValidator = function() {
			if (validator!=undefined) {
				return validator.applyValidator();
			}
		}

		this.setValue(my_value);

	},
});


 
