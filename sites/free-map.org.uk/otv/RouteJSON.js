OpenLayers.Format.RouteJSON = OpenLayers.Class(OpenLayers.Format.JSON, {
    
    initialize: function(options)
    {
        OpenLayers.Format.JSON.prototype.initialize.apply(this,[options]);
    },

    read: function (json)
    {
         //alert('reading the json: ' + json);

        // foreach ID
        // find feature with that id
        // add the geometry of that feature to the geometry
        // return a feature with the current geometry

        var features = new Array();
        var f,g,p;
        var data = json.evalJSON();
        for (routeID in data)
        {
            f = new OpenLayers.Feature.Vector();
            g = new OpenLayers.Geometry.LineString();
            for(var i=0; i<data[routeID].length; i++)
            {
                p = new OpenLayers.Geometry.Point
                    (data[routeID][i].lon,
                    data[routeID][i].lat);
                p.photo_id = data[routeID][i].id;
                g.addPoint (p);
                //alert('p=' + p.x+','+p.y);
            }
            f.geometry=g;
            f.fid=routeID;
            features.push(f);
        }

        //alert('read: ' + features.length + ' features from the json');
        return features;
                
    },

    CLASS_NAME: "OpenLayers.Format.RouteJSON"

});
