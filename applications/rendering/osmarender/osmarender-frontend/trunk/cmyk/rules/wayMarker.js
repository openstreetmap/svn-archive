dojo.provide("cmyk.rules.wayMarker");

dojo.require("cmyk.rules.renderingInstruction");

/**
	@lends cmyk.rules.wayMarker
*/

dojo.declare("cmyk.rules.wayMarker",cmyk.rules.renderingInstruction,{
	/** 
	      @constructs
	      @class A "Way Marker" rendering instruction
	      @memberOf cmyk.rules
	      @extends cmyk.rules.renderingInstruction
	*/
	constructor: function(node) {
		this._mytag = "wayMarker";
	},
});
