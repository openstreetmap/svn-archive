<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>OSM TMC in Deutschland</title>
    <meta name="ROBOTS" content="NOFOLLOW" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="icon" href="favicon.ico" type="image/x-icon" />
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" type="text/css" href="map.css" />

    <script type='text/javascript'><!--
<?php 
$lat=$_GET["lat"];
$lon=$_GET["lon"];
$zoom=$_GET["zoom"];

if ($lat =="") {
	$lat=53.475;
}
if ($lon =="") {
	$lon=9.90;
}
if ($zoom =="") {
	$zoom=14;
}

include "function.php";
?>
var lon=<? print $lon ?>;
var lat=<? print $lat ?>;
var zoom=<? print $zoom ?>;

//
var PI = 3.14159265358979323846;
lon_map = lon * 20037508.34 / 180;
lat_map = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);
lat_map = lat_map * 20037508.34 / 180;
--></script>

<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"></script>



    <script type="text/javascript" src="MarkerGrid.js"></script>

    <script type="text/javascript" src="MarkerTile.js"></script>

    <script type="text/javascript" src="bounds.js"></script>

    <script type="text/javascript"><!--
var map;
var POI;
function get_osm_url (bounds) {
var res = this.map.getResolution();
var x = Math.round ((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
var y = Math.round ((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
var z = this.map.getZoom();
var path =  z + "/" + x + "/" + y ;
var url = this.url;
if (url instanceof Array) {
url = this.selectUrl(path, url);
}
return url + path;
}
function get_poi_url (bounds) {
//bounds = this.adjustBounds(bounds);
var res = this.map.getResolution();
var z = this.map.getZoom();
var path = "?z=" + z
+ "&l=" + getLeft(bounds)
+ "&t=" + getTop(bounds)
+ "&r=" + getRight(bounds)
+ "&b=" + getBottom(bounds);

var url = "features.php";
return url + path;
}
function init()
{
OpenLayers.Util.onImageLoadErrorColor= "#E7FFC2";
OpenLayers.Util.onImageLoadError = function()
{
this.style.backgroundColor= null;
this.src = "img/my404t.png";
}
//45.93591170013053,13.524035160395303
//51.08285790895948,26.004501207203198
//5770084.965702645,1505488.7073159674
//6635963.434452645,2894807.8323159674
map = new OpenLayers.Map( "map",
{ 
controls: [new OpenLayers.Control.Permalink(),
				new OpenLayers.Control.MouseDefaults(),
				new OpenLayers.Control.LayerSwitcher(),
	                        new OpenLayers.Control.MousePosition(),
				new OpenLayers.Control.PanZoomBar()],
maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
numZoomLevels:17, maxResolution:156543, units:'m', projection: "EPSG:41001",
 displayProjection: new OpenLayers.Projection("EPSG:4326")

} );


 layerTilesAtHome = new OpenLayers.Layer.OSM.Osmarender("Osmarender");
            map.addLayer(layerTilesAtHome);
 layerMapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik");
            map.addLayer(layerMapnik);


// create POI layer
POI = new OpenLayers.Layer.MarkerGrid( "POI",
{
type:'txt',
getURL: get_poi_url,
buffer: 0
});
POI.setIsBaseLayer(false);
POI.setVisibility(true);
map.addLayer( POI);  
map.addControl(new OpenLayers.Control.LayerSwitcher());

map.setCenter(new OpenLayers.LonLat(lon_map, lat_map), zoom);
}
// -->
    </script>

</head>
<body onload='init()' style="margin: 0px; padding: 0px" topmargin="0" leftmargin="0">
    <table style="width: 100%; height: 100%" border="0px" cellspacing="0px" cellpadding="0px">
        <tr>
            <td style="width: 100%;" id="map_header"><b>TMC Landkarte Deutschland / OpenStreetMap.org / Openlayers.org - <a href="area.php">Nach Gebieten</a> <a href="http://wiki.openstreetmap.org/wiki/DE:TMC">TMC im OSM Wiki</a></b><iframe id="HiFrame" src="about:blank" style="display: none; visibility: hidden; width: 0; height: 0;"> </iframe>
<script type="text/javascript">
			function josm(url)
			{
				var hiFrame = document.getElementById('HiFrame');
				if(hiFrame)
				{
					hiFrame.src = url;
					return false;
				}
				
				return true;
			}
</script>
</td>
        </tr>

        <tr>
            <td style="width: 100%; height: 100%">
                <div id="mapborder" style="width: 100%; height: 100%">
                    <div id="map">
                    </div>
                </div>
            </td>
        </tr>
        <tr>

            <td>
                <div id="mapfooter">
                    Copyright &copy; 2005-2010, various <a href="http://www.openstreetmap.org/">OpenStreetMap</a>
                    contributors. <b>Some rights reserved</b>. Licensed as Creative Commons <a href="http://creativecommons.org/licenses/by-sa/2.0/">
                        CC-BY-SA 2.0</a> Stand:
<?php
$query="select MAX(ts) as t from osm";
$result=mysql_query($query);
 $ts=mysql_result($result,$i,"t");
print $ts;
?>

                </div>
            </td>
        </tr>
    </table>
</body>
</html>
