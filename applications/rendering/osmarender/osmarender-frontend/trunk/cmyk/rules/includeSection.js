dojo.provide("cmyk.rules.includeSection");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.includeSection
*/

dojo.declare("cmyk.rules.includeSection",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A include
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		this._mytag = "include";
		var _ref = "";
		var _xmlNodeRead = node;
		var _xmlNodeWrite = null;

		_ref = node.getAttribute("ref");

		this.write = function() {
			var my_node = document.createElementNS("",this._mytag);
			my_node.setAttribute("ref",_ref);
			_xmlNodeWrite = my_node;
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}

	},
});
