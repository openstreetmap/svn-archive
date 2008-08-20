dojo.provide("cmyk.data.Features");

dojo.require("cmyk.structures.base.elementTypeCollection");
dojo.require("cmyk.structures.features.Facet");
dojo.require("cmyk.structures.features.Category");
dojo.require("cmyk.structures.features.Feature");

/**
@lends cmyk.data.Features
*/
dojo.declare("cmyk.data.Features",null,{
	/** 
	@constructs
	@class A class that represent key/value pairs data
	@requires cmyk.structures.base.elementTypeCollection
	@memberOf cmyk.data
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
	constructor: function() {
		// this contains heredoc javascript syntax, thanks to http://www.scribd.com/doc/1026312/Javascript-Shorthand-QuickReference
		var wiki_facet = new cmyk.structures.features.Facet("Wiki");
		var wiki_facet_physical = new cmyk.structures.features.Category("Physical",wiki_facet);
		var wiki_facet_physical_highway = new cmyk.structures.features.Category("Highway Tag",wiki_facet,wiki_facet_physical);
		var wiki_facet_physical_cycleway = new cmyk.structures.features.Category("Cycleway Tag",wiki_facet,wiki_facet_physical);
		var wiki_facet_physical_tracktype = new cmyk.structures.features.Category("Tracktype Tag",wiki_facet,wiki_facet_physical);
		
		var NODE = new cmyk.structures.base.Node();
		var WAY = new cmyk.structures.base.Way();
		var AREA = new cmyk.structures.base.Area();

		//TODO:Add sub and super category management: Physical=>highway, Non Physical=>route
		//TODO:Add relations
		
		var features_classification = {
			"highway" : {
				description: "",
				tags : {
					"motorway": {
						types: [WAY],
						categories: [wiki_facet_physical_highway],
						description: (<r><![CDATA[A restricted access major divided highway, normally with 2 or more running lanes plus emergency hard shoulder. Equivalent to the Freeway, Autobahn etc..]]></r>).toString(),
						wiki_page: "http://wiki.openstreetmap.org/index.php/Tag:highway%3Dmotorway"
					},
					"motorway_link" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"trunk" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"trunk_link" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"primary" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"primary_link" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"secondary" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"tertiary" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"unclassified" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"track" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"residential" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"living_street" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"service" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"bridleway" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"cycleway" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"footway" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"pedestrian" : {
						types: [WAY,AREA],
						categories: [wiki_facet_physical_highway]
					},
					"steps" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"bus_guideway" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
					"mini_roundabout" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"stop" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"traffic_signals" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"crossing" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"gate" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"stile" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"cattle_grid" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"toll_booth" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"incline" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"viaduct" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"motorway_junction" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"services" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"ford" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"bus_stop" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"turning_circle" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"stop" : {
						types: [NODE],
						categories: [wiki_facet_physical_highway]
					},
					"construction" : {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					}
				}
			},
			"junction" : {
				description: "",
				tags : {
					"roundabout": {
						types: [WAY],
						categories: [wiki_facet_physical_highway]
					},
				}
			},
			"cycleway" : {
				description: "",
				tags : {
					"lane": {
						types: [WAY],
						categories: [wiki_facet_physical_cycleway]
					},
					"track": {
						types: [WAY],
						categories: [wiki_facet_physical_cycleway]
					},
					"opposite_lane": {
						types: [WAY],
						categories: [wiki_facet_physical_cycleway]
					},
					"opposite_track": {
						types: [WAY],
						categories: [wiki_facet_physical_cycleway]
					},
					"opposite": {
						types: [WAY],
						categories: [wiki_facet_physical_cycleway]
					}
				}
			},
			"tracktype" : {
				description: "",
				tags : {
					"grade1": {
						types: [WAY],
						categories: [wiki_facet_physical_tracktype]
					},
					"grade2": {
						types: [WAY],
						categories: [wiki_facet_physical_tracktype]
					},
					"grade3": {
						types: [WAY],
						categories: [wiki_facet_physical_tracktype]
					},
					"grade4": {
						types: [WAY],
						categories: [wiki_facet_physical_tracktype]
					},
					"grade5": {
						types: [WAY],
						categories: [wiki_facet_physical_tracktype]
					}
				}
			}
		}
	}
	//Construct a variable that would contains all the tree for facets
/*var osmafeats = new Array();
for (var key in features_classification) {
	var tags = features_classification[key].tags;
	for (var value in tags) {
		osmafeats[osmafeats.length]=new osmarenderFeature(key,value,elementTypes(tags[value].types),tags[value].categories[0]);
	}
}*/



//This could be a future test case
/*var stringtoprint="";
for (var object in osmafeats) {
	supercategoryparsed = osmafeats[object].getCategoryObject().getSuperCategory().getName();
	if (supercategoryparsed) {
		stringtoprint+="macrocategory: "+supercategoryparsed+",";
	}
	else {
		stringtoprint+="macrocategory: no one,";
	}
	stringtoprint+=" category: "+osmafeats[object].getCategoryName()+" key: "+osmafeats[object].getKey()+" value: "+osmafeats[object].getValue()+"\r\n";
}*/
//alert(stringtoprint);

});
