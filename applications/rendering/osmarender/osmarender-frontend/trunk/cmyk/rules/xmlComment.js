dojo.provide("cmyk.rules.xmlComment");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.xmlComment
*/

dojo.declare("cmyk.rules.xmlComment",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A class that represent an XML comment. Necessary to keep comments needed for t@h
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		var _text = "";
		var xmlNodeRead = node;
		var _xmlNodeWrite = null;

		_setComment = function(text) {
			_text = dojo.clone(text);
		}

		_setComment(node.nodeValue);

		this.getComment = function() {
			return dojo.clone(_text);
		}

		this.write = function() {
			_xmlNodeWrite = document.createComment(_text);
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	},
});
