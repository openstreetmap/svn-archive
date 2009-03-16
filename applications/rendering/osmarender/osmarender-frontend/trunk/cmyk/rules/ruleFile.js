dojo.provide("cmyk.rules.ruleFile");

dojo.require("cmyk.rules.ruleFileAttr");
dojo.require("cmyk.rules.ruleFileMemberFactory");


/**
	@lends cmyk.rules.ruleFile
*/

dojo.declare("cmyk.rules.ruleFile",null,{
	/** 
	      @constructs
	      @class A class that represent a rule file
	      @memberOf cmyk.rules
	*/
	constructor: function(dataFileName) {
		var rulesFileRaw;
		var rulesFileRaw_output;
		var ruleFileAttributes;
		var ruleFileMembers = [];
		var ruleFileStyles;
		var ruleFileMemberFactory = new cmyk.rules.ruleFileMemberFactory();

		// associate classes object for every ruleFileMember
		_assignStyles = function(member) {
			if (member instanceof cmyk.rules.renderingInstruction) {
				var member_attributes = member.getAttributes();
				dojo.forEach(member_attributes,function(attribute,index,array) {
					if (attribute.getName()=="class") {
						dojo.forEach(attribute.getValue(), function(class,index,array) {
							try {
								var class_object = ruleFileStyles.getClassByName(class);
								if (class_object!=null) {
									member.addClass(class,class_object);
								}
							}
							catch (err) {
								console.debug(err.message);
							}

						});
					}
				});
			}
			dojo.forEach(member.children, function(child,index,array) {
				_assignStyles(child);
			});
		};


		_createModel = function() {
console.debug("Creating model "+new Date().getMinutes()+":"+new Date().getSeconds());
			// Create all objects to encapsulate rule file members
			dojo.forEach(rulesFileRaw.childNodes,function(node,index,array) {
				ruleFileMembers.push(ruleFileMemberFactory.createRuleFileMember(node));
			});

console.debug("Assigning CSS Section "+new Date().getMinutes()+":"+new Date().getSeconds());
			// Assign the CSS section a global variable inside the ruleFile object
			// This iteration can be useful for other kind of similar tasks
			dojo.forEach(ruleFileMembers, function (member,index,array) {
				var first_iteration = function(member) {
					dojo.forEach(member.children,function(inner_member,index,array) {
						if (inner_member instanceof cmyk.rules.cssSection) {
							ruleFileStyles = inner_member;
						}
						first_iteration(inner_member);
					});
				}
				first_iteration(member);
			});

console.debug("Assigning Styles "+new Date().getMinutes()+":"+new Date().getSeconds());
			dojo.forEach(ruleFileMembers,function(member,index,array) {
				_assignStyles(member);
			});

			rulesSection = rulesFileRaw.getElementsByTagName("rules")[0];
			var my_attributes = new Object();
console.debug("Creating global attributes "+new Date().getMinutes()+":"+new Date().getSeconds());
			with (rulesSection) {
				dojo.forEach(attributes, function(attribute, index, array) {
					if (attribute.nodeName!="xmlns:xlink" && attribute.nodeName!="xmlns:svg") {
						eval("my_attributes."+attribute.nodeName+" = \""+attribute.nodeValue+"\"");
					}
				});
				ruleFileAttributes = new cmyk.rules.ruleFileAttr(my_attributes);
			}
		}

		if (dataFileName == null) {
			//TODO: create a void rulefile
			console.debug("null");
		}
		else {
			dojo.xhrGet({
				url: dataFileName,
				handleAs: "xml",
				sync: true,
				load: function(data) {
					rulesFileRaw = data;
					_createModel();
				},
				error: function(data) {
					//TODO: Error handling
				}
			});
		}

		this.getGlobalAttributes = function() {
			return dojo.clone(ruleFileAttributes.getAttributes());
		}

		this.write = function() {
console.debug("Writing file "+new Date().getMinutes()+":"+new Date().getSeconds());
			ruleFileRaw_output = document.implementation.createDocument('','rules',null);
			ruleFileAttributes.write(ruleFileRaw_output.documentElement);
			var xmlNode = ruleFileRaw_output.documentElement;
			dojo.forEach(ruleFileMembers, function (member,index,array) {
				if (member.declaredClass=="cmyk.rules.xslProcessingInstruction") {
					member.write(ruleFileRaw_output);
				}
				else {
					member.write();
					var member_node = member.getXmlNodeWrite();
					var write_iteration = function(member,member_node) {
						dojo.forEach(member.children,function(inner_member,index,array) {
							inner_member.write();
							inner_member_node = inner_member.getXmlNodeWrite();
							member_node.appendChild(inner_member_node);
							write_iteration(inner_member,inner_member_node);
						});
					}
					xmlNode.appendChild(member_node);
					write_iteration(member,member_node);
				}
			});
//https://developer.mozilla.org/En/Parsing_and_serializing_XML
			console.debug(XML((new XMLSerializer()).serializeToString(ruleFileRaw_output)).toXMLString());
		}
	}


	
});
