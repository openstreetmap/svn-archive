dojo.provide("juice._propertyEditorNumberSpinner");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.form.NumberSpinner");
dojo.require("juice._propertyEditorWidget");

dojo.declare("juice._propertyEditorNumberSpinner",[dijit._Widget,dijit._Templated,juice._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("juice","_propertyEditorNumberSpinner.html"),
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
