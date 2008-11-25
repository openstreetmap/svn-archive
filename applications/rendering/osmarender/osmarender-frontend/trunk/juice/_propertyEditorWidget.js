dojo.provide("juice._propertyEditorWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dojo.i18n"); 
dojo.requireLocalization("juice", "main");

/* 
Every child object need to define 
<button onclick="javascript:dojo.publish('juice.propertyEditorWidget.deleteStyles',['${_property}'])">${_mainmessages.buttonDelete}</button>
for publishing that a property needs to be deleted.
*/

dojo.declare("juice._propertyEditorWidget",[dijit._Widget,dijit._Templated],{
	_property: null,
	_value: null,
	_images: null,
	_mainmessages: null,

	constructor: function() {
		this._mainmessages = dojo.i18n.getLocalization("juice", "main");
	},

	getCSSWidgetValue : function() {
		return this._value;
	},

	getCSSWidgetProperty : function() {
		return this._property;
	}
});
 
 
