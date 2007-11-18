// OSMNode class 
// Represents an OSM node

OpenLayers.OSMNode = OpenLayers.Class.create();

OpenLayers.OSMNode.prototype = 
    OpenLayers.Class.inherit (OpenLayers.GeometriedOSMItem, {

        initialize: function() {
            OpenLayers.GeometriedOSMItem.prototype.initialize.apply
                    (this,arguments);
            //OpenLayers.OSMItem.prototype.initialize.apply(this);
        },

        setGeometry: function(g) {
            if (g instanceof OpenLayers.Geometry.Point) {
                geometry=g;
            }
        },

        toXML : function() { 
            var cvtr=new converter("OSGB");
            var a = new OpenLayers.LonLat(this.geometry.x,
                            this.geometry.y);
            var b=cvtr.customToNorm(a);
            var xml = "<node id='" + (this.osmid>0 ? this.osmid : 0)  +
                        "' lat='" + b.lat + "' lon='" + 
                        b.lon + "'>";

            xml += this.tagsToXML();
            xml += "</node>";    
            return xml;
        },

        // Is this node a point of interest?
        // If it contains tags other than 'created_by', it is considered so.
        isPOI:  function() {
            if(this.tags) {
                for(k in this.tags) {
                    if (k != 'created_by' && k != 'source' && k != 'note' &&
                        !(k=='class' && this.tags[k]=='node')) {
                        return true;
                    }
                }
            }
            return false;
        },

        isPlace: function() {
                return  this.tags && this.tags['place'] && 
                        (this.tags['place']=='village' || 
                         this.tags['place']=='town' ||
                         this.tags['place']=='city' ||
                         this.tags['place']=='hamlet' ||
                         this.tags['place']=='suburb') ;
        },

        CLASS_NAME : "OpenLayers.OSMNode"
});
            
