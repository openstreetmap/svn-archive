OpenLayers.Layer.OSM = OpenLayers.Class.create();
OpenLayers.Layer.OSM.prototype = 
    OpenLayers.Class.inherit (OpenLayers.Layer.Vector, {

    location: "http://www.free-map.org.uk/freemap/common/osmproxy2.php",
    nodes : null,
    ways : null,
    routeTypes : null,
    nextNodeId : -1,
    nextWayId: -1,
    featureids : 1,
    numNodesToBeUploaded : 0,
	statusCallback: null,
	existingWaysToBeModified: null,
	uploadError: false,

    initialize: function(name,statCB,options) {
		this.statusCallback=statCB;
		var newArguments = new Array();
		newArguments.push(name);
		newArguments.push(options);
        OpenLayers.Layer.Vector.prototype.initialize.apply(this,newArguments);
        this.nodes = new Array();
        this.ways = new Array();
        this.routeTypes = new RouteTypes();
    },

    load: function(bounds) {
        var bboxURL = this.location + "?bbox=" + bounds.toBBOX(); 
        OpenLayers.loadURL(bboxURL,null,this,this.parseData);
    },

    parseData: function(ajaxRequest) {
        var doc = ajaxRequest.responseXML;
        if(!doc || ajaxRequest.fileType!="XML") {
            doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
        }

        var n = doc.getElementsByTagName("node"),
            w = doc.getElementsByTagName("way");

        this.statusCallback
			('Got ' + n.length + ' nodes, ' + w.length + ' ways.');

        var cvtr = new converter("Mercator");

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
            var point = new OpenLayers.OSMNode();
            //point.geometry = new OpenLayers.Geometry.Point(lon,lat);
            //NPE 
            point.geometry = new OpenLayers.Geometry.Point(osgb.lon,osgb.lat);
            //END NPE
            point.osmid = id;

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
                var tp = this.routeTypes.getType(point.tags);
                if(tp!="unknown")    
                {
                    var f = new OpenLayers.Feature.OSM(point);
                    this.addFeatures(f);
                    this.nodes[id].setType(tp);
                }
            }
            
        }

        var exSeg;

        // Ways become OpenLayers LineStrings. 
        for(var count=0; count<w.length; count++)
        {
            var id = w[count].getAttribute("id");
            if(id<223)
                continue;

            exSeg = false;

            // Create a polyline object


            var nds = w[count].getElementsByTagName("nd");
                


            var t = w[count].getElementsByTagName("tag");
    
            if(t)
            {
                var tags = new Array();

                for(var count2=0; count2<t.length; count2++)
                {
                    var k = t[count2].getAttribute("k");
                    var v = t[count2].getAttribute("v");
                    tags[k] = v;
                    if((k=="note" && v=="FIXME previously unwayed segment") ||
                       (k=="note" && v=="FIXME previously tagged segment"))
                    {
                        exSeg = true;
                    }
                }
            }

            if(!exSeg)
            {
                var tp = this.routeTypes.getType(tags);

                var colour = this.routeTypes.getColour(tp);
                var width = this.routeTypes.getWidth(tp);
                var polygon = this.routeTypes.isPolygon(tp);

                if(!polygon)
                {
                    var wayGeom = (polygon) ? 
                    new OpenLayers.Geometry.LinearRing():
                    new OpenLayers.Geometry.LineString();

                    var ndids = new Array();

                    for(var count2=0; count2<nds.length; count2++)
                    {
                        var ndid = nds[count2].getAttribute("ref");
                        ndids.push(ndid);
                        if(this.nodes[ndid])
                        {
                            wayGeom.addComponent (this.nodes[ndid].geometry);
                            this.nodes[ndid].way = id; 
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
                    curWay.nds = ndids;
                    this.addFeatures
                        (new OpenLayers.Feature.OSM(curWay,null,style));
                    this.ways[id] = curWay;
                }
            }
        }
    },
    
    uploadNewNode: function (node)
    {
            var info1 = new Array();
            info1.node = node; 
            node.upload
            ('http://www.free-map.org.uk/freemap/common/osmproxy2.php?'+
                'call=node&id=0',
                        this,this.newNodeHandler,info1);
    },

    uploadNewWay: function(way)
    {
		this.uploadError = false;
   		this.existingWaysToBeModified = new Array(); 

        // Loop through nodes
        // Upload nodes with ID > 0
        // Count nodes with ID>0, in callback function reduce 
        // count by 1
        // when count=0, start 

        if(way.osmid < 0)
        {
            this.statusCallback('uploadNewWay(): uploading a new way');
            var newNodes = new Array(), nodeWayIndices = new Array();
            var info = { 'way' : way, 'node' : null, 'idxInWay' : null }; 
            var newNodeFirst = null, newNodeLast = null;

            // Create an array of new nodes in the way
            for(var count=0; count<way.nds.length; count++)
            {
                if(this.nodes[way.nds[count]].osmid<0)
                {
                    newNodes.push(this.nodes[way.nds[count]]);
                    nodeWayIndices.push(count);
                }
            }

            this.numNodesToBeUploaded = newNodes.length;

			
            this.statusCallback('uploadNewWay(): no. of new nodes: ' + 
                newNodes.length);

            for(var count=0; count<newNodes.length; count++)
            {
                var info1 = new Array();
                info1.node = newNodes[count];
                info1.way = way;
                info1.idxInWay = nodeWayIndices[count]; 
                newNodes[count].upload
                    ('http://www.free-map.org.uk/freemap/common/osmproxy2.php?'+
                     'call=node&id=0', this,this.newNodeHandler,info1);
            }

            // If no nodes to upload, go straight to uploading way 
            if(!newNodes.length)
            {
                this.uploadWay(way);
            }
        }
    },

    uploadWay: function(way)
    {
        way.upload(
            'http://www.free-map.org.uk/freemap/common/osmproxy2.php?'+
            'call=way&id=0', this,this.wayDone,way);
    },

    newNodeHandler: function(xmlHTTP, info)
    {
		if(this.uploadError==true) return;

		if(xmlHTTP.status!=200)
		{
			if(xmlHTTP.status==401) 
			{
				alert("You're not logged in to OSM correctly, so can't upload");
			}
			else
			{
				alert('Error uploading node in way: Error code=' + 
					xmlHTTP.status);
			}

			this.uploadError = true;
			return;
		}

        var nodeid = parseInt(xmlHTTP.responseText);

        // Blank original negative node index
        this.nodes[info.node.osmid] = null;
        info.node.osmid = nodeid;

        // Index the new node
        this.nodes[info.node.osmid] = info.node;


        // If we're in  the process of uploading a way, continue
        if(info.way)
        {
            // Update the way IDs to reflect the ID of the newly uploaded node 
            if(info.idxInWay||info.idxInWay===0)
            {
                //alert('resetting node ID at index = ' + info.idxInWay );
                 info.way.nds[info.idxInWay] = nodeid;
            }

			// Find out if this new node is near any *existing* ways
			var wayIntersectInfo = this.isPointNearWay(info.node);
			if(wayIntersectInfo)
			{
				for(var count=0; count<wayIntersectInfo.length; count++)
				{
					alert('Point ' + info.idxInWay + ' in way intersects an ' +
					  'existing way: way id=' 
					  + wayIntersectInfo[count].osmid + 
					  ' nodeidx=' +
					  wayIntersectInfo[count].ndidx);

					this.modifyExistingWay(wayIntersectInfo[count]);

					var found=false;

					for(var count2=0;
						count2<this.existingWaysToBeModified.length; 
						count2++)
					{
						if(this.existingWaysToBeModified[count]==
							wayIntersectInfo[count].osmid)
						{
							found=true;
							break;
						}
					}

					if(!found)
					{ 
						this.existingWaysToBeModified.push
							(wayIntersectInfo[count].osmid);
					}
				}
			}


            if(--this.numNodesToBeUploaded==0)
            {
                this.uploadWay(info.way);
            }
        }
        // Otherwise it's just a node so create a feature out of it
        else
        {
            var f = new OpenLayers.Feature.OSM(info.node);
            this.addFeatures(f);
            this.statusCallback
				('Point of interest added successfully. ID=' + nodeid);
        }
    },

    wayDone : function(xmlHTTP,info) {
		if(this.uploadError==true) return;
		if(xmlHTTP.status!=200)
		{
			if(xmlHTTP.status==401) 
			{
				alert("You're not logged in to OSM correctly, so can't upload");
			}
			else
			{
				alert('Error uploading way: error code=' + 
					xmlHTTP.status);
			}

			this.uploadError = true;
			return;
		}

        // If info was passed it's a new way
        if(info) {
            var wayid = parseInt(xmlHTTP.responseText);

            // Blank original negative node index
            this.ways[info.osmid] = null;
            info.osmid = wayid;

            // Index the new node
            this.ways[info.osmid] = info;

            // Create a new feature out of the way
            var style = { fillColor: 'gray', fillOpacity: 0.4,
                            strokeColor: 'gray', strokeOpacity: 1,
                            strokeWidth: 3 };
            var wayFeature= new OpenLayers.Feature.OSM(info,null,style);
            this.addFeatures(wayFeature);

            for(var count=0; count<info.nds.length; count++) {
                this.nodes[info.nds[count]].way = wayid;
            }
            
            this.statusCallback('Way uploaded successfully. ID=' + wayid);

			if(this.existingWaysToBeModified)
			{
				for(var count=0; count<this.existingWaysToBeModified.length;
					count++)
				{
					alert('Uploading way with ID : '
							+this.existingWaysToBeModified[count]);

        			this.ways[this.existingWaysToBeModified[count]].upload(
            			'http://www.free-map.org.uk/freemap/common/'+
						'osmproxy2.php?'+
            			'call=way&id='+this.existingWaysToBeModified[count],
						null,null,null);
				}
			}
        }
    },

	modifyExistingWay: function(wayIntersectInfo)
	{
		var newNds = new Array();
		for(var count2=0; count2<=wayIntersectInfo.ndidx; count2++)
			newNds.push( this.ways[wayIntersectInfo.osmid].nds[count2]);
		newNds.push(wayIntersectInfo.nodeid);
		for(var count2=wayIntersectInfo.ndidx+1; 
			count2<this.ways[wayIntersectInfo.osmid].nds.length;count2++)
		{
			newNds.push( this.ways[wayIntersectInfo.osmid].nds[count2]);
		}
		this.ways[wayIntersectInfo.osmid].nds = newNds;
	},

    // find the distance from a point to a line
     // based on theory at:
     // astronomy.swin.edu.au/~pbourke/geometry/pointline/

    distp: function(px,py,x1,y1,x2,y2) {
        var u = ((px-x1)*(x2-x1)+(py-y1)*(y2-y1)) / 
            (Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
        var xintersection = x1+u*(x2-x1), yintersection=y1+u*(y2-y1);
        return (u>=0&&u<=1) ? this.dist(px,py,xintersection,yintersection):
                   999999; 
    },
    
    dist: function(x1,y1,x2,y2) {
        var dx=x2-x1,dy=y2-y1;
        return Math.sqrt(dx*dx + dy*dy);
    },

	isPointNearWay: function(node)
	{
		var foundWays = null, thisWay, found, ll1, ll2, node1, node2,
			px1, px2;
		var ll = new OpenLayers.LonLat (node.geometry.x,node.geometry.y);
		var px = this.map.getViewPortPxFromLonLat(ll);
		for (var wayid in this.ways)
		{
			if(wayid>=1)
			{
				thisWay = new Array();
				found=false;
				for(var count=0; count<this.ways[wayid].nds.length-1; count++)
				{
					node1 = this.nodes[this.ways[wayid].nds[count]];
					node2 = this.nodes[this.ways[wayid].nds[count+1]];

					// convert node coords to pixels
					ll1 = new OpenLayers.LonLat 
						(node1.geometry.x,node1.geometry.y);
					ll2 = new OpenLayers.LonLat 
						(node2.geometry.x,node2.geometry.y);
					px1 = this.map.getViewPortPxFromLonLat(ll1);
					px2 = this.map.getViewPortPxFromLonLat(ll2);
					var distp=this.distp(px.x,px.y,px1.x,px1.y,px2.x,px2.y);
					if(distp<3)
					{
						thisWay.osmid = wayid;	
						thisWay.ndidx=count;
						thisWay.nodeid=node.osmid;
						found=true;
						break;
					}
				}
			}

			if(found)
			{
				if(!foundWays)
					foundWays = new Array();
				foundWays.push(thisWay);
			}
		}

		return foundWays;
	},

    CLASS_NAME: "OpenLayers.Layer.OSM"
});
