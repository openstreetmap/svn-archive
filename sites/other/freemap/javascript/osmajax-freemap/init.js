var osmajax;

function init()
{
	var tpURL = (trackid==null) ?
		'http://www.free-map.org.uk/freemap/common/tp.php':
		'http://www.free-map.org.uk/freemap/common/tp.php?trackid='+trackid;

    var map = new OpenLayers.Map('map',
            { maxExtent:new OpenLayers.Bounds(-700000,6000000,200000,8100000),
            resolutions: [2.5, 5,10],
            tileSize : new OpenLayers.Size(500,500),
            units: 'meters' }
    );
    
    var mapnik = new OpenLayers.Layer.WMS("Freemap/Mapnik",
                "http://www.free-map.org.uk/cgi-bin/render",
                {buffer: 1} );

	var tp = new OpenLayers.Layer.WMS("trackpoints",tpURL,
                {buffer: 1} );

    //map.addLayer(mapnik);
    map.addLayer(tp);
            
    map.setCenter(new OpenLayers.LonLat(easting,northing));
    map.addControl(new OpenLayers.Control.LayerSwitcher());

    osmajax = new Osmajax(map);
    osmajax.activate();
}

function placeSearch()
{
    var loc = document.getElementById('search').value;
    ajaxrequest    
            ("http://www.free-map.org.uk/freemap/common/geocoder_ajax.php", 
            "place="+loc+"&country=uk", searchCallback);
}

function searchCallback(xmlHTTP, addData)
{
	alert(xmlHTTP.responseText);
    var latlon = xmlHTTP.responseText.split(",");
    if(latlon[0]!="0" && latlon[1]!="0")
    {
        var ll2 = new OpenLayers.LonLat
                (parseFloat(latlon[1]),parseFloat(latlon[0]));
        osmajax.setCentre(ll2);
    }
    else
    {
        alert("That place is not in the database");
    }
}

