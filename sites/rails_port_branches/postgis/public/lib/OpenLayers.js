/* Copyright (c) 2006 MetaCarta, Inc., published under the BSD license.
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the full
 * text of the license. */
////
/// This blob sucks in all the files in uncompressed form for ease of use
///

OpenLayers = new Object();

OpenLayers._scriptName = ( 
    typeof(_OPENLAYERS_SFL_) == "undefined" ? "lib/OpenLayers.js" 
                                            : "OpenLayers.js" );

OpenLayers._getScriptLocation = function () {
    var scriptLocation = "";
    var SCRIPT_NAME = OpenLayers._scriptName;
 
    var scripts = document.getElementsByTagName('script');
    for (var i = 0; i < scripts.length; i++) {
        var src = scripts[i].getAttribute('src');
        if (src) {
            var index = src.lastIndexOf(SCRIPT_NAME); 
            // is it found, at the end of the URL?
            if ((index > -1) && (index + SCRIPT_NAME.length == src.length)) {  
                scriptLocation = src.slice(0, -SCRIPT_NAME.length);
                break;
            }
        }
    }
    return scriptLocation;
}

/*
  `_OPENLAYERS_SFL_` is a flag indicating this file is being included
  in a Single File Library build of the OpenLayers Library.

  When we are *not* part of a SFL build we dynamically include the
  OpenLayers library code.

  When we *are* part of a SFL build we do not dynamically include the 
  OpenLayers library code as it will be appended at the end of this file.
*/
if (typeof(_OPENLAYERS_SFL_) == "undefined") {
    /*
      The original code appeared to use a try/catch block
      to avoid polluting the global namespace,
      we now use a anonymous function to achieve the same result.
     */
    (function() {
    var jsfiles=new Array(
        "Prototype.js", 
        "Rico/Corner.js",
        "Rico/Color.js",
        "OpenLayers/Util.js",
        "OpenLayers/Ajax.js",
        "OpenLayers/Events.js",
        "OpenLayers/Map.js",
        "OpenLayers/Layer.js",
        "OpenLayers/Icon.js",
        "OpenLayers/Marker.js",
        "OpenLayers/Popup.js",
        "OpenLayers/Tile.js",
        "OpenLayers/Feature.js",
        "OpenLayers/Feature/WFS.js",
        "OpenLayers/Tile/Image.js",
        "OpenLayers/Tile/WFS.js",
//        "OpenLayers/Layer/Google.js",
//        "OpenLayers/Layer/VirtualEarth.js",
//        "OpenLayers/Layer/Yahoo.js",
        "OpenLayers/Layer/Grid.js",
        "OpenLayers/Layer/KaMap.js",
        "OpenLayers/Layer/Markers.js",
        "OpenLayers/Layer/Text.js",
        "OpenLayers/Layer/WMS.js",
        "OpenLayers/Layer/WFS.js",
        "OpenLayers/Layer/WMS/Untiled.js",
        "OpenLayers/Popup/Anchored.js",
        "OpenLayers/Popup/AnchoredBubble.js",
        "OpenLayers/Control.js",
        "OpenLayers/Control/MouseDefaults.js",
        "OpenLayers/Control/MouseToolbar.js",
        "OpenLayers/Control/KeyboardDefaults.js",
        "OpenLayers/Control/PanZoom.js",
        "OpenLayers/Control/PanZoomBar.js",
        "OpenLayers/Control/LayerSwitcher.js"
    ); // etc.

    var allScriptTags = "";
    var host = OpenLayers._getScriptLocation() + "lib/";

    // check to see if prototype.js was already loaded
    //  if so, skip the first dynamic include 
    //
    var start=1;
    try { x = Prototype; }
    catch (e) { start=0; }

    for (var i = start; i < jsfiles.length; i++) {
        var currentScriptTag = "<script src='" + host + jsfiles[i] + "'></script>"; 
        allScriptTags += currentScriptTag;
    }
    document.write(allScriptTags);
    })();
}
