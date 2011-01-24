

// Initialise the 'map' object
function init() {
 
    freemap=new Freemap();

    freemap.map = new OpenLayers.Map ("map", {
        controls:[
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.Attribution(),
            new OpenLayers.Control.Permalink()],
        maxExtent: new OpenLayers.Bounds
                (-20037508.34,-20037508.34,20037508.34,20037508.34),
        maxResolution: 156543.0399,
        numZoomLevels: 19,
        units: 'm',
        projection: new OpenLayers.Projection("EPSG:900913"),
        displayProjection: new OpenLayers.Projection("EPSG:4326")
    } );
 

    // Define the map layer
    freemap.layerFreemap = new OpenLayers.Layer.OSM
                ("Freemap",
                "http://tilesrv.sucs.org/ofm/"+
                "${z}/${x}/${y}.png",{numZoomLevels:17} );
    freemap.map.addLayer(freemap.layerFreemap);

    if( ! freemap.map.getCenter() ){
        var lonLat = new 
                OpenLayers.LonLat(lon, lat).transform
                (new OpenLayers.Projection("EPSG:4326"), 
                freemap.map.getProjectionObject());
        freemap.map.setCenter (lonLat, zoom);
    }

    var sm = new OpenLayers.StyleMap (
        {
            "default": 
                { strokeWidth:8, strokeColor:'yellow',
                  strokeOpacity:0.6 }
        } );

    var asm=new OpenLayers.StyleMap (
        {
            "default":
                new OpenLayers.Style ( {
                    externalGraphic: 'images/annotation.png',
                    graphicHeight: 16,
                    graphicWidth: 16
                }
                )
        } );


    freemap.layerSelWays = new OpenLayers.Layer.Vector ("Selected Ways",
            { styleMap: sm } );
    freemap.wgs84 = new OpenLayers.Projection("EPSG:4326");
    freemap.layerAnnotations = new OpenLayers.Layer.Vector ("Annotations",
            { strategies: [new OpenLayers.Strategy.BBOX()],
             protocol: new OpenLayers.Protocol.HTTP
                 ( { url: "/freemap/annotation.php?action=getByBbox",
                    format: new OpenLayers.Format.FmapGeoRSS() } ),
            styleMap: asm } );

    freemap.map.addLayer(freemap.layerSelWays);
    freemap.map.addLayer(freemap.layerAnnotations);

    freemap.selectedWays = new Array();

    freemap.map.events.register("click",freemap,freemap.clickHandler);
    freemap.map.events.register("mousedown",freemap,freemap.mouseDownHandler);
    freemap.map.events.register("mousemove",freemap,freemap.mouseMoveHandler);
    freemap.map.events.register("mouseup",freemap,freemap.mouseUpHandler);
    freemap.map.events.register("zoomend",freemap,freemap.zoomendHandler);

    $('resetDist').onclick = freemap.resetDistance.bind(freemap);
    $('units').onchange = freemap.changeUnits.bind(freemap); 
    $('searchBtn').onclick = freemap.search.bind(freemap); 

    $('osmedit').href='http://www.openstreetmap.org/edit.html?lat='+
            lat+'&lon='+lon+'&zoom='+zoom;

    var selF = new OpenLayers.Control.SelectFeature
        (freemap.layerAnnotations,
            { onSelect: freemap.annotationSelect.bind(freemap) }  
        );
    freemap.map.addControl(selF);
    selF.activate();

    var hOptions = { callbacks:
            { done:freemap.routeDrawn.bind(freemap) }
                    };

    freemap.drawF = new OpenLayers.Control.DrawFeature
        (freemap.layerSelWays,OpenLayers.Handler.Path, hOptions);


    freemap.map.addControl(freemap.drawF);
    freemap.setMode(MODE_NORMAL);

    freemap.layerAnnotations.refresh();

    $('backToMap').onclick = freemap.hidePhotoCanvas.bind(freemap);
}


var Freemap = Class.create ( { 
    initialize: function()
    {
        this.map = null;
        this.popup = null;
        this.layerSelWays=null;
        this.layerAnnotations=null;
        this.selectedWays=null;
        this.lookupPos=null;
        this.dragging = false;
        this.navControl = null;
        this.dist = 0;
        this.lastPos = null;
        this.mode=null;
    },

    setMode : function(m)
    { 
        if(this.mode !== null)
            $('mode'+this.mode).setAttribute('class','deactivated');

        if($('mode'+m))
        {
            this.mode=m;
            $('mode'+this.mode).setAttribute('class','activated');
            if(this.mode!=MODE_NORMAL)
                this.getNavControl().deactivate();
            else
                this.getNavControl().activate();

            if(this.mode==MODE_ROUTE)
            {
                this.drawF.activate();
            }
            else
            {
                this.drawF.deactivate();
            }
        }
        else
        {
            alert('No mode ' +m);
        }
    },

    routeDrawn:function(g) 
    { 
        this.csvroute="";
        for(var i=0; i<g.components.length; i++)
        {
            if(i>0)
                this.csvroute+=",";
            this.csvroute+=g.components[i].x+" " +g.components[i].y;
        }
        var html = "<p>Select what to do with your route:</p>" +
                    "<p><select id='routeaction'>"+
 //                   "<option value='test'>Test (not for users)</option>"+
                    "<option value='gethtml'>Route as HTML</option>"+
                    "<option value='getpdf'>Route as PDF</option>"+
                    "<option value='getxml'>Route as XML</option>";
        if(loggedin)
            html += "<option value='add'>Save to database</option>";
        html += "</select>"+
                    "</p>"+
                    "<input type='button' id='routeactionbtn' value='Go' />";
        this.displayCentredPopup(html,0.25);
        $('routeactionbtn').onclick=this.routeSend.bind(this);
    },

    routeSend:function()
    {
        var qs;
        if($('routeaction').value.substr(0,3)=='get')
        {
            var format=$('routeaction').value.substr(3);
            qs='action=get&format='+format;
            this.map.removePopup(this.popup);
            this.popup=null;
            if(format!='html')
            {
                window.location='/freemap/route.php?'+qs+
                '&route='+this.csvroute;
            }
            else
            {
                new Ajax.Request
                ("/freemap/route.php",
                    { method: 'GET',
                     parameters: qs+'&route='+this.csvroute,
                     onComplete: this.routeSendCallback.bind(this) 
                     }
                );
            }
        }
        else if ($('routeaction').value=='test')
        {
            qs='action=get&format=xml';
            new Ajax.Request
            ("/freemap/route.php",
                { method : 'GET',
                parameters: qs+'&route=' + this.csvroute,
                onComplete: function(xmlHTTP) { alert(xmlHTTP.responseText); }
                }
            );
            this.map.removePopup(this.popup);
            this.popup=null;
        }
        else if ($('routeaction').value=='add')
        {
            qs='action=add';
            
            new Ajax.Request
            ("/freemap/route.php",
                { method : 'GET',
                parameters: qs+'&route=' + this.csvroute,
                onComplete: function(xmlHTTP) { alert(xmlHTTP.responseText); }
                }
            );
            this.map.removePopup(this.popup);
            this.popup=null;
        }
    },

    routeSendCallback: function(xmlHTTP)
    {
        this.displayCentredPopup(xmlHTTP.responseText +
            "<p><a href='/freemap/route.php?action=get&format=htmlpage"
            +"&route="+this.csvroute+"'>Printable</a></p>",0.8);
    },

    mouseDownHandler: function(e)
    {
        this.dragging = true;
    },

    mouseMoveHandler: function(e)
    {
        if(this.dragging==true)
        {

            if(this.mode==MODE_DISTANCE)
            {
                var latlon = this.map.getLonLatFromViewPortPx(e.xy).transform
                    (this.map.getProjectionObject(),this.wgs84);
                if(this.lastPos!=null)
                {
                    this.distance(latlon,this.lastPos);
                }
                this.lastPos = latlon;
            }
        }
    },

    mouseUpHandler:function(e)
    {

        this.dragging=false;
        var ll = this.map.getCenter().transform
            (this.map.getProjectionObject(),this.wgs84);
        $('osmedit').href='http://www.openstreetmap.org/edit.html?lat='+
            ll.lat+'&lon='+ll.lon+'&zoom='+this.map.getZoom();
        var expiry = new Date();
        expiry.setTime(expiry.getTime() + (1000*60*60*24*365));
        document.cookie='lat='+ll.lat+'; expires=' + expiry.toGMTString();
        document.cookie='lon='+ll.lon+'; expires=' + expiry.toGMTString();
        document.cookie='zoom='+this.map.getZoom()+'; expires=' + 
            expiry.toGMTString();
    },

    zoomendHandler: function(e)
    {
        this.layerAnnotations.setVisibility(this.map.getZoom() >= 14);        
    },

    clickHandler:function(e)
    {
        this.lookupPos = this.map.getLonLatFromViewPortPx(e.xy);

        // find distance equivalent to 8 pixels (so get a hit within 8px) 
        var dist=this.map.getResolution() * 8;

        if(this.mode == MODE_NORMAL || this.mode==MODE_ANNOTATE)
        {
            var what="both";
            var p= 'type=byCoord&x='+this.lookupPos.lon+ 
                    '&y='+this.lookupPos.lat+'&what='+what+'&dist='+dist;
            // use Ajax.Request to allow us to pass arbitrary data (lookup pos)
            // to the callback
            new Ajax.Request('/freemap/search.php',
                { method:'get',
                    lookupPos:this.lookupPos,
                    parameters:p,
                     onSuccess:this.lookupCallback.bind(this),
                    onFailure:this.failCallback.bind(this) }
                        );
        }
    },

    lookupCallback: function(xmlHTTP)
    {
        this.selNode=null;
        this.lookupPos = xmlHTTP.request.options.lookupPos;
           var nodes = xmlHTTP.responseXML.getElementsByTagName("node");
        var ways=null;
        var description="";
		var name="";
        if(this.mode!=MODE_ROUTE && nodes && nodes.length==1)
        {
            name=nodes[0].getElementsByTagName("name")[0].
                firstChild.nodeValue;
            var type=nodes[0].getElementsByTagName("type")[0].
                    firstChild.nodeValue;
            
            description=
                (nodes[0].getElementsByTagName("description").length>0)
            ?  nodes[0].getElementsByTagName("description")[0].
                    firstChild.nodeValue: "";
            var html = "<h1>"+name+"</h1><p>" +name+ " is a <em>"+type+
                "</em></p><p>"+description+"</p>";

            if(this.mode!=MODE_ANNOTATE)
            {
                this.displayPopup (html, this.lookupPos,200,200,true);
            }
            this.selNode = nodes[0].getElementsByTagName("osm_id")[0].
                firstChild.nodeValue;
        }
        else
        {
            var ways=xmlHTTP.responseXML.getElementsByTagName("way");
            if(ways && ways.length>0)
            {
                if(ways.length==1)
                {
                    var des= 
                    (ways[0].getElementsByTagName("designation").length>0) ? 
                     ways[0].getElementsByTagName("designation")[0].
                    firstChild.nodeValue: "";

                    var hwy=ways[0].getElementsByTagName("highway")[0].
                    firstChild.nodeValue;

                    var id=ways[0].getElementsByTagName("osm_id")[0].
                    firstChild.nodeValue;

                    var html = 
                    "<h1>Way "+id+"</h1><p>Designation : "+des+"<br />"+
                    "Highway : " + hwy +"</p>";
                    if(this.popup)
                    {
                        this.map.removePopup(this.popup);
                        this.popup = null;
                    }

                    if(this.mode!=MODE_ROUTE && this.mode!=MODE_ANNOTATE)
                    {
                        this.displayPopup(html,this.lookupPos,200,200,true);
                    }
                }

                this.routeReset();
            
                for(var w=0; w<ways.length; w++)
                {
                    var id=ways[w].getElementsByTagName("osm_id")[0].
                        firstChild.nodeValue;
                    var pts = ways[w].getElementsByTagName("point");
                    var annotations=ways[w].getElementsByTagName("annotation");
                    var f = new OpenLayers.Feature.Vector();
                    var g = new OpenLayers.Geometry.LineString();
                    var j = 0;
                    for(var i=0; i<pts.length; i++)
                    {
                        var xy=pts[i].firstChild.nodeValue.split(" ");
                        g.addPoint(new OpenLayers.Geometry.Point(xy[0],xy[1]));
                    }

                    if(annotations && annotations.length)
                    {
                        /*
                        j=0;
                        while(j<annotations.length)
                        {
                            var pt=new OpenLayers.Geometry.Point
                                (annotations[j].getAttribute("x"),
                                annotations[j].getAttribute("y") );
                            pt.annotationId = parseInt(annotations[j].
                                    getAttribute("id"));
                            var annotationFeature = new
                                OpenLayers.Feature.Vector();
                            annotationFeature.geometry=pt;
                            annotationFeature.text=
                                annotations[j].firstChild.nodeValue;
                            this.layerAnnotations.
                                addFeatures(annotationFeature);
                            j++;
                        }    
                        */
                    }

                    g.dir=compassDirection(wayDirection(g));
                    f.geometry = g;
                    f.fid = id;

                    this.selectedWays.push(f);
                    this.layerSelWays.addFeatures(f);
                }
            }
        }
        if (this.mode==MODE_ANNOTATE) 
        {
				var html = "";
				if(nodes && nodes.length==1 &&
					nodes[0].getElementsByTagName("name").length==1)
				{
					html += "<h1>"+
							nodes[0].getElementsByTagName("name")[0].
								firstChild.nodeValue  + "</h1>";
				}
                html+="<strong>Please enter the annotation:"+
                            "</strong>"+
                            "<br />"+
                            "<textarea id='annotationText'>"+description+
                            "</textarea><br />";

                if(ways && this.selectedWays.length==1)
                {
                    var oppdir = 
                            oppositeDirection
                                (this.selectedWays[0].geometry.dir);
                    html +=
                            "Which direction of "+
                            "travel does this apply to? <br /> "+
                            "<select id='direction'>"+
                            "<option value='0'>Both</option>"+
                            "<option value='1'>"+
                                this.selectedWays[0].geometry.dir+
                            "</option>"+
                            "<option value='-1'>"+oppdir+"</option>"+
                            "</select><br />";
                }
        
                html +=
                            "<input type='button' id='annotationBtn' "+ 
                            "value='go' />";
                this.displayCentredPopup(html,0.5);
                $('annotationBtn').onclick=this.doAnnotate.bind(this);
        }
    },

    failCallback:function(xmlHTTP)
    {
        if(xmlHTTP.status==401)
            alert('You need to be logged in to do that.');
        else
            alert('The server sent back an error: http code=' + xmlHTTP.status);
    },

    getNavControl: function()
    {
        if(this.navControl==null)
            this.navControl=this.map.getControlsByClass
                ('OpenLayers.Control.Navigation')[0];
        return this.navControl;
    },
    
    displayDistance: function (dist)
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
    },


    addDistance:function(d)
    {
        this.dist+=d;
        this.displayDistance(this.dist);
    },

    resetDistance:function()
    {
        this.lastPos = null;
        this.dist = 0;
        this.displayDistance(0);
    },

    distance:function (p1,p2)
    {
        var miles = (document.getElementById('units').value=="miles");
        this.addDistance(calcDistance(p1,p2) * (miles ? 0.6214 : 1));
    },

    changeUnits: function()
    {
        var miles = (document.getElementById('units').value=="miles");
        var factor = (miles) ?  0.6214: 1.6093;
        this.dist *=factor;
        this.displayDistance(this.dist);
    },

    search : function()
    {
        //alert('search not working yet! Try again soon.');
        OpenLayers.loadURL('/freemap/search.php?type=byName&q='+$('q').value,
             null, this,this.searchCallback,this.failCallback); 
    },

    searchCallback:function(xmlHTTP)
    {
        var nodes = xmlHTTP.responseXML.getElementsByTagName('node');
        if(nodes.length==1)
        {
            var x=nodes[0].getElementsByTagName('x')[0].firstChild.nodeValue;
            var y=nodes[0].getElementsByTagName('y')[0].firstChild.nodeValue;
            this.map.setCenter(new OpenLayers.LonLat(x,y));
        }
        else if (nodes.length>=2)
        {
            var html="<p><strong>Search results</strong></p>", x, y, name, type;
            for(i=0; i<nodes.length; i++)
            {
                x=nodes[i].getElementsByTagName('x')[0].firstChild.nodeValue;
                y=nodes[i].getElementsByTagName('y')[0].firstChild.nodeValue;
                name=nodes[i].getElementsByTagName('name')[0].
                    firstChild.nodeValue;
                type= nodes[i].getElementsByTagName('type')[0].
                    firstChild.nodeValue;
                html += "<li><a href='#' onclick='freemap.moveMap("+
                    x+","+y+")'>" + 
                    name + "</a> ("+type+")</li>"; 
            } 
            html += "</ul>";
            $('infopanel').innerHTML = html;
        }
        else
        {
            alert('no results');
        }
    },


    moveMap:function(x,y)
    {
        this.map.setCenter(new OpenLayers.LonLat(x,y));
    },

    routeReset: function()
    {
        this.layerSelWays.removeFeatures(this.selectedWays);
        this.selectedWays.clear();
        this.dist=0;
        this.displayDistance(0);
    },

    doAnnotate:function()
    {
        var dir=(this.selNode!==null) ? null:
            ($('direction') ? $('direction').value: 0);
        this.insertAnnotation(new OpenLayers.Geometry.Point
                    (this.lookupPos.lon, this.lookupPos.lat),
                    (this.selNode===null)?
                    this.selectedWays:this.selNode,$('annotationText').value,
                    dir);
        this.map.removePopup(this.popup);
    },


    // Insert an annotation into way features (on the server)

    insertAnnotation:function(pt,f,text,dir)
    {
        if(this.selNode===null)
        {
            var ids="";
            for(var i=0; i<f.length; i++)
            {
                if(i==0)    
                    ids+='&ways=';
                else    
                    ids+=',';
                ids+=f[i].fid;
            }

            var rqst = new Ajax.Request
                ('/freemap/annotation.php?action=create'+ids+
                '&text='+text+'&x='+pt.x+'&y='+pt.y+
                '&dir='+dir,
                    { method: 'get',
                         aText: text,
                         point:pt,
                          onSuccess: this.insertAnnotationResponse.bind(this),
                          onFailure: this.failCallback.bind(this) }
                  );
        }
        else
        {

            var url='/freemap/node.php?action=annotate&id='+
                this.selNode+'&text='+text;
			alert(url);
            var rqst=new Ajax.Request
                (url,
                    {
                        method:'get',
                        onSuccess:function(xmlHTTP)
                        { 
                            alert('added successfully.'); 
                        },
                        onFailure: this.failCallback.bind(this)
                    });

        }
    },

    insertAnnotationResponse:function(xmlHTTP)
    {
        alert('response from inserting annotation: ' + xmlHTTP.responseText);
        var pt=xmlHTTP.request.options.point;
        var id=xmlHTTP.responseText;
        var annotationFeature=new OpenLayers.Feature.Vector();
        annotationFeature.geometry = pt;
        annotationFeature.text = xmlHTTP.request.options.aText;
        this.layerAnnotations.addFeatures(annotationFeature);
        var html = "<p>If your annotation is accompanied by a photo or "+
                    "diagram, upload it here (JPEGs only for the moment). "+
                    "Otherwise click Cancel.</p>"+
                "<form method='post' action='annotation.php' "+
                "enctype='multipart/form-data'>"+
                "<input type='file' name='photofile' /><br />"+
                "<input type='hidden' name='id' value='"+id+"' />"+
                "<input type='hidden' name='action' value='addPhoto' />"+
                "<input type='submit' value='Go!' />"+
                "<input type='button' value='Cancel' id='uploadcancel'/>"+
                "</form>";
        this.displayCentredPopup(html,0.4);
        $('uploadcancel').onclick = this.removePopup.bind(this);
    },

    displayPopup:function(text,pos,w,h,fc)
    {
        if(this.popup)
            this.map.removePopup(this.popup);
        this.popup = (fc===true) ?
        new OpenLayers.Popup.FramedCloud
        (
            null,
            pos,    
            new OpenLayers.Size(w,h),
            text,
            null,
            true
        )
        :
        new OpenLayers.Popup
        (
            null,
            pos,    
            new OpenLayers.Size(w,h),
            text,
            true
        );
        this.map.addPopup(this.popup);
    },

    displayCentredPopup:function(html,relsize)
    {
        this.displayPopup(html,
                this.map.getLonLatFromViewPortPx(
                    new OpenLayers.Pixel(
                    this.map.getSize().w*0.5-
                    this.map.getSize().w*relsize*0.5,
                    this.map.getSize().h*0.5-
                    this.map.getSize().h*relsize*0.5)),
                this.map.getSize().w*relsize,this.map.getSize().h*relsize,
                false);
    },

    removePopup: function()
    {
        this.map.removePopup(this.popup);
    },

     annotationSelect:            function (f)
    {
                    var html = 
                        (f.attributes.hasPhoto==1) ?
                    "<p>"+f.attributes.description+"</p>"+
                    "<p><img src='annotation.php?id="+f.fid+
                        "&action=getPhoto' width='320px' height='200px'"+
                        "/></p>" +
                        "<p><a href='#' id='viewFullSize'>View full size</a>":
                    f.attributes.description;
                    freemap.displayPopup(html,
                        new OpenLayers.LonLat(f.geometry.x,f.geometry.y),
                            200,200,true);
					if($('viewFullSize'))
					{
                    	$('viewFullSize').onclick=this.showPhotoCanvas.bind
							(this);
					}
                    $('canvas').curId = f.fid;
    },

    showPhotoCanvas: function()
    {
        if($('canvas') && $('canvas').getContext)
        {
            $('map').style.width='0px';
            $('canvas').style.visibility='visible';
            $('backToMap').style.visibility='visible';
            var ctx = $('canvas').getContext('2d');
            var img=new Image();
            img.src='annotation.php?id='+$('canvas').curId+'&action=getPhoto';
            ctx.drawImage(img,0,0,800,600);
        }
        else
        {
            alert('sorry: only available on canvas-supporting browsers');
        }
    },

    hidePhotoCanvas: function()
    {
        $('canvas').style.visibility='hidden';
        $('backToMap').style.visibility='hidden';
        $('map').style.width='800px';
    }

} );

function calcdist (x1,y1,x2,y2)
{
    var dx = x2-x1, dy=y2-y1;
    return Math.sqrt(dx*dx+dy*dy);
}


// find the distance from a point to a line
// based on theory at:
// http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/

function distp ( px, py, x1, y1, x2,  y2)
{
    var u = ((px-x1)*(x2-x1)+(py-y1)*(y2-y1)) / 
            (Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    var xintersection = x1+u*(x2-x1), yintersection=y1+u*(y2-y1);
    return (u>=0&&u<=1) ? 
            calcdist(px,py,xintersection,yintersection) :
                99999999;
}

function wayDirection(g)
{
    var x1=g.components[0].x;
    var y1=g.components[0].y;
    var x2=g.components[g.components.length-1].x;
    var y2=g.components[g.components.length-1].y;
    var dx = x2-x1;
    var dy = y2-y1;
    var ang=(-rad2deg(Math.atan2(dy,dx))) + 90;
    return (ang<0 ? ang+360:ang);
}

function rad2deg(r)
{
    return r*(180.0/Math.PI);
}

function compassDirection(a)
{
    if(a<22.5 || a>=337.5)
        return "N";
    else if(a<67.5)
        return "NE";
    else if(a<112.5)
        return "E";
    else if(a<157.5)
        return "SE";
    else if(a<202.5)
        return "S";
    else if(a<247.5)
        return "SW";
    else if(a<292.5)
        return "W";
    else
        return "NW";
}

function oppositeDirection(dir)
{
    var dirs = new Array ("N","NE","E","SE","S","SW","W","NW");
    for(var i=0; i<8; i++)
    {
        if(dirs[i]==dir)
            return dirs[i<4 ? i+4:i-4];
    }
    return null;
}
function calcDistance (pos1,pos2)
{
    // This takes latlon (only!) 
    var d =  OpenLayers.Util.distVincenty (pos1,pos2);
    return d;
}

function    getInsertSegment(pt,rte)
{
            var segPt1, segPt2;
            var  newComponents, curDist, lowestDist, nearestSeg;
            var lastAnnotationId=0, annotationId=0;

            // annotations must be no more than 100m from the way
            lowestDist=100; 
            nearestSeg = -1;
                
            for(var seg=0; seg<rte.components.length-1; seg++)
            {
                segPt1 = rte.components[seg];
                segPt2 = rte.components[seg+1];

                if(segPt1.annotationId)
                    lastAnnotationId = segPt1.annotationId;

                curDist = distp(pt.x,pt.y,
                    segPt1.x,segPt1.y,
                    segPt2.x,segPt2.y);
                if(curDist < lowestDist)
                {
                    lowestDist = curDist;
                    nearestSeg = seg;
                    annotationId = lastAnnotationId+1;
                }
            }

        return [nearestSeg,annotationId];
}
