

function in_array(nodes,nodeid)
{
    for(var idx in nodes)
    {
        if(nodes[idx].osmid == nodeid)
            return true;
    }
    return false;
}

OpenLayers.Layer.OSMMarkers = OpenLayers.Class.create();
OpenLayers.Layer.OSMMarkers.prototype = 
    OpenLayers.Class.inherit (OpenLayers.Layer.Markers, {

    location: "http://www.free-map.org.uk/freemap/common/osmproxy2.php",
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
    doWays: true,
    basemap: null,
	features: null,

    initialize: function(name,options) {
        OpenLayers.Layer.Markers.prototype.initialize.apply(this,arguments);
        this.nodes = new Array();
        this.segments = new Array();
        this.ways = new Array();
		this.features = new Array();
        this.routeTypes = new RouteTypes();
    },

    setWays: function(w) {
        this.doWays=w;
    },

    load: function(bounds) {
        var bboxURL = this.location + "?bbox=" + bounds.toBBOX(); 
        statusMsg('Retrieving data...');
        OpenLayers.loadURL(bboxURL,null,this,this.parseData);
    },

    parseData: function(ajaxRequest) {
	    if(ajaxRequest.responseText.substring(0,5)=="ERROR")
		{
			var msg = "The nodes could not be retrieved. The server sent back "
					  + "an error code: " + xmlHTTP.responseText.substring(6);
			if(xmlHTTP.responseText.substring(6,9)=="500")
				msg += ". This is a temporary error, try moving the map again.";
			alert(msg);
		}
		else
		{
        var doc = ajaxRequest.responseXML;
        if(!doc || ajaxRequest.fileType!="XML") {
            doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
        }

        var n = doc.getElementsByTagName("node");


        // insert parsing from osmajax
        for(var count=0; count<n.length; count++)
        {
            var id = n[count].getAttribute("id");
            var lat = n[count].getAttribute("lat");
            var lon = n[count].getAttribute("lon");

            // Only do if the node doesn't exist already
            if (! this.nodes[id])
            {
                // NPE
                var osgb = cvtr.normToCustom(new OpenLayers.LonLat(lon,lat));
                // END NPE
        
                // Create point feature 
                var point = new OpenLayers.OSMNode();
                //NPE 
                point.geometry=new OpenLayers.Geometry.Point(osgb.lon,osgb.lat);
                //END NPE
                point.osmid = id;

                var tags = n[count].getElementsByTagName("tag");
                var tp0="";
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


                if (this.nodes[id].isPOI())
                {
					var data = new Array();
					data['popupContentHTML'] = 'testing';
        			data.icon =
            		new OpenLayers.Icon
                		("http://www.free-map.org.uk/images/smallnode.png",
                 		new OpenLayers.Size(10,10));
                    var feature = new OpenLayers.Feature.OSMMarker
                        (this.nodes[id],this,data);
                    feature.id=id;
                    this.features.push(feature);
                    var marker = feature.createMarker();
                    marker.events.register('click',feature,this.markerClick);
                    this.addMarker(marker);
                }
            }
        }
        statusMsg('Got all nodes.');
		}
    },
   
    markerClick: function(evt)
    {
        // bring up edit box
        var sameMarkerClicked = (this==selectedFeature);
 	    var selFeature = (!sameMarkerClicked)?this:null;
        if (!sameMarkerClicked)
            changeFeature(selFeature);
    },

    uploadNewNode: function (node)
    {
            var info1 = new Array();
            info1.node = node; 
            node.upload
            ('http://www.free-map.org.uk/freemap/common/osmproxy2.php?call=node&id=0',
                        this,this.newWayNodeHandler,info1);
    },

    newWayNodeHandler: function(xmlHTTP, info)
    {
        var nodeid = parseInt(xmlHTTP.responseText);

        // Blank original negative node index
        this.nodes[info.node.osmid] = null;
        info.node.osmid = nodeid;

        // Index the new node
        this.nodes[info.node.osmid] = info.node;

        // If we're in  the process of uploading a way, continue
        if(info.way)
        {
            if(--this.numNodesToBeUploaded==0)
            {
                statusMsg
                    ('All nodes were uploaded successfully. Doing segments');
                this.uploadWaySegments(info.way);
            }
        }
        // Otherwise it's just a node so create a feature out of it
        else
        {
            if(xmlHTTP.responseText.substring(0,5)!="ERROR")
            {
                var data = new Array();
                data.icon =
                    new OpenLayers.Icon
                        ("http://www.free-map.org.uk/images/smallnode.png",
                         new OpenLayers.Size(10,10));
                var f = new OpenLayers.Feature.OSMMarker(info.node,this,data);
                f.id=nodeid;
                this.features.push(f);
                var marker = f.createMarker();
                marker.events.register('click',f,this.markerClick);
                this.addMarker(marker);
                statusMsg('Point of interest added successfully. ID=' + nodeid);
            }
            else
            {
                alert("Error uploading node, code " + 
					xmlHTTP.responseText.substring(6));
            }
        }
    },

    CLASS_NAME: "OpenLayers.Layer.OSMMarkers"
});
