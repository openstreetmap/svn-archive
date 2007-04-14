var map;


function init()
{
	map = new OpenLayers.Map('map',
	{ maxExtent: new OpenLayers.Bounds (0,0,599999,999999),
	  maxResolution : 8, 
	  units: 'meters' }	
	);
	
					        
	var npeosm = new OpenLayers.Layer.WMS( "freemap-npe", 
		"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
		{'layers':'npe,osm', 'buffer':1 } );
	map.addLayer(npeosm);

	var npe = new OpenLayers.Layer.WMS( "npe only", 
		"http://nick.dev.openstreetmap.org/openpaths/freemap.php",
		{'layers': 'npe','buffer':1} );

	map.addLayer(npe);

	map.setCenter(new OpenLayers.LonLat(easting,northing));

	map.addControl (new OpenLayers.Control.LayerSwitcher());
}


function placeSearch()
{
	var loc = document.getElementById('search').value;
	ajax("http://nick.dev.openstreetmap.org/openpaths/geocoder_ajax.php", 
			"place="+loc+"&country=uk", searchCallback);
}

function ajax(URL,data,callback,addData)
{
    var name, xmlHTTP;

    if(window.XMLHttpRequest)
    {
        xmlHTTP = new XMLHttpRequest();
        // Opera doesn't like the overrideMimeType()
        if(!window.opera)
            xmlHTTP.overrideMimeType('text/xml');
    }
    else if(window.ActiveXObject)
        xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
    

    xmlHTTP.open('POST',URL,true);
    xmlHTTP.setRequestHeader('Content-Type',
    'application/x-www-form-urlencoded');
    
    xmlHTTP.onreadystatechange =     function()
    
    {
        if (xmlHTTP.readyState==4)
        {
            if(callback!=null)
            {
                callback (xmlHTTP, addData);
            }
			else
			{
				//alert(xmlHTTP.responseText);
			}
        }
    }

    xmlHTTP.send(data); // param required even if nothing there!
    
}

function searchCallback(xmlHTTP, addData)
{
	var en = xmlHTTP.responseText.split(",");
	if(en[0]!="0" && en[1]!="0")
	{
		map.setCenter(new OpenLayers.LonLat(parseInt(en[0]), parseInt(en[1])));
	}
	else
	{
		alert("That is not a known UK place!");
	}
}
