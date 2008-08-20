dojo.provide("cmyk.structures.features.Feature");

dojo.require("cmyk.structures.base.elementTypeCollection");
dojo.require("cmyk.structures.features.Category");

/**
@lends cmyk.structures.features.Feature
*/
dojo.declare("cmyk.structures.features.Feature",null,{
	/** 
	@constructs
	@class A class that represent a Feature for features classification
	@requires cmyk.structures.base.elementTypeCollection, cmyk.structures.features.Category
	@memberOf cmyk.structures.features
	@example
	<code>
	var myTypes = new cmyk.structures.base.elementTypeCollection([new cmyk.structures.base.Way()]);
	var myFacet = new cmyk.structures.features.Facet("wiki");
	var myCategory = new cmyk.structures.features.Category("wiki_category",myFacet);
	var myFeature = new cmyk.structures.features.Feature("highway","motorway",myTypes,myCategory);
	</code>

	<strong>more compact:</strong>
	<code>
	with (cmyk.structures) {
		var myTypes = new base.elementTypeCollection([new base.Way()]);
		var myFacet = new features.Facet("wiki");
		var myCategory = new features.Category("wiki_category",myFacet);
		var myFeature = new features.Feature("highway","motorway",myTypes,myCategory);
	}
	</code>
	@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
	      @param {String} key A string containing the key of the feature
	      @param {String} value A string containing the value of the feature
	      @param {cmyk.structures.base.elementTypeCollection} type An elementTypeCollection that contains the valid types associated to this key/value pair
	      @param {cmyk.structures.features.Category} [category] The category that contains this feature, optional
	      @throws Error if arguments are of incorrect type
	*/
	constructor: function(key,value,type,category) {
		if (typeof(key)!="string") throw new Error ("key must be a string");
		if (typeof(value)!="string") throw new Error ("value must be a string");
		if (!(type instanceof cmyk.structures.base.elementTypeCollection)) throw new Error ("type must be an instance of cmyk.structures.base.elementTypeCollection");
		if (!((category==undefined) || (category instanceof cmyk.structures.features.Category))) throw new Error ("category must be an instance of cmyk.structures.features.Category");

		var _key=key;
		var _value=value;
		var _type=type;
		var _category=category;
		
		/** Get an array of elementTypes that are valid for this feature
		@returns {cmyk.structures.base.elementTypes[]} array of elementTypes
		*/
		this.getTypeElement = function() {
			return dojo.clone(_type.getTypes());
		};
		
		/** Get the elementTypeCollection object
		@returns {cmyk.structures.base.elementTypeCollection} the object that contains the collection of valid elementTypes
		*/
		this.getTypeObject = function() {
			return dojo.clone(_type);
		};
		
		/** Get the key of this feature
		@returns {String}
		*/
		this.getKey = function() {
			return dojo.clone(_key);
		};
		
		/** Get the value of this feature
		@returns {String}
		*/
		this.getValue = function() {
			return dojo.clone(this._value);
		};
		
		/** Get the name of the category that contains this feature
		@returns {String} if category present, else return null
		*/
		this.getCategoryName = function() {
			if (_category!=undefined) return dojo.clone(_category.getName()); else return null;
		};
		
		/** Get the category object that contains this feature
		@returns {cmyk.structures.features.Category} if category present, else return null
		*/
		this.getCategoryObject = function() {
			if (_category!=undefined) return dojo.clone(_category); else return null;
		};
	}
});
