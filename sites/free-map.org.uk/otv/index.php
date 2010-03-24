<?php
$lat = (isset($_GET['lat'])) ? $_GET['lat']:50.9;
$lon = (isset($_GET['lon'])) ? $_GET['lon']:-1.4;
$zoom = (isset($_GET['lon'])) ? $_GET['zoom']:14;
?>
<html>
<head>
    <title>OpenTrailView</title>

    <!-- based on example on osm wiki -->

    <!-- bring in the OpenLayers javascript library
         (here we bring it from the remote site, but you could
         easily serve up this javascript yourself) -->
    <script src="http://www.openlayers.org/api/OpenLayers.js"></script>
 
    <!-- bring in the OpenStreetMap OpenLayers layers.
         Using this hosted file will make sure we are kept up
         to date with any necessary changes -->
    <script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js">
    </script>
 
    <script type="text/javascript">
        // Start position for the map 
        var lat=<?php echo $lat; ?>;
        var lon=<?php echo $lon; ?>;
        var zoom=<?php echo $zoom;?>;
 
        var map; //complex object of type OpenLayers.Map
        var feature, p, control, selectControl;
        var georss;
        var wgs84;


        function onFeatureSelect(feature)
        {
            $('panimg').src='/otv/panorama/'+feature.fid;
        }

        function onFeatureUnselect(feature)
        {
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
                        $('status').innerHTML = 'Rotation=' +
                                this.feature.attributes.description;
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
                    onSelect: onFeatureSelect,
                    onUnselect: onFeatureUnselect
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

            control.activate();
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
            alert('error aligning photo: http code=' + xmlHTTP.status);
        }

    </script>
    <style type='text/css'>
	#maindiv { width:1024px; 
		border-style:solid; border-width:1px; padding:5px; }
	#map {  margin:100px }
    #photodiv { width:1024px; overflow:auto; margin-top:10px; height:200px }
    </style>
</head>
 
<!-- body.onload is called once the page is loaded (call the init function) -->
<body onload="init()">

<div id='maindiv'>
<h1>OpenTrailView</h1>
<p>StreetView for the world's trails and footpaths!</p>
<ul>
<li><a href='pansubmit.php'>Submit a panorama</a></li>
<li>Align a panorama by rotating the camera icons. The camera should face
the same direction as the centre of the panorama.</li>
<li>To view a panorama, move the mouse over the camera icon</li>
</ul>
<div id='status'></div>


<!-- define a DIV into which the map will appear. Make it take up the whole 
window -->
<div style="width:1024px; height:320px" id="map"></div>
<div id='photodiv'>
<img id='panimg' src='' alt='Panorama will appear here if you move the mouse
over a camera icon.' />
</div>

</div>

    

</body>
 
</html>
