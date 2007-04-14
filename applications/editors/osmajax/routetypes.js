
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
			   track :
			    { conditions :  
					{highway : 'unsurfaced', foot: 'permissive' },
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
				unknown :
				{ colour : 'gray',
				  width : 4 
				}
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
		return rt[type]['width'];
	}
}		
