
function in_array(nodes,nodeid)
{
	for(var idx in nodes)
	{
		if(nodes[idx].nid == nodeid)
			return true;
	}
	return false;
}

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
	numNodesToBeUploaded : 0,
	numSegsToBeUploaded : 0,

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
			//var point = new OpenLayers.Geometry.Point(lon,lat);
			var point = new OSMNode();
			point.point = new OpenLayers.Geometry.Point(lon,lat);
			point.nid = id;

			var tags = n[count].getElementsByTagName("tag");
			if(tags)
			{
				for(var count2=0; count2<tags.length; count2++)
				{
					var k = tags[count2].getAttribute("k");
					var v = tags[count2].getAttribute("v");
					point.addTag(k,v);
				}
			}

			this.nodes[id] = point;

			// If the node is a point of interest (i.e. contains tags other
			// than 'created_by'), add it to the vector layer as a point 
			// feature.
			
			if (point.isPOI())
			{
				this.addFeatures(new OpenLayers.Feature.OSM(point.point));
			}
			
		}
		//alert('nodes set up');

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
				//this.segments[id] = [ from, to ]; 
				
				this.segments[id] = new OSMSegment();
				this.segments[id].id = id;
				this.segments[id].setNodes(this.nodes[from], this.nodes[to]);
				var tags = s[count].getElementsByTagName("tag");
				if(tags)
				{
					for(var count2=0; count2<tags.length; count2++)
					{
						var k = tags[count2].getAttribute("k");
						var v = tags[count2].getAttribute("v");
						this.segments[id].addTag(k,v);
					}
				}
			}
		}
		//alert('segments set up');

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
	
			if(t)
			{
				var tags = new Array();
				var polygon=false;

				for(var count2=0; count2<t.length; count2++)
				{
					var k = t[count2].getAttribute("k");
					var v = t[count2].getAttribute("v");
					tags[k] = v;
					

					// TODO: proper tag testing
					if( (k=="natural" && v=="wood") || 
					(k=="natural" && v=="water")
					|| (k=="natural" && v=="heath"))
					{
						polygon=true;
					}
				}
			}

			//alert(curWay.tagsToString());
			var tp = this.routeTypes.getType(tags);

			var colour = this.routeTypes.getColour(tp);
			var width = this.routeTypes.getWidth(tp);

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
								(this.segments[sid].nodes[0].point);
						}
						wayGeom.addComponent 
							(this.segments[sid].nodes[1].point);
					}
				}

				var style = { fillColor: colour, fillOpacity: 0.4,
							strokeColor: colour, strokeOpacity: 1,
							strokeWidth: width };
				var curWay = new OpenLayers.Feature.OSMWay(wayGeom,null,style);
				curWay.fid=id;
				curWay.tags = tags;
				curWay.setType(tp);
				curWay.segs = segids;
				this.addFeatures(curWay);
			}
			}
		}
		//alert('ways set up');
	},

	/*
	 Must set Feature ID first if it's an existing feature
	 */
	addFeatures : function(feature) {
		// default addFeatures()

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

	uploadNewWay: function(way)
	{

		// Loop through nodes
		// Upload nodes with ID > 0
		// Count nodes with ID>0, in callback function reduce 
		// count by 1
		// when count=0, start 

		if(way.fid < 0)
		{
			alert('uploadNewWay(): uploading a new way');
			var newNodes = new Array();
			var info = { 'way' : way, 'node' : null }; 

			for(var count=0; count<way.segs.length; count++)
			{
				if(this.segments[way.segs[count]].nodes[0].nid<0 && 
			!in_array(newNodes,this.segments[way.segs[count]].nodes[0].nid))
				{
					newNodes.push(this.segments[way.segs[count]].nodes[0]);
					alert('seg: ' + way.segs[count] + 
							' adding new node ID ' +
							this.segments[way.segs[count]].nodes[0].nid);
				}
				if(this.segments[way.segs[count]].nodes[1].nid<0 &&
			!in_array(newNodes,this.segments[way.segs[count]].nodes[1].nid))
				{
					newNodes.push(this.segments[way.segs[count]].nodes[1]);
					alert('seg: ' + way.segs[count] + 
							' adding new node ID ' +
							this.segments[way.segs[count]].nodes[1].nid);
				}
			}

			this.numNodesToBeUploaded = newNodes.length;
			alert('uploadNewWay(): no. of new nodes: ' + 
				this.numNodesToBeUploaded);

			for(var count=0; count<newNodes.length; count++)
			{
				var info1 = new Array();
				info1.node = newNodes[count];
				info1.way = way;
				newNodes[count].upload
					('http://www.free-map.org.uk/vector/dummy.php',
						this,this.newWayNodeHandler,info1);
			}

			// If no nodes to upload, go straight to uploading segments
			if(!newNodes.length)
			{
				this.uploadWaySegments(way);
			}
		}
	},

	newWayNodeHandler: function(xmlHTTP, info)
	{
		alert('newWayNodeHandler: old id was: ' + info.node.nid +
			' new id is: ' + xmlHTTP.responseText );
		var nodeid = parseInt(xmlHTTP.responseText);

		// Blank original negative node index
		this.nodes[info.node.nid] = null;
		info.node.nid = nodeid;

		// Index the new node
		this.nodes[info.node.nid] = info.node;

		if(--this.numNodesToBeUploaded==0)
		{
			alert('all nodes have an ID. doing segments');
			this.uploadWaySegments(info.way);
		}
	},

	uploadWaySegments: function(way)
	{
		alert('uploadWaySegments');
		var newSegs = new Array();
		var info = { 'way' : way, 'segment' : null }; 

		for(var count=0; count<way.segs.length; count++)
		{
			if(way.segs[count] < 0)
			{
				newSegs.push(this.segments[way.segs[count]]);
			}
		}

		this.numSegsToBeUploaded = newSegs.length;
		alert('uploadWaySegments: number of segments=' + newSegs.length);
		if(newSegs.length)
		{
			for(var count=0; count<newSegs.length; count++)
			{
				var info1 = new Array();
				info1.segment = newSegs[count];
				info1.way = way;
				newSegs[count].upload
					('http://www.free-map.org.uk/vector/dummy.php',
						this,this.newWaySegmentHandler,info1);
			}
		}
		else
		{
			way.upload('http://www.free-map.org.uk/vector/dummy.php',
						this,this.wayDone);
		}
	},

	newWaySegmentHandler : function(xmlHTTP, info)
	{
		var segid = parseInt(xmlHTTP.responseText);
		alert('newWaySegmentHandler: old id was: ' + info.segment.id +
			' new id is: ' + xmlHTTP.responseText );

		// Blank original negative segment index
		this.segments[info.segment.id] = null;

		// Change the segment ID in the parent way
		for (var count=0; count<info.way.segs.length; count++)
		{
			if(info.way.segs[count] == info.segment.id)
				info.way.segs[count] = segid;
		}

		info.segment.id = segid;

		// Index the new segment
		this.segments[info.segment.id] = info.segment;


		if(--this.numSegsToBeUploaded==0)
		{
			alert('done all segments, now doing the way');
			info.way.upload('http://www.free-map.org.uk/vector/dummy.php',
							this,this.wayDone);
		}
	},

	wayDone : function(xmlHTTP,info) {

		// If info was passed it's a new way
		if(info) {
			var wayid = parseInt(xmlHTTP.responseText);
			alert('way uploaded. Way ID=' + wayid);

			// Blank original negative node index
			this.ways[info.fid] = null;
			info.fid = wayid;

			// Index the new node
			this.ways[info.fid] = info;

			alert('done.');
		}
	},

	CLASS_NAME: "OpenLayers.Layer.OSM"
});
