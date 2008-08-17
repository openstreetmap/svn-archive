dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorText");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorWidget");

dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorText",[dijit._Widget,dijit._Templated,osmarender_frontend.widgets.css_editor._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","_propertyEditorText.html"),

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value=this.propertyText.value;
	}

});
 
