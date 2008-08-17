dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

//Every child object need to define <button onclick="javascript:dojo.publish('osmarender_frontend.widgets.css_editor.propertyEditorWidget.deleteStyles',['${_property}'])">Delete</button> for publishing that a property needs to be deleted


dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorWidget",[dijit._Widget,dijit._Templated],{
	_property: null,
	_value: null,
	_images: null,

	constructor: function(property,value,images) {
		this._property=property;
		this._value=value;
		this._images=images;
	},

	getCSSWidgetValue : function() {
		return this._value;
	},

	getCSSWidgetProperty : function() {
		return this._property;
	}
});
 
 
