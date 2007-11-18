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
		var prev=null, feat, lastId, thisId, firstId, segs = new Array();

		if(geometry instanceof OpenLayers.Geometry.LineString||
			geometry instanceof OpenLayers.Geometry.Polygon) 
		{
			if(geometry instanceof OpenLayers.Geometry.Polygon)
			{
				// Remove duplicates in the LinearRing
				var nonDupComponents = new Array();
				var lngth = geometry.components[0].components.length;
				for(var count=0; count<lngth-1; count++)
				{
					if(! (geometry.components[0].components[count].equals
							(geometry.components[0].components[count+1])))
					{
						nonDupComponents.push	
							(geometry.components[0].components[count]);
					}
					else
					{
//						alert('found a duplicate at point ' + count);
					}
				}
				if(! (geometry.components[0].components[lngth-1].equals
							(geometry.components[0].components[0])))
				{
						nonDupComponents.push	
							(geometry.components[0].components[lngth-1]);
				}
				else
				{
//						alert('found a duplicate at point ' + (lngth-1));
				}
				geometry.components[0].components = nonDupComponents;
			}

			var realComponents = 
				(geometry instanceof OpenLayers.Geometry.Polygon) ?
				geometry.components[0].components: geometry.components;

			for(var count=0; count<realComponents.length; count++) 
			{
				// Check whether the new points are near any current nodes
				// If so, use the existing node. 
				var found=false;
				/* Don't do this for polygon and POI only mode
				for(nodeid in this.layer.nodes)	
				{
					if(this.layer.nodes[nodeid]) 
					{
						ll1 = new OpenLayers.LonLat
							(this.layer.nodes[nodeid].geometry.x,
						 	this.layer.nodes[nodeid].geometry.y);

						ll2 = new OpenLayers.LonLat 
							(realComponents[count].x,
							realComponents[count].y);

						var px1 = this.layer.map.getViewPortPxFromLonLat(ll1);
						var px2 = this.layer.map.getViewPortPxFromLonLat(ll2);
						
						var dx = px2.x-px1.x;
						var dy = px2.y-px1.y;
						var dist = Math.sqrt(dx*dx + dy*dy);
//						if(OpenLayers.Util.distVincenty(ll1,ll2) < 0.01) 
						if(dist < 3)
						{
							found=nodeid;
							break;
						}
					} 
				}
				*/

				// If one of the points is near an existing node, use the
				// existing node in the geometry.
				if(found!==false)
				{
					alert('point ' + count + ' near an existing node ');
					if(geometry instanceof OpenLayers.Geometry.Polygon)
					{
						geometry.components[0].components[count] = 
							this.layer.nodes[found].geometry;
					}
					else
					{
							geometry.components[count] = 
									this.layer.nodes[found].geometry;
					}
					thisId = found; 
				}
				// If not, add the geometry point to the node list and give 
				// it a negative ID to indicate it's not in OSM yet.
				else
				{
					//alert('point ' + count + ' not near an existing node');
					thisId = this.layer.nextNodeId--;
					this.layer.nodes[thisId]=new OpenLayers.OSMNode();
					this.layer.nodes[thisId].geometry = realComponents[count];
					this.layer.nodes[thisId].tags['created_by'] = 'osmajax';
					this.layer.nodes[thisId].osmid = thisId;
				}

				// Create a new segment.
				if(count>0)
				{
					var segment =	new OpenLayers.OSMSegment();
					segment.osmid = this.layer.nextSegmentId;
					segment.setNodes(this.layer.nodes[lastId], 
										this.layer.nodes[thisId]);
					this.layer.segments[this.layer.nextSegmentId] = segment;
					segs.push(this.layer.nextSegmentId--);
				}
				else // for closing polygons
				{
					firstId = thisId;
				}	

				lastId = thisId;
			}

			// If we're doing a polygon, create a final segment to close
			// the polygon.
			if(geometry instanceof OpenLayers.Geometry.Polygon)
			{
				var segment =	new OpenLayers.OSMSegment();
				segment.osmid = this.layer.nextSegmentId;
				segment.setNodes(this.layer.nodes[lastId], 
										this.layer.nodes[firstId]);
				this.layer.segments[this.layer.nextSegmentId] = segment;
				segs.push(this.layer.nextSegmentId--);
			}

			var style = { fillColor: 'gray', fillOpacity: 0.4,
							strokeColor: 'gray', strokeOpacity: 1,
							strokeWidth: 3 };
			feat = new OpenLayers.OSMWay();
			feat.osmid = this.layer.nextWayId;
			feat.tags['created_by'] = 'osmajax';
			this.layer.ways[this.layer.nextWayId--] = feat; 
			feat.geometry=geometry;
			feat.setType('unknown');
			feat.segs = segs;
			var style = { fillColor: 'gray', fillOpacity: 0.4,
							strokeColor: 'gray', strokeOpacity: 1,
							strokeWidth: 3 };
			//var feat2= new OpenLayers.Feature.OSM(feat,null,style);

			// Don't add feature yet - only do it when actually uploaded
			// successfully to OSM.
			//this.layer.addFeatures(feat2);
			//this.featureAdded(feat2);
			
			this.layer.uploadNewWay(feat);
		}
		else if (geometry instanceof OpenLayers.Geometry.Point)
		{
			var id = this.layer.nextNodeId--;
			this.layer.nodes[id]=new OpenLayers.OSMNode();
			this.layer.nodes[id].geometry = geometry; 
			this.layer.nodes[id].tags['created_by'] = 'osmajax';
			if(this.layer.basemap=='npe' || this.layer.basemap=='landsat')
				this.layer.nodes[id].tags['source'] = this.layer.basemap;
			this.layer.nodes[id].osmid = id;
			this.layer.uploadNewNode(this.layer.nodes[id]);
		}
//		alert('added new feature.');
	},

	CLASS_NAME: "OpenLayers.Control.DrawOSMFeature"
});
