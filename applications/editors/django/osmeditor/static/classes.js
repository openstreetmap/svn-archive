        var OSMEditor = {}
        OSMEditor.HashControl = OpenLayers.Class(OpenLayers.Control.Permalink, {
            updateUrl: true,
            updateLink: function() {
                var base = this.base || "" ; 
                var params = this.createParams();
                delete params.layers;
                var href = OpenLayers.Util.getParameterString(params);
                if (href && href != this.lastHref) {
                    var s = "m-" + href;
                    if (this.updateUrl) {
                        document.location.hash= s;
                    }
                    this.element.href = this.base + "#" + s;
                    this.lastHref = href;
                }
            },
    draw: function() {
        OpenLayers.Control.prototype.draw.apply(this, arguments);
          
        if (!this.element) {
            this.div.className = this.displayClass;
            this.element = document.createElement("a");
            this.element.innerHTML = OpenLayers.i18n("permalink");
            this.element.href="";
            this.div.appendChild(this.element);
        }
        this.map.events.on({
            'moveend': this.updateLink,
            'changelayer': this.updateLink,
            scope: this
        });
        
        // Make it so there is at least a link even though the map may not have
        // moved yet.
        this.updateLink();
        
        return this.div;
    }
        });
        OSMEditor.HashParser = OpenLayers.Class(OpenLayers.Control.ArgParser, {
            setMap: function(map) {
                OpenLayers.Control.prototype.setMap.apply(this, arguments);
                    var hstr = "?"+document.location.hash.substr(3, document.location.hash.length - 1);
                    var args = OpenLayers.Util.getParameters(hstr);
                    // Be careful to set layer first, to not trigger unnecessary layer loads
                    if (args.layers) {
                        this.layers = args.layers;
    
                        // when we add a new layer, set its visibility 
                        this.map.events.register('addlayer', this, 
                                                 this.configureLayers);
                        this.configureLayers();
                    }
                    if (args.lat && args.lon) {
                        this.center = new OpenLayers.LonLat(parseFloat(args.lon),
                                                            parseFloat(args.lat));
                        if (args.zoom) {
                            this.zoom = parseInt(args.zoom);
                        }
    
                        // when we add a new baselayer to see when we can set the center
                        this.map.events.register('changebaselayer', this, 
                                                 this.setCenter);
                        this.setCenter();
                    }
            }
        });

    

