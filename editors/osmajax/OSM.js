
OpenLayers.Layer.OSM = OpenLayers.Class.create();
OpenLayers.Layer.OSM.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Layer.Vector, {

	location: "http://www.free-map.org.uk/common/osmproxy.php",
	nodes : null,
	segments : null,
	ways : null,
	routeTypes : null,
	nextNodeFid : -1,
	nextSegmentFid : -1,
	nextWayFid: -1,

	initialize: function(name,options) {
		OpenLayers.Layer.Vector.prototype.initialize.apply(this,arguments);
		this.nodes = new Array();
		this.segments = new Array();
		this.ways = new Array();
		this.routeTypes = new RouteTypes();

	},

	load: function(bounds) {
		var bboxURL = this.location + "?bbox=" + bounds.toBBOX(); 
		alert('load: ' + bboxURL);
		OpenLayers.loadURL(bboxURL,null,this,this.parseData);
	},

	parseData: function(ajaxRequest) {
		var doc = ajaxRequest.responseXML;
		if(!doc || ajaxRequest.fileType!="XML") {
			doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
		}

		var n = doc.getElementsByTagName("node"),
			s = doc.getElementsByTagName("segment"),
			w = doc.getElementsByTagName("way");

		alert('Got ' + n.length + ' nodes, ' + s.length + 
				' segments, ' + w.length + ' ways.');


		// insert parsing from osmajax
		for(var count=0; count<n.length; count++)
		{
			var id = n[count].getAttribute("id");
			var lat = n[count].getAttribute("lat");
			var lon = n[count].getAttribute("lon");

		
			// Create point feature 
			var point = new OpenLayers.Geometry.Point(lon,lat);
			//var point = new OpenLayers.Geometry.OSMNode(lon,lat);
			//point.id = id;

			var tags = n[count].getElementsByTagName("tag");
			for(var count2=0; count2<tags.length; count2++)
			{
				var k = tags[count2].getAttribute("k");
				var v = tags[count2].getAttribute("v");
				//point.addTag(k,v);
			}

			this.nodes[id] = point;

			// If the node is a point of interest (i.e. contains tags other
			// than 'created_by'), add it to the vector layer as a point 
			// feature.
			/*
			if (point.isPOI())
			{
				this.addFeatures(new OpenLayers.Feature.Vector(point));
			}
			*/
		}

		for(var count=0; count<s.length; count++)
		{
			var id = s[count].getAttribute("id");
			var from = s[count].getAttribute("from");
			var to = s[count].getAttribute("to");
		
			// Create a new line feature or something.

			// Add line feature to the layer - if both nodes exist
			// this.nodes is an array of node features
			if(this.nodes[from] && this.nodes[to])
			{
				this.segments[id] = [ from, to ]; 
				/*
				this.segments[id] = new OSMSegment();
				this.segments[id].id = id;
				this.segments[id].setNodes(from,to);
				var tags = s[count].getElementsByTagName("tag");
				for(var count2=0; count2<tags.length; count2++)
				{
					var k = tags[count2].getAttribute("k");
					var v = tags[count2].getAttribute("v");
					this.segments[id].addTag(k,v);
				}
				*/
			}
		}

		// Ways become OpenLayers LineStrings. 
		for(var count=0; count<w.length; count++)
		{
			var id = w[count].getAttribute("id");
			if(id>=223)
			{

			// Create a polyline object


			var segs = w[count].getElementsByTagName("seg");


			var t = w[count].getElementsByTagName("tag");
			//alert('way id : ' + id + ' tags=' +  tags.length);

			var tags = new Array();
			var polygon=false;

			for(var count2=0; count2<t.length; count2++)
			{
				var k = t[count2].getAttribute("k");
				var v = t[count2].getAttribute("v");
				tags[k] = v;
				if( (k=="natural" && v=="wood") || (k=="natural" && v=="water")
					|| (k=="natural" && v=="heath"))
				{
					polygon=true;
				}
			}

			//alert(curWay.tagsToString());
			var tp = (this.routeTypes.getType(tags));

			var colour = this.routeTypes.getColour(tp);
			var width = this.routeTypes.getWidth(tp);
			//alert('Type: ' + tp + ' colour: ' + colour +  ' width: ' + width);


			if(!polygon)
			{
				var wayGeom = new OpenLayers.Geometry.LineString();
				var segids = new Array();

				for(var count2=0; count2<segs.length; count2++)
				{
					var sid = segs[count2].getAttribute("id");
					segids.push(sid);
					if(this.segments[sid])
					{
						if(wayGeom.components.length==0)
						{
							wayGeom.addComponent 
								(this.nodes[this.segments[sid][0]]);
						}
						wayGeom.addComponent 
							(this.nodes[this.segments[sid][1]]);
					}
				}

				var style = { fillColor: colour, fillOpacity: 0.4,
							strokeColor: colour, strokeOpacity: 1,
							strokeWidth: width };
				var curWay = new OpenLayers.Feature.OSMWay(wayGeom,null,style);
				curWay.setFid(id);
				curWay.tags = tags;
				curWay.setType(tp);
				this.addFeatures(curWay);
			}
			}
		}
	},

	/*
	 Must set Feature ID first if it's an existing feature
	 */
	addFeatures : function(feature) {
		// default addFeatures()

		// Test we don't have an existing feature there...
		OpenLayers.Layer.Vector.prototype.addFeatures.apply(this,arguments);


		// Also add the feature to the nodes or segments array.
		// If it's a new feature (no feature ID) then allocate a negative ID.
		if (feature instanceof OpenLayers.Feature.OSMWay &&
				!(this.ways[feature.fid])) {
			if(feature.fid===null)
			{
				feature.fid=this.nextWayFid--;
			}
			this.ways[feature.fid] = feature;
		}
	},

	CLASS_NAME: "OpenLayers.Layer.OSM"
});
