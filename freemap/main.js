/*
    Copyright (C) 2004-05 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the Lesser GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Lesser GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

*/

var lat=51.05, lon=-0.72, zoom=12, view=0,
	tileURL, tile_engine; 

function init(lt,ln,zm,t)
{

	lat=lt;
	lon=ln;
	zoom=zm;
	view=t;
	document.getElementById('view').selectedIndex = view;

	tileURL = 'http://nick.dev.openstreetmap.org/drawmap.php';
  tile_engine = new tile_engine_new("drag",'FULL','',tileURL,lon,lat,zoom,800,
  						640);
//  initTileEngine("drag"); 
  document.getElementById("magnify").onclick = zoomIn; 
  document.getElementById("shrink").onclick =  zoomOut; 
	
	document.getElementById("btnLL").onclick = readPosition;

  fmapinit();
}

function zoomIn()
{
	tile_engine.tile_engine_zoomin();
}

function zoomOut()
{
	tile_engine.tile_engine_zoomout();
}

function enableTileEngine()
{
	tile_engine.event_catch();
	//tile_engine.parent.onmousemove = msmv;
}

function msmv(e)
{
/*
	var bounds = tile_engine.getTileBounds();

	// handle tile engine drag
	tile_engine.event_mouse_move(e);

	// work out new exposed area if any
	var bounds2 = tile_engine.getTileBounds(), bounds3 = new Array();
	bounds3.e = bounds3.w = bounds3.n = bounds3.s = null;

	var needUpdate = false;

	if(bounds2.w < bounds.w)
	{
		bounds3.w = bounds2.w;
		bounds3.e = bounds.w;
		bounds
		needUpdate = true;
	}
	else if (bounds2.e > bounds.e)
	{
		bounds3.e = bounds2.e;
		bounds3.w = bounds.e;
		needUpdate = true;
	}	

	if(bounds2.s < bounds.s)
	{
		bounds3.s = bounds2.s;
		bounds3.n = bounds.s;
		needUpdate = true;
	}

	else if (bounds2.n > bounds.n)
	{
		bounds3.n = bounds2.n;
		bounds3.s = bounds.n;
		needUpdate = true;
	}

	if(needUpdate)
	{
		var boundsVis = tile_engine.getVisibleBounds();

		if(bounds3.w = null) bounds3.w = boundsVis.w;
		if(bounds3.e = null) bounds3.e = boundsVis.e;
		if(bounds3.s = null) bounds3.s = boundsVis.s;
		if(bounds3.n = null) bounds3.n = boundsVis.n;
	}
	*/
}

function enableFreemapStuff()
{
    // registering an event on the viewport will mean that the event is
    // received by both the map itself and the 'X marks the spot'
    tile_engine.parent.onmousedown = mouseDownHandler;
    tile_engine.parent.onmousemove = mouseMoveHandler;
    tile_engine.parent.onmouseup = mouseUpHandler;
	tile_engine.parent.style.cursor = 'default';
}

function setPosition(newlat,newlon)
{
	tile_engine.setLatLon(newlat,newlon);
}
 

function readPosition()
{
	var txtLat = document.getElementById("txtLat"),
		txtLon = document.getElementById("txtLon");

	setPosition(txtLat.value,txtLon.value);
}

////////////////////////////////////////////////////////////////////////////////

// freemap stuff


var mapWidth = 800, mapHeight = 640;
var mapPos;
var routeString="", drag2 = -1, mapID=0;
var action="distance", polygontype=1 ;
var dist = 0,  miles = true, lastPos;
var featureName="",featureDesc="";
var timer=null;
var ct=0;
var popupTypes = new Array("hamlet","village","small town", "large town",
                            "hill","railway station","viewpoint","pub",
                            "church","campsite","amenity","point of interest",
                            "tea shop", "restaurant","caution");
var wikipediaTypes = new Array("hamlet","village","small town", "large town",
                            "hill","point of interest");
var segColours;

var savedX, savedY;

var ways;

var osmVer = "0.3";

var curNodeIndex = 0;

function fmapinit()
{
    var popup=document.createElement("DIV");
    popup.id = "popup";
    var h3  = document.createElement("H3");
    h3.appendChild(document.createTextNode(""));
    popup.appendChild(h3);
    var p = document.createElement("P");
    p.appendChild(document.createTextNode(""));
    popup.appendChild(p);
    document.body.appendChild(popup);
	document.getElementById("action").onchange = setOption;
	document.getElementById("view").onchange = setOption;
    document.getElementById("featurego").onclick = dlgOkPressed;

	document.getElementById("reload").onclick = reload; 
	document.getElementById("uploadway").onclick = uploadWay;

	segColours = new Array();
	segColours["footpath"] = "green";
	segColours["bridleway"] = "brown";
	segColours["byway"] = "red";
	segColours["residential road"] = "black";
	segColours["minor road"] = "black";
	segColours["B road"] = "black";
	segColours["A road"] = "black";
	segColours["motorway"] = "blue";

	ways = new Array ();
	ways[0] = new Way (new Array(), new Array(), 
						document.getElementById('segtype').value);

}


function setOption(e)
{
    if(!e) e=window.event;
    var eventObject = getEventObject(e);
	var prevAction = action;
    eval(eventObject.id+'="'+eventObject.value+'"');
    if(eventObject.id=="view") 
	{
		tile_engine.setAdditional("&tp="+view);
		tile_engine.forceRefresh();
	}
	else if(eventObject.id=="action")
	{
		if(eventObject.value=="drag")
			enableTileEngine();
		else
			enableFreemapStuff();

		if(prevAction=="draw" && eventObject.value!="draw")
			finishDrawing();
		else if (eventObject.value=="draw")
			startDrawing();

		prevAction = eventObject.value;
	}	
		
}

function Coord(x,y)
{
    this.x=x;
    this.y=y;

    this.toString = function()
        { return "x = " + this.x + " y = " + this.y; }
}

function Node(lat,lon,id)
{
    this.lat=lat;
    this.lon=lon;
	this.id=id;
}

function Segment(id)
{
	this.id=id;
}

function Way(nodes,segs,type)
{
	this.nodes=nodes;
	this.segs=segs;
	this.type=type;
}

// get the position of an element on the page
// technique from quirksmode.org
// http://www.quirksmode.org/js/findpos.html
function getPosition(id)
{
    var elem = document.getElementById(id);
    var pos = new Coord(0,0);

    while(elem)
    {
        pos.x += elem.offsetLeft;
        pos.y += elem.offsetTop;

        elem = elem.offsetParent;
    }
    return pos;
}

//get position of a mouse click
//e is the event object
//based on theory from:
//http://evolt.org/article/Mission_Impossible_mouse_position/17/23335/index.html
//This will now work in current versions of:
//- Firefox/Mozilla, IE, Konqueror (3.3.2), Opera (8.02)


function getMousePos(e)
{
    var pos = new Coord(e.clientX,e.clientY);

    
    pos.x += document.body.scrollLeft;
    pos.y += document.body.scrollTop;
    return pos;
}

// gets the x,y coordinate relative to the map
function getMapPos(id,e)
{
    var mapPos = getPosition(id);
    var p = getMousePos(e);
    return new Coord(p.x-mapPos.x,p.y-mapPos.y);
}

function mouseDownHandler(e)
{
    if(!e) e=window.event;
    if(timer) 
    {
        clearTimeout(timer);
        timer = null;
    }
    routeString="";
	
    var p=getMapPos('drag',e);

	if(action=="distance")
	{
		var ll = new LatLon ( tile_engine.yToLat(p.y),
							  tile_engine.xToLon(p.x) );
    	lastPos = ll_to_gr(ll);
	}

    drag2 = 0; 

    // Deal with default action prevention for dragging. The default action
    // appears to occur at the time of the mouse down event for Mozilla and
    // the time of the mouse drag for IE. So put default prevention in 
    // both. Use the W3C preventDefault and also "teturn false" (the old DOM
    // approach); thankfully this works in IE preventing us having to use
    // any of Bill's proprietary stuff :-)
    if(e.preventDefault)
    {
        e.preventDefault(); 
    }
    return false;
}

function mouseMoveHandler(e)
{
    if(!e) e=window.event;
    var p=getMapPos("drag",e);

    if(!drag2)
    {
        if (action=="distance")
        {
			var ll = new LatLon ( tile_engine.yToLat(p.y),
							  	  tile_engine.xToLon(p.x) );
    	 	var thisPos = ll_to_gr(ll);
            distance(lastPos,thisPos);
            lastPos = thisPos;
        }
    }
    else
    {
        if(timer) clearTimeout(timer);
        timer = setTimeout('showPopup('+p.x+','+p.y+')',500);
    }

    return false;
}

function showPopup(x,y)
{
    var popup=document.getElementById("popup");
    popup.style.visibility='hidden';
    var addData = new Array();
    addData["isPopup"] = true;
    addData["popupX"] = (17+x)+"px";
    addData["popupY"] = (17+y)+"px";

    timer=null;
    var data=formPostData();
	var lat = tile_engine.yToLat(y);
	var lon = tile_engine.xToLon(x);
    data+="&points="+lon+","+lat;
    ajax2(data,"featurequery",displayFeatureLookup, addData);
}

function mouseUpHandler(e)
{
    if(!e) e=window.event;
    drag2=-1;
    if(action=="feature" || action=="featuredel" || action=="featureupdate"
            || action=="featurequery")
    {
        var p=getMapPos("drag",e), addData;
		var lat = tile_engine.yToLat(y);
		var lon = tile_engine.xToLon(x);
        routeString += lon;
        routeString += ",";
        routeString += lat;
        var data=formPostData(); 
        switch(action)
        {
            case "featuredel":
                data += "&points="+routeString;
                ajax2(data,"featuredel",null,null);
            case "featurequery":
                data += "&points="+routeString;
                addData = new Array();
                addData["isPopup"] = false;

                ajax2(data,"featurequery",displayFeatureLookup,addData);
                break;
            case "featureupdate":
                data += "&points="+routeString;
                ajax2(data,"featurequery",fillDlgWithFeatureInfo,null);
                break;

            case "feature":
                showDlgBox("","");
                break;
        }
    }    
}

function showDlgBox(name,description)
{
    var a = routeString.split(",");
    var dlg = document.getElementById("promptbox");
    dlg.style.left = (17+parseInt(a[0]))+"px";
    dlg.style.top = (17+parseInt(a[1]))+"px";
    dlg.style.visibility='visible';
    document.getElementById('featurename').value = name;
    document.getElementById('featuredesc').value = description;
}

function dlgOkPressed()
{
    document.getElementById('promptbox').style.visibility='hidden';
    
    featureName = document.getElementById('featurename').value;
    featureDesc = document.getElementById('featuredesc').value;
    var data = formPostData();
    data += "&name="+featureName+"&description="+featureDesc;
    data += "&points="+routeString;
    if(action=="feature")
    {
//        var type = document.getElementById("featuretype").value;
		var type="caution";
        data += "&type="+type;
    }
    ajax2(data,action,null,null);
}

function srch()
{
    data =  formPostData();
    data += "&name="+document.getElementById("name").value +
                "&gridref="+document.getElementById("gridref").value;
    ajax2(data,"search",displaySearchResults, null);
}

// forms the common part of every XMLHTTPRequest
function formPostData ()
{
	/*
    var data="e="+easting+
                "&n="+northing+"&scale="+zoom;
				*/
	var data="blah=1"
    return data;
}

function ajax2(data,ajaxAction,callback,additionalData)
{
	
	/*
	alert('data='+data+' ajaxAction='+ajaxAction+
			' addData='+additionalData);
			*/
	
    var name, xmlHTTP;

    data += "&action="+ajaxAction;

    if(window.XMLHttpRequest)
    {
        xmlHTTP = new XMLHttpRequest();
        // Opera doesn't like the overrideMimeType()
        if(!window.opera)
            xmlHTTP.overrideMimeType('text/xml');
    }
    else if(window.ActiveXObject)
        xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    
    


    var URL = 'http://www.free-map.org.uk/osm/ajaxserver3.php';

    xmlHTTP.open('POST',URL,true);
    xmlHTTP.setRequestHeader('Content-Type',
    'application/x-www-form-urlencoded');
    
    xmlHTTP.onreadystatechange =     function()
    
    {
        if (xmlHTTP.readyState==4)
        {
			//alert(xmlHTTP.responseText);
            if(callback!=null)
            {
                callback (xmlHTTP, additionalData);
            }
            else
            {
                alert(xmlHTTP.responseText);
                reloadMap();
            }
        }
    }

    xmlHTTP.send(data); // param required even if nothing there!
    
}


function displayFeatureLookup(xmlHTTP, addData)
{
    if(xmlHTTP.status != 404)
    {
        var regexp = new RegExp("^name=(.*);desc=(.*);type=(.*)$");
        var arr = regexp.exec(xmlHTTP.responseText);
    
        var incl = (!addData["isPopup"] || (arr[2]!='' && popupType(arr[3])));

        if(incl)
        {
            if(addData["isPopup"])
            {
                var popup=document.getElementById("popup");
                document.getElementById("popup").style.visibility='visible';
                popup.style.left = addData["popupX"]; 
                popup.style.top = addData["popupY"]; 
            }

            var bottomarea=document.getElementById((addData["isPopup"])?
                    "popup":"bottomarea");
            //queryPageStruct(bottomarea);
            // h3 and p will be the first 2 child nodes as long as 
            // there is no new line between them in the document -
                // otherwise the ENTER character is treated as a text node
            var h3 = bottomarea.childNodes[0],
                p = bottomarea.childNodes[1];
            var text = document.createTextNode(xmlHTTP.responseText);

            removeExcess(bottomarea);
    
            // blank all stuff in the p element
            while(p.hasChildNodes())
                p.removeChild(p.firstChild);


            var regexp = new RegExp("^name=(.*);desc=(.*);type=(.*)$");
            var arr = regexp.exec(xmlHTTP.responseText);
            h3.firstChild.nodeValue = arr[1];
            p.appendChild(document.createTextNode(arr[2]));


            if(wikipediaType(arr[3]))
            {
                var p2=document.createElement("P");
                bottomarea.appendChild(p2);
                var a = document.createElement("A");
                a.setAttribute("class","wikipediaLink");
                a.href="http://www.wikipedia.org/wiki/"+arr[1];

                a.appendChild
                    (document.createTextNode("Look up on Wikipedia..."));

            //    p.appendChild(document.createElement("BR"));
                p2.appendChild(a);
            }
        }
    }
}

function displaySearchResults(xmlHTTP, addData)
{
    if(xmlHTTP.status == 404)
    {
        alert('Not found');
    }
    else
    {
        if(xmlHTTP.responseText.substr(0,8)=="<places>")
        {
            var placesNode = xmlHTTP.responseXML.firstChild;

            var allnodes = placesNode.childNodes;
        
            var places = placesNode.getElementsByTagName ("place");
            if(places.length==1)
            {
                var coords = placesNode.getElementsByTagName
                            ("place")[0].getElementsByTagName
                            ("coords")[0].firstChild.nodeValue;
                var co = coords.split(",");
				/*
                easting = parseInt(co[0])-getGRunits(mapWidth/2);
                northing = parseInt(co[1])-getGRunits(mapHeight/2);
				*/
                loadMap();
            }
            else
            {
                var count;
                var bottomarea=document.getElementById("bottomarea");
                var h3 = bottomarea.childNodes[0],
                    p = bottomarea.childNodes[1];
                var a, linkText, br;
                h3.firstChild.nodeValue = "Multiple hits found...";
            
                removeExcess(bottomarea);

                // blank all stuff in the p element
                while(p.hasChildNodes())
                    p.removeChild(p.firstChild);

                var curPlace, curName, curType, curCoords,
                    places = placesNode.getElementsByTagName ("place");

                for(count=0; count<places.length; count++)
                {
                    curName = places[count].getElementsByTagName
                            ("name")[0].firstChild.nodeValue;
                    curType = places[count].getElementsByTagName
                            ("type")[0].firstChild.nodeValue;
                    curCoords = places[count].getElementsByTagName
                            ("coords")[0].firstChild.nodeValue;
            
                    a = document.createElement("SPAN"); 
                    a.setAttribute("class", "hitlink");
                    linkText = document.createTextNode 
                        (curName + "(" + curType + ")" );
                    a.appendChild(linkText);
                
                    a.coords = curCoords;
            
                    p.appendChild(a);
                    br = document.createElement("BR");
                    p.appendChild(br);
                }
            }
        }
        else
        {
            var regexp = new RegExp("^e=([0-9]+);n=([0-9]+)$");
            var ar = regexp.exec(xmlHTTP.responseText);
            //easting = parseInt(ar[1]);
            //northing=parseInt(ar[2]);
            loadMap();
        }
    }
}

function fillDlgWithFeatureInfo(xmlHTTP, addData)
{
    var regexp = new RegExp("^name=(.*);desc=(.*);type=(.*)$");
    var arr = regexp.exec(xmlHTTP.responseText);
    showDlgBox(arr[1],arr[2]);
}

function setMode(m)
{
    mode=m;
    loadMap();
}


// Input is grid references - 0.001km
// Returns : distance (km)
function distance(lastPos,pos)
{
    dist += (calcDistance(lastPos,pos) * (miles ? 0.6214 : 1));
    displayDistance(dist);
}

// Input is grid references - 0.001km
function calcDistance(pos1,pos2)
{
    return (Math.sqrt (
				Math.pow((pos2.x-pos1.x)/1000,2) + 
				Math.pow((pos2.y-pos1.y)/1000,2)
					  )
			) ; 
}

function resetDistance()
{
    dist = 0;
    displayDistance(0);
}

function displayDistance(dist)
{
    var intDist = Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
        displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
        distUnitsElem = document.getElementById("distUnits"),
        distTenthsElem = document.getElementById("distTenths");

    displayedIntDist += intDist;

    distUnitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 distUnitsElem.childNodes[0] );

    distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
}

function changeUnits()
{
    var factor = (miles) ? 1.6093 : 0.6214;
    dist *=factor;
    miles = !miles;
    var distchangeText = (miles) ? "Use km" : "Use mi";
    var distchange = document.getElementById("distchange");

    distchange.replaceChild (document.createTextNode(distchangeText),
                                distchange.childNodes[0]);

    var unitsText = (miles) ? "miles":"km";
    var units = document.getElementById("units");

    units.replaceChild (document.createTextNode(unitsText),units.childNodes[0]);

    displayDistance(dist);
}


function getEventObject(e)
{
    return (e.target) ? e.target : e.srcElement;
}

function removeExcess(bottomarea)
{
    while (bottomarea.childNodes.length>2)
        bottomarea.removeChild(bottomarea.childNodes[2]);
}

function popupType(type)
{
    var count;
    for(count=0; count<popupTypes.length; count++)
    {
        if(type==popupTypes[count])
            return true;
    }
    return false;
}

function wikipediaType(type)
{

    var count;
    for(count=0; count<wikipediaTypes.length; count++)
    {
        if(type==wikipediaTypes[count])
            return true;
    }
    return false;
}


function loadMap()
{
}

function reloadMap()
{
}


function canvasDraw(e)
{
    var p=getMapPos('canvas1',e);
  	var canvas1 = document.getElementById("canvas1");
	var ctx1 = canvas1.getContext('2d');
	var prevX, prevY, curX, curY;


	var nodes = ways[ways.length-1].nodes;
	var segs = ways[ways.length-1].segs;

	if(nodes.length)
	{
		prevX = tile_engine.lonToX(nodes[nodes.length-1].lon);
		prevY = tile_engine.latToY(nodes[nodes.length-1].lat);
		ctx1.strokeStyle = segColours
					[document.getElementById("segtype").value];
		ctx1.moveTo(prevX,prevY);
		ctx1.lineTo(p.x,p.y);
		segs[segs.length] = new Segment(0);
		ctx1.stroke();
		ctx1.fillRect(p.x-2,p.y-2,5,5);
		addNode(p);

	}
	else
	{
		ctx1.moveTo(p.x,p.y);
		ctx1.fillRect(p.x-2,p.y-2,5,5);
		addNode(p);
	}
	ctx1.stroke();
}


function startDrawing()
{
	// enable canvas
	document.getElementById("canvas1").style.visibility="visible";
	document.getElementById("canvas1").onclick = canvasDraw;
	var ctx=document.getElementById("canvas1").getContext("2d");
	ctx.clearRect(0,0,800,640);
	drawNodes(ctx);
	ctx.stroke();
	document.getElementById("newline").style.visibility="visible";
}

function finishDrawing()
{
	// disable canvas
	document.getElementById("canvas1").style.visibility="hidden";
	document.getElementById("newline").style.visibility="hidden";

	//tile_engine.forceRefresh();
}

function addNode(p)
{
	var foundNode;
	var nodes = ways[ways.length-1].nodes;

	// If we're on an existing node, copy over the existing node and
	// add the segment based on the id of the existing node i.e. do not
	// make an ajax call to add the new node
	if((foundNode=findNode(p.x,p.y)) != null)
	{
		alert('found an existing node');
		nodes[curNodeIndex++] = foundNode;
		addSegment(nodes[curNodeIndex-2].id,nodes[curNodeIndex-1].id,
						document.getElementById("segtype").value);
	}
	else
	{
		var lat = tile_engine.yToLat(p.y), lon = tile_engine.xToLon(p.x);
		var osm = nodeToOSM (lat,lon,osmVer);
		nodes[curNodeIndex] = new Node (lat,lon, 0);
		ajax2("osmapicall=node/0&ver=0.3&osm="+osm,"osmupload",osmCallback,"node");
	}
}

function addSegment(id1,id2,type)
{
	var osm = segToOSM(id1,id2,type,osmVer);
	ajax2("osmapicall=segment/0&ver=0.3&osm="+osm,"osmupload",osmCallback,"segment");
}

function nodeToOSM(lat,lon,version)
{
	return "<osm version='0.3' generator='Freemap AJAX'><node id='0' lat='"+lat+"' lon='"+lon+"' /></osm>";
}

function segToOSM(id1,id2,type,version)
{
	return "<osm version='0.3' generator='Freemap AJAX'><segment id='0' from='"+id1+"' to='"+id2+ "' /></osm>";
}

function reload()
{
	tile_engine.forceRefresh();
}

function uploadWay()
{
	var wayName = prompt('Enter the name of the way:');
	ways[ways.length-1].type = document.getElementById("segtype").value;
	var osm=wayToOSM(ways[ways.length-1],wayName);
	ajax2("osmapicall=way/0&ver=0.3&osm="+osm,"osmupload",osmCallback,"way");
}
					
function wayToOSM(way, wayName)
{
	var segArray = way.segs;
	var type = way.type;

	var osm = "<osm version='0.3' generator='Freemap AJAX'>";
	osm += '<way id="0">';

	for(var count=0; count<segArray.length; count++)
	{
		osm += '<seg id="' + segArray[count].id + '"/>';	
	}

	osm += '<tag k="name" v="' + wayName + '"/>';
	var tags = getTags(type);
	for (i in tags)
	{
		if(i=='theClass') 
			osm += '<tag k="class" v="' + tags['theClass'] + '"/>';
		else
			osm += '<tag k="' + i + '" v="' + tags[i] + '"/>';
	}

	osm += "</way></osm>";

	return osm;
}

function getTags(type)
{
	var tags = new Array();

	switch(type)
	{
		case "footpath":
			tags.foot='yes';
			tags.theClass='path';
			break;

		case "bridleway":
			tags.foot=tags.horse=tags.bike='yes';
			tags.theClass='path';
			break;

		
		case "byway":
			tags.foot=tags.horse=tags.bike=tags.car='yes';
			tags.theClass='unsurfaced';
			break;

		case "residential road":
			tags.foot=tags.horse=tags.bike=tags.car='yes';
			tags.theClass='residential';
			break;

		case "minor road":
			tags.foot=tags.horse=tags.bike=tags.car='yes';
			tags.theClass='minor';
			break;

		case "B road":
			tags.foot=tags.horse=tags.bike=tags.car='yes';
			tags.theClass='secondary';
			break;

		case "A road":
			tags.foot=tags.horse=tags.bike=tags.car='yes';
			tags.theClass='primary';
			break;

		case "motorway":
			tags.car='yes';
			tags.theClass='motorway';
			break;
	}

	return tags;
}

function osmCallback(xmlHTTP, addData)
{
	var nodes = ways[ways.length-1].nodes;
	var segs = ways[ways.length-1].segs;

    if(xmlHTTP.status != 404 && xmlHTTP.status != 401)
	{
		//alert("osmCallback: responseText:" + xmlHTTP.responseText);

		// If we have just added a node, we then go ahead and add a segment
		// between the just-added node and the last one.
		if(parseInt(xmlHTTP.responseText) != 0)
		{
			if(addData=="node") 
			{
				var nid = xmlHTTP.responseText;
				nodes[curNodeIndex].id = nid;
				alert("Node added successfully. ID=" + nid);
				if(curNodeIndex++>=1)
				{
					addSegment(nodes[curNodeIndex-2].id,
								nodes[curNodeIndex-1].id,
								document.getElementById("segtype").value);
				}
			}
			else if (addData=="segment")
			{
				alert("Segment added successfully. ID="+xmlHTTP.responseText);
				segs[segs.length-1].id = xmlHTTP.responseText;
			}
			else if (addData=="way")
			{
				alert("Way added successfully. ID="+xmlHTTP.responseText);
				ways[ways.length] = new Way(new Array(),new Array(),
					document.getElementById('segtype').value);
				curNodeIndex=0;
			}
		}
		else
		{	
			alert('could not add: returned: ' + xmlHTTP.responseText);

			// If we tried to add a segment but failed, reduce the node
			// index by one; this will mean that we can try adding the 
			// segment again by clicking the last node again. 
			if(addData=="segment")
			{
				curNodeIndex--;
			}
		}
	}
	else
	{
		alert('HTTP status: ' + xmlHTTP.status);
		if(addData=="way")
		{
			ways[ways.length] = new Way(new Array(),new Array(),
					document.getElementById('segtype').value);
			curNodeIndex=0;
		}
	}
}

function drawNodes(ctx1)
{
	var nodes, segs;
	for(var w=0; w<ways.length; w++)
	{
		nodes = ways[w].nodes;
		segs = ways[w].segs;

		if(nodes.length)
		{
			var x, y, xp, yp;
			x = tile_engine.lonToX(nodes[0].lon);
			y = tile_engine.latToY(nodes[0].lat);
			ctx1.fillRect(x-2,y-2,5,5);

			for(var count=1; count<(w==ways.length-1 ? curNodeIndex:
						nodes.length); count++)
			{
				xp = tile_engine.lonToX(nodes[count-1].lon);
				yp = tile_engine.latToY(nodes[count-1].lat);
				x = tile_engine.lonToX(nodes[count].lon);
				y = tile_engine.latToY(nodes[count].lat);
				ctx1.strokeStyle = segColours[ways[w].type];
				ctx1.moveTo(xp,yp);
				ctx1.lineTo(x,y);
				ctx1.stroke();
				ctx1.fillRect(x-2,y-2,5,5);
				ctx1.stroke();
			}
		}
	}
}

function findNode(x,y)
{
	var curX, curY, dx, dy, nodes;

	for(var w=0; w<ways.length; w++)
	{
		nodes = ways[w].nodes;

		for(var count=0; count<(w==ways.length-1 ? curNodeIndex:
									nodes.length); count++)
		{	
			curX = tile_engine.lonToX(nodes[count].lon);
			curY = tile_engine.latToY(nodes[count].lat);

			dx = Math.abs(x-curX);
			dy = Math.abs(y-curY);

			if(dx<3 && dy<3)
				return nodes[count];
		}
	}

	return null;
}
