
var PI = 3.14159265358979323846;

// Class to handle converting between lat/lon and other custom coordinate
// systems e.g. Google or OSGB. Useful if you want to overlay lat/lon markers
// (e.g. from GeoRSS) on base maps using another coordinate system

// type can be:
//     GOOG - Google coord system (as used in Mapnik OSM)
//     OSGB - UK grid refs (requires jscoord from www.jstott.me.uk/jscoord)

function converter(type)
{
	this.type = type;
	this.customToNorm = customToNorm;
	this.normToCustom = normToCustom;
	this.customToNormBounds = customToNormBounds;
	this.normToCustomBounds = normToCustomBounds;
}

// convert between different coordinate schemes (e.g. google and osgb) and
// lat/lon
// partially borrowed from the main openstreetmap site
function customToNorm (custom)
{
	var lat_deg, lon_deg;
	switch(this.type)
	{
		case "GOOG": 
           lat_deg = (custom.lat / 20037508.34) * 180;
           lon_deg = (custom.lon / 20037508.34) * 180;
           lat_deg = 180/PI * 
		   		(2 * Math.atan(Math.exp(lat_deg * PI / 180)) - PI / 2);
		   break;

		case "OSGB": 
			var ll = new OSRef(custom.lon,custom.lat).toLatLng();		
			ll.OSGB36ToWGS84();
			lat_deg = ll.lat;
			lon_deg = ll.lng;
			break;
	}
	return new OpenLayers.LonLat(lon_deg,lat_deg);
}

function normToCustom(norm)
{
	var custLon, custLat; 

	switch(this.type)
	{
		case "GOOG":
         var a = 
			 Math.log(Math.tan((90+parseFloat(norm.lat))*PI / 360))/(PI / 180);
 		custLat = a * 20037508.34 / 180;
		custLon=norm.lon;
 		custLon = custLon * 20037508.34 / 180;
		break;

		case "OSGB":
			var ll = new LatLng(norm.lat,norm.lon);
			ll.WGS84ToOSGB36();
			var osgb = ll.toOSRef();
			custLon = osgb.easting;
			custLat = osgb.northing;
			break;
	}
	return new OpenLayers.LonLat(custLon,custLat);
}

function customToNormBounds(bounds)
{
	var bl = this.customToNorm
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
	var tr = this.customToNorm
				(new OpenLayers.LonLat(bounds.right,bounds.top));
	var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
	return newBounds;
}

function normToCustomBounds(bounds)
{
	var bl = this.normToCustom
				(new OpenLayers.LonLat(bounds.left,bounds.bottom));
	var tr = this.normToCustom
				(new OpenLayers.LonLat(bounds.right,bounds.top));
	var newBounds = new OpenLayers.Bounds(bl.lon,bl.lat,tr.lon,tr.lat);
	return newBounds;
}
