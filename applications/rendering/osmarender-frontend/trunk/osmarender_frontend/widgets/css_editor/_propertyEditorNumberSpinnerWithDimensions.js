dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinnerWithDimensions");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.form.NumberSpinner");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorWidget");

dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinnerWithDimensions",[dijit._Widget,dijit._Templated,osmarender_frontend.widgets.css_editor._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","_propertyEditorNumberSpinnerWithDimensions.html"),
	_delta_for_editing: null,
	_numbervalue: null,
	_dimensionvalue: null,
	widgetsInTemplate: true,

	postMixInProperties: function() {
		//TODO: compatibility with other dimensions
		if (this._value.indexOf(".")!=-1) this._delta_for_editing = 0.1; else this._delta_for_editing = 1;
		this._numbervalue = this._value.substring(0,this._value.indexOf("p"));
		this._dimensionvalue = this._value.substring(this._value.indexOf("p"));
	},

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._numbervalue=this.editorNumberSpinner.value;
		this._value=""+this._numbervalue+this._dimensionvalue;
	}

});
