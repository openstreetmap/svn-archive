dojo.provide("juice._propertyEditorViewer");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("juice._propertyEditorWidget");

//TODO: Zoom for markers/patterns
dojo.declare("juice._propertyEditorViewer",[dijit._Widget,dijit._Templated,juice._propertyEditorWidget],{
	templatePath: dojo.moduleUrl("juice","_propertyEditorViewer.html"),
	imagesSelectable: new Array(),
	SVGReadable: new Array(),
	exists: false,

	postCreate: function() {
		var _this = this;

		if (this._property=="marker-start" || this._property=="marker-mid" || this._property=="marker-end") {
			dojo.forEach(this._images.markers,
				function(marker,index,array) {
					//Inside forEach _this variable needs to be referenced
					_this.imagesSelectable.push(marker.id);
					_this.SVGReadable.push(marker);
				}
			);
		}
		if (this._property=="fill" || this._property=="stroke") {
			dojo.forEach(this._images.patterns,
				function(pattern,index,array) {
					_this.imagesSelectable.push(pattern.id);
					_this.SVGReadable.push(pattern);
				}
			);
		}
		dojo.forEach(this.imagesSelectable,
			function(name,index,array) {
				var selectedValue="";
				if (name==_this._value.split("(")[1].split("#")[1].split(")")[0]) {
					selectedValue=' selected="selected"';
					_this.exists=true;
				}
				else {
					selectedValue="";
				}
				_this.editorSelect.innerHTML+='<option value="'+name+'"'+selectedValue+'>'+name+'</option>';
			}
		);
		// If the marker doesn't exist inside the rule file, add it anyway
		if (!this.exists) {
			this.editorSelect.innerHTML+='<option value="'+this._value.split("(")[1].split("#")[1].split(")")[0]+'" selected="selected">'+this._value.split("(")[1].split("#")[1].split(")")[0]+'</option>';
		}

		this._onChange();
	},

	getCSSWidgetValue: function() {
		this._onChange();
		return this._value;
	},

	_onChange: function() {
		this._value='url(#'+this.editorSelect.value+')';
		var myobject = this.SVGReadable[this.imagesSelectable.indexOf(this.editorSelect.value)];
		if (myobject!=null) {
			//TODO: Can't do this, because namespace isn't appended
			//this.SVGViewer.appendChild(this.SVGReadable[this.indexReadable]);
			while (this.SVGViewer.hasChildNodes()) {
				this.SVGViewer.removeChild(this.SVGViewer.firstChild);
			}
			var svg_container = document.createElementNS("http://www.w3.org/2000/svg","svg");
			dojo.forEach(myobject.attributes,
//					console.debug("attribute" +array[index].getAttribute(name));
				function(name,index,array) {
					svg_container.setAttribute(array[index].name,array[index].value);
				}
			);
			dojo.forEach(myobject.childNodes,
				function(name,index,array) {
					if (array[index].nodeType!=Node.TEXT_NODE) {
						svg_container.appendChild(array[index].cloneNode(true));
					}
				}
			);
			this.SVGViewer.appendChild(svg_container);
		}
		else {
			this.SVGViewer.innerHTML="<p>Not found!</p>";
		}
	}
});
