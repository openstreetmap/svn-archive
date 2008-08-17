dojo.provide("osmarender_frontend.widgets.css_editor._propertyEditorWidgetFactory"); 

dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorColorPicker");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinner");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinnerWithDimensions");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorSelect");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorText");
dojo.require("osmarender_frontend.widgets.css_editor._propertyEditorViewer");

//Thanks to http://www.zvon.org/xxl/svgReference/Output/attr_text-anchor.html and such for SVG attributes
// http://www.zvon.org/xxl/svgReference/Output/index.html
dojo.declare("osmarender_frontend.widgets.css_editor._propertyEditorWidgetFactory",null,{

	markupFactory: function(property,value,images) {
		//TODO: change to switch
		if (property=="fill" || property=="stroke") {
			if (value.substring(0,3)!="url") {
				return new osmarender_frontend.widgets.css_editor._propertyEditorColorPicker(property,value);
			}
			else {
				return new osmarender_frontend.widgets.css_editor._propertyEditorViewer(property,value,images);
			}
		}
		if (property=="marker-end" || property=="marker-mid" || property=="marker-start") {
			if (value.substring(0,3)!="url") {
				return new osmarender_frontend.widgets.css_editor._propertyEditorColorPicker(property,value);
			}
			else {
				return new osmarender_frontend.widgets.css_editor._propertyEditorViewer(property,value,images);
			}
		}
		if (property=="opacity" || property=="fill-opacity" || property=="stroke-opacity" || property=="stroke-miterlimit") {
			if (value.search("[0-9]")!=-1) {
				return new osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinner(property,value);
			}
		}
		if (property=="stroke-width" || property=="font-size") {
			if (value.search("[0-9]")!=-1) {
				return new osmarender_frontend.widgets.css_editor._propertyEditorNumberSpinnerWithDimensions(property,value);
			}
		}
		if (property=="stroke-linecap" || property=="stroke-linejoin" || property=="font-weight" || property=="text-anchor" || property=="display" || property=="fill-rule") {
			return new osmarender_frontend.widgets.css_editor._propertyEditorSelect(property,value);
		}
		//if this property is unknown, return a read only text
		return new osmarender_frontend.widgets.css_editor._propertyEditorText(property,value);
	}
});