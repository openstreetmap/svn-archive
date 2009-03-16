dojo.provide("cmyk.rules.ruleFileMember");

dojo.require("cmyk.rules.ruleFileMemberFactory");

/**
	@lends cmyk.rules.ruleFileMember
*/

dojo.declare("cmyk.rules.ruleFileMember",null,{
	/** 
	      @constructs
	      @class A class that represent a Rule File Member. This is the superclass of any member of the rule file
	      @memberOf cmyk.rules
	*/
//http://www.ibm.com/developerworks/web/library/wa-aj-dojo/
	constructor: function(node) {
		this.children = [];
		var _xmlNodeWrite = null;
		var xmlNodeRead = node;
		var _tagName = null;
		var _ruleFileMemberFactory = new cmyk.rules.ruleFileMemberFactory();

		var children = this.children;

		dojo.forEach(node.childNodes,function(node,index,array) {
			children.push(_ruleFileMemberFactory.createRuleFileMember(node));
		});
		
		this.write = function() {
console.debug("rulefilemember");
			_xmlNodeWrite = document.createElementNS("","generic_rule_file_member");
console.debug("fine rulefilemember");
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	}
});
