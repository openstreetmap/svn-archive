/**
 * @author Mario Ferraro <fadinlight@gmail.com>
 * http://osmarenderfrontend.wordpress.com
 * Released under GPL v2 or later
 */

var cmyk;

var classesAndProperties = new Array();

viewPropertiesFromClass = function(key) {
	if (key=="osmarender_frontend:null") return false;

	var array_da_aggiornare = new Array();
	cmyk.getRuleFromClass(cmyk.getRuleModel(),key,array_da_aggiornare);
	
	console.debug(array_da_aggiornare);
	
	var div_result = document.getElementById("result_process_rules");

	var todelete = document.getElementById("result_process_css_key");
	if (todelete) div_result.removeChild(document.getElementById("result_process_css_key"));


/*	if (document.getElementById("label_feature_value")) {
		div_result.removeChild(document.getElementById("label_feature_value"));
		div_result.removeChild(document.getElementById("select_feature_value"));
		div_result.removeChild(document.getElementById("label_feature_rule"));
		div_result.removeChild(document.getElementById("select_feature_rule"));
	}*/

	var div_properties = createElementCB("div");
	div_properties.setAttribute("id","result_process_css_key");
	div_result.appendChild(div_properties);

	refreshProperties();

	propertiesToPrint = classesAndProperties[key];

	var dl_container = createElementCB("dl");
	div_properties.appendChild(dl_container);

	for(single_property in propertiesToPrint) {
		
		var dt_container = createElementCB("dt");
		dl_container.appendChild(dt_container);
		
		var label_container = createElementCB("label");
		label_container.setAttribute("id","label_property_"+single_property);
		label_container.setAttribute("for",single_property);
		var label = document.createTextNode(single_property+": ");
		label_container.appendChild(label);
		dt_container.appendChild(label_container);
		
		var dd_container = createElementCB("dd");
		dl_container.appendChild(dd_container);

		var text_container = createElementCB("input");
		text_container.setAttribute("id",single_property);
		text_container.setAttribute("value",propertiesToPrint[single_property]);
		dd_container.appendChild(text_container);
		
		if ((single_property=="marker-end" || single_property=="marker-mid" || single_property=="marker-start" || single_property=="fill") && propertiesToPrint[single_property].substring(0,3)=="url") {
			var svg_url=propertiesToPrint[single_property].split("(")[1].split("#")[1].split(")")[0];
			if (cmyk.getRulesFile().getElementById(svg_url)) {
				var svg_marker = cmyk.getRulesFile().getElementById(svg_url);
				var svg_container;
				if (typeof document.createElementNS != 'undefined') {
					svg_container = document.createElementNS("http://www.w3.org/2000/svg","svg");
				}
				else {
					svg_container = createElementCB("svg");
				}
				for (var a=0; a<svg_marker.attributes.length; a++) {
					svg_container.setAttribute(svg_marker.attributes[a].name,svg_marker.attributes[a].value);
				}
				svg_container.setAttribute("width","20");
				svg_container.setAttribute("height","20");
				for (var a=0; a<svg_marker.childNodes.length; a++) {
					if (svg_marker.childNodes[a].nodeType!=Node.TEXT_NODE) {
						svg_container.appendChild(svg_marker.childNodes[a].cloneNode(true));
					}
				}
				dd_container.appendChild(svg_container);
			}
		}
		
		dl_container.appendChild(createElementCB("br"));

	}
	/*	for (pippo in classesAndProperties) {
		alert(pippo); //untagged-segments
		for (pluto in classesAndProperties[pippo]) {
			alert(pluto); //stroke-width
			alert(classesAndProperties[pippo][pluto]); //0.5pixels
		}
	}*/


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

loadOsmAndRules = function() {
	var rulesfilename_written = document.getElementById("rules_file_name_written").value;
	var rulesfilename_selected = document.getElementById("rules_file_name").value;

	var rulesfilename="osm-map-features-z13.xml";
	
	if (rulesfilename_selected == "") {
		if (rulesfilename_written != "") {
			rulesfilename = rulesfilename_written;
		}
	}
	else {
		rulesfilename = rulesfilename_selected;
	}

//	var rulesfilename = document.getElementById("rules_file_name").value;
	cmyk = new CMYK(rulesfilename);

	var div_result = document.getElementById("result_process_rules");

	while (div_result.hasChildNodes()) {
		div_result.removeChild(div_result.firstChild);
	}

	var label_container = createElementCB("label");
	label_container.setAttribute("for","select_class");
	var label = document.createTextNode("Select a CSS class: ");
	label_container.appendChild(label);
	div_result.appendChild(label_container);
	
	var select_css_classes = createElementCB("select");
	select_css_classes.setAttribute("id","select_class");
	select_css_classes.setAttribute("onchange","javascript:viewPropertiesFromClass(this.value);");

	var sorted_list_of_unique_classes = refreshProperties() 
	
	var new_option_null = createElementCB("option");
	new_option_null.setAttribute("value","osmarender_frontend:null");
	var new_option_null_text = document.createTextNode("Select a CSS class");
	new_option_null.appendChild(new_option_null_text);
	select_css_classes.appendChild(new_option_null);

	for (var key_name in sorted_list_of_unique_classes) {
			var new_option = createElementCB("option");
			new_option.setAttribute("value",sorted_list_of_unique_classes[key_name]);
			var new_option_text = document.createTextNode(sorted_list_of_unique_classes[key_name]);
			new_option.appendChild(new_option_text);
			select_css_classes.appendChild(new_option);
	}
	div_result.appendChild(select_css_classes);

}

refreshProperties = function() {
	var sorted_list_of_unique_classes = new Array();
	for (var stylesobject in cmyk.Styles) {
		sorted_list_of_unique_classes[sorted_list_of_unique_classes.length] = cmyk.Styles[stylesobject].selectors[0].singleSelectors[0].classes.values;
		props = new Array();
		for (properties in cmyk.Styles[stylesobject].selectors[0].properties.values) {
			if (typeof(properties) != "function") {
				props[properties] = cmyk.Styles[stylesobject].selectors[0].properties.values[properties];
				classesAndProperties[cmyk.Styles[stylesobject].selectors[0].singleSelectors[0].classes.values]=props;
			}
		}
	}
/*	for (pippo in classesAndProperties) {
		alert(pippo); //untagged-segments
		for (pluto in classesAndProperties[pippo]) {
			alert(pluto); //stroke-width
			alert(classesAndProperties[pippo][pluto]); //0.5pixels
		}
	}*/
//	sorted_list_of_unique_classes = sorted_list_of_unique_classes.sort();
	return sorted_list_of_unique_classes.sort();
}

Osmatranform = function() {
	var osmfilename_written = document.getElementById("osm_file_name_written").value;
	var osmfilename_selected = document.getElementById("osm_file_name_selected").value;

	var osmfilename="data.osm";
	
	if (osmfilename_selected == "") {
		if (osmfilename_written != "") {
			osmfilename = osmfilename_written;
		}
	}
	else {
		osmfilename = osmfilename_selected;
	}
	
	cmyk.setOsmFile(osmfilename);
	
	xslfile = Sarissa.getDomDocument();
	xslfile.async=false;
	xslfile.load("osmarender.xsl");
	
	var rulesfile=cmyk.getRulesFile();
	try{
			var processor = new XSLTProcessor();
			processor.importStylesheet(xslfile);
			var svgfile = processor.transformToDocument(rulesfile.documentElement);
			while (document.getElementById("svgfile").hasChildNodes()) {
				document.getElementById("svgfile").removeChild(document.getElementById("svgfile").firstChild);
			}
			var title_container = createElementCB("h1");
//			var title = document.createTextNode("data file: "+osmfilename+" with rules : "+rulesfilename);
			var title = document.createTextNode("data file: "+osmfilename);
			title_container.appendChild(title);
			document.getElementById("svgfile").appendChild(title_container);
			document.getElementById("svgfile").appendChild(svgfile.documentElement);
	}
	catch (error) {
	  alert(error);
	}

}

setStyle = function () {
var class = document.getElementById("select_class").value;
var labels_array = document.getElementsByTagName("label");
var magic_string_to_search="label_property_";
var labels_styles_array = new Array();

for(labels in labels_array) {
	var label_id = labels_array[labels].id;
	if (!!label_id) {
		if (label_id.substring(0,(magic_string_to_search.length))==magic_string_to_search) {
			property = labels_array[labels].getAttribute("id").substring((magic_string_to_search.length));
			editValue = document.getElementById(property).value;
			cmyk.setSingleStyle(class,property,editValue);
		}
	}
}
	cmyk.setStyle();
}

saveFile = function() {
	var string = new XMLSerializer().serializeToString(cmyk.getRulesFile().documentElement);
//	console.debug(string);
	var newWindow = window.open("","xml");
	newWindow.location="data:text/xml;charset=utf8,"+encodeURIComponent(string);
	//var newWindow = window.open("","xml");
	//newWindow.document.open();
	//newWindow.document.write(<p>pippo</p>);
	//newWindow.document.documentElement.appendChild(cmyk.getRulesFile().documentElement);
	//newWindow.document.close();
	
//	<a href="javascript: window.location='data:text/csv;charset=utf8,' + encodeURIComponent('a,b,c,d');">dowload CSV</a>
}

clearSVG = function() {
	while (document.getElementById("svgfile").hasChildNodes()) {
		document.getElementById("svgfile").removeChild(document.getElementById("svgfile").firstChild);
	}
}

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
