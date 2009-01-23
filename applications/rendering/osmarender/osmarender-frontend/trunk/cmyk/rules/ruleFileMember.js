dojo.provide("cmyk.rules.ruleFileMember");

dojo.require("cmyk.rules.xmlComment");
dojo.require("cmyk.rules.xmlText");
dojo.require("cmyk.rules.Rule");
dojo.require("cmyk.rules.Area");
dojo.require("cmyk.rules.Else");
dojo.require("cmyk.rules.Line");
dojo.require("cmyk.rules.selectorRule");
dojo.require("cmyk.rules.areaSymbol");
dojo.require("cmyk.rules.Circle");
dojo.require("cmyk.rules.Symbol");
dojo.require("cmyk.rules.textWithK");
dojo.require("cmyk.rules.wayMarker");

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
		var children = [];
		var string_debug="unknown type: "+node.nodeType+" name: "+node.nodeName+" value: "+node.nodeValue;
		//TODO: overload constructor for new node based on text

//TODO: All the work is in Line


		switch (node.nodeType) {
			case node.PROCESSING_INSTRUCTION_NODE:
				//TODO: save the XSL link inside?
			break;
			case node.COMMENT_NODE:
				string_debug="";
				children.push(new cmyk.rules.xmlComment(node.nodeValue));
			break;
			case node.ELEMENT_NODE:
				switch (node.nodeName) {
					case "rules":
						string_debug="";
					break;
						case "style":
							string_debug="";
						break;
						case "rule":
							string_debug="";
							if (node.getAttribute("s")==null) {
								children.push(new cmyk.rules.Rule(node));
							}
							else {
								children.push(new cmyk.rules.selectorRule(node));
							}
						break;
							case "area":
								string_debug="";
								children.push(new cmyk.rules.Area(node));
							break;
							case "else":
								string_debug="";
								children.push(new cmyk.rules.Else(node));
							break;
							case "line":
								string_debug="";
								children.push(new cmyk.rules.Line(node));
							break;
							case "tag":
							break;
							case "text":
								if (node.getAttribute("k")!=null) {
									string_debug="";
									children.push(new cmyk.rules.textWithK(node));
								}
							break;
							case "areaText":
							break;
							case "wayMarker":
								string_debug="";
								children.push(new cmyk.rules.wayMarker(node));
							break;
							case "circle":
								string_debug="";
								children.push(new cmyk.rules.Circle(node));
							break;
							case "symbol":
								string_debug="";
								children.push(new cmyk.rules.Symbol(node));
							break;
							case "areaSymbol":
								string_debug="";
								children.push(new cmyk.rules.areaSymbol(node));
							break;
							case "svg:marker":
							break;
							case "svg:pattern":
							break;
								case "svg:path":
								break;
								case "svg:rect":
								break;
								case "svg:g":
								break;
					case "include":
					break;
					case "defs":
					break;
				}
			break;
			case node.TEXT_NODE:
				string_debug="";
				children.push(new cmyk.rules.xmlText(node.nodeValue));
			break;
			default:
				throw new Error('unknown node type: '+node.nodeType+' for node: '+node.nodeName+' with value '+node.nodeValue);
		}
		dojo.forEach(node.childNodes,function(node,index,array) {
			children.push(new cmyk.rules.ruleFileMember(node));
		});
		if (string_debug!="") console.debug(string_debug);
//		console.dir(children);
	}
});
