dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorColorPicker");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.Dialog");
dojo.require("dojox.widget.ColorPicker");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorWidget");

dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorColorPicker",[dijit._Widget,dijit._Templated,osmarender_frontend.widgets.css_editor._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","_propertyEditorColorPicker.html"),
	widgetsInTemplate: true,

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value=""+this.colorText.value;
		this._refreshBackgroundViewer();
	},

	_refreshBackgroundViewer: function() {
		this._value=""+this.colorText.value;
		this.backgroundViewer.style.backgroundColor=this._value;
	},

	_viewColorPicker: function() {
		this._value=""+this.colorText.value;
		this.dialogColorPicker.show();
	},

	_closeColorPicker: function() {
		this.dialogColorPicker.hide();
	},

	_saveColor: function() {
		this.dialogColorPicker.hide();
		this._value=this.colorPicker.value;
		this.colorText.value=this._value;
		this._refreshBackgroundViewer();
	}

});
