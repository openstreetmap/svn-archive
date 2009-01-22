  /**
 * @author Mario Ferraro <fadinlight@gmail.com>
 * http://osmarenderfrontend.wordpress.com
 * Released under GPL v2 or later
 */


//TODO: When refactoring remember to be compatible also with old xlink:href symbols/markers

PROGRAM_NAME="osmarender_frontend";
XHTML_NS="http://www.w3.org/1999/xhtml";
var rulesfile;
var rulesfilename;
var elements=new Array();
var osmfilename;
var osmfile;
var updateProgressBar;
var storemodel;

//Min and Max LatLon
var bounds = {
	lat : {
		min: 0.0,
		max: 0.0
	},
	lon : {
		min: 0.0,
		max: 0.0
	}
};

CMYK = function(rulesfilenamePar,osmfilenamePar,progressData,progressBar,callBackFunc) {
	rulesfilename=rulesfilenamePar;
	osmfilename=osmfilenamePar;
	updateProgressBar=progressBar;

// Load the rule file

rulesfile = function () {
	var xmlhttp = new XMLHttpRequest();  
	xmlhttp.open("GET", rulesfilename, false);  
	xmlhttp.send('');
	rulesfile=xmlhttp.responseXML;
	return rulesfile;
}();

markersfile = function() {
	var MARKERS_TAG = "include";
	if(rulesfile.getElementsByTagName(MARKERS_TAG).length) {
		var markersfilename = rulesfile.getElementsByTagName(MARKERS_TAG)[0].getAttribute("ref");
		markersfilename = rulesfilename.substring(0,rulesfilename.lastIndexOf("/")+1)+markersfilename;
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("GET", markersfilename, false);  
		xmlhttp.send('');
		markersfile=xmlhttp.responseXML;
		return markersfile;
	}
}();

osmfile = function () {
	var xmlhttp = new XMLHttpRequest();  
	xmlhttp.open("GET", osmfilename, false);  
	xmlhttp.send('');
	osmfile=xmlhttp.responseXML;
	return osmfile;
}();

setOsmFile(osmfilename);

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
this.symbols = function() {
	var symbols_array=new Array();
	plain_symbols = rulesfile.getElementsByTagName("symbol");
	area_symbols = rulesfile.getElementsByTagName("areaSymbol");
	dojo.forEach(plain_symbols,addContent);
	dojo.forEach(area_symbols,addContent);

	function addContent(symbol) {
		symbol_referenced = symbol.getAttribute("ref");
		if (symbol_referenced!=null) {
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.open("GET", SYMBOL_PATH+symbol_referenced+".svg", false);  
			xmlhttp.send('');
			symbols_array[symbols_array.length]=xmlhttp.responseXML.documentElement;
		}
	}
	return symbols_array;
//	rulesfile.getElementsByTagName("svg:symbol");
}();

// Analysing rule file to model rules

function RuleFile() {
	this.baseAttributes = new Object();
	this.bounds = new Object();
	this.childrenRules = new Array();
};

rulemodel = new RuleFile();

with (rulesfile.getElementsByTagName("rules")[0]) {
	for (var index=0; index<attributes.length; index++) {
		eval("rulemodel[\"baseAttributes\"][\""+attributes[index].name+"\"]=\""+attributes[index].value+"\"");
	}
}

// TODO: Check if there are bounds tag already

function SingleRule() {
	this.parent;
	this.type=new Array();
	this.keys=new Array();
	this.values=new Array();
	this.layer=new Array();
	this.childrenRules=new Array();
	this.render=new Array();
}

var toprogress=0,progressMax=0;
var global_tree_uuid=0;
store_model={
	label: "label",
	identifier: "id",
		items: [{id: ""+global_tree_uuid, top:true, label: "Rule Tree",children: []}]
};
createRuleModel(rulesfile.getElementsByTagName("rules")[0],rulemodel,store_model.items[0].children);
var id = setInterval(checkOkRule,10);


function checkOkRule() {
	if (progressMax!=0 && toprogress==progressMax) {
		clearInterval(id);
		toprogress=0;
		progressMax=0;
		parseKeyValuePairs(osmfile);
		id=setInterval(checkOkKeyValue,10);
	}
}

function checkOkKeyValue() {
	if (progressMax!=0 && toprogress==progressMax) {
		clearInterval(id);
		callBackFunc();
	}
}
function setOsmFile(osmfilename) {
	tags = rulesfile.getElementsByTagName("rules");
	tags[0].setAttribute("data",osmfilename);
}


function createRuleModel(dom,model,store) {
	progressData.step.maximum=(progressMax+=dom.childNodes.length);
	progressData.step.message="Processing rule file element "+toprogress+" of "+progressMax;
	updateProgressBar();
	var i=0;
// Thanks to http://jsninja.com/Timers
	setTimeout(function() {
	for (var index = i; index<dom.childNodes.length; index++) {
		progressData.step.progress=(++toprogress);
		progressData.step.message="Processing rule file element "+toprogress+" of "+progressMax;
		updateProgressBar();
		if (dom.childNodes[index].nodeName=="rule") {
			store[store.length] = {id: ""+(++global_tree_uuid), type: "rule", label:"keys: "+dom.childNodes[index].getAttribute("k")+" values: "+dom.childNodes[index].getAttribute("v")};
			if (dom.childNodes[index].hasChildNodes()) store[store.length-1].children = new Array();
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
			createRuleModel(dom.childNodes[index],temp,store[store.length-1].children);
		}
		else if (dom.childNodes[index].nodeName=="else") {
			store[store.length]={id: ""+(++global_tree_uuid), type: "else", label: "else"};
			if (dom.childNodes[index].hasChildNodes()) store[store.length-1].children = new Array();
			var temp = new SingleRule();
			temp.type = "else";
			model.childrenRules[model.childrenRules.length] = temp;
			createRuleModel(dom.childNodes[index],temp,store[store.length-1].children);
		}
		else if (dom.nodeName!="rules" && dom.childNodes[index].nodeType!=Node.TEXT_NODE && dom.childNodes[index].nodeType!=Node.COMMENT_NODE && dom.childNodes[index].nodeName!="defs") {
			store[store.length]={id: ""+(++global_tree_uuid), type: dom.childNodes[index].nodeName, label:dom.childNodes[index].nodeName+" class: "+dom.childNodes[index].getAttribute("class")};

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
		}i++;
	}
	if (i<dom.childNodes.length) setTimeout(arguments.callee,0);
    },0);
}

this.rulemodelresult=rulemodel;
//console.debug(this.rulemodelresult);

function parseKeyValuePairs(osmfile) {
	elements["nodes"] = new Array();
	elements["ways"] = new Array();

	nodes = osmfile.documentElement.selectNodes("//node");
	ways = osmfile.documentElement.selectNodes("//way");
	progressData.total.progress++;
	progressData.total.message="Processing Osm File";
	progressData.step.maximum=(progressMax+=nodes.length);
	progressData.step.message="Progressing osm file node "+toprogress+" of "+progressMax;
	updateProgressBar();
	
	var i=0;
	setTimeout(function() {
		for (var nodes_counter = i; nodes_counter < nodes.length; nodes_counter++) {
			// first iteration set max/min lat/lon
			if (nodes_counter==0) {
				bounds.lat.max = bounds.lat.min = parseFloat(nodes[nodes_counter].getAttribute("lat"));
				bounds.lon.max = bounds.lon.min = parseFloat(nodes[nodes_counter].getAttribute("lon"));
			}
			with (nodes[nodes_counter]) {
				var thisLat = parseFloat(getAttribute("lat"));
				var thisLon = parseFloat(getAttribute("lon"));
				if (thisLat>bounds.lat.max) bounds.lat.max=thisLat;
				if (thisLat<bounds.lat.min) bounds.lat.min=thisLat;
				if (thisLon>bounds.lon.max) bounds.lon.max=thisLon;
				if (thisLon<bounds.lon.min) bounds.lon.min=thisLon;
			}
			progressData.step.progress=(++toprogress);
			progressData.step.message="Processing osm file node "+toprogress+" of "+progressMax;
			updateProgressBar();
			var nodetags = nodes[nodes_counter].selectNodes("tag");
			var addmetags = elements["nodes"][nodes[nodes_counter].getAttribute("id")]=new Array();
			for (var nodetag_counter = 0; nodetag_counter < nodetags.length; nodetag_counter++) {
				addmetags[nodetags[nodetag_counter].getAttribute("k")] = nodetags[nodetag_counter].getAttribute("v");
			}i++;
		}if (i<nodes.length) setTimeout(arguments.callee,0);},0);

	for (var ways_counter = 0; ways_counter < ways.length; ways_counter++) {
		var waytags = ways[ways_counter].selectNodes("tag");
		var addmetags = elements["ways"][ways[ways_counter].getAttribute("id")]=new Array();
		for (var waytag_counter = 0; waytag_counter < waytags.length; waytag_counter++) {
			addmetags[waytags[waytag_counter].getAttribute("k")] = waytags[waytag_counter].getAttribute("v");
		}
	}
}
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
//TODO: To port into the data model
	setGlobalBooleanAttribute = function(attribute,show) {
		with (rulesfile.getElementsByTagName("rules")[0]) {
			if (show)
				setAttribute(attribute,"yes");
			else
				setAttribute(attribute,"no");
		}
	}
	
};

CMYK.prototype.getSymbols = function() {
	return this.symbols;
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


//TODO: doesn't work any more => probably the problem is the difference between the id hard-coded in the svg and the "ref" tag of the corresponding rule
CMYK.prototype.getRuleFromSymbol = function(rulemodel,symbol_url,rulestoreturn) {
	if (rulemodel.render && rulemodel.render.length) {
		for (renderRules in rulemodel.render) {
			if (rulemodel.render[renderRules].type =="symbol" && symbol_url==rulemodel.render[renderRules]["ref"].substring(1)) {
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

CMYK.prototype.getOsmFileName = function() {
	return osmfilename.substring(osmfilename.lastIndexOf("/"));
}

CMYK.prototype.getKeyValuePairs = function() {
	return elements;
}

CMYK.prototype.getBounds = function() {
	return bounds;
}

CMYK.prototype.getScale = function() {
	return this.getRuleModel().baseAttributes.scale;
}

CMYK.prototype.getTextAttenuation = function() {
	return this.getRuleModel().baseAttributes.textAttenuation;
}

CMYK.prototype.setScale = function(scale) {
	rulesfile.getElementsByTagName("rules")[0].setAttribute("scale",scale);
}

CMYK.prototype.setTextAttenuation = function(textAttenuation) {
	rulesfile.getElementsByTagName("rules")[0].setAttribute("textAttenuation",textAttenuation);
}

//TODO: The following functions must be ported to a data model

CMYK.prototype.setBounds = function(north,south,east,west) {
	bounds.lon.min=east;
	bounds.lon.max=west;
	bounds.lat.max=north;
	bounds.lat.min=south;
	with (rulesfile.getElementsByTagName("rules")[0]) {
		var bounds_tag;
		if (rulesfile.getElementsByTagName("bounds").length!=0) {
			bounds_tag = rulesfile.getElementsByTagName("bounds")[0];
		}
		else {
			XHTML_NS="";
			bounds_tag = createElementCB("bounds");
			XHTML_NS="http://www.w3.org/1999/xhtml";
		}
		bounds_tag.setAttribute("minlon",bounds.lon.min);
		bounds_tag.setAttribute("maxlon",bounds.lon.max);
		bounds_tag.setAttribute("maxlat",bounds.lat.max);
		bounds_tag.setAttribute("minlat",bounds.lat.min);
		appendChild(bounds_tag);
	}
}


CMYK.prototype.setShowScale = function(show) {
	if (typeof(show)!="boolean") throw new Error("not a boolean");
	setGlobalBooleanAttribute("showScale",show);
}

CMYK.prototype.setShowGrid = function(show) {
	if (typeof(show)!="boolean") throw new Error("not a boolean");
	setGlobalBooleanAttribute("showGrid",show);
}

CMYK.prototype.setShowBorder = function(show) {
	if (typeof(show)!="boolean") throw new Error("not a boolean");
	setGlobalBooleanAttribute("showBorder",show);
}

CMYK.prototype.setShowLicense = function(show) {
	if (typeof(show)!="boolean") throw new Error("not a boolean");
	setGlobalBooleanAttribute("showLicense",show);
}

CMYK.prototype.setShowInteractive = function(show) {
	if (typeof(show)!="boolean") throw new Error("not a boolean");
	setGlobalBooleanAttribute("interactive",show);
}

CMYK.prototype.attachSymbol = function(symbol_id,feature_key,feature_value,symbol_width,symbol_height,symbol_layer) {
	XHTML_NS="";
	rule_tag = createElementCB("rule");
//	XHTML_NS="http://www.w3.org/2000/svg";
	symbol_tag = createElementCB("symbol");
	XHTML_NS="http://www.w3.org/1999/xhtml";
	rule_tag.setAttribute("e","node");
	rule_tag.setAttribute("k",feature_key);
	rule_tag.setAttribute("v",feature_value);
	rule_tag.setAttribute("layer",symbol_layer);

//	symbol_tag.setAttribute("xlink:href","#"+symbol_id);
//	symbol_tag.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
// Senza di questo non viene inserito l'xlink nell'use del risultante svg
	symbol_tag.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href","#"+symbol_id);
	symbol_tag.setAttribute("width",symbol_width+"px");
	symbol_tag.setAttribute("height",symbol_height+"px");
	//TODO: parametrize this
	symbol_tag.setAttribute("transform","translate(-15,-15)");
	
	rule_tag.appendChild(symbol_tag);
	rulesfile.getElementsByTagName("rules")[0].appendChild(rule_tag);
}

CMYK.prototype.getRuleTree = function() {
	return store_model;
}

CMYK.prototype.getObjects = function() {
	var objects = {
		patterns: new Array(),
		markers: new Array(),
		symbols: new Array()
	}

	defs = rulesfile.getElementsByTagName("defs")[0];
	dojo.forEach(defs.getElementsByTagName("svg:pattern"),
		function(pattern,index,array) {
			objects.patterns.push(pattern);
		}
	);
	if (markersfile) {
		dojo.forEach(markersfile.getElementsByTagName("svg:marker"),
			function(marker,index,array) {
				objects.markers.push(marker);
			}
		);
	}
	dojo.forEach(this.symbols,
		function(symbol,index,array) {
			objects.symbols.push(symbol);
		}
	);
	return objects;
}