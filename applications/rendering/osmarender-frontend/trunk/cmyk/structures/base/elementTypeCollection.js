dojo.provide("cmyk.structures.base.elementTypeCollection");

dojo.require("cmyk.structures.base.Node");
dojo.require("cmyk.structures.base.Way");
dojo.require("cmyk.structures.base.Area");

/**
@lends cmyk.structures.base.elementTypeCollection
*/
dojo.declare("cmyk.structures.base.elementTypeCollection",null,{
	/** 
	@constructs
	@class A class that represent a collection of Element Types
	@requires cmyk.structures.base.Node, cmyk.structures.base.Way, cmyk.structures.base.Area
	@memberOf cmyk.structures.base
	@example
	<code>var myelementTypeCollection = new cmyk.structures.base.elementTypeCollection([new cmyk.structures.base.Node(), new cmyk.structures.base.Way()]);</code>

	<strong>more compact:</strong>
	<code>
	with (cmyk.structures.base) {
		var myelementTypeCollection = new elementTypeCollection([new Node(),new Way()]);
	}
	</code>
	@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
	      @param {cmyk.structures.base.elementType[]} types An Array of element Types
	      @throws Error if argument is not an array of elementType objects
	*/
	constructor: function(types) {
		for (var i in types) {
			if (!(types[i] instanceof cmyk.structures.base.elementType)) throw new Error("Argument must be an array of elementType");
		}
		/** Stores the types that this collection represents
			@private
		*/
		var _myTypes = types;

		/** Get the types that this collection represents
			@returns {cmyk.structures.base.elementType[]} the element Types stored in the object
		*/
		this.getTypes = function() {
			return dojo.clone(_myTypes);
		};
	}
});
