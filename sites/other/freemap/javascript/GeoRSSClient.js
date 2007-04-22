// GeoRSSClient 
// A class designed to provide a generic markers interface for a GeoRSS layer
// Users can add or delete markers; also markers appear when the user scrolls
// the map.

// Constructor
//
// Parameters:
// map - the parent OpenLayers map
// addFeatureURL - the URL for adding a new feature to the GeoRSS layer 
//     (see below)
// georssURL - the URL of the GeoRSS feed - needs a bbox
// initLon, initLat - initial *standard* latitude and longitude
// initZoom - initial zoom level
// cvtrtype - if the base map is in a non-standard projection the 
//     converter object (see below) is used to convert between lat/long and
//     the custom coords of the custom projection so that the GeoRSS layer
//     will be superimposed correctly.

function GeoRSSClient(map,addFeatureURL,georssURL,
				initLon,initLat,initZoom,converter,link)
{
	var self = this;
	var obj =  this;

	this.cvtr = converter; 
	this.osmmarkersLayer = new OpenLayers.Layer.GeoRSS2('Markers',
						georssURL);
	this.addFeatureURL=addFeatureURL;
	this.map=map;
	this.map.georssclient=this;
	this.mode = 0;
	this.lastPos = null;
	this.thisPos = null;
	this.dragging = false;
	this.curPopup = null;
	this.featuretypes = new Array();
	this.mapclickpos = null;
	this.link = (link) ? true:false;

	// Adds a feature type to the select box for adding a new feature.
	// (corresponds to the featuretypetag in GeoRSS
	this.addFeatureType = function(t)
	{
		this.featuretypes.push(t);
	}

	// Adds an icon for a given feature type
	// Also adds the feature (see function above)
	this.addIcon = function(featuretype,url,size) 
	{
		this.osmmarkersLayer.addIcon(featuretype,url,size);
		this.addFeatureType(featuretype);
	}


	// For handling map clicks
	// mode 0 = normal. mode 1 = annotate. mode 2 = delete.
	this.mapClick = function(e) 
	{
		self.mapclickpos  = map.events.getMousePosition(e);

		switch(self.mode)
		{
			case 1:
				self.addFeature();
				break;
		}
		if(e.preventDefault)
			e.preventDefault();
		return false;
	}

	// Add a feature
	// Brings up a popup box allowing the user to add a feature.
	this.addFeature = function()
	{

		if (!this.curPopup)
		{
		var html = " <h3>Please enter details of the annotation</h3> "+
		"<label for='ptitle'>Title (e.g. name)</label>  <br/>" +
		"<input id='ptitle' class='textbox' /> <br/> "+
		"<label for='pdescription'>Description or comments</label>  <br/>"+
		"<textarea id='pdescription' class='textbox' ></textarea> <br/> "+
		"<label for='ptype'>Type</label>  <br/>";

		if(this.featuretypes.length> 0)
		{
			html += "<select id='ptype' class='textbox'> ";
			for(var count=0; count<this.featuretypes.length; count++)
			{
				html += "<option>"+this.featuretypes[count]+"</option>";
			}
			html += "</select> <br/> ";
		}
		else
		{
			html += "<input id='ptype' class='textbox' /> <br/>";
		}

		if(self.link)
		{
			html +=
		"<label for='plink'>Hyperlink, providing more info about the feature"+
		"</label> <br/>"+
		"<input id='plink' /> <br/>";
		}

		html += "<input type='button' id='button1' value='Go!' "+
		"<input type='button' value='Cancel' id='button2' /> ";
		
		this.curPopup =   new OpenLayers.Popup('wrp'+i,
				this.map.getLonLatFromViewPortPx(new OpenLayers.Pixel(50,50)),
				new OpenLayers.Size(480,360), html);
		this.curPopup.setBackgroundColor('#ffffc0');

		this.map.addPopup(this.curPopup);
		document.getElementById('button1').onclick = this.descSend;
		document.getElementById('button2').onclick = this.removePopup;
		}
	}

	// For AJAX calls
	// This should really be replaced by the standard OL AJAX API 
	this.ajax=function (URL,data,callback,addData)
	{
		//alert('ajax: URL=' + URL + ' data=' + data);

    	var name, xmlHTTP;

    	if(window.XMLHttpRequest)
    	{
        	xmlHTTP = new XMLHttpRequest();
        	// Opera doesn't like the overrideMimeType()
        	if(!window.opera)
            	xmlHTTP.overrideMimeType('text/xml');
    	}
    	else if(window.ActiveXObject)
        	xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    

    	xmlHTTP.open('POST',URL,true);
    	xmlHTTP.setRequestHeader('Content-Type',
    		'application/x-www-form-urlencoded');
    
    	xmlHTTP.onreadystatechange =     function()
    
    	{
        	if (xmlHTTP.readyState==4)
        	{
            	if(callback!=null)
            	{
                	callback (xmlHTTP, addData);
            	}
				else
				{
					//alert(xmlHTTP.responseText);
				}
			}
        }

   	    xmlHTTP.send(data); // param required even if nothing there!
    
	}

	// Send a new feature to the server.
	this.doSend = function (description,type,lat,lon,link,title,extra)
	{
		var qstring = "action=add&description="+description+
			"&type="+type+ "&lat="+lat+"&lon="+lon+
			"&title="+title+"&link="+link+
			(extra ? ("&"+extra) : "");
		this.ajax(this.addFeatureURL,qstring, this.addCallback); 
	}


		
	// Callback which runs when a feature has been successfully added.
	this.addCallback = function(xmlHTTP, addData)
	{
		alert('added. '+xmlHTTP.responseText);

		// Request markers in current area - this should make the new one appear
		var newBounds = self.cvtr.customToNormBounds ( self.map.getExtent() );
		self.osmmarkersLayer.load(newBounds);
	}

	// remove the 'add feature' popup
	this.removePopup = function()
	{
		self.map.removePopup(self.curPopup);
		self.curPopup = null;
		/*
		document.getElementById(elem).style.visibility = 'hidden';
		this.map.events.register('click',map,mapClick );
		*/
	}

	// Reads what the user entered in the popup box for adding a new feature
	// (see this.addFeature above) and calls doSend(), above 
	this.descSend = function()
	{
		//this.removePopup('inputbox');
		var markerIcon = self.osmmarkersLayer.getIcon
			(document.getElementById('ptype').value);

		self.mapclickpos.x  -= markerIcon.size.w/2;
		self.mapclickpos.y  -= markerIcon.size.h;

		lonLat = map.getLonLatFromViewPortPx(self.mapclickpos) ;

		var priv = (document.getElementById('visibility')) ? 
					document.getElementById('visibility').value : 0;
		var normLL = self.cvtr.customToNorm(lonLat);
		
		self.doSend(document.getElementById('pdescription').value,
					document.getElementById('ptype').value,
					normLL.lat,normLL.lon,
					(self.link) ?
					(document.getElementById('plink').value) : "",
					document.getElementById('ptitle').value,
					"private="+priv);	

		self.removePopup();
	}

	// For handling mouse up events
	// If mode is 0 (normal mode), load in new markers for the visible area
	this.mouseUpHandler = function(e)
	{
		if(self.mode==0)
		{
			var bounds = self.map.getExtent();

			var bl = self.cvtr.customToNorm
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
			var tr = self.cvtr.customToNorm
				(new OpenLayers.LonLat(bounds.right,bounds.top));
			var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
			self.osmmarkersLayer.load(newBounds);
		}
		self.map.controls[0].defaultMouseUp(e);

		if(e.preventDefault)
			e.preventDefault();
		document.onselectstart = null; 
		return false;
	}

	this.mouseMoveHandler = function(e)
	{
		if(self.mode==0)
			self.map.controls[0].defaultMouseMove(e);

		if(e.preventDefault)
			e.preventDefault();
		return false;
	}

	this.mouseDownHandler = function(e)
	{
		self.map.controls[0].defaultMouseDown(e);
		if(e.preventDefault)
			e.preventDefault();
		return false;
	}

	// Set the mode
	// note that:
	// 0 = normal mode (pan)
	// 1 = add annotations mode
	// 2 = delete annotations mode
	this.setMode = function(m)
	{
		self.mode=m;
		lastPos = thisPos = null;
	}

	this.getEventElement = function(e)
	{
		if(e.srcElement)
			return e.srcElement;
		return e.target;
	}

	// run from the GeoRSS2 layer to convert normal latitudes/longitudes
	// (from the GeoRSS) into the coordinate system used in the map
	this.converterProxy = function(lonlat)
	{
		var a= self.cvtr.normToCustom (lonlat);
		return a;
	}


	// Test whether we are in delete mode
	// Intended to be called from the GeoRSS2 layer 
	this.isDeleteMode = function()
	{
		return self.mode==2;
	}

	// Delete a marker
	// Set up from the GeoRSS2 layer
	this.deleteMarker = function(id)
	{
		//alert('deleting marker: ' + id);	
		self.ajax (self.addFeatureURL, "action=delete&guid="+id);
	}

	// Set the default icon
	this.setDefaultIcon = function (url,size) 
	{
		this.osmmarkersLayer.setDefaultIcon(url,size);
	}

	this.setupEvents = function()
	{
		obj.map.events.register('click',this.map,obj.mapClick );

		obj.map.events.remove('mousemove');
		obj.map.events.remove('mouseup');
		obj.map.events.remove('mousedown');

		obj.map.events.register('mousedown',this.map,obj.mouseDownHandler );
		obj.map.events.register('mouseup',this.map,obj.mouseUpHandler );
		obj.map.events.register('mousemove',this.map,obj.mouseMoveHandler );
	}

	this.osmmarkersLayer.setConverterFunction(this.converterProxy);
	this.osmmarkersLayer.setDeleteTestFunction(this.isDeleteMode);
	this.osmmarkersLayer.setDeleteFunction(this.deleteMarker);


	this.map.addLayer ( this.osmmarkersLayer );


	if(initZoom)
	{
		this.map.setCenter(this.cvtr.normToCustom
		(new OpenLayers.LonLat(initLon,initLat)), initZoom);
	}
	else
	{
		this.map.setCenter(this.cvtr.normToCustom
		(new OpenLayers.LonLat(initLon,initLat)));



	}

	this.osmmarkersLayer.load(this.cvtr.customToNormBounds(map.getExtent()));
}

