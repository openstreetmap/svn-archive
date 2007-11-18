

OpenLayers.Layer.Walkroute = OpenLayers.Class.create();
OpenLayers.Layer.Walkroute.prototype = 
	OpenLayers.Class.inherit (OpenLayers.Layer.Vector, {

	location: 
		"http://www.free-map.org.uk/freemap/common/walkroute.php?action=get",
	walkroutes : null,
	nextWalkrouteId: -1,
	renderedRoute: null,
	points:null,
	curID: 0,
	cvtr:null,

	initialize: function(name,cvtr,options)
	{
		this.cvtr=cvtr;
		var newArguments = new Array();
		newArguments.push(name);
		newArguments.push(options);
		OpenLayers.Layer.Vector.prototype.initialize.apply(this,newArguments);
		this.walkroutes = new Array();
	},

	parseData: function(ajaxRequest) {

		var doc = ajaxRequest.responseXML;
		if(!doc || ajaxRequest.fileType!="XML") {
			doc = OpenLayers.parseXMLString(ajaxRequest.responseText);
		}

		var id = doc.getElementsByTagName("id")[0].firstChild.nodeValue;
		var p = doc.getElementsByTagName("point");

		alert('Got ' + p.length + ' points. ');

		this.walkroutes[id] = new Array();
		this.walkroutes[id].points = new Array();

		// new OpenLayers.feature(start point)	


		// insert parsing from osmajax
		for(var count=0; count<p.length; count++)
		{
			var lat = p[count].getAttribute("lat");
			var lon = p[count].getAttribute("lon");

			var ll = this.cvtr.normToCustom(new OpenLayers.LonLat(lon,lat));
			// Create point feature 
			var point = new OpenLayers.Geometry.Point(ll.lon,ll.lat);
			this.walkroutes[id].points.push(point);
		}

		// Remove any walkroute already on the layer
		this.removeRenderedRoute();

		// Full routes become OpenLayers LineStrings. 

		// Create a polyline object

		var colour = "yellow"; 
		var width = 5; 

		var lineString = new OpenLayers.Geometry.LineString();

		var style = { fillColor: colour, fillOpacity: 0.4,
							strokeColor: colour, strokeOpacity: 0.4,
							strokeWidth: (width===false ? 1:width) };

		//var curWay = new OpenLayers.WalkRoute();
		//curWay.geometry = wayGeom; 

		var p = this.walkroutes[id].points;

		for(var count=0; count<p.length; count++)
		{
			lineString.addComponent (  p[count] );
		}

		this.renderedRoute=new OpenLayers.Feature.Vector(lineString,null,style);
		this.addFeatures(this.renderedRoute);
	},

	removeRenderedRoute: function()
	{
		if(this.renderedRoute!==null)
		{
			this.removeFeatures(this.renderedRoute);
			this.renderedRoute=null;
		}
	},

	drawRoute: function(points)
	{
		var lineString = new OpenLayers.Geometry.LineString();

		var style = { fillColor: 'red', fillOpacity: 0.4,
							strokeColor: 'red', strokeOpacity: 0.4,
							strokeWidth: 5 };

		for(var count=0; count<points.length; count++)
		{
			lineString.addComponent (  points[count] );
		}

		this.renderedRoute=new OpenLayers.Feature.Vector(lineString,null,style);
		this.addFeatures(this.renderedRoute);
	},


	CLASS_NAME: "OpenLayers.Layer.Walkroute"
});
