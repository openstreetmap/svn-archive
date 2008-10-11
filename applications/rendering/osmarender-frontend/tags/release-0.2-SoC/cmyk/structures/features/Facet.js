dojo.provide("cmyk.structures.features.Facet");

dojo.require("cmyk.structures.features.Category");

/**
@lends cmyk.structures.features.Facet
*/
dojo.declare("cmyk.structures.features.Facet",null,{
	/** 
	@constructs
	@class A class that represent a Facet for features classification
	@requires cmyk.structures.features.Category
	@memberOf cmyk.structures.features
	@example
	<code>
	var myFacet = new cmyk.structures.features.Facet("wiki");
	</code>

	<strong>more compact:</strong>
	<code>
	with (cmyk.structures.features) {
		var myFacet = new Facet("wiki");
	}
	</code>
	@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
	      @param {String} name A string containing the name of the facet
	      @throws Error if argument is not a string
	*/
	constructor: function(name) {
		if (typeof(name)!="string") throw new Error ("facet name must be a string");

		var _name=name;
		var _categories=new Array();
		
		/** Get the name of the facet
		@returns String the name of the facet
		*/
		this.getName = function() {
			return dojo.clone(_name);
		};
		
		/** Add a category to the facet
		@private
		*/
		this._addCategory = function(category) {
			if (!(category instanceof cmyk.structures.features.Category)) throw new Error ("category must be an instance of cmyk.structures.features.Category");
			_categories[_categories.length]=category;
		};
	}
});
