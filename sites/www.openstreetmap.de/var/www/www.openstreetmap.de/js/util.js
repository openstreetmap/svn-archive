
function jumpTo(lon, lat, zoom) {
    var x = Lon2Merc(lon);
    var y = Lat2Merc(lat);
    map.setCenter(new OpenLayers.LonLat(x, y), zoom);
    return false;
}

function Lon2Merc(lon) {
    return 20037508.34 * lon / 180;
}

function Lat2Merc(lat) {
    var PI = 3.14159265358979323846;
    lat = Math.log(Math.tan( (90 + lat) * PI / 360)) / (PI / 180);
    return 20037508.34 * lat / 180;
}

function addMarker(layer, lon, lat, popupContentHTML) {
    var ll = new OpenLayers.LonLat(Lon2Merc(lon), Lat2Merc(lat));
    var feature = new OpenLayers.Feature(layer, ll); 
    feature.closeBox = true;
    feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(300, 180) } );
    feature.data.popupContentHTML = popupContentHTML;
    feature.data.overflow = "hidden";
            
    var size = new OpenLayers.Size(16, 16);
    var offset = new OpenLayers.Pixel(-(size.w/2), -(size.h/2));
    var icon = new OpenLayers.Icon('/img/reddot.png', size, offset);
    var marker = new OpenLayers.Marker(ll, icon);
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

function showLayer(l) {
    for (i=0; i < map.layers.length; i++) {
        if (!map.layers[i].isBaseLayer && map.layers[i] != l) {
            map.layers[i].setVisibility(false);
        }
    }
    l.setVisibility(true);
}

function startSearch() {
}

function endSearch() {
}

function doSearch() {
    new Ajax.Request('/geocoder/search', {
        asynchronous: true,
        evalScripts: true,
        onComplete: function(request){endSearch()},
        onLoading: function(request){startSearch()},
        parameters: Form.serialize(this)
    });
    return false;
}

