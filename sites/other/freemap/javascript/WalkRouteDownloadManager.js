// WalkRouteDownloadManager
// manages the download of walking routes, so that the walk route is
// added to the vector layer and the annotations to the walk route markers
// layer.

function WalkRouteDownloadManager (vectorLayer, markersLayer, 
										div, closeFunction)
{
	this.vectorLayer = vectorLayer;
	this.markersLayer = markersLayer;
	this.div=div;
	this.closeFunction = closeFunction;
	var self=this;


	this.getRouteById = function(id)
	{
		self.markersLayer.clearAnnotations();
		var url = 
			"http://www.free-map.org.uk/freemap/common/walkroute.php?"+
				"action=getById&format=xml&id="+id;
		OpenLayers.loadURL(url,null,this,this.ajaxCallback);

	}

	this.ajaxCallback = function(xmlHTTP)
	{
		self.vectorLayer.parseData(xmlHTTP);
		self.markersLayer.parseAnnotations(xmlHTTP);
		self.fillDivWithWalkRoute(xmlHTTP);
	}

	this.fillDivWithWalkRoute = function(xmlHTTP)
	{
		var title = xmlHTTP.responseXML.getElementsByTagName('title')[0].
			firstChild.nodeValue;
		var description = 
			xmlHTTP.responseXML.getElementsByTagName('description')[0].
			firstChild.nodeValue;
		var id = xmlHTTP.responseXML.getElementsByTagName('id')[0].
			firstChild.nodeValue;

		
		var html = "<div id='walkroute'>";
		html += "<h3>" + title + "</h3> <hr/> <p>" +
			description + "</p><hr/><ol>";

		var annotations = xmlHTTP.responseXML.getElementsByTagName
			('annotation');

		for (var count=0; count<annotations.length; count++)
			html += "<li>" + annotations[count].firstChild.nodeValue + "</li>";
			
		html += "</ol>";
		html += "<p><a href='/freemap/common/walkroute.php?action=getById&format=gpx&id="+id+"'>Get GPX File for your GPS</a></p>";
		html += "<input type='button' value='close' id='close'/></div>";

		self.div.innerHTML = html;
		$('close').onclick = self.closeFunction;
	}
}
