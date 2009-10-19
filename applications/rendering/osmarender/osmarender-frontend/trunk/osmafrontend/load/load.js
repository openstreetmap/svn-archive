/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.load.load");

dojo.require("osmafrontend.load.preset");
dojo.require("osmafrontend.load.local");
dojo.require("osmafrontend.load.url");

dojo.require("dijit._Widget");
dojo.require("dojox.dtl._Templated");

dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.ProgressBar");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend.load", "load");

/**
@lends osmafrontend.load
*/
dojo.declare("osmafrontend.load.load", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend.load","load.html"),

	widgetsInTemplate: true,

	constructor: function() {
		this._nls = dojo.i18n.getLocalization("osmafrontend.load","load");
	},

	postCreate: function() {
		this.inherited(arguments);
		//workaround otherwise tab_container would display content panes one under the other
		this.load_tab_container.resize();
	}
});

 
