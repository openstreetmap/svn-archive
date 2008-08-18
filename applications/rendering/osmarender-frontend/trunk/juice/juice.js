/*
	@namespace juice
*/

dojo.provide("juice.juice");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.Dialog");

dojo.require("juice._propertyEditorWidgetFactory");

dojo.require("dojo.i18n");
dojo.requireLocalization("juice", "main");

/**
@lends juice.juice
*/
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
	
	/** 
	@constructs
	@class A dojox widget driven CSS Editor for single properties
	@requires juice._propertyEditorWidgetFactory
	@memberOf juice
	@example
	<code>
	var css_editor = new juice.juice({
		id:"css_editor",
		_myclass:CSSclassname,
		CSSproperties: properties,
		images:objects
	});
	</code>
	where:<br />
	<strong>CSSclassname</strong> is a string that contains the name of the CSS class to edit<br />
	<strong>properties</strong> is an associative array that contains strings as "property name"=>"property value"<br />
	<strong>objects</strong> is an array of SVGDocumentElements
	@description
	Once created, this object will <strong>publish</strong> the following topics:
	<ol>
	<li><strong>juice.deleteStyle</strong>: when deleting a property<br />
	To catch the topic and the args given, to do something useful like updating your own CSS model:<br />
	<code>
	dojo.subscribe("juice.deleteStyle",null,deleteStyleFunction);
	function deleteStyleFunction(args) {
		alert("name of the class to delete: "+args.CSSclass);
		alert("name of the property to delete: "+args.style);
	}
	</code></li>
	<li><strong>juice.setStyle</strong>: when setting a property<br />
	<code>
	dojo.subscribe("juice.setStyle",null,setStyleFunction);
	function setStyleFunction(args) {
		dojo.forEach(args,
			function(element,index,array) {
				alert("name of the class to modify: "+element.CSSclass);
				alert("name of the property to modify: "+element.property);
				alert("new value for the property: "+element.editValue);
			}
		);
	}
	</code></li>
	<li><strong>juice.addStyle</strong>: when adding a new property<br />
	<code>
	dojo.subscribe("juice.addStyle",null,addStyleFunction);

	function addStyleFunction(args) {
		alert("name of the class in which the property should be added: "+args.CSSClass);
		alert("name of the property that should be added: "+args.CSSname);
		alert("value of the property: "+args.CSSvalue);
	}
	</code></li>
	</ol>

	@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
	*/
	constructor: function() {
		this._mainmessages = dojo.i18n.getLocalization("juice", "main");
		//Necessary otherwise subscribed functions can't reference to this object
		_this=this;
	},

	/**
		@private
	*/
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

	/**
		@private
	*/
	_addCSSProperty: function(args) {
		this.dialogAddCSSProperty.hide();
		var objectToPublish = {
			CSSclass: this._myclass,
			CSSname: this.addCSSPropertyName.value,
			CSSvalue: this.addCSSPropertyValue.value
		}
		dojo.publish("juice.addStyle",[objectToPublish]);
	},

	/**
		@private
	*/
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

	/**
		@private
	*/
	_deleteStyle: function(styleName) {
		//This method uses a publish/subscribe
		// Unsubscribe handle... always needed otherwise it remains in memory even if this object no longer exists
		dojo.unsubscribe(_this.subscribedHandle);
		// Need to transfer an object, an array doesn't work
		dojo.publish("juice.deleteStyle",[{CSSclass: _this._myclass, style: styleName}]);
	},

	/**
		@private
	*/
	_viewCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.show();
	},

	/**
		@private
	*/
	_closeCSSPropertyDialog: function() {
		this.dialogAddCSSProperty.hide();
	}
});