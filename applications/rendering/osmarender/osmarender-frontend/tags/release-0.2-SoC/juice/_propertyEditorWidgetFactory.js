dojo.provide("juice._propertyEditorWidgetFactory"); 

dojo.require("juice._propertyEditorColorPicker");
dojo.require("juice._propertyEditorNumberSpinner");
dojo.require("juice._propertyEditorNumberSpinnerWithDimensions");
dojo.require("juice._propertyEditorSelect");
dojo.require("juice._propertyEditorText");
dojo.require("juice._propertyEditorViewer");

//Thanks to http://www.zvon.org/xxl/svgReference/Output/attr_text-anchor.html and such for SVG attributes
// http://www.zvon.org/xxl/svgReference/Output/index.html
dojo.declare("juice._propertyEditorWidgetFactory",null,{

	markupFactory: function(property,value,images) {
		//TODO: change to switch
		if (property=="fill" || property=="stroke") {
			if (value.substring(0,3)!="url") {
				return new juice._propertyEditorColorPicker(property,value);
			}
			else {
				return new juice._propertyEditorViewer(property,value,images);
			}
		}
		if (property=="marker-end" || property=="marker-mid" || property=="marker-start") {
			if (value.substring(0,3)!="url") {
				return new juice._propertyEditorColorPicker(property,value);
			}
			else {
				return new juice._propertyEditorViewer(property,value,images);
			}
		}
		if (property=="opacity" || property=="fill-opacity" || property=="stroke-opacity" || property=="stroke-miterlimit") {
			if (value.search("[0-9]")!=-1) {
				return new juice._propertyEditorNumberSpinner(property,value);
			}
		}
		if (property=="stroke-width" || property=="font-size") {
			if (value.search("[0-9]")!=-1) {
				return new juice._propertyEditorNumberSpinnerWithDimensions(property,value);
			}
		}
		if (property=="stroke-linecap" || property=="stroke-linejoin" || property=="font-weight" || property=="text-anchor" || property=="display" || property=="fill-rule") {
			return new juice._propertyEditorSelect(property,value);
		}
		//if this property is unknown, return a read only text
		return new juice._propertyEditorText(property,value);
	}
});