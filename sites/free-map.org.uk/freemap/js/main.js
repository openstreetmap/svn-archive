
var map; //complex object of type OpenLayers.Map

var wgs84;

//Initialise the 'map' object
function init() {
 
    map = new OpenLayers.Map ("map", {
        controls:[
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.Attribution()],
        maxExtent: new OpenLayers.Bounds
                (-20037508.34,-20037508.34,20037508.34,20037508.34),
        maxResolution: 156543.0399,
        numZoomLevels: 19,
        units: 'm',
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326")
    } );
 
 
    // Define the map layer
    layerFreemap = new OpenLayers.Layer.OSM
                ("Freemap",
                "http://tilesrv.sucs.org/ofm/"+
                "${z}/${x}/${y}.png",{numZoomLevels:17} );
    map.addLayer(layerFreemap);
 
    if( ! map.getCenter() ){
        var lonLat = new 
                OpenLayers.LonLat(lon, lat).transform
                (new OpenLayers.Projection("EPSG:4326"), 
                map.getProjectionObject());
        map.setCenter (lonLat, zoom);
    }

    wgs84 = new OpenLayers.Projection("EPSG:4326");
    map.events.register("click",map,clickHandler);
    map.events.register("mousedown",map,mouseDownHandler);
    map.events.register("mousemove",map,mouseMoveHandler);
    map.events.register("mouseup",map,mouseUpHandler);

    $('resetDist').onclick = resetDistance;
    $('units').onchange = changeUnits; 
    $('searchBtn').onclick = search; 
}


var dragging = false;
var navControl = null;
var mode = 0;
var dist = 0;
var lastPos = null;

function setMode(m)
{
    $('mode'+mode).setAttribute('class','deactivated');
    if(($('mode')+mode)!=null)
    {
        mode=m;
        $('mode'+mode).setAttribute('class','activated');
        if(mode!=0)
            getNavControl().deactivate();
        else
            getNavControl().activate();
    }
    else
    {
        alert('No mode ' +m);
    }
}

function mouseDownHandler(e)
{
    $('status').innerHTML = 'mouse down';
    dragging = true;
}

function mouseMoveHandler(e)
{

    if(dragging==true)
    {
        $('status').innerHTML = 'mouse drag';

        if(mode==MODE_DISTANCE)
        {
            var goog = map.getLonLatFromViewPortPx(e.xy);
            var latlon=goog.transform(map.getProjectionObject(),wgs84);
            if(lastPos!=null)
            {
                distance(latlon,lastPos);
            }
            lastPos = latlon;
        }
    }
}

function mouseUpHandler(e)
{

    $('status').innerHTML = 'mouse up';
    dragging=false;
}

function clickHandler(e)
{
    $('status').innerHTML = 'click';
}

function getNavControl()
{
    if(navControl==null)
        navControl=map.getControlsByClass('OpenLayers.Control.Navigation')[0];
    return navControl;
}
    
function displayDistance (dist)
{
    var intDist=Math.floor(dist%1000), decPt=Math.floor(10*(dist-intDist)), 
    displayedIntDist = (intDist<10) ? "00" : ((intDist<100) ? "0" : ""),
    unitsElem = $('distUnits'); 
    distTenthsElem = $('distTenths'); 

    displayedIntDist += intDist;

    unitsElem.replaceChild ( document.createTextNode(displayedIntDist),
                                 unitsElem.childNodes[0] );

    distTenthsElem.replaceChild ( document.createTextNode(decPt),
                                  distTenthsElem.childNodes[0] );
}

function calcDistance (pos1,pos2)
{
    // This takes latlon (only!) 
    var d =  OpenLayers.Util.distVincenty (pos1,pos2);
    return d;
}

function  distance (p1,p2)
{
    var miles = (document.getElementById('units').value=="miles");
    dist += (calcDistance(p1,p2) * (miles ? 0.6214 : 1));
    displayDistance(dist);
}


function resetDistance()
{
    lastPos = null;
    dist = 0;
    displayDistance(0);
}

function changeUnits()
{
    var miles = (document.getElementById('units').value=="miles");
    var factor = (miles) ?  0.6214: 1.6093;
    dist *=factor;
    displayDistance(dist);
}

function search()
{
    //alert('search not working yet! Try again soon.');
    OpenLayers.loadURL('/freemap/search.php?action=search&q='+$('q').value,
         null, null,searchCallback,failCallback); 
}

function searchCallback(xmlHTTP)
{
    var nodes = xmlHTTP.responseXML.getElementsByTagName('node');
    if(nodes.length==1)
    {
        var x=nodes[0].getElementsByTagName('x')[0].firstChild.nodeValue;
        var y=nodes[0].getElementsByTagName('y')[0].firstChild.nodeValue;
        alert(x+','+y);
        map.setCenter(new OpenLayers.LonLat(x,y));
    }
    else if (nodes.length>=2)
    {
        var html="<p><strong>Search results</strong></p>", x, y, name;
        for(i=0; i<nodes.length; i++)
        {
            x=nodes[i].getElementsByTagName('x')[0].firstChild.nodeValue;
            y=nodes[i].getElementsByTagName('y')[0].firstChild.nodeValue;
            name=nodes[i].getElementsByTagName('name')[0].firstChild.nodeValue;
            html +=
                "<li><a href='#' onclick='moveMap("+x+","+y+")'>" +
                    name + "</a> </li>";
        }
		html += "</ul>";
        $('infopanel').innerHTML = html;
    }
    else
    {
        alert('no results');
    }
}

function failCallback(xmlHTTP)
{
    alert('The server sent back an error: http code=' + xmlHTTP.status);
}

function moveMap(x,y)
{
    map.setCenter(new OpenLayers.LonLat(x,y));
}
