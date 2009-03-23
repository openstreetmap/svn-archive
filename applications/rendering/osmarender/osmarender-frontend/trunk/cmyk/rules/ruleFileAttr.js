dojo.provide("cmyk.rules.ruleFileAttr");

/**
	@lends cmyk.rules.ruleFileAttr
*/

dojo.declare("cmyk.rules.ruleFileAttr",null,{
	/** 
		@constructs
		@class A class that represent main attributes of a rule file
		@memberOf cmyk.rules
		@example
		<code>
ruleFileAttributes = new cmyk.rules.ruleFileAttr(
	new Object({
		scale:2,
		data:"my_file.xml",
		showScale:true
	})
);
		</code>
		@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
		@param {Object} attributes An Object containing Osmarender general attributes as name:value
		@param {String} [attributes.data="data.osm"] The name of the file
		@param {String} [attributes.svgBaseProfile="full"] The svg Base Profile
		@param {Number} [attributes.scale=1.0] The Scale
		@param {Number} [attributes.symbolScale=1.0] The symbolScale
		@param {Number} [attributes.textAttenuation=14.0] The textAttenuation
		@param {Number} [attributes.minimumMapWidth=1] The minimum Map Width
		@param {Number} [attributes.minimumMapHeight=1] The minimum Map Height
		@param {boolean} [attributes.withOSMLayers] The with OSM Layers
		@param {boolean} [attributes.withUntaggedSegments] The with Untagged Segments
		@param {boolean} [attributes.showScale] The show Scale
		@param {boolean} [attributes.showGrid] The show Grid
		@param {boolean} [attributes.showBorder] The show Border
		@param {boolean} [attributes.showLicense] The show License
		@param {boolean} [attributes.interactive] The interactive
		@param {boolean} [attributes.showRelationRoute] The show Relation Route
		@param {String} [attributes.symbolsDir] The symbols Dir
		@param {Number} [attributes.meter2pixel=0.1375] The minimum Map Height

		@throws Error If attribute is unknown or is of wrong type
	*/
	constructor: function(attributes) {

		var _attributes;

		_setAttributes(attributes);

		function _setAttributes (attributes) {
			_checkMyTypes(attributes);
			_attributes = dojo.clone(attributes);
		}


		function _checkMyTypes(attributes) {
			// Define correct variable types
			if (attributes==undefined || typeof(attributes)!='object') throw new Error('attributes parameter is undefined');

			var checkAttributes = new Array();
			checkAttributes['string'] = ['data','svgBaseProfile','symbolsDir'];
			checkAttributes['number'] = ['scale','symbolScale','textAttenuation','minimumMapHeight','minimumMapWidth','meter2pixel'];
			checkAttributes['boolean'] = ['withOSMLayers','withUntaggedSegments','showScale','showGrid','showBorder','showLicense','interactive','showRelationRoute'];

			// Check if passed proper data
//TODO: change to dojo.forEach and dojo.indexOf
			for (var attribute in attributes) {
				var attribute_found=false;
				for (var myType in checkAttributes) {
					if (checkAttributes[myType].indexOf(attribute)!=-1) {
						attribute_found=true;
					}
				}
				if (!attribute_found) throw new Error('unknown variable name: '+attribute+' with content: '+attributes[attribute]);

			}

			var defaultValues = {
				data: "data.osm",
				svgBaseProfile: "full",
				scale: 1.0,
				symbolScale: 1.0,
				textAttenuation: 14.0,
				minimumMapWidth: 1,
				minimumMapHeight: 1,
				withOSMLayers: true,
				withUntaggedSegments: false,
				showScale: false,
				showGrid: false,
				showBorder: false,
				showLicense: false,
				interactive: false,
				showRelationRoute:false,
				symbolsDir: "../stylesheets/symbols",
				meter2pixel: 0.1375
			};

			// Check all types
			for (var objectType in checkAttributes) {
				for (var attribute in checkAttributes[objectType]) {
					if (attributes[checkAttributes[objectType][attribute]]==undefined) {
						// Attribute has not been passed, searching for existing attribute
						if (_attributes!=undefined && _attributes[checkAttributes[objectType][attribute]]!=undefined) {
							attributes[checkAttributes[objectType][attribute]]=_attributes[checkAttributes[objectType][attribute]]
						}
						// This is a new instance, defaulting
						else {
							attributes[checkAttributes[objectType][attribute]]=defaultValues[checkAttributes[objectType][attribute]];
						}
					}
					else {
						//Needed to assign string variables properly
						var separator='';
						var current_attribute_value = eval('attributes.'+checkAttributes[objectType][attribute]);
						var current_attribute_name = checkAttributes[objectType][attribute];
						if (dojo.indexOf(checkAttributes['string'],current_attribute_name)!=-1) {
							//TODO: should test for a regular expression instead
							current_attribute_value = new String(current_attribute_value);
							separator='"';
						}
						else if (dojo.indexOf(checkAttributes['number'],current_attribute_name)!=-1) {
							current_attribute_value = new Number(current_attribute_value);
							if (isNaN(current_attribute_value)) {
								throw new Error('attribute '+current_attribute_name+' must be a Number. Value '+current_attribute_value+' encountered instead');
							}
						}
						else if (dojo.indexOf(checkAttributes['boolean'],current_attribute_name)!=-1) {
							if (current_attribute_value=="yes") current_attribute_value==true;
							else if (current_attribute_value=="no") current_attribute_value==false;
							else throw new Error('attribute '+current_attribute_name+' must be "yes" or "no". Value '+current_attribute_value+' encountered instead');
							current_attribute_value = new Boolean(current_attribute_value);
						}
						else {
							//value not existent and not manageable. Should never see this error
							throw new Error('Value '+current_attribute_name+' with value '+current_attribute_value+' not manageable by this CMYK version');
						}
						eval('attributes.'+current_attribute_name+'='+separator+current_attribute_value+separator);
					}
				}
			}
		}

		/** Get the attributes
			@returns {Object} an Attributes object, see constructor for details
		*/
		this.getAttributes = function() {
			return dojo.clone(_attributes);
		}

		/** Set attributes
			@param {Object} attributes An Object containing Osmarender general attributes as name:value, see constructor for details
		@example
		<code>
newAttributes = new Object({scale:10,data:"dumb_file2.osm"});
ruleFileAttributes.setAttributes(newAttributes);
		</code>
		*/
		this.setAttributes = function(attributes) {
			_setAttributes(attributes);
		}

		this.write = function(xmlNode) {
			for (var attribute_name in _attributes) {
				var attribute_to_write = _attributes[attribute_name];
				if (attribute_to_write==new Boolean(true)) attribute_to_write="yes";
				if (attribute_to_write==new Boolean(false)) attribute_to_write="no";
				xmlNode.setAttribute(attribute_name,attribute_to_write);
			}
		}
	}
});
 
