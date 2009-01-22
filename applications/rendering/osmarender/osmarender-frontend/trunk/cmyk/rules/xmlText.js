dojo.provide("cmyk.rules.xmlText");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.xmlText
*/

dojo.declare("cmyk.rules.xmlText",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A class that represent an XML text. Needed to format again properly
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(text) {
		var _text = "";

		_setText = function(text) {
			_text = dojo.clone(text);
		}

		_setText(text);

		this.getText = function() {
			return dojo.clone(_text);
		}
	}
});
