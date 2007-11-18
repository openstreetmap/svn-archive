
function RouteTypes()
{
	var rt = { footpath :
	            { conditions :
					{ highway : 'footway', foot : 'yes' } ,
				  colour : 'green',
			      featureclass: 'line',
				  width : 4
				},
			   bridleway :
			   	{ conditions :
					{ highway : 'bridleway', foot : 'yes',
				  	  horse: 'yes', bicycle : 'yes' } ,
				   colour : 'brown',
			       featureclass: 'line',
				   width : 4
				},
			   byway :
			   	{ conditions :
				 	{highway : 'byway', foot : 'yes',
				  	 horse: 'yes', bicycle:'yes',motorcar:'yes' } ,
				  colour : 'red',
			       featureclass: 'line',
				  width : 4
				},
			   residential :
			    { conditions :   { highway : 'residential' },
				  colour : 'gray',
			       featureclass: 'line',
				  width : 4
				},
			   'minor road'  :
			    { conditions: { highway : 'unclassified' },
				  colour : 'gray',
			       featureclass: 'line',
				  width : 5
				},
			   'B road' : 
			    { conditions: { highway : 'secondary' },
				  featureclass: 'line',
				  colour : '#fdbf6f',
				  width : 6
				},
			   'A road' :
			    { conditions : { highway : 'primary' },
				  colour : '#fb8059',
			       featureclass: 'line',
				  width : 6
				},
			   'trunk road' :
			    { conditions : { highway : 'trunk' },
				  colour : '#477147',
			       featureclass: 'line',
				  width : 6
				},
			   motorway :
			    { conditions: { highway : 'motorway' },
				  colour : '#809bc0',
			       featureclass: 'line',
				  width : 6
				},
			   railway : 
			    { conditions :  {railway : 'rail' },
				  colour : 'black',
			       featureclass: 'line',
				  width : 4
				},
			   'permissive footpath' :
			    { conditions : { highway : 'footway' },
				  colour : '#808080',
			       featureclass: 'line',
				  width : 4
				},
			   'permissive bridleway' :
			    { conditions : { highway : 'bridleway' },
				  colour : '#804000',
			       featureclass: 'line',
				  width : 4
				},
			   'cycle path' :
			    { conditions : { highway : 'cycleway' },
				  colour : 'magenta',
			       featureclass: 'line',
				  width: 4
				},
			   'unsurfaced road' :
			    { conditions :  
					{highway : 'unsurfaced'},
				  colour : 'gray',
			       featureclass: 'line',
				  width : 4
				},
				river:
				{
					conditions:
					{waterway: 'river' },
					colour: 'blue',
			       featureclass: 'line',
					width: 4
				},
				wood:
				{
					conditions: { natural: 'wood'},
					colour : '#c0ffc0',
					featureclass: 'polygon',
					width: 3
				},
				heath:
				{
					conditions: { natural: 'heath'},
					colour : '#ffe0c0',
					featureclass: 'polygon',
					width: 3
				},
				lake:
				{
					conditions: { natural: 'water'},
					colour : '#c0c0ff',
					featureclass: 'polygon',
					width: 3
				},
				common:
				{
					conditions: { leisure: 'common'},
					colour : '#c0ffc0',
					featureclass: 'polygon',
					width: 3
				},
				crag:
				{
					conditions: { natural: 'crag'},
					colour : '#808080',
					featureclass: 'polygon',
					width: 3
				},
				scree:
				{
					conditions: { natural: 'scree'},
					colour : '#808080',
					featureclass: 'polygon',
					width: 3
				},
				'golf course':
				{
					conditions: { amenity: 'golf_course'},
					colour : '#c0ffc0',
					featureclass: 'polygon',
					width: 3
				},
				unknown :
				{ colour : 'gray',
			       featureclass: 'line',
				  width : 4 
				},
				pub: { conditions: {amenity: 'pub'}, featureclass:'point'},
				peak: { conditions: {natural: 'peak'}, featureclass:'point' },
				village: { conditions:{place: 'village'}, featureclass:'point'},
				hamlet: { conditions:{place: 'hamlet'}, featureclass:'point' },
				suburb: { conditions:{place: 'suburb'}, featureclass:'point' },
				town: { conditions:{place: 'town'}, featureclass:'point' },
				'church': { conditions:{amenity: 'place_of_worship',
						religion:'christian'}, featureclass:'point' },
				'C of E church': { conditions:{amenity: 'place_of_worship',
						denomination:'anglican'}, featureclass:'point' },
				'Catholic church': { conditions:{amenity: 'place_of_worship',
						denomination:'catholic'}, featureclass:'point' },
				'mosque': { conditions:{amenity: 'place_of_worship',
						religion:'muslim'}, featureclass:'point' },
				'synagogue': { conditions:{amenity: 'place_of_worship',
						religion:'jewish'}, featureclass:'point' },
				viewpoint: { conditions: {tourism: 'viewpoint'}, 
					featureclass:'point' },
				campsite: { conditions: {tourism: 'camp_site'}, 
					featureclass:'point' },
				hotel: { conditions: {tourism: 'hotel'}, featureclass:'point' },
				mast: { conditions: {man_made: 'mast'}, featureclass:'point' },
				farm: { conditions: {residence: 'farm'},featureclass:'point' },
				cinema: {conditions: {amenity: 'cinema'},featureclass:'point' },
				'post office': {conditions: {amenity: 'post_office'},
					featureclass:'point' },
				'post box': {conditions: {amenity: 'post_box'},
					featureclass:'point' },
				'toilets': {conditions: {amenity: 'toilets'},
					featureclass:'point' },
				'chemist': {conditions: {amenity: 'pharmacy'},
					featureclass:'point' },
				'library': {conditions: {amenity: 'library'},
					featureclass:'point' },
				'police station': {conditions: {amenity: 'police'},
					featureclass:'point' },
				'fire station': {conditions: {amenity: 'fire_station'},
					featureclass:'point' },
				'theatre': {conditions: {amenity: 'theatre'},
					featureclass:'point' },
				'restaurant': {conditions: {amenity: 'restaurant'},
					featureclass:'point' },
				'fast food': {conditions: {amenity: 'fast_food'},
					featureclass:'point' },
				'tea shop': {conditions: {amenity: 'teashop'},
					featureclass:'point' },
				'car park': { conditions: {amenity: 'parking'},
						featureclass:'point' },
				'fuel station' : { conditions: {amenity:'fuel'},
						featureclass:'point' },
				'supermarket' :  {conditions: {amenity:'supermarket'},
						featureclass:'point'},
				'golf course (node)': { conditions: {leisure: 'golf_course'},
						featureclass:'point'},
				'country house':
					{ conditions: { residence: 'country_house'},
					 featureclass:'point' },
				attraction:
					{ conditions: { tourism: 'attraction'},
							featureclass:'point'},
				'railway station':
					{ conditions: { railway: 'station'},
							featureclass: 'point' }
			};

	this.getTags = function(type) {
		return (rt[type] ? rt[type]['conditions']:null);
	}

	this.getType = function(kv) {

		var type="unknown", bestMatch=0, match, mismatch,
				routeType, conditionKey, suppliedKey;

		for (routeType in rt)
		{
			mismatch=false;
			match=0;
			//alert('routeType=' + routeType);
			for (conditionKey in rt[routeType]['conditions'])
			{
				//alert('conditionKey=' + conditionKey);
				if(kv[conditionKey])
				{
					//alert('this key is in the supplied keyvals');
					for(suppliedKey in kv)
					{
						if(suppliedKey[kv]!==null)
						{
							//alert('suppliedKey=' + suppliedKey);
							// Matching keys/values - increase match count
							if(conditionKey==suppliedKey && 
							rt[routeType]['conditions'][conditionKey]==
							kv[suppliedKey])
							{
								//alert('match');
								match++;
							}
							// Matching keys but non-matching values-not a match
							else if ( (conditionKey==suppliedKey && 
							rt[routeType]['conditions'][conditionKey]!=
							kv[suppliedKey])  )
							{
								//alert('mismatch');
								match=0;
								mismatch=true;
								break;
							}
						}
					}
				}
				else
				{
					//alert('mismatch - conditionKey not in supplied');
					mismatch=true;
				}
				if(mismatch) break;
			}
			if(match>bestMatch)
			{
				bestMatch=match;
				type=routeType;
			}
		}
		return type;
	}

	this.getColour = function(type) {
		return rt[type]['colour'];
	}

	this.getWidth = function(type) { 
		return (rt[type]['width']) ? rt[type]['width'] : false;
	}

	this.isPolygon = function(type) {
		return (rt[type]['featureclass'] && rt[type]['featureclass']=='polygon')
			? true:false;
	}

	this.getFeatureClass = function(type) {
		return (rt[type]['featureclass']) ? rt[type]['featureclass']:null;
	}

	this.getTypes = function(featureClass) {
		var types = new Array();
		for (var key in rt) {
			if (rt[key]['featureclass']==featureClass) {
				types.push(key);
			}
		}
		return types;
	}
}
