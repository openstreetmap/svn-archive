/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.load.local");

dojo.require("dijit._Widget");
dojo.require("dojox.dtl._Templated");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend.load", "local");

/**
@lends osmafrontend.load
*/
dojo.declare("osmafrontend.load.local", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend.load","local.html"),

	widgetsInTemplate: true,

	constructor: function() {
		this._nls = dojo.i18n.getLocalization("osmafrontend.load","local");
	},

	postCreate: function() {
		this.inherited(arguments);
	}
});

 
