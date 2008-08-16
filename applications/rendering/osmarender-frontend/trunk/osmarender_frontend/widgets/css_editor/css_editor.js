dojo.provide("osmarender_frontend.widgets.css_editor.css_editor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.requireLocalization("osmarender_frontend.widgets.css_editor", "strings");

dojo.declare("osmarender_frontend.widgets.css_editor.css_editor", [dijit._Widget,dijit._Templated], {
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","css_editor.html"),
	_messages:null,
	
	constructor: function() {
		this._messages = dojo.i18n.getLocalization("osmarender_frontend.widgets.css_editor", "strings");
	},

	_addCSSProperty: function() {
// add css property, pass it?
//		javascript:addCSSProperty(this);
	},
	_saveStyle: function() {
// save the current style, probably I can pass a callback function and externalise cmyk settings
//		javascript:setStyle();
	}
});