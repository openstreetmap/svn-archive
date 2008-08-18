dojo.provide("juice._propertyEditorText");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("juice._propertyEditorWidget");

dojo.declare("juice._propertyEditorText",[dijit._Widget,dijit._Templated,juice._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("juice","_propertyEditorText.html"),

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value=this.propertyText.value;
	}

});
 
