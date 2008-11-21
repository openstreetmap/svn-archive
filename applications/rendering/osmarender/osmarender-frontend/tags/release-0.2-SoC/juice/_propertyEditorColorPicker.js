dojo.provide("juice._propertyEditorColorPicker");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.Dialog");
dojo.require("dojox.widget.ColorPicker");
dojo.require("juice._propertyEditorWidget");

dojo.declare("juice._propertyEditorColorPicker",[dijit._Widget,dijit._Templated,juice._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("juice","_propertyEditorColorPicker.html"),
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
