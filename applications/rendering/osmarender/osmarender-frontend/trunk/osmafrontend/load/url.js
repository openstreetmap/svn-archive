/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.load.url");

dojo.require("dijit._Widget");
dojo.require("dojox.dtl._Templated");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend.load", "url");

/**
@lends osmafrontend.load
*/
dojo.declare("osmafrontend.load.url", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend.load","url.html"),

	widgetsInTemplate: true,

	constructor: function() {
		this._nls = dojo.i18n.getLocalization("osmafrontend.load","url");
	},

	postCreate: function() {
		this.inherited(arguments);
	}
});

 
