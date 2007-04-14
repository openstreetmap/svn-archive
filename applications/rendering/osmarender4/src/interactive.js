/*

Osmarender

interactive.js

*/

function fnResize() {
    fnResizeElement("gAttribution")
    fnResizeElement("gLicense")
    fnResizeElement("gZoomIn")
    fnResizeElement("gZoomOut")
}


function fnResizeElement(e) {
    //
    var oSVG,scale,currentTranslateX,currentTranslateY,oe
    //
    oSVG=document.rootElement
    scale=1/oSVG.currentScale
    currentTranslateX=oSVG.currentTranslate.x
    currentTranslateY=oSVG.currentTranslate.y
    oe=document.getElementById(e)
    if (oe) oe.setAttributeNS(null,"transform","scale("+scale+","+scale+") translate("+(-currentTranslateX)+","+(-currentTranslateY)+")")
}


function fnToggleImage(osmImage) {
    var xlink = 'http://www.w3.org/1999/xlink';
    ogThumbnail=document.getElementById('gThumbnail')
    if (ogThumbnail.getAttributeNS(null,"visibility")=="visible") fnHideImage()
    else {
        ogThumbnail.setAttributeNS(null,"visibility","visible")
        oThumbnail=document.getElementById('thumbnail')
        oThumbnail.setAttributeNS(xlink,"href",osmImage)
    }
}

function fnHideImage() {
    ogThumbnail=document.getElementById('gThumbnail')
    ogThumbnail.setAttributeNS(null,"visibility","hidden")
}


/* The following code originally written by Jonathan Watt (http://jwatt.org/), Aug. 2005 */

if (!window)
window = this;


function fnOnLoad(evt) {
    if (!document) window.document = evt.target.ownerDocument
}


/**
  * Event handlers to change the current user space for the zoom and pan
  * controls to make them appear to be scale invariant.
  */

function fnOnZoom(evt) {
    try {
        if (evt.newScale === undefined) throw 'bad interface'
        // update the transform list that adjusts for zoom and pan
        var tlist = document.getElementById('staticElements').transform.baseVal
        tlist.getItem(0).setScale(1/evt.newScale, 1/evt.newScale)
        tlist.getItem(1).setTranslate(-evt.newTranslate.x, -evt.newTranslate.y)
        }
    catch (e) {
        // work around difficiencies in non-moz implementations (some don't
        // implement the SVGZoomEvent or SVGAnimatedTransform interfaces)
        var de = document.documentElement
        var tform = 'scale(' + 1/de.currentScale + ') ' + 'translate(' + (-de.currentTranslate.x) + ', ' + (-de.currentTranslate.y) + ')'
        document.getElementById('staticElements').setAttributeNS(null, 'transform', tform)
        }
    }


function fnOnScroll(evt) {
    var ct = document.documentElement.currentTranslate
    try {
        // update the transform list that adjusts for zoom and pan
        var tlist = document.getElementById('staticElements').transform.baseVal
        tlist.getItem(1).setTranslate(-ct.x, -ct.y)
        }
    catch (e) {
        // work around difficiencies in non-moz implementations (some don't
        // implement the SVGAnimatedTransform interface)
        var tform = 'scale(' + 1/document.documentElement.currentScale + ') ' + 'translate(' + (-ct.x) + ', ' + (-ct.y) + ')';
        document.getElementById('staticElements').setAttributeNS(null, 'transform', tform)
        }
    }


function fnZoom(type) {
    var de = document.documentElement;
    var oldScale = de.currentScale;
    var oldTranslate = { x: de.currentTranslate.x, y: de.currentTranslate.y };
    var s = 2;
    if (type == 'in') {de.currentScale *= 1.5;}
    if (type == 'out') {de.currentScale /= 1.4;}
    // correct currentTranslate so zooming is to the center of the viewport:

    var vp_width, vp_height;
    try {
        vp_width = de.viewport.width;
        vp_height = de.viewport.height;
    }
    catch (e) {
        // work around difficiency in moz ('viewport' property not implemented)
        vp_width = window.innerWidth;
        vp_height = window.innerHeight;
    }
    de.currentTranslate.x = vp_width/2 - ((de.currentScale/oldScale) * (vp_width/2 - oldTranslate.x));
    de.currentTranslate.y = vp_height/2 - ((de.currentScale/oldScale) * (vp_height/2 - oldTranslate.y));

}


function fnPan(type) {
    var de = document.documentElement;
    var ct = de.currentTranslate;
    var t = 150;
    if (type == 'right') ct.x += t;
    if (type == 'down') ct.y += t;
    if (type == 'left') ct.x -= t;
    if (type == 'up') ct.y -= t;
}


var gCurrentX,gCurrentY
var gDeltaX,gDeltaY
var gMouseDown=false
var gCurrentTranslate=document.documentElement.currentTranslate

function fnOnMouseDown(evt) {
    gCurrentX=gCurrentTranslate.x
    gCurrentY=gCurrentTranslate.y
    gDeltaX=evt.clientX
    gDeltaY=evt.clientY
    gMouseDown=true
    evt.target.ownerDocument.rootElement.setAttributeNS(null,"cursor","move")
}


function fnOnMouseUp(evt) {
    gMouseDown=false
    evt.target.ownerDocument.rootElement.setAttribute("cursor","default")
}


function fnOnMouseMove(evt) {
    var id
    if (gMouseDown) {
        gCurrentTranslate.x=gCurrentX+evt.clientX-gDeltaX
        gCurrentTranslate.y=gCurrentY+evt.clientY-gDeltaY
    }
}

