/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.load.preset");

dojo.require("dijit._Widget");
dojo.require("dojox.dtl._Templated");

dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.Button");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend.load", "load");
dojo.requireLocalization("osmafrontend.load", "preset");

/**
@lends osmafrontend.load
*/
dojo.declare("osmafrontend.load.preset", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend.load","preset.html"),

	widgetsInTemplate: true,

	defaultOSMFiles: {
		"Simple Data": "data.xml",
		"Small section of London": "somewhere_in_london.xml",
		"Center of Rome": "rome_centre.xml",
		"Google in Zurich": "zurich_google.xml"
	},

	defaultRuleFiles: {
		"Zoom 12": "osm-map-features-z12.xml",
		"Zoom 13": "osm-map-features-z13.xml",
		"Zoom 14": "osm-map-features-z14.xml",
		"Zoom 15": "osm-map-features-z15.xml",
		"Zoom 16": "osm-map-features-z16.xml",
		"Zoom 17": "osm-map-features-z17.xml"
	},

	constructor: function() {
		this._nls = {
                  load: dojo.i18n.getLocalization("osmafrontend.load","load"),
                  preset: dojo.i18n.getLocalization("osmafrontend.load","preset")
		};
	},

	_onChange : function () {
		rule_file_name = this.preset_rules_file_name.value;
		osm_file_name = this.preset_osm_file_name.value;

		var setButton = function() {
			dojo.byId("span_load_preset_data").innerHTML=osm_file_name;
			dojo.byId("span_load_preset_rule").innerHTML=rule_file_name;
		}
		if (rule_file_name!="" && osm_file_name!="") {
			dojo.fadeIn({
				node:this.button_load_preset.domNode,
				beforeBegin: setButton
			}).play();
		}
		else {
			dojo.fadeOut({
				node:this.button_load_preset.domNode,
				onEnd: setButton
			}).play();
		}
	},

	_loadData: function() {
		osmarender_frontend.loadOsmRule(this.preset_osm_file_name.value,this.preset_rules_file_name.value)
	},

	postCreate: function() {
		this.inherited(arguments);
	},

	startup: function() {
		var widget = this;
		dojo.addOnLoad(
			function() {
				widget._onChange();
			}
		);
	}
});

 
