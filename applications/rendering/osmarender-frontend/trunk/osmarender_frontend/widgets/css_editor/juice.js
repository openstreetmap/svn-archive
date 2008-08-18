dojo.provide("juice.juice");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.Dialog");

dojo.require("juice._propertyEditorWidgetFactory");

dojo.require("dojo.i18n");
dojo.requireLocalization("juice", "main");

dojo.declare("juice.juice", [dijit._Widget,dijit._Templated], {
	templatePath: dojo.moduleUrl("juice","juice.html"),
	widgetsInTemplate: true,
	_mainmessages:null,
	CSSproperties: null,
	images: null,
	_myclass: null,
	_mywidgets: new Array(),
	_this: null,
	subscribedHandle: null,
	
	constructor: function() {
		this._mainmessages = dojo.i18n.getLocalization("juice", "main");
		//Necessary otherwise subscribed functions can't reference to this object
		_this=this;
	},

	postCreate : function() {
		this.subscribedHandle = dojo.subscribe("juice.propertyEditorWidget.deleteStyles",null,this._deleteStyle);
		for (property in this.CSSproperties) {
			//create a instance of editor widget, markupFactory function will take care of selecting the proper editor widget
			var editorWidgetFactory = new juice._propertyEditorWidgetFactory();
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
		dojo.publish("juice.addStyle",[objectToPublish]);
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
		dojo.publish("juice.setStyle",[objectToPublish]);
	},

	_deleteStyle: function(styleName) {
		//This method uses a publish/subscribe
		// Unsubscribe handle... always needed otherwise it remains in memory even if this object no longer exists
		dojo.unsubscribe(_this.subscribedHandle);
		// Need to transfer an object, an array doesn't work
		dojo.publish("juice.deleteStyle",[{CSSclass: _this._myclass, style: styleName}]);
	},

	_viewCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.show();
	},

	_closeCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.hide();
	}
});