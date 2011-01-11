
OpenLayers.Format.FmapGeoRSS = OpenLayers.Class (OpenLayers.Format.GeoRSS,
    {

        initialize: function(options)
        {
            OpenLayers.Format.GeoRSS.prototype.initialize.apply
                (this,arguments);
        },

        createFeatureFromItem: function (item)
        {
            var f=
                OpenLayers.Format.GeoRSS.prototype.
				createFeatureFromItem.apply (this,arguments);
            f.attributes.hasPhoto = 
                    (this.getChildValue
                    (item,"http://www.georss.org/georss","featuretypetag")
					== "photo" ) ? 1:0;
            return f;
        },


        CLASS_NAME: "OpenLayers.Format.FmapGeoRSS"

    }
);
