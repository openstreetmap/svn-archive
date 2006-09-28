// This is modified from the Tile/WFS in OpenLayers and hence is under the same
// BSD licence as OpenLayers.

// @require: OpenLayers/Tile.js
     /**
     * @class
     */
     OpenLayers.Tile.FreemapAnnotations = Class.create();
     OpenLayers.Tile.FreemapAnnotations.prototype =
       Object.extend( new OpenLayers.Tile(), {
     
         features: null,
     
     
         /**
         * @constructor
         *
         * @param {OpenLayers.Layer} layer
         * @param {OpenLayers.Pixel} position
         * @param {OpenLayers.Bounds} bounds
         * @param {String} url
         * @param {OpenLayers.Size} size
         */
         initialize: function(layer, position, bounds, url, size) {
             OpenLayers.Tile.prototype.initialize.apply(this, arguments);
            
             this.features = new Array();
         },
     
         /**
          *
          */
         destroy: function() {
             for(var i=0; i < this.features.length; i++) {
                 this.features[i].destroy();
             }
             OpenLayers.Tile.prototype.destroy.apply(this, arguments);
         },
     
         /**
         */
         draw:function() {
             this.loadFeaturesForRegion(this.requestSuccess);       
         },
     
        
         /** get the full request string from the ds and the tile params
         *     and call the AJAX loadURL().
         *
         *     input are function pointers for what to do on success and failure.
         *
         * @param {function} success
         * @param {function} failure
         */
         loadFeaturesForRegion:function(success, failure) {
	
			// Needed to avoid confusion about "this" - see Erik Uzureau's 
			// email (early July?)
			var success = success.bind(this);

            // Replace openlayers AJAX call with my own stuff, to avoid
            // going through the non-present proxy
            if(!this.loaded)
            {
                this.loaded=true;
                 var xmlHTTP;
                if(window.XMLHttpRequest)
                {
                    xmlHTTP = new XMLHttpRequest();
                    // Opera doesn't like the overrideMimeType()
                    if(!window.opera)
                        xmlHTTP.overrideMimeType('text/xml');
                }
                else if(window.ActiveXObject)
                    xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    

                xmlHTTP.open('GET',this.url,true);
    
                xmlHTTP.onreadystatechange =     function()
    
                {
                    if (xmlHTTP.readyState==4)
                    {
                        if(success!=null)
                            success (xmlHTTP);
                    }
                }

                xmlHTTP.send(''); // param required even if nothing there!
            }
         },
        
         /** Return from AJAX request
         *
         * @param {} request
         */

        // This parses the XML returned from the AJAX request 

         requestSuccess:function(xmlHTTP) {
			
			var featuresNode = xmlHTTP.responseXML.firstChild;
			var featureTags = featuresNode.getElementsByTagName("feature");

            // Create markers and add them to the parent layer...
            //for (var i=0; i < arr.length; i++)
			for (var i=0; i<featureTags.length; i++)
            {
                //var t = arr[i].split(",");
                //if(t[0] && t[1])
				var title=featureTags[i].getElementsByTagName("title")[0].
						firstChild.nodeValue;
				var desc=featureTags[i].getElementsByTagName("description")[0].
						firstChild.nodeValue;
				var lon=featureTags[i].getElementsByTagName("lon")[0].
						firstChild.nodeValue;
				var lat=featureTags[i].getElementsByTagName("lat")[0].
						firstChild.nodeValue;
				var id=featureTags[i].getElementsByTagName("id")[0].
						firstChild.nodeValue;
                {
                    var lonLat = new OpenLayers.LonLat (lon, lat);

					// Use a feature rather than a marker, as features have an
					// associated popup which is what we want.

					/*
                    this.layer.addMarker 
						( new OpenLayers.Marker 
							(lonLat, new OpenLayers.Icon
					('http://nick.dev.openstreetmap.org/images/amenity.png')));
					*/

					
					var data =  { icon: 
								new OpenLayers.Icon
					('http://nick.dev.openstreetmap.org/images/amenity.png'),
									popupContentHTML:
								'<h3>'+title+'</h3><p>'+desc+'</p>',
									popupSize:
								new OpenLayers.Size(320,200)
								};

					var f = new OpenLayers.Feature.FreemapAnnotations 
						(this.layer,lonLat,data);
					f.id=id;
					//this.features.append(f);
					

                }
            }
         },
     
         /** @final @type String */
         CLASS_NAME: "OpenLayers.Tile.FreemapAnnotations"
       }
     );
