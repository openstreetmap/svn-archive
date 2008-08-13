dojo.provide("cmyk.structures.base.Area");

dojo.require("cmyk.structures.base.elementType");

/**
	@lends cmyk.structures.base.Area
*/

dojo.declare("cmyk.structures.base.Area",cmyk.structures.base.elementType,{
	/** 
		@constructs
		@class A class that represent an Area
		@memberOf cmyk.structures.base
		@extends cmyk.structures.base.elementType
	*/
	constructor: function() {
	},
	/**
		@static
	*/
	statics: {
		/**
			@constant
			numeric identification
  		*/
		uuid: 2,
		/**
			@constant
			string identification
  		*/
		string: "AREA"
	}
});
