function osm_getTileURL(bounds) {
    var res = this.map.getResolution();
    var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();
    var limit = Math.pow(2, z);

    if (y < 0 || y >= limit) {
        return OpenLayers.Util.getImagesLocation() + "404.png";
    } else {
        x = ((x % limit) + limit) % limit;
        return this.url + z + "/" + x + "/" + y + "." + this.type;
    }
}
function mapOf(div, type, id) {
    var options = {
        displayProjection: new OpenLayers.Projection("EPSG:4326"),
        projection: new OpenLayers.Projection("EPSG:900913"),
        units: "m",
        numZoomLevels: 19,
        maxResolution: 156543.0339,
        maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34,
                                         20037508.34, 20037508.34),
        controls: [new OSMEditor.HashControl(null, "/static/map.html", {updateUrl: false}), new OpenLayers.Control.Navigation()]                                 
    };

    var map = new OpenLayers.Map(div, options);
    var mapnik = new OpenLayers.Layer.TMS(
        "OpenStreetMap (Mapnik)",
        "http://tile.openstreetmap.org/",
        {
            type: 'png', getURL: osm_getTileURL,
            displayOutsideMaxExtent: true,
            attribution: '<a href="http://www.openstreetmap.org/">OpenStreetMap</a>',
            buffer: 0
        }
    );
    map.addLayer(mapnik);
    var url = "/api/0.5/" + type + "/" + id;
    if (type != 'node') { url += '/full'; }
    var osm = new OpenLayers.Layer.GML("", url, {
        format: OpenLayers.Format.OSM,
        projection: map.displayProjection
    });
    osm.events.on({"loadend": function() { 
        this.map.zoomToExtent(this.getDataExtent()); 
        if (type == "way") {
            var f = this.features[0];
            switch (f.geometry.CLASS_NAME) { 
                case "OpenLayers.Geometry.LineString":
                    var start = new OpenLayers.Feature.Vector(f.geometry.components[0], null, {fillColor: 'green', pointRadius: 6 });
                    this.addFeatures(start);
                    var end = new OpenLayers.Feature.Vector(f.geometry.components[f.geometry.components.length-1], null, {fillColor: 'red', pointRadius: 6 });
                    this.addFeatures(end);
            }
        }     
    }, "scope": osm});
    map.addLayer(osm);
    osm.loadGML();
}
