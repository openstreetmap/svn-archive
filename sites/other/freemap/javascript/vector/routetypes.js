
function RouteTypes()
{
	var rt = { footpath :
	            { conditions :
					{ highway : 'footway', foot : 'yes' } ,
				  colour : 'green',
				  width : 4
				},
			   bridleway :
			   	{ conditions :
					{ highway : 'bridleway', foot : 'yes',
				  	  horse: 'yes', bicycle : 'yes' } ,
				   colour : 'brown',
				   width : 4
				},
			   byway :
			   	{ conditions :
				 	{highway : 'byway', foot : 'yes',
				  	 horse: 'yes', bicycle:'yes',motorcar:'yes' } ,
				  colour : 'red',
				  width : 4
				},
			   residential :
			    { conditions :   { highway : 'residential' },
				  colour : 'gray',
				  width : 4
				},
			   'minor road'  :
			    { conditions: { highway : 'unclassified' },
				  colour : 'gray',
				  width : 5
				},
			   'B road' : 
			    { conditions: { highway : 'secondary' },
				  colour : '#fdbf6f',
				  width : 6
				},
			   'A road' :
			    { conditions : { highway : 'primary' },
				  colour : '#fb8059',
				  width : 6
				},
			   'trunk road' :
			    { conditions : { highway : 'trunk' },
				  colour : '#477147',
				  width : 6
				},
			   motorway :
			    { conditions: { highway : 'motorway' },
				  colour : '#809bc0',
				  width : 6
				},
			   railway : 
			    { conditions :  {railway : 'rail' },
				  colour : 'black',
				  width : 4
				},
			   'permissive footpath' :
			    { conditions : { highway : 'footway' },
				  colour : '#808080',
				  width : 4
				},
			   'permissive bridleway' :
			    { conditions : { highway : 'bridleway' },
				  colour : '#804000',
				  width : 4
				},
			   'cycle path' :
			    { conditions : { highway : 'cycleway' },
				  colour : 'magenta',
				  width: 4
				},
			   'unsurfaced road' :
			    { conditions :  
					{highway : 'unsurfaced'},
				  colour : 'gray',
				  width : 4
				},
				river:
				{
					conditions:
					{waterway: 'river' },
					colour: 'blue',
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
				  width : 4 
				},
				pub: { conditions: {amenity: 'pub'}, featureclass:'point'},
				peak: { conditions: {natural: 'peak'}, featureclass:'point' },
				village: { conditions:{place: 'village'}, featureclass:'point'},
				hamlet: { conditions:{place: 'hamlet'}, featureclass:'point' },
				town: { conditions:{place: 'town'}, featureclass:'point' },
				church: { conditions:{amenity: 'place_of_worship',
						denomination:'anglican'}, featureclass:'point' },
				viewpoint: { conditions: {tourism: 'viewpoint'}, 
					featureclass:'point' },
				mast: { conditions: {man_made: 'mast'}, featureclass:'point' },
				farm: { conditions: {residence: 'farm'},featureclass:'point' },
				'country house':
					{ conditions: { residence: 'country_house'},
					 featureclass:'point' }
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
		return (rt[type]['featureclass']) ? rt[type]['featureclass']:"line";
	}
}		
