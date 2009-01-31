var epsg4326 = new OpenLayers.Projection("EPSG:4326");
var map;
var markers;
var popup;

var nonamekeys = {
   'openstreetmap.org': '2f59745a6b525b4ebdb100891d5b6711',
   'www.openstreetmap.org': 'fd093e52f0965d46bb1c6c6281022199',
   'openstreetmap.com': '4c60e7f5f31c576a9bb8da71c8d61152',
   'www.openstreetmap.com': '142f25a0770a51a9a400b3513834a199',
   'openstreetmap.net': '687c58fd1d715596bfc94abe653d8ac0',
   'www.openstreetmap.net': '0bd1654141c85d30b9c2ccdb5302f2e4'
};

OpenLayers._getScriptLocation = function () {
  // Should really have this file as an erb, so that this can return 
  // the real rails root
   return "/openlayers/";
}

function createMap(divName, options) {
   options = options || {};

   map = new OpenLayers.Map(divName, {
      controls: options.controls || [
         new OpenLayers.Control.ArgParser(),
         new OpenLayers.Control.Attribution(),
         new OpenLayers.Control.LayerSwitcher(),
         new OpenLayers.Control.Navigation(),
         new OpenLayers.Control.PanZoomBar(),
         new OpenLayers.Control.ScaleLine()
      ],
      units: "m",
      maxResolution: 156543.0339,
      numZoomLevels: 20,
      displayProjection: new OpenLayers.Projection("EPSG:4326")
   });

   var mapnik = new OpenLayers.Layer.OSM.Mapnik("Mapnik", {
      displayOutsideMaxExtent: true,
      wrapDateLine: true
   });
   map.addLayer(mapnik);

   var osmarender = new OpenLayers.Layer.OSM.Osmarender("Osmarender", {
      displayOutsideMaxExtent: true,
      wrapDateLine: true
   });
   map.addLayer(osmarender);

   var cyclemap = new OpenLayers.Layer.OSM.CycleMap("Cycle Map", {
      displayOutsideMaxExtent: true,
      wrapDateLine: true
   });
   map.addLayer(cyclemap);

   var nonamekey = nonamekeys[document.domain];
   var noname = new OpenLayers.Layer.OSM("NoName", [
      "http://a.tile.cloudmade.com/" + nonamekey + "/3/256/",
      "http://b.tile.cloudmade.com/" + nonamekey + "/3/256/",
      "http://c.tile.cloudmade.com/" + nonamekey + "/3/256/"
   ], {
      displayOutsideMaxExtent: true,
      wrapDateLine: true,
      numZoomLevels: 19
   });
   map.addLayer(noname);

   var maplint = new OpenLayers.Layer.OSM.Maplint("Maplint", {
      displayOutsideMaxExtent: true,
      wrapDateLine: true
   });
   map.addLayer(maplint);

   var numZoomLevels = Math.max(mapnik.numZoomLevels, osmarender.numZoomLevels);
   markers = new OpenLayers.Layer.Markers("Markers", {
      displayInLayerSwitcher: false,
      numZoomLevels: numZoomLevels,
      maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
      maxResolution: 156543,
      units: "m",
      projection: "EPSG:900913"
   });
   map.addLayer(markers);

   return map;
}

function getArrowIcon() {
   var size = new OpenLayers.Size(25, 22);
   var offset = new OpenLayers.Pixel(-30, -27);
   var icon = new OpenLayers.Icon("/images/arrow.png", size, offset);

   return icon;
}

function addMarkerToMap(position, icon, description) {
   var marker = new OpenLayers.Marker(position.clone().transform(epsg4326, map.getProjectionObject()), icon);

   markers.addMarker(marker);

   if (description) {
      marker.events.register("click", marker, function() { openMapPopup(marker, description) });
   }

   return marker;
}

function openMapPopup(marker, description) {
   closeMapPopup();

   popup = new OpenLayers.Popup.AnchoredBubble("popup", marker.lonlat, null,
                                               description, marker.icon, true);
   popup.setBackgroundColor("#E3FFC5");
   popup.autoSize = true;
   map.addPopup(popup);

   return popup;
}

function closeMapPopup() {
   if (popup) {
      map.removePopup(popup);
      delete popup;
   }
}

function removeMarkerFromMap(marker){
   markers.removeMarker(marker);
}

function getMapCenter(center, zoom) {
   return map.getCenter().clone().transform(map.getProjectionObject(), epsg4326);
}

function setMapCenter(center, zoom) {
   zoom = parseInt(zoom);
   var numzoom = map.getNumZoomLevels();
   if (zoom >= numzoom) zoom = numzoom - 1;
   map.setCenter(center.clone().transform(epsg4326, map.getProjectionObject()), zoom);
}

function setMapExtent(extent) {
   map.zoomToExtent(extent.clone().transform(epsg4326, map.getProjectionObject()));
}

function getMapExtent(extent) {
   return map.getExtent().clone().transform(map.getProjectionObject(), epsg4326);
}

function getEventPosition(event) {
   return map.getLonLatFromViewPortPx(event.xy).clone().transform(map.getProjectionObject(), epsg4326);
}

function getMapLayers() {
   var layerConfig = "";

   for (var layers = map.getLayersBy("isBaseLayer", true), i = 0; i < layers.length; i++) {
      layerConfig += layers[i] == map.baseLayer ? "B" : "0";
   }

   for (var layers = map.getLayersBy("isBaseLayer", false), i = 0; i < layers.length; i++) {
      layerConfig += layers[i].getVisibility() ? "T" : "F";
   }

   return layerConfig;
}

function setMapLayers(layerConfig) {
   var l = 0;

   for (var layers = map.getLayersBy("isBaseLayer", true), i = 0; i < layers.length; i++) {
      var c = layerConfig.charAt(l++);

      if (c == "B") {
         map.setBaseLayer(layers[i]);
      }
   }

   while (layerConfig.charAt(l) == "B" || layerConfig.charAt(l) == "0") {
      l++;
   }

   for (var layers = map.getLayersBy("isBaseLayer", false), i = 0; i < layers.length; i++) {
      var c = layerConfig.charAt(l++);

      if (c == "T") {
         layers[i].setVisibility(true);
      } else if(c == "F") {
         layers[i].setVisibility(false);
      }
   }
}

function scaleToZoom(scale) {
   return Math.log(360.0/(scale * 512.0)) / Math.log(2.0);
}
