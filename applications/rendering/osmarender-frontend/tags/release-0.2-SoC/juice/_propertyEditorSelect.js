dojo.provide("juice._propertyEditorSelect");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("juice._propertyEditorWidget");

dojo.declare("juice._propertyEditorSelect",[dijit._Widget,dijit._Templated,juice._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("juice","_propertyEditorSelect.html"),
	_types_of_strokes : {
		"stroke-linecap": ["butt","round","square","inherit"],
		"stroke-linejoin": ["miter","round","bevel","inherit"],
		"font-weight": ["normal","bold","bolder","lighter","100","200","300","400","500","600","700","800","900","inherit"],
		"text-anchor": ["start","middle","end","inherit"],
		"display": ["inline","block","list-item","run-in","compact","marker","table","inline-table","table-row-group","table-header-group","table-footer-group","table-row","table-column-group","table-column","table-cell","table-caption","none","inherit"],
		"fill-rule": ["nonzero","evenodd","inherit"]
	},

	postCreate: function() {
		for (types in this._types_of_strokes[this._property]) {
			var selectedValue="";
			if (this._types_of_strokes[this._property][types]==this._value) {
				selectedValue=' selected="selected"';
			}
			else {
				selectedValue="";
			}
			this.editorSelect.innerHTML+='<option value="'+this._types_of_strokes[this._property][types]+'"'+selectedValue+'>'+this._types_of_strokes[this._property][types]+'</option>';
		}
	},

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value=""+this.editorSelect.value;
	}
});
