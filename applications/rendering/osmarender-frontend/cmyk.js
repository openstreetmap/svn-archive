/**
 * @author Mario Ferraro <fadinlight@gmail.com>
 * http://osmarenderfrontend.wordpress.com
 * Released under GPL v2 or later
 */

PROGRAM_NAME="osmarender_frontend";
XHTML_NS="http://www.w3.org/1999/xhtml";
var rulesfile;
var server;
// Models for faceting features
CMYK = function(rulesfilename) {
	var rulesfilename=rulesfilename;
	
	function elementTypes() {
		// if the object that calls the function is not already an instance return a new object. This example provide a rough overloading implementation of object constructors
		if (!(this instanceof arguments.callee)) {
			// if the function is not called with an argument (e.g. elements.WAY) then only a reference to the internal representation is wanted
			if (arguments.length==0) return new elementTypes();
			if (arguments.length==1) {
				// if the function is called with an argument, which is an array of types (e.g. [elements.WAY,elements.NODE]), then an object that should be included in a Feature object is wanted. The object returned will call itself the section "if (arguments.length==1)" below
				if (!(arguments[0] instanceof Array)) throw new Error("not an array");
				return new elementTypes(arguments[0]);
			}
			throw new Error("usage");
		}
	
		node_encoded = {
			uuid: 0,
			string: "NODE"
		}
		way_encoded = {
			uuid: 1,
			string: "WAY"
		}
		area_encoded = {
			uuid: 2,
			string: "AREA"
		};
	
		// Public variables that references names of functions that returns the integers
		this.NODE = function() {
			return node_encoded;
		}();
		this.WAY = function() {
			return way_encoded;
		}();
		this.AREA = function() {
			return area_encoded;
		}();
		
		// Private variable that will store the types compatible with the feature
	
		// If this is an object that should be included in a feature
		if (arguments.length==1) {
			if (!(arguments[0] instanceof Array)) throw new Error("not an array");
			// Unnullify the _mytype private variable, it will be an array of types (for example highway=pedestrian can be applied to ways and areas as well)
			var _mytype=new Array();
	
			// Iterate the array passed as the argument of the function
			for (type_defined in arguments[0]) {
				// Create a new object in the array
				_mytype[_mytype.length] = {
					// Assign the value of the object. This is achieved by eval() the type in the argument and adding "()" to call the desired function
					value: arguments[0][type_defined].uuid,
					// Create a value that remember the type requested
					string: arguments[0][type_defined].string,
					toString: function(){
						return this.string;
					}
				}
			}
		// This is a public function that returns the types associated to the feature
			this.getTypes = function() {
				//returning a clone object to avoid accidental overwriting
				return clone(_mytype);
			}
		}
	}
	elements = elementTypes();

	function Features_Facet(name) {
		// If I'm not an already instantiated function, return a new instantiated object
		if (!(this instanceof arguments.callee)) {
			return new Features_Facet(name);
		}
		// Set my name, this would be a private variable, so it's not declared as this.name
		var _name = name;
		var _categories = new Array();
		
	
		// Public function to retrieve facet's name
		this.getName = function() {
			return _name;
		}
		
		this.addCategory = function(category) {
			if (!(category instanceof Features_Category)) throw new Error("This param should be an instance of Features_Category");
			_categories[_categories.length]=category;
		}
		
		// Some private function, for later use
		function getName2() {
			return this._name;
		}
	};

	function Features_Category(name,facet,category) {
		if (!(facet instanceof Features_Facet)) throw new Error("Second argument must be an instance of Features_Facet");
		if (!(this instanceof arguments.callee)) {
			return new Features_Category(name,facet,category);
		}
		var _name = name;
		var _facet = facet;
		var _features = new Array();
		var _supercategory;
		var _subcategories = new Array();
	
		this.getName = function() {
			return clone(_name);
		}
	
		facet.addCategory(this);
	
		if (category!=undefined) {
			_supercategory=category;
			category.addSubCategory(this);
		}
		
		this.getSuperCategory = function() {
			if (!!_supercategory) {
				return clone(_supercategory);
			}
		}
	
		this.addSubCategory = function(category) {
			if (!(category instanceof Features_Category)) throw new Error("Argument must be an instance of Features_Category");
			_subcategories[_subcategories.length]=category;
		}
	
		this.addFeature = function(feature) {
			if (!(feature instanceof Features_Feature)) throw new Error("Argument must be an instance of Features_Feature");
			_features[_features.length]=feature;
		}
	}

	function Features_Feature(name,value,category) {
		if (!(this instanceof arguments.callee)) {
			return new Features_Feature(name,value,category);
		}
		
		var _name = name;
		var _value = value;
		
		this.getName = function() {
			return _name;
		}
		
		this.getValue = function() {
			return _value;
		}

		category.addFeature(this);
	}

/* 	Define a structure that includes all well-known key/value rendered by
	Osmarender. The structure will store key/value pairs and type
*/

	function osmarenderFeature(key,value,type,category) {
		if (!(type instanceof elementTypes)) throw new Error("errore di tipo elemento");
		if (!(this instanceof arguments.callee)) {
			return new osmarenderFeature(key,value,type);
		}
	
		var _key=key;
		var _value=value;
		var _type=type;
		var _category=category
		
		this.getTypeElement = function() {
			return _type.getTypes();
		}
	
		this.getTypeObject = function() {
			return clone(_type);
		}
		
		this.getKey = function() {
			return clone(_key);
		}
	
		this.getValue = function() {
			return clone(_value);
		}
	
		this.getCategoryName = function() {
			return category.getName();
		}
	
		this.getCategoryObject = function() {
			return clone(_category);
		}
	}

/* 	We define a structure for category store: this is a sort of associative array
	The array is structured as: Category Name => tag[array(value1,value2,...)]
*/

//var osmafeat = new osmarenderFeature("highway","motorway",elementTypes([elements.WAY,elements.NODE]),category_highway);

//TODO: Iterazione per creazione oggetti; documentazione,debug,debug message,
//test,package, blog, documentazione api su wiki

// this contains heredoc javascript syntax, thanks to http://www.scribd.com/doc/1026312/Javascript-Shorthand-QuickReference
var wiki_facet = Features_Facet("Wiki");
var wiki_facet_physical = Features_Category("Physical",wiki_facet);
var wiki_facet_physical_highway = Features_Category("Highway Tag",wiki_facet,wiki_facet_physical);
var wiki_facet_physical_cycleway = Features_Category("Cycleway Tag",wiki_facet,wiki_facet_physical);
var wiki_facet_physical_tracktype = Features_Category("Tracktype Tag",wiki_facet,wiki_facet_physical);

//aggiungere la gestione di sopra e sottocategorie: Physical=>highway, Non Physical=>route
//aggiungere relazioni

var features_classification = {
	"highway" : {
		description: "",
		tags : {
			"motorway": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway],
				description: (<r><![CDATA[A restricted access major divided highway, normally with 2 or more running lanes plus emergency hard shoulder. Equivalent to the Freeway, Autobahn etc..]]></r>).toString(),
				wiki_page: "http://wiki.openstreetmap.org/index.php/Tag:highway%3Dmotorway"
			},
			"motorway_link" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"trunk" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"trunk_link" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"primary" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"primary_link" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"secondary" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"tertiary" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"unclassified" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"track" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"residential" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"living_street" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"service" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"bridleway" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"cycleway" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"footway" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"pedestrian" : {
				types: [elements.WAY,elements.AREA],
				categories: [wiki_facet_physical_highway]
			},
			"steps" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"bus_guideway" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
			"mini_roundabout" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"stop" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"traffic_signals" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"crossing" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"gate" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"stile" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"cattle_grid" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"toll_booth" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"incline" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"viaduct" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"motorway_junction" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"services" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"ford" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"bus_stop" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"turning_circle" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"stop" : {
				types: [elements.NODE],
				categories: [wiki_facet_physical_highway]
			},
			"construction" : {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			}

		}
	},
	"junction" : {
		description: "",
		tags : {
			"roundabout": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_highway]
			},
		}
	},
	"cycleway" : {
		description: "",
		tags : {
			"lane": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_cycleway]
			},
			"track": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_cycleway]
			},
			"opposite_lane": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_cycleway]
			},
			"opposite_track": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_cycleway]
			},
			"opposite": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_cycleway]
			}
		}
	},
	"tracktype" : {
		description: "",
		tags : {
			"grade1": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_tracktype]
			},
			"grade2": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_tracktype]
			},
			"grade3": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_tracktype]
			},
			"grade4": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_tracktype]
			},
			"grade5": {
				types: [elements.WAY],
				categories: [wiki_facet_physical_tracktype]
			}
		}
	}
}


//Construct a variable that would contains all the tree for facets
var osmafeats = new Array();
for (var key in features_classification) {
	var tags = features_classification[key].tags;
	for (var value in tags) {
		osmafeats[osmafeats.length]=new osmarenderFeature(key,value,elementTypes(tags[value].types),tags[value].categories[0]);
	}
}



//This could be a future test case
/*var stringtoprint="";
for (var object in osmafeats) {
	supercategoryparsed = osmafeats[object].getCategoryObject().getSuperCategory().getName();
	if (supercategoryparsed) {
		stringtoprint+="macrocategory: "+supercategoryparsed+",";
	}
	else {
		stringtoprint+="macrocategory: no one,";
	}
	stringtoprint+=" category: "+osmafeats[object].getCategoryName()+" key: "+osmafeats[object].getKey()+" value: "+osmafeats[object].getValue()+"\r\n";
}*/
//alert(stringtoprint);

// Load the rule file

rulesfile = function () {
	var scripts = document.getElementsByTagName("script");

	for (script in scripts) {
		if (typeof(scripts[script])=="object" && scripts[script].getAttribute("cmyk_server")!=null && scripts[script].getAttribute("cmyk_server")!=undefined) {
			server = scripts[script].getAttribute("cmyk_server");
		}
	}

	var xmlhttp = new XMLHttpRequest();  
	xmlhttp.open("GET", server+rulesfilename, false);  
	xmlhttp.send('');  
	rulesfile=xmlhttp.responseXML;
	return rulesfile;
}();

function getDefs(rulefile) {
	if (!(rulefile instanceof XMLDocument)) throw new Error("argument must be an XMLDocument");
	var DEFS_TAG="defs";
	defs_section = rulefile.getElementsByTagName(DEFS_TAG)[0];
	return defs_section;
}

this.getCssSectionFromRules = function(rulesfile) {
	defssection = getDefs(rulesfile);
	if (!(defssection instanceof Element)) throw new Error("argument must be an Element");
	var STYLE_TAG="style";
	style_section = defssection.getElementsByTagName(STYLE_TAG)[0];
	return style_section;
}

// get CSS text
var css = getDefs(rulesfile);
// CSS Parser. Thanks to http://www.senocular.com/index.php?id=1.289
this.Parser = new CSSParser("styles",css);
this.Styles = this.Parser.getStyles();
// Look for CSS Parsed classes, test case
/*stylestoprint="";
for (var stylesobject in Styles) {
	stylestoprint += Styles[stylesobject].selectors[0].singleSelectors[0].classes.values+"\r\n";
}
alert(stylestoprint);*/

// Create symbols links/data

//Necessary change for firefox3... handles SVG as SVG not as XML/DOM
//this.symbols = css.getElementsByTagName("symbol");
this.symbols = rulesfile.getElementsByTagName("symbol");

// Analysing rule file to model rules

function RuleFile() {
	this.baseAttributes = new Object();
	this.childrenRules = new Array();
};

rulemodel = new RuleFile();

with (rulesfile.getElementsByTagName("rules")[0]) {
	for (var index=0; index<attributes.length; index++) {
		eval("rulemodel[\"baseAttributes\"][\""+attributes[index].name+"\"]=\""+attributes[index].value+"\"");
	}
}

function SingleRule() {
	this.parent;
	this.type=new Array();
	this.keys=new Array();
	this.values=new Array();
	this.layer=new Array();
	this.childrenRules=new Array();
	this.render=new Array();
}

createRuleModel(rulesfile.getElementsByTagName("rules")[0],rulemodel);

function createRuleModel(dom,model) {
	for (var index=0; index<dom.childNodes.length; index++) {
		if (dom.childNodes[index].nodeName=="rule") {
			var temp = new SingleRule();
			for (var a=0; a<dom.childNodes[index].attributes.length; a++) {
				with (dom.childNodes[index].attributes[a]) {
					switch (name) {
						case "e":
							temp.type = value.split("|");
							break;
						case "k":
							temp.keys = value.split("|");
							break;
						case "v":
							temp.values = value.split("|");
							break;
						default:
							temp[name] = value;
					}
				}
			}
			model.childrenRules[model.childrenRules.length] = temp;
			createRuleModel(dom.childNodes[index],temp);
		}
		else if (dom.childNodes[index].nodeName=="else") {
			var temp = new SingleRule();
			temp.type = "else";
			model.childrenRules[model.childrenRules.length] = temp;
			createRuleModel(dom.childNodes[index],temp);
		}
		else if (dom.childNodes[index].nodeType!=Node.TEXT_NODE && dom.childNodes[index].nodeType!=Node.COMMENT_NODE && dom.childNodes[index].nodeName!="defs") {
			newIndex = model.render.length;
			model.render[newIndex]=new Object();
			model.render[newIndex].type=dom.childNodes[index].nodeName;
			for (var a=0; a<dom.childNodes[index].attributes.length; a++) {
				with (dom.childNodes[index].attributes[a]) {
					switch(name) {
						case "class":
							model.render[newIndex][name] = value.split(" ");
							break;
						case "mask-class":
							model.render[newIndex][name] = value.split(" ");
							break;
						default:
							model.render[newIndex][name] = value;
					}
				}
			}
		}
	}
}

this.rulemodelresult=rulemodel;
//console.debug(this.rulemodelresult);


// Utility functions

/**
 * 
 * @param {String} element
 * 
 */
	function createElementCB(element) {
		with (document) {
			if (typeof createElementNS != 'undefined') {
				return createElementNS(XHTML_NS, element);
			}
			if (typeof createElement != 'undefined') {
				return createElement(element);
			}
		}
		return false;
	}
		
/*
 * Utility to clone objects
 * Thanks to http://keithdevens.com/weblog/archive/2007/Jun/07/javascript.clone
 */

	function clone(arr) {
		var i, _i, temp;
		if( typeof arr !== 'object' ) {
			return arr;
		}
		else {
			if (arr.concat) {
				temp = [];
				for (i = 0, _i = arr.length; i < _i; i++) {
					temp[i] = arguments.callee(arr[i]);
				}
			}
			else {
				temp = {};
				for (i in arr) {
					temp[i] = arguments.callee(arr[i]);
				}
			}       
			return temp;
		}
	}
	
};

CMYK.prototype.getSymbols = function() {
	return this.symbols;
}

CMYK.prototype.setOsmFile = function(osmfilename) {
	tags = rulesfile.getElementsByTagName("rules");
	tags[0].setAttribute("data",osmfilename);
}

CMYK.prototype.getRulesFile = function() {
	return rulesfile;
}

CMYK.prototype.getRuleModel = function() {
	return this.rulemodelresult;
}

CMYK.prototype.getRuleFromClass = function(rulemodel,CSSclassname,rulestoreturn) {
	if (rulemodel.render && rulemodel.render.length) {
		for (renderRules in rulemodel.render) {
			var classesAssociated = rulemodel.render[renderRules].class;
			for (class in classesAssociated) {
				if (classesAssociated[class]==CSSclassname) {
					rulestoreturn[rulestoreturn.length]=rulemodel;
				}
			}
			var maskclassesAssociated = rulemodel.render[renderRules]["mask-class"];
			for (class in maskclassesAssociated) {
				if (maskclassesAssociated[class]==CSSclassname) {
					rulestoreturn[rulestoreturn.length]=rulemodel;
				}
			}
		}
	}
	if (rulemodel.childrenRules && rulemodel.childrenRules.length) {
		for (childrenIndex in rulemodel.childrenRules) {
			this.getRuleFromClass(rulemodel.childrenRules[childrenIndex],CSSclassname,rulestoreturn);
		}
		return;
	}
}

CMYK.prototype.getClassFromRule = function(rulemodel,my_key,my_value,csstoreturn) {
	if (rulemodel.keys && rulemodel.keys.length) {
		var key_found=false;
		for (single_key in rulemodel.keys) {
			if (my_key == rulemodel.keys[single_key]) key_found=true;
		}
		if (key_found) {
			for (single_value in rulemodel.values) {
				if (my_value == rulemodel.values[single_value] || rulemodel.values[single_value]=="*") {
					if (rulemodel.render && rulemodel.render.length) {
						for (renderRules in rulemodel.render) {
							var classesAssociated = rulemodel.render[renderRules].class;
							for (class in classesAssociated) {
								csstoreturn[csstoreturn.length]=classesAssociated[class];
							}
							var maskclassesAssociated = rulemodel.render[renderRules]["mask-class"];
							for (class in maskclassesAssociated) {
								csstoreturn[csstoreturn.length]=maskclassesAssociated[class];
							}
						}
					}
				}
			}
		}
	}
	if (rulemodel.childrenRules && rulemodel.childrenRules.length) {
		for (childrenIndex in rulemodel.childrenRules) {
			this.getClassFromRule(rulemodel.childrenRules[childrenIndex],my_key,my_value,csstoreturn);
		}
		return;
	}
}

CMYK.prototype.getRuleFromSymbol = function(rulemodel,symbol_url,rulestoreturn) {
	if (rulemodel.render && rulemodel.render.length) {
		for (renderRules in rulemodel.render) {
			if (rulemodel.render[renderRules].type =="symbol" && symbol_url==rulemodel.render[renderRules]["xlink:href"].substring(1)) {
				rulestoreturn[rulestoreturn.length]=rulemodel;
			}
		}
	}
	if (rulemodel.childrenRules && rulemodel.childrenRules.length) {
		for (childrenIndex in rulemodel.childrenRules) {
			this.getRuleFromSymbol(rulemodel.childrenRules[childrenIndex],symbol_url,rulestoreturn);
		}
		return;
	}
}

CMYK.prototype.getSymbolFromRule = function(rulemodel,my_key,my_value,symboltoreturn) {
	if (rulemodel.keys && rulemodel.keys.length) {
		var key_found=false;
		for (single_key in rulemodel.keys) {
			if (my_key == rulemodel.keys[single_key]) key_found=true;
		}
		if (key_found) {
			for (single_value in rulemodel.values) {
				if (my_value == rulemodel.values[single_value] || rulemodel.values[single_value]=="*") {
					if (rulemodel.render && rulemodel.render.length) {
						for (renderRules in rulemodel.render) {
							if (rulemodel.render[renderRules].type =="symbol") {
								symboltoreturn[symboltoreturn.length]=rulemodel.render[renderRules]["xlink:href"].substring(1);
							}
						}
					}
				}
			}
		}
	}
	if (rulemodel.childrenRules && rulemodel.childrenRules.length) {
		for (childrenIndex in rulemodel.childrenRules) {
			this.getSymbolFromRule(rulemodel.childrenRules[childrenIndex],my_key,my_value,symboltoreturn);
		}
		return;
	}
}

CMYK.prototype.getStylesFromRuleFile = function() {
	return this.Styles;
}

CMYK.prototype.setStyle = function() {
	var string = this.Parser.getWriterString(this.Styles);
	actual_style = this.getCssSectionFromRules(rulesfile);
	while (actual_style.hasChildNodes()) {
		actual_style.removeChild(actual_style.firstChild);
	}
	da_inserire = document.createTextNode(string);
	actual_style.appendChild(da_inserire);
}

CMYK.prototype.setSingleStyle = function(class,property,editValue) {
	for (object in this.Styles) {
		with (this.Styles[object].selectors[0]) {
			if (singleSelectors[0].classes.values==class) {
				for (inner_property in properties.values) {
					if (typeof(inner_property) != "function") {
						if (inner_property==property) {
							properties.values[inner_property]=editValue;
						}
					}
				}
			}
		}
	}
	this.setStyle();
}

CMYK.prototype.addSingleStyle = function(class,property,editValue) {
	for (object in this.Styles) {
		with (this.Styles[object].selectors[0]) {
			if (singleSelectors[0].classes.values==class) {
				properties.values[property]=editValue;
			}
		}
	}
	this.setStyle();
}

CMYK.prototype.deleteSingleStyle = function(class,property) {
	for (object in this.Styles) {
		with (this.Styles[object].selectors[0]) {
			if (singleSelectors[0].classes.values==class) {
				delete properties.values[property];
			}
		}
	}
	this.setStyle();
}
