dojo.provide("cmyk.structures.base.Way");

dojo.require("cmyk.structures.base.elementType");

/**
	@lends cmyk.structures.base.Way
*/

dojo.declare("cmyk.structures.base.Way",cmyk.structures.base.elementType,{
	/** 
	      @constructs
	      @class A class that represent a Way
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
		uuid: 1,
		/**
			@constant
			string identification
  		*/
		string: "WAY"
	}
});
