var orjs = {};

orjs.node_object = function() {};

orjs.way_object = function() {};

orjs.relation_object = function() {};

orjs.multipolygon_object = function() {};

orjs.e_to_object = {
	"way": "orjs.way_object",
	"node": "orjs.node_object",
	"relation" : "orjs.relation_object",
	"multipolygon" : "orjs.multipolygon_object"
}

orjs.instructions = {
	"line": "orjs.draw_lines",
	"area": "orjs.draw_areas",
	"circle": "orjs.draw_circles",
	"symbol": "orjs.draw_symbols",
	"wayMarker": "orjs.draw_way_markers",
	"caption": "orjs.draw_area_text",
	"pathText": "orjs.draw_text",
	// The following names are kept for compatibility
	"text": "orjs.draw_text",
	"areaText": "orjs.draw_area_text",
	"areaSymbol": "orjs.draw_simbols"
}

orjs.drawing_commands = new Array();

orjs.multipolygon_wayid = 0;

var osm_file;
var rule_file;

orjs.inBrowser = false;
orjs.tagSvg = "";

orjs.selection = new Array();

orjs.node_storage = new Object();
orjs.way_storage = new Object();
orjs.relation_storage = new Object();

orjs.index_way_tags = new Object();
orjs.index_node_tags = new Object();

orjs.title = "";
orjs.showBorder = "no";
orjs.showScale = "no";
orjs.showLicense = "no";
orjs.textAttenuation = "";
orjs.meter2pixel = "0.1375";
orjs.marginaliaTopHeight = 40;
orjs.marginaliaBottomHeight = 45;
orjs.extraWidth = 3;
orjs.extraHeight = 3;
orjs.svgBaseProfile;
orjs.withOSMLayers = "yes";
orjs.minlat;
orjs.minlon;
orjs.maxlat;
orjs.maxlon;
orjs.scale = 1;
orjs.symbolScale = 1;
orjs.projection;
orjs.km;
orjs.dataWidth;
orjs.dataHeight;
orjs.minimumMapWidth;
orjs.minimumMapHeight;
orjs.documentWidth;
orjs.documentHeight;
orjs.width;
orjs.height;

orjs.svgWidth;
orjs.svgHeight;

orjs.style;
orjs.outputFile;

orjs.load = function (osm_file_path,rule_file_path,inBrowser) {
	// If the resulting svg needs to be viewed in browser, we need to prepend svg: to tags
	orjs.inBrowser = inBrowser;
	if (orjs.inBrowser) orjs.tagSvg = "http://www.w3.org/2000/svg";
	orjs.loadOsmFile(osm_file_path,rule_file_path);
}

orjs.loadOsmFile = function(file_path,rule_file_path) {
	osm_file = document.implementation.createDocument("","",null);
	osm_file.onload = orjs.loadRuleFile(rule_file_path);
	// Line 233 of orp.pl
	osm_file.load(file_path);
};

orjs.loadRuleFile = function(file_path) {
	rule_file= document.implementation.createDocument("","",null);
	rule_file.onload = orjs.startProcess;
	rule_file.load(file_path);
};

orjs.startProcess = function() {
	orjs.createInternalReference();
	orjs.buildReferences();
	orjs.globalVariables();
	orjs.startSVG();
	orjs.processRules();
}

orjs.createInternalReference = function () {
	// Implementation of orp-sax.pm
	var myTreeWalker = osm_file.createTreeWalker(
		osm_file.documentElement,
		NodeFilter.SHOW_ALL,
		{ acceptNode: function(node) { return NodeFilter.FILTER_ACCEPT; } },
		false
	);
	var current_array="";
	while (myTreeWalker.nextNode()) {
		with (myTreeWalker.currentNode) {
			switch (nodeName) {
				case "node":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="node_storage";
					var current_node_object = new orjs.node_object();
					current_node_object.id = current_id,
					current_node_object.lat = getAttribute("lat"),
					current_node_object.lon = getAttribute("lon"),
					current_node_object.ways = new Array(),
					current_node_object.relations =  new Array()
					orjs.node_storage[current_id] = current_node_object;
				break;
				case "way":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="way_storage";
					var current_way_object = new orjs.way_object();
					current_way_object.id = current_id;
					current_way_object.layer = 0;
					current_way_object.nodes = new Array();
					current_way_object.relations = new Array();
					orjs.way_storage[current_id] = current_way_object;
				break;
				case "relation":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="relation_storage";
					var current_relation_object = new orjs.relation_object();
					current_relation_object.id = current_id;
					current_relation_object.members = new Array();
					current_relation_object.relations = new Array();
					orjs.relation_storage[current_id] = current_relation_object;
				break;
				case "nd":
					if (parentNode.nodeName!="way") continue;
					orjs.way_storage[parentNode.getAttribute("id")].nodes.push(getAttribute("ref"));
				break;
				case "member":
					if (parentNode.nodeName!="relation") continue;
					orjs.relation_storage[parentNode.getAttribute("id")].members.push([getAttribute("role"),getAttribute("type")+":"+getAttribute("ref")]);
				break;
				case "tag":
					var my_current_object = eval("orjs."+current_array+"["+parentNode.getAttribute("id")+"]");
					if (my_current_object["tags"]==undefined) my_current_object["tags"] = new Array();
					var json = eval ("({\""+getAttribute("k")+"\":\""+getAttribute("v")+"\"})");
					my_current_object["tags"].push(json);
					if (getAttribute("k")=="layer") {
						my_current_object.layer = getAttribute("v");
					}
				break;
				default:
			}
		}
	}
	//console.dir(orjs.node_storage);
	//console.dir(orjs.way_storage);
	//console.dir(orjs.relation_storage);
}

orjs.buildReferences = function() {
	//line 240-246
	//update way reference in nodes
	for (way_id in orjs.way_storage) {
		for (node_index in orjs.way_storage[way_id].nodes) {
			orjs.node_storage[orjs.way_storage[way_id].nodes[node_index]].ways.push(orjs.way_storage[way_id]);
		}
	}
	//line 254-271
	//update references in relations
	for (relation_id in orjs.relation_storage) {
		for (member_index in orjs.relation_storage[relation_id].members) {
			var type_id = orjs.relation_storage[relation_id].members[member_index][1].split(":");
			var deref;
			switch (type_id[0]) {
				case "node":
					deref = orjs.node_storage[type_id[1]];
				break;
				case "way":
					deref = orjs.way_storage[type_id[1]];
				break;
				case "relation":
					deref = orjs.relation_storage[type_id[1]];
				break;
				default:
			}
			orjs.relation_storage[relation_id].members[member_index][1] = deref;
			if (deref!=undefined) {
				deref.relations.push([orjs.relation_storage[relation_id].members[member_index][0],orjs.relation_storage[relation_id].members[member_index]])
			}
		}
	}
	// line 464 selection level 0
	orjs.selection[0] = new Array();
	for (way in orjs.way_storage) {
		orjs.selection[0].push(orjs.way_storage[way]);
	}
	for (node in orjs.node_storage) {
		orjs.selection[0].push(orjs.node_storage[node]);
	}

	// line 471-484
	for (way_id in orjs.way_storage) {
		for (tag_index in orjs.way_storage[way_id].tags) {
			for (key in orjs.way_storage[way_id].tags[tag_index]) {
				orjs.index_way_tags[key] = orjs.way_storage[way_id];
			}
		}
	}
	for (node_id in orjs.node_storage) {
		for (tag_index in orjs.node_storage[node_id].tags) {
			for (key in orjs.node_storage[node_id].tags[tag_index]) {
				orjs.index_node_tags[key] = orjs.node_storage[node_id];
			}
		}
	}
}

orjs.globalVariables = function() {
	with (rule_file.documentElement) {
		hasAttribute("title") ? orjs.title = getAttribute("title"):
		hasAttribute("showBorder") ? orjs.showBorder = getAttribute("showBorder"):
		hasAttribute("showScale") ? orjs.showScale = getAttribute("showScale"):
		hasAttribute("showLicense") ? orjs.showLicense = getAttribute("showLicense"):
		hasAttribute("textAttenuation") ? orjs.textAttenuation = getAttribute("textAttenuation"):
		hasAttribute("meter2pixel") ? orjs.meter2pixel = getAttribute("meter2pixel"):
		hasAttribute("scale") ? orjs.scale = parseFloat(getAttribute("scale")):
		hasAttribute("symbolsScale") ? orjs.symbolsScale = getAttribute("symbolsScale"):
		hasAttribute("minimumMapWidth") ? orjs.minimumMapWidth = parseFloat(getAttribute("minimumMapWidth")):
		hasAttribute("minimumMapHeight") ? orjs.minimumMapHeight = parseFloat(getAttribute("minimumMapHeight")):
		orjs.title == "" ? orjs.marginaliaTopHeight = 40 : orjs.showBorder == "yes" ? orjs.marginaliaTopHeight = 1.5 : orjs.marginaliaTopHeight = 0;
		(orjs.showScale == "yes" || orjs.showLicense == "yes") ? 45 : orjs.showBorder == "yes" ? 1.5 : 0;
		orjs.showBorder == "yes" ? orjs.extraWidth = 3 : orjs.extraWidth = 0;
		(orjs.title == "" && orjs.showBorder == "yes") ? orjs.extraHeight = 3 : orjs.extraHeight = 0;
		//hasAttribute("svgBaseProfile") ? orjs.svgBaseProfile = getAttribute("svgBaseProfile"):
		//FIXME change all to this
		orjs.svgBaseProfile = hasAttribute("svgBaseProfile") ? getAttribute("svgBaseProfile") : undefined;
		orjs.withOSMLayers = hasAttribute("withOSMLayers") ? getAttribute("withOSMLayers") : "yes";
	}
	//TODO: retrieve bounds from other sources
	var iterator = osm_file.evaluate("//bounds",osm_file.documentElement,null,XPathResult.UNORDERED_NODE_ITERATOR_TYPE,null);
	var boundsnode = iterator.iterateNext();
	with (boundsnode) {
		orjs.minlat = parseFloat(getAttribute("minlat"));
		orjs.minlon = parseFloat(getAttribute("minlon"));
		orjs.maxlat = parseFloat(getAttribute("maxlat"));
		orjs.maxlon = parseFloat(getAttribute("maxlon"));
	}
	// Calculate other variables
	orjs.projection = 1 / Math.cos((orjs.maxlat + orjs.minlat) / 360 * Math.PI);
	orjs.km = 0.0089928 * orjs.scale * 10000 + orjs.projection;
	orjs.dataWidth = (orjs.maxlon - orjs.minlon) * 10000 * orjs.scale;
	// original osmarender: our $dataHeight = ($maxlat - $minlat) * 10000 * $scale * $projection;
	orjs.dataHeight = (orjs.projectF(orjs.maxlat) - orjs.projectF(orjs.minlat)) * 180 / Math.PI * 10000 * orjs.scale;
	orjs.documentWidth = (orjs.minimumMapWidth == undefined || orjs.dataWidth > orjs.minimumMapWidth * orjs.km) ? orjs.dataWidth : (orjs.minimumMapWidth * orjs.km);
	orjs.documentHeight = (orjs.minimumMapHeight == undefined || orjs.dataHeight > orjs.minimumMapHeight * orjs.km) ? orjs.dataHeight : (orjs.minimumMapHeight * orjs.km);
	orjs.width = (orjs.documentWidth + orjs.dataWidth) / 2;
	orjs.height = (orjs.documentHeight + orjs.dataHeight) / 2;
	orjs.svgWidth = orjs.documentWidth + orjs.extraWidth;
	orjs.svgHeight = orjs.documentHeight + orjs.marginaliaBottomHeight + orjs.marginaliaTopHeight;
}

orjs.startSVG = function() {
	// creating File and setting main attributes
	orjs.outputFile = document.implementation.createDocument("http://www.w3.org/2000/svg","svg",null);
	with (orjs.outputFile.documentElement) {
		setAttribute("xmlns","http://www.w3.org/2000/svg");
		setAttribute("xmlns:svg","http://www.w3.org/2000/svg");
		setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		setAttribute("xmlns:xi","http://www.w3.org/2001/XInclude");
		setAttribute("xmlns:inkscape","http://www.inkscape.org/namespaces/inkscape");
		setAttribute("xmlns:cc","http://web.resource.org/cc/");
		setAttribute("xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		setAttribute("id","main");
		setAttribute("version","1.1");
		setAttribute("baseProfile",orjs.svgBaseProfile);
		setAttribute("width",orjs.svgWidth+"px");
		setAttribute("height",orjs.svgHeight+"px");
		setAttribute("preserveAspectRatio","none");
		setAttribute("viewBox","-"+(orjs.extraWidth/2)+" -"+(orjs.extraHeight/2)+" "+orjs.svgWidth+" "+orjs.svgHeight);
	}

	// Insert processing instruction
	var iterator = rule_file.evaluate("//processing-instruction()",rule_file.documentElement,null,XPathResult.UNORDERED_NODE_ITERATOR_TYPE,null);
	var processing_instruction = iterator.iterateNext();
	if (processing_instruction) {
		orjs.style = processing_instruction;
		orjs.outputFile.appendChild(orjs.outputFile.createProcessingInstruction(orjs.style.target, orjs.style.data));
	}

	// Copy definitions from rule file
	var defs = orjs.outputFile.documentElement.appendChild(document.createElementNS(orjs.tagSvg,"defs"));
	defs.setAttribute("id","defs-rulefile");
	var original_defs = rule_file.evaluate("//rules/defs/*[local-name() != 'svg' and local-name() != 'symbol']",rule_file.documentElement,null,XPathResult.ANY_TYPE,null);
	//FIXME: check if this is the desired behavior, creating the <style></style> tag inside the svg file
	defs.appendChild(original_defs.iterateNext());

	// Clipping rectangle for the map
	//TODO: change all the various callings to createelement/setattribute to direct parsing form string
	var clip_path = document.createElementNS(orjs.tagSvg,"clipPath");
		clip_path.setAttribute("id","map-clipping");
	var rect = document.createElementNS(orjs.tagSvg,"rect");
		rect.setAttribute("id","map-clipping-rect");
		rect.setAttribute("x","0px");
		rect.setAttribute("y","0px");
		rect.setAttribute("height",orjs.documentHeight+"px");
		rect.setAttribute("width",orjs.documentWidth+"px");
	clip_path.appendChild(rect);
	orjs.outputFile.documentElement.appendChild(clip_path);

	// Start of main drawing
	var starting_g = document.createElementNS(orjs.tagSvg,"g");
	with (starting_g) {
		setAttribute("id","map");
		setAttribute("clip-path","url(#map-clipping)");
		setAttribute("inkscape:groupmode","layer");
		setAttribute("inkscape:label","Map");
		setAttribute("transform","translate(0,"+orjs.marginaliaTopHeight+")");
	}

	// Draw a nice background layer
	var rect = document.createElementNS(orjs.tagSvg,"rect");
		rect.setAttribute("id","background");
		rect.setAttribute("x","0px");
		rect.setAttribute("y","0px");
		rect.setAttribute("height",orjs.documentHeight+"px");
		rect.setAttribute("width",orjs.documentWidth+"px");
		rect.setAttribute("class","map-background");

	orjs.outputFile.documentElement.appendChild(rect);
}

// Line 737
orjs.processRules = function() {
	var rule_list = rule_file.evaluate("//rules/rule",rule_file.documentElement,null,XPathResult.ANY_TYPE,null);
	while (current_rule = rule_list.iterateNext()) {
		// First rule tag
		var depth = 0;
		var layer = 0;
		if (orjs.withOSMLayers == "no") {
			orjs.process_rule(current_rule,depth,layer);
		}
		else {
			// Process all layers
			orjs.process_rule(current_rule,depth,layer);
			
			// TODO:Render z-mode=bottom line 750
			
			// prepare z-mode=normal
			var normalInstructions;
			for (command_index in orjs.drawing_commands) {
				console.dir(orjs.drawing_commands[command_index]);
				break;
			}
		}
		//var children = current_rule.getChildren();
		
	}
//	console.debug(XML((new XMLSerializer()).serializeToString(orjs.outputFile)).toXMLString());
	//console.debug((new XMLSerializer()).serializeToString(orjs.outputFile));
	document.getElementById("resulting_svg").appendChild(orjs.outputFile.documentElement);
}

orjs.process_rule = function (node,depth,layer,previous) {
	var rule_layer = node.hasAttribute("layer") ? node.getAttribute("layer") : "";
	if (rule_layer != "" && layer!=undefined) {
		if (rule_layer != layer) {
			return;
		}
		layer = undefined;
	}
	// Part 1 of process rule: line 1.151
	if (node.nodeName == "rule") {
		orjs.selection[depth+1] = new Array();
		orjs.selection[depth+1] = orjs.makeSelection(node, orjs.selection[depth]);
	}
	else if (node.nodeName == "else") {
		//TODO
		console.debug("else");
		// "else" selection - a selection for our level of 
		// recursion already exists (from the previous rule) and 
		// we need to select all objects that are present in the
		// selection one level up and not in the previous rule's
		// selection (which is on our level of recursion).

		//orjs.selection[depth+1] = orjs.selection
	}

	var selected = orjs.selection[depth+1];
	if (!selected.length) return;

	// ----------------------------------------------------
	// Part 2 of process_rule:
	//the selection is complete; iterate over child nodes
	// of the rule and either do recursive rule processing, 
	// or execute drawing instructions.
	// ----------------------------------------------------

	var previous_child;
	for (index_instruction in node.childNodes) {
		var instruction = node.childNodes[index_instruction];
		if (instruction.nodeType != node.ELEMENT_NODE) continue;
if (instruction.nodeName!="rule" && instruction.nodeName!="else" && instruction.nodeName!="#comment" && instruction.nodeName!="#text") {
	console.debug("INSTRUCTION");
	console.dir(instruction);
}
		var name = instruction.nodeName || "";
		if (name == "layer") {
			orjs.process_layer(instruction,depth+1,layer);
		}
		else if (name == "rule") {
			orjs.process_rule(instruction,depth+1,layer);
		}
		else if (name == "else") {
			if (previous_child == undefined || previous_child.nodeName != "rule") {
				console.debug("<else> not following <rule>, ignored");
			}
			else {
				orjs.process_rule(instruction,depth+1,layer,previous_child);
			}
		}
		else if (orjs.instructions[name]!=undefined) {
			console.debug("processing instruction with name: "+name);
			var command = {"instruction":instruction,"elements":selected};
			orjs.drawing_commands.push(command);
		}
		else if (name != "") {
			console.debug("unknown drawing instruction "+name+", ignored");
		}
		if (name != "") previous_child == node;
	}
}

orjs.makeSelection = function(node, oldsel) {
	var k = node.getAttribute("k");
	var v = node.getAttribute("v");

	// read the "e" attribute of the rule (type of element)
	// and execute the selection for these types. "e" is assumed
	// to be either "node", "way", or "node|way".

	var e = node.getAttribute("e");
	var s = node.hasAttribute("s") ? node.getAttribute("s") : null;
	var rows_affected;

	// make sure $e is either "way" or "node" or undefined (=selects both)

	var e_pieces = new Object();
	var temp_e = e.split("|");
	for (selection_type in temp_e) {
		eval("e_pieces."+temp_e[selection_type]+" = ''");
	}

	if (e_pieces["way"] != undefined && e_pieces["node"] != undefined) e_pieces = undefined;
	if (e_pieces != undefined && e_pieces["way"] != undefined) e = "way";
	if (e_pieces |= undefined && e_pieces["node"] != undefined) e = "node";

	var interim;
console.debug("e: "+e+" s: "+s+" k: "+k+" v: "+v);

	if (k == "*" || k == undefined) {
		// rules that apply to any key. these don't occur often
		// but are in theory supported by osmarender.

		if (v == "~") {
			// k=* v=~ means elements without tags.
			// FIXME "s"
			interim = orjs.select_elements_without_tags(oldsel,e);
		}
		else if (v == "*") {
			// k=* v=* means elements with any tag.
			// FIXME "s"
			interim = orjs.select_elements_with_any_tag(oldsel,e);
		}
		else {
			// k=* v=something means elements with a tag that has the
			// value "something". "something" may be a pipe-separated
			// list of values. The "~" symbol is not supported in the
			// list.
			// FIXME "s"
			interim = orjs.select_elements_with_given_tag_value(oldsel,e,v);
		}
	}
	else {
		// rules that apply to the specifc key given in $k. This may
		// be a pipe-separated list of values.

		if (v == "*") {
			// objects that have the given key(s), with any value.
			// FIXME "s"
			interim = orjs.select_elements_with_given_tag_key(oldsel,e,k);
		}
		else if (v == "~") {
			// objects that don't have the key(s)
			// FIXME "s"
			interim = orjs.select_elements_without_given_tag_key(oldsel,e,k);
		}
		else if (s == null && v.indexOf("~") == -1) {
			// objects that have the given keys and values, where none of the
			// values is "~"
			interim = orjs.select_elements_with_given_tag_key_and_value_fast(oldsel,e,k,v);
		}
		else if (s == "way" && v.indexOf("~") == -1) {
			// nodes that belong to a way that has the given keys and values,
			// where none of the values is "~"
			interim = orjs.select_nodes_with_given_tag_key_and_value_for_way_fast(oldsel,k,v);
		}
		else {
			// the code that can handle "~" in values (i.e. rules like "the 
			// 'highway' tag must be 'bridleway' or not present at all)
			// is slower since it cannot use indexes.
			interim = orjs.select_elements_with_given_tag_key_and_value_slow(oldsel,e,k,v,s);
		}
	}
console.dir(interim);

	if (interim==undefined) console.debug("something is wrong");

	// post-process the selection according to proximity filter, if set.

	// the following control the proximity filter. horizontal and vertical proximity
	// control the size of the imaginary box drawn around the point where the label is
	// placed. proximityClass is a storage class; if it is shared by multiple rules,
	// then all these rules compete for the same space. If no class is set then the
	// filter only works on objects selected by this rule.
	// FIXME: proximity filtering does not take text length into account, and boxes
	// are currently based on lat/lon values to remain compatible to Osmarender,
	// yielding reduced spacings the closer you get to the poles.

	var hp = node.hasAttribute("horizontalProximity") ? node.getAttribute("horizontalProximity") : null;
	var vp = node.hasAttribute("verticalProximity") ? node.getAttribute("verticalProximity") : null;
	var pc = node.hasAttribute("proximityClass") ? node.getAttribute("proximityClass") : null;
	if (hp != null && vp != null) {
		interim = orjs.select_proximity(interim, hp, vp, pc);
	}

	// post-process with minSize filter, if set
	var minsize = node.hasAttribute("minSize") ? node.getAttribute("minSize") : null;
	if (minsize != null) {
		interim = orjs.select_minsize(interim, minsize);
	}

	// post-process with notConnectedSameTag filter, if set
	var notConnectedSameTag = node.hasAttribute("notConnectedSameTag") ? node.getAttribute("notConnectedSameTag") : null;
	if (notConnectedSameTag != null) {
		interim = orjs.select_not_connected_same_tag(interim,notConnectedSameTag);
	}

	// Filter for closed or unclosed ways if closed= is set
	var closed = node.hasAttribute("closed") ? node.getAttribute("closed") : null;
	if (closed != null) {
		if (closed != "yes" && closed != "no") {
			console.debug("Error in stylesheet: <rule closed= must be 'yes' or 'no'");
		}
		orjs.select_closed(interim,closed);
	}

	return interim;
}

// Lines 1583-1588
orjs.projectF = function (projected) {
	var Lat = projected / 180 * Math.PI;
	//sec = 1/cos
	var Y = Math.log(Math.tan(Lat) + (1/Math.cos(Lat)));
	return Y;
}

// Implementation of orp-select.pm

orjs.select_elements_with_given_tag_key_and_value_slow = function(oldsel,e,k,v,s) {
	var values_wanted = v.split("|");
	var newsel = new Array();
	var keys_wanted = k.split("|");

outer:
	for (member in oldsel) {
		var instance_string_e = orjs.e_to_object[e];
		if (instance_string_e != undefined && e != undefined && eval("!(oldsel[member] instanceof "+instance_string_e+" )") && !(e == "way" && (oldsel[member] instanceof orjs.multipolygon_object))) continue;
		// determine whether we're comparing against the tags of the object
		// itself or the tags selected with the "s" attribute.
		var tagsets;
		if (s == "way") {
			tagsets = new Array();
			if (oldsel[member].ways != undefined) {
				for (way in oldsel[member].ways) {
					tagsets.push(way.tags);
				}
			}
		}
		else {
			tagsets = oldsel[member].tags;
		}
		for (key in keys_wanted) {
			for (value in values_wanted) {
				for (tagset in tagsets) {
					var keyval = tagsets[tagset][keys_wanted[key]];
					if ((values_wanted[value] == "~" && keyval == undefined) || (keyval!=undefined && values_wanted[value] == keyval)) {
						newsel.push(oldsel[member]);
						continue outer;
					}
				}
			}
		}

	}
	return newsel;
}

orjs.select_elements_with_given_tag_key_and_value_fast = function(oldsel,e,k,v) {
	var values_wanted = v.split("|");
	var newsel = new Array();
	var keys_wanted = k.split("|");
	for (key in keys_wanted) {
		// retrieve list of objects with this key from index.
		var objects = new Array();
		if (e != undefined && e == "way") {
			if (orjs.index_way_tags[keys_wanted[key]]!=undefined) {
				objects.push(orjs.index_way_tags[keys_wanted[key]]);
			}
			else {
				objects.push(new Array());
			}
		}
		else if (e != undefined && e == "node") {
			if (orjs.index_node_tags[keys_wanted[key]]!=undefined) {
				objects.push(orjs.index_node_tags[keys_wanted[key]]);
			}
			else {
				objects.push(new Array());
			}
		}
		else {
			objects.push(orjs.index_way_tags[keys_wanted[key]]);
			objects.push(orjs.index_node_tags[keys_wanted[key]]);
		}
		outer:
		for (element in objects) {
			if (!orjs.arrayContains(oldsel,objects[element])) continue;
			for (value in values_wanted) {
				if (objects[element].tags[keys_wanted[key]]!=undefined && objects[element].tags[keys_wanted[key]] == values_wanted[value]) {
					newsel.push(objects[element]);
					continue outer;
				}
			}
		}
	}
	return newsel;
}

orjs.select_elements_without_given_tag_key = function(oldsel,e,k) {
	var newsel = new Array();
	var keys_wanted = k.split("|");

outer:
	for (member_index in oldsel) {
		var instance_string_e = orjs.e_to_object[e];
		if (instance_string_e != undefined && e!=undefined && eval("!(oldsel[member_index] instanceof "+instance_string_e+" )") && !(e == "way" && (oldsel[member_index] instanceof orjs.multipolygon_object))) continue;
		for (key_index in keys_wanted) {
			if (oldsel[member_index].tags[keys_wanted[key_index]] != undefined) continue outer;
		}
		newsel.push(oldsel[member_index]);
	}
	return newsel;
}

orjs.select_elements_with_given_tag_key = function(oldsel,e,k) {
	var newsel = new Array();
	var keys_wanted = k.split("|");
	var instance_string_e = orjs.e_to_object[e];
outer:
	for (member_index in oldsel) {
		if (instance_string_e != undefined && e!=undefined && eval("!(oldsel[member_index] instanceof "+instance_string_e+" )") && !(e == "way" && (oldsel[member_index] instanceof orjs.multipolygon_object))) continue;
		for (key_index in keys_wanted) {
			if (oldsel[member_index].tags[keys_wanted[key_index]] != undefined) {
				newsel.push(oldsel[member_index]);
				continue outer;
			}
		}
	}
	return newsel;
}

orjs.select_closed = function (selection,closed) {
	for (member_index in selection) {
		//TODO: select_closed function
		console.debug("Function select_closed not implemented");
	}
}

orjs.select_nodes_with_given_tag_key_and_value_for_way_fast = function(oldsel,k,v) {
	var values_wanted = v.split("|");
	var newsel = new Array();
	var keys_wanted = v.split("|");
	for (key_index in keys_wanted) {
		// process only those from oldsel that have this key.
		for (way_index in orjs.index_way_tags[keys_wanted[key_index]]) {
			var way = orjs.index_way_tags[keys_wanted[key_index]][way_index];
			for (value_index in values_wanted) {
				if (way.tags[keys_wanted[key_index]] != undefined && way.tags[keys_wanted[key_index]]==values_wanted[value_index]) {
					for (node_index in way.nodes) {
						if (!orjs.arrayContains(oldsel,way.nodes[node_index])) continue;
						newsel.push(way.nodes[node_index]);
					}
				}
			}
		}
	}
	return newsel;
}

// notConnectedSameTag filter
// it selects objects which are not connected to at least one other object with the same value of $tag

orjs.select_not_connected_same_tag = function(oldsel,tag) {
	var newsel = new Array();
outer:
	for (member_index in oldsel) {
		// only ways are supported for now
		if (!(oldsel[member_index] instanceof orjs.way_object)) {
			newsel.push(oldsel[member_index]);
			continue;
		}
		var value = oldsel[member_index].tags[tag];
		if (value == undefined) continue;

		for (node_index in oldsel[member_index].nodes) {
			var node = oldsel[member_index].nodes[node_index];
			for (way_index in oldsel[member_index].ways) {
				var way = oldsel[member_index].ways[way_index];
				var otherValue = way.tags[tag];
				if (otherValue == undefined) continue;
				if (way.id == oldsel[member_index].id) continue;
		                // skip element if other element with the same value is connected
				if (otherValue == value) continue outer;
			}
		}
		newsel.push(oldsel[member_index]);
	}
	return newsel;
}

// Helper
orjs.arrayContains = function(array,element) {
	for (element_index in array) {
		if (array[element_index] === element) {
			return true;
		}
	}
	return false;
}