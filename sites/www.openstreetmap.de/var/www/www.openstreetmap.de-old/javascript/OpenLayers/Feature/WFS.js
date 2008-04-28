/* Copyright (c) 2006 MetaCarta, Inc., published under a modified BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/repository-license.txt 
 * for the full text of the license. */


/**
 * @class
 * 
 * @requires OpenLayers/Feature.js
 */
OpenLayers.Feature.WFS = OpenLayers.Class.create();
OpenLayers.Feature.WFS.prototype = 
  OpenLayers.Class.inherit( OpenLayers.Feature, {
      
    /** 
     * @constructor
     * 
     * @param {OpenLayers.Layer} layer
     * @param {XMLNode} xmlNode
     */
    initialize: function(layer, xmlNode) {
        var newArguments = arguments;
        var data = this.processXMLNode(xmlNode);
        newArguments = new Array(layer, data.lonlat, data)
        OpenLayers.Feature.prototype.initialize.apply(this, newArguments);
        this.createMarker();
        this.layer.addMarker(this.marker);
    },
    
    destroy: function() {
        if (this.marker != null) {
            this.layer.removeMarker(this.marker);  
        }
        OpenLayers.Feature.prototype.destroy.apply(this, arguments);
    },

    /**
     * @param {XMLNode} xmlNode
     * 
     * @returns Data Object with 'id', 'lonlat', and private properties set
     * @type Object
     */
    processXMLNode: function(xmlNode) {
        //this should be overridden by subclasses
        // must return an Object with 'id' and 'lonlat' values set
        var point = OpenLayers.Ajax.getElementsByTagNameNS(xmlNode, "http://www.opengis.net/gml", "gml", "Point");
        var text  = OpenLayers.Util.getXmlNodeValue(OpenLayers.Ajax.getElementsByTagNameNS(point[0], "http://www.opengis.net/gml","gml", "coordinates")[0]);
        var floats = text.split(",");
        return {lonlat: new OpenLayers.LonLat(parseFloat(floats[0]),
                                              parseFloat(floats[1])),
                id: null};

    },
    
    /** @final @type String */
    CLASS_NAME: "OpenLayers.Feature.WFS"
});
  
  
  
  

