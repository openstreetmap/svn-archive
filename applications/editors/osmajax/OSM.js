
function in_array(nodes,nodeid)
{
	for(var idx in nodes)
	{
		if(nodes[idx].osmid == nodeid)
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
	nextNodeId : -1,
	nextSegmentId : -1,
	nextWayId: -1,
	featureids : 1,
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

		var cvtr = new converter("OSGB");

		// insert parsing from osmajax
		for(var count=0; count<n.length; count++)
		{
			var id = n[count].getAttribute("id");
			var lat = n[count].getAttribute("lat");
			var lon = n[count].getAttribute("lon");

			// NPE
			var osgb = cvtr.normToCustom(new OpenLayers.LonLat(lon,lat));
			// END NPE
		
			// Create point feature 
			//var point = new OpenLayers.Geometry.Point(lon,lat);
//			alert('creating new feature');
			var point = new OpenLayers.OSMItem();
//			alert('creating new node');
			var point = new OpenLayers.OSMNode();
			//point.geometry = new OpenLayers.Geometry.Point(lon,lat);
			//NPE 
			point.geometry = new OpenLayers.Geometry.Point(osgb.lon,osgb.lat);
			//END NPE
			point.osmid = id;
//			if(count==0)alert('creating new node=done');

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

	//		alert('putting node in array of nodes');
			this.nodes[id] = point;
	//		alert('putting node in array of nodes done');

			// If the node is a point of interest (i.e. contains tags other
			// than 'created_by'), add it to the vector layer as a point 
			// feature.
			
	//		alert('testing point of itnerest');
			if (point.isPOI())
			{
	//			var f = new OpenLayers.Feature.OSM(point);
	//			this.addFeatures(f);
			}
	//		alert('testing point of itnerest - done');
			
		}
//		alert('nodes set up');

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
				
				this.segments[id] = new OpenLayers.OSMSegment();
				this.segments[id].osmid = id;
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
//		alert('segments set up');

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

				for(var count2=0; count2<t.length; count2++)
				{
					var k = t[count2].getAttribute("k");
					var v = t[count2].getAttribute("v");
					tags[k] = v;
				}
			}

			//alert(curWay.tagsToString());
			var tp = this.routeTypes.getType(tags);

			var colour = this.routeTypes.getColour(tp);
			var width = this.routeTypes.getWidth(tp);
			var polygon = this.routeTypes.isPolygon(tp);

			if(1 || !polygon)
			{
				var wayGeom = (polygon) ? 
					new OpenLayers.Geometry.LinearRing():
					new OpenLayers.Geometry.LineString();

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
								(this.segments[sid].nodes[0].geometry);
						}
						wayGeom.addComponent 
							(this.segments[sid].nodes[1].geometry);
					}
				}

				var style = { fillColor: colour, fillOpacity: 0.4,
							strokeColor: colour, strokeOpacity: 1,
							strokeWidth: (width===false ? 1:width) };

				var curWay = new OpenLayers.OSMWay();
				curWay.geometry = 
					(polygon ? 
						new OpenLayers.Geometry.Polygon ([wayGeom]):
						wayGeom); 

				curWay.osmid=id;
				curWay.tags = tags;
				curWay.setType(tp);
				curWay.segs = segids;
				this.addFeatures(new OpenLayers.Feature.OSM(curWay,null,style));
				this.ways[id] = curWay;
			}
			}
		}
//		alert('ways set up');
	},

	uploadNewWay: function(w)
	{
	
		var way = w.osmitem;

		// Loop through nodes
		// Upload nodes with ID > 0
		// Count nodes with ID>0, in callback function reduce 
		// count by 1
		// when count=0, start 

		if(way.osmid < 0)
		{
			statusMsg('uploadNewWay(): uploading a new way');
			var newNodes = new Array();
			var info = { 'way' : way, 'node' : null }; 

			for(var count=0; count<way.segs.length; count++)
			{
				if(this.segments[way.segs[count]].nodes[0].osmid<0 && 
			!in_array(newNodes,this.segments[way.segs[count]].nodes[0].osmid))
				{
					newNodes.push(this.segments[way.segs[count]].nodes[0]);
					statusMsg('seg: ' + way.segs[count] + 
							' adding new node ID ' +
							this.segments[way.segs[count]].nodes[0].osmid);
				}
				if(this.segments[way.segs[count]].nodes[1].osmid<0 &&
			!in_array(newNodes,this.segments[way.segs[count]].nodes[1].osmid))
				{
					newNodes.push(this.segments[way.segs[count]].nodes[1]);
					statusMsg('seg: ' + way.segs[count] + 
							' adding new node ID ' +
							this.segments[way.segs[count]].nodes[1].osmid);
				}
			}

			this.numNodesToBeUploaded = newNodes.length;
			statusMsg('uploadNewWay(): no. of new nodes: ' + 
				this.numNodesToBeUploaded);

			for(var count=0; count<newNodes.length; count++)
			{
				var info1 = new Array();
				info1.node = newNodes[count];
				info1.w = w;
				newNodes[count].upload
					//('http://www.free-map.org.uk/vector/dummy.php',
					('http://www.free-map.org.uk/common/osmproxy2.php?call=node&id=0',
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
		/*
		alert('newWayNodeHandler: old id was: ' + info.node.osmid +
			' new id is: ' + xmlHTTP.responseText );
		*/
		var nodeid = parseInt(xmlHTTP.responseText);

		// Blank original negative node index
		this.nodes[info.node.osmid] = null;
		info.node.osmid = nodeid;

		// Index the new node
		this.nodes[info.node.osmid] = info.node;

		if(--this.numNodesToBeUploaded==0)
		{
			statusMsg('All nodes were uploaded successfully. Doing segments');
			this.uploadWaySegments(info.w);
		}
	},

	uploadWaySegments: function(w)
	{
		var way = w.osmitem;
		statusMsg('uploadWaySegments');
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
		statusMsg('uploadWaySegments: number of segments=' + newSegs.length);
		if(newSegs.length)
		{
			for(var count=0; count<newSegs.length; count++)
			{
				var info1 = new Array();
				info1.segment = newSegs[count];
				info1.w = w;
				newSegs[count].upload
	//				('http://www.free-map.org.uk/vector/dummy.php',
					('http://www.free-map.org.uk/common/osmproxy2.php?call=segment&id=0',
						this,this.newWaySegmentHandler,info1);
			}
		}
		else
		{
//			way.upload('http://www.free-map.org.uk/vector/dummy.php',
			way.upload(
			'http://www.free-map.org.uk/common/osmproxy2.php?call=way&id=0',
						this,this.wayDone,way);
		}
	},

	newWaySegmentHandler : function(xmlHTTP, info)
	{
		var segid = parseInt(xmlHTTP.responseText);
		/*
		alert('newWaySegmentHandler: old id was: ' + info.segment.osmid +
			' new id is: ' + xmlHTTP.responseText );
		*/

		// Blank original negative segment index
		this.segments[info.segment.osmid] = null;

		// Change the segment ID in the parent way
		for (var count=0; count<info.w.osmitem.segs.length; count++)
		{
			if(info.w.osmitem.segs[count] == info.segment.osmid)
				info.w.osmitem.segs[count] = segid;
		}

		info.segment.osmid = segid;

		// Index the new segment
		this.segments[info.segment.osmid] = info.segment;


		if(--this.numSegsToBeUploaded==0)
		{
			statusMsg('all segments uploaded successfully, now doing the way');
//			info.w.osmitem.upload('http://www.free-map.org.uk/vector/dummy.php',
			info.w.osmitem.upload('http://www.free-map.org.uk/common/osmproxy2.php?call=way&id=0',
							this,this.wayDone,info.w.osmitem);
		}
	},

	wayDone : function(xmlHTTP,info) {

		// If info was passed it's a new way
		if(info) {
			var wayid = parseInt(xmlHTTP.responseText);
			statusMsg('way uploaded. Way ID=' + wayid);

			// Blank original negative node index
			this.ways[info.osmid] = null;
			info.osmid = wayid;

			// Index the new node
			this.ways[info.osmid] = info;

			statusMsg('Way uploaded successfully. ID=' + wayid);
		}
	},

	CLASS_NAME: "OpenLayers.Layer.OSM"
});
