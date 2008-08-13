/**
 * @author Mario Ferraro <fadinlight@gmail.com>
 * http://osmarenderfrontend.wordpress.com
 * Released under GPL v2 or later
 */

var cmyk;

var classesAndProperties = new Array();

var OSMARENDER_LOCATION=location.href.substring(0,location.href.lastIndexOf("/")+1);

var dojowidgets = new Array();

viewPropertiesFromClass = function(key) {

// destroy dojowidgets found
	if (dojowidgets.length) {
		for (widget in dojowidgets) {
			if (dojowidgets[widget].id) {
				dijit.byId(dojowidgets[widget].id).destroy();
			}
		}
		dojowidgets = new Array();
	}

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

		var array_without_duplicates = new Array();
		
		for (single_rule in array_da_aggiornare) {
			for (single_key_value in array_da_aggiornare[single_rule].keys) {
				for (single_value_value in array_da_aggiornare[single_rule].values) {
					if (array_without_duplicates[array_da_aggiornare[single_rule].keys[single_key_value]]==null) {
						array_without_duplicates[array_da_aggiornare[single_rule].keys[single_key_value]]=new Array();
					}
					if (array_without_duplicates[array_da_aggiornare[single_rule].keys[single_key_value]][array_da_aggiornare[single_rule].values[single_value_value]]==null) {
						array_without_duplicates[array_da_aggiornare[single_rule].keys[single_key_value]][array_da_aggiornare[single_rule].values[single_value_value]]=0;
					}
					array_without_duplicates[array_da_aggiornare[single_rule].keys[single_key_value]][array_da_aggiornare[single_rule].values[single_value_value]]++;
				}
			}
		}

		for (single_key in array_without_duplicates) {
			var li_rule = createElementCB("li");
			var li_rule_strong_1 = createElementCB("strong");
			li_rule_strong_1.appendChild(document.createTextNode("key: "));
			li_rule.appendChild(li_rule_strong_1);
			li_rule.appendChild(document.createTextNode(single_key));
			var li_rule_strong_2 = createElementCB("strong");
			li_rule_strong_2.appendChild(document.createTextNode(", values: "));
			li_rule.appendChild(li_rule_strong_2);
			first_value_found=false;
			for (single_value in array_without_duplicates[single_key]) {
				if (first_value_found) {
					li_rule.appendChild(document.createTextNode(", "));
				}
				li_rule.appendChild(document.createTextNode(single_value+" ("+array_without_duplicates[single_key][single_value]+")"));
				first_value_found=true;
			}
			rules_list.appendChild(li_rule);
			//rules_list.appendChild(createElementCB("li").appendChild(document.createTextNode("key: "+array_da_aggiornare[single_rule].keys[single_key_value]+" value: "+array_da_aggiornare[single_rule].values[single_key_value])))
		}

//TODO: add type of checking everywhere, look for safeRef() method at http://blog.stchur.com/2006/07/08/safely-referencing-objects-in-javascript-understanding-null-and-undefined/2/

//TODO: add removing duplicates also in symbols list
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
	
	button_set_style=createElementCB("button");
	button_set_style.setAttribute("onclick","javascript:setStyle();");
	button_set_style.appendChild(document.createTextNode("Set Style"));
	dd_container_button.appendChild(button_set_style);

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
//Thanks to http://www.zvon.org/xxl/svgReference/Output/attr_text-anchor.html and such for SVG attributes
		if ((single_property=="fill" || single_property=="stroke") && propertiesToPrint[single_property].substring(0,3)!="url") {
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

		if ((single_property=="opacity" || single_property=="fill-opacity" || single_property=="stroke-opacity" || single_property=="stroke-miterlimit") && (propertiesToPrint[single_property].search("[0-9]")!=-1)) {
			text_container.parentNode.removeChild(text_container);
			var delta_for_editing;
			if (propertiesToPrint[single_property].indexOf(".")!=-1) delta_for_editing = 0.1; else delta_for_editing = 1;
		  	var slider = new dijit.form.NumberSpinner(
 				{id: single_property,
				value: propertiesToPrint[single_property],
				smallDelta: delta_for_editing,
				style: "width:5em;height:1.1em;"
 				}, dd_container
 			);
			if (single_property=="stroke-miterlimit") {
				slider.constraints={min:0};
			}
			else {
				slider.constraints={min:0,max:1};
			}
		 	slider.startup();
			dojowidgets[dojowidgets.length]=slider;
		}
		if ((single_property=="stroke-width" || single_property=="font-size") && (propertiesToPrint[single_property].search("[0-9]")!=-1)) {
//TODO: compatibility with other units (em, ecc)
			text_container.parentNode.removeChild(text_container);
			var delta_for_editing;
			if (propertiesToPrint[single_property].indexOf(".")!=-1) delta_for_editing = 0.1; else delta_for_editing = 1;
			var div_interno=createElementCB("div");
			dd_container.appendChild(div_interno);
		  	var slider = new dijit.form.NumberSpinner(
 				{id: single_property,
				value: propertiesToPrint[single_property].substring(0,propertiesToPrint[single_property].indexOf("p")),
				constraints: {min:0},
				smallDelta: delta_for_editing,
				style: "width:5em;height:1.1em;"
 				}, div_interno
 			);
		 	slider.startup();
			dojowidgets[dojowidgets.length]=slider;
			dd_container.appendChild(document.createTextNode("px"));
		}

		if (single_property=="stroke-linecap" || single_property=="stroke-linejoin" || single_property=="font-weight" || single_property=="text-anchor" || single_property=="display" || single_property=="fill-rule") {
			text_container.parentNode.removeChild(text_container);
			var select_strokes=createElementCB("select");
			select_strokes.setAttribute("id",single_property);
			var types_of_strokes = {
				"stroke-linecap": ["butt","round","square","inherit"],
				"stroke-linejoin": ["miter","round","bevel","inherit"],
				"font-weight": ["normal","bold","bolder","lighter","100","200","300","400","500","600","700","800","900","inherit"],
				"text-anchor": ["start","middle","end","inherit"],
				"display": ["inline","block","list-item","run-in","compact","marker","table","inline-table","table-row-group","table-header-group","table-footer-group","table-row","table-column-group","table-column","table-cell","table-caption","none","inherit"],
				"fill-rule": ["nonzero","evenodd","inherit"]
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
		
		if ((single_property=="marker-end" || single_property=="marker-mid" || single_property=="marker-start" || single_property=="fill" || single_property=="stroke") && propertiesToPrint[single_property].substring(0,3)=="url") {
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

var MAX_TOTAL_STEPS=3;
	
var progressData = {
	total: {
		id: "",
		progress:0,
		maximum:1,
		message:""
	},
	step : {
		id: "",
		progress:0,
		maximum:1,
		message:""
	}
};

function updateProgressBar() {
	for (progressBar in progressData) {
		with (progressData[progressBar]) {
			dijit.byId(id).update({
				progress:progress,
				maximum:maximum,
				report: function(percent) {
					return message;
				}
			});
		}
	}
}

var onLoadTransform;

loadOsmAndRules = function(rulesfilename,osmfilename,ProgressBarTotal,ProgressBarStep,TransformonLoad) {

	rulesfilename = location.href.substring(0,location.href.lastIndexOf("/")+1)+rulesfilename;
	osmfilename = location.href.substring(0,location.href.lastIndexOf("/")+1)+osmfilename;

	onLoadTransform=TransformonLoad;

	with (progressData) {
		with(total) {
			id = ProgressBarTotal;
			progress++;
			maximum = MAX_TOTAL_STEPS;
			message = "Clearing map";
		}
		with(step) {
			id = ProgressBarStep;
		}
	}

	updateProgressBar();
	clearSVG();
	
	with (progressData) {
		with(total) {
			progress++;
			message = "Getting rules file elements";
		}
	}
	updateProgressBar();

	cmyk = new CMYK(rulesfilename,osmfilename,progressData,updateProgressBar,AfterCMYKLoad);
	
}

AfterCMYKLoad = function() {

	with (progressData) {
		with(total) {
			progress++;
			message = "Drawing key/value pairs";
		}
	}
	updateProgressBar();
	listKeys();

	with (progressData) {
		with(total) {
			progress++;
			message = "Getting symbols";
		}
	}
	updateProgressBar();

	SymbolsResult();

	with (progressData) {
		with(total) {
			progress++;
			message = "Getting symbols";
		}
	}

	updateProgressBar();

	with (progressData) {
		with(total) {
			progress++;
			message = "Creating Settings interface";
		}
	}
	updateProgressBar();

	SettingsResults();
	

	var div_result = document.getElementById("result_process_rules");

	while (div_result.hasChildNodes()) {
		div_result.removeChild(div_result.firstChild);
	}

	div_result.appendChild(createElementCB("br"));

	var label_container = createElementCB("label");
	label_container.setAttribute("id","select_class_label");
	label_container.setAttribute("for","select_class");
	var label = document.createTextNode("Select a CSS class: ");
	label_container.appendChild(label);
	div_result.appendChild(label_container);

	var sorted_list_of_unique_classes = refreshProperties() 
	
	var store_classes = {
		identifier: "name",
		items : []
	};
	
	for (var key_name in sorted_list_of_unique_classes) {
			store_classes.items[store_classes.items.length]= {name: ""+sorted_list_of_unique_classes[key_name]};
	}
	store_classes_data_store = new dojo.data.ItemFileReadStore({data:store_classes});

	//There was a duplicate "landuse-retail" on z13.xml, so check for duplicates
	//TODO: copy this type of check for "duplicates" in cmyk parsing too
	var printError = function (error,request) {
		document.getElementById("progressContainer").style.display="none";
		document.getElementById("load_preset_button_div").style.display="block";
		alert(error);
	}
	store_classes_data_store.fetch({
		onError: printError
	});

	var div_to_insert = createElementCB("div");
	div_result.appendChild(div_to_insert);
	if (dijit.byId("select_class")) dijit.byId("select_class").destroy();
	var select_css_classes = new dijit.form.FilteringSelect(
			{
			id:"select_class",
			autoComplete:"false",
			invalidMessage:"Select a Valid CSS class!",
			onChange:	function (value) {
						if (value!=undefined) {
							viewPropertiesFromClass(value);
						}
					},
			store:store_classes_data_store,
			searchAttr: "name"
			}, div_to_insert
		);

	select_css_classes.startup();
	document.getElementById("load_file").style.display="none";

	// Reset progress Bars and fade out them
	with (progressData) {
		with(total) {
			progress=0;
			maximum=0;
			message = "";
		}
		with(step) {
			progress=0;
			maximum=0;
			message = "";
		}
	}
	updateProgressBar();
	dojo.fadeOut({node:"progressContainer"}).play();
	document.getElementById("load_preset_button_div").style.display="block";
	document.getElementById("menu").style.display="block";
	document.getElementById("menu_views").style.display="block";
	div_result.style.display="block";
	if (onLoadTransform) Osmatransform();
}

SymbolsResult = function() {
	var div_result = dijit.byId("result_symbols");

	clearContentPanes([div_result]);
	loadIntoNodeAndParse("osmarender_frontend/panels/symbols/select_symbol.xml",div_result.domNode);

	var symbols_section = cmyk.getSymbols();

	var symbols_array=new Array();

	for (single_symbol in symbols_section) {
		if (typeof(symbols_section[single_symbol])=="object" && symbols_section[single_symbol].id) {
			symbols_array[symbols_array.length]=symbols_section[single_symbol].id;
		}
//Necessary change for firefox3... handles SVG as SVG not as XML/DOM
		else if (typeof(symbols_section[single_symbol]) == "object" && symbols_section[single_symbol]["attributes"][0]) {
			symbols_array[symbols_array.length]=symbols_section[single_symbol]["attributes"][0].nodeValue.substring(1);
		}
	}
	//TODO: I have to delete this... why is it parsing multiple ids?
	symbols_array = RemoveDuplicates(symbols_array.sort());
	
	var store_symbols = {
		identifier: "name",
		items : []
	};
	
	for (var symbol_name in symbols_array) {
			store_symbols.items[store_symbols.items.length]= {name: ""+symbols_array[symbol_name]};
	}

	store_symbols_data_store = new dojo.data.ItemFileReadStore({data:store_symbols});
	dijit.byId("select_symbols").store=store_symbols_data_store;
}

//thanks to http://dojocampus.org/content/2008/03/14/functional-ajax-with-dojo/
SettingsResults = function() {
	div_results = dojo.byId("result_settings");
	if (dijit.byId("settings_container")) dijit.byId("settings_container").destroyRecursive();
	loadIntoNodeAndParse("osmarender_frontend/panels/settings/settings.xml",div_results);

	dojo.byId("BoundsNorth").value=cmyk.getBounds().lat.max;
	dojo.byId("BoundsSouth").value=cmyk.getBounds().lat.min;
	dojo.byId("BoundsEast").value=cmyk.getBounds().lon.min;
	dojo.byId("BoundsWest").value=cmyk.getBounds().lon.max;
	dojo.byId("Scale").value=cmyk.getScale();
	dojo.byId("TextAttenuation").value=cmyk.getTextAttenuation();
}


viewSymbol = function(svg_url) {
	if (cmyk.getRulesFile().getElementById(svg_url)) {
		div_result = dijit.byId("div_viewSymbol");
		var svg_symbol = cmyk.getRulesFile().getElementById(svg_url);
		var svg_container;
		if (typeof document.createElementNS != 'undefined') {
			svg_container = document.createElementNS("http://www.w3.org/2000/svg","svg");
		}
		else {
			svg_container = createElementCB("svg");
		}
		for (var a=0; a<svg_symbol.attributes.length; a++) {
			svg_container.setAttribute(svg_symbol.attributes[a].name,svg_symbol.attributes[a].value);
		}
		svg_container.setAttribute("height","100px");

		//TODO: Fixing viewBox, more hack needed
		var viewBox_string = svg_container.getAttribute("viewBox");
		var viewBox_array = viewBox_string.split(" ");
		if (viewBox_array[1]>0) {
			viewBox_array[1]="0";
		}
		viewBox_string = viewBox_array.join(" ");
		svg_container.setAttribute("viewBox",viewBox_string);

		for (var a=0; a<svg_symbol.childNodes.length; a++) {
			if (svg_symbol.childNodes[a].nodeType!=Node.TEXT_NODE) {
				svg_container.appendChild(svg_symbol.childNodes[a].cloneNode(true));
			}
		}

		clearContentPanes([div_result,dijit.byId("div_appliesTo"),dijit.byId("div_symbolDetails")]);
		div_result.domNode.appendChild(svg_container);
		
		// Get key/value pairs applied
		//Search rules in which this symbol is used
		var array_da_aggiornare = new Array();
		cmyk.getRuleFromSymbol(cmyk.getRuleModel(),svg_url,array_da_aggiornare);

		div_appliesTo = dijit.byId("div_appliesTo");

		if (array_da_aggiornare.length) {
			div_appliesTo.domNode.innerHTML='<br />Applies to:<ol>';

			for (single_rule in array_da_aggiornare) {
				for (single_key_value in array_da_aggiornare[single_rule].keys) {
					div_appliesTo.domNode.innerHTML+='<li><strong>key:</strong> '+array_da_aggiornare[single_rule].keys[single_key_value]+',<strong> values:</strong>';
					first_value_found=false;
					for (single_value_value in array_da_aggiornare[single_rule].values) {
						if (first_value_found) {
							div_appliesTo.domNode.innerHTML+=',';
						}
						div_appliesTo.domNode.innerHTML+=(' '+array_da_aggiornare[single_rule].values[single_value_value]);
						first_value_found=true;
					}
					div_appliesTo.domNode.innerHTML+='</li>';
				}
			}
			div_appliesTo.domNode.innerHTML+='</ol>';
		} else {
			div_appliesTo.domNode.innerHTML='<strong>Attention! </strong>Selected symbol is not associated to any rule!';
		}

		if ((dijit.byId("select_feature_way") && dijit.byId("select_feature_value")) && (dijit.byId("select_feature_way").value!=undefined && dijit.byId("select_feature_value").value!=undefined)) {
			var key_chosen = dijit.byId("select_feature_way").value;
			var value_chosen = dijit.byId("select_feature_value").value;

			loadIntoNodeAndParse("osmarender_frontend/panels/symbols/symbol_details.xml",dojo.byId("div_symbolDetails"));

			dijit.byId("symbol_width").setValue(viewBox_array[2]);
			dijit.byId("symbol_height").setValue(viewBox_array[3]);

			dojo.byId("dest_attach_symbol").href='javascript:attachSymbol(\''+svg_url+'\',\''+key_chosen+'\',\''+value_chosen+'\',dojo.byId(\'symbol_width\').value,dojo.byId(\'symbol_height\').value,dojo.byId(\'symbol_layer\').value);';
			dojo.byId("dest_attach_symbol").innerHTML='Attach this symbol to '+key_chosen+', '+value_chosen+'';
		}
	}
}

displayLoad = function() {
	document.getElementById("result_process_rules").style.display="none";
	document.getElementById("result_settings").style.display="none";
	document.getElementById("result_process_key").style.display="none";
	document.getElementById("result_symbols").style.display="none";
	document.getElementById("load_file").style.display="block";
	//if (cmyk!=undefined) document.getElementById("return_to_rules").style.display="block";
}

displaySettings = function() {
	document.getElementById("result_process_rules").style.display="none";
	document.getElementById("result_settings").style.display="block";
	document.getElementById("result_process_key").style.display="none";
	document.getElementById("result_symbols").style.display="none";
	document.getElementById("load_file").style.display="none";
	//if (cmyk!=undefined) document.getElementById("return_to_rules").style.display="block";
}

displayRules = function() {
	document.getElementById("load_file").style.display="none";
	document.getElementById("result_settings").style.display="none";
	document.getElementById("result_symbols").style.display="none";
	document.getElementById("result_process_key").style.display="none";
	document.getElementById("result_process_rules").style.display="block";
}

displayKeyValue = function() {
	document.getElementById("load_file").style.display="none";
	document.getElementById("result_settings").style.display="none";
	document.getElementById("result_symbols").style.display="none";
	document.getElementById("result_process_key").style.display="block";
	document.getElementById("result_process_rules").style.display="none";
}

displaySymbols = function() {
	document.getElementById("result_settings").style.display="none";
	document.getElementById("load_file").style.display="none";
	document.getElementById("result_symbols").style.display="block";
	document.getElementById("result_process_key").style.display="none";
	document.getElementById("result_process_rules").style.display="none";
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

// This function clear an array of contentPanes, deleting any dijit widget from the register and resetting the HTML content
function clearContentPanes(contentPanes) {
	if (!(contentPanes instanceof Array)) throw new Error ("divs must be an array of dijit's contentPanes");
	for (var i in contentPanes) {
		if(contentPanes[i]) {
			contentPanes[i].destroyDescendants();
			contentPanes[i].setContent();
		}
	}
}

// This function load an XML file into a node's innerHTML, and the dojo-parse it
function loadIntoNodeAndParse(my_url,node) {
	dojo.xhrGet({
		url: my_url,
		sync: true,
		load: function(data){
			node.innerHTML = data;
		}
	});
	dojo.parser.parse(node);
}


listKeys = function() {
	var elements = cmyk.getKeyValuePairs();

	var sorted_list_of_unique_keys = new Array();
	
	//TODO: change this calling into corresponding classes as soon as modeling is finished
	for (var dettaglio in elements.ways) {
		for (var tag in elements.ways[dettaglio]) {
			sorted_list_of_unique_keys[sorted_list_of_unique_keys.length]=tag;
		}
	}
	
	for (var dettaglio in elements.nodes) {
		for (var tag in elements.nodes[dettaglio]) {
			sorted_list_of_unique_keys[sorted_list_of_unique_keys.length]=tag;
		}
	}

	sorted_list_of_unique_keys = RemoveDuplicates(sorted_list_of_unique_keys.sort());
	
	var div_result = dijit.byId("div_select_key");

	clearContentPanes([div_result,dijit.byId("div_select_value"),dijit.byId("list_css"),dijit.byId("list_symbols"),dijit.byId("div_tree")]);
	loadIntoNodeAndParse("osmarender_frontend/panels/rules/select_key.xml",div_result.domNode);

	var rulesStore = new dojo.data.ItemFileReadStore({data:cmyk.getRuleTree()});
	var printError = function (error,request) {
		alert(error);
	}
	rulesStore.fetch({
		onError: printError
	});

// See http://dojotoolkit.org/2008/02/24/dijit-tree-and-dojo-data-dojo-1-1-model
	var myModel = new dijit.tree.TreeStoreModel({
		store: rulesStore,
		query: {top:true},
		rootId: "0",
		rootLabel: "rules",
		childrenAttrs: ["children"],
	});

	loadIntoNodeAndParse("osmarender_frontend/panels/rules/tree.xml",dojo.byId("div_tree"));
//TODO: Lazy loading the tree, see http://www.ibm.com/developerworks/websphere/techjournal/0805_col_johnson/0805_col_johnson.html
	var treerules = new dijit.Tree({model:myModel});

	dojo.byId("div_tree").appendChild(treerules.domNode);

	var store_keys = {
		identifier: "name",
		items : []
	};
	
	for (var key_name in sorted_list_of_unique_keys) {
			store_keys.items[store_keys.items.length]= {name: ""+sorted_list_of_unique_keys[key_name]};
	}

	store_keys_data_store = new dojo.data.ItemFileReadStore({data:store_keys});
	dijit.byId("select_feature_way").store=store_keys_data_store;
}

function viewValuesFromKey(key) {
	var elements = cmyk.getKeyValuePairs();

	var div_result = dijit.byId("div_select_value");

	clearContentPanes([div_result,dijit.byId("list_css"),dijit.byId("list_symbols")]);

	var sorted_list_of_unique_values = new Array();
	sorted_list_of_unique_values[0]="~";
	
	for (var dettaglio in elements.ways) {
		for (var tag in elements.ways[dettaglio]) {
			if (tag == key) {
				sorted_list_of_unique_values[sorted_list_of_unique_values.length] = elements.ways[dettaglio][tag];
			}
		}
	}

	for (var dettaglio in elements.nodes) {
		for (var tag in elements.nodes[dettaglio]) {
			if (tag == key) {
				sorted_list_of_unique_values[sorted_list_of_unique_values.length] = elements.nodes[dettaglio][tag];
			}
		}
	}

	sorted_list_of_unique_values = RemoveDuplicates(sorted_list_of_unique_values.sort());
	
	loadIntoNodeAndParse("osmarender_frontend/panels/rules/select_value.xml",div_result.domNode);

	var store_values = {
		identifier: "name",
		items : []
	};
	
	for (var value_name in sorted_list_of_unique_values) {
			store_values.items[store_values.items.length]= {name: ""+sorted_list_of_unique_values[value_name]};
	}

	store_values_data_store = new dojo.data.ItemFileReadStore({data:store_values});
	dijit.byId("select_feature_value").store=store_values_data_store;

}

searchCSSfromKeyValue = function(key,value) {
	var my_key = key;
	var my_value = value;

	div_list_css = dijit.byId("list_css");

	clearContentPanes([div_list_css,dijit.byId("list_symbols")]);

	//Search classes that applies this key/value pair
	var array_da_aggiornare = new Array();
	cmyk.getClassFromRule(cmyk.getRuleModel(),my_key,my_value,array_da_aggiornare);

	if (array_da_aggiornare.length) {
		loadIntoNodeAndParse("osmarender_frontend/panels/rules/list_css.xml",div_list_css.domNode);

		var temp_CSS_list = new Array();

		for (CSSclass in array_da_aggiornare) {
			// no-bezier is actually not a class
			if (array_da_aggiornare[CSSclass] != "no-bezier") {
				temp_CSS_list[temp_CSS_list.length] = array_da_aggiornare[CSSclass];
			}
		}

		temp_CSS_list = RemoveDuplicates(temp_CSS_list.sort());

		for (CSSname in temp_CSS_list) {
			dojo.byId("select_css_class").innerHTML+='<option value="'+temp_CSS_list[CSSname]+'">'+temp_CSS_list[CSSname]+'</option>';
		}
	}

	// Search symbols that applies this key/value pair

	div_list_symbols=dijit.byId("list_symbols");

	var array_da_aggiornare = new Array();
	cmyk.getSymbolFromRule(cmyk.getRuleModel(),my_key,my_value,array_da_aggiornare);

	if (array_da_aggiornare.length) {
		loadIntoNodeAndParse("osmarender_frontend/panels/rules/list_symbols.xml",div_list_symbols.domNode);

		for (symbols in array_da_aggiornare) {
			dojo.byId("select_symbols").innerHTML+='<option value="'+array_da_aggiornare[symbols]+'">'+array_da_aggiornare[symbols]+'</option>';
		}

	}
}

loadSymbol = function(symbolname) {
	displaySymbols();
	with (dijit.byId("select_symbols")) {
		setValue(symbolname);
		onChange();
	}
}

loadCSS = function(cssname) {
	selectCSS = dijit.byId("select_class");
	selectCSS.setDisplayedValue(cssname);
	selectCSS.onChange();
	displayRules();
}


function RemoveDuplicates(arr) {
	//get sorted array as input and returns the same array without duplicates.
	var result=new Array();
	var lastValue="";
	for (var i=0; i<arr.length; i++) {
		var curValue=arr[i];
		if (curValue != lastValue) {
			result[result.length] = curValue;
		}
		lastValue=curValue;
    }
    return result;
}

//End section to port

function Osmatransform () {
	var xmlhttp = new XMLHttpRequest();  
	xmlhttp.open("GET", OSMARENDER_LOCATION+"osmarender.xsl", false);  
	xmlhttp.send('');  
	xslfile=xmlhttp.responseXML;
	
	var rulesfile=cmyk.getRulesFile();
//console.debug((new XMLSerializer).serializeToString(rulesfile));
	try{
			var processor = new XSLTProcessor();
			processor.importStylesheet(xslfile);
			var svgfile = processor.transformToDocument(rulesfile.documentElement);
			while (document.getElementById("svgfile").hasChildNodes()) {
				document.getElementById("svgfile").removeChild(document.getElementById("svgfile").firstChild);
			}
			var title_container = createElementCB("h1");
			var title = document.createTextNode("data file: "+cmyk.getOsmFileName());
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
						if (property=="stroke-width") {
							editValue+=document.getElementById("widget_"+property).nextSibling.nodeValue;
						}
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
//TODO: need to find something more attractive to do it, perhaps pasting in the main body instead of the location bar, if I can find a way to do it
	var string = new XMLSerializer().serializeToString(cmyk.getRulesFile().documentElement);
	var newWindow = window.open("","xml");
	newWindow.location="data:text/xml;charset=utf8,"+encodeURIComponent(string);
}

handleLoadPreset = function (rule_file_name,osm_file_name) {
	var setButton = function() {
		dojo.byId("span_load_preset_data").innerHTML=osm_file_name;
		dojo.byId("span_load_preset_rule").innerHTML=rule_file_name;
	}
	if (rule_file_name!="" && osm_file_name!="") {
		dojo.fadeIn({
			 node:"load_preset_button_div",
			 beforeBegin: setButton()
		}).play();
	}
	else {
		dojo.fadeOut({
			 node:"load_preset_button_div",
			 onEnd: setButton
		}).play();
	}
}

saveSVGFile = function() {
	if (document.getElementById("svgfile").childNodes.length>1) {
		var string = new XMLSerializer().serializeToString(document.getElementById("svgfile").childNodes[1]);
		var newWindow = window.open("","xml");
		newWindow.location="data:text/xml;charset=utf8,"+encodeURIComponent(string);
	}
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

setBounds = function(north,south,east,west) {
	cmyk.setBounds(north,south,east,west);
}

setShowScale = function(show,transform) {
	cmyk.setShowScale(show);
	if (transform) Osmatransform();
}

setShowGrid = function(show,transform) {
	cmyk.setShowGrid(show);
	if (transform) Osmatransform();
}

setShowBorder = function(show,transform) {
	cmyk.setShowBorder(show);
	if (transform) Osmatransform();
}

setShowLicense = function(show,transform) {
	cmyk.setShowLicense(show);
	if (transform) Osmatransform();
}

setShowInteractive = function(show,transform) {
	cmyk.setShowInteractive(show);
	if (transform) Osmatransform();
}

setScale = function(scale) {
	cmyk.setScale(scale);
}

setTextAttenuation = function(textAttenuation) {
	cmyk.setTextAttenuation(textAttenuation);
}

attachSymbol = function(symbol_id,feature_key,feature_value,symbol_width,symbol_height,symbol_layer) {
	cmyk.attachSymbol(symbol_id,feature_key,feature_value,symbol_width,symbol_height,symbol_layer);
	SymbolsResult();
}
