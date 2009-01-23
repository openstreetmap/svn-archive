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
	constructor: function() {
		var _name = "";
		var _value = "";
		var _validator = null;

		this.getName = function() {
			return dojo.clone(_name);
		}

		this.setName = function(name) {
			_name = dojo.clone(name);
		}

		this.setValue = function(value) {
			_value = dojo.clone(value);
		}

		this.getValue = function() {
			return dojo.clone(_value);
		}

		this.getValidator = function() {
			return dojo.clone(_validator);
		}

		this.setValidator = function(validator) {
			_validator = validator;
		}

		this.applyValidator = function() {
			if (validator!=undefined) {
				return validator.applyValidator();
			}
		}
	},
});


 
