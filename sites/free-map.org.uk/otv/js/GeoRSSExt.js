
OpenLayers.Format.GeoRSSExt = OpenLayers.Class (OpenLayers.Format.GeoRSS,
    {

        initialize: function(options)
        {
            OpenLayers.Format.GeoRSS.prototype.initialize.apply
                (this,arguments);
        },

        createFeatureFromItem: function (item)
        {
            var f=
                OpenLayers.Format.GeoRSS.prototype.createFeatureFromItem.apply
                    (this,arguments);
            f.attributes.direction=
                    this.getChildValue
                    (item,"http://www.w3.org/2003/01/geo/wgs84_pos#","dir");
            f.attributes.isPano =  1;
			/*
                    (this.getChildValue
                    (item,"http://www.georss.org/georss","featuretypetag") == 
                    "panorama" ) ? 1:0;
			*/
            return f;
        },


        CLASS_NAME: "OpenLayers.Format.GeoRSSExt"

    }
);
