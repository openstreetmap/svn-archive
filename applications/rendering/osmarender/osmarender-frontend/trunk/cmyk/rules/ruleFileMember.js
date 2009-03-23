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
                this._mytag = "generic_rule_file_member";
		var _xmlNodeWrite = null;
		var xmlNodeRead = node;
		var _ruleFileMemberFactory = new cmyk.rules.ruleFileMemberFactory();

		var children = this.children;

		dojo.forEach(node.childNodes,function(node,index,array) {
			children.push(_ruleFileMemberFactory.createRuleFileMember(node));
		});
		
		this.write = function() {
			_xmlNodeWrite = document.createElementNS("",this._mytag);
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	}
});
