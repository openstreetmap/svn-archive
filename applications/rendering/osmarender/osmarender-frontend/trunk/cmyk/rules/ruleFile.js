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
					if (attribute.name=="class") {
						dojo.forEach(attribute.value, function(class,index,array) {
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
					var member_node=xmlNode;
					if (member.declaredClass!="cmyk.rules.rulesSection") {
						member.write();
						member_node = member.getXmlNodeWrite();
					}
					var write_iteration = function(member,member_node) {
						dojo.forEach(member.children,function(inner_member,index,array) {
							inner_member.write();
							inner_member_node = inner_member.getXmlNodeWrite();
							member_node.appendChild(inner_member_node);
							write_iteration(inner_member,inner_member_node);
						});
					}
//					xmlNode.appendChild(member_node);
					write_iteration(member,member_node);
				}
			});
//https://developer.mozilla.org/En/Parsing_and_serializing_XML
var xml_string = XML((new XMLSerializer()).serializeToString(ruleFileRaw_output)).toXMLString();
			//console.debug(xml_string);
/*
http://straxus.javadevelopersjournal.com/creating_a_mozillafirefox_drag_and_drop_file_upload_script_p.htm
http://www.captain.at/programming/xul/
https://developer.mozilla.org/Special:Search?search=save+file&type=fulltext&go=Search
https://developer.mozilla.org/index.php?title=En/Code_snippets/File_I%2F%2FO
http://jslib.mozdev.org/
http://kb.mozillazine.org/Io.js
*/
netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
var nsIFilePicker = Components.interfaces.nsIFilePicker;
var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance(nsIFilePicker);
fp.init(window, "Select a File", nsIFilePicker.modeSave);
var res = fp.show();
if (res == nsIFilePicker.returnOK){
  var file = fp.file;
  file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
  var outputStream = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance( Components.interfaces.nsIFileOutputStream );
outputStream.init( file, 0x04 | 0x08 | 0x20, 420, 0 );
	var output = xml_string;
	var result = outputStream.write( output, output.length );
	outputStream.close();

}

		}
	}


	
});
