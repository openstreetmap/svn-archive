OpenLayers.Control.DrawOSMFeature = OpenLayers.Class.create();

OpenLayers.Control.DrawOSMFeature.prototype =
	OpenLayers.Class.inherit(OpenLayers.Control.DrawFeature, {

	initialize: function(layer,handler,options) {
		OpenLayers.Control.DrawFeature.prototype.initialize.apply
				(this,arguments);
	},

	// This is overridden so that if a line was drawn, it's split into 
	// segments and each segment added to the layer.
	drawFeature : function(geometry) {

		var ll1, ll2;
		var prev=null, feature, lastId, thisId, segs = new Array();

		if(geometry instanceof OpenLayers.Geometry.LineString) 
		{
			alert('drawFeature(): this is a line string');
			for(var count=0; count<geometry.components.length; count++) 
			{
				// Check whether the new points are near any current nodes
				// If so, use the existing node. 
				var found=false;
				for(nodeid in this.layer.nodes)	
				{
					ll1 = new OpenLayers.LonLat
							(this.layer.nodes[nodeid].x,
						 	this.layer.nodes[nodeid].y);

					ll2 = new OpenLayers.LonLat (geometry.components[count].x,
								geometry.components[count].y);

					if(OpenLayers.Util.distVincenty(ll1,ll2) < 0.01) 
					{
						found=nodeid;
						break;
					} 
				}

				// If one of the points is near an existing node, use the
				// existing node in the geometry.
				if(found!==false)
				{
					alert('point ' + count + ' near an existing node ');
					geometry.components[count] = this.layer.nodes[nodeid];
					thisId = found; 
				}
				// If not, add the geometry point to the node list and give 
				// it a negative ID to indicate it's not in OSM yet.
				else
				{
					alert('point ' + count + ' not near an existing node');
					thisId = this.layer.nextNodeFid--;
					this.layer.nodes[thisId]=geometry.components[count];
				}

				// Create a new segment.
				if(count>0)
				{
					this.layer.segments[this.layer.nextSegmentFid] = 
							[ lastId, thisId ];
					segs.push(this.layer.nextSegmentFid--);
				}

				lastId = thisId;
			}
			var style = { fillColor: 'gray', fillOpacity: 0.4,
							strokeColor: 'gray', strokeOpacity: 1,
							strokeWidth: 3 };
			feat = new OpenLayers.Feature.OSMWay(geometry,null,style);
			feat.setType('unknown');
			feat.segs = segs;
		}
		else if (geometry instanceof OpenLayers.Geometry.Point)
		{
			feat = new OpenLayers.Feature.OSM(geometry);
		}
		this.layer.addFeatures(feat);
		this.featureAdded(feat);
		alert('added new feature.');
	},
	CLASS_NAME: "OpenLayers.Control.DrawOSMFeature"
});
