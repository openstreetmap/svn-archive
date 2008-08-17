dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinner");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.form.NumberSpinner");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorWidget");

dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinner",[dijit._Widget,dijit._Templated,osmarender_frontend.widgets.css_editor._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","_propertyEditorNumberSpinner.html"),
	_delta_for_editing: null,
	_constraints: null,
	widgetsInTemplate: true,


	postMixInProperties: function() {
		if (this._value.indexOf(".")!=-1) this._delta_for_editing = 0.1; else this._delta_for_editing = 1;
		if (this._property=="stroke-miterlimit") this._constraints="{min:0}"; else this._constraints="{min:0,max:1}";
	},

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value=""+this.editorNumberSpinner.value;
	}
});
