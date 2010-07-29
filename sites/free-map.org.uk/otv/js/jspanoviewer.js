/*
 * JSPanoViewer.js / .css
 * Copyright (C) Bart van Andel, 2009
 * --
 * Implements a panorama viewer in javascript.
 *
 * Input : equirectilinear or cylindrical jpg file.
 * Output: rectilinear, pannini (vedutismo)
 */

 
// Math extensions

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

 
/*
 * Variables:
 *   xSrc, ySrc : coordinates in source image
 *   xDst, yDst : coordinates in destination (projected) image
 *   uSrc, vSrc : uniform coordinates in source image
 *   uDst, vDst : uniform coordinates in destination image
 *   wSrc, hSrc : width and height of source image
 *   wDst, hDst : width and height of destination image
 * Assumption: pixels have a dimension (are not infinitely small).
 * Then source image has coordinates (x,y) = ([0..width],[0..height]).
 * Or, otherwise, coordinates (u,v) = ([-0.5..0.5],[-0.5..0.5])
 * This corresponds to ([-hfov/2..hfov/2],[-vfov/2..vfov/2]).
 * So:
 *   xAngSrc = hfov * (xSrc    / wSrc - 0.5)
 *   xSrc    = wSrc * (xAngSrc / hfov + 0.5)
 * And:
 *   xDst    = wDst * tan(xAngSrc)
 *           = wDst * tan(hfov * (xSrc / wSrc - 0.5))
 *   xAngSrc = atan(xDst) / wDst
 *   xSrc    = wSrc * ((atan(xDst) / wDst) / hfov + 0.5
 * Or:
 *   xAngSrc = hfov    * uSrc
 *   uSrc    = xAngSrc / hfov
 * And:
 *   uDst    = tan(xAngSrc)
 *           = tan(hfov * uSrc)
 *   xAngSrc = atan(uDst)
 *   uSrc    = atan(uDst) / hfov
 *
 * Same for y dimension of course.
 */

function JSPanoViewer(opts) {
	var debug = false;	// Set to true to enable some debug information
	
	// Private variables (non-changeable after initialization)
	var containerId;	// id of pano container
	var container;		// pano container
	var imageUrl;		// url of source image
	var image;			// source image
	var slices;			// array with vertical image slices
	var wSlice;			// Width of each slice (in screen pixels)
	
	// Image parameters for source and destination image (screen):
	// width, height, horizontal/vertical field of view
	var wSrc, hSrc, hFovSrc, vFovSrc;
	var wDst, hDst, hFovDst, vFovDst;
	
	var angCenter;		// View angle
	
	var mode;
	var optShift, optScale, optMath, optPower;
	var optCircleSize;
	
	// Set default options
	this.setDefaults();
	// Supplied options override default options
	this.setOptions(opts);
	
	// Load CSS, if not already loaded
	if(!this.cssLoaded)
		this.loadCss();
	
	this.init();
	this.addControls();
	if(typeof(this.imageUrl) != 'undefined') {
		this.loadImage(this.imageUrl);
	}
}
JSPanoViewer.prototype = {
	cssLoaded      : false,
	MODE_UNDEFINED : -1,
	MODE_NORMAL    :  0,
	MODE_MIRROR1   : 11,
	MODE_MIRROR2   : 12,
	MODE_MIRROR3   : 13,
	MODE_PRESS1    : 21,
	MODE_PRESS2    : 22,
	MODE_PRESS3    : 23,
	MODE_PARABOLIC1: 31,
	MODE_PARABOLIC2: 32,
	MODE_PARABOLIC3: 33,
	MODE_WEIRD1    : 41,
	MODE_WEIRD2    : 42,
	MODE_WEIRD3    : 43,
	MODE_PANNINI   : 51,
	MODE_TEST      :999,
	defaults:{
		containerId   : 'panoContainer',
		wSlice        : 2,
		mode          : this.MODE_NORMAL,
		hFovSrc       : 360,
		hFovDst       : 120,
		optCircleSize : 1,
		cssWidth      : '500px',
		cssHeight     : '200px'
	},
	setDefaults:function() {
		this.setOptions(this.defaults);
	},
	setOptions:function(opts) {
		for(i in opts) {
			this[i] = opts[i];
		}
	},
	init:function() {
		// Prevent initializing more than once
		if(this.initComplete)
			return;
		
		this.container = document.getElementById(this.containerId);
		
		// Determine destination size
		this.wDst = this.container.clientWidth;
		this.hDst = this.container.clientHeight;
		
		// Determine vertical fields of view
		this.vFovDst = this.hFovDst * this.hDst / this.wDst; // todo: really? maybe dependent on curvature?
		
		// Create subcontainer for the image slices
		pano = document.createElement('div');
		pano.className = 'panoSlices';
		this.container.appendChild(pano);
		
		// Create divs for the image slices, and save the image slices for future reference
		var elem, slice, pano; 
		this.slices = [];
		for(x=0; x<this.wDst; x+=this.wSlice) {
			elem = document.createElement('div');
			elem.style.left   = x + 'px';
			elem.style.top    = 0 + 'px';
			elem.style.width  = this.wSlice + 'px';
			elem.style.height = this.hDst   + 'px';
			elem.className = 'panoSlice';
			slice = document.createElement('img');
			slice.uDst = x / this.wDst;
			this.slices.push(slice);
			elem.appendChild(slice);
			pano.appendChild(elem);
		}
		
		// Create overlay element to cover the slices
		if(!this.debug) {
			elem = document.createElement('div');
			elem.className = 'overlay';
			this.container.appendChild(elem);
		}
		
		this.initComplete = true;
	},
	loadImage:function(url, options) {
		// Start loading the image, and make loadImageComplete finish up after loading has completed
		this.imageUrl = url;
		this.image = new Image();
		// Set the onload property before loading the image, otherwise the function may never be called.
		// Confirmed for IE7.
		if(this.image.onload) {
			var self = this;
			this.image.onload = function(){self.loadImageComplete(options);};
			this.image.src = url;
		}
		else {
			this.image.src = url;
			this.loadImageComplete(options);
		}
	},
	loadImageComplete:function(options) {
		if(!this.image.onload && !this.image.complete) {
			var self = this;
			setTimeout(function(){self.loadImageComplete(options);},125);
			return;
		}
		this.wSrc = this.image.width;
		this.hSrc = this.image.height;
		this.setOptions(options);
		this.hFovSrc = (typeof(this.hFovSrc) != 'undefined') ? this.hFovSrc : 360;
		this.vFovSrc = this.hFovSrc * this.hSrc / this.wSrc;
		// View image center
		this.angCenter = this.hFovSrc / 2;
		
		this.setMode(this.mode);
		// Propagate to image slices
		for(i in this.slices) {
			this.slices[i].src = this.imageUrl;
		}
	},
	setMode:function(mode) {
		// Default value
		if(typeof(mode) == 'undefined') mode = this.MODE_UNDEFINED;
		this.mode = mode;
		
		// Apply options, which will be used as follows:
		// val = opts[0] + opts[1] * opts[2](val, opts[3])
		var opts;
		switch(mode) {
			case this.MODE_MIRROR1:		opts = [ 1,  2, Math.abs  ,   0]; break; //uDst = 2        * abs (uDst          ) + 1; break;
			case this.MODE_MIRROR2:		opts = [-3,  2, Math.abs  ,   0]; break; //uDst = 2        * abs (uDst          ) - 3; break;
			case this.MODE_MIRROR3:		opts = [ 0,  2, Math.abs  ,   0]; break; //uDst = 2        * abs (uDst          )    ; break;
			case this.MODE_PARABOLIC1:	opts = [ 1,  2, Math.powa ,   2]; break; //uDst = 2        * powa(uDst, 2       ) + 1; break;
			case this.MODE_PARABOLIC2:	opts = [-3,  2, Math.powa ,   2]; break; //uDst = 2        * powa(uDst, 2       ) - 3; break;
			case this.MODE_PARABOLIC3:	opts = [ 0,  2, Math.pows ,   2]; break; //uDst = 2        * pows(uDst, 2       )    ; break;
			case this.MODE_PRESS1:		opts = [ 1,  2, Math.powa , 1/2]; break; //uDst = 2        * powa(uDst, 1/2     ) + 1; break;
			case this.MODE_PRESS2:		opts = [-3,  2, Math.powa , 1/2]; break; //uDst = 2        * powa(uDst, 1/2     ) - 3; break;
			case this.MODE_PRESS3:		opts = [ 0,  2, Math.pows , 1/2]; break; //uDst = 2        * pows(uDst, 1/2     )    ; break;
			//case this.MODE_WEIRD1:		opts = [-1,  ?, Math.powa , 1/2]; break; //uDst = optScale * powa(uDst, 1/2     ) - 1; break;
			//case this.MODE_WEIRD2:		opts = [-1,  ?, Math.powa ,   ?]; break; //uDst = optScale * powa(uDst, optPower) - 1; break;
			case this.MODE_NORMAL :
			default:					opts = [ 0,  1, Math.ident,   0]; break; // do nothing
		}
		this.optShift = opts[0]; // uniform shift factor
		this.optScale = opts[1]; // uniform scale factor
		this.optMath  = opts[2]; // mathematical function
		this.optPower = opts[3]; // power (used in power functions)
		
		var preScale, scale, slice, uDst, uDst1, hBorder, aSrc, uSrc, xSrc;
		
		// Relative height at the image border, compared to the image center
		uDst1 = Math.tand(this.hFovDst/2)/2;
		hBorder = Math.sqrt(uDst1*uDst1 + this.optCircleSize*this.optCircleSize);
		
		preScale = (this.wDst / this.wSrc) * this.hFovSrc / this.hFovDst;
		
		for(i in this.slices) {
			slice = this.slices[i];
			
			// Transform coordinates from [0..1] to [-1..1] range
			uDst = 2 * slice.uDst - 1;
			// Apply view mode
			uDst = this.optShift + this.optScale * this.optMath(uDst, this.optPower);
			
			// Transform coordinates into range [-tan(hFovDst/2)..tan(hFovDst/2))]
			// (if view mode hasn't altered the range boundaries)
			uDst1 = uDst * Math.tand(this.hFovDst/2);
			scale   = preScale * Math.sqrt(uDst1*uDst1 + this.optCircleSize*this.optCircleSize) / hBorder;
			if(mode == this.MODE_WEIRD3) {
				scale = preScale * Math.tand(this.hFovDst/2) * (1+Math.abs(Math.pows(uDst, this.optPower))) / hBorder;
			}
			aSrc = Math.atand(uDst1);						// correct
			if(mode == this.MODE_PANNINI) {
				// See http://groups.google.com/group/hugin-ptx/browse_thread/thread/9acc6eb237a28c99
				/*
					filter panini (image in, float FoV: 1-360 (230.07), float pan: -180-180 (0.0)) 
					# Vedutismo / Panini effect 
					# Bruno Postle December 2008 
					# input is any 360 cylindrical image 
						maxphi=atan(pi/2); 
						newphi=FoV*pi/360/2; 
						scale=tan(newphi)/tan(maxphi); 
						phi=atan(x*scale*pi/W); 
						yscale=cos(phi)*cos(phi); 
						in(xy:[phi/pi*W+(W*pan/360),y*scale*yscale]) 
					end 
				*/
				var maxphi = Math.atan(Math.PI/2); 
				var newphi = this.hFovDst/2*Math.PI/360/2; 
				scale = Math.tan(newphi)/Math.tan(maxphi); 
				var phi = Math.atan(uDst*scale*Math.PI); 
				var yscale = Math.cos(phi)*Math.cos(phi); 
				aSrc = phi/Math.PI*360;
				scale = 1/2 * preScale / (scale * yscale);
			}
			// Transform angle into relative coordinate (range [0..1])
			uSrc = Math.wrap(aSrc / 360, 0, 1);
			// Transform relative coordinate into image space coordinate (range [0..wSrc])
			xSrc = this.wSrc * uSrc;						// correct
			// Propagate values to image slices
			slice.uDst1 = uDst1;
			slice.aSrc = aSrc;
			slice.uSrc = uSrc;
			slice.scale = scale;
			// Set appropriate style (rounded by hand to ensure proper display without rigs)
			slice.style.width  = (scale * this.wSrc) + 'px';
			slice.style.height = (2*Math.floor(scale/2 * this.hSrc)) + 'px';
			slice.style.top    = Math.floor((this.hDst - this.hSrc * scale)/2) + 'px';
		}
		this.updateImages();
	},
	shiftPano:function(degrees) {
		// constrain shift
		this.angCenter = Math.wrap(this.angCenter + Math.limit(degrees, -180, 180), 0, 360);
		this.updateImages();
	},
	updateImages:function() {
		var slice, aSrc, xSrc;
		for(i in this.slices) {
			slice = this.slices[i];
			// Update image shift and scale
			aSrc = slice.aSrc + this.angCenter;
			xSrc = this.wSrc * (360/this.hFovSrc) * Math.wrap(aSrc / 360, 0, 1);
			slice.style.left = (-(slice.scale * xSrc)) + 'px';
			if(this.debug) {
				slice.parentNode.title =
				slice.title =
					'aSrc: '  + slice.aSrc + '\n' +
					'uSrc: '  + slice.uSrc + '\n' +
					'uDst: '  + slice.uDst + '\n' +
					'scale: ' + slice.scale + '\n';
			}
		}
	},
	addControls:function(controller) {
		// Default: add mouse controls directly to pano container
		if(typeof(controller) == 'undefined') controller = this.container;
		
		var self = this;
		
		controller.onmousedown = function(event) {
			if(typeof(event) == 'undefined') event = window.event;
			controller.mouseDown = true;
			controller.lastX = event.clientX;
			controller.lastY = event.clientY;
			return false; // Prevent default behaviour
		};
		controller.onmouseup = function(event) {
			controller.mouseDown = false;
			return false; // Prevent default behaviour
		};
		controller.onmousemove = function(event) {
			if(typeof(event) == 'undefined') event = window.event;
			if(controller.mouseDown) {
				var degrees = (controller.lastX - event.clientX) / 5;
				self.shiftPano(degrees);
				controller.lastX = event.clientX;
				controller.lastY = event.clientY;
			}
			return false; // Prevent default behaviour
		};
	},
	loadCss:function() {
		var cssDimension = "";
		if(typeof(this.cssWidth != 'undefined')) cssDimension += "width: " + this.cssWidth + ";";
		if(typeof(this.cssHeight != 'undefined')) cssDimension += "height: " + this.cssHeight + ";";
		var cssText = " \
		div.panoViewer { \
			position: relative; \
			" + cssDimension + " \
			overflow: hidden; \
		} \
		div.panoSlices { \
			position: absolute; \
			left: 0px; \
			top: 0px; \
			width: 100%; \
			height: 100%; \
			background: none; \
		} \
		div.panoSlices div { \
			position: absolute; \
			background: black; \
			overflow: hidden; \
		} \
		div.panoSlices img { \
			position: absolute; \
		} \
		div.overlay { \
			position: absolute; \
			left: 0px; \
			top: 0px; \
			width: 100%; \
			height: 100%; \
			border: none; \
			background: none; \
			z-index: 10; \
		} \
		";
		
		var head = document.getElementsByTagName('head')[0];
		var style = document.createElement('style');
		style.setAttribute("type", "text/css");
		if(style.styleSheet) { // IE
			style.styleSheet.cssText = cssText;
		} else { // w3c
			style.appendChild(document.createTextNode(cssText));
		}		
		head.appendChild(style);
		
		this.cssLoaded = true;
	}
};
