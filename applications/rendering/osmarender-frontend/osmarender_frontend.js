/**
 * @author Mario Ferraro <fadinlight@gmail.com>
 * http://osmarenderfrontend.wordpress.com
 * Released under GPL v2 or later
 */

var cmyk;

var classesAndProperties = new Array();

viewPropertiesFromClass = function(key) {
	if (key=="osmarender_frontend:null") return false;

	//Search rules in which this class is used
	var array_da_aggiornare = new Array();
	cmyk.getRuleFromClass(cmyk.getRuleModel(),key,array_da_aggiornare);
	
	//console.debug(array_da_aggiornare);
	
	var div_result = document.getElementById("result_process_rules");

	var todelete = document.getElementById("result_process_css_key");
	if (todelete) div_result.removeChild(document.getElementById("result_process_css_key"));

	var div_properties = createElementCB("div");
	div_properties.setAttribute("id","result_process_css_key");
	div_result.appendChild(div_properties);

	refreshProperties();

	propertiesToPrint = classesAndProperties[key];

	if (array_da_aggiornare.length) {

		div_properties.appendChild(document.createTextNode("Applies to: "));
	
		var rules_list = createElementCB("ol");
		div_properties.appendChild(rules_list);

		for (single_rule in array_da_aggiornare) {
			for (single_key_value in array_da_aggiornare[single_rule].keys) {
				var li_rule = createElementCB("li");
				var li_rule_strong_1 = createElementCB("strong");
				li_rule_strong_1.appendChild(document.createTextNode("key: "));
				li_rule.appendChild(li_rule_strong_1);
				li_rule.appendChild(document.createTextNode(array_da_aggiornare[single_rule].keys[single_key_value]));
				var li_rule_strong_2 = createElementCB("strong");
				li_rule_strong_2.appendChild(document.createTextNode(", value: "));
				li_rule.appendChild(li_rule_strong_2);
				li_rule.appendChild(document.createTextNode(array_da_aggiornare[single_rule].values[single_key_value]));
				rules_list.appendChild(li_rule);
				//rules_list.appendChild(createElementCB("li").appendChild(document.createTextNode("key: "+array_da_aggiornare[single_rule].keys[single_key_value]+" value: "+array_da_aggiornare[single_rule].values[single_key_value])))
			}
		}
	} else {
		var bold_string = createElementCB("strong");
		bold_string.appendChild(document.createTextNode("Attention! "));
		div_properties.appendChild(bold_string);
		div_properties.appendChild(document.createTextNode("Selected class is not associated to any rule!"));
	}

	var dl_container = createElementCB("dl");
	div_properties.appendChild(dl_container);
	
	// Insert button for adding new properties:

	var div_container_button = createElementCB("div");
	div_container_button.setAttribute("style","display:table-cell;");
	dl_container.appendChild(div_container_button);

	dl_container.appendChild(createElementCB("dt"));
	dd_container_button = createElementCB("dd");
	dd_container_button.appendChild(addCSSPropertyButton());
	div_container_button.appendChild(dd_container_button);
	div_container_button.appendChild(createElementCB("br"));
	
	for(single_property in propertiesToPrint) {
		dl_container.appendChild(createElementCB("br"));
		var div_container = createElementCB("div");
		div_container.setAttribute("style","display:table-cell;border:1px solid grey;border-left:none;border-right:none;");
		dl_container.appendChild(div_container);
	
		var dt_container = createElementCB("dt");
		div_container.appendChild(dt_container);
		
		var label_container = createElementCB("label");
		label_container.setAttribute("id","label_property_"+single_property);
		label_container.setAttribute("for",single_property);
		var label = document.createTextNode(single_property+": ");
		label_container.appendChild(label);
		dt_container.appendChild(label_container);
		
		var dd_container = createElementCB("dd");
		div_container.appendChild(dd_container);

		var text_container = createElementCB("input");
		text_container.setAttribute("id",single_property);
		text_container.setAttribute("value",propertiesToPrint[single_property]);
		dd_container.appendChild(text_container);
		
		if (single_property=="fill" || single_property=="stroke") {
			var div_container=createElementCB("div");
			div_container.setAttribute("id","viewColor["+single_property+"]");
			div_container.setAttribute("style","float:left;display:table-cell;display:inline-block;border: medium dotted grey;height:20px;width:20px;background-color:"+propertiesToPrint[single_property]+";");
			text_container.setAttribute("onkeyup","javascript:changeInputBackground(this);");
			dd_container.appendChild(div_container);
			
			var button_color_picker=createElementCB("button");
			button_color_picker.setAttribute("id","button_color_picker["+single_property+"]");
			button_color_picker.setAttribute("osmarender_frontend_button_property",single_property);
			button_color_picker.setAttribute("onclick","javascript:viewColorPicker(this);");
			button_color_picker.appendChild(document.createTextNode("Pick color"));
			dd_container.appendChild(button_color_picker);
		}
//TODO: unique index of active dojo widgets to destroy when select menu changes
/*		if (single_property=="opacity") {
			text_container.parentNode.removeChild(text_container);
		  	var slider = new dijit.form.NumberSpinner(
 				{id: "opacity_spinner["+single_property+"]",
				value: propertiesToPrint[single_property],
				constraints: {min:0,max:1,places:1},
				smallDelta: 0.1,
				style: "width:5em"
 				}, dd_container
 			);
		 	slider.startup();
		}
		
		if (single_property=="stroke-width") {
//TODO: compatibility with other units (em, ecc)
			text_container.parentNode.removeChild(text_container);
			var div_interno=createElementCB("div");
			dd_container.appendChild(div_interno);
		  	var slider = new dijit.form.NumberSpinner(
 				{id: "stroke_width_spinner["+single_property+"]",
				value: propertiesToPrint[single_property].substring(0,propertiesToPrint[single_property].indexOf("p")),
				constraints: {min:0,places:1},
				smallDelta: 0.1,
				style: "width:5em"
 				}, div_interno
 			);
		 	slider.startup();
			dd_container.appendChild(document.createTextNode("px"));
		}*/
		
		if (single_property=="stroke-linecap" || single_property=="stroke-linejoin" || single_property=="font-weight") {
			text_container.parentNode.removeChild(text_container);
			var select_strokes=createElementCB("select");
			select_strokes.setAttribute("id",single_property);
			var types_of_strokes = {
				"stroke-linecap": ["butt","round","square","inherit"],
				"stroke-linejoin": ["miter","round","bevel","inherit"],
				"font-weight": ["normal","bold","bolder","lighter","100","200","300","400","500","600","700","800","900","inherit"]
			};
			for (types in types_of_strokes[single_property]) {
				var option = createElementCB("option");
				option.setAttribute("value",types_of_strokes[single_property][types]);
				option.appendChild(document.createTextNode(types_of_strokes[single_property][types]));
				if (types_of_strokes[single_property][types]==propertiesToPrint[single_property]) {
					option.setAttribute("selected","selected");
				}
				select_strokes.appendChild(option);
			}
			dd_container.appendChild(select_strokes);
		}
		
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
		delete_button = createElementCB("button");
		delete_button.setAttribute("id","delete_property_button_"+single_property);
		delete_button.setAttribute("onclick","javascript:deleteSingleProp(this);")
		delete_button.appendChild(document.createTextNode("Delete"));
		dd_container.appendChild(delete_button);
		div_container.appendChild(createElementCB("br"));

	}
}

viewColorPicker = function(button) {
	var div = createElementCB("div");
	div.setAttribute("id","div_color_picker["+button.getAttribute("osmarender_frontend_button_property")+"]");
	div.setAttribute("style","float:left;margin-left:5px;margin-bottom:10px;display:block;border: 1px solid black;padding:5px");
	button.parentNode.appendChild(div);
	div.appendChild(createElementCB("div"));
  	var color_picker = new dojox.widget.ColorPicker(
 		{id: "color_picker["+button.getAttribute("osmarender_frontend_button_property")+"]",
 		}, div.firstChild
 	);
 	color_picker.startup();
	var button_close_save = createElementCB("button");
	button_close_save.appendChild(document.createTextNode("Close and Save"));
	button_close_save.setAttribute("onclick","javascript:closeColorPicker(document.getElementById(\"color_picker["+button.getAttribute("osmarender_frontend_button_property")+"]\"),\""+button.getAttribute("osmarender_frontend_button_property")+"\",true)");
	div.appendChild(button_close_save);

	var button_close_discard = createElementCB("button");
	button_close_discard.appendChild(document.createTextNode("Close and Discard"));
	button_close_discard.setAttribute("onclick","javascript:closeColorPicker(document.getElementById(\"color_picker["+button.getAttribute("osmarender_frontend_button_property")+"]\"),\""+button.getAttribute("osmarender_frontend_button_property")+"\",false)");
	div.appendChild(button_close_discard);
}

closeColorPicker = function(color_picker,property,save) {
	if (save) {
		document.getElementById(property).setAttribute("value",color_picker.value);
		document.getElementById("viewColor["+property+"]").setAttribute("style","float:left;display:table-cell;display:inline-block;border: medium dotted grey;height:20px;width:20px;background-color:"+color_picker.value+";");
	}
	document.getElementById("div_color_picker["+property+"]").parentNode.removeChild(document.getElementById("div_color_picker["+property+"]"));
	dijit.byId("color_picker["+property+"]").destroy();
}

changeInputBackground = function(dom) {
	document.getElementById("viewColor["+dom.id+"]").setAttribute("style","float:left;display:table-cell;display:inline-block;border: medium dotted grey;height:20px;width:20px;background-color: "+dom.value+";");
}

addCSSPropertyButton = function () {
	var button_add = createElementCB("button");
	button_add.setAttribute("id","add_property_button");
	button_add.setAttribute("onclick","javascript:addCSSProperty(this);");
	var button_text = document.createTextNode("Add a CSS Property");
	button_add.appendChild(button_text);
	return button_add;
}

addCSSProperty = function (dom_here) {
	var container = dom_here.parentNode;
	with (container) {
		removeChild(firstChild);
		var label_property_name = createElementCB("label");
		label_property_name.appendChild(document.createTextNode("Name: "));
		label_property_name.setAttribute("id","label_property_name_to_add");
		appendChild(label_property_name);
		appendChild(createElementCB("br"));
		var input_property_name = createElementCB("input");
		input_property_name.setAttribute("id","property_name_to_add");
		appendChild(input_property_name);
		appendChild(createElementCB("br"));
		var label_property_value = createElementCB("label");
		label_property_value.appendChild(document.createTextNode("Value: "));
		label_property_value.setAttribute("id","label_property_value_to_add");
		appendChild(label_property_value);
		appendChild(createElementCB("br"));
		var input_property_value = createElementCB("input");
		input_property_value.setAttribute("id","property_value_to_add");
		appendChild(input_property_value);
		appendChild(createElementCB("br"));
		var button_to_add = createElementCB("button");
		button_to_add.setAttribute("id","confirm_add_property_button");
		button_to_add.setAttribute("onclick","javascript:addSingleProp(document.getElementById(\"select_class\").value,document.getElementById(\"property_name_to_add\").value,document.getElementById(\"property_value_to_add\").value);")
		button_to_add.appendChild(document.createTextNode("Add Property"));
		appendChild(button_to_add);
	}
}

addSingleProp = function (class,property_name,property_value) {
	cmyk.addSingleStyle(class,property_name,property_value);
	document.getElementById("select_class").onchange();
	if(document.getElementById("transform_on_style_add").checked) {
		Osmatransform();
	}
}

deleteSingleProp = function (button) {
	var magic_string_to_search="delete_property_button_";
	property = button.getAttribute("id").substring((magic_string_to_search.length));
	class_to_delete = document.getElementById("select_class").value;
	cmyk.deleteSingleStyle(class_to_delete,property);
	document.getElementById("select_class").onchange();
	if (document.getElementById("transform_on_style_delete").checked) {
		Osmatransform();
	}
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

	document.getElementById("load_file").style.display="none";

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

	var return_load_link = createElementCB("a");
	return_load_link.setAttribute("id","link_return_load");
	return_load_link.setAttribute("href","javascript:displayLoad();");
	return_load_link.appendChild(document.createTextNode("Reload other files"));
	div_result.appendChild(return_load_link);
	div_result.appendChild(createElementCB("br"));

	var label_container = createElementCB("label");
	label_container.setAttribute("id","select_class_label");
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
	div_result.style.display="block";

}

displayLoad = function() {
	document.getElementById("result_process_rules").style.display="none";
	document.getElementById("load_file").style.display="block";
	if (cmyk!=undefined) document.getElementById("return_to_rules").style.display="block";
}

displayRules = function() {
	document.getElementById("load_file").style.display="none";
	document.getElementById("result_process_rules").style.display="block";
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
	return sorted_list_of_unique_classes.sort();
}

function Osmatransform () {
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
	
	if (labels_array && labels_array.length) {

		for(labels in labels_array) {
			if (labels_array[labels]) {
				var label_id = labels_array[labels].id;
				if (!!label_id) {
					if (label_id.substring(0,(magic_string_to_search.length))==magic_string_to_search) {
						property = labels_array[labels].getAttribute("id").substring((magic_string_to_search.length));
						editValue = document.getElementById(property).value;
						cmyk.setSingleStyle(class,property,editValue);
					}
				}
			}
		}
	}
	cmyk.setStyle();
	if (document.getElementById("transform_on_style_set").checked) {
		Osmatransform();
	}
}

saveFile = function() {
	var string = new XMLSerializer().serializeToString(cmyk.getRulesFile().documentElement);
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
