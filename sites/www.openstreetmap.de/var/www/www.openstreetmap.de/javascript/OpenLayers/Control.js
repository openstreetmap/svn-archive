/* Copyright (c) 2006 MetaCarta, Inc., published under a modified BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/repository-license.txt 
 * for the full text of the license. */


/**
* @class
*/
OpenLayers.Control = OpenLayers.Class.create();
OpenLayers.Control.prototype = {

    /** @type String */
    id: null,
    
    /** this gets set in the addControl() function in OpenLayers.Map
    * @type OpenLayers.Map */
    map: null,

    /** @type DOMElement */
    div: null,

    /** @type OpenLayers.Pixel */
    position: null,

    /** @type OpenLayers.Pixel */
    mouseDragStart: null,

    /**
     * @constructor
     * 
     * @param {Object} options
     */
    initialize: function (options) {
        OpenLayers.Util.extend(this, options);
        
        this.id = OpenLayers.Util.createUniqueID(this.CLASS_NAME + "_");
    },

    /**
     * 
     */
    destroy: function () {
        // eliminate circular references
        this.map = null;
    },

    /** Set the map property for the control. This is done through an accessor
     *   so that subclasses can override this and take special action once 
     *   they have their map variable set. 
     * 
     * @param {OpenLayers.Map} map
     */
    setMap: function(map) {
        this.map = map;
    },
  
    /**
     * @param {OpenLayers.Pixel} px
     *
     * @returns A reference to the DIV DOMElement containing the control
     * @type DOMElement
     */
    draw: function (px) {
        if (this.div == null) {
            this.div = OpenLayers.Util.createDiv();
            this.div.id = this.id;
            this.div.className = 'olControl';
        }
        if (px != null) {
            this.position = px.clone();
        }
        this.moveTo(this.position);        
        return this.div;
    },

    /**
     * @param {OpenLayers.Pixel} px
     */
    moveTo: function (px) {
        if ((px != null) && (this.div != null)) {
            this.div.style.left = px.x + "px";
            this.div.style.top = px.x + "px";
        }
    },

    /** @final @type String */
    CLASS_NAME: "OpenLayers.Control"
};
