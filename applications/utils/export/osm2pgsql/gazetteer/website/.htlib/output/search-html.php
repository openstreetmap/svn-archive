<?php

?>
<html>
<head>
	<title>OpenStreetMap ByName: Search</title>

	<script src="OpenLayers.js"></script>
	<script src="http://www.openstreetmap.org/openlayers/OpenStreetMap.js"></script>
	<script src="prototype-1.6.0.3.js"></script>

	<style>
* {-moz-box-sizing: border-box;}
body {
  margin:0px;
  padding:0px;
  overflow: hidden;
  background:#ffffff;
  height: 100%;
  font: normal 12px/15px arial,sans-serif;
}
#seachheader {
  position:absolute;
  z-index:5;
  top:0px;
  left:0px;
  width:100%;
  height:38px;
  background:#F0F7FF;
  border-bottom: 2px solid #75ADFF;
}
#q {
  width:300px;
}
#seachheaderfade1, #seachheaderfade2, #seachheaderfade3, #seachheaderfade4{
  position:absolute;
  z-index:4;
  top:0px;
  left:0px;
  width:100%;
  opacity: 0.15;
  filter: alpha(opacity = 15);
  background:#000000;
  border: 1px solid #000000;
}
#seachheaderfade1{
  height:39px;
}
#seachheaderfade2{
  height:40px;
}
#seachheaderfade3{
  height:41px;
}
#seachheaderfade4{
  height:42px;
}
#searchresultsfade1, #searchresultsfade2, #searchresultsfade3, #searchresultsfade4 {
  position:absolute;
  z-index:2;
  top:0px;
  left:200px;
  height: 100%;
  opacity: 0.2;
  filter: alpha(opacity = 20);
  background:#ffffff;
  border: 1px solid #ffffff;
}
#searchresultsfade1{
  width:1px;
}
#searchresultsfade2{
  width:2px;
}
#searchresultsfade3{
  width:3px;
}
#searchresultsfade4{
  width:4px;
}

#searchresults{
  position:absolute;
  z-index:3;
  top:41px;
  width:200px;
  height: 100%;
  background:#ffffff;
  border: 1px solid #ffffff;
}
#map{
  position:absolute;
  z-index:1;
  top:38px;
  left:200px;
  width:100%;
  height:100%;
  background:#eee;
}
#report{
  position:absolute;
  z-index:2;
  top:38px;
  left:200px;
  width:100%;
  height:100%;
  background:#eee;
  font: normal 12px/15px arial,sans-serif;
  padding:20px;
}
#report table {
  margin-left:20px;
}
#report th {
  vertical-align:top;
  text-align:left;
}
#report td.button {
  text-align:right;
}
.result {
  margin:5px;
  margin-bottom:0px;
  padding:2px;
  padding-left:4px;
  padding-right:4px;
  border-radius: 5px;
  -moz-border-radius: 5px;
  -webkit-border-radius: 5px;
  background:#F0F7FF;
  border: 2px solid #D7E7FF;
  font: normal 12px/15px arial,sans-serif;
  cursor:pointer;
}
.result img{
  float:right;
}
.result .latlon{
  display: none;
}
.result .place_id{
  display: none;
}
.result .type{
  color: #ccc;
  text-align:center;
  font: normal 9px/10px arial,sans-serif;
  padding-top:4px;
}
.result .details, .result .details a{
  color: #ccc;
  text-align:center;
  font: normal 9px/10px arial,sans-serif;
  padding-top:4px;
}
.noresults{
  color: #000;
  text-align:center;
  font: normal 12px arial,sans-serif;
  padding-top:4px;
}
.disclaimer{
  color: #ccc;
  text-align:center;
  font: normal 9px/10px arial,sans-serif;
  padding-top:4px;
}
form{
  margin:0px;
  padding:0px;
}
	</style>

	<script type="text/javascript">
        
		var map;

		function handleResize()
		{
			if ($('searchresults'))
			{
				$('map').style.width = (document.documentElement.clientWidth > 0?document.documentElement.clientWidth:document.documentElement.offsetWidth) - 200;
				$('report').style.width = (document.documentElement.clientWidth > 0?document.documentElement.clientWidth:document.documentElement.offsetWidth) - 200;
			}
			else
			{
				$('map').style.width = (document.documentElement.clientWidth > 0?document.documentElement.clientWidth:document.documentElement.offsetWidth) - 0;
				$('map').style.left = 0;
			}
			
			$('map').style.height = (document.documentElement.clientHeight > 0?document.documentElement.clientHeight:document.documentElement.offsetHeight) - 38;
		}
		window.onresize = handleResize;

		function panToLatLon(lat,lon) {
			var lonLat = new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
			map.panTo(lonLat, <?php echo $iZoom ?>);
		}

		function panToLatLonZoom(lat,lon, zoom) {
			var lonLat = new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
			if (zoom != map.getZoom())
				map.setCenter(lonLat, zoom);
			else
				map.panTo(lonLat, 10);
		}

		function panToLatLonBoundingBox(lat,lon,minlat,maxlat,minlon,maxlon,points) {
		        var proj_EPSG4326 = new OpenLayers.Projection("EPSG:4326");
		        var proj_map = map.getProjectionObject();
                        map.zoomToExtent(new OpenLayers.Bounds(minlon,minlat,maxlon,maxlat).transform(proj_EPSG4326, proj_map));

                        var pointList = [];
                        var style = {
                                strokeColor: "#75ADFF",
                                fillColor: "#F0F7FF",
                                strokeWidth: 2,
                                strokeOpacity: 0.75,
                                fillOpacity: 0.75,
                        };
                        var proj_EPSG4326 = new OpenLayers.Projection("EPSG:4326");
                        var proj_map = map.getProjectionObject();
			if (points)
			{
				points.each(function(p){
					pointList.push(new OpenLayers.Geometry.Point(p[0],p[1]));
					});
        	                var linearRing = new OpenLayers.Geometry.LinearRing(pointList).transform(proj_EPSG4326, proj_map);;
                	        var polygonFeature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([linearRing]),null,style);
				vectorLayer.destroyFeatures();
                        	vectorLayer.addFeatures([polygonFeature]);
			}
			else
			{
				var lonLat = new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
				var point = new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat);
				var pointFeature = new OpenLayers.Feature.Vector(point,null,style);
				vectorLayer.destroyFeatures();
				vectorLayer.addFeatures([pointFeature]);
			}
		}

		function mapEventMove() {
			var proj = new OpenLayers.Projection("EPSG:4326");
			var bounds = map.getExtent();
			bounds = bounds.transform(map.getProjectionObject(), proj);
			$('viewbox').value = bounds.left+','+bounds.top+','+bounds.right+','+bounds.bottom;
		}

    function init() {
			handleResize();
			map = new OpenLayers.Map ("map", {
                controls:[
										new OpenLayers.Control.Navigation(),
										new OpenLayers.Control.PanZoomBar(),
										new OpenLayers.Control.MouseDefaults(),
										new OpenLayers.Control.MousePosition(),
										new OpenLayers.Control.Attribution()],
                maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34),
                maxResolution: 156543.0399,
                numZoomLevels: 19,
                units: 'm',
                projection: new OpenLayers.Projection("EPSG:900913"),
                displayProjection: new OpenLayers.Projection("EPSG:4326"),
                eventListeners: {
									"moveend": mapEventMove,
								}
            	} );
			map.addLayer(new OpenLayers.Layer.OSM.Mapnik("Mapnik"));

			var layer_style = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
			layer_style.fillOpacity = 0.2;
			layer_style.graphicOpacity = 1;
			vectorLayer = new OpenLayers.Layer.Vector("Points", {style: layer_style});
			map.addLayer(vectorLayer);
			
//			var lonLat = new OpenLayers.LonLat(<?php echo $fLon ?>, <?php echo $fLat ?>).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
//			map.setCenter (lonLat, <?php echo $iZoom ?>);
		}
		
	</script>
</head>

<body>

	<div id="seachheaderfade1"></div><div id="seachheaderfade2"></div><div id="seachheaderfade3"></div><div id="seachheaderfade4"></div>

	<div id="seachheader">
		<form>
			<table border="0" width="100%">
				<tr>
					<td valign="center" style="width:30px;"><img src="images/logo.gif"></td>
					<td valign="center" style="width:300px;"><input id="q" name="q" value="<?php echo htmlspecialchars($sQuery); ?>" style="width:300px;"><input type="hidden" id="viewbox" name="viewbox"></td>
					<td style="width:80px;"><input type="submit" value="Search"></td>
<?php if (CONST_Search_AreaPolygons) { ?>					<td style="width:200px;"><input type="checkbox" value="1" name="polygon" <?php if ($bShowPolygons) echo "checked"; ?>> Show Area Polygons</td>
<?php } ?>					<td style="text-align:right;"><?php if ($sQuery) { if ($sReportDescription) {?><div style="text-align:center;"><b>Thank you for your problem report</b></div><?php } else { ?><input type="button" value="Report Problem With Results" onclick="$('report').style.visibility=($('report').style.visibility=='hidden'?'visible':'hidden')"><?php }} ?></td>
				</tr>
			</table>
		</form>
	</div>

<?php
	if ($sQuery || sizeof($aSearchResults))
	{
?>
	<div id="searchresultsfade1"></div><div id="searchresultsfade2"></div><div id="searchresultsfade3"></div><div id="searchresultsfade4"></div>
	<div id="searchresults">
<?php
	foreach($aSearchResults as $iResNum => $aResult)
	{
		if (isset($aResult['aPointPolygon']))
		{
			echo '<div class="result" onClick="panToLatLonBoundingBox('.$aResult['lat'].', '.$aResult['lon'];
			echo ', '.$aResult['aPointPolygon']['minlat'];
			echo ', '.$aResult['aPointPolygon']['maxlat'];
			echo ', '.$aResult['aPointPolygon']['minlon'];
			echo ', '.$aResult['aPointPolygon']['maxlon'];
			echo ', '.javascript_renderData($aResult['aPolyPoints']);
			echo ');">';
		}
		elseif (isset($aResult['zoom']))
		{
			echo '<div class="result" onClick="panToLatLonZoom('.$aResult['lat'].', '.$aResult['lon'].', '.$aResult['zoom'].');">';
		}
		else
		{
			echo '<div class="result" onClick="panToLatLon('.$aResult['lat'].', '.$aResult['lon'].');">';
		}

		echo $aResult['icon'];
		echo ' <span class="name">'.$aResult['name'].'</span>';
		echo ' <span class="latlon">'.round($aResult['lat'],3).','.round($aResult['lat'],3).'</span>';
		echo ' <span class="place_id">'.$aResult['place_id'].'</span>';
		echo ' <span class="type">('.ucwords(str_replace('_',' ',$aResult['type'])).')</span>';
		echo ' <span class="details">(<a href="details.php?place_id='.$aResult['place_id'].'">details</a>)</span>';
		echo '</div>';
	}
	if (!sizeof($aSearchResults))
	{
		echo '<div class="noresults">No search results found</div>';
	}

?>
		<div class="disclaimer">Addresses and postcodes are approximate</div>
	<input type="button" value="Report Problem With Results" onclick="$('report').style.visibility=($('report').style.visibility=='hidden'?'visible':'hidden')">
	</div>
<?php
}
?>

	<div id="map"></div>
	<div id="report" style="visibility:hidden;">
		<h2>Report a problem</h2>
		<p>Please use this form to report problems with the search results.  Of particular interest are items missing, but please also use this form to report any other problems.</p>
		<p>If your problem relates to the address of a particular search result please use the 'details' link to check how the address was generated before reporting a problem.</p>
		<p>If you are reporting a missing result please (if possible) include the OSM ID of the item you where expecting (i.e. node 422162)</p>
		<form method="post">
		<table>
		<tr><th>Your Query:</th><td><input type="hidden" name="report:query" value="<?php echo htmlspecialchars($sQuery); ?>" style="width:500px;"><?php echo htmlspecialchars($sQuery); ?></td></tr>
		<tr><th>Your Email Address(opt):</th><td><input type="text" name="report:email" value="" style="width:500px;"></td></tr>
		<tr><th>Description of Problem:</th><td><textarea name="report:description" style="width:500px;height:200px;"></textarea></td></tr>
		<tr><td colspan="2" class="button"><input type="button" value="Cancel" onclick="$('report').style.visibility='hidden'"><input type="submit" value="Report"></td></tr>
		</table>
		</form>
		<h2>Known Problems</h2>
		<ul>
		<li>Countries where missed out of the index</li>
		<li>Area Polygons relate to the search area - not the address area which would make more sense</li>
		</ul>
	</div>

	<script type="text/javascript">
init();
<?php
	foreach($aSearchResults as $iResNum => $aResult)
	{
		if ($aResult['aPolyPoints'])
		{
			echo 'panToLatLonBoundingBox('.$aResult['lat'].', '.$aResult['lon'];
			echo ', '.$aResult['aPointPolygon']['minlat'];
			echo ', '.$aResult['aPointPolygon']['maxlat'];
			echo ', '.$aResult['aPointPolygon']['minlon'];
			echo ', '.$aResult['aPointPolygon']['maxlon'];
			echo ', '.javascript_renderData($aResult['aPolyPoints']);
			echo ');'."\n";
		}
		else
		{
			echo 'panToLatLonZoom('.$fLat.', '.$fLon.', '.$iZoom.');'."\n";
		}
		break;
	}
	if (!sizeof($aSearchResults))
	{
		echo 'panToLatLonZoom('.$fLat.', '.$fLon.', '.$iZoom.');'."\n";
	}
?>
</script>
</body>

</html>
