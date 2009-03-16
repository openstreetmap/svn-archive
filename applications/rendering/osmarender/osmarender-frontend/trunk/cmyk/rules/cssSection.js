dojo.provide("cmyk.rules.cssSection");

/**
	@lends cmyk.rules.cssSection
*/

dojo.declare("cmyk.rules.cssSection",null,{
	/** 
	      @constructs
	      @class CSS Section
	      @memberOf cmyk.rules
	*/
	constructor: function(node) {
		var _xmlNodeRead = node;
		var _xmlNodeWrite = null;

		// CSS Parser. Thanks to http://www.senocular.com/index.php?id=1.289
		var parser = new CSSParser("styles",node);
		var styles_objects = new Array();
		var styles = parser.getStyles();
		dojo.forEach(styles,function(style,index,array) {
			dojo.forEach(style.selectors[0].singleSelectors[0].classes.values,function(style_name,index,array) {
				styles_objects[style_name]=style;
			});
		});

		this.getClassByName = function(name) {
			if (name=="no-bezier") return null;
			if (styles_objects[name]!=undefined) {
				return styles_objects[name];
			}
			throw new Error("unknown class name: "+name);
		}

		/*_write = function(xmlDoc) {
			return xmlDoc.createTextNode(parser.getWriterString(styles));
		};*/

		this.write = function() {
			_xmlNodeWrite = document.createElementNS("","css");
		}

		this.getXmlNodeWrite = function() {
			return _xmlNodeWrite;
		}
	},

});
