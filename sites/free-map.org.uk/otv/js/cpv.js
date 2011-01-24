
/*
 * CanvasPanoViewer
 * 
 * <canvas> based panorama viewer.
 *
 * This is partly based on:
 *
 * JSPanoViewer.js / .css, which is
 * Copyright (C) Bart van Andel, 2009
 * --
 * Implements a panorama viewer in javascript.
 *
 * JSPanoViewer is available at http://code.google.com/p/jspanoviewer/
 *
 * The original code by Bart van Andel used <div>s with no <canvas> but has
 * a lot more options involving reprojection.
 * --
 * New sections (c) nickw 2010
 * 24/06/10 Note that this does not yet do any reprojection. I only partially 
 * understand the maths in the original JSPanoViewer (basically, I understand
 * what's happening with horizontal reprojection, but not with vertical)
 * and don't want to blindly convert without understanding; so my strategy has 
 * been to get a simple, bare-bones canvas pano viewer first and then deal 
 * with the hairy maths when I get to overlaying OSM data on it.
 *
 */

 
// Math extensions - all (c) Bart van Andel

// Identity function
Math.ident = function(val) { return val; };
// Return sign of value (1 for positive or zero, -1 for negative)
Math.sign  = function(val) { return (val < 0) ? -1 : 1; };
// Convert degrees to radians
Math.d2r   = function(deg) { return deg / 180.0 * Math.PI; };
// Convert radians to degrees
Math.r2d   = function(rad) { return rad * 180.0 / Math.PI; };
// Tangens (degrees)
Math.tand  = function(deg) { return Math.tan(Math.d2r(deg)); };
// Inverse tangens (degrees)
Math.atand = function(val) { return Math.r2d(Math.atan(val)); };
// Sine (degrees)
Math.sind  = function(deg) { return Math.sin(Math.d2r(deg)); };
// Inverse sine (degrees)
Math.asind = function(val) { return Math.r2d(Math.asin(val)); };
// Cosine (degrees)
Math.cosd  = function(deg) { return Math.cos(Math.d2r(deg)); };
// Inverse cosine (degrees)
Math.acosd = function(val) { return Math.r2d(Math.acos(val)); };
// Computes the powerth power of the absolute value of val.
Math.powa  = function(val, power) { return Math.pow(Math.abs(val), power); };
// Computes the signed powerth power of the absolute value of val.
Math.pows  = function(val, power) { return Math.sign(val) * Math.pow(Math.abs(val), power); };
// Limit value between min and max (cut off)
Math.limit = function(val, min, max) { return Math.max(min, Math.min(val, max)); };
// Limit value between min and max (wrap around)
Math.wrap  = function(val, min, max) {
    range = max - min;
    val = min + (val-min) % range;
    if     (val < min) return val + range;
    else if(val > max) return val - range;
    else               return val;
};

var CanvasPanoViewer = Class.create ( {

    initialize: function(opts)
    {
        // Set default options
        this.setDefaults();
        // Supplied options override default options
        this.setOptions(opts);
    
        this.doInit();

        this.panoOrientation = 0;
        if(typeof(this.imageUrl) != 'undefined') 
        {
            this.loadImage(this.imageUrl);
        }
    },

    setDefaults:function() 
    {
        this.defaults = {
        canvasId   : 'panoCanvas',
        wSlice        : 2,
        hFovSrc       : 360,
        hFovDst       : 120,
        optCircleSize : 1,
        statusElement: 'status',
        dirs: new Array(),
        bearing: 0,
        showStatus : 0
        };
        this.setOptions(this.defaults);
    },

    setOptions:function(opts) {
        for(i in opts) {
            this[i] = opts[i];
        }
    },

    doInit:function() 
    {
        // Prevent initializing more than once
        if(this.initComplete)
            return;
        
        this.canvas = document.getElementById(this.canvasId);
        this.ctx = this.canvas.getContext('2d');
        
        // Determine destination size
        this.wDst = parseInt(this.canvas.width);
        this.hDst = parseInt(this.canvas.height);
   
        
        this.viewportX = 0;

        this.initComplete = true;
    },

    loadImage:function(url, options) 
    {
		// disable controls until done
		this.removeControls();

        // Start loading the image, and make loadImageComplete finish up after 
        // loading has completed
        this.imageUrl = url;
        this.image = new Image();
        // Set the onload property before loading the image, 
        // otherwise the function may never be called.
        // Confirmed for IE7.
        if(this.image.onload) 
        {
            var self = this;
            this.image.onload = function(){self.loadImageComplete(options);};
            this.image.src = url;
        }
        else 
        {
            this.image.src = url;
            this.loadImageComplete(options);
        }
    },

    loadImageComplete:function(options) 
    {
        if(!this.image.onload && !this.image.complete) 
        {
            var self = this;
            setTimeout(function(){self.loadImageComplete(options);},125);
            return;
        }
		// only allow controlling when complete
        this.addControls();
        this.wSrc = this.image.width;
        this.hSrc = this.image.height;
        this.setOptions(options);
        this.hFovSrc = 
            (typeof(this.hFovSrc) != 'undefined') ? this.hFovSrc : 360;
        // View image center
        this.angCenter = this.hFovSrc / 2;
    
        this.xStretch=(this.wDst*this.hFovSrc) / (this.wSrc*this.hFovDst);
        this.yStretch = this.hDst / this.hSrc;

        // the width of the viewport in the source image
        this.wDstToSrc = (this.wDst/this.xStretch);

        // what's the width of an input slice of defined width in the output?
        this.wSliceDst = this.wSlice*this.xStretch;

        // set the direction accordingly
        this.updateImages();
    },

    // originally used degrees change, not pixels change
    shiftPano:function(pixels) 
    {
        // constrain shift
        /*
        this.angCenter = Math.wrap
            (this.angCenter + Math.limit(degrees, -180, 180), 0, 360);
        */
        // viewportX is the viewport position in the *output* (projected)
        // world, this is because the mouse position will be with respect to
        // the output
        this.viewportX = Math.wrap
            (this.viewportX + 
                Math.limit(pixels,-80,80),
                0, this.wSrc*this.xStretch);
        //this.viewportX = 0;
        this.updateImages();
    },

    updateImages:function() 
    {
        // draw according to the current viewport


        // convert viewport x to the x coord in the source image
        var viewportXSrc=this.viewportX / this.xStretch;
        var xPxDst = 0;


        // do each slice
        // slices aren't strictly necessary just now but are done for
        // futureproofing if any reprojection is done
        var xPxSrc, xPxSrcSliceEnd;
        for(var vpXPxSrc=0; vpXPxSrc<this.wDstToSrc; vpXPxSrc+=this.wSlice) 
        { 
            // position of end of slice
            xPxSrcSliceEnd=Math.wrap(vpXPxSrc+viewportXSrc+this.wSlice,
                                    0,this.wSrc);
            xPxSrc = Math.wrap(vpXPxSrc+viewportXSrc,0, this.wSrc);


            // Does the current slice contain the extreme right of the source
            // image plus the extreme left? If so, draw the slice in two parts.
            if(xPxSrc>xPxSrcSliceEnd) 
            {
                this.ctx.drawImage(this.image,xPxSrc,0,this.wSrc-xPxSrc,
                    this.hSrc,xPxDst,0,this.wSliceDst+1,this.hDst);
                this.ctx.drawImage(this.image,0,0,xPxSrcSliceEnd,
                    this.hSrc,xPxDst,0,this.wSliceDst+1,this.hDst);
            } else 
            {
                // nb output width this.wSliceDst+1 to avoid gaps from
                // rounding errors
                this.ctx.drawImage(this.image,xPxSrc,0,this.wSlice,
                    this.hSrc,xPxDst,0,this.wSliceDst+1,this.hDst);
            }

            xPxDst+=this.wSliceDst;
            this.drawDirections();
        }
    },

    drawDirections: function() 
    {
        if(this.dirs==null) return;
        var viewportBearing=this.getViewportBearing();
        for(var i=0; i<this.dirs.length; i++) 
        {
            var d2 = (viewportBearing>360-this.hFovDst &&
                this.dirs[i]+360 < viewportBearing+this.hFovDst) ?
                this.dirs[i]+360: this.dirs[i];
            if((d2>=viewportBearing && d2<=viewportBearing+this.hFovDst))
            {
                dirX = ((d2-viewportBearing)/this.hFovDst) * this.wDst;
                this.ctx.fillStyle = '#ffff00';
                this.ctx.strokeStyle = '#ffff00';
                this.ctx.beginPath();
                this.ctx.moveTo(dirX-10,this.hDst-10);
                this.ctx.lineTo(dirX+10,this.hDst-10);
                this.ctx.lineTo(dirX,this.hDst-30);
                this.ctx.closePath();
                this.ctx.stroke();
            }
        }
    },

    getClosestBearing: function() 
    {
        var closest = -1, lastDist = 999;
        var viewportXSrc = this.viewportX/this.xStretch;
        var viewportCtrAng =  Math.wrap
            ((viewportXSrc * (this.hFovSrc/this.wSrc)) + this.hFovDst/2,0,360);
        var viewportBearing = Math.wrap(this.bearing+(viewportCtrAng-180),
                                    0,360);
        for(var i=0; i<this.dirs.length; i++) {
            var dist = Math.abs(this.dirs[i] - viewportBearing);
            if(dist < lastDist && dist < 30) {
                lastDist = dist;
                closest = i;
            }
        }
        return closest;
    },

    setViewportBearing: function(desiredDir) 
    {
        desiredDir -= this.bearing-180;
        desiredDir = Math.wrap(desiredDir,0,360);
        var viewportXSrc = desiredDir * (this.wSrc/this.hFovSrc);
        this.viewportX = viewportXSrc * this.xStretch;
    },

    getViewportBearing: function()
    {
        var viewportXSrc=this.viewportX/this.xStretch;
        var viewportAng = Math.wrap
            ( (viewportXSrc * (this.hFovSrc/this.wSrc)), 0, 360);
        return Math.wrap(this.bearing+(viewportAng-180),0,360);
    },

    addControls:function(controller) 
    {
        // Default: add mouse controls directly to pano canvas
        if(typeof(controller) == 'undefined') controller = this.canvas;
        
        var self = this;
        
        controller.onmousedown = function(event) 
        {
            if(typeof(event) == 'undefined') event = window.event;
            controller.mouseDown = true;
            controller.lastX = event.clientX;
            controller.lastY = event.clientY;
            return false; // Prevent default behaviour
        };
        controller.onmouseup = function(event) 
        {
            controller.mouseDown = false;
            return false; // Prevent default behaviour
        };
        controller.onmousemove = function(event) 
        {
            if(typeof(event) == 'undefined') event = window.event;
            if(controller.mouseDown) {
                //var degrees = (controller.lastX - event.clientX) / 5;
                var pixDiff = controller.lastX - event.clientX;
                self.shiftPano(pixDiff);
                controller.lastX = event.clientX;
                controller.lastY = event.clientY;
            }
            return false; // Prevent default behaviour
        };

    },

	removeControls: function (controller)
	{
		if(typeof(controller)=='undefined') controller=this.canvas;
		controller.onmousedown = controller.onmouseup = controller.onmousemove=
			null;
	},


    status: function(msg) 
    {
        if(this.showStatus!=0) 
        {
            document.getElementById(this.statusElement).innerHTML = msg;
        }
    },
} );
