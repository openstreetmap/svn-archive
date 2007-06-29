var map;
var markers;
var popup;

function createMap(divName, centre, zoom) {
   OpenLayers.Util.onImageLoadError = function() {
      this.src = OpenLayers.Util.getImagesLocation() + "404.png";
   }

   map = new OpenLayers.Map(divName,
                            { maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
                              numZoomLevels: 19,
                              maxResolution: 156543,
                              units: 'm',
                              projection: "EPSG:41001" });

   var mapnik = new OpenLayers.Layer.TMS("Mapnik",
                                         "http://tile.openstreetmap.org/",
                                         { type: 'png', getURL: getTileURL, displayOutsideMaxExtent: true });
   map.addLayer(mapnik);

   var osmarender = new OpenLayers.Layer.TMS("Osmarender",
                                             "http://dev.openstreetmap.org/~ojw/Tiles/tile.php/",
                                             { type: 'png', getURL: getTileURL, displayOutsideMaxExtent: true });
   map.addLayer(osmarender);

   map.addControl(new OpenLayers.Control.LayerSwitcher());
   map.setCenter(centre, zoom);

   return map;
}

function getTileURL(bounds) {
   var res = this.map.getResolution();
   var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
   var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
   var z = this.map.getZoom();
   var limit = Math.pow(2, z);

   if (y < 0 || y >= limit)
   {
     return null;
   }
   else
   {
     x = ((x % limit) + limit) % limit;

     return this.url + z + "/" + x + "/" + y + "." + this.type;
   }
}

function addMarkerToMap(position, icon, description) {
   if (markers == null) {
      markers = new OpenLayers.Layer.Markers("markers");
      map.addLayer(markers);
   }

   var marker = new OpenLayers.Marker(position, icon);

   markers.addMarker(marker);

   if (description) {
      marker.events.register("click", marker, function() { openMapPopup(marker, description) });
   }

   return marker;
}

function openMapPopup(marker, description) {
//   var box = document.createElement("div");
//   box.innerHTML = description;
//   box.style.display = 'none';
//   box.style.width = "200px";
//   document.body.appendChild(box);

   closeMapPopup();

   popup = new OpenLayers.Popup.AnchoredBubble("popup", marker.lonlat,
                                               new OpenLayers.Size(200, 50),
                                               "<p>" + description + "</p>",
                                               marker.icon, true);
   popup.setBackgroundColor("#E3FFC5");
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

function mercatorToLonLat(merc) {
   var lon = (merc.lon / 20037508.34) * 180;
   var lat = (merc.lat / 20037508.34) * 180;
   var PI = 3.14159265358979323846;

   lat = 180/PI * (2 * Math.atan(Math.exp(lat * PI / 180)) - PI / 2);

   return new OpenLayers.LonLat(lon, lat);
}

function lonLatToMercator(ll) {
   var lon = ll.lon * 20037508.34 / 180;
   var lat = Math.log(Math.tan((90 + ll.lat) * PI / 360)) / (PI / 180);

   lat = lat * 20037508.34 / 180;

   return new OpenLayers.LonLat(lon, lat);
}

function scaleToZoom(scale) {
   return Math.log(360.0/(scale * 512.0)) / Math.log(2.0);
}
