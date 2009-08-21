/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.load");

dojo.require("dojox.dtl._Templated");
dojo.require("dijit._Widget");

dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.ProgressBar");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend", "load");

/**
@lends osmafrontend.load
*/
dojo.declare("osmafrontend.load", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend","load.html"),
	widgetsInTemplate: true,

	constructor: function() {
		this._nls = dojo.i18n.getLocalization("osmafrontend","load");
	},

	_onChange : function () {
		/*var rule_file_name;
		var osm_file_name;
		switch (type) {
			case "preset":
				rule_file_name = this.preset_rules_file_name;
				osm_file_name = this.preset_osm_file_name;
			break;
			default:
		}
		var setButton = function() {
			this.span_load_preset_data.innerHTML=osm_file_name;
			this.span_load_preset_rule.innerHTML=rule_file_name;
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
		}*/
	},

	postCreate: function() {
		this.inherited(arguments);
		//workaround otherwise tab_container would display content panes one under the other
		this.load_tab_container.resize();
	}
});

 
