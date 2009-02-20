/**
 * Namespace: Util.NPE
 */
OpenLayers.Util.NPE = {};

/**
 * Constant: MISSING_TILE_URL
 * {String} URL of image to display for missing tiles
 */
OpenLayers.Util.NPE.MISSING_TILE_URL = "http://openstreetmap.org/openlayers/img/404.png";

/**
 * Property: originalOnImageLoadError
 * {Function} Original onImageLoadError function.
 */
OpenLayers.Util.NPE.originalOnImageLoadError = OpenLayers.Util.onImageLoadError;

/**
 * Function: onImageLoadError
 */
OpenLayers.Util.onImageLoadError = function() {
    if (this.src.match(/^http:\/\/npe\.openstreetmap\.org\//)) {
        this.src = OpenLayers.Util.NPE.MISSING_TILE_URL;
    } else {
        OpenLayers.Util.NPE.originalOnImageLoadError;
    }
};

/**
 * @requires OpenLayers/Layer/TMS.js
 *
 * Class: OpenLayers.Layer.NPE
 *
 * Inherits from:
 *  - <OpenLayers.Layer.TMS>
 */
OpenLayers.Layer.NPE = OpenLayers.Class(OpenLayers.Layer.TMS, {
    /**
     * Constructor: OpenLayers.Layer.NPE
     *
     * Parameters:
     * name - {String}
     * url - {String}
     * options - {Object} Hashtable of extra options to tag onto the layer
     */
    initialize: function(name, url, options) {
        options = OpenLayers.Util.extend({
            maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
            maxResolution: 156543,
            units: "m",
            projection: "EPSG:900913",
            numZoomLevels: 16,
            transitionEffect: "resize"
        }, options);
        var newArguments = [name, "http://npe.openstreetmap.org/", options];
        OpenLayers.Layer.TMS.prototype.initialize.apply(this, newArguments);
    },

    /**
     * Method: getUrl
     *
     * Parameters:
     * bounds - {<OpenLayers.Bounds>}
     *
     * Returns:
     * {String} A string with the layer's url and parameters and also the
     *          passed-in bounds and appropriate tile size specified as
     *          parameters
     */
    getURL: function (bounds) {
        var res = this.map.getResolution();
        var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
        var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
        var z = this.map.getZoom();
        var limit = Math.pow(2, z);

        if (y < 0 || y >= limit)
        {
            return OpenLayers.Util.NPE.MISSING_TILE_URL;
        }
        else
        {
            x = ((x % limit) + limit) % limit;

            var url = this.url;
            var path = z + "/" + x + "/" + y + ".png";

            if (url instanceof Array)
            {
                url = this.selectUrl(path, url);
            }

            return url + path;
        }
    },

    CLASS_NAME: "OpenLayers.Layer.NPE"
});
