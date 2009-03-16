dojo.provide("cmyk.rules.ruleFileMemberFactory");

dojo.require("cmyk.rules.Area");
dojo.require("cmyk.rules.Caption");
dojo.require("cmyk.rules.Circle");
dojo.require("cmyk.rules.Else");
dojo.require("cmyk.rules.Line");
dojo.require("cmyk.rules.pathText");
dojo.require("cmyk.rules.Rule");
dojo.require("cmyk.rules.selectorRule");
dojo.require("cmyk.rules.svgG");
dojo.require("cmyk.rules.svgMarker");
dojo.require("cmyk.rules.svgPath");
dojo.require("cmyk.rules.svgPattern");
dojo.require("cmyk.rules.svgRect");
dojo.require("cmyk.rules.svgSymbol");
dojo.require("cmyk.rules.Symbol");
dojo.require("cmyk.rules.Tag");
dojo.require("cmyk.rules.wayMarker");
dojo.require("cmyk.rules.xmlComment");
dojo.require("cmyk.rules.xmlText");
dojo.require("cmyk.rules.xslProcessingInstruction");

dojo.require("cmyk.rules.rulesSection");
dojo.require("cmyk.rules.includeSection");
dojo.require("cmyk.rules.cssSection");

/**
	@lends cmyk.rules.ruleFileMemberFactory
*/

dojo.declare("cmyk.rules.ruleFileMemberFactory",null,{
	constructor : function() {
		this.createRuleFileMember = function(node) {
			//TODO: overload this function for new node based on text
			var string_debug="unknown type: "+node.nodeType+" name: "+node.nodeName+" value: "+node.nodeValue;

			switch (node.nodeType) {
				case node.PROCESSING_INSTRUCTION_NODE:
					string_debug="";
					return new cmyk.rules.xslProcessingInstruction(node);
				break;
				case node.COMMENT_NODE:
					string_debug="";
					return new cmyk.rules.xmlComment(node);
				break;
				case node.TEXT_NODE:
					string_debug="";
					return new cmyk.rules.xmlText(node.nodeValue);
				break;
				case node.ELEMENT_NODE:
					switch (node.nodeName) {
						case "rules":
							return new cmyk.rules.rulesSection(node);
						break;
							case "style":
								string_debug="";
							break;
							case "rule":
								string_debug="";
								if (node.getAttribute("s")==null) {
									return new cmyk.rules.Rule(node);
								}
								else {
									return new cmyk.rules.selectorRule(node);
								}
							break;
								case "area":
									string_debug="";
									return new cmyk.rules.Area(node);
								break;
								case "else":
									string_debug="";
									return new cmyk.rules.Else(node);
								break;
								case "line":
									string_debug="";
									return new cmyk.rules.Line(node);
								break;
								case "caption":
									string_debug="";
									return new cmyk.rules.Caption(node);
								break;
								case "pathText":
									string_debug="";
									return new cmyk.rules.pathText(node);
								break;
								case "wayMarker":
									string_debug="";
									return new cmyk.rules.wayMarker(node);
								break;
								case "circle":
									string_debug="";
									return new cmyk.rules.Circle(node);
								break;
								case "symbol":
									string_debug="";
									return new cmyk.rules.Symbol(node);
								break;
								case "tag":
									string_debug="";
									return new cmyk.rules.Tag(node);
								break;
								case "svg:marker":
									string_debug="";
									return new cmyk.rules.svgMarker(node);
								break;
								case "svg:pattern":
									string_debug="";
									return new cmyk.rules.svgPattern(node);
								break;
								case "svg:symbol":
									string_debug="";
									return new cmyk.rules.svgSymbol(node);
								break;
									case "svg:path":
										string_debug="";
										return new cmyk.rules.svgPath(node);
									break;
									case "svg:rect":
										string_debug="";
										return new cmyk.rules.svgRect(node);
									break;
									case "svg:g":
										string_debug="";
										return new cmyk.rules.svgRect(node);
									break;
						case "include":
							return new cmyk.rules.includeSection(node);
						break;
						case "defs":
							string_debug="";
							return new cmyk.rules.cssSection(node);
						break;
					}
				break;
				default:
					throw new Error('unknown node type: '+node.nodeType+' for node: '+node.nodeName+' with value '+node.nodeValue);
			}
		}
	}
});
