dojo.provide("cmyk.structures.features.Category");

//dojo.require("cmyk.structures.features.Facet");

/**
@lends cmyk.structures.features.Category
*/
dojo.declare("cmyk.structures.features.Category",null,{
	/** 
	@constructs
	@class A class that represent a Category for features classification
	@requires cmyk.structures.features.Facet
	@memberOf cmyk.structures.features
	@example
	<code>
	var myFacet = new cmyk.structures.features.Facet("wiki");
	var myCategory = new cmyk.structures.features.Category("wiki_category",myFacet);
	alert(myCategory.getName());
	</code>

	<strong>more compact:</strong>
	<code>
	with (cmyk.structures.features) {
		var myCategory = new Category("wiki_category",new Facet("wiki"));
		alert(myCategory.getName());
	}
	</code>
	@author <a href="mailto:fadinlight@gmail.com">Mario Ferraro</a>
	      @param {String} name A string containing the name of the category
	      @param {cmyk.structures.features.Facet} facet A facet that will contain this category
	      @param {cmyk.structures.features.Category} [supercategory] The supercategory that contain this category
	      @throws Error if arguments are of incorrect type
	*/
	constructor: function(name,facet,supercategory) {
		if (typeof(name)!="string") throw new Error ("category name must be a string");
		if (!(facet instanceof cmyk.structures.features.Facet)) throw new Error ("facet must be an instance of cmyk.structures.features.Facet");
		if (!((supercategory==undefined) || (supercategory instanceof cmyk.structures.features.Category))) throw new Error ("supercategory must be an instance of cmyk.structures.features.Category");

		var _name=name;
		var _facet=facet;
		var _supercategory;
		var _features=new Array();
		var _subcategories=new Array();

		if (supercategory!=undefined) {
			_supercategory=supercategory;
			supercategory._addSubCategory(this);
		}
		facet._addCategory(this);
		
		/** Get the name of the category
			@returns {String} the name of the category
		*/
		this.getName = function() {
			return dojo.clone(_name);
		};
		
		/** Get a reference of the facet object that contains this category
			@returns {cmyk.structures.features.Facet} reference to the facet
		*/
		this.getFacet = function() {
			return _facet;
		}
		
		/** Get a reference to the supercategory object, if exists, else return undefined
		@returns {cmyk.structures.features.Category} the supercategory
		*/
		this.getSuperCategory = function() {
			if (!!_supercategory) return _supercategory; else return undefined;
		};

		/** Add a subcategory to this Category object
		@private
		*/
		this._addSubCategory = function(category) {
			if (!(category instanceof cmyk.structures.features.Category)) throw new Error ("category must be an instance of cmyk.structures.features.Category");
			_subcategories[_subcategories.length]=category;
		};
		
		/** Add a feature to this Category object
		*/
		this.addFeature = function(feature) {
			if (!(category instanceof cmyk.structures.features.Feature)) throw new Error ("feature must be an instance of cmyk.structures.features.Feature");
			_features[_features.length]=feature;
		};

	}
});
