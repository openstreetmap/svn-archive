var orjs = {};

var osm_file;
var rule_file;

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

orjs.load = function (osm_file_path,rule_file_path) {
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
}

orjs.createInternalReference = function () {
	// Implementation of orp-sax.pm
	var myThreeWalker = osm_file.createTreeWalker(
		osm_file.documentElement,
		NodeFilter.SHOW_ALL,
		{ acceptNode: function(node) { return NodeFilter.FILTER_ACCEPT; } },
		false
	);
	var current_array="";
	while (myThreeWalker.nextNode()) {
		with (myThreeWalker.currentNode) {
			switch (nodeName) {
				case "node":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="node_storage";
					orjs.node_storage[current_id] = {
						"id": current_id,
						"lat": getAttribute("lat"),
						"lon": getAttribute("lon"),
						"ways": new Array(),
						"relations": new Array()
					}
				break;
				case "way":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="way_storage";
					orjs.way_storage[current_id] = {
						"id": current_id,
						"layer": 0,
						"nodes": new Array(),
						"relations": new Array()
					}
				break;
				case "relation":
					if (getAttribute("action")=="delete") break;
					var current_id = getAttribute("id");
					current_array="relation_storage";
					orjs.relation_storage[current_id] = {
						"id": current_id,
						"members": new Array(),
						"relations": new Array()
					}
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
	console.dir(orjs.node_storage);
	console.dir(orjs.way_storage);
	console.dir(orjs.relation_storage);
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
console.dir(orjs);
}

// Lines 1583-1588
orjs.projectF = function (projected) {
	var Lat = projected / 180 * Math.PI;
	//sec = 1/cos
	var Y = Math.log(Math.tan(Lat) + (1/Math.cos(Lat)));
	return Y;
}