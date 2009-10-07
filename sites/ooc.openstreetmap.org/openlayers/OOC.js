/**
 * Namespace: Util.OOC
 */
OpenLayers.Util.OOC = {};

/**
 * Constant: MISSING_TILE_URL
 * {String} URL of image to display for missing tiles
 */
OpenLayers.Util.OOC.MISSING_TILE_URL = "http://openstreetmap.org/openlayers/img/404.png";

/**
 * Property: originalOnImageLoadError
 * {Function} Original onImageLoadError function.
 */
OpenLayers.Util.OOC.originalOnImageLoadError = OpenLayers.Util.onImageLoadError;

/**
 * Function: onImageLoadError
 */
OpenLayers.Util.onImageLoadError = function() {
    if (this.src.match(/^http:\/\/npe\.openstreetmap\.org\//)) {
        this.src = OpenLayers.Util.OOC.MISSING_TILE_URL;
    } else {
        OpenLayers.Util.OOC.originalOnImageLoadError;
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
OpenLayers.Layer.NPE = OpenLayers.Class(OpenLayers.Layer.XYZ, {
    /**
     * Constructor: OpenLayers.Layer.NPE
     *
     * Parameters:
     * name - {String}
     * url - {String}
     * options - {Object} Hashtable of extra options to tag onto the layer
     */
    initialize: function(name, options) {
        var url = [
            "http://a.ooc.openstreetmap.org/npe/${z}/${x}/${y}.png",
            "http://b.ooc.openstreetmap.org/npe/${z}/${x}/${y}.png",
            "http://c.ooc.openstreetmap.org/npe/${z}/${x}/${y}.png"
        ];
        options = OpenLayers.Util.extend({
            numZoomLevels: 16,
            transitionEffect: "resize",
            sphericalMercator: true
        }, options);
        var newArguments = [name, url, options];
        OpenLayers.Layer.XYZ.prototype.initialize.apply(this, newArguments);
    },

    CLASS_NAME: "OpenLayers.Layer.NPE"
});
