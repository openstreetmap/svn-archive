dojo.provide("osmarender_frontend.widgets.css_editor.css_editor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.Dialog");

dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorWidgetFactory");
dojo.requireLocalization("osmarender_frontend.widgets.css_editor", "strings");

dojo.declare("osmarender_frontend.widgets.css_editor.css_editor", [dijit._Widget,dijit._Templated], {
	templatePath: dojo.moduleUrl("osmarender_frontend.widgets.css_editor","css_editor.html"),
	widgetsInTemplate: true,
	_messages:null,
	CSSproperties: null,
	images: null,
	_myclass: null,
	_mywidgets: new Array(),
	_this: null,
	subscribedHandle: null,
	
	constructor: function() {
		this._messages = dojo.i18n.getLocalization("osmarender_frontend.widgets.css_editor", "strings");
		//Necessary otherwise subscribed functions can't reference to this object
		_this=this;
	},

	postCreate : function() {
		this.subscribedHandle = dojo.subscribe("osmarender_frontend.widgets.css_editor.propertyEditorWidget.deleteStyles",null,this._deleteStyle);
		for (property in this.CSSproperties) {
			//create a instance of editor widget, markupFactory function will take care of selecting the proper editor widget
			var editorWidgetFactory = new osmarender_frontend.widgets.css_editor._propertyEditorWidgetFactory();
			var singleEditor = editorWidgetFactory.markupFactory(property,this.CSSproperties[property],this.images);
			this._mywidgets.push(singleEditor);
			this.div_properties.appendChild(singleEditor.domNode);
		}
	},

	_addCSSProperty: function(args) {
		this.dialogAddCSSProperty.hide();
		var objectToPublish = {
			CSSclass: this._myclass,
			CSSname: this.addCSSPropertyName.value,
			CSSvalue: this.addCSSPropertyValue.value
		}
		dojo.publish("osmarender_frontend.widgets.css_editor.addStyle",[objectToPublish]);
	},

	_saveStyle: function() {
		var objectToPublish = new Array();
		dojo.forEach(this._mywidgets,
			function(widget,index,array) {
				objectToPublish.push({
					CSSclass: _this._myclass,
					property: widget.getCSSWidgetProperty(),
					editValue: widget.getCSSWidgetValue()
				});
			}
		);
		dojo.publish("osmarender_frontend.widgets.css_editor.setStyle",[objectToPublish]);
	},

	_deleteStyle: function(styleName) {
		//This method uses a publish/subscribe
		// Unsubscribe handle... always needed otherwise it remains in memory even if this object no longer exists
		dojo.unsubscribe(_this.subscribedHandle);
		// Need to transfer an object, an array doesn't work
		dojo.publish("osmarender_frontend.widgets.css_editor.deleteStyle",[{CSSclass: _this._myclass, style: styleName}]);
	},

	_viewCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.show();
	},

	_closeCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.hide();
	}
});