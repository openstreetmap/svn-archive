/*
	@namespace osmafrontend
*/

dojo.provide("osmafrontend.version");

dojo.require("dojox.dtl._Templated");
dojo.require("dijit._Widget");

dojo.require("dojo.i18n");
dojo.requireLocalization("osmafrontend", "version");

/**
@lends osmafrontend.osmafrontend
*/
dojo.declare("osmafrontend.version", [dijit._Widget,dojox.dtl._Templated], {

	templatePath: dojo.moduleUrl("osmafrontend","version.html"),

	constructor: function() {
		this._version = osmarender_frontend.version;
		this._date = osmarender_frontend.date;
		this._nls = dojo.i18n.getLocalization("osmafrontend","version");
	}
});

 
