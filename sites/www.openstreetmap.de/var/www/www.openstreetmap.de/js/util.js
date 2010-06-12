
var projmerc = new OpenLayers.Projection("EPSG:900913");
var proj4326 = new OpenLayers.Projection("EPSG:4326");

function parseParams(handler) {
    var perma = location.search.substr(1);
    if (perma != '') {
        paras = perma.split('&');
        for (var i = 0; i < paras.length; i++) {
            var p = paras[i].split('=');
            handler(p[0], p[1]);
        }
    }
}

function jumpTo(lon, lat, zoom) {
    var lonlat = new OpenLayers.LonLat(lon, lat);
    lonlat.transform(proj4326, projmerc);
    map.setCenter(lonlat, zoom);
    return false;
}

function makeIcon(url, x, y) {
    var size = new OpenLayers.Size(x, y);
    var offset = new OpenLayers.Pixel(-(size.w/2), -(size.h/2));
    return new OpenLayers.Icon(url, size, offset);
}

var icon = makeIcon('/img/localgroup.png', 16, 16);

function addMarker(layer, lon, lat, popupContentHTML) {
    var lonlat = new OpenLayers.LonLat(lon, lat);
    lonlat.transform(proj4326, projmerc);
    var feature = new OpenLayers.Feature(layer, lonlat); 
    feature.closeBox = true;
    feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(300, 180) } );
    feature.data.popupContentHTML = popupContentHTML;
    feature.data.overflow = "hidden";
            
    var marker = new OpenLayers.Marker(lonlat, icon.clone());
    marker.feature = feature;

    var markerClick = function(evt) {
        if (this.popup == null) {
            this.popup = this.createPopup(this.closeBox);
            map.addPopup(this.popup);
            this.popup.show();
        } else {
            this.popup.toggle();
        }
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("mousedown", feature, markerClick);

    layer.addMarker(marker);
}

function setMarker() {
    if (marker != null) {
        layer_marker.removeMarker(marker);
        marker.destroy();
    }
    var size = new OpenLayers.Size(21, 25);
    var offset = new OpenLayers.Pixel(-size.w/2-1, -size.h-1);
    var icon = new OpenLayers.Icon('/img/marker.png', size, offset);
    var lonlat = new OpenLayers.LonLat(mlon, mlat);
    marker = new OpenLayers.Marker(lonlat.transform(proj4326, projmerc), icon);
    layer_marker.addMarker(marker);
}

function updateMapKey(force) {
    if (force || jQuery('#mapkey_area iframe').size() > 0) {
        var layer = map.baseLayer.keyname;
        var zoom = map.getZoom();

        var mapkey = jQuery('#mapkey_area');
        mapkey.html('<iframe src="http://www.openstreetmap.org/key?layer=' + layer + '&zoom=' + zoom + '"/>');
    }
}

function mapMoved() {
    var lonlat = map.getCenter().clone();
    lonlat.transform(projmerc, proj4326);
    var pos = '?lon=' + lonlat.lon.toFixed(5) + '&lat=' + lonlat.lat.toFixed(5) + '&zoom=' + map.getZoom();
    jQuery('#editlink')[0].href = 'http://www.openstreetmap.org/edit' + pos;
    jQuery('#buglink')[0].href = 'http://www.openstreetbugs.org/' + pos;
    if (jQuery('#largemap').size() > 0) {
        jQuery('#largemap')[0].href = '/karte.html' + pos;
    }
}

