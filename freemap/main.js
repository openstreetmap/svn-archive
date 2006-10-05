var map;
var popup = null;
var mode = 0;
var markersLayer;
var osmmarkersLayer;

// For distance
var lastPos, thisPos, dragging=false, miles=true, dist=0;

var icons = { 'Viewpoint' : 'images/viewpoint.png', 'Path blockage/hazard' : 'images/caution.png', 'Pub' : 'images/pub.png' , 'Directions' : 'images/interest.png', 'Summit' : 'images/peak_small.png', 'Other point of interest' : 'images/interest.png' };

var lonLat;

var wrannotations, wrpoints, wra;
var curWRPopup;
var canvasLayer;

function init()
{
	map = new OpenLayers.Map('map');
	
					        
	/*
	var srtm = new OpenLayers.Layer.WMS( "SRTM", 
			"http://nick.dev.openstreetmap.org/srtm.php" );
	*/

	/*
	var landsat=new OpenLayers.Layer.WMS( 'landsat',
	'http://nick.dev.openstreetmap.org/ls.php');
	*/

	var osmdev = new OpenLayers.Layer.WMS( "OSM with SRTM Contours", 
		"http://nick.dev.openstreetmap.org/openlayers/freemap.php",
		{ 'layers' : 'areas,srtm,osm' }, { 'buffer' : 1 } );

	var osmlsat2=new OpenLayers.Layer.WMS
	( "OSM with Landsat satellite photos",
		"http://nick.dev.openstreetmap.org/openlayers/freemap.php",
	{ 'layers' : 'osm,landsat', 'styles' : 'osmeditor' } );

//		{ 'TRANSPARENT' : true });

	//map.addLayer(landsat);

	//map.addLayer(srtm);
	//map.addLayer(osmdev);


	document.getElementById("searchButton").onclick = placeSearch;
	document.getElementById("descButton").onclick = descSend; 

	/*
	document.getElementById("wrclear").onclick = clearWalkRoutes;
	document.getElementById("wrgo").onclick = walkRouteGo; 
	*/
	document.getElementById("gpsmap").onclick = gpsMapRedirect;

	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).onclick = function(e)
			{ var el = (e.target) ? e.target:e.srcElement;
			  setMode(parseInt(el.id.substring(4))); doBgCols(); }
		count++;
	}


	osmmarkersLayer = new OpenLayers.Layer.FreemapAnnotations('Freemap Markers',
		'http://nick.dev.openstreetmap.org/openlayers/markers3.php' );
	//map.addLayer(osmmarkersLayer);

	//canvasLayer = new OpenLayers.Layer.Canvas("walkroute canvas");
	//map.addLayer(canvasLayer);

	//map.addLayers ( [osmdev,osmlsat2,osmmarkersLayer,canvasLayer]);
	map.addLayers ( [osmdev,osmlsat2,osmmarkersLayer]);

	//canvasLayer.setStrokeColor("yellow");
	//canvasLayer.setStrokeWidth(2);

	// OpenLayers.ProxyHost = ''; needed for georss

	map.events.register('click',map,mapClick );

	map.events.remove('mousemove');
	map.events.remove('mouseup');
	map.events.remove('mousedown');

	map.events.register('mousedown',map,mouseDownHandler );
	map.events.register('mouseup',map,mouseUpHandler );
	map.events.register('mousemove',map,mouseMoveHandler );

	map.setCenter(new OpenLayers.LonLat(initLon,initLat),13);
	map.addControl (new OpenLayers.Control.LayerSwitcher());

	document.getElementById("units").onchange =  changeUnits;
	document.getElementById("resetDist").onclick =  resetDistance;

	doBgCols();

	wrannotations = new Array();
	wrpoints = new Array();
	wra = new Array();

}


function mapClick(e)
{
	lonLat = map.getLonLatFromViewPortPx(map.events.getMousePosition(e));

	switch(mode)
	{
		case 0:
			lookupPathReport(lonLat);
			break;

		case 1:
			addFeature(lonLat);
			break;

		case 4:
			pathReport(lonLat);
			break;
	}
	e.preventDefault();
	return false;
}

function addFeature(lonLat)
{
	
	document.getElementById('inputbox').style.visibility ='visible';
	/*
	document.getElementById('title').style.visibility ='visible';
	document.getElementById('description').style.visibility ='visible';
	document.getElementById('type').style.visibility ='visible';
	document.getElementById('link').style.visibility ='visible';
	document.getElementById('inputbox').style.left ='100px';
	document.getElementById('inputbox').style.top ='100px';
	document.getElementById('descButton').onclick = descSend;
	*/


	document.getElementById('description').focus();
}

function placeSearch()
{
	var loc = document.getElementById('search').value;
	ajax("http://nick.dev.openstreetmap.org/openlayers/geocoder_ajax.php", 
			"place="+loc+"&country=uk", searchCallback);
}

function descSend()
{
	var marker = new OpenLayers.Marker
	(lonLat,
		new OpenLayers.Icon(icons[document.getElementById('type').value]) );
		

	//markersLayer.addMarker(marker);

	var data =  { icon: 
								new OpenLayers.Icon
					('http://nick.dev.openstreetmap.org/openlayers/images/amenity.png'),
									popupContentHTML:
								'<h3>'+document.getElementById('title').value+
								'</h3><p>'
								+document.getElementById('description').value+
								'</p>',
									popupSize:
								new OpenLayers.Size(320,200)
								};

	var f = new OpenLayers.Feature.FreemapAnnotations 
							(osmmarkersLayer,lonLat,data);

	removePopup('inputbox');

	var priv = (document.getElementById('visibility')) ? 
					document.getElementById('visibility').value : 0;
	ajax("http://nick.dev.openstreetmap.org/openlayers/ajaxserver.php",
			"action=add&description="
				+document.getElementById('description').value+
			"&type="+document.getElementById('type').value+
			"&lat="+lonLat.lat+"&lon="+lonLat.lon+"&link="
			+document.getElementById('link').value+"&title="
			+document.getElementById('title').value+"&private="
			+priv, addCallback);
}

function pathReport(lonLat)
{
	/*
	document.getElementById('inputbox').style.visibility ='visible';
	document.getElementById('title').style.visibility ='hidden';
	document.getElementById('description').style.visibility ='visible';
	document.getElementById('type').style.visibility ='hidden';
	document.getElementById('link').style.visibility ='hidden';
	document.getElementById('inputbox').style.left ='100px';
	document.getElementById('inputbox').style.top ='100px';
	document.getElementById('descButton').onclick = pathReportSend;
	document.getElementById('description').focus();
	*/

	// seems to work. lat and long can be a right pain to work with
	var limit = 0.01; 

	var report = prompt('Please enter the report:');

	ajax("http://nick.dev.openstreetmap.org/openlayers/pathreport.php",
		 "action=add&lat="+lonLat.lat+"&lon="+lonLat.lon+
		 "&report="+report+"&limit="+limit, addCallback);
}

function lookupPathReport(lonLat)
{
	// seems to work. lat and long can be a right pain to work with
	var limit = 0.01; 

	ajax("http://nick.dev.openstreetmap.org/openlayers/pathreport.php",
		 "action=get&lat="+lonLat.lat+"&lon="+lonLat.lon+
		 "&limit="+limit, pathReportCallback);
}

function ajax(URL,data,callback,addData)
{
    var name, xmlHTTP;

	/*
	alert(URL);
	alert(data);
	*/

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

function searchCallback(xmlHTTP, addData)
{
	var latlon = xmlHTTP.responseText.split(",");
	if(latlon[0]!="0" && latlon[1]!="0")
	{
		map.setCenter(new OpenLayers.LonLat(parseFloat(latlon[1]),
								parseFloat(latlon[0])), map.getZoom() );
	}
	else
	{
		alert("That place is not in the database");
	}
}

function addCallback(xmlHTTP, addData)
{
	alert('added.');
	//alert(xmlHTTP.responseText);
}

function pathReportCallback(xmlHTTP, addData)
{
	document.getElementById('message').firstChild.nodeValue = 
		xmlHTTP.responseText;
}

function removePopup(elem)
{
	/*
	if(popup) 
	{ 
		popup.hide(); 
		popup.destroy(); 
		popup = null;		
	}
	*/
	document.getElementById(elem).style.visibility = 'hidden';
	map.events.register('click',map,mapClick );
}

function doBgCols()
{
	var count=0;
	while(document.getElementById("mode"+count))
	{
		document.getElementById("mode"+count).style.backgroundColor = 
			(count==mode) ? '#8080ff': '#000080';
		count++;
	}
}

// Input is grid references - 0.001km
// Returns : distance (km)
function distance(lastPos,pos)
{
	var miles = (document.getElementById("units").value=="miles");
    dist += (calcDistance(lastPos,pos) * (miles ? 0.6214 : 1));
    displayDistance(dist);
}

function calcDistance(pos1,pos2)
{
	return OpenLayers.Util.distVincenty (pos1,pos2);
}

function resetDistance()
{
	lastPos = null;
    dist = 0;
    displayDistance(0);
}

function displayDistance(dist)
{
    var intDist = Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
        displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
        unitsElem = document.getElementById("distUnits"),
        distTenthsElem = document.getElementById("distTenths");

    displayedIntDist += intDist;

    unitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 unitsElem.childNodes[0] );

    distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
}

function changeUnits()
{
	var miles = (document.getElementById("units").value=="miles");
    var factor = (miles) ?  0.6214: 1.6093;
    dist *=factor;
    displayDistance(dist);
}

function mouseMoveHandler(e)
{
    if(mode==3)
    {
        if (dragging)
        {
			
			thisPos=map.getLonLatFromViewPortPx(map.events.getMousePosition(e));
			if(lastPos)
			{
            	distance(lastPos,thisPos);
				//if(mode==5) canvasLayer.drawLine(lastPos,thisPos);
			}
            lastPos = thisPos;
			//if(mode==5) wrpoints[wrpoints.length]=thisPos;
        }
    }
	else
		map.controls[0].defaultMouseMove(e);

	e.preventDefault();
	return false;
}

function mouseUpHandler(e)
{
	if(mode==3)
	{
		dragging=false;
		return false;
	}
	else
		map.controls[0].defaultMouseUp(e);

	e.preventDefault();
	document.onselectstart = null; 
	return false;
}

function mouseDownHandler(e)
{

	if(mode==3)
	{
		dragging=true;
		e.preventDefault();
		//document.onselectstart = function () { return false; }
		//Event.stop(e);
		return false;
	}
	else if (mode==5 && !curWRPopup)
	{
			if (e.ctrlKey)
				walkRouteAnnotate(lonLat);
			else
			{
				thisPos=map.getLonLatFromViewPortPx
						(map.events.getMousePosition(e));
				if(lastPos)
				{
					distance(lastPos,thisPos);
					//canvasLayer.drawLine(lastPos,thisPos);
				}
				lastPos = thisPos;
				wrpoints[wrpoints.length]=thisPos;
			}
			e.preventDefault();
			return false;
	}
	else
		map.controls[0].defaultMouseDown(e);

}

function walkRouteAnnotate(lonLat)
{
	var i = wrannotations.length;

	curWRPopup =   new OpenLayers.Popup('wrp'+i,lonLat,
				new OpenLayers.Size(320,128),
			"<p><textarea id='wrt"+i+"'>Enter annotation</textarea></p>"+
			"<input type='button' value='ok' " +
			"onclick='walkRouteEnter("+lonLat.lon+","+lonLat.lat+")' />" ) ;

	curWRPopup.setBackgroundColor('#ffffc0');

	map.addPopup(curWRPopup);
}

function walkRouteEnter(lon,lat)
{
	var i = wrannotations.length;
	var a = new Array();
	a.text = document.getElementById("wrt"+i).value;
	a.lat = lat;
	a.lon = lon;
	wrannotations[wrannotations.length] = a;
	map.removePopup(curWRPopup);
	curWRPopup = null;
}

function setMode(m)
{
	mode=m;
	osmmarkersLayer.mode=m;
	lastPos = thisPos = null;
	if(mode==5)
	{
		//canvasLayer.setStrokeColor("yellow");
		//canvasLayer.setStrokeWidth(4);
	}

	document.getElementById('wrgo').disabled = (mode!=5);
	document.getElementById('wrclear').disabled = (mode!=5);
}

function gpsMapRedirect()
{
	var bounds = map.getExtent();
	window.location = 'gpsmap.php?bbox='+bounds.left+','+bounds.bottom
						+","+bounds.right+","+bounds.top;
}

/*
function walkRouteGo()
{
	var xml = "<walkroutes><walkroute>";
	xml += "<distance>" + dist + "</distance>";
	xml += "<difficulty> </difficulty>";
	for(var count=0; count<wrpoints.length; count++)
	{
		xml += "<point lat='"+wrpoints[count].lat +
				"' lon='"+wrpoints[count].lon + "' />";
	}

	for(var count=0; count<wrannotations.length; count++)
	{
		xml += "<annotation lat='"+wrannotations[count].lat +
				"' lon='"+wrannotations[count].lon + "' >";
		xml += "<text>"+wrannotations[count].text+"</text>";
		xml += "</annotation>";
	}
	xml += "</walkroute></walkroutes>";
	wrpoints = new Array();
	wrannotations = new Array();
	ajax("http://nick.dev.openstreetmap.org/openlayers/walkroute.php",
			"walkroute="+xml+"&action=add", addCallback);
}

function getWalkRoutes()
{
	ajax("http://nick.dev.openstreetmap.org/openlayers/walkroute.php", 
			"bbox="+w+","+s+","+e+","+n+"&action=getroutes", 
			walkRoutesCallback);
}

function getWalkRouteById(e)
{
	var el = (e.target) ? e.target: e.srcElement;
	ajax("http://nick.dev.openstreetmap.org/openlayers/walkroute.php", 
			"id="+el.id.substring[2]+"&action=getroute", walkRouteByIdCallback);
}

function walkRoutesCallback (xmlHTTP, addData)
{
	var walkroutes = xmlHTTP.getElementsByTagName("route");
	var wrDiv = document.getElementById("wrDiv");

	var span;

	while(wrDiv.hasChildNodes())
		wrDiv.removeChild(wrDiv.firstChild);

	for(var count=0; count<walkroutes.length; count++)
	{
		var distance = 
			walkroutes[count].getElementsByTagName("distance")[0].
				firstChild.nodeValue;

		var title = 
			walkroutes[count].getElementsByTagName("title")[0].
				firstChild.nodeValue;

		span = document.createElement("SPAN");
		span.id = "wr" + walkroutes[count].getElementsByTagName("id")[0].
						firstChild.nodeValue;

		span.appendChild(document.createTextNode (title + "("+distance+")m"));
		span.onclick = getWalkRouteById;
		wrDiv.appendChild(span);
		wrDiv.appendChild(document.createElement("BR"));
	}
}

function walkRouteByIdCallback (xmlHTTP, addData)
{

	var points = xmlHTTP.getElementsByTagName("point"),
		annotations = xmlHTTP.getElementsByTagName("annotation"),
		prevLL, curLL, text;

	data =  { icon: 
								new OpenLayers.Icon
					('http://nick.dev.openstreetmap.org/openlayers/images/amenity.png'),
									popupContentHTML: '',
									popupSize:
								new OpenLayers.Size(320,200)
								};

	for(var count=0; count<points.length; count++)
	{
		curLL = new OpenLayers.LonLat ( 
				points[count].getAttribute("lon"),
				points[count].getAttribute("lat")
										  );

		if(prevLL) {  
		//canvasLayer.drawLine (prevLL, curLL);
		}

		prevLL = curLL;

	}

	for(var count=0; count<annotations.length; count++)
	{
		text = 
			annotations[count].getElementsByTagName("text")[0].
				firstChild.nodeValue;

		
		curLL = new OpenLayers.LonLat ( 
				annotations[count].getAttribute("lon"),
				annotations[count].getAttribute("lat")
										  );
		data['popupContentHTML'] = "<p>"+text+"</p>";	

	 	wra[wra.length] = new OpenLayers.Feature.FreemapAnnotations 
				(markersLayer, curLL,	data);
	}
}

function clearWalkRoutes()
{
	for(var count=0; count<wra.length; count++)
		wra[count].destroy();
	wra = new Array();
	canvasLayer.clearCanvas();
}

*/
