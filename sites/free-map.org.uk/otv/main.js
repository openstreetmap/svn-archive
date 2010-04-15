
        var map; //complex object of type OpenLayers.Map
        var feature, p, control, selectControl;
        var georss;
        var wgs84;


        function onFeatureSelect(feature)
        {
			$('map').style.width='0px';
			$('photodiv').style.width='1024px';
            $('panimg').src='/otv/panorama/'+feature.fid;
			$('mainctrl').onclick = onPanoramaClose;
			$('mainctrl').value='Map';
        }

        function onPanoramaClose()
        {
			$('map').style.width='1024px';
			$('photodiv').style.width='0px';
			$('mainctrl').value =
				(selectControl.active===true ? "Align photos" : "View photos");
			$('mainctrl').onclick = modeSet;
        }

		function modeSet()
		{
			if(selectControl.active===true)
			{
            	control.activate();
            	selectControl.deactivate();
				$('mainctrl').value='View photos';
			}
			else
			{
            	control.deactivate();
            	selectControl.activate();
				$('mainctrl').value='Align photos';
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
                    ("GeoRSS",
                        { strategies: [new OpenLayers.Strategy.BBOX()],
                        protocol: new OpenLayers.Protocol.HTTP 
                            ({ url: "/otv/rss.php",
                              format: new OpenLayers.Format.GeoRSS() }),
                          'styleMap' : sm,
                          'projection': wgs84
                         } );

            map.addLayer(georss);
            map.events.register('click',map,
                    function(e) { 
                        var featurePos = this.getLonLatFromViewPortPx(e.xy).
                                transform( this.getProjectionObject(),wgs84);

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
                    hover: true,
                    onSelect: onFeatureSelect
                }
            );
            map.addControl(control);
            map.addControl(selectControl);

            if( ! map.getCenter() ){
                var lonLat = new 
                OpenLayers.LonLat(lon, lat).transform
                (wgs84,
                map.getProjectionObject());
                map.setCenter (lonLat, zoom);
            }

            control.deactivate();
            selectControl.activate();
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

