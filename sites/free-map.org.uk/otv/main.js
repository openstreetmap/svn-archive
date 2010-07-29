
        var map; //complex object of type OpenLayers.Map
        var feature, p, control, selectControl, lineControl;
        var georss, lineLayer;
        var wgs84;
        var pv=null;
        var currentRoute = null;


        function onFeatureSelect(feature)
        {
            $('map').style.width='0px';
            $('photodiv').style.width='1024px';
            $('photodiv').style.visibility='visible';
            //$('panimg').src='/otv/panorama/'+feature.fid;
            // do panorama instead
            pv=new JSPanoViewer({
                containerId: 'photodiv',
                mode: 1, 
                hFovDst : 90,
                hFovSrc : 360,
                cssWidth : '1024px',
                cssHeight : '400px'
            } );
            pv.loadImage('/otv/panorama/'+feature.fid,null);
            /*
            $('mainctrl').onclick = onPanoramaClose;
            $('mainctrl').value='Map';
            */
            $('backtomap').style.visibility='visible';
        }

        function onPanoramaClose()
        {
            $('map').style.width='1024px';
            $('photodiv').style.width='0px';
            $('photodiv').style.visibility='hidden';
            pv=null;
            /*
            $('mainctrl').value =
                (selectControl.active===true ? "Align photos" : "View photos");
            $('mainctrl').onclick = modeSet;
            */
            $('backtomap').style.visibility='hidden';
        }

        function modeSet()
        {
            if($('mainctrl').value=='Align photos')
            {
                control.activate();
                selectControl.deactivate();
                lineControl.deactivate();
                //$('mainctrl').value='View photos';
            }
            else if ($('mainctrl').value=='View photos')
            {
                control.deactivate();
                selectControl.activate();
                lineControl.deactivate();
                //$('mainctrl').value='Align photos';
            }
            else if ($('mainctrl').value=='Connect photos')
            {
                control.deactivate();
                selectControl.deactivate();
                lineControl.activate();
                currentRoute = new PanoRoute('/otv/route.php',
                                            addRouteToVectorLayer);
            }
        }

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
 

            // Define the map layer
            var layerFreemap = new OpenLayers.Layer.OSM.Mapnik("OSM");
            /*
                ("Freemap [new]",
                "http://www.free-map.org.uk/images/tiles/"+
                "${z}/${x}/${y}.png",{numZoomLevels:15} );
            */
            map.addLayer(layerFreemap);

            var featurePos = new OpenLayers.LonLat(-0.72,51.05).
                transform(wgs84,
                map.getProjectionObject());
            p=new OpenLayers.Geometry.Point(featurePos.lon,featurePos.lat);

            // Vector layer test
            var vecLayer = new OpenLayers.Layer.Vector ("Vector Layer");
            feature = new OpenLayers.Feature.Vector
                (p, null,
                    { externalGraphic: 'images/cam.png', graphicHeight:16 ,
                        graphicWidth:16, rotation:0 } );
            vecLayer.addFeatures(feature);
            //map.addLayer(vecLayer);

            var ctxt = {
                getRotation: function(feature)
                {
                    /* quick hack for the moment: put the rotation in the
                       description tag. Really need to subclass
                       OpenLayers.Format.GeoRSS to read geo:dir */

                    return feature.attributes.description;
                    /*
                    return (feature.attributes.rotation===null) ? 0:
                            feature.attributes.rotation ;
                    */
                }
            };

            var style = new OpenLayers.Style (
                    { externalGraphic: 'images/cam.png',
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
                              format: new OpenLayers.Format.GeoRSS() }),
                          'styleMap' : sm,
                          'projection': wgs84
                         } );

            lineLayer = new OpenLayers.Layer.Vector("Lines");
            
            map.addLayer(georss);
            map.addLayer(lineLayer);

            map.events.register('click',map,
                    function(e) { 
                        var featurePos = this.getLonLatFromViewPortPx(e.xy).
                                transform( this.getProjectionObject(),wgs84);

                        if(uploading)
                        {
                        var latEl=(uploading) ?
                            $('iframe1').
                            contentDocument.getElementById('ifr_lat'): 
                            $('lat');
                        var lonEl=(uploading) ?
                            $('iframe1').
                            contentDocument.getElementById('ifr_lon'): 
                            $('lon');
                        latEl.value  = featurePos.lat;
                        lonEl.value  = featurePos.lon;
                        }
                    });
            control = new OpenLayers.Control.DragFeature (
                georss,
                {
                    moveFeature: function(pixel)
                    {

                        var angle=10;

                        /*
                        this.feature.attributes.rotation = 
                            this.feature.attributes.rotation===null ?
                                angle:
                                (this.feature.attributes.rotation < 360-angle ?
                                this.feature.attributes.rotation+angle: 0);
                        */

                        var d = this.feature.attributes.description;

                        this.feature.attributes.description = 
                            this.feature.attributes.description===null ?
                                angle:
                                (d < 360-angle ?  d+10: 0);
                        
                        this.layer.drawFeature(this.feature);
                    },

                    doneDragging: function()
                    {
                        // AJAX request to update the rotation...
                         OpenLayers.loadURL
                            ("/otv/panorama/"
                                +this.feature.fid+ "/rotate/"+
                                this.feature.attributes.description,'',
                                null,cb,failcb);
                    }
                }
            );

            selectControl = new OpenLayers.Control.SelectFeature (
                georss,
                {
                    //hover: true,
                    onSelect: onFeatureSelect
                }
            );

            lineControl = new OpenLayers.Control.DrawFeature(
                lineLayer,OpenLayers.Handler.Path);

            var handler = new OpenLayers.Handler.Path
                (lineControl,{point: pointHandler, done:isDone } );
            lineControl.handler = handler;

            map.addControl(control);
            map.addControl(selectControl);
            map.addControl(lineControl);

            if( ! map.getCenter() ){
                var lonLat = new 
                OpenLayers.LonLat(lon, lat).transform
                (wgs84,
                map.getProjectionObject());
                map.setCenter (lonLat, zoom);
            }

            control.deactivate();
            selectControl.activate();

            $('backtomap').onclick = onPanoramaClose;
            $('backtomap').style.visibility='hidden';
        }
        

        /*
        for(var count=0; count<georss.features.length; count++)
        {
            for(a in georss.features[count].attributes)
                alert('a=' +a+' value='+georss.features[count].attributes[a]);
        }
        */

        function cb(xmlHTTP)
        {
            alert('successfully aligned photo');
        }

        function failcb(xmlHTTP)
        {
            if(xmlHTTP.status==401)
            {
                alert("Log in to orientate photos. You can orientate "+
                    "other people's but you must be logged in.");
            }
            else
            {
                alert('Internal error: http code=' + xmlHTTP.status);
            }
        }

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
                    alert ('found: panorama ' + count);
                    currentRoute.panoramas.push(georss.features[count]);
                }
            }
        }

        function isDone(g)
        {
            // show the geometry permanently
            currentRoute.upload();
        }

        function addRouteToVectorLayer(panoramas)
        {
            var style =   {  strokeWidth: 5, strokeColor: 'yellow'};
            var geom = new OpenLayers.Geometry.LineString();
            for(var count=0; count<panoramas.length; count++)
            {
                geom.addPoint(panoramas[count].geometry);
            }
            var f = new OpenLayers.Feature.Vector();
            f.style=style;
            f.geometry = geom;
            lineLayer.addFeatures([f]);
            // clear() doesn't work without Prototype, I think it must be
            // a prototype specific function
            currentRoute.panoramas = new Array();
        }

        function dist (x1,y1,x2,y2)
        {
            var dx = x2-x1, dy=y2-y1;
            return Math.sqrt(dx*dx+dy*dy);
        }

