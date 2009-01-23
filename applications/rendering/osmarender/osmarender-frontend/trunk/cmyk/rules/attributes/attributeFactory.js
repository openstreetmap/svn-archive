dojo.provide("cmyk.rules.attributes.attributeFactory");

dojo.require("cmyk.rules.attributes.CSSClass");
dojo.require("cmyk.rules.attributes.CSSMaskClass");
dojo.require("cmyk.rules.attributes.suppressMarkersTag");
dojo.require("cmyk.rules.attributes.widthScaleFactor");
dojo.require("cmyk.rules.attributes.minimumWidth");
dojo.require("cmyk.rules.attributes.maximumWidth");
dojo.require("cmyk.rules.attributes.honorWidth");
dojo.require("cmyk.rules.attributes.smartLineCap");
dojo.require("cmyk.rules.attributes.Layer");
dojo.require("cmyk.rules.attributes.Ref");
dojo.require("cmyk.rules.attributes.Position");
dojo.require("cmyk.rules.attributes.Transform");
dojo.require("cmyk.rules.attributes.Radius");
dojo.require("cmyk.rules.attributes.Stroke");
dojo.require("cmyk.rules.attributes.strokeWidth");
dojo.require("cmyk.rules.attributes.Fill");
dojo.require("cmyk.rules.attributes.Key");
dojo.require("cmyk.rules.attributes.startOffset");
dojo.require("cmyk.rules.attributes.fontSize");
dojo.require("cmyk.rules.attributes.textAnchor");
dojo.require("cmyk.rules.attributes.avoidDuplicates");
dojo.require("cmyk.rules.attributes.Dx");
dojo.require("cmyk.rules.attributes.Dy");
dojo.require("cmyk.rules.attributes.textAttenuation");
dojo.require("cmyk.rules.attributes.strokeOpacity");
dojo.require("cmyk.rules.attributes.markerMid");

/**
	@lends cmyk.rules.attributes.attributeFactory
*/

dojo.declare("cmyk.rules.attributes.attributeFactory",null,{
	/** 
	      @constructs
	      @class Factory of all valid attributes
	      @memberOf cmyk.rules.attributes
	*/
	constructor: function() {

		this.factory = function(attribute_name,attribute_value,calling_class) {
			//TODO: support checking of valid attribute for this calling class
			switch (attribute_name) {
				case "class":
					return new cmyk.rules.attributes.CSSClass(attribute_value);
				break;
				case "mask-class":
					return new cmyk.rules.attributes.CSSMaskClass(attribute_value);
				break;
				case "suppress-markers-tag":
					return new cmyk.rules.attributes.suppressMarkersTag(attribute_value);
				break;
				case "width-scale-factor":
					return new cmyk.rules.attributes.widthScaleFactor(attribute_value);
				break;
				case "minimum-width":
					return new cmyk.rules.attributes.minimumWidth(attribute_value);
				break;
				case "maximum-width":
					return new cmyk.rules.attributes.maximumWidth(attribute_value);
				break;
				case "honor-width":
					return new cmyk.rules.attributes.honorWidth(attribute_value);
				break;
				case "smart-linecap":
					return new cmyk.rules.attributes.smartLineCap(attribute_value);
				break;
				case "layer":
					return new cmyk.rules.attributes.Layer(attribute_value);
				break;
				case "ref":
					return new cmyk.rules.attributes.Ref(attribute_value);
				break;
				case "position":
					return new cmyk.rules.attributes.Position(attribute_value);
				break;
				case "transform":
					return new cmyk.rules.attributes.Transform(attribute_value);
				break;
				case "r":
					return new cmyk.rules.attributes.Radius(attribute_value);
				break;
				case "stroke":
					return new cmyk.rules.attributes.Stroke(attribute_value);
				break;
				case "stroke-width":
					return new cmyk.rules.attributes.strokeWidth(attribute_value);
				break;
				case "fill":
					return new cmyk.rules.attributes.Fill(attribute_value);
				break;
				case "k":
					return new cmyk.rules.attributes.Key(attribute_value);
				break;
				case "startOffset":
					return new cmyk.rules.attributes.startOffset(attribute_value);
				break;
				case "font-size":
					return new cmyk.rules.attributes.fontSize(attribute_value);
				break;
				case "text-anchor":
					return new cmyk.rules.attributes.textAnchor(attribute_value);
				break;
				case "avoid-duplicates":
					return new cmyk.rules.attributes.avoidDuplicates(attribute_value);
				break;
				case "dx":
					return new cmyk.rules.attributes.Dx(attribute_value);
				break;
				case "dy":
					return new cmyk.rules.attributes.Dy(attribute_value);
				break;
				case "textAttenuation":
					return new cmyk.rules.attributes.textAttenuation(attribute_value);
				break;
				case "stroke-opacity":
					return new cmyk.rules.attributes.strokeOpacity(attribute_value);
				break;
				case "marker-mid":
					return new cmyk.rules.attributes.markerMid(attribute_value);
				break;
				default:
					throw new Error('unknown attribute: '+attribute_name+' with value '+attribute_value+' for class '+calling_class);			
			}
		}
	}

});


 
