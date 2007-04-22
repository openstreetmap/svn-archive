
function FreemapClient(map,addFeatureURL,georssURL,
				initEasting,initNorthing,initZoom,cvtrtype,du,dt,u,rd,
				ggURL)
{
	this.cvtr = new converter(cvtrtype);
	var initLL = this.cvtr.customToNorm(new OpenLayers.LonLat
			(initEasting,initNorthing));
	this.parent = new GeoRSSClient
			(map,addFeatureURL,georssURL,
							initLL.lon,initLL.lat,initZoom,this.cvtr);

	var self = this;
	this.dragging=false;
	this.lastPos = null;
	this.dist = 0;
	this.distUnitsId = du;
	this.distTenthsId = dt;
	this.unitsId = u;
	this.ggLayer=null;

	if(ggURL!=null)
	{
		this.ggLayer = new OpenLayers.Layer.GGKML('Geograph Photo Markers',
						ggURL);
		this.ggLayer.setConverterFunction(this.parent.converterProxy);
		this.ggLayer.setDefaultIcon
		('http://www.free-map.org.uk/images/cam.png',
		 new OpenLayers.Size(16,16) );
		this.parent.map.addLayer ( this.ggLayer );
	}

	//this.ggLayer.load(this.parent.cvtr.customToNormBounds(map.getExtent()));

	this.displayDistance = function(dist)
	{
    	var intDist=Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
        displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
        unitsElem = document.getElementById(this.distUnitsId),
        distTenthsElem = document.getElementById(this.distTenthsId);

    	displayedIntDist += intDist;

    	unitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 unitsElem.childNodes[0] );

    	distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
	}

	this.calcDistance = function(pos1,pos2)
	{
		var d =  OpenLayers.Util.distVincenty (pos1,pos2);
		return d;
	}

	this.distance = function(p1,p2)
	{
		var miles = (document.getElementById(this.unitsId).value=="miles");
    	this.dist += (this.calcDistance(p1,p2) * (miles ? 0.6214 : 1));
    	this.displayDistance(this.dist);
	}


	this.resetDistance = function()
	{
		self.lastPos = null;
    	self.dist = 0;
    	self.displayDistance(0);
	}


	this.changeUnits = function()
	{
		var miles = (document.getElementById(self.unitsId).value=="miles");
    	var factor = (miles) ?  0.6214: 1.6093;
    	self.dist *=factor;
    	self.displayDistance(self.dist);
	}

	this.updateLinks = function(lon,lat)
	{
		document.getElementById('mapnikLink').href =
			'/freemap/index.php?mode=mapnik&lat='+lat+'&lon='+lon;
		document.getElementById('npeLink').href =
			'/freemap/index.php?mode=npe&lat='+lat+'&lon='+lon;
		document.getElementById('POIeditorLink').href =
			'/freemap/index.php?mode=POIeditor&lat='+lat+'&lon='+lon;
		document.getElementById('osmajaxLink').href =
			'/freemap/index.php?mode=osmajax&lat='+lat+'&lon='+lon;
	}

	this.mouseMoveHandler = function(e)
    {

        if (self.dragging && self.parent.mode==3)
        {
			
			thisPos=map.getLonLatFromViewPortPx(map.events.getMousePosition(e));
			if(self.lastPos)
			{
				var lastPosLL = self.cvtr.customToNorm(self.lastPos);
				var thisPosLL = self.cvtr.customToNorm(thisPos);
            	self.distance(lastPosLL,thisPosLL);
			}
            self.lastPos = thisPos;
			//if(mode==5) wrpoints[wrpoints.length]=thisPos;
        }
		return self.parent.mouseMoveHandler(e);
    }

	this.mouseDownHandler = function(e)
	{
		self.dragging=true;
		return self.parent.mouseDownHandler(e);
	}
	this.mouseUpHandler = function(e)
	{
		self.dragging=false;
		var bounds = self.parent.map.getExtent();
		var bl = self.cvtr.customToNorm
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
		var tr = self.cvtr.customToNorm
				(new OpenLayers.LonLat(bounds.right,bounds.top));

		/* GGKML */
		if(self.parent.mode==0)
		{

			var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
			if(self.ggLayer)
				self.ggLayer.load(newBounds);

		}
		self.updateLinks((bl.lon+tr.lon)/2,(bl.lat+tr.lat)/2);
		return self.parent.mouseUpHandler(e);
	}
	this.mapClick = function(e)
	{
		return self.parent.mapClick(e);
	}

	this.setupEvents = function()
	{
		this.parent.map.events.register('click',this.parent.map,this.mapClick );

		this.parent.map.events.remove('mousemove');
		this.parent.map.events.remove('mouseup');
		this.parent.map.events.remove('mousedown');

		this.parent.map.events.register('mousedown',this.parent.map,
				this.mouseDownHandler );
		this.parent.map.events.register('mouseup',this.parent.map,
			this.mouseUpHandler );
		this.parent.map.events.register('mousemove',this.parent.map,
			this.mouseMoveHandler );
	}

	this.searchCallback = function(xmlHTTP, addData)
	{
		var latlon = xmlHTTP.responseText.split(",");
		if(latlon[0]!="0" && latlon[1]!="0")
		{
			var normLL = new OpenLayers.LonLat
				(parseFloat(latlon[1]),parseFloat(latlon[0]));
			this.updateLinks(normLL.lon,normLL.lat);
			var prjLL = self.cvtr.normToCustom(normLL);
			map.setCenter(prjLL, self.parent.map.getZoom() );
		}
		else
		{
			alert("That place is not in the database");
		}
	}

	this.placeSearch = function()
	{
		var loc = document.getElementById('search').value;
		self.parent.ajax("http://www.free-map.org.uk/common/geocoder_ajax.php", 
			"place="+loc+"&country=uk", self.searchCallback);
	}

	document.getElementById(rd).onclick = this.resetDistance;
	document.getElementById(u).onchange = this.changeUnits;
	document.getElementById('searchButton').onclick = this.placeSearch;

}
