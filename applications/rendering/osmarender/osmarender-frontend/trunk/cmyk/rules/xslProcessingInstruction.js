dojo.provide("cmyk.rules.xslProcessingInstruction");

dojo.require("cmyk.rules.ruleFileMember");

/**
	@lends cmyk.rules.xslProcessingInstruction
*/

dojo.declare("cmyk.rules.xslProcessingInstruction",cmyk.rules.ruleFileMember,{
	/** 
	      @constructs
	      @class A class that represent an XSL Processing Instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.ruleFileMember
	*/
	constructor: function(node) {
		var _target = "";
		var _data = "";
		var xmlNodeRead = node;
		var _xmlNodeWrite = null;

		_target = node.target;
		_data = node.data;

		this.getTarget = function() {
			return dojo.clone(_target);
		}

		this.getData = function() {
			return dojo.clone(_data);
		}

		this.write = function(xmlNode) {
//console.debug("sto creando il processing instruction con target: "+_target+" e data: "+_data);
			xmlNode.createProcessingInstruction(_target,_data);
//console.debug("ho creato il processing instruction");
		}
	},
});
