dojo.provide("cmyk.utils.Validators");

/**
	@lends cmyk.utils.Validators
*/

dojo.declare("cmyk.utils.Validators",null,{
	/** 
	      @constructs
	      @class A class with various validator utilities
	      @memberOf cmyk.utils
	*/
	constructor: function(name_of_attribute,value_to_validate,calling_class) {
		this._ISBOOLEANYESNO = 0;
		this._ISBOOLEANTRUEFALSE = 1;
		this._ISNUMBER = 2;
		this._ISVALIDCLASS = 3;
		this._ISVALIDTAG = 4;
		this._ISVALIDSYMBOL = 5;
		this._ISVALIDPOSITION = 6;
		this._ISVALIDTRANSFORM = 7;
		this._ISDIMENSION = 8;
		this._NONE = 9;
		this._ISVALIDKEY = 10;
		this._ISVALIDMARKER = 11;

		var _validator_type;
		_value_to_validate = value_to_validate;
		_calling_class = calling_class;
		_name_of_attribute = name_of_attribute;

		isBoolean = function(my_value) {
			if (my_value=='yes' || my_value=='true' || my_value=='no' || my_value=='false') {
				return true;
			}
			return false;
		}

		isNumber = function(my_variable,my_value,calling_class) {
			var current_value = new Number(my_value);
			if (isNaN(current_value)) {
				throw new Error('attribute '+my_variable+' must be a Number. Value '+my_value+' encountered instead for class '+calling_class);
			}
			return true;
		}


		convertToBoolean = function(my_variable,my_value,calling_class,type_validator) {
			if (type_validator==this._YESNO) {
				if (my_value=='yes') return true;
				else if (my_value=='no') return false;
				else throw new Error('attribute '+my_variable+' must be "yes" or "no". Value '+my_value+' encountered instead for class '+calling_class);
			}
			else if (type_validator==this._TRUEFALSE) {
				if (my_value=='true') return true;
				else if (my_value=='false') return false;
				else throw new Error('attribute '+my_variable+' must be "true" or "false". Value '+my_value+' encountered instead for class '+calling_class);
			}
			else throw new Error('invalid boolean validator called by '+calling_class);
		}

		//TODO: verify if validator_type is one instance of the variables _ISxxx
		this.setValidator = function(validator_type) {
			_validator_type = validator_type;
		}

		this.applyValidator = function() {
			switch (_validator_type) {
				case this._ISBOOLEAN:
					return isBoolean(_value_to_validate);
				break;
				case this._ISNUMBER:
					return isNumber(_name_of_attribute,_value_to_validate,_calling_class);
				break;
				case this._ISBOOLEANYESNO:
					return isBoolean(_value_to_validate);
				break;
				case this._ISBOOLEANTRUEFALSE:
					return isBoolean(_value_to_validate);
				break;
			}

		}
	}

});


 
