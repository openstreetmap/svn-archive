dojo.provide("cmyk.structures.base.Node");

dojo.require("cmyk.structures.base.elementType");

/**
	@lends cmyk.structures.base.Node
*/

dojo.declare("cmyk.structures.base.Node",cmyk.structures.base.elementType,{
	/** 
	      @constructs
	      @class A class that represent a Node
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
		uuid: 0,
		/**
			@constant
			string identification
  		*/
		string: "NODE"
	}
});
