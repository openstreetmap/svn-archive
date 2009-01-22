dojo.provide("cmyk.rules.ruleFile");

dojo.require("cmyk.rules.ruleFileAttr");
dojo.require("cmyk.rules.ruleFileMember");


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
		var ruleFileAttributes;
		var ruleFileMembers = [];

		_createModel = function() {
			dojo.forEach(rulesFileRaw.childNodes,function(node,index,array) {
				ruleFileMembers.push(new cmyk.rules.ruleFileMember(node));
			});
			console.dir(ruleFileMembers);
			rulesSection = rulesFileRaw.getElementsByTagName("rules")[0];
			var my_attributes = new Object();
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

	}


	
});
