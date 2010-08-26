
        var map; //complex object of type OpenLayers.Map
        var feature, p, rotateControl, selectControl, lineControl, dragControl;
        var georss, lineLayer;
        var wgs84;
        var pv=null;
        var currentRoute = null;
        var photosInNewRoute = null;
        var mode;
        var lstyle,lselectStyle, lsstyle;
        var preDragGeom;



        //Initialise the 'map' object
        function init() {

            wgs84 = new OpenLayers.Projection("EPSG:4326");

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
 
            mode=0;
            photosInNewRoute = new Array();


            // Define the map layer
            var layerOSM = new OpenLayers.Layer.OSM.Mapnik("OSM");
            map.addLayer(layerOSM);

            var ctxt = {
                getRotation: function(feature)
                {
                    return feature.attributes.direction;
                }
            };

            var style = new OpenLayers.Style (
                    { strokeWidth: 5, strokeColor: 'blue',
                      strokeOpacity: 0.6,
                      externalGraphic: 'images/cam.png',
                                  graphicHeight: 16,
                                graphicWidth: 16,
                                rotation: "${getRotation}" },
                                    { context: ctxt }
                                            );


            var sm = new OpenLayers.StyleMap ( 
            { "default": style }
                );

            georss = new OpenLayers.Layer.Vector
                    ("OTV Panoramas",
                        { strategies: [new OpenLayers.Strategy.BBOX()],
                        protocol: new OpenLayers.Protocol.HTTP 
                            ({ url: "/otv/rss.php",
                              format: new OpenLayers.Format.GeoRSSExt()
                              }),
                          'styleMap' : sm,
                          'projection': wgs84
                         } );

             lstyle =   {  strokeWidth: 5, strokeColor: 'blue',
                                strokeOpacity: 0.6  };

             lsstyle =   {  strokeWidth: 5, strokeColor: 'red',
                                strokeOpacity: 0.6  };

            var lsm = new OpenLayers.StyleMap ( 
            { "default": lstyle, "select" : lsstyle }
                );

        
            lineLayer = new OpenLayers.Layer.Vector("Routes",
                        { styleMap: lsm,
                           strategies: [new OpenLayers.Strategy.BBOX()],
                           protocol: new OpenLayers.Protocol.HTTP
                               ( 
                                { url: "/otv/route.php?format=json&action=get",
                                   format: new OpenLayers.Format.RouteJSON ()
                                   //( { checkTags: true } )
                                }
                            ),
                            projection: wgs84 }
                        );
            
            map.addLayer(georss);
             map.addLayer(lineLayer);

            rotateControl = new OpenLayers.Control.DragFeature (
                georss,
                {
                    downFeature : function(pixel)
                    {
                        this.feature.originalAngle =
                            this.feature.attributes.direction;
                    },
                    
                    moveFeature: function(pixel)
                    {

                            var angle=10;

                            var d = this.feature.attributes.direction;

                            this.feature.attributes.direction = 
                                this.feature.attributes.direction===null ?
                                    angle:
                                    (d < 360-angle ?  d+10: 0);
                        
                            this.layer.drawFeature(this.feature);
                    },

                    doneDragging: function()
                    {
                            // AJAX request to update the rotation...
                            var rqst = new Ajax.Request
            ('/otv/panorama/'+this.feature.fid+'/rotate/'+
                this.feature.attributes.direction,
                { method: 'get',
                  alteredFeature: this.feature, 
                  operation: 'rotate',
                  onSuccess: cb,
                  onFailure: failcb }
                  );
                    }
                }
            );

            dragControl = new OpenLayers.Control.DragFeature (georss,
                    {
                        onStart: function(f)
                        {
                            preDragGeom = new OpenLayers.Geometry.Point
                                (f.geometry.x,f.geometry.y);
                            preDragGeom.photo_id = f.fid;
                        },
                        onDrag: function(f)
                        {


                            if(!(f.routes))
                                f.routes=new Array();

                            if(!(f.intersected))
                                f.intersected=new Array();
                            else
                                f.intersected.clear();

                            // idea of polygon from:
                            // http://gis.ibbeck.de/ginfo/apps/
                            // OLExamples/OL27/examples/
                            // intersection%20of%20features.asp
                           var poly = OpenLayers.Geometry.Polygon.
                                createRegularPolygon
                                (new OpenLayers.Geometry.Point(f.geometry.x,
                                f.geometry.y),
                                 5*map.getResolution(),6,0);

                            for(var i=0; i<lineLayer.features.length; i++)
                            {
                                var drawThis=false;
                                for(var j=0; 
                                        j<lineLayer.features[i].geometry.
                                        components.length; 
                                        j++)
                                {
                                    // we want any parent routes to move
                                    // with the photo
                                    if(lineLayer.features[i].geometry.
                                            components[j].photo_id  && 
                                           lineLayer.features[i].geometry.
                                            components[j].photo_id  ==
                                            f.fid)
                                    {
                                        if(f.routes.indexOf
                                            (lineLayer.features[i].fid)== -1)
                                        {
                                            f.routes.push
                                                (lineLayer.features[i].fid);
                                        }

                                        lineLayer.features[i].geometry.
                                            components[j].x = f.geometry.x;

                                        lineLayer.features[i].geometry.
                                            components[j].y = f.geometry.y;

                                        drawThis=true;
                                    }
                                }

                                // does the photo intersect a new geometry?
                                if(poly.intersects
                                (lineLayer.features[i].geometry))
                                {

                                    // if the feature isn't already part
                                    // of this geometry
                                    if(f.routes.indexOf
                                            (lineLayer.features[i].fid) == -1)
                                    {
                                        lineLayer.features[i].style=
                                                lsstyle;
                                        lineLayer.features[i].state=
                                            'selected';
                                        f.intersected.push
                                                (lineLayer.features[i]);
                                    }
                                    drawThis=true;
                                }
                                else if (lineLayer.features[i].state==
                                            'selected')
                                {
                                    lineLayer.features[i].style=lstyle;
                                    lineLayer.features[i].state='';
                                    drawThis=true;
                                }

                                if(drawThis)
                                {
                                    lineLayer.drawFeature
                                    (lineLayer.features[i]);
                                }
                            }
                        },

                        onComplete: function(f)
                        {
                                if(f.intersected.length>0)
                                {
                                    alert(f.geometry.x+" "+ f.geometry.y);
                                    insertPhotoInRoute(f,preDragGeom);
                                }
                                else
                                {
                                    var lonlat=new OpenLayers.LonLat
                                    (f.geometry.x,f.geometry.y).
                                    transform(map.getProjectionObject(),wgs84);

                                    var rqst = new Ajax.Request
                                    ('/otv/panorama.php?id='+f.fid+
                                    '&lat='+lonlat.lat+'&lon='+lonlat.lon+
                                    '&action=setAttributes',
                                        { method: 'get',
                                              alteredFeature: f,
                                            operation: 'move',
                                            predragG: preDragGeom,
                                              onSuccess: cb,
                                              onFailure: failcb }
                                  );
                                }
                        }
                    }
            );

            selectControl = new OpenLayers.Control.SelectFeature (
                [georss,lineLayer],
                {
                    //hover: true,
                    onSelect: onFeatureSelect,
                    onUnselect: onFeatureUnselect
                }
            );

            lineControl = new OpenLayers.Control.DrawFeature(
                lineLayer,OpenLayers.Handler.Path);

            var handler = new OpenLayers.Handler.Path
                (lineControl,{point: pointHandler, done:isDone } );
            lineControl.handler = handler;

            map.addControl(rotateControl);
            map.addControl(selectControl);
            map.addControl(lineControl);
            map.addControl(dragControl);

            if( ! map.getCenter() ){
                var lonLat = new 
                OpenLayers.LonLat(lon, lat).transform
                (wgs84,
                map.getProjectionObject());
                map.setCenter (lonLat, zoom);
            }

            //map.events.register("click",map,clickHandler);

            rotateControl.deactivate();
            selectControl.activate();

            $('backtomap').onclick = onPanoramaClose;
            $('backtomap').style.visibility='hidden';
        }
        

        function onFeatureSelect(feature)
        {
            if(mode==4)
            {
                var server=(feature.geometry.CLASS_NAME==
                    'OpenLayers.Geometry.LineString') ?
                    'route.php' : 'panorama.php';
                var rqst= new Ajax.Request
                    ("/otv/"+server+"?action=delete&id="+feature.fid,
                        {method:"get",
                        onSuccess:cb,
                        onFailure:failcb,
                        operation:"delete",
                        feature: feature,
                        layer: (feature.geometry.CLASS_NAME==
                            'OpenLayers.Geometry.LineString') ?
                            lineLayer: georss
                        }
                    );
            }
            else
            {
                if(feature.geometry.CLASS_NAME==
                    'OpenLayers.Geometry.LineString')
                {
                    feature.state="selected";
                }
                else if(feature.attributes.isPano==1)
                {
                    var id = feature.fid;
                    $('map').style.width='0px';
                    $('backtomap').style.visibility='visible';
                    $('pancanvas').style.visibility='visible';
                    //$('pancanvas').style.height='400px';
                    $('pancanvas').height = 400;
                    pv=new CanvasPanoViewer({
                    canvasId: 'pancanvas',
                    statusElement: 'status', 
                    hFovDst : 90,
                    hFovSrc : 360,
                    showStatus : 0,
                    wSlice:10,
                    imageUrl: '/otv/panorama/'+id
                    } );
                }
                else
                {
                    $('map').style.width='0px';
                       $('backtomap').style.visibility='visible';
                    $('imgdiv').style.height='400px';
                    $('imgdiv').style.visibility='visible';
                    var img = document.createElement("img");
                    img.src = '/otv/panorama/'+feature.fid;
                       //img.height='400px';
                    img.id = 'photoimg';
                    $('imgdiv').appendChild(img);
                }
            }
        }

        function onFeatureUnselect(feature)
        {
            if(feature.geometry.CLASS_NAME=='OpenLayers.Geometry.LineString')
            {
                feature.state="unselected";
            }
        }

        function onPanoramaClose()
        {
            $('map').style.width='1024px';
            if($('imgdiv').style.visibility=='visible')
            {
                $('imgdiv').removeChild($('photoimg'));
                $('imgdiv').style.visibility='hidden';
                $('imgdiv').style.height = '0px';
            }
            else if($('pancanvas').style.visibility=='visible')
            {
                $('pancanvas').style.visibility='hidden';
                $('pancanvas').height = 0;
            }
            pv=null;
            $('backtomap').style.visibility='hidden';
        }

        function modeSet(obj)
        {

            //mode = $('mainctrl').value;
            mode = obj.id.substr(4);

            // align
            if(mode==1)
            {
                rotateControl.activate();
                selectControl.deactivate();
                lineControl.deactivate();
                dragControl.deactivate();
            }
            // view, delete
            else if (mode==0 || mode==4)
            {
                rotateControl.deactivate();
                selectControl.activate();
                lineControl.deactivate();
                dragControl.deactivate();
            }
            // drag features
            else if (mode==3)
            {
                rotateControl.deactivate();
                dragControl.activate();
                selectControl.deactivate();
                lineControl.deactivate();
            }
            // connect
            else if (mode==2)
            {
                rotateControl.deactivate();
                selectControl.deactivate();
                lineControl.activate();
                dragControl.deactivate();
            }
            // anything else
            else 
            {
                rotateControl.deactivate();
                selectControl.deactivate();
                lineControl.deactivate();
                dragControl.deactivate();
            }
        }
        function cb(xmlHTTP)
        {
            alert('successfully performed a '+xmlHTTP.request.options.operation+
                ' operation');
            switch(xmlHTTP.request.options.operation)
            {
                case "delete":
                    xmlHTTP.request.options.layer.removeFeatures
                        ([xmlHTTP.request.options.feature]);
                    removePhotoPointFromRoutes
                        (xmlHTTP.request.options.feature.fid);
                    break;

                case "newroute":
                    var photos = xmlHTTP.request.options.photos;
                    var f = xmlHTTP.request.options.route;


                    if(f.fid)
                    {
                        f.style=lstyle;
                        f.state='';
                        lineLayer.removeFeatures([f]); // has old geom
                        f.geometry=xmlHTTP.request.options.geometry;
                        lineLayer.drawFeature(f);
                    }
                    else
                    { 
                        f.geometry=xmlHTTP.request.options.geometry;
                        f.fid=xmlHTTP.responseText;
                        lineLayer.addFeatures([f]);
                    }

                    for(var i=0; i<photos.length; i++)
                    {
                        if(!(photos[i].routes))
                            photos[i].routes=new Array();
                        if(photos[i].routes.indexOf(f.fid)==-1)
                            photos[i].routes.push(f.fid);
                    }
                    break;

                case "insert":
                    var manRoutes=xmlHTTP.request.options.manipulatedRoutes;
                    var f = xmlHTTP.request.options.insertedFeature;
                    if(!(f.routes))
                        f.routes=new Array();
                    for(var i=0; i<manRoutes.length; i++)
                        f.routes.push(manRoutes[i].fid);
                    drawAllFeatureRoutes(f);

                    break;
            }
        }

        function failcb(xmlHTTP)
        {
            if(xmlHTTP.status==401)
            {
                alert("Log in to orientate/move/delete photos or manipulate"
                +" routes. You can orientate/move "+
                    "other people's but you must be logged in.");
            }
            else
            {
                alert('Internal error: http code=' + xmlHTTP.status);
            }

            var f = xmlHTTP.request.options.alteredFeature;
            switch(xmlHTTP.request.options.operation)
            {
                case 'move':
                    var predragG = xmlHTTP.request.options.predragG;
                    f.geometry.x = predragG.x;
                    f.geometry.y = predragG.y;
                    georss.drawFeature(f);
                    resetPhotoLines(f,predragG);
                    break;

                case 'rotate':
                    f.attributes.direction = f.originalAngle;
                    georss.drawFeature(f);
                    break;

                case 'insert':
                    var r = xmlHTTP.request.options.manipulatedRoutes;
                    for(var i=0; i<r.length; i++)
                    {
                        r[i].geometry.removePoint
                            (xmlHTTP.request.options.insertedPoint);
                    }
                    lineLayer.drawFeature(f);
                    break;
            }
        }

        function drawAllFeatureRoutes(f)
        {
            if(f.routes)
            {
                      for(var i=0; i<f.routes.length; i++)
                      {
                        for(var j=0; j<lineLayer.features.length; j++)
                        {
                            if(lineLayer.features[j].fid==f.routes[i])
                            {    
                                lineLayer.drawFeature(lineLayer.features[j]);
                                break;
                            }
                        }
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
        }

        /////////////////// Routes stuff ///////////////////

        function pointHandler(p)
        {
            var screenPos = georss.getViewPortPxFromLonLat
                        (new OpenLayers.LonLat(p.x,p.y)),
                panScreenPos;

            var limit = 5; // in tests, 5 is about right

            // Go through all panorama features and see if there is one within
            // range  (use pixel not absolute distance)
            for(var count=0; count<georss.features.length; count++)
            {
                    panScreenPos = georss.getViewPortPxFromLonLat
                    (new OpenLayers.LonLat
                        (georss.features[count].geometry.x,
                        georss.features[count].geometry.y));
                    var d =dist(screenPos.x,screenPos.y,
                            panScreenPos.x,panScreenPos.y);
                    if (d < limit) 
                    {     
                        photosInNewRoute.push(georss.features[count]);
//                        georss.features[count].inRoute=true;
                    }
            }
        }

        function isDone(g)
        {
            // show the geometry permanently
            //currentRoute.upload();
            addRouteToVectorLayer();
        }

        function addRouteToVectorLayer()
        {
            var newGeom = new OpenLayers.Geometry.LineString();
            var selectedRoute = null;
            var selectedStart, selectedEnd, newStart, newEnd;
            for(var i=0; i<lineLayer.features.length; i++)
            {
                if( lineLayer.features[i].state=='selected')
                {
                    selectedRoute = lineLayer.features[i];
                    break;
                }
            }
       

            if(selectedRoute)
            {
                var headhead=
                selectedRoute.geometry.components[0].distanceTo
                    (photosInNewRoute[0].geometry);

                var tailtail=
                 selectedRoute.geometry.components
                     [selectedRoute.geometry.components.length-1].distanceTo
                     (photosInNewRoute[photosInNewRoute.length-1].geometry);

                var forward=
                selectedRoute.geometry.components
                    [selectedRoute.geometry.components.length-1].distanceTo
                    (photosInNewRoute[0].geometry);

                var reverse=
                selectedRoute.geometry.components
                    [0].distanceTo
                    (photosInNewRoute[photosInNewRoute.length-1].geometry);

                if(headhead<tailtail && headhead<forward && headhead<reverse)
                {
                    // head to head
                    selectedStart=selectedRoute.geometry.components.length-1;
                    selectedEnd=-1;
                    newStart=1;
                    newEnd = photosInNewRoute.length;
                }
                else if (tailtail<forward && tailtail<reverse)
                {
                    // tail to tail
                    selectedStart=0;
                    selectedEnd=selectedRoute.geometry.components.length;
                    newStart = photosInNewRoute.length-2;
                    newEnd=-1;
                }
                else if (forward<reverse)
                {
                    selectedStart=0;
                    selectedEnd=selectedRoute.geometry.components.length;
                    newStart=1;
                    newEnd = photosInNewRoute.length;
                }
                else
                {
                    selectedStart=selectedRoute.geometry.components.length-1;
                    selectedEnd=-1;
                    newStart = photosInNewRoute.length-2;
                    newEnd=-1;
                }
                for(var i=selectedStart; i!=selectedEnd; 
                    i+=(selectedStart<selectedEnd ? 1:-1))
                {
                    newGeom.addPoint(selectedRoute.geometry.components[i]);
                }

            }
            else
            {
                newStart = 0;
                newEnd = photosInNewRoute.length;
            }

            for(var i=newStart; i!=newEnd; i+=(newStart<newEnd ? 1:-1))
            {
                // Do NEW geometries
                //newGeom.addPoint(photosInNewRoute[i].geometry);
                var p = new OpenLayers.Geometry.Point
                            (photosInNewRoute[i].geometry.x,
                             photosInNewRoute[i].geometry.y);

                // Set the photo_id of the point to the id of the panorama
                 p.photo_id = photosInNewRoute[i].fid; 
                 newGeom.addPoint(p);
            } 
   
               alert('newGeom.length='+newGeom.components.length);
            var panoIDs="";
            for(var i=0; i<newGeom.components.length; i++)
            {
                if(i>0)
                    panoIDs+=',';
                panoIDs+=newGeom.components[i].photo_id;
            }

            var f;
            if(selectedRoute)
            {
                url='/otv/route.php?action=modify&id='+
                    selectedRoute.fid+'&panoIDs='+panoIDs;
                f=selectedRoute;
            }
            else
            {
                url='/otv/route.php?action=add&panoIDs='+panoIDs;
                f=new OpenLayers.Feature.Vector();
                f.fid=null;
                f.style = lstyle;
                f.state = '';
            }

        
            var rqst = new Ajax.Request
                (url,
                        {method: 'get',
                        route: f,
                        geometry: newGeom,
                        operation: 'newroute',
                        photos: photosInNewRoute,
                        onSuccess: cb,
                        onFailure: failcb }
                );

            // clear() doesn't work without Prototype, I think it must be
            // a prototype specific function
            photosInNewRoute.clear();
        }

        function insertPhotoInRoute(f,predrag)
        {
            var pt = f.geometry;
            var segPt1, segPt2;
            var m, c, val, newComponents, p, curDist, lowestDist,
                nearestSeg;

            var rtes=new Array(), prevs=new Array();
            var manRoutes = new Array();

            //for(var route=0; route<lineLayer.features.length; route++)
            for(var route=0; route<f.intersected.length; route++) 
            {
                lowestDist=1024;
                nearestSeg = -1;
                        alert('route index ' + route + '(id' +
                        f.intersected[route].fid+ ' is slected');
                        
                    var rte=f.intersected[route].geometry;
                    for(var seg=0; seg<rte.components.length-1; seg++)
                    {
                        alert('seg='+seg);
                        segPt1 = rte.components[seg];
                        segPt2 = rte.components[seg+1];
                        curDist = distp(pt.x,pt.y,
                            segPt1.x,segPt1.y,
                            segPt2.x,segPt2.y);
                        if(curDist < lowestDist)
                        {
                            alert('this is the lowest: curDist=' +curDist);
                            lowestDist = curDist;
                            nearestSeg = seg;
                        }
                    }
                    alert('nearestSeg = ' + nearestSeg);
                    newComponents = new Array();
                    for(var newpt=0; newpt<=nearestSeg; newpt++)
                    {
                        alert('adding old component idx='+newpt);
                        newComponents.push(rte.components[newpt]);
                    }
                    // Now create a NEW geometry
                    // is ok, predrag is already new
                    alert('adding new componet');
                    newComponents.push (predrag);
                    for(var newpt=nearestSeg+1; newpt<rte.components.length; 
                            newpt++)
                    {
                        alert('adding old component after insert='+newpt);
                        newComponents.push(rte.components[newpt]);
                    }
                    rte.components = newComponents;
                    f.intersected[route].state = '';
                    f.intersected[route].style=lstyle;
                  if (rtes.length>=1)
                      rtes+=",";
                  rtes += f.intersected[route].fid;
                  if (prevs.length>=1)
                      prevs+=",";
                  prevs += nearestSeg;
                  alert("rtes="+rtes+" prevs="+prevs);
                  manRoutes.push(f.intersected[route]);
            }
                alert('/otv/route.php?action=insert&id='
                                +rtes+'&prev='+prevs+
                                '&new=' + predrag.photo_id);

                    // do server update
                            var rqst = new Ajax.Request
                                ('/otv/route.php?action=insert&id='
                                +rtes+'&prev='+prevs+
                                '&new=' + predrag.photo_id,
                { method: 'get',
                  manipulatedRoutes: manRoutes, 
                  insertedPoint: predrag,
                  insertedFeature: f,
                  operation: 'insert',
                  onSuccess: cb,
                  onFailure: failcb }
                  );
            georss.eraseFeatures([f]);
            f.geometry.x = predrag.x;
            f.geometry.y = predrag.y;
            georss.drawFeature(f);
            //f.inRoute = true;
            f.intersected.clear();
        }

        function removePhotoPointFromRoutes(photo_id)
        {
            for(var i=0; i<lineLayer.features.length; i++)
            {
                for(var j=0; j<lineLayer.features[i].geometry.components.length;
                    j++)
                {
                    if(lineLayer.features[i].geometry.components[j].photo_id&&
                        lineLayer.features[i].geometry.components[j].photo_id==
                        photo_id)
                    {
                        lineLayer.features[i].geometry.removePoint
                            (lineLayer.features[i].geometry.components[j]);
                        lineLayer.drawFeature(lineLayer.features[i]);
                        break;
                    }
                }
            }
        }

        function resetPhotoLines(f,origGeom)
        {
            for(var i=0; i<lineLayer.features.length; i++)
            {
                for(var j=0; j<lineLayer.features[i].geometry.components.length;
                    j++)
                {
                    if(lineLayer.features[i].geometry.components[j].photo_id&&
                        lineLayer.features[i].geometry.components[j].photo_id==
                        f.fid)
                    {
						lineLayer.features[i].geometry.components[j].x=
							origGeom.x;
						lineLayer.features[i].geometry.components[j].y=
							origGeom.y;
                        lineLayer.drawFeature(lineLayer.features[i]);
                        break;
                    }
                }
            }
        }

        function dist (x1,y1,x2,y2)
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
                    dist(px,py,xintersection,yintersection) :
                        99999999;
        }


            
