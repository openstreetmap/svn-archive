
        var map; //complex object of type OpenLayers.Map
        var feature, p, rotateControl, selectControl, lineControl, dragControl;
        var georss, lineLayer;
        var wgs84;
        var panorama=null;
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
                            ({ url: "/rss.php",
                              format: new OpenLayers.Format.GeoRSSExt()
                              }),
                          'styleMap' : sm
                         } );

            map.addLayer(georss);

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
            ('/panorama/'+this.feature.fid+'/rotate/'+
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


                        },

                        onComplete: function(f)
                        {
                            var lonlat=new OpenLayers.LonLat
                                    (f.geometry.x,f.geometry.y).
                                    transform(map.getProjectionObject(),wgs84);

                            var rqst = new Ajax.Request
                                    ('/panorama.php?id='+f.fid+
                                    '&x='+f.geometry.x+'&y='+f.geometry.y+
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
            );

            selectControl = new OpenLayers.Control.SelectFeature (
                [georss],
                {
                    //hover: true,
                    onSelect: onFeatureSelect,
                    onUnselect: onFeatureUnselect
                }
            );

            map.addControl(rotateControl);
            map.addControl(selectControl);
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
            if(mode==0)
            {
                var id = feature.fid;
                $('map').style.width='0px';
                $('backtomap').style.visibility='visible';
                $('pancanvas').style.visibility='visible';
                //$('pancanvas').style.height='400px';
                $('pancanvas').height = 400;
                panorama = new PanController(feature,'status');
            }
            else
            {
                var popup = 
                    new OpenLayers.Popup.FramedCloud
                    ( null, new OpenLayers.LonLat
                        (feature.geometry.x,feature.geometry.y),   
                        new OpenLayers.Size(400,800),
                    "<img src='/panorama.php?id="+feature.fid+"&resize=40' />",
                    null, true);
                map.addPopup(popup);
            }
        }

        function onFeatureUnselect(feature)
        {
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
            panorama=null;
            $('backtomap').style.visibility='hidden';
        }

        function modeSet(obj)
        {

            //mode = $('mainctrl').value;
            mode = obj.id.substr(4);

            // rotate
            if(mode==2)
            {
                rotateControl.activate();
                selectControl.deactivate();
                dragControl.deactivate();
            }
            // view
            else if (mode==0 || mode==1)
            {
                rotateControl.deactivate();
                selectControl.activate();
                dragControl.deactivate();
            }
            // drag features
            else if (mode==3)
            {
                rotateControl.deactivate();
                selectControl.deactivate();
                dragControl.activate();
            }
            // anything else
            else 
            {
                rotateControl.deactivate();
                selectControl.deactivate();
                dragControl.deactivate();
            }
        }
        function cb(xmlHTTP)
        {
            alert('successfully performed a '+xmlHTTP.request.options.operation+
                ' operation');
            switch(xmlHTTP.request.options.operation)
            {
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
                case 'rotate':
                    f.attributes.direction = f.originalAngle;
                    georss.drawFeature(f);
                    break;
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


            
