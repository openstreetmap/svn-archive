dojo.provide("cmyk.rules._wayText");

dojo.require("cmyk.rules._wayAreaText");

/**
	@lends cmyk.rules._wayText
*/

dojo.declare("cmyk.rules._wayText",cmyk.rules._wayAreaText,{
	/** 
	      @constructs
	      @class This is the superclass of every text rendering instruction for ways
	      @memberOf cmyk.rules
	      @extends cmyk.rules._wayAreaText
	      @private
	*/
	constructor: function() {
	},
});
