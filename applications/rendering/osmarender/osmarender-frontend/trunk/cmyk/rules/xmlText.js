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
	constructor: function(node) {
		var _text = "";
		var xmlNodeRead = node;
		var _xmlNodeWrite = null;

		_setText = function(text) {
			_text = dojo.clone(text);
		}

		_setText(node);

		this.getText = function() {
			return dojo.clone(_text);
		}

/*		_write = function(xmlNode) {
			xmlNode.createTextNode(_text);
		}*/

		this.write = function() {
			_xmlNodeWrite = document.createTextNode("",_text);
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	}
});
