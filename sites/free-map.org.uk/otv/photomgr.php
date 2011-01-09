<?php

include ('../lib/functionsnew.php');
include('otv_funcs.php');

session_start();

if(!isset($_SESSION["gatekeeper"]))
{
    echo "You need to be logged in to manage your photos!";
}
else
{
    echo "<html>";
    write_header();
    echo "<body onload='init()' onunload='savePageState()'>";
    ?>
    <h1>OpenTrailView - manage your panoramas</h1>
    <div id='photomgmt'>
        <h2>Instructions</h2>
        <ul>
        <li>Click on a panorama to select it. Then click on the map in the
        appropriate place to position the panorama on the earth.</li>
        </li>
        </ul>
    </div>
    <?php
    echo "<div id='photodiv'></div>";
    display_map(480,480);
    display_controls();
	echo "<p><a href='/index.php'>Back to main page</a></p>";
    echo "</body></html>\n";
}

function display_controls()
{
}

function write_header()
{
    ?>
    <head>
    <style type='text/css'>
    #map { position:absolute; left:720px; top:240px;  }
    #photomgmt { position:absolute; left:720px; top:0px; width:480px }
    #photodiv { width:880px }
    td { height: 200px; }
    </style>
    <link rel='stylesheet' type='text/css' href='css/osv.css' />
    <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
 
    <script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js">
    </script>
 
    <script type='text/javascript' 
    src='http://www.free-map.org.uk/javascript/prototype.js'></script>

    <script type='text/javascript'>

    var selectedPhotos;
    var angleBoxes;
    var userPhotos = null;

    var nrows=8,ncols=1,pixwidth=600;

    var currentPage;

    var initPage = <?php echo ($_GET['pg']) ? $_GET['pg']: 
            (isset($_COOKIE['otvPhotoMgrCurPage']) ?
            $_COOKIE['otvPhotoMgrCurPage']: 0); ?>;

    function init()
    {
        selectedPhotos = new Array();
        angleBoxes = false;
        initialiseMap();
        displayCurrentPage(initPage);
    }

    function ok1(xmlHTTP)
    {
        userPhotos = xmlHTTP.responseText.evalJSON();
        //alert('got ' + userPhotos.length + ' photos.');
        displayCurrentPage(initPage);
    }

    function error1(xmlHTTP)
    {
        alert('HTTP error: ' + xmlHTTP.status);
    }


    //Initialise the 'map' object
    var map;
    var curPos;
    var lat=<?php echo (isset($_COOKIE['otvLat']))?$_COOKIE['otvLat']:50;?>;
    var lon=<?php echo (isset($_COOKIE['otvLon']))?$_COOKIE['otvLon']:0;?>;
    var zoom=<?php echo (isset($_COOKIE['otvZoom']))?$_COOKIE['otvZoom']:4;?>;
    var session=<?php echo (isset($_GET['pssn']))? $_GET['pssn']:"null";?>;
    var wgs84;
    var mapX, mapY;
    function initialiseMap() 
    {
         wgs84 = new OpenLayers.Projection("EPSG:4326");
        curPos = null;
        map = new OpenLayers.Map ("map", {
                controls:[
                    new OpenLayers.Control.Navigation(),
                    new OpenLayers.Control.LayerSwitcher(),
                    new OpenLayers.Control.PanZoomBar(),
                    new OpenLayers.Control.Attribution()],
                maxExtent: new OpenLayers.Bounds
                (-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 15,
                units: 'm',
                projection: new OpenLayers.Projection("EPSG:900913"),
                displayProjection: wgs84 
            } );
 


        // Define the map layer
        layerMap = new OpenLayers.Layer.OSM();
        map.addLayer(layerMap);

        if( ! map.getCenter() )
        {
            var lonLat = new 
            OpenLayers.LonLat(lon, lat).transform
                (wgs84, map.getProjectionObject());
            map.setCenter (lonLat, zoom);
        }

        map.events.register("click", map, function(e)
            {
                var pos1 = this.getLonLatFromViewPortPx(e.xy);
                setCoords(pos1);
            }
        );
    }

    function setCoords(pos1)
    {
        if(selectedPhotos.length==1 && curPos==null)
        {
            curPos = pos1;
            var url=('/panorama.php?action=setAttributes&id='+
                selectedPhotos[0].id+'&x='+pos1.lon+'&y='+pos1.lat);
            OpenLayers.loadURL('/panorama.php?action=setAttributes&id='+
                selectedPhotos[0].id+'&x='+pos1.lon+'&y='+pos1.lat,'',null,
                latLonUpdated, latLonFail);
        }
        else
        {
            alert('ERROR: cannot set lat/lon; either no photo selected '+
                    'or an update of the lat/lon of last photo still '+
                    'in progress');
        }
    }

    function latLonUpdated(xmlHTTP)
    {
		var ll=curPos.transform(map.getProjectionObject(),wgs84);
        $('lat'+selectedPhotos[0].id).innerHTML = 'Lat: '+ll.lat.toFixed(3);
        $('lon'+selectedPhotos[0].id).innerHTML = 'Lon: '+ll.lon.toFixed(3);
        curPos = null;
    }

    function latLonFail(xmlHTTP)
    {
        alert('Error : '  + xmlHTTP.status);
    }

    function photoClick(e)
    {
        var evt = (e) ? e:window.event;
        var el = (evt.target) ? evt.target:evt.srcElement;
        if (!(e.shiftKey))    
        {
            removeSelected();
        }
        el.style.borderColor='red';    
        var id = el.id.substr(3);
        selectedPhotos.push ( { 'id' : id } );
    }

    function removeSelected()
    {
            var el;
            for(var i=selectedPhotos.length-1; i>=0; i--)
            {
                el=$('img'+selectedPhotos[i].id);
                if(el)
                    el.style.borderColor='black';
                selectedPhotos.pop();
            }
    }

    function position()
    {
        $('map').style.visibility='visible';
    }

    function positionDone()
    {
        $('map').style.visibility='hidden';
    }

    function displayCurrentPage(pg)
    {
        currentPage=pg;
        //var start=pg*nrows*ncols;

        // save any selected angles
        if(userPhotos)
        {
            for(var i=0; i<userPhotos.photos.length; i++)
            {
                for(var j=1; j<selectedPhotos.length; j++)
                {
                    if(userPhotos.photos[i].id==selectedPhotos[j].id)
                    {
                        selectedPhotos[j].angle = 
                            $('angle'+selectedPhotos[j].id).value;
                    }
                }
            }
        }

        var url = '/photomgr_server.php?pg='+pg+'&n='+(nrows*ncols);
        if(session!==null)
            url+='&pssn='+session;
        $('photodiv').innerHTML = "Loading...";

        var rqst = new Ajax.Request(url,
                { method: 'get',
                  onSuccess: doDisplayCurrentPage, 
                  onFailure: error1 }
                 );
    }

    function doDisplayCurrentPage(xmlHTTP)
    {
        userPhotos = xmlHTTP.responseText.evalJSON();
        var html="";
        html += "<table border='1'>"; // yeah i know 

        var w = pixwidth/ncols;
        for(var trow=0; trow<nrows; trow++)
        {
            html += "<tr>";
            for(var tcol=0; tcol<ncols; tcol++)
            {
                if(trow*ncols+tcol < userPhotos.photos.length)
                {
                    var p = userPhotos.photos[trow*ncols+tcol];

                    var isSelected=false;
                    var angle=null;
                    for(var i=0; i<selectedPhotos.length; i++)
                    {
                        if(selectedPhotos[i].id==p.id)
                        {
                            isSelected=true;
                            break;
                        }
                    }

                    html += "<td id='td"+p.id+"'>";
                    html += "<div><img id='img"+p.id+"' "+
                        "style='border-width: 5; border-style:solid;"
                        +"border-color: "+(isSelected?'red':'black')+"' "+
                        "width='"+w+"px' src='/panorama.php?id="+p.id+
							"&resize=25' "+
                        "alt='photo "+p.id+"' /></div>";
					var lonLat=null;
					if(p.x && p.y)
					{
						lonLat=new OpenLayers.LonLat(p.x,p.y).transform
							(map.getProjectionObject(),wgs84);
					}
                    html += "<span id='lat"+p.id+"'>Lat: "+
                        ((lonLat) ? lonLat.lat:'unknown')+
                        "</span><br />";
                    html += "<span id='lon"+p.id+"'>Lon: "+
                        ((lonLat) ?lonLat.lon:'unknown')+
                        "</span> <br />";
					html += "<a href='/panorama/"+p.id+"'>"+
                            "View full size</a> ";
                    html += "<br />";

                    html += "</td>";
                }
                else
                {
                    html += "<td></td>";
                }
            }
            html += "</tr>";
        }

        html += "</table>";
        if(currentPage>0)
        {
            html += "<p><input type='button' onclick='displayCurrentPage("+
            (currentPage-1)+")' value='Previous' />";
        }
        if(!(userPhotos.lst))
        {
            html += "<input type='button' onclick='displayCurrentPage("+
            (currentPage+1)+")' value='Next' /></p>";
        }
        $('photodiv').innerHTML = html;
        setupEvents();
    }

    function setupEvents()
    {
        var allPhotos = document.getElementsByTagName('img');
        var ids="";
        for(var i=0; i<allPhotos.length; i++)
        {
            if(allPhotos[i].id.substr(0,3)=='img')
            {
                allPhotos[i].onclick = photoClick;
                ids += allPhotos[i].id + " ";
            }
        }
    }

    function savePageState()
    {
        var lonLat = map.getCenter(). transform
                (map.getProjectionObject(),wgs84);
        document.cookie='otvLon='+lonLat.lon;
        document.cookie='otvLat='+lonLat.lat;
        document.cookie='otvZoom='+map.getZoom();
        document.cookie='otvPhotoMgrCurPage='+currentPage;
    }

    </script>
    </head>
    <?php
}
?>
